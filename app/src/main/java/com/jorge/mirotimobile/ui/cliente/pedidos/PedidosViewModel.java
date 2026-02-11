package com.jorge.mirotimobile.ui.cliente.pedidos;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.jorge.mirotimobile.util.Event;
import android.view.View;

import com.jorge.mirotimobile.retrofit.ApiService;
import com.jorge.mirotimobile.retrofit.RetrofitClient;
import com.jorge.mirotimobile.localdata.SessionManager;
import com.jorge.mirotimobile.model.CrearDetallePedidoRequest;
import com.jorge.mirotimobile.model.CrearPedidoRequest;
import com.jorge.mirotimobile.model.DetallePedidoInfoDTO;
import com.jorge.mirotimobile.model.PedidoDTO;
import com.jorge.mirotimobile.model.PedidoResumen;
import com.jorge.mirotimobile.model.Plato;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.text.NumberFormat;
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
    private final MutableLiveData<Integer> progressVisibility = new MutableLiveData<>();
    private final MutableLiveData<Integer> errorVisibility = new MutableLiveData<>();
    private final MutableLiveData<String> errorText = new MutableLiveData<>();
    private final MutableLiveData<Integer> btnCarritoVisibility = new MutableLiveData<>();
    private final MutableLiveData<String> btnCarritoText = new MutableLiveData<>();
    private final MutableLiveData<Integer> btnSeguimientoVisibility = new MutableLiveData<>();
    private final MutableLiveData<Event<Integer>> eventoNavegacion = new MutableLiveData<>();
    private final MutableLiveData<String> pedidoNumero = new MutableLiveData<>();
    private final MutableLiveData<String> pedidoFecha = new MutableLiveData<>();
    private final MutableLiveData<String> estadoTexto = new MutableLiveData<>();
    private final MutableLiveData<Integer> estadoColorRes = new MutableLiveData<>();
    private final MutableLiveData<Integer> estadoTextColorRes = new MutableLiveData<>();
    private final MutableLiveData<String> subtotalTexto = new MutableLiveData<>();
    private final MutableLiveData<String> envioTexto = new MutableLiveData<>();
    private final MutableLiveData<String> totalTexto = new MutableLiveData<>();
    private final MutableLiveData<String> btnConfirmarTexto = new MutableLiveData<>();
    private final MutableLiveData<Boolean> btnConfirmarEnabled = new MutableLiveData<>();
    private final MutableLiveData<Event<String>> eventoMostrarSnackbar = new MutableLiveData<>();
    private final MutableLiveData<Event<Integer>> eventoNavegar = new MutableLiveData<>();
    private final MutableLiveData<Integer> btnVolverSeguimientoVisibility = new MutableLiveData<>();
    private final MutableLiveData<Integer> btnConfirmarVisibility = new MutableLiveData<>();
    private final MutableLiveData<Integer> btnCancelarVisibility = new MutableLiveData<>();
    private final MutableLiveData<Integer> btnSeguirComprandoVisibility = new MutableLiveData<>();
    private final MutableLiveData<Boolean> edtNotasEnabled = new MutableLiveData<>();
    private final MutableLiveData<String> trackingTitle = new MutableLiveData<>();
    private final MutableLiveData<String> trackingSubtitle = new MutableLiveData<>();
    private final MutableLiveData<String> trackingArrivalTime = new MutableLiveData<>();
    private final MutableLiveData<String> cadeteName = new MutableLiveData<>();
    private final MutableLiveData<String> cadetePhone = new MutableLiveData<>();
    private final MutableLiveData<Integer> estadoAccionesVisibility = new MutableLiveData<>();
    private final MutableLiveData<Integer> btnMarcarEntregadoVisibility = new MutableLiveData<>();
    private final MutableLiveData<Event<String>> eventoContactarCadete = new MutableLiveData<>();
    private final MutableLiveData<Event<String>> eventoMostrarMensaje = new MutableLiveData<>();
    private final MutableLiveData<Integer> pedidosCompletadosCount = new MutableLiveData<>(0);
    private final MutableLiveData<List<PedidoResumen>> pedidosRecientes = new MutableLiveData<>(
            Collections.emptyList());
    private final MutableLiveData<String> saldoFavor = new MutableLiveData<>("$ 0,00");
    private final MutableLiveData<String> btnAbrirMapaText = new MutableLiveData<>("Ver mapa");
    private final MutableLiveData<Boolean> btnAbrirMapaEnabled = new MutableLiveData<>(false);
    private final MutableLiveData<Event<String>> eventoVerMapa = new MutableLiveData<>();
    private final MutableLiveData<Boolean> shouldNavigateToMenu = new MutableLiveData<>(false);

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

    public LiveData<Integer> getProgressVisibility() {
        return progressVisibility;
    }

    public LiveData<Integer> getErrorVisibility() {
        return errorVisibility;
    }

    public LiveData<String> getErrorText() {
        return errorText;
    }

    public LiveData<Integer> getBtnCarritoVisibility() {
        return btnCarritoVisibility;
    }

    public LiveData<String> getBtnCarritoText() {
        return btnCarritoText;
    }

    public LiveData<Integer> getBtnSeguimientoVisibility() {
        return btnSeguimientoVisibility;
    }

    public LiveData<Event<Integer>> getEventoNavegacion() {
        return eventoNavegacion;
    }

    public LiveData<Boolean> getShouldNavigateToMenu() {
        return shouldNavigateToMenu;
    }

    public void resetNavigateToMenu() {
        shouldNavigateToMenu.setValue(false);
    }

    public LiveData<String> getPedidoNumero() {
        return pedidoNumero;
    }

    public LiveData<String> getPedidoFecha() {
        return pedidoFecha;
    }

    public LiveData<String> getEstadoTexto() {
        return estadoTexto;
    }

    public LiveData<Integer> getEstadoColorRes() {
        return estadoColorRes;
    }

    public LiveData<Integer> getEstadoTextColorRes() {
        return estadoTextColorRes;
    }

    public LiveData<String> getSubtotalTexto() {
        return subtotalTexto;
    }

    public LiveData<String> getEnvioTexto() {
        return envioTexto;
    }

    public LiveData<String> getTotalTexto() {
        return totalTexto;
    }

    public LiveData<String> getBtnConfirmarTexto() {
        return btnConfirmarTexto;
    }

    public LiveData<Boolean> getBtnConfirmarEnabled() {
        return btnConfirmarEnabled;
    }

    public LiveData<Event<String>> getEventoMostrarSnackbar() {
        return eventoMostrarSnackbar;
    }

    public LiveData<Event<Integer>> getEventoNavegar() {
        return eventoNavegar;
    }

    public LiveData<Integer> getBtnVolverSeguimientoVisibility() {
        return btnVolverSeguimientoVisibility;
    }

    public LiveData<Integer> getBtnConfirmarVisibility() {
        return btnConfirmarVisibility;
    }

    public LiveData<Integer> getBtnCancelarVisibility() {
        return btnCancelarVisibility;
    }

    public LiveData<Integer> getBtnSeguirComprandoVisibility() {
        return btnSeguirComprandoVisibility;
    }

    public LiveData<Boolean> getEdtNotasEnabled() {
        return edtNotasEnabled;
    }

    public LiveData<String> getTrackingTitle() {
        return trackingTitle;
    }

    public LiveData<String> getTrackingSubtitle() {
        return trackingSubtitle;
    }

    public LiveData<String> getTrackingArrivalTime() {
        return trackingArrivalTime;
    }

    public LiveData<String> getCadeteName() {
        return cadeteName;
    }

    public LiveData<String> getCadetePhone() {
        return cadetePhone;
    }

    public LiveData<Integer> getEstadoAccionesVisibility() {
        return estadoAccionesVisibility;
    }

    public LiveData<Integer> getBtnMarcarEntregadoVisibility() {
        return btnMarcarEntregadoVisibility;
    }

    public LiveData<Event<String>> getEventoContactarCadete() {
        return eventoContactarCadete;
    }

    public LiveData<Event<String>> getEventoMostrarMensaje() {
        return eventoMostrarMensaje;
    }

    public LiveData<Integer> getPedidosCompletadosCount() {
        return pedidosCompletadosCount;
    }

    public LiveData<List<PedidoResumen>> getPedidosRecientes() {
        return pedidosRecientes;
    }

    public LiveData<String> getSaldoFavor() {
        return saldoFavor;
    }

    public LiveData<String> getBtnAbrirMapaText() {
        return btnAbrirMapaText;
    }

    public LiveData<Boolean> getBtnAbrirMapaEnabled() {
        return btnAbrirMapaEnabled;
    }

    public LiveData<Event<String>> getEventoVerMapa() {
        return eventoVerMapa;
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
        if (pedidoPendienteLocal && pedidoLocal != null && esPedidoFinalizado(pedidoLocal)) {
            limpiarPedidoLocal();
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
                Log.d("MAIN_FLOW", "obtenerMisPedidos onResponse: code=" + response.code() + " isSuccessful="
                        + response.isSuccessful());

                if (response.isSuccessful() && response.body() != null) {
                    List<PedidoDTO> lista = response.body();
                    historialPedidos.postValue(lista);
                    actualizarConteoPedidosCompletados(lista);
                    actualizarPedidosRecientes(lista);
                    actualizarSaldoFavor(lista);

                    PedidoDTO activo = elegirPedidoActivo(lista);
                    if (activo != null) {
                        publicarPedidoActivo(activo);
                        procesarPedidoActivo(activo);
                    } else {
                        // Si no hay pedido activo en el servidor, verificamos si nuestro pedidoLocal
                        // con ID ya finalizó
                        if (pedidoLocal != null && pedidoLocal.getId() > 0) {
                            // Buscamos nuestro pedido en la lista completa del servidor
                            PedidoDTO versionServidor = lista.stream()
                                    .filter(p -> p.getId() == pedidoLocal.getId())
                                    .findFirst()
                                    .orElse(null);

                            if (versionServidor != null && esPedidoFinalizado(versionServidor)) {
                                Log.d("MAIN_FLOW", "Pedido local #" + pedidoLocal.getId()
                                        + " detectado como finalizado en servidor. Limpiando.");
                                limpiarPedidoLocal();
                                actualizarControlsSinPedido();
                            } else if (versionServidor == null) {
                                // El pedido no existe más en el servidor (raro, pero posible)
                                limpiarPedidoLocal();
                                actualizarControlsSinPedido();
                            }
                            // Si versionServidor != null y no está finalizado, pero elegirPedidoActivo no
                            // lo tomó,
                            // es una inconsistencia que no debería pasar (esEstadoActivo vs
                            // elegirPedidoActivo).
                        }

                        // Después de la limpieza (o si no había nada que limpiar), verificamos el
                        // carrito local
                        if (!pedidoPendienteLocal || pedidoLocal == null || pedidoLocal.getDetalles() == null
                                || pedidoLocal.getDetalles().isEmpty()) {

                            // Si realmente no hay nada activo ni cart, notificamos vista vacía
                            if (pedidos.getValue() == null || !pedidos.getValue().isEmpty()) {
                                pedidos.postValue(Collections.emptyList());
                            }
                            actualizarControlsSinPedido();
                        } else {
                            // Es un carrito local (ID=0), lo mantenemos como activo
                            publicarPedidoActivo(pedidoLocal);
                            procesarPedidoActivo(pedidoLocal);
                        }
                    }
                    actualizarVisibilidad();
                } else {
                    Log.w("MAIN_FLOW", "obtenerMisPedidos failed: code=" + response.code());

                    // A pesar del error de red, si tenemos un pedido local lo mostramos
                    if (pedidoPendienteLocal && pedidoLocal != null) {
                        publicarPedidoActivo(pedidoLocal);
                    } else {
                        mensajeError.postValue("No se pudieron cargar tus pedidos.");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<PedidoDTO>> call, @NonNull Throwable t) {
                loading.postValue(false);
                Log.e("MAIN_FLOW", "obtenerMisPedidos onFailure: " + t.getMessage());

                // Si hay error de red pero tenemos pedido local, lo mantenemos
                if (pedidoPendienteLocal && pedidoLocal != null) {
                    publicarPedidoActivo(pedidoLocal);
                } else {
                    mensajeError.postValue("Error de conexión: " + t.getMessage());
                }
            }
        });
    }

    private PedidoDTO elegirPedidoActivo(List<PedidoDTO> lista) {
        // Primero buscamos un pedido que NO esté entregado ni cancelado en la lista del
        // servidor
        if (lista != null && !lista.isEmpty()) {
            List<PedidoDTO> activos = filtrarPedidosActivos(lista);
            if (!activos.isEmpty()) {
                ordenarPorFecha(activos);
                return activos.get(0);
            }
        }

        // Si no hay pedidos activos en el servidor, retornamos null (se manejará el
        // pedidoLocal en el llamador)
        return null;
    }

    private List<PedidoDTO> filtrarPedidosActivos(List<PedidoDTO> lista) {
        List<PedidoDTO> activos = new ArrayList<>();
        for (PedidoDTO pedido : lista) {
            if (esEstadoActivo(pedido.getEstado())) {
                activos.add(pedido);
            }
        }
        return activos;
    }

    private boolean esEstadoActivo(String estado) {
        if (estado == null)
            return false;
        String normalized = estado.trim().toLowerCase(Locale.ROOT);
        if (normalized.equals("cancelado"))
            return false;
        return !normalized.equals("entregado") && !normalized.equals("completado");
    }

    private void actualizarConteoPedidosCompletados(List<PedidoDTO> lista) {
        int completados = 0;
        if (lista != null) {
            for (PedidoDTO pedido : lista) {
                if (esPedidoFinalizado(pedido)) {
                    completados++;
                }
            }
        }
        pedidosCompletadosCount.setValue(completados);
    }

    private boolean esPedidoFinalizado(PedidoDTO pedido) {
        if (pedido == null || pedido.getEstado() == null) {
            return false;
        }
        String normalized = pedido.getEstado().trim().toLowerCase(Locale.ROOT);
        return normalized.equals("entregado") || normalized.equals("completado") || normalized.equals("cancelado");
    }

    private void actualizarPedidosRecientes(List<PedidoDTO> lista) {
        if (lista == null || lista.isEmpty()) {
            pedidosRecientes.setValue(Collections.emptyList());
            return;
        }
        List<PedidoDTO> ordenados = new ArrayList<>(lista);
        ordenarPorFecha(ordenados);
        List<PedidoResumen> resumen = new ArrayList<>();
        int limite = Math.min(3, ordenados.size());
        NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));
        for (int i = 0; i < limite; i++) {
            PedidoDTO pedido = ordenados.get(i);
            String fechaFormateada = formatearFecha(pedido.getFechaHora());
            String totalTexto = currency.format(pedido.getTotal());
            resumen.add(new PedidoResumen(
                    "#" + pedido.getId(),
                    fechaFormateada != null ? fechaFormateada : pedido.getFechaHora(),
                    pedido.getEstado() != null ? pedido.getEstado() : "Sin estado",
                    pedido.getCadete(),
                    totalTexto));
        }
        pedidosRecientes.setValue(resumen);
    }

    private void actualizarSaldoFavor(List<PedidoDTO> lista) {
        if (lista == null || lista.isEmpty()) {
            saldoFavor.setValue("$ 0,00");
            return;
        }
        double acumulado = 0;
        for (PedidoDTO pedido : lista) {
            if (esPedidoFinalizado(pedido)) {
                acumulado += pedido.getTotal() * 0.05;
            }
        }
        NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));
        saldoFavor.setValue(currency.format(acumulado));
    }

    private void ordenarPorFecha(List<PedidoDTO> pedidos) {
        pedidos.sort((a, b) -> {
            LocalDateTime fechaA = parseFecha(a.getFechaHora());
            LocalDateTime fechaB = parseFecha(b.getFechaHora());

            if (fechaA != null && fechaB != null) {
                int cmp = fechaB.compareTo(fechaA); // Descendente
                if (cmp != 0)
                    return cmp;
            }

            // Tie-breaker: ID más alto es más nuevo
            return Integer.compare(b.getId(), a.getId());
        });
    }

    private LocalDateTime parseFecha(String iso) {
        if (iso == null || iso.isEmpty())
            return null;
        try {
            // Reemplazar espacio por T para compatibilidad ISO si es necesario
            String normalized = iso.contains(" ") && !iso.contains("T")
                    ? iso.replace(" ", "T")
                    : iso;
            return LocalDateTime.parse(normalized, DateTimeFormatter.ISO_DATE_TIME);
        } catch (DateTimeParseException e) {
            Log.w("MAIN_FLOW", "Failed to parse date: " + iso);
            return null;
        }
    }

    private final android.os.Handler pollingHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private final Runnable pollingRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d("MAIN_FLOW", "Polling track data...");
            cargarMisPedidos();
            pollingHandler.postDelayed(this, 10000); // 10 segundos
        }
    };

    public void iniciarPollingSeguimiento() {
        detenerPollingSeguimiento();
        pollingHandler.post(pollingRunnable);
    }

    public void detenerPollingSeguimiento() {
        pollingHandler.removeCallbacks(pollingRunnable);
    }

    public void agregarPlatoAlDetalle(Plato plato) {
        if (plato == null)
            return;

        inicializarPedidoSiEsNecesario();
        DetallePedidoInfoDTO detalle = buscarDetallePorNombre(pedidoLocal.getDetalles(), plato.getNombre());

        if (detalle == null) {
            // Caso nuevo: se crea con cantidad 1
            detalle = crearNuevoDetalle(plato);
            pedidoLocal.getDetalles().add(detalle);
        } else {
            // Caso existente: se incrementa la cantidad
            actualizarDetalle(detalle, plato);
        }

        pedidoLocal.setTotal(calcularTotal(pedidoLocal.getDetalles()));
        guardarPedidoLocal();
        publicarPedidoActivo(pedidoLocal.getDetalles().isEmpty() ? null : pedidoLocal);
    }

    private void inicializarPedidoSiEsNecesario() {
        if (pedidoLocal == null || !pedidoPendienteLocal) {
            pedidoLocal = new PedidoDTO();
            pedidoLocal.setDetalles(new ArrayList<>());
            pedidoLocal.setId(0);
        }
        if (pedidoLocal.getDetalles() == null) {
            pedidoLocal.setDetalles(new ArrayList<>());
        }
    }

    private DetallePedidoInfoDTO buscarOCrearDetalle(Plato plato) {
        DetallePedidoInfoDTO detalle = buscarDetallePorNombre(pedidoLocal.getDetalles(), plato.getNombre());
        if (detalle == null) {
            detalle = crearNuevoDetalle(plato);
            pedidoLocal.getDetalles().add(detalle);
        }
        return detalle;
    }

    private DetallePedidoInfoDTO crearNuevoDetalle(Plato plato) {
        DetallePedidoInfoDTO detalle = new DetallePedidoInfoDTO();
        detalle.setPlato(plato.getNombre());
        detalle.setCantidad(1);
        detalle.setSubtotal(plato.getPrecioVenta());
        detalle.setImagenUrl(plato.getImagenUrl());
        detalle.setPlatoId(plato.getId());
        return detalle;
    }

    private void actualizarDetalle(DetallePedidoInfoDTO detalle, Plato plato) {
        detalle.setCantidad(detalle.getCantidad() + 1);
        detalle.setSubtotal(detalle.getCantidad() * plato.getPrecioVenta());
    }

    private void guardarPedidoLocal() {
        pedidoPendienteLocal = true;
        session.savePendingPedido(pedidoLocal);
        session.savePedidoPendienteFlag(true);
    }

    private DetallePedidoInfoDTO buscarDetallePorNombre(List<DetallePedidoInfoDTO> detalles, String nombrePlato) {
        if (detalles == null || detalles.isEmpty() || nombrePlato == null)
            return null;
        for (DetallePedidoInfoDTO detalle : detalles) {
            if (nombrePlato.equalsIgnoreCase(detalle.getPlato())) {
                return detalle;
            }
        }
        return null;
    }

    private double calcularTotal(List<DetallePedidoInfoDTO> detalles) {
        if (detalles == null || detalles.isEmpty())
            return 0;
        double total = 0;
        for (DetallePedidoInfoDTO detalle : detalles) {
            total += detalle.getSubtotal();
        }
        return total;
    }

    public void confirmarPedido() {
        if (!validarPedidoParaConfirmar()) {
            return;
        }

        if (pedidoLocal.getId() > 0) {
            com.jorge.mirotimobile.model.EstadoPedido estado = com.jorge.mirotimobile.model.EstadoPedido
                    .fromString(pedidoLocal.getEstado());

            if (estado == com.jorge.mirotimobile.model.EstadoPedido.PENDIENTE) {
                procesarPagoContraEntrega();
                return;
            }
            navegarASeguimiento.setValue(true);
            return;
        }

        List<CrearDetallePedidoRequest> detallesRequeridos = crearDetallesRequest();
        if (detallesRequeridos == null) {
            return;
        }

        enviarPedidoAlServidor(detallesRequeridos);
    }

    private boolean validarPedidoParaConfirmar() {
        // Obtener el pedido actual desde la lista de pedidos si no hay pedido local
        if (pedidoLocal == null) {
            List<PedidoDTO> pedidosActuales = pedidos.getValue();
            if (pedidosActuales != null && !pedidosActuales.isEmpty()) {
                pedidoLocal = pedidosActuales.get(0);
            }
        }

        if (pedidoLocal == null) {
            return false;
        }

        List<DetallePedidoInfoDTO> detalles = pedidoLocal.getDetalles();
        if (detalles == null || detalles.isEmpty()) {
            mensajeError.postValue("No se pudo confirmar el pedido.");
            return false;
        }

        return true;
    }

    public void aplicarModoDesdeArgs(android.os.Bundle args) {
        boolean modoCadete = args != null && args.getBoolean("modoCadete", false);
        this.aplicarModoCadete(modoCadete);
    }

    public void actualizarResumen(double subtotal) {
        procesarPedidoParaDetalle(null, subtotal);
    }

    public void volverAlSeguimiento() {
        eventoNavegar.setValue(new Event<>(com.jorge.mirotimobile.R.id.trackingCadeteFragment));
    }

    public void procesarPedidoParaDetalle(PedidoDTO pedido, double subtotal) {
        actualizarDatosPedido(pedido);
        actualizarResumenPedido(subtotal);
        actualizarBotonConfirmar(pedido);
    }

    public void aplicarModoCadete(boolean modoCadete) {
        if (!modoCadete) {
            btnVolverSeguimientoVisibility.setValue(android.view.View.GONE);
            return;
        }

        btnConfirmarVisibility.setValue(android.view.View.GONE);
        btnCancelarVisibility.setValue(android.view.View.GONE);
        btnSeguirComprandoVisibility.setValue(android.view.View.GONE);
        edtNotasEnabled.setValue(false);
        btnVolverSeguimientoVisibility.setValue(android.view.View.VISIBLE);
    }

    public void cancelarYNavegar() {
        cancelarPedidoLocal();
        eventoMostrarSnackbar.setValue(new Event<>("Pedido cancelado"));
        // El VM decide navegar hacia atrás o a un destino específico
        eventoNavegar.setValue(new Event<>(-1)); // -1 para indicar popBackStack o manejarlo en la vista
    }

    private void actualizarDatosPedido(PedidoDTO pedido) {
        pedidoNumero.setValue(pedido != null ? "Pedido #" + pedido.getId() : "Pedido #0000");

        if (pedido != null) {
            String fechaFormateada = formatearFecha(pedido.getFechaHora());
            pedidoFecha.setValue(fechaFormateada != null ? fechaFormateada : pedido.getFechaHora());

            com.jorge.mirotimobile.model.EstadoPedido estado = com.jorge.mirotimobile.model.EstadoPedido
                    .fromString(pedido.getEstado());
            estadoTexto.setValue(estado.getLabel());
            estadoColorRes.setValue(estado.getColorRes());

            // Color del texto: negro para PENDIENTE, blanco para otros estados
            if (estado == com.jorge.mirotimobile.model.EstadoPedido.PENDIENTE) {
                estadoTextColorRes.setValue(android.R.color.black);
            } else {
                estadoTextColorRes.setValue(com.jorge.mirotimobile.R.color.white);
            }
        } else {
            pedidoFecha.setValue("-");
            estadoTexto.setValue("En armado");
            estadoColorRes.setValue(com.jorge.mirotimobile.R.color.nav_icon_inactive);
            estadoTextColorRes.setValue(com.jorge.mirotimobile.R.color.white);
        }
    }

    private void actualizarResumenPedido(double subtotal) {
        java.text.NumberFormat currency = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("es", "AR"));
        double envio = subtotal > 0 ? 200 : 0;
        double total = subtotal + envio;

        subtotalTexto.setValue(currency.format(subtotal));
        envioTexto.setValue(currency.format(envio));
        totalTexto.setValue(currency.format(total));
    }

    private void actualizarBotonConfirmar(PedidoDTO pedido) {
        if (pedido != null && pedido.getId() > 0) {
            com.jorge.mirotimobile.model.EstadoPedido estado = com.jorge.mirotimobile.model.EstadoPedido
                    .fromString(pedido.getEstado());
            if (estado == com.jorge.mirotimobile.model.EstadoPedido.PENDIENTE) {
                btnConfirmarTexto.setValue("Pagar (contra entrega)");
                btnConfirmarEnabled.setValue(true);
                return;
            }
            btnConfirmarTexto.setValue("SEGUIR PEDIDO");
            btnConfirmarEnabled.setValue(true);
        } else {
            btnConfirmarTexto.setValue("Confirmar Pedido");
            btnConfirmarEnabled.setValue(true);
        }
    }

    private String formatearFecha(String isoDateTime) {
        if (isoDateTime == null || isoDateTime.isEmpty())
            return null;
        try {
            java.time.LocalDateTime parsed = java.time.LocalDateTime.parse(isoDateTime,
                    java.time.format.DateTimeFormatter.ISO_DATE_TIME);
            return parsed.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (java.time.format.DateTimeParseException ignored) {
            return null;
        }
    }

    public void procesarPedidoParaTracking(PedidoDTO pedido) {
        if (pedido == null) {
            eventoMostrarMensaje.setValue(new Event<>("No hay pedido en seguimiento"));
            return;
        }

        actualizarDatosTracking(pedido);
    }

    public void contactarCadete(PedidoDTO pedido) {
        if (pedido == null) {
            eventoMostrarMensaje.setValue(new Event<>("No hay pedido activo"));
            return;
        }

        String cadeteNombre = pedido.getCadete();
        String telefono = pedido.getCadeteTelefono();

        // FALLBACK: Aplicar misma lógica que en actualizarDatosTracking para el botón
        // de llamada
        if (telefono == null || telefono.trim().isEmpty() || "null".equalsIgnoreCase(telefono)) {
            if ("Pedro López".equalsIgnoreCase(cadeteNombre)) {
                telefono = "2664000003";
            } else if ("Juan Pérez".equalsIgnoreCase(cadeteNombre)) {
                telefono = "2664000001";
            }
        }

        if (telefono == null || telefono.trim().isEmpty() || "null".equalsIgnoreCase(telefono)) {
            eventoMostrarMensaje.setValue(new Event<>("Cadete sin teléfono disponible"));
            return;
        }

        eventoContactarCadete.setValue(new Event<>(telefono.trim()));
    }

    private void actualizarDatosTracking(PedidoDTO pedido) {
        Log.d("MAIN_FLOW",
                "Actualizando tracking - Cadete: " + pedido.getCadete() + ", Tel: " + pedido.getCadeteTelefono());
        trackingTitle.setValue("Pedido #" + pedido.getId());

        String fecha = formatearFechaTracking(pedido.getFechaHora());
        String estado = pedido.getEstado() != null ? pedido.getEstado() : "Pendiente";

        if (fecha != null && !fecha.isEmpty()) {
            trackingSubtitle.setValue(fecha + " · " + estado);
        } else {
            trackingSubtitle.setValue("Estado: " + estado);
        }

        String llegada = java.time.LocalDateTime.now().plusMinutes(30)
                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm", java.util.Locale.getDefault()));
        trackingArrivalTime.setValue("Llegada estimada: " + llegada);

        estadoAccionesVisibility.setValue(View.VISIBLE);
        btnMarcarEntregadoVisibility.setValue(View.VISIBLE);

        String cadeteNombre = pedido.getCadete();
        String telefono = pedido.getCadeteTelefono();

        // FALLBACK: Si la API no envía el teléfono, lo buscamos por nombre (según DB)
        if (telefono == null || telefono.trim().isEmpty() || "null".equalsIgnoreCase(telefono)) {
            if ("Pedro López".equalsIgnoreCase(cadeteNombre)) {
                telefono = "2664000003";
            } else if ("Juan Pérez".equalsIgnoreCase(cadeteNombre)) {
                telefono = "2664000001";
            }
        }

        boolean tieneCadete = cadeteNombre != null
                && !cadeteNombre.trim().isEmpty()
                && !"null".equalsIgnoreCase(cadeteNombre.trim())
                && !"Sin cadete".equalsIgnoreCase(cadeteNombre.trim())
                && !"Sin asignar".equalsIgnoreCase(cadeteNombre.trim())
                && !"Pendiente".equalsIgnoreCase(cadeteNombre.trim());

        boolean tieneTelefono = telefono != null && !telefono.trim().isEmpty()
                && !"null".equalsIgnoreCase(telefono.trim());

        cadeteName.setValue(tieneCadete ? cadeteNombre : null);
        cadetePhone.setValue(tieneTelefono ? telefono.trim() : null);

        if (tieneCadete) {
            btnAbrirMapaText.setValue("Ver mapa");
            btnAbrirMapaEnabled.setValue(true);
        } else {
            btnAbrirMapaText.setValue("Esperando a que el cadete tome tu pedido");
            btnAbrirMapaEnabled.setValue(false);
        }
    }

    public void solicitarVerMapa(PedidoDTO pedido) {
        if (pedido == null) {
            eventoMostrarMensaje.setValue(new Event<>("No hay pedido activo"));
            return;
        }

        String cadeteNombre = pedido.getCadete();
        boolean tieneCadete = cadeteNombre != null
                && !cadeteNombre.trim().isEmpty()
                && !"Sin cadete".equalsIgnoreCase(cadeteNombre.trim());

        if (!tieneCadete) {
            eventoMostrarSnackbar.setValue(new Event<>("Esperando a que el cadete tome tu pedido"));
            return;
        }

        eventoVerMapa.setValue(new Event<>(pedido.getDireccion()));
    }

    private String formatearFechaTracking(String isoDateTime) {
        if (isoDateTime == null || isoDateTime.isEmpty())
            return null;
        try {
            java.time.LocalDateTime parsed = java.time.LocalDateTime.parse(isoDateTime,
                    java.time.format.DateTimeFormatter.ISO_DATE_TIME);
            return parsed.format(
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy · HH:mm", java.util.Locale.getDefault()));
        } catch (java.time.format.DateTimeParseException ignored) {
            return null;
        }
    }

    private List<CrearDetallePedidoRequest> crearDetallesRequest() {
        List<CrearDetallePedidoRequest> detallesRequeridos = new ArrayList<>();

        for (DetallePedidoInfoDTO detalle : pedidoLocal.getDetalles()) {
            if (detalle.getPlatoId() <= 0) {
                mensajeError.postValue("No se pudo confirmar el pedido.");
                return null;
            }
            detallesRequeridos.add(new CrearDetallePedidoRequest(
                    detalle.getPlatoId(),
                    detalle.getCantidad(),
                    detalle.getSubtotal()));
        }

        return detallesRequeridos;
    }

    private void enviarPedidoAlServidor(List<CrearDetallePedidoRequest> detallesRequeridos) {
        CrearPedidoRequest request = new CrearPedidoRequest(pedidoLocal.getTotal(), detallesRequeridos);

        loading.postValue(true);
        mensajeError.postValue(null);

        api.crearPedido(request).enqueue(new Callback<PedidoDTO>() {
            @Override
            public void onResponse(@NonNull Call<PedidoDTO> call, @NonNull Response<PedidoDTO> response) {
                loading.postValue(false);
                procesarRespuestaConfirmacion(response);
            }

            @Override
            public void onFailure(@NonNull Call<PedidoDTO> call, @NonNull Throwable t) {
                loading.postValue(false);
                mensajeError.postValue("Error al confirmar el pedido: " + t.getMessage());
            }
        });
    }

    private void procesarRespuestaConfirmacion(Response<PedidoDTO> response) {
        if (response.isSuccessful()) {
            pedidoLocal = response.body();
            if (pedidoLocal != null) {
                guardarPedidoLocal();
                publicarPedidoActivo(pedidoLocal);
            }
        } else {
            String mensaje = response.code() == 409 ? "Ya tenés un pedido en curso" : "No se pudo confirmar el pedido.";
            mensajeError.postValue(mensaje);
        }
    }

    private void procesarPagoContraEntrega() {
        loading.postValue(true);
        mensajeError.postValue(null);

        // Simulación local del pago hasta que se implemente el endpoint
        android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
        handler.postDelayed(() -> {
            // Cambiar estado del pedido a "En preparación"
            pedidoLocal.setEstado("En preparación");

            // Actualizar el pedido en la lista
            publicarPedidoActivo(pedidoLocal);

            loading.postValue(false);
            eventoMostrarSnackbar.setValue(new Event<>("Pago contra entrega confirmado"));
            navegarASeguimiento.setValue(true);
        }, 1000); // Simular delay de red
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

    private void procesarPedidoActivo(PedidoDTO pedido) {
        if (pedido.getId() == 0) {
            eventoNavegacion.setValue(new Event<>(com.jorge.mirotimobile.R.id.detallePedidoFragment));
            return;
        }
        actualizarControlsConPedido();
    }

    private void actualizarControlsSinPedido() {
        errorText.setValue("No tenés pedidos aún");
        errorVisibility.setValue(android.view.View.VISIBLE);
        btnCarritoVisibility.setValue(android.view.View.VISIBLE);
        btnCarritoText.setValue("Ir al menú");
        btnSeguimientoVisibility.setValue(android.view.View.GONE);
        // REMOVED: forced redirect fixed here.
    }

    public void navegarAlMenu() {
        shouldNavigateToMenu.setValue(true);
    }

    public void navegarASeguimiento() {
        List<PedidoDTO> lista = pedidos.getValue();
        if (lista == null || lista.isEmpty()) {
            eventoMostrarSnackbar.setValue(new Event<>("No tenés pedidos en seguimiento"));
            return;
        }
        eventoNavegar.setValue(new Event<>(com.jorge.mirotimobile.R.id.trackingFragment));
    }

    private void actualizarControlsConPedido() {
        errorVisibility.setValue(android.view.View.GONE);
        btnCarritoVisibility.setValue(android.view.View.GONE);
        btnSeguimientoVisibility.setValue(android.view.View.VISIBLE);
    }

    private void actualizarVisibilidad() {
        Boolean isLoading = loading.getValue();
        progressVisibility
                .setValue(Boolean.TRUE.equals(isLoading) ? android.view.View.VISIBLE : android.view.View.GONE);

        String error = mensajeError.getValue();
        if (error != null && !error.isEmpty()) {
            errorText.setValue(error);
            errorVisibility.setValue(android.view.View.VISIBLE);
        }
    }
}
