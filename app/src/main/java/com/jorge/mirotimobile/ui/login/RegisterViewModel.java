package com.jorge.mirotimobile.ui.login;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jorge.mirotimobile.Retrofit.ApiService;
import com.jorge.mirotimobile.Retrofit.RetrofitClient;
import com.jorge.mirotimobile.localdata.SessionManager;
import com.jorge.mirotimobile.model.RegisterRequest;
import com.jorge.mirotimobile.model.Usuario;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterViewModel extends AndroidViewModel {

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<Boolean> showError = new MutableLiveData<>();
    private final MutableLiveData<String> mensajeError = new MutableLiveData<>();
    private final MutableLiveData<Void> navigateToMain = new MutableLiveData<>();
    private final SessionManager session;

    public RegisterViewModel(@NonNull Application application) {
        super(application);
        session = new SessionManager(application.getApplicationContext());
    }

    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<Boolean> getShowError() { return showError; }
    public LiveData<String> getMensajeError() { return mensajeError; }
    public LiveData<Void> getNavigateToMain() { return navigateToMain; }

    public void onRegisterClicked(String nombre, String email, String password, String direccion, String telefono) {
        if (nombre.isEmpty() || email.isEmpty() || password.isEmpty() || direccion.isEmpty() || telefono.isEmpty()) {
            mostrarError("Por favor, completa todos los campos.");
            return;
        }

        if (password.length() < 6) {
            mostrarError("La contraseña debe tener al menos 6 caracteres.");
            return;
        }

        loading.postValue(true);
        showError.postValue(false);

        RegisterRequest request = new RegisterRequest(nombre, email, password, direccion, telefono);
        ApiService api = RetrofitClient.getClient(getApplication()).create(ApiService.class);

        api.register(request).enqueue(new Callback<ApiService.TokenResponse>() {
            @Override
            public void onResponse(Call<ApiService.TokenResponse> call, Response<ApiService.TokenResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.TokenResponse body = response.body();
                    
                    // Si el backend devuelve token, guardamos sesión directamente
                    if (body.getToken() != null && !body.getToken().isEmpty()) {
                        session.saveToken(body.getToken());
                        session.saveUserData(body.getId(), body.getEmail(), body.getRol());
                        session.saveCredentials(email, password);
                        loading.postValue(false);
                        navigateToMain.postValue(null);
                    } else {
                        // Si no hay token, intentamos login automático
                        iniciarSesionAutomatico(email, password);
                    }
                } else {
                    loading.postValue(false);
                    mostrarError("Error en registro: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiService.TokenResponse> call, Throwable t) {
                loading.postValue(false);
                mostrarError("Error de conexión: " + t.getMessage());
            }
        });
    }

    private void iniciarSesionAutomatico(String email, String password) {
        ApiService api = RetrofitClient.getClient(getApplication()).create(ApiService.class);
        Usuario usuario = new Usuario(email, password);

        api.login(usuario).enqueue(new Callback<ApiService.TokenResponse>() {
            @Override
            public void onResponse(Call<ApiService.TokenResponse> call, Response<ApiService.TokenResponse> response) {
                loading.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.TokenResponse body = response.body();
                    session.saveToken(body.getToken());
                    session.saveUserData(body.getId(), body.getEmail(), body.getRol());
                    session.saveCredentials(email, password);
                    navigateToMain.postValue(null);
                } else {
                    mostrarError("Registro exitoso, pero falló el inicio de sesión automático.");
                }
            }

            @Override
            public void onFailure(Call<ApiService.TokenResponse> call, Throwable t) {
                loading.postValue(false);
                mostrarError("Registro exitoso. Error de conexión al iniciar sesión.");
            }
        });
    }

    private void mostrarError(String mensaje) {
        mensajeError.postValue(mensaje);
        showError.postValue(true);
    }
}
