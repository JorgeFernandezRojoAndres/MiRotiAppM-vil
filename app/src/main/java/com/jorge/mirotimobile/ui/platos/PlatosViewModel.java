package com.jorge.mirotimobile.ui.platos;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jorge.mirotimobile.retrofit.ApiService;
import com.jorge.mirotimobile.retrofit.RetrofitClient;
import com.jorge.mirotimobile.localdata.SessionManager;
import com.jorge.mirotimobile.model.Plato;

import java.util.List;

import android.util.Log;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 🧠 PlatosViewModel — Maneja la carga de platos desde la API.
 * Aplica MVVM puro: sin lógica en la vista, usando LiveData para comunicar
 * cambios.
 * Envía el token JWT con prefijo "Bearer".
 */
public class PlatosViewModel extends AndroidViewModel {

    private final MutableLiveData<List<Plato>> platos = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<String> mensajeError = new MutableLiveData<>();
    private final MutableLiveData<Integer> progressVisibility = new MutableLiveData<>();
    private final MutableLiveData<Integer> errorVisibility = new MutableLiveData<>();
    private final MutableLiveData<List<Plato>> platosFiltered = new MutableLiveData<>();
    private final MutableLiveData<Integer> badgeCount = new MutableLiveData<>();
    private final MutableLiveData<Boolean> shouldAnimate = new MutableLiveData<>();
    private final MutableLiveData<BadgeData> badgeData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> shouldAnimateBadge = new MutableLiveData<>();
    private final MutableLiveData<String> greetingTitle = new MutableLiveData<>();
    private final MutableLiveData<String> metricTotalValue = new MutableLiveData<>();
    private final MutableLiveData<String> metricSaldoValue = new MutableLiveData<>();

    public static class BadgeData {
        public final int count;
        public final boolean visible;

        public BadgeData(int count, boolean visible) {
            this.count = count;
            this.visible = visible;
        }
    }

    private String currentFilter = "todos";
    private List<Plato> allPlatos = new java.util.ArrayList<>();

    private final SessionManager session;
    private final ApiService api;

    public PlatosViewModel(@NonNull Application application) {
        super(application);
        session = new SessionManager(application.getApplicationContext());
        api = RetrofitClient.getClient(application.getApplicationContext()).create(ApiService.class);
    }

    public LiveData<List<Plato>> getPlatos() {
        return platos;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getMensajeError() {
        return mensajeError;
    }

    public LiveData<Integer> getProgressVisibility() {
        return progressVisibility;
    }

    public LiveData<Integer> getErrorVisibility() {
        return errorVisibility;
    }

    public LiveData<List<Plato>> getPlatosFiltered() {
        return platosFiltered;
    }

    public LiveData<Integer> getBadgeCount() {
        return badgeCount;
    }

    public LiveData<Boolean> getShouldAnimate() {
        return shouldAnimate;
    }

    public LiveData<BadgeData> getBadgeData() {
        return badgeData;
    }

    public LiveData<Boolean> getShouldAnimateBadge() {
        return shouldAnimateBadge;
    }

    public LiveData<String> getGreetingTitle() {
        return greetingTitle;
    }

    public LiveData<String> getMetricTotalValue() {
        return metricTotalValue;
    }

    public LiveData<String> getMetricSaldoValue() {
        return metricSaldoValue;
    }

    public void cargarPlatos() {
        iniciarCarga();

        if (!esSesionValida()) {
            return;
        }

        ejecutarPeticionPlatos();
    }

    private void iniciarCarga() {
        loading.postValue(true);
        mensajeError.postValue(null);
        actualizarVisibilidad();
    }

    private boolean esSesionValida() {
        String token = session.getToken();
        if (token == null || token.isEmpty()) {
            loading.postValue(false);
            mensajeError.postValue("Sesión inválida. Iniciá sesión nuevamente.");
            actualizarVisibilidad();
            return false;
        }
        return true;
    }

    private void ejecutarPeticionPlatos() {
        api.obtenerPlatos().enqueue(new Callback<List<Plato>>() {
            @Override
            public void onResponse(@NonNull Call<List<Plato>> call, @NonNull Response<List<Plato>> response) {
                loading.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    allPlatos = new java.util.ArrayList<>(response.body());
                    platos.postValue(allPlatos);
                    applyFilter();
                } else {
                    mensajeError.postValue(obtenerMensajeError(response.code()));
                }
                actualizarVisibilidad();
            }

            @Override
            public void onFailure(@NonNull Call<List<Plato>> call, @NonNull Throwable t) {
                loading.postValue(false);
                mensajeError.postValue("Error de conexión: " + t.getMessage());
                actualizarVisibilidad();
            }
        });
    }

    public void filtrar(String category) {
        currentFilter = category;
        applyFilter();
    }

    public void updateBadgeFromPedidos(List<com.jorge.mirotimobile.model.PedidoDTO> pedidos) {
        int cantidad = 0;
        if (pedidos != null && !pedidos.isEmpty()) {
            com.jorge.mirotimobile.model.PedidoDTO pedido = pedidos.get(0);
            if (pedido.getDetalles() != null) {
                for (com.jorge.mirotimobile.model.DetallePedidoInfoDTO detalle : pedido.getDetalles()) {
                    cantidad += detalle.getCantidad();
                }
            }
        }

        Integer prevCount = badgeCount.getValue();

        badgeCount.setValue(cantidad);
        badgeData.setValue(new BadgeData(cantidad, cantidad > 0));

        boolean shouldAnimateNow = prevCount != null && cantidad > prevCount;
        shouldAnimate.setValue(shouldAnimateNow);
        shouldAnimateBadge.setValue(shouldAnimateNow);
    }

    public void configurarSaludo(String email) {
        String nombre = "Cliente";
        if (email != null && email.contains("@")) {
            nombre = email.substring(0, email.indexOf('@'));
        }
        greetingTitle.setValue("¡Bienvenido, " + nombre + "!");
    }

    public void actualizarMetricas(List<Plato> lista) {
        java.text.NumberFormat currency = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("es", "AR"));

        if (lista == null || lista.isEmpty()) {
            metricTotalValue.setValue("0");
            metricSaldoValue.setValue(currency.format(0));
            return;
        }

        int total = lista.size();
        double sum = 0;
        for (Plato p : lista) {
            sum += p.getPrecioVenta();
        }

        metricTotalValue.setValue(String.valueOf(total));
        metricSaldoValue.setValue(currency.format(sum));
    }

    private void applyFilter() {
        if (allPlatos.isEmpty()) {
            platosFiltered.setValue(java.util.Collections.emptyList());
            return;
        }

        if ("todos".equals(currentFilter)) {
            platosFiltered.setValue(new java.util.ArrayList<>(allPlatos));
            return;
        }

        java.util.List<Plato> filtered = new java.util.ArrayList<>();
        String query = currentFilter.toLowerCase(java.util.Locale.ROOT);

        // Maneja singular/plural: "pollos" busca "pollo", "ensaladas" busca "ensalada",
        // etc.
        String querySingular = query.endsWith("s") ? query.substring(0, query.length() - 1) : query;

        for (Plato plato : allPlatos) {
            // Busca en nombre y descripción
            String nombre = plato.getNombre() != null ? plato.getNombre().toLowerCase(java.util.Locale.ROOT) : "";
            String descripcion = plato.getDescripcion() != null
                    ? plato.getDescripcion().toLowerCase(java.util.Locale.ROOT)
                    : "";
            String textoCombinado = nombre + " " + descripcion;

            // Búsqueda flexible: busca por la palabra (singular o plural)
            if (textoCombinado.contains(query) || textoCombinado.contains(querySingular)) {
                filtered.add(plato);
            }
        }

        platosFiltered.setValue(filtered);
    }

    private boolean validarSesion() {
        String token = session.getToken();
        if (token == null || token.isEmpty()) {
            loading.postValue(false);
            mensajeError.postValue("Sesión inválida. Iniciá sesión nuevamente.");
            actualizarVisibilidad();
            return false;
        }
        return true;
    }

    private void procesarRespuestaPlatos(Response<List<Plato>> response) {
        if (response.isSuccessful() && response.body() != null) {
            allPlatos = new java.util.ArrayList<>(response.body());
            platos.postValue(response.body());
            applyFilter();
        } else {
            String mensaje = obtenerMensajeError(response.code());
            mensajeError.postValue(mensaje);
        }
    }

    private String obtenerMensajeError(int codigo) {
        switch (codigo) {
            case 401:
                return "Token inválido o expirado. Iniciá sesión nuevamente.";
            case 404:
                return "No se encontraron platos disponibles.";
            default:
                return "Error al obtener los platos (HTTP " + codigo + ")";
        }
    }

    private void actualizarVisibilidad() {
        Boolean isLoading = loading.getValue();
        progressVisibility
                .setValue(Boolean.TRUE.equals(isLoading) ? android.view.View.VISIBLE : android.view.View.GONE);

        String error = mensajeError.getValue();
        errorVisibility
                .setValue(error != null && !error.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
    }
}
