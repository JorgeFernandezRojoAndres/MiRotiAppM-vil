package com.jorge.mirotimobile.ui.login;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.jorge.mirotimobile.retrofit.ApiService;
import com.jorge.mirotimobile.retrofit.RetrofitClient;
import com.jorge.mirotimobile.model.GenericResponse;
import com.jorge.mirotimobile.model.ResetPasswordRequest;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordViewModel extends AndroidViewModel {

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> mensajeExito = new MutableLiveData<>("");
    private final MutableLiveData<String> mensajeError = new MutableLiveData<>("");
    private final MutableLiveData<Void> navigateToLogin = new MutableLiveData<>();
    private final MutableLiveData<Integer> progressVisibility = new MutableLiveData<>(android.view.View.GONE);
    private final MutableLiveData<Integer> errorVisibility = new MutableLiveData<>(android.view.View.GONE);
    private final MutableLiveData<Integer> successVisibility = new MutableLiveData<>(android.view.View.GONE);
    private final MutableLiveData<String> errorText = new MutableLiveData<>("");
    private final MutableLiveData<String> successText = new MutableLiveData<>("");

    public ResetPasswordViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<Void> getNavigateToLogin() {
        return navigateToLogin;
    }
    
    public LiveData<Integer> getProgressVisibility() {
        return progressVisibility;
    }
    
    public LiveData<Integer> getErrorVisibility() {
        return errorVisibility;
    }
    
    public LiveData<Integer> getSuccessVisibility() {
        return successVisibility;
    }
    
    public LiveData<String> getErrorText() {
        return errorText;
    }
    
    public LiveData<String> getSuccessText() {
        return successText;
    }

    public void resetPassword(String email, String pass1, String pass2) {
        String emailTrim = email != null ? email.trim() : "";

        if (!validarCamposReset(emailTrim, pass1, pass2)) {
            return;
        }

        enviarSolicitudReset(emailTrim, pass1, pass2);
    }

    private boolean validarCamposReset(String email, String pass1, String pass2) {
        if (TextUtils.isEmpty(email)) {
            mostrarError("Por favor ingrese un correo electrónico.");
            return false;
        }
        
        if (TextUtils.isEmpty(pass1) || TextUtils.isEmpty(pass2)) {
            mostrarError("Por favor ingrese la nueva contraseña en ambos campos.");
            return false;
        }
        
        if (!pass1.equals(pass2)) {
            mostrarError("Las contraseñas no coinciden.");
            return false;
        }
        
        return true;
    }
    
    private void enviarSolicitudReset(String email, String pass1, String pass2) {
        loading.setValue(true);
        limpiarMensajes();
        actualizarUI();

        ApiService api = RetrofitClient.getClientNoAuth().create(ApiService.class);
        Call<GenericResponse> call = api.resetPassword(new ResetPasswordRequest(email, pass1, pass2));
        
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<GenericResponse> call, @NonNull Response<GenericResponse> response) {
                loading.setValue(false);
                actualizarUI();
                procesarRespuestaReset(response);
            }

            @Override
            public void onFailure(@NonNull Call<GenericResponse> call, @NonNull Throwable t) {
                loading.setValue(false);
                actualizarUI();
                mostrarError("Error en la solicitud, intente más tarde.");
            }
        });
    }
    
    private void limpiarMensajes() {
        mensajeError.setValue(null);
        mensajeExito.setValue(null);
    }
    
    private void procesarRespuestaReset(Response<GenericResponse> response) {
        if (response.isSuccessful() && response.body() != null) {
            String mensaje = response.body().getMensaje();
            String textoExito = !TextUtils.isEmpty(mensaje) ? 
                mensaje : "Contraseña restablecida correctamente.";
            mostrarExito(textoExito);
            navigateToLogin.setValue(null);
        } else {
            String error = extraerMensajeError(response);
            mostrarError(error);
        }
    }
    
    private void mostrarExito(@NonNull String mensaje) {
        String texto = TextUtils.isEmpty(mensaje) ? "Operación completada" : mensaje;
        mensajeExito.setValue(texto);
        successText.setValue(texto);
    }
    
    private void mostrarError(@NonNull String mensaje) {
        String texto = TextUtils.isEmpty(mensaje) ? "Ocurrió un error inesperado" : mensaje;
        mensajeError.setValue(texto);
        errorText.setValue(texto);
        actualizarUI();
    }
    
    private String extraerMensajeError(Response<GenericResponse> response) {
        if (response == null) return "No se pudo recuperar la contraseña.";

        try (ResponseBody errorBody = response.errorBody()) {
            if (errorBody != null) {
                String raw = errorBody.string();
                GenericResponse parsed = new Gson().fromJson(raw, GenericResponse.class);
                if (parsed != null && !TextUtils.isEmpty(parsed.getMensaje())) {
                    return parsed.getMensaje();
                }
            }
        } catch (Exception ignored) {
        }

        return "No se pudo recuperar la contraseña.";
    }
    
    private void actualizarUI() {
        Boolean isLoading = loading.getValue();
        progressVisibility.setValue(Boolean.TRUE.equals(isLoading) ? 
            android.view.View.VISIBLE : android.view.View.GONE);
            
        String error = mensajeError.getValue();
        errorVisibility.setValue(!TextUtils.isEmpty(error) ? 
            android.view.View.VISIBLE : android.view.View.GONE);
           
        String success = mensajeExito.getValue();
        successVisibility.setValue(!TextUtils.isEmpty(success) ? 
            android.view.View.VISIBLE : android.view.View.GONE);
    }
}
