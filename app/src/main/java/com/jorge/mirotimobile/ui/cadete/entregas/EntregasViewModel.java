package com.jorge.mirotimobile.ui.cadete.entregas;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;

import com.jorge.mirotimobile.retrofit.ApiService;
import com.jorge.mirotimobile.retrofit.RetrofitClient;
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
                boolean mostrarCompletado) {
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
    private final MutableLiveData<String> productosTexto = new MutableLiveData<>();
    private final MutableLiveData<String> totalTexto = new MutableLiveData<>();
    private final MutableLiveData<String> notasTexto = new MutableLiveData<>();
    private final MutableLiveData<String> trackingTitle = new MutableLiveData<>();
    private final MutableLiveData<String> trackingSubtitle = new MutableLiveData<>();
    private final MutableLiveData<String> arrivalTime = new MutableLiveData<>();
    private final MutableLiveData<String> mensajeError = new MutableLiveData<>();
    private final MutableLiveData<String> mensajeExito = new MutableLiveData<>();
    private final MutableLiveData<Event<String>> eventoLlamarTelefono = new MutableLiveData<>();
    private final MutableLiveData<Integer> estadoIconRes = new MutableLiveData<>();
    private final MutableLiveData<String> estadoTitle = new MutableLiveData<>();
    private final MutableLiveData<String> estadoSubtitle = new MutableLiveData<>();
    private final MutableLiveData<Integer> mapButtonVisibility = new MutableLiveData<>();
    private final MutableLiveData<Integer> strokeColor = new MutableLiveData<>();
    private final MutableLiveData<Integer> strokeWidth = new MutableLiveData<>();
    private final MutableLiveData<Integer> cardStrokeColor = new MutableLiveData<>();
    private final MutableLiveData<Integer> cardStrokeWidth = new MutableLiveData<>();
    private final MutableLiveData<Integer> cardBackgroundColor = new MutableLiveData<>();
    private final MutableLiveData<android.content.res.ColorStateList> buttonStrokeColor = new MutableLiveData<>();
    private final MutableLiveData<Integer> buttonStrokeWidth = new MutableLiveData<>();
    private final MutableLiveData<android.content.res.ColorStateList> buttonBackgroundTint = new MutableLiveData<>();
    private final MutableLiveData<ColorFilterData> iconColorFilter = new MutableLiveData<>();
    private final MutableLiveData<Integer> cardEntregaActualVisibility = new MutableLiveData<>();
    private final MutableLiveData<Integer> cardSinEntregaVisibility = new MutableLiveData<>();
    private final MutableLiveData<Integer> cardEstadoBackgroundColor = new MutableLiveData<>();
    private final MutableLiveData<String> estadoEntregaTitulo = new MutableLiveData<>();
    private final MutableLiveData<String> estadoEntregaDescripcion = new MutableLiveData<>();
    private final MutableLiveData<Integer> btnTomarPedidoVisibility = new MutableLiveData<>();
    private final MutableLiveData<Integer> btnIniciarEntregaVisibility = new MutableLiveData<>();

    public static class ColorFilterData {
        public final int color;
        public final android.graphics.PorterDuff.Mode mode;

        public ColorFilterData(int color, android.graphics.PorterDuff.Mode mode) {
            this.color = color;
            this.mode = mode;
        }
    }

    private final MutableLiveData<String> pedidoActualId = new MutableLiveData<>();
    private final MutableLiveData<String> pedidoActualDireccion = new MutableLiveData<>();
    private final MutableLiveData<String> pedidoActualCliente = new MutableLiveData<>();
    private final MutableLiveData<Integer> sinEntregasVisibility = new MutableLiveData<>();
    private final MutableLiveData<Event<Boolean>> eventoNavegarEntregas = new MutableLiveData<>();
    private final MutableLiveData<Integer> historialTituloVisibility = new MutableLiveData<>();
    private final MutableLiveData<String> historialItems = new MutableLiveData<>();
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
        pedidosAsignadosService = RetrofitClient.getClient(application.getApplicationContext())
                .create(PedidosAsignadosService.class);

        boolean initialEnServicio = sessionManager.isCadeteEnServicio();
        cadeteEnServicio.setValue(initialEnServicio);
        estadoCadete.setValue(mapTextoCadeteEnServicio(initialEnServicio));

        String nombreGuardado = sessionManager.getUserName();
        if (nombreGuardado == null || nombreGuardado.isEmpty()) {
            nombreGuardado = sessionManager.getUserEmail();
        }
        nombreCadete.setValue(nombreGuardado != null && !nombreGuardado.isEmpty() ? nombreGuardado : "Cadete");

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
                    if (!TextUtils.isEmpty(perfil.getNombre())) {
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

    public LiveData<String> getProductosTexto() {
        return productosTexto;
    }

    public LiveData<String> getTotalTexto() {
        return totalTexto;
    }

    public LiveData<String> getNotasTexto() {
        return notasTexto;
    }

    public LiveData<String> getTrackingTitle() {
        return trackingTitle;
    }

    public LiveData<String> getTrackingSubtitle() {
        return trackingSubtitle;
    }

    public LiveData<String> getArrivalTime() {
        return arrivalTime;
    }

    public LiveData<String> getMensajeError() {
        return mensajeError;
    }

    public LiveData<String> getMensajeExito() {
        return mensajeExito;
    }

    public LiveData<Event<String>> getEventoLlamarTelefono() {
        return eventoLlamarTelefono;
    }

    public LiveData<Integer> getEstadoIconRes() {
        return estadoIconRes;
    }

    public LiveData<String> getEstadoTitle() {
        return estadoTitle;
    }

    public LiveData<String> getEstadoSubtitle() {
        return estadoSubtitle;
    }

    public LiveData<Integer> getMapButtonVisibility() {
        return mapButtonVisibility;
    }

    public LiveData<Integer> getStrokeColor() {
        return strokeColor;
    }

    public LiveData<Integer> getStrokeWidth() {
        return strokeWidth;
    }

    public LiveData<Integer> getCardStrokeColor() {
        return cardStrokeColor;
    }

    public LiveData<Integer> getCardStrokeWidth() {
        return cardStrokeWidth;
    }

    public LiveData<Integer> getCardBackgroundColor() {
        return cardBackgroundColor;
    }

    public LiveData<android.content.res.ColorStateList> getButtonStrokeColor() {
        return buttonStrokeColor;
    }

    public LiveData<Integer> getButtonStrokeWidth() {
        return buttonStrokeWidth;
    }

    public LiveData<android.content.res.ColorStateList> getButtonBackgroundTint() {
        return buttonBackgroundTint;
    }

    public LiveData<ColorFilterData> getIconColorFilter() {
        return iconColorFilter;
    }

    public LiveData<Integer> getCardEntregaActualVisibility() {
        return cardEntregaActualVisibility;
    }

    public LiveData<Integer> getCardSinEntregaVisibility() {
        return cardSinEntregaVisibility;
    }

    public LiveData<Integer> getCardEstadoBackgroundColor() {
        return cardEstadoBackgroundColor;
    }

    public LiveData<String> getEstadoEntregaTitulo() {
        return estadoEntregaTitulo;
    }

    public LiveData<String> getEstadoEntregaDescripcion() {
        return estadoEntregaDescripcion;
    }

    public LiveData<Integer> getBtnTomarPedidoVisibility() {
        return btnTomarPedidoVisibility;
    }

    public LiveData<Integer> getBtnIniciarEntregaVisibility() {
        return btnIniciarEntregaVisibility;
    }

    public LiveData<String> getPedidoActualId() {
        return pedidoActualId;
    }

    public LiveData<String> getPedidoActualDireccion() {
        return pedidoActualDireccion;
    }

    public LiveData<String> getPedidoActualCliente() {
        return pedidoActualCliente;
    }

    public LiveData<Integer> getSinEntregasVisibility() {
        return sinEntregasVisibility;
    }

    public LiveData<Integer> getHistorialTituloVisibility() {
        return historialTituloVisibility;
    }

    public LiveData<String> getHistorialItems() {
        return historialItems;
    }

    public LiveData<Event<Boolean>> getEventoNavegarEntregas() {
        return eventoNavegarEntregas;
    }

    public void cerrarEntregaActual() {
        PedidoDTO actual = pedidoActual.getValue();
        if (actual == null)
            return;

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
                if (!response.isSuccessful())
                    return;

                List<PedidoDTO> asignados = response.body();
                if (asignados != null && !asignados.isEmpty()) {
                    setPedidoActual(asignados.get(0));
                } else {
                    setPedidoActual(null);
                }
            }

            @Override
            public void onFailure(Call<List<PedidoDTO>> call, Throwable t) {
                setPedidoActual(null);
            }
        });
    }

    private void cargarPedidosDisponibles() {
        apiService.obtenerPedidosDisponibles().enqueue(new Callback<List<PedidoDTO>>() {
            @Override
            public void onResponse(Call<List<PedidoDTO>> call, Response<List<PedidoDTO>> response) {
                if (!response.isSuccessful())
                    return;

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
        if (actual == null)
            return;
        iniciarEntrega(actual.getId());
    }

    public void tomarPedido() {
        PedidoDTO actual = pedidoActual.getValue();
        if (actual == null)
            return;
        actualizarPedidoActual(actual, ESTADO_ASIGNADO_AL_CADETE);
    }

    public void tomarPedido(int idPedido) {
        if (pedidoActual.getValue() != null)
            return;
        apiService.tomarPedido(idPedido).enqueue(new Callback<PedidoDTO>() {
            @Override
            public void onResponse(Call<PedidoDTO> call, Response<PedidoDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    setPedidoActual(response.body());

                    List<PedidoDTO> actuales = proximasEntregas.getValue();
                    if (actuales == null)
                        return;
                    List<PedidoDTO> restantes = new ArrayList<>();
                    for (PedidoDTO p : actuales) {
                        if (p != null && p.getId() != idPedido)
                            restantes.add(p);
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
                    eventoNavegarEntregas.setValue(new Event<>(true));
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
        if (actual == null) {
            mensajeError.setValue("Dirección del cliente no disponible");
            return;
        }

        Log.d("MAPA_DEBUG", new com.google.gson.Gson().toJson(actual));

        String direccion = actual.getDireccion();
        if (direccion == null) {
            mensajeError.setValue("Dirección del cliente no disponible");
            return;
        }
        String direccionTrimmed = direccion.trim();
        if (TextUtils.isEmpty(direccionTrimmed)) {
            mensajeError.setValue("Dirección del cliente no disponible");
            return;
        }
        eventoAbrirMapa.setValue(new Event<>(direccionTrimmed));
    }

    public void marcarEntrega() {
        PedidoDTO actual = pedidoActual.getValue();
        if (actual == null) {
            mensajeError.setValue("No hay pedido activo");
            return;
        }
        marcarEntregaCompletada();
        mensajeExito.setValue("Entrega completada");
    }

    public void contactarCliente() {
        PedidoDTO actual = pedidoActual.getValue();
        if (actual == null) {
            mensajeError.setValue("No hay pedido activo");
            return;
        }
        String telefono = actual.getTelefono();
        if (telefono == null) {
            mensajeError.setValue("Teléfono no disponible");
            return;
        }
        String telefonoTrimmed = telefono.trim();
        if (TextUtils.isEmpty(telefonoTrimmed)) {
            mensajeError.setValue("Teléfono no disponible");
            return;
        }
        eventoLlamarTelefono.setValue(new Event<>(telefonoTrimmed));
    }

    private EstadoEntrega mapEstado(String estado) {
        if (estado == null)
            return EstadoEntrega.EN_ESPERA;
        String normalized = estado.trim().toUpperCase();
        switch (normalized) {
            case ESTADO_EN_PREPARACION:
            case "EN PREPARACION":
            case "PENDIENTE":
                return EstadoEntrega.EN_PREPARACION;
            case ESTADO_ASIGNADO_AL_CADETE:
            case "ASIGNADO":
                return EstadoEntrega.ASIGNADO_AL_CADETE;
            case "EN_CAMINO":
            case "EN_PROCESO":
            case "EN CAMINO":
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
        pedidoActual.setValue(pedido);
        puedeTomarPedidos.setValue(pedido == null);

        EstadoEntrega estado = mapEstado(pedido != null ? pedido.getEstado() : null);
        estadoEntrega.setValue(estado);

        EstadoEntregaUiState uiState = mapEstadoEntregaUi(estado);
        estadoEntregaUi.setValue(uiState);

        if (pedido != null) {
            actualizarTextosUI(pedido);
            actualizarEstadoUI(estado);
            actualizarPedidoActualUI(pedido);
        }
        actualizarEstadoEntregaUI(uiState);
    }

    private void actualizarTextosUI(PedidoDTO pedido) {
        if (pedido == null) {
            trackingTitle.setValue("Seguimiento del Cadete");
            trackingSubtitle.setValue("Esperando asignación de pedido");
            arrivalTime.setValue("Se mostrará la llegada una vez asignado");
            productosTexto.setValue("");
            totalTexto.setValue("");
            notasTexto.setValue("");
            return;
        }

        try {
            // Tracking info
            trackingTitle.setValue("Entrega #" + pedido.getId());
            trackingSubtitle.setValue(formatFechayEstado(pedido));

            // Arrival time (20 min from now)
            java.time.LocalDateTime llegada = java.time.LocalDateTime.now().plusMinutes(20);
            String llegadaTexto = "Llegada estimada: " + llegada
                    .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm", java.util.Locale.getDefault()));
            arrivalTime.setValue(llegadaTexto);

            // Productos
            StringBuilder productos = new StringBuilder("Productos: ");
            if (pedido.getDetalles() != null && !pedido.getDetalles().isEmpty()) {
                for (int i = 0; i < pedido.getDetalles().size(); i++) {
                    com.jorge.mirotimobile.model.DetallePedidoInfoDTO detalle = pedido.getDetalles().get(i);
                    productos.append(detalle.getCantidad()).append("x ").append(detalle.getPlato());
                    if (i < pedido.getDetalles().size() - 1) {
                        productos.append(", ");
                    }
                }
            } else {
                productos.append("Sin detalles");
            }
            productosTexto.setValue(productos.toString());

            // Total
            totalTexto.setValue("Total: $" + String.format("%.0f", pedido.getTotal()));

            // Notas
            String direccion = pedido.getDireccion() != null ? pedido.getDireccion() : "dirección no disponible";
            notasTexto.setValue("Notas: Entregar en " + direccion);
        } catch (Exception e) {
            // Valores por defecto en caso de error
            trackingTitle.setValue("Entrega #" + pedido.getId());
            trackingSubtitle.setValue("Estado: " + (pedido.getEstado() != null ? pedido.getEstado() : "Pendiente"));
            arrivalTime.setValue("Calculando tiempo...");
            productosTexto.setValue("Productos: Sin detalles");
            totalTexto.setValue("Total: $" + String.format("%.0f", pedido.getTotal()));
            notasTexto.setValue("Notas: Entregar pedido");
        }
    }

    private String formatFechayEstado(PedidoDTO pedido) {
        String fecha = pedido.getFechaHora();
        String estado = pedido.getEstado() != null ? pedido.getEstado() : "Pendiente";
        if (fecha != null && !fecha.isEmpty()) {
            return fecha + " · " + estado;
        }
        return "Estado: " + estado;
    }

    private void actualizarEstadoUI(EstadoEntrega estado) {
        try {
            boolean enCamino = estado == EstadoEntrega.EN_CAMINO;

            estadoIconRes.setValue(enCamino ? android.R.drawable.ic_dialog_map : android.R.drawable.ic_popup_sync);

            estadoTitle.setValue(enCamino ? "Entrega en camino" : "Sin entrega activa");
            estadoSubtitle.setValue(enCamino ? "Abrí el mapa para llegar al domicilio del cliente"
                    : "Tomá un pedido desde Próximas entregas");

            mapButtonVisibility.setValue(enCamino ? android.view.View.VISIBLE : android.view.View.GONE);

            strokeColor.setValue(enCamino ? com.jorge.mirotimobile.R.color.miroti_orange
                    : com.jorge.mirotimobile.R.color.cadete_state_espera);

            strokeWidth.setValue(enCamino ? 2 : 0);

            actualizarEstilosUI(enCamino);
        } catch (Exception e) {
            // Valores por defecto en caso de error
            estadoIconRes.setValue(android.R.drawable.ic_popup_sync);
            estadoTitle.setValue("Estado no disponible");
            estadoSubtitle.setValue("Cargando...");
            mapButtonVisibility.setValue(android.view.View.GONE);
        }
    }

    private void actualizarEstilosUI(boolean enCamino) {
        try {
            android.content.Context context = getApplication().getApplicationContext();

            int colorRes = enCamino
                    ? androidx.core.content.ContextCompat.getColor(context,
                            com.jorge.mirotimobile.R.color.miroti_orange)
                    : androidx.core.content.ContextCompat.getColor(context,
                            com.jorge.mirotimobile.R.color.cadete_state_espera);

            int backgroundColorRes = androidx.core.content.ContextCompat.getColor(context,
                    com.jorge.mirotimobile.R.color.cadete_state_espera);
            int whiteColor = androidx.core.content.ContextCompat.getColor(context, android.R.color.white);

            cardStrokeColor.setValue(colorRes);
            cardStrokeWidth.setValue(
                    (int) Math.round(context.getResources().getDisplayMetrics().density * (enCamino ? 2 : 0)));
            cardBackgroundColor.setValue(backgroundColorRes);

            android.content.res.ColorStateList colorStateList = android.content.res.ColorStateList.valueOf(colorRes);
            buttonStrokeColor.setValue(colorStateList);
            buttonStrokeWidth.setValue(
                    (int) Math.round(context.getResources().getDisplayMetrics().density * (enCamino ? 2 : 0)));
            buttonBackgroundTint.setValue(android.content.res.ColorStateList.valueOf(backgroundColorRes));

            iconColorFilter.setValue(new ColorFilterData(whiteColor, android.graphics.PorterDuff.Mode.SRC_IN));
        } catch (Exception e) {
            // No hacer nada si hay error en estilos
        }
    }

    private void actualizarEstadoEntregaUI(EstadoEntregaUiState ui) {
        android.content.Context context = getApplication().getApplicationContext();

        cardEntregaActualVisibility
                .setValue(ui.mostrarEntregaActual ? android.view.View.VISIBLE : android.view.View.GONE);
        cardSinEntregaVisibility.setValue(ui.mostrarEntregaActual ? android.view.View.GONE : android.view.View.VISIBLE);

        cardEstadoBackgroundColor.setValue(
                androidx.core.content.ContextCompat.getColor(context, ui.backgroundColorRes));

        estadoEntregaTitulo.setValue(ui.titulo);
        estadoEntregaDescripcion.setValue(ui.descripcion);

        btnTomarPedidoVisibility.setValue(ui.mostrarTomarPedido ? android.view.View.VISIBLE : android.view.View.GONE);
        btnIniciarEntregaVisibility
                .setValue(ui.mostrarIniciarEntrega ? android.view.View.VISIBLE : android.view.View.GONE);
    }

    private void actualizarPedidoActualUI(PedidoDTO pedido) {
        if (pedido == null) {
            pedidoActualId.setValue("");
            pedidoActualDireccion.setValue("");
            pedidoActualCliente.setValue("");
            return;
        }

        pedidoActualId.setValue("Pedido #" + pedido.getId());
        pedidoActualDireccion.setValue(pedido.getDireccion() != null ? pedido.getDireccion() : "");
        pedidoActualCliente.setValue(pedido.getCliente() != null ? pedido.getCliente() : "");
    }

    public void onProximasEntregasChanged(List<PedidoDTO> proximas) {
        sinEntregasVisibility
                .setValue(proximas == null || proximas.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
    }

    public void onHistorialChanged(List<PedidoDTO> historial) {
        if (historial == null || historial.isEmpty()) {
            historialTituloVisibility.setValue(android.view.View.GONE);
            historialItems.setValue("");
            return;
        }

        historialTituloVisibility.setValue(android.view.View.VISIBLE);
        StringBuilder items = new StringBuilder();
        for (PedidoDTO pedido : historial) {
            items.append(String.format("Pedido #%d · %s\n", pedido.getId(),
                    pedido.getEstado() != null ? pedido.getEstado() : ""));
        }
        historialItems.setValue(items.toString());
    }

    private void actualizarPedidoActual(PedidoDTO actual, String nuevoEstado) {
        Log.d("ENTREGA_FLOW", "actualizarPedidoActual: " + nuevoEstado);
        actual.setEstado(nuevoEstado);
        Log.d("ENTREGA_FLOW", "Calling setPedidoActual");
        setPedidoActual(actual);
        Log.d("ENTREGA_FLOW", "actualizarPedidoActual COMPLETE");
    }

    private EstadoEntregaUiState mapEstadoEntregaUi(EstadoEntrega estado) {
        if (estado == null)
            estado = EstadoEntrega.EN_ESPERA;
        switch (estado) {
            case EN_PREPARACION:
                return new EstadoEntregaUiState(
                        com.jorge.mirotimobile.R.color.estado_proceso,
                        "🍽️ En preparación",
                        "Pedido asignado y en preparación.",
                        true,
                        false,
                        true,
                        false,
                        false);
            case ASIGNADO_AL_CADETE:
                return new EstadoEntregaUiState(
                        com.jorge.mirotimobile.R.color.estado_proceso,
                        "📦 Asignado al cadete",
                        "Cuando estés listo, iniciá la entrega.",
                        true,
                        false,
                        true,
                        false,
                        false);
            case EN_CAMINO:
                return new EstadoEntregaUiState(
                        com.jorge.mirotimobile.R.color.estado_proceso,
                        "🚴 En camino",
                        "Dirígete a la dirección del pedido y mantenete al tanto.",
                        true,
                        false,
                        false,
                        true,
                        false);
            case ENTREGADO:
                return new EstadoEntregaUiState(
                        com.jorge.mirotimobile.R.color.estado_entregado,
                        "✅ Entrega finalizada",
                        "Gracias por entregar a tiempo.",
                        true,
                        false,
                        false,
                        false,
                        true);
            case EN_ESPERA:
            default:
                return new EstadoEntregaUiState(
                        com.jorge.mirotimobile.R.color.nav_icon_inactive,
                        "No tienes pedidos activos",
                        "Esperando nuevos pedidos",
                        false,
                        false,
                        false,
                        false,
                        false);
        }
    }
}
