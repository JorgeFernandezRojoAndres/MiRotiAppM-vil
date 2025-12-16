package com.jorge.mirotimobile.ui.cadete.entregas;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jorge.mirotimobile.Retrofit.ApiService;
import com.jorge.mirotimobile.Retrofit.RetrofitClient;
import com.jorge.mirotimobile.data.CadeteService;
import com.jorge.mirotimobile.localdata.SessionManager;
import com.jorge.mirotimobile.model.PedidoDTO;
import com.jorge.mirotimobile.model.Usuario;
import com.jorge.mirotimobile.util.Event;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.GET;

public class EntregasViewModel extends AndroidViewModel {

    public enum EstadoEntrega {
        EN_PREPARACION,
        ASIGNADO_AL_CADETE,
        EN_ESPERA,
        EN_CAMINO,
        ENTREGADO
    }

    public static class EstadoEntregaUiState {
        public final int backgroundColorRes;
        public final String titulo;
        public final String descripcion;
        public final boolean mostrarEntregaActual;
        public final boolean mostrarTomarPedido;
        public final boolean mostrarIniciarEntrega;
        public final boolean mostrarAcciones;
        public final boolean mostrarCompletado;

        public EstadoEntregaUiState(
                int backgroundColorRes,
                String titulo,
                String descripcion,
                boolean mostrarEntregaActual,
                boolean mostrarTomarPedido,
                boolean mostrarIniciarEntrega,
                boolean mostrarAcciones,
                boolean mostrarCompletado
        ) {
            this.backgroundColorRes = backgroundColorRes;
            this.titulo = titulo;
            this.descripcion = descripcion;
            this.mostrarEntregaActual = mostrarEntregaActual;
            this.mostrarTomarPedido = mostrarTomarPedido;
            this.mostrarIniciarEntrega = mostrarIniciarEntrega;
            this.mostrarAcciones = mostrarAcciones;
            this.mostrarCompletado = mostrarCompletado;
        }
    }

    private static final String ESTADO_EN_PREPARACION = "EN_PREPARACION";
    private static final String ESTADO_ASIGNADO_AL_CADETE = "ASIGNADO_AL_CADETE";
    private static final String ESTADO_EN_CAMINO = "EN_CAMINO";
    private static final String ESTADO_ENTREGADO = "ENTREGADO";

    private final MutableLiveData<String> nombreCadete = new MutableLiveData<>();
    private final MutableLiveData<Boolean> cadeteEnServicio = new MutableLiveData<>();
    private final MutableLiveData<String> estadoCadete = new MutableLiveData<>();
    private final MutableLiveData<String> tiempoPromedio = new MutableLiveData<>("Tiempo promedio: 25–30 min");
    private final MutableLiveData<PedidoDTO> pedidoActual = new MutableLiveData<>();
    private final MutableLiveData<Boolean> puedeTomarPedidos = new MutableLiveData<>(true);
    private final MutableLiveData<List<PedidoDTO>> proximasEntregas = new MutableLiveData<>();
    private final MutableLiveData<EstadoEntrega> estadoEntrega = new MutableLiveData<>();
    private final MutableLiveData<EstadoEntregaUiState> estadoEntregaUi = new MutableLiveData<>();
    private final MutableLiveData<Event<Integer>> eventoIrTracking = new MutableLiveData<>();
    private final MutableLiveData<Event<String>> eventoAbrirMapa = new MutableLiveData<>();
    private final MutableLiveData<List<PedidoDTO>> historialEntregas = new MutableLiveData<>(new ArrayList<>());
    private final CadeteService cadeteService;
    private final ApiService apiService;
    private final PedidosAsignadosService pedidosAsignadosService;
    private final SessionManager sessionManager;

    private interface PedidosAsignadosService {
        @GET("pedidos/asignados")
        Call<List<PedidoDTO>> obtenerPedidosAsignados();
    }

    public EntregasViewModel(@NonNull Application application) {
        super(application);
        sessionManager = new SessionManager(application.getApplicationContext());
        cadeteService = RetrofitClient.getClient(application.getApplicationContext()).create(CadeteService.class);
        apiService = RetrofitClient.getClient(application.getApplicationContext()).create(ApiService.class);
        pedidosAsignadosService = RetrofitClient.getClient(application.getApplicationContext()).create(PedidosAsignadosService.class);

        boolean initialEnServicio = sessionManager.isCadeteEnServicio();
        cadeteEnServicio.setValue(initialEnServicio);
        estadoCadete.setValue(mapTextoCadeteEnServicio(initialEnServicio));

        String nombreGuardado = sessionManager.getUserName();
        if (nombreGuardado == null || nombreGuardado.isEmpty()) {
            nombreGuardado = sessionManager.getUserEmail();
        }
        nombreCadete.setValue(nombreGuardado != null && !nombreGuardado.isEmpty() ? nombreGuardado : "Cadete");

        // Estado inicial: sin pedido tomado (no hay "Entrega actual")
        setPedidoActual(null);

        cargarPerfilCadete();
        cargarPedidosAsignados();
        cargarPedidosDisponibles();
    }

    private void cargarPerfilCadete() {
        apiService.obtenerPerfil().enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Usuario perfil = response.body();
                    if (perfil.getNombre() != null && !perfil.getNombre().isEmpty()) {
                        sessionManager.saveUserName(perfil.getNombre());
                        nombreCadete.setValue(perfil.getNombre());
                    }
                }
            }

            @Override
            public void onFailure(Call<Usuario> call, Throwable t) {
                // No romper UI
            }
        });
    }

    public LiveData<String> getNombreCadete() {
        return nombreCadete;
    }

    public LiveData<String> getEstadoCadete() {
        return estadoCadete;
    }

    public LiveData<Boolean> getCadeteEnServicio() {
        return cadeteEnServicio;
    }

    public void setCadeteEnServicio(boolean enServicio) {
        sessionManager.setCadeteEnServicio(enServicio);
        cadeteEnServicio.setValue(enServicio);
        estadoCadete.setValue(mapTextoCadeteEnServicio(enServicio));
    }

    public LiveData<String> getTiempoPromedio() {
        return tiempoPromedio;
    }

    public LiveData<PedidoDTO> getPedidoActual() {
        return pedidoActual;
    }

    public LiveData<Boolean> getPuedeTomarPedidos() {
        return puedeTomarPedidos;
    }

    public LiveData<PedidoDTO> getEntregaActual() {
        return pedidoActual;
    }

    public LiveData<List<PedidoDTO>> getProximasEntregas() {
        return proximasEntregas;
    }

    public LiveData<EstadoEntrega> getEstadoEntrega() {
        return estadoEntrega;
    }

    public LiveData<EstadoEntregaUiState> getEstadoEntregaUi() {
        return estadoEntregaUi;
    }

    public LiveData<Event<Integer>> getEventoIrTracking() {
        return eventoIrTracking;
    }

    public LiveData<Event<String>> getEventoAbrirMapa() {
        return eventoAbrirMapa;
    }

    public LiveData<List<PedidoDTO>> getHistorialEntregas() {
        return historialEntregas;
    }

    public void cerrarEntregaActual() {
        PedidoDTO actual = pedidoActual.getValue();
        if (actual == null) return;

        List<PedidoDTO> historial = historialEntregas.getValue();
        if (historial == null) {
            historial = new ArrayList<>();
        } else {
            historial = new ArrayList<>(historial);
        }
        historial.add(0, actual);
        historialEntregas.setValue(historial);

        setPedidoActual(null);
    }

    public void cargarEntregaActual() {
        cadeteService.getEntregaActual().enqueue(new Callback<PedidoDTO>() {
            @Override
            public void onResponse(Call<PedidoDTO> call, Response<PedidoDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    setPedidoActual(response.body());
                } else if (response.code() == 404 || response.body() == null) {
                    setPedidoActual(null);
                }
            }

            @Override
            public void onFailure(Call<PedidoDTO> call, Throwable t) {
                // Mantener el valor actual para no romper la UI, se puede loggear si se desea.
            }
        });
    }

    private void cargarPedidosAsignados() {
        pedidosAsignadosService.obtenerPedidosAsignados().enqueue(new Callback<List<PedidoDTO>>() {
            @Override
            public void onResponse(Call<List<PedidoDTO>> call, Response<List<PedidoDTO>> response) {
                if (!response.isSuccessful()) return;

                List<PedidoDTO> asignados = response.body();
                if (asignados != null && !asignados.isEmpty()) {
                    setPedidoActual(asignados.get(0));
                    return;
                }

                setPedidoActual(null);
            }

            @Override
            public void onFailure(Call<List<PedidoDTO>> call, Throwable t) {
                // No romper UI
            }
        });
    }

    private void cargarPedidosDisponibles() {
        apiService.obtenerPedidosDisponibles().enqueue(new Callback<List<PedidoDTO>>() {
            @Override
            public void onResponse(Call<List<PedidoDTO>> call, Response<List<PedidoDTO>> response) {
                if (!response.isSuccessful()) return;

                List<PedidoDTO> disponibles = response.body();
                proximasEntregas.setValue(disponibles != null ? disponibles : new ArrayList<>());
            }

            @Override
            public void onFailure(Call<List<PedidoDTO>> call, Throwable t) {
                // No romper UI
            }
        });
    }

    public void limpiarEventos() {
        eventoIrTracking.setValue(null);
        eventoAbrirMapa.setValue(null);
    }

    public void iniciarEntrega() {
        Log.d("ENTREGA_FLOW", "iniciarEntrega() called");
        PedidoDTO actual = pedidoActual.getValue();
        Log.d("ENTREGA_FLOW", "pedidoActual: " + (actual != null ? actual.getId() : "null"));
        if (actual == null) return;
        iniciarEntrega(actual.getId());
    }

    public void tomarPedido() {
        PedidoDTO actual = pedidoActual.getValue();
        if (actual == null) return;
        actualizarPedidoActual(actual, ESTADO_ASIGNADO_AL_CADETE);
    }

    public void tomarPedido(int idPedido) {
        if (pedidoActual.getValue() != null) return;
        apiService.tomarPedido(idPedido).enqueue(new Callback<PedidoDTO>() {
            @Override
            public void onResponse(Call<PedidoDTO> call, Response<PedidoDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    setPedidoActual(response.body());

                    List<PedidoDTO> actuales = proximasEntregas.getValue();
                    if (actuales == null) return;
                    List<PedidoDTO> restantes = new ArrayList<>();
                    for (PedidoDTO p : actuales) {
                        if (p != null && p.getId() != idPedido) restantes.add(p);
                    }
                    proximasEntregas.setValue(restantes);
                }
            }

            @Override
            public void onFailure(Call<PedidoDTO> call, Throwable t) {
                // No romper UI
            }
        });
    }

    public void iniciarEntrega(int idPedido) {
        Log.d("ENTREGA_FLOW", "iniciarEntrega(" + idPedido + ") called");
        PedidoDTO actual = pedidoActual.getValue();
        if (actual == null || actual.getId() != idPedido) {
            Log.d("ENTREGA_FLOW", "Pedido validation failed");
            return;
        }
        Log.d("ENTREGA_FLOW", "Calling actualizarPedidoActual");
        actualizarPedidoActual(actual, ESTADO_EN_CAMINO);
        Log.d("ENTREGA_FLOW", "Setting eventoIrTracking");
        eventoIrTracking.setValue(new Event<>(idPedido));
        Log.d("ENTREGA_FLOW", "iniciarEntrega COMPLETE");
    }

    public void marcarEntregaCompletada() {
        Log.d("ENTREGA_FLOW", "marcarEntregaCompletada called");
        PedidoDTO actual = pedidoActual.getValue();
        if (actual == null) {
            Log.d("ENTREGA_FLOW", "No hay pedido actual para marcar");
            return;
        }
        apiService.marcarPedidoEntregado(actual.getId()).enqueue(new Callback<PedidoDTO>() {
            @Override
            public void onResponse(Call<PedidoDTO> call, Response<PedidoDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("ENTREGA_FLOW", "Pedido marcado como entregado desde API");
                    cerrarEntregaActual();
                } else {
                    Log.e("ENTREGA_FLOW", "API respondió con error al marcar entrega: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<PedidoDTO> call, Throwable t) {
                Log.e("ENTREGA_FLOW", "Error al marcar entrega: " + t.getMessage());
            }
        });
    }

    public void abrirMapa() {
        PedidoDTO actual = pedidoActual.getValue();
        if (actual == null) return;
        
        Log.d("MAPA_DEBUG", new com.google.gson.Gson().toJson(actual));
        
        String direccion = actual.getDireccion();
        eventoAbrirMapa.setValue(new Event<>(direccion != null ? direccion : ""));
    }

    private EstadoEntrega mapEstado(String estado) {
        if (estado == null) return EstadoEntrega.EN_ESPERA;
        String normalized = estado.trim().toUpperCase();
        switch (normalized) {
            case ESTADO_EN_PREPARACION:
            case "EN PREPARACION":
                return EstadoEntrega.EN_PREPARACION;
            case ESTADO_ASIGNADO_AL_CADETE:
            case "ASIGNADO":
                return EstadoEntrega.ASIGNADO_AL_CADETE;
            case "EN_CAMINO":
            case "EN_PROCESO":
                return EstadoEntrega.EN_CAMINO;
            case "ENTREGADO":
                return EstadoEntrega.ENTREGADO;
            default:
                return EstadoEntrega.EN_ESPERA;
        }
    }

    private String mapTextoCadeteEnServicio(boolean enServicio) {
        return enServicio ? "En servicio" : "Fuera de servicio";
    }

    private void setPedidoActual(PedidoDTO pedido) {
        Log.d("ENTREGA_FLOW", "setPedidoActual called");
        pedidoActual.setValue(pedido);
        Log.d("ENTREGA_FLOW", "pedidoActual.setValue DONE");
        puedeTomarPedidos.setValue(pedido == null);
        Log.d("ENTREGA_FLOW", "puedeTomarPedidos.setValue DONE");
        EstadoEntrega estado = mapEstado(pedido != null ? pedido.getEstado() : null);
        Log.d("ENTREGA_FLOW", "mapEstado DONE: " + estado);
        estadoEntrega.setValue(estado);
        Log.d("ENTREGA_FLOW", "estadoEntrega.setValue DONE");
        estadoEntregaUi.setValue(mapEstadoEntregaUi(estado));
        Log.d("ENTREGA_FLOW", "setPedidoActual COMPLETE");
    }

    private void actualizarPedidoActual(PedidoDTO actual, String nuevoEstado) {
        Log.d("ENTREGA_FLOW", "actualizarPedidoActual: " + nuevoEstado);
        actual.setEstado(nuevoEstado);
        Log.d("ENTREGA_FLOW", "Calling setPedidoActual");
        setPedidoActual(actual);
        Log.d("ENTREGA_FLOW", "actualizarPedidoActual COMPLETE");
    }

    private EstadoEntregaUiState mapEstadoEntregaUi(EstadoEntrega estado) {
        if (estado == null) estado = EstadoEntrega.EN_ESPERA;
        switch (estado) {
            case EN_PREPARACION:
                return new EstadoEntregaUiState(
                        com.jorge.mirotimobile.R.color.estado_proceso,
                        "🍽️ En preparación",
                        "Tomá el pedido cuando esté listo para salir.",
                        true,
                        true,
                        false,
                        false,
                        false
                );
            case ASIGNADO_AL_CADETE:
                return new EstadoEntregaUiState(
                        com.jorge.mirotimobile.R.color.estado_proceso,
                        "📦 Asignado al cadete",
                        "Cuando estés listo, iniciá la entrega.",
                        true,
                        false,
                        true,
                        false,
                        false
                );
            case EN_CAMINO:
                return new EstadoEntregaUiState(
                        com.jorge.mirotimobile.R.color.estado_proceso,
                        "🚴 En camino",
                        "Dirígete a la dirección del pedido y mantenete al tanto.",
                        true,
                        false,
                        false,
                        true,
                        false
                );
            case ENTREGADO:
                return new EstadoEntregaUiState(
                        com.jorge.mirotimobile.R.color.estado_entregado,
                        "✅ Entrega finalizada",
                        "Gracias por entregar a tiempo.",
                        true,
                        false,
                        false,
                        false,
                        true
                );
            case EN_ESPERA:
            default:
                return new EstadoEntregaUiState(
                        com.jorge.mirotimobile.R.color.nav_icon_inactive,
                        "",
                        "",
                        false,
                        false,
                        false,
                        false,
                        false
                );
        }
    }
}
