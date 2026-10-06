package com.jorge.mirotimobile.ui.insumos;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jorge.mirotimobile.localdata.SessionManager;
import com.jorge.mirotimobile.model.Ingrediente;
import com.jorge.mirotimobile.model.IngredienteCrearRequest;
import com.jorge.mirotimobile.model.IngredientePrecioRequest;
import com.jorge.mirotimobile.retrofit.ApiService;
import com.jorge.mirotimobile.retrofit.RetrofitClient;
import com.jorge.mirotimobile.util.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.util.Log;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 🧠 InsumosViewModel — Maneja la carga, creación y edición de ingredientes
 * desde la API MiRoti. Rol: Administrador de Insumos.
 * Aplica MVVM puro igual que PlatosViewModel, con token JWT "Bearer".
 */
public class InsumosViewModel extends AndroidViewModel {

    private static final String TAG = "INSUMOS_FLOW";

    private final MutableLiveData<List<Ingrediente>> ingredientes = new MutableLiveData<>();
    private final MutableLiveData<List<Ingrediente>> ingredientesFiltered = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<Integer> progressVisibility = new MutableLiveData<>();
    private final MutableLiveData<Integer> errorVisibility = new MutableLiveData<>();
    private final MutableLiveData<String> mensajeError = new MutableLiveData<>();
    private final MutableLiveData<Event<EventoGuardado>> eventoGuardado = new MutableLiveData<>();

    private String filtroActual = "";
    private List<Ingrediente> todosLosIngredientes = new ArrayList<>();

    private final SessionManager session;
    private final ApiService api;

    /** Resultado de guardar/actualizar un ingrediente. */
    public static class EventoGuardado {
        public final boolean exitoso;
        public final String mensaje;

        public EventoGuardado(boolean exitoso, String mensaje) {
            this.exitoso = exitoso;
            this.mensaje = mensaje;
        }
    }

    public InsumosViewModel(@NonNull Application application) {
        super(application);
        session = new SessionManager(application.getApplicationContext());
        api = RetrofitClient.getClient(application.getApplicationContext()).create(ApiService.class);
    }

    public LiveData<List<Ingrediente>> getIngredientes() {
        return ingredientes;
    }

    public LiveData<List<Ingrediente>> getIngredientesFiltered() {
        return ingredientesFiltered;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<Integer> getProgressVisibility() {
        return progressVisibility;
    }

    public LiveData<Integer> getErrorVisibility() {
        return errorVisibility;
    }

    public LiveData<String> getMensajeError() {
        return mensajeError;
    }

    public LiveData<Event<EventoGuardado>> getEventoGuardado() {
        return eventoGuardado;
    }

    public void cargarIngredientes() {
        loading.postValue(true);
        mensajeError.postValue(null);
        actualizarVisibilidad();

        if (!esSesionValida()) {
            return;
        }

        api.obtenerIngredientes().enqueue(new Callback<List<Ingrediente>>() {
            @Override
            public void onResponse(@NonNull Call<List<Ingrediente>> call,
                                   @NonNull Response<List<Ingrediente>> response) {
                loading.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    todosLosIngredientes = new ArrayList<>(response.body());
                    ingredientes.postValue(todosLosIngredientes);
                    aplicarFiltro();
                } else {
                    mensajeError.postValue(obtenerMensajeError(response.code(), response.errorBody()));
                }
                actualizarVisibilidad();
            }

            @Override
            public void onFailure(@NonNull Call<List<Ingrediente>> call, @NonNull Throwable t) {
                loading.postValue(false);
                mensajeError.postValue("Error de conexión: " + t.getMessage());
                actualizarVisibilidad();
            }
        });
    }

    public void filtrar(String query) {
        filtroActual = query != null ? query : "";
        aplicarFiltro();
    }

    private void aplicarFiltro() {
        if (todosLosIngredientes.isEmpty()) {
            ingredientesFiltered.setValue(new ArrayList<>());
            return;
        }

        String query = filtroActual.trim().toLowerCase(Locale.ROOT);
        if (query.isEmpty()) {
            ingredientesFiltered.setValue(new ArrayList<>(todosLosIngredientes));
            return;
        }

        List<Ingrediente> filtrados = new ArrayList<>();
        for (Ingrediente ing : todosLosIngredientes) {
            String nombre = ing.getNombre() != null ? ing.getNombre().toLowerCase(Locale.ROOT) : "";
            String descripcion = ing.getDescripcion() != null ? ing.getDescripcion().toLowerCase(Locale.ROOT) : "";
            String textoCombinado = nombre + " " + descripcion;
            if (textoCombinado.contains(query)) {
                filtrados.add(ing);
            }
        }
        ingredientesFiltered.setValue(filtrados);
    }

    /**
     * Crea un ingrediente nuevo (id == 0) o actualiza uno existente (id > 0).
     */
    public void guardarIngrediente(Ingrediente ingrediente) {
        loading.postValue(true);
        mensajeError.postValue(null);
        actualizarVisibilidad();

        if (!esSesionValida()) {
            return;
        }

        boolean esNuevo = ingrediente.getId() == 0;
        Call<Ingrediente> call;
        if (esNuevo) {
            // POST: el backend espera { nombre, costoUnitario, unidadMedida: "kg" }
            IngredienteCrearRequest request = new IngredienteCrearRequest(
                    ingrediente.getNombre(),
                    ingrediente.getPrecio(),
                    ingrediente.getUnidadMedidaTexto());
            call = api.crearIngrediente(request);
        } else {
            // PUT: el backend espera { costoUnitario }
            call = api.actualizarIngrediente(ingrediente.getId(),
                    new IngredientePrecioRequest(ingrediente.getPrecio()));
        }

        call.enqueue(new Callback<Ingrediente>() {
            @Override
            public void onResponse(@NonNull Call<Ingrediente> call,
                                   @NonNull Response<Ingrediente> response) {
                loading.postValue(false);
                if (response.isSuccessful()) {
                    Log.d(TAG, "Ingrediente " + (esNuevo ? "creado" : "actualizado") + " (HTTP " + response.code() + ")");
                    eventoGuardado.postValue(new Event<>(new EventoGuardado(true,
                            esNuevo ? "Ingrediente cargado correctamente" : "Precio actualizado correctamente")));
                    cargarIngredientes(); // recargar lista desde el backend
                } else {
                    String msg = obtenerMensajeError(response.code(), response.errorBody());
                    mensajeError.postValue(msg);
                    eventoGuardado.postValue(new Event<>(new EventoGuardado(false, msg)));
                }
                actualizarVisibilidad();
            }

            @Override
            public void onFailure(@NonNull Call<Ingrediente> call, @NonNull Throwable t) {
                loading.postValue(false);
                String msg = "Error de conexión: " + t.getMessage();
                mensajeError.postValue(msg);
                eventoGuardado.postValue(new Event<>(new EventoGuardado(false, msg)));
                actualizarVisibilidad();
            }
        });
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

    /**
     * Traduce códigos HTTP y extrae { "mensaje": "..." } del error del backend
     * (ej. 409 ingrediente duplicado, 400 unidad inválida).
     */
    private String obtenerMensajeError(int codigo, okhttp3.ResponseBody errorBody) {
        // Intentar leer el mensaje del backend primero
        if (errorBody != null) {
            try {
                String json = errorBody.string();
                if (json != null && json.contains("mensaje")) {
                    // Extracción simple sin dependencias extra:
                    int idx = json.indexOf("mensaje");
                    int dosPuntos = json.indexOf(':', idx);
                    int inicio = json.indexOf('"', dosPuntos);
                    int fin = json.lastIndexOf('"');
                    if (dosPuntos > -1 && inicio > dosPuntos && fin > inicio) {
                        return json.substring(inicio + 1, fin);
                      }
                }
            } catch (Exception ignored) {
                // cae al mensaje genérico
            }
      }

        switch (codigo) {
            case 400:
                return "Datos inválidos. Verificá nombre, precio y unidad.";
            case 401:
                return "Token inválido o expirado. Iniciá sesión nuevamente.";
            case 403:
                return "No tenés permiso para esta acción (rol Administrador de Insumos requerido).";
            case 404:
                return "Ingrediente no encontrado o endpoint inexistente.";
            case 409:
                return "Ya existe un ingrediente con ese nombre.";
            default:
                return "Error (HTTP " + codigo + ") al procesar el ingrediente.";
        }
    }

    private void actualizarVisibilidad() {
        Boolean isLoading = loading.getValue();
        progressVisibility.setValue(Boolean.TRUE.equals(isLoading)
                ? android.view.View.VISIBLE : android.view.View.GONE);

        String error = mensajeError.getValue();
        errorVisibility.setValue(error != null && !error.isEmpty()
                ? android.view.View.VISIBLE : android.view.View.GONE);
    }
}
