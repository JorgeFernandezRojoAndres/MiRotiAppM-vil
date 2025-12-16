package com.jorge.mirotimobile.ui.login;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.jorge.mirotimobile.Retrofit.ApiService;
import com.jorge.mirotimobile.Retrofit.RetrofitClient;
import com.jorge.mirotimobile.model.GenericResponse;
import com.jorge.mirotimobile.model.ResetPasswordRequest;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordViewModel extends AndroidViewModel {

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> mensajeExito = new MutableLiveData<>();
    private final MutableLiveData<String> mensajeError = new MutableLiveData<>();
    private final MutableLiveData<Void> navigateToLogin = new MutableLiveData<>();

    public ResetPasswordViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getMensajeExito() {
        return mensajeExito;
    }

    public LiveData<String> getMensajeError() {
        return mensajeError;
    }

    public LiveData<Void> getNavigateToLogin() {
        return navigateToLogin;
    }

    public void resetPassword(String email, String pass1, String pass2) {
        String emailTrim = email != null ? email.trim() : "";
        if (emailTrim.isEmpty()) {
            mensajeError.setValue("Por favor ingrese un correo electrónico.");
            return;
        }
        if (pass1 == null || pass1.isEmpty() || pass2 == null || pass2.isEmpty()) {
            mensajeError.setValue("Por favor ingrese la nueva contraseña en ambos campos.");
            return;
        }
        if (!pass1.equals(pass2)) {
            mensajeError.setValue("Las contraseñas no coinciden.");
            return;
        }

        loading.setValue(true);
        mensajeError.setValue(null);
        mensajeExito.setValue(null);

        ApiService api = RetrofitClient.getClientNoAuth().create(ApiService.class);
        Call<GenericResponse> call = api.resetPassword(new ResetPasswordRequest(emailTrim, pass1, pass2));
        call.enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                loading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    String mensaje = response.body().getMensaje();
                    mensajeExito.setValue(mensaje != null && !mensaje.isEmpty() ? mensaje : "Contraseña restablecida correctamente.");
                    navigateToLogin.setValue(null);
                    return;
                }
                mensajeError.setValue(extraerMensajeError(response));
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                loading.setValue(false);
                mensajeError.setValue("Error en la solicitud, intente más tarde.");
            }
        });
    }

    private String extraerMensajeError(Response<GenericResponse> response) {
        if (response == null) return "No se pudo recuperar la contraseña.";

        try {
            if (response.errorBody() != null) {
                String raw = response.errorBody().string();
                GenericResponse parsed = new Gson().fromJson(raw, GenericResponse.class);
                if (parsed != null && parsed.getMensaje() != null && !parsed.getMensaje().isEmpty()) {
                    return parsed.getMensaje();
                }
            }
        } catch (IOException ignored) {
        } catch (Exception ignored) {
        }
        return "No se pudo recuperar la contraseña.";
    }
}
