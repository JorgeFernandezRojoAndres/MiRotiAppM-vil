package com.jorge.mirotimobile.ui.login;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jorge.mirotimobile.retrofit.ApiService;
import com.jorge.mirotimobile.retrofit.RetrofitClient;
import com.jorge.mirotimobile.localdata.SessionManager;
import com.jorge.mirotimobile.model.Usuario;
import com.jorge.mirotimobile.util.Event;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * LoginViewModel: Maneja la lógica de login (incluye huella y persistencia de credenciales).
 */
public class LoginViewModel extends AndroidViewModel {

    // Credenciales fijas para login con huella digital
    private static final String HUELLA_EMAIL = "carlos@mail.com"; // Reemplaza con tu email
    private static final String HUELLA_PASSWORD = "carlos123"; // Reemplaza con tu contraseña

    private final MutableLiveData<Event<Boolean>> navigateToMain = new MutableLiveData<>();
    private final MutableLiveData<Event<Boolean>> navigateToRegister = new MutableLiveData<>();
    private final MutableLiveData<Event<Boolean>> navigateToResetPassword = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<Boolean> showError = new MutableLiveData<>();
    private final MutableLiveData<String> mensajeError = new MutableLiveData<>();
    private final MutableLiveData<Integer> errorVisibility = new MutableLiveData<>();
    private final MutableLiveData<Integer> progressVisibility = new MutableLiveData<>();

    private final SessionManager session;

    public LoginViewModel(@NonNull Application application) {
        super(application);
        session = new SessionManager(application.getApplicationContext());
    }

    public LiveData<Event<Boolean>> getNavigateToMain() {
        return navigateToMain;
    }

    public LiveData<Event<Boolean>> getNavigateToRegister() {
        return navigateToRegister;
    }

    public LiveData<Event<Boolean>> getNavigateToResetPassword() {
        return navigateToResetPassword;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<Boolean> getShowError() {
        return showError;
    }

    public LiveData<String> getMensajeError() {
        return mensajeError;
    }
    
    public LiveData<Integer> getErrorVisibility() {
        return errorVisibility;
    }
    
    public LiveData<Integer> getProgressVisibility() {
        return progressVisibility;
    }

    public void onLoginClicked(String email, String password) {
        Log.d("LOGIN_FLOW", "onLoginClicked: " + email);
        if (email.isEmpty() || password.isEmpty()) {
            mostrarError("Completa todos los campos");
            return;
        }
        iniciarSesion(email, password);
    }

    public void onHuellaClicked() {
        iniciarSesionConHuella();
    }

    public void onForgotPasswordClicked() {
        navigateToResetPassword.postValue(new Event<>(true));
    }

    public void onRegisterClicked() {
        navigateToRegister.postValue(new Event<>(true));
    }

    private void iniciarSesion(String email, String password) {
        loading.postValue(true);
        showError.postValue(false);
        actualizarVisibilidad();

        ApiService api = RetrofitClient.getClient(getApplication()).create(ApiService.class);
        Usuario usuario = new Usuario(email, password);

        Call<ApiService.TokenResponse> call = api.login(usuario);
        call.enqueue(new Callback<ApiService.TokenResponse>() {
            @Override
            public void onResponse(Call<ApiService.TokenResponse> call,
                                   Response<ApiService.TokenResponse> response) {
                loading.postValue(false);
                actualizarVisibilidad();

                if (response.isSuccessful() && response.body() != null) {
                    ApiService.TokenResponse body = response.body();
                    Log.d("LOGIN_FLOW", "Login SUCCESS (HTTP " + response.code() + "): token=" + 
                        (body.getToken() != null ? "presente" : "null") + ", email=" + body.getEmail());

                    // Validar acceso permitido (permite Admin/Administrador, Cliente, Cadete)
                    String rol = body.getRol();
                    boolean rolPermitido =
                            "Cliente".equalsIgnoreCase(rol) ||
                            "Cadete".equalsIgnoreCase(rol) ||
                            "Admin".equalsIgnoreCase(rol) ||
                            "Administrador".equalsIgnoreCase(rol);

                    if (!rolPermitido) {
                        Log.d("LOGIN_FLOW", "Rol no permitido: " + rol);
                        mostrarError("Acceso restringido: rol no permitido en la app.");
                        return;
                    }

                    // Guardar token y datos
                    session.saveToken(body.getToken());
                    session.saveUserData(body.getId(), body.getEmail(), body.getRol());

                    // Guardar credenciales para login por huella
                    session.saveCredentials(body.getEmail(), password);
                    
                    // Habilitar huella SOLO para Carlos
                    if ("carlos@mail.com".equals(body.getEmail())) {
                        session.enableHuellaForCarlos();
                    }

                    cargarPerfilUsuario(api);

                    mensajeError.postValue(null);
                    showError.postValue(false);
                    Log.d("LOGIN_FLOW", "Login successful, posting navigateToMain Event");
                    navigateToMain.postValue(new Event<>(true));
                } else {
                    int httpCode = response.code();
                    String errorBody = null;
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        errorBody = "Error al leer error body";
                    }
                    Log.d("LOGIN_FLOW", "Login FAILED (HTTP " + httpCode + "): " + errorBody);
                    
                    if (httpCode == 401) {
                        mostrarError("HTTP 401: Credenciales inválidas. Verifica email y contraseña.");
                    } else {
                        mostrarError("Error " + httpCode + ": Credenciales inválidas o usuario no encontrado");
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiService.TokenResponse> call, Throwable t) {
                loading.postValue(false);
                actualizarVisibilidad();
                Log.d("LOGIN_FLOW", "Login failed: " + t.getMessage());
                mostrarError("Error de conexión: " + t.getMessage());
            }
        });
    }

    private void iniciarSesionConHuella() {
        Log.d("HUELLA_DEBUG", "Iniciando sesión con huella");
        Log.d("HUELLA_DEBUG", "Flag huella habilitada: " + session.isHuellaEnabledForCarlos());
        
        // Verificar flag explícita de seguridad
        if (!session.isHuellaEnabledForCarlos()) {
            Log.d("HUELLA_DEBUG", "Flag no habilitada, habilitando para Carlos");
            // Si no está habilitada, habilitarla para Carlos
            session.enableHuellaForCarlos();
        }
        
        // Verificar si hay sesión activa de Carlos
        if (session.isLoggedIn() && "carlos@mail.com".equals(session.getUserEmail())) {
            Log.d("HUELLA_DEBUG", "Sesión activa de Carlos, navegando");
            navigateToMain.postValue(new Event<>(true));
            return;
        }
        
        Log.d("HUELLA_DEBUG", "Haciendo login con credenciales de Carlos");
        iniciarSesion(HUELLA_EMAIL, HUELLA_PASSWORD);
    }

    private void cargarPerfilUsuario(ApiService api) {
        api.obtenerPerfil().enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Usuario perfil = response.body();
                    if (perfil.getNombre() != null && !perfil.getNombre().isEmpty()) {
                        session.saveUserName(perfil.getNombre());
                    }
                }
            }

            @Override
            public void onFailure(Call<Usuario> call, Throwable t) {
                // No romper el flujo de login
            }
        });
    }

    private void mostrarError(String mensaje) {
        mensajeError.postValue(mensaje);
        showError.postValue(true);
        actualizarVisibilidad();
    }
    
    private void actualizarVisibilidad() {
        Boolean isLoading = loading.getValue();
        Boolean hasError = showError.getValue();
        
        // Actualizar visibilidad de progress
        progressVisibility.postValue(Boolean.TRUE.equals(isLoading) ? 
            android.view.View.VISIBLE : android.view.View.GONE);
            
        // Actualizar visibilidad de error
        errorVisibility.postValue(Boolean.TRUE.equals(hasError) ? 
            android.view.View.VISIBLE : android.view.View.GONE);
    }

    public void borrarCredencialesGuardadas() {
        session.clearCredentials();
    }
    
    public void logout() {
        session.logout();
    }
}
