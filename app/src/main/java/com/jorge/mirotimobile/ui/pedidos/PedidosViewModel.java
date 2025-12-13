package com.jorge.mirotimobile.ui.pedidos;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jorge.mirotimobile.Retrofit.ApiService;
import com.jorge.mirotimobile.Retrofit.RetrofitClient;
import com.jorge.mirotimobile.localdata.SessionManager;
import com.jorge.mirotimobile.model.CrearDetallePedidoRequest;
import com.jorge.mirotimobile.model.CrearPedidoRequest;
import com.jorge.mirotimobile.model.DetallePedidoInfoDTO;
import com.jorge.mirotimobile.model.PedidoDTO;
import com.jorge.mirotimobile.model.Plato;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PedidosViewModel extends AndroidViewModel {

    private final MutableLiveData<List<PedidoDTO>> pedidos = new MutableLiveData<>();
    private final MutableLiveData<List<PedidoDTO>> historialPedidos = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<String> mensajeError = new MutableLiveData<>();

    private final SessionManager session;
    private final ApiService api;
    private boolean pedidoPendienteLocal;
    private PedidoDTO pedidoLocal;
    private final MutableLiveData<Boolean> navegarASeguimiento = new MutableLiveData<>(false);

    public PedidosViewModel(@NonNull Application application) {
        super(application);
        session = new SessionManager(application.getApplicationContext());
        api = RetrofitClient.getClient(application.getApplicationContext()).create(ApiService.class);
        restorePedidoFromPrefs();
    }

    public LiveData<List<PedidoDTO>> getPedidos() {
        return pedidos;
    }

    public LiveData<List<PedidoDTO>> getHistorialPedidos() {
        return historialPedidos;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getMensajeError() {
        return mensajeError;
    }

    public LiveData<Boolean> getNavegarASeguimiento() {
        return navegarASeguimiento;
    }

    public void clearNavegarASeguimiento() {
        navegarASeguimiento.setValue(false);
    }

    public void cancelarPedidoLocal() {
        pedidoPendienteLocal = false;
        pedidoLocal = null;
        session.clearPendingPedido(); // FIX: drop persisted carrito when user cancels.
        publicarPedidoActivo(null);
    }

    private void restorePedidoFromPrefs() {
        pedidoPendienteLocal = session.isPedidoPendientePersisted();
        pedidoLocal = session.getPendingPedido();
        if (pedidoPendienteLocal && pedidoLocal != null && pedidoLocal.getDetalles() != null
                && !pedidoLocal.getDetalles().isEmpty()) {
            // FIX: reload persisted carrito when the process restarts.
            publicarPedidoActivo(pedidoLocal);
        } else if (pedidoPendienteLocal) {
            pedidoPendienteLocal = false;
            pedidoLocal = null;
            session.clearPendingPedido();
            pedidos.postValue(Collections.emptyList());
        }
    }

    public void cargarMisPedidos() {
        if (pedidoPendienteLocal) {
            loading.postValue(false);
            return;
        }

        loading.postValue(true);
        mensajeError.postValue(null);

        String token = session.getToken();
        if (token == null || token.isEmpty()) {
            loading.postValue(false);
            mensajeError.postValue("Sesión inválida.");
            return;
        }

        api.obtenerMisPedidos().enqueue(new Callback<List<PedidoDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<PedidoDTO>> call, @NonNull Response<List<PedidoDTO>> response) {
                loading.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    // FIX: keep the local carrito state instead of clearing it during refresh.
                    List<PedidoDTO> lista = response.body();
                    historialPedidos.postValue(lista);
                    PedidoDTO activo = elegirPedidoActivo(lista);
                    if (activo != null) {
                        publicarPedidoActivo(activo);
                    } else {
                        pedidos.postValue(Collections.emptyList());
                    }
                } else {
                    mensajeError.postValue("No se pudieron cargar tus pedidos.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<PedidoDTO>> call, @NonNull Throwable t) {
                loading.postValue(false);
                mensajeError.postValue("Error de conexión: " + t.getMessage());
            }
        });
    }

    private PedidoDTO elegirPedidoActivo(List<PedidoDTO> lista) {
        if (lista == null || lista.isEmpty()) return null;
        List<PedidoDTO> activos = new ArrayList<>();
        for (PedidoDTO pedido : lista) {
            String estado = pedido.getEstado();
            if (estado == null) continue;
            String normalized = estado.trim().toLowerCase();
            if (!normalized.equals("completado") && !normalized.equals("cancelado")) {
                activos.add(pedido);
            }
        }
        if (activos.isEmpty()) return null;
        activos.sort((a, b) -> {
            LocalDateTime fechaA = parseFecha(a.getFechaHora());
            LocalDateTime fechaB = parseFecha(b.getFechaHora());
            if (fechaA == null || fechaB == null) return 0;
            return fechaB.compareTo(fechaA);
        });
        return activos.get(0);
    }

    private LocalDateTime parseFecha(String iso) {
        if (iso == null || iso.isEmpty()) return null;
        try {
            return LocalDateTime.parse(iso, DateTimeFormatter.ISO_DATE_TIME);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    public void agregarPlatoAlDetalle(Plato plato) {
        if (plato == null) return;

        Log.d("carrito", "ANTES agregar -> pedidoLocal=" + pedidoLocal +
                " pendiente=" + pedidoPendienteLocal);

        if (pedidoLocal != null && pedidoPendienteLocal) {
            // FIX: keep using the pending pedidoLocal instead of recreating it.
        } else {
            Log.d("carrito", "CREANDO PEDIDO NUEVO");
            pedidoLocal = new PedidoDTO();
            pedidoLocal.setDetalles(new ArrayList<>());
            pedidoLocal.setId(0);
        }
        if (pedidoLocal.getDetalles() == null) {
            pedidoLocal.setDetalles(new ArrayList<>());
        }
        PedidoDTO pedido = pedidoLocal;

        DetallePedidoInfoDTO detalle = buscarDetallePorNombre(pedido.getDetalles(), plato.getNombre());
        if (detalle == null) {
            detalle = new DetallePedidoInfoDTO();
            detalle.setPlato(plato.getNombre());
            detalle.setCantidad(1);
            detalle.setSubtotal(plato.getPrecioVenta());
            detalle.setImagenUrl(plato.getImagenUrl());
            detalle.setPlatoId(plato.getId());
            pedido.getDetalles().add(detalle);
        } else {
            detalle.setCantidad(detalle.getCantidad() + 1);
            detalle.setSubtotal(detalle.getSubtotal() + plato.getPrecioVenta());
        }

        pedido.setTotal(calcularTotal(pedido.getDetalles()));
        pedidoPendienteLocal = true;
        session.savePendingPedido(pedido);
        session.savePedidoPendienteFlag(true); // FIX: keep the persisted carrito in sync.
        List<DetallePedidoInfoDTO> detalles = pedido.getDetalles();
        int size = detalles != null ? detalles.size() : 0;
        Log.d("CARRITO", "pedidoLocal size: " + size);
        Log.d("carrito", "DESPUES agregar -> size=" + pedidoLocal.getDetalles().size());
        if (!pedido.getDetalles().isEmpty()) {
            publicarPedidoActivo(pedido);
        } else {
            publicarPedidoActivo(null);
        }
    }

    private DetallePedidoInfoDTO buscarDetallePorNombre(List<DetallePedidoInfoDTO> detalles, String nombrePlato) {
        if (detalles == null || detalles.isEmpty() || nombrePlato == null) return null;
        for (DetallePedidoInfoDTO detalle : detalles) {
            if (nombrePlato.equalsIgnoreCase(detalle.getPlato())) {
                return detalle;
            }
        }
        return null;
    }

    private double calcularTotal(List<DetallePedidoInfoDTO> detalles) {
        if (detalles == null || detalles.isEmpty()) return 0;
        double total = 0;
        for (DetallePedidoInfoDTO detalle : detalles) {
            total += detalle.getSubtotal();
        }
        return total;
    }

    public void confirmarPedido() {
        if (!pedidoPendienteLocal || pedidoLocal == null) return;

        List<DetallePedidoInfoDTO> detalles = pedidoLocal.getDetalles();
        if (detalles == null || detalles.isEmpty()) {
            mensajeError.postValue("No se pudo confirmar el pedido.");
            return;
        }

        if (pedidoLocal.getId() > 0) {
            navegarASeguimiento.setValue(true); // FIX: emit navigation event instead of using state.
            return;
        }

        List<CrearDetallePedidoRequest> detallesRequeridos = new ArrayList<>();
        for (DetallePedidoInfoDTO detalle : detalles) {
            if (detalle.getPlatoId() <= 0) {
                mensajeError.postValue("No se pudo confirmar el pedido.");
                return;
            }
            detallesRequeridos.add(new CrearDetallePedidoRequest(
                    detalle.getPlatoId(),
                    detalle.getCantidad(),
                    detalle.getSubtotal()
            ));
        }

        CrearPedidoRequest request = new CrearPedidoRequest(pedidoLocal.getTotal(), detallesRequeridos);

        loading.postValue(true);
        mensajeError.postValue(null);

        api.crearPedido(request).enqueue(new Callback<PedidoDTO>() {
            @Override
            public void onResponse(@NonNull Call<PedidoDTO> call, @NonNull Response<PedidoDTO> response) {
                loading.postValue(false);
                if (response.isSuccessful()) {
                    pedidoLocal = response.body();
                    if (pedidoLocal != null) {
                        pedidoPendienteLocal = true;
                        session.savePendingPedido(pedidoLocal);
                        session.savePedidoPendienteFlag(true);
                        publicarPedidoActivo(pedidoLocal);
                    }
                } else if (response.code() == 409) {
                    mensajeError.postValue("Ya tenés un pedido en curso");
                } else {
                    mensajeError.postValue("No se pudo confirmar el pedido.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<PedidoDTO> call, @NonNull Throwable t) {
                loading.postValue(false);
                mensajeError.postValue("Error al confirmar el pedido: " + t.getMessage());
            }
        });
    }

    private void limpiarPedidoLocal() {
        pedidoPendienteLocal = false;
        pedidoLocal = null;
        session.clearPendingPedido(); // FIX: wipe persisted carrito when pedido termina.
        publicarPedidoActivo(null);
    }

    private void publicarPedidoActivo(PedidoDTO pedido) {
        if (pedido == null || pedido.getDetalles() == null || pedido.getDetalles().isEmpty()) {
            pedidos.postValue(Collections.emptyList());
            return;
        }
        List<PedidoDTO> activos = new ArrayList<>();
        activos.add(pedido);
        pedidos.postValue(activos);
    }
}
