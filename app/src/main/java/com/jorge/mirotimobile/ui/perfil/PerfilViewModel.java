package com.jorge.mirotimobile.ui.perfil;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jorge.mirotimobile.Retrofit.ApiService;
import com.jorge.mirotimobile.Retrofit.RetrofitClient;
import com.jorge.mirotimobile.model.Usuario;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerfilViewModel extends AndroidViewModel {

    private final MutableLiveData<String> nombreLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> emailLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> direccionLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> telefonoLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> rolLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();

    private final ApiService api;

    public PerfilViewModel(@NonNull Application application) {
        super(application);
        api = RetrofitClient.getClient(getApplication()).create(ApiService.class);
    }

    public LiveData<String> getNombreLiveData() {
        return nombreLiveData;
    }

    public LiveData<String> getEmailLiveData() {
        return emailLiveData;
    }

    public LiveData<String> getDireccionLiveData() {
        return direccionLiveData;
    }

    public LiveData<String> getTelefonoLiveData() {
        return telefonoLiveData;
    }

    public LiveData<String> getRolLiveData() {
        return rolLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public LiveData<Boolean> getLoadingLiveData() {
        return loadingLiveData;
    }

    public void cargarPerfil() {
        loadingLiveData.postValue(true);
        errorLiveData.postValue(null);

        api.obtenerPerfil().enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(@NonNull Call<Usuario> call, @NonNull Response<Usuario> response) {
                loadingLiveData.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    Usuario usuario = response.body();
                    nombreLiveData.postValue(usuario.getNombre());
                    emailLiveData.postValue(usuario.getEmail());
                    direccionLiveData.postValue(usuario.getDireccion());
                    telefonoLiveData.postValue(usuario.getTelefono());
                    rolLiveData.postValue(usuario.getRol());
                } else {
                    errorLiveData.postValue("No se pudo cargar el perfil.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Usuario> call, @NonNull Throwable t) {
                loadingLiveData.postValue(false);
                errorLiveData.postValue("Error de conexión: " + t.getMessage());
            }
        });
    }
}
