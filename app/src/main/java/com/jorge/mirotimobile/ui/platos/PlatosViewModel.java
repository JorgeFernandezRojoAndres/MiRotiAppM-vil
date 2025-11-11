package com.jorge.mirotimobile.ui.platos;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.jorge.mirotimobile.Retrofit.ApiService;
import com.jorge.mirotimobile.Retrofit.RetrofitClient;
import com.jorge.mirotimobile.localdata.SessionManager;
import com.jorge.mirotimobile.model.Plato;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 🧠 PlatosViewModel — Maneja la carga de platos desde la API.
 * Aplica MVVM puro: sin lógica en la vista, usando LiveData para comunicar cambios.
 * Envía el token JWT con prefijo "Bearer".
 */
public class PlatosViewModel extends AndroidViewModel {

    private final MutableLiveData<List<Plato>> platos = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<String> mensajeError = new MutableLiveData<>();

    private final SessionManager session;
    private final ApiService api;

    public PlatosViewModel(@NonNull Application application) {
        super(application);
        session = new SessionManager(application.getApplicationContext());
        api = RetrofitClient.getClient(application.getApplicationContext()).create(ApiService.class);
    }

    // 🔹 LiveData observables
    public MutableLiveData<List<Plato>> getPlatos() {
        return platos;
    }

    public MutableLiveData<Boolean> getLoading() {
        return loading;
    }

    public MutableLiveData<String> getMensajeError() {
        return mensajeError;
    }

    /**
     * 🔸 Carga los platos desde la API y publica el resultado en LiveData.
     * Envía el token JWT almacenado en SessionManager.
     */
    public void cargarPlatos() {
        loading.postValue(true);
        mensajeError.postValue(null);

        String token = session.getToken();
        if (token == null || token.isEmpty()) {
            loading.postValue(false);
            mensajeError.postValue("Sesión inválida. Iniciá sesión nuevamente.");
            return;
        }

        // 🔐 Agregar prefijo Bearer al token JWT
        String bearerToken = "Bearer " + token;

        Call<List<Plato>> call = api.obtenerPlatos(bearerToken);
        call.enqueue(new Callback<List<Plato>>() {
            @Override
            public void onResponse(@NonNull Call<List<Plato>> call, @NonNull Response<List<Plato>> response) {
                loading.postValue(false);

                if (response.isSuccessful() && response.body() != null) {
                    platos.postValue(response.body());
                } else if (response.code() == 401) {
                    mensajeError.postValue("Token inválido o expirado. Iniciá sesión nuevamente.");
                } else if (response.code() == 404) {
                    mensajeError.postValue("No se encontraron platos disponibles.");
                } else {
                    mensajeError.postValue("Error al obtener los platos (HTTP " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Plato>> call, @NonNull Throwable t) {
                loading.postValue(false);
                mensajeError.postValue("Error de conexión: " + t.getMessage());
            }
        });
    }
}
