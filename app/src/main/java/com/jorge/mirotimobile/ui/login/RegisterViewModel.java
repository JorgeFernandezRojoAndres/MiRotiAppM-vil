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
    private final MutableLiveData<Integer> loadingVisibility = new MutableLiveData<>();
    private final MutableLiveData<Boolean> buttonEnabled = new MutableLiveData<>();
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();
    private final SessionManager session;

    public RegisterViewModel(@NonNull Application application) {
        super(application);
        session = new SessionManager(application.getApplicationContext());
    }

    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<Boolean> getShowError() { return showError; }
    public LiveData<String> getMensajeError() { return mensajeError; }
    public LiveData<Void> getNavigateToMain() { return navigateToMain; }
    public LiveData<Integer> getLoadingVisibility() { return loadingVisibility; }
    public LiveData<Boolean> getButtonEnabled() { return buttonEnabled; }
    public LiveData<String> getToastMessage() { return toastMessage; }

    public void onRegisterClicked(String nombre, String email, String password, String direccion, String telefono) {
        if (!validarCampos(nombre, email, password, direccion, telefono)) {
            return;
        }

        if (!validarPassword(password)) {
            return;
        }

        iniciarRegistro(nombre, email, password, direccion, telefono);
    }

    private boolean validarCampos(String nombre, String email, String password, String direccion, String telefono) {
        if (nombre.isEmpty() || email.isEmpty() || password.isEmpty() || direccion.isEmpty() || telefono.isEmpty()) {
            mostrarError("Por favor, completa todos los campos.");
            return false;
        }
        return true;
    }
    
    private boolean validarPassword(String password) {
        if (password.length() < 6) {
            mostrarError("La contraseña debe tener al menos 6 caracteres.");
            return false;
        }
        return true;
    }
    
    private void iniciarRegistro(String nombre, String email, String password, String direccion, String telefono) {
        loading.postValue(true);
        showError.postValue(false);
        actualizarUI();

        RegisterRequest request = new RegisterRequest(nombre, email, password, direccion, telefono);
        ApiService api = RetrofitClient.getClient(getApplication()).create(ApiService.class);

        api.register(request).enqueue(new Callback<ApiService.TokenResponse>() {
            @Override
            public void onResponse(Call<ApiService.TokenResponse> call, Response<ApiService.TokenResponse> response) {
                procesarRespuestaRegistro(response, email, password);
            }

            @Override
            public void onFailure(Call<ApiService.TokenResponse> call, Throwable t) {
                loading.postValue(false);
                actualizarUI();
                mostrarError("Error de conexión: " + t.getMessage());
            }
        });
    }
    
    private void procesarRespuestaRegistro(Response<ApiService.TokenResponse> response, String email, String password) {
        if (response.isSuccessful() && response.body() != null) {
            ApiService.TokenResponse body = response.body();
            
            if (body.getToken() != null && !body.getToken().isEmpty()) {
                guardarSesionYNavegar(body, email, password);
            } else {
                iniciarSesionAutomatico(email, password);
            }
        } else {
            loading.postValue(false);
            actualizarUI();
            mostrarError("Error en registro: " + response.message());
        }
    }
    
    private void guardarSesionYNavegar(ApiService.TokenResponse body, String email, String password) {
        session.saveToken(body.getToken());
        session.saveUserData(body.getId(), body.getEmail(), body.getRol());
        session.saveCredentials(email, password);
        loading.postValue(false);
        actualizarUI();
        toastMessage.postValue("Registro exitoso");
        navigateToMain.postValue(null);
    }
    
    private void iniciarSesionAutomatico(String email, String password) {
        ApiService api = RetrofitClient.getClient(getApplication()).create(ApiService.class);
        Usuario usuario = new Usuario(email, password);

        api.login(usuario).enqueue(new Callback<ApiService.TokenResponse>() {
            @Override
            public void onResponse(Call<ApiService.TokenResponse> call, Response<ApiService.TokenResponse> response) {
                loading.postValue(false);
                actualizarUI();
                procesarRespuestaLoginAutomatico(response, email, password);
            }

            @Override
            public void onFailure(Call<ApiService.TokenResponse> call, Throwable t) {
                loading.postValue(false);
                actualizarUI();
                mostrarError("Registro exitoso. Error de conexión al iniciar sesión.");
            }
        });
    }
    
    private void procesarRespuestaLoginAutomatico(Response<ApiService.TokenResponse> response, String email, String password) {
        if (response.isSuccessful() && response.body() != null) {
            ApiService.TokenResponse body = response.body();
            session.saveToken(body.getToken());
            session.saveUserData(body.getId(), body.getEmail(), body.getRol());
            session.saveCredentials(email, password);
            toastMessage.postValue("Registro exitoso");
            navigateToMain.postValue(null);
        } else {
            mostrarError("Registro exitoso, pero falló el inicio de sesión automático.");
        }
    }

    private void mostrarError(String mensaje) {
        mensajeError.postValue(mensaje);
        showError.postValue(true);
        toastMessage.postValue(mensaje);
    }
    
    private void actualizarUI() {
        Boolean isLoading = loading.getValue();
        loadingVisibility.postValue(Boolean.TRUE.equals(isLoading) ? 
            android.view.View.VISIBLE : android.view.View.GONE);
        buttonEnabled.postValue(!Boolean.TRUE.equals(isLoading));
    }
}
