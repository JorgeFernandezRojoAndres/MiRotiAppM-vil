package com.jorge.mirotimobile.ui.login;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jorge.mirotimobile.Retrofit.ApiService;
import com.jorge.mirotimobile.Retrofit.RetrofitClient;
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

        ApiService api = RetrofitClient.getClient(getApplication()).create(ApiService.class);
        Usuario usuario = new Usuario(email, password);

        Call<ApiService.TokenResponse> call = api.login(usuario);
        call.enqueue(new Callback<ApiService.TokenResponse>() {
            @Override
            public void onResponse(Call<ApiService.TokenResponse> call,
                                   Response<ApiService.TokenResponse> response) {
                loading.postValue(false);

                if (response.isSuccessful() && response.body() != null) {
                    ApiService.TokenResponse body = response.body();

                    // Validar acceso permitido (permite Admin/Administrador, Cliente, Cadete)
                    String rol = body.getRol();
                    boolean rolPermitido =
                            "Cliente".equalsIgnoreCase(rol) ||
                            "Cadete".equalsIgnoreCase(rol) ||
                            "Admin".equalsIgnoreCase(rol) ||
                            "Administrador".equalsIgnoreCase(rol);

                    if (!rolPermitido) {
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
                    mostrarError("Credenciales inválidas o usuario no encontrado");
                }
            }

            @Override
            public void onFailure(Call<ApiService.TokenResponse> call, Throwable t) {
                loading.postValue(false);
                Log.d("LOGIN_FLOW", "Login failed: " + t.getMessage());
                mostrarError("Error de conexión: " + t.getMessage());
            }
        });
    }

    private void iniciarSesionConHuella() {
        // Verificar flag explícita de seguridad
        if (!session.isHuellaEnabledForCarlos()) {
            mostrarError("Este usuario no tiene acceso por huella digital");
            return;
        }
        
        // Verificar si hay sesión activa de Carlos
        if (session.isLoggedIn() && "carlos@mail.com".equals(session.getUserEmail())) {
            // Carlos ya está logueado, reutilizar sesión
            navigateToMain.postValue(new Event<>(true));
            return;
        }
        
        // No hay sesión activa, hacer login de Carlos
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
    }

    public void borrarCredencialesGuardadas() {
        session.clearCredentials();
    }
    
    public void logout() {
        session.logout();
    }
}
