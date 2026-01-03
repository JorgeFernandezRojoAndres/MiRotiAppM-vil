package com.jorge.mirotimobile.ui.cliente.perfil;

import android.app.Application;

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

public class PerfilViewModel extends AndroidViewModel {

    private final MutableLiveData<String> nombreLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> emailLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> direccionLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> telefonoLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> rolLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> nombreFormateado = new MutableLiveData<>();
    private final MutableLiveData<String> emailFormateado = new MutableLiveData<>();
    private final MutableLiveData<String> direccionFormateada = new MutableLiveData<>();
    private final MutableLiveData<String> telefonoFormateado = new MutableLiveData<>();
    private final MutableLiveData<String> rolFormateado = new MutableLiveData<>();
    private final MutableLiveData<Event<Boolean>> eventoLogout = new MutableLiveData<>();

    private final ApiService api;
    private final SessionManager sessionManager;

    public PerfilViewModel(@NonNull Application application) {
        super(application);
        api = RetrofitClient.getClient(getApplication()).create(ApiService.class);
        sessionManager = new SessionManager(getApplication().getApplicationContext());
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
    
    public LiveData<String> getNombreFormateado() {
        return nombreFormateado;
    }
    
    public LiveData<String> getEmailFormateado() {
        return emailFormateado;
    }
    
    public LiveData<String> getDireccionFormateada() {
        return direccionFormateada;
    }
    
    public LiveData<String> getTelefonoFormateado() {
        return telefonoFormateado;
    }
    
    public LiveData<String> getRolFormateado() {
        return rolFormateado;
    }
    
    public LiveData<Event<Boolean>> getEventoLogout() {
        return eventoLogout;
    }
    
    public void cerrarSesion() {
        sessionManager.logout();
        eventoLogout.setValue(new Event<>(true));
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
                    
                    // Actualizar textos formateados
                    actualizarTextosFormateados(usuario);
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
    
    private void actualizarTextosFormateados(Usuario usuario) {
        nombreFormateado.postValue(formatearTexto(usuario.getNombre()));
        emailFormateado.postValue(formatearTexto(usuario.getEmail()));
        direccionFormateada.postValue(formatearTexto(usuario.getDireccion()));
        telefonoFormateado.postValue(formatearTexto(usuario.getTelefono()));
        rolFormateado.postValue(formatearTexto(usuario.getRol()));
    }
    
    private String formatearTexto(String texto) {
        return texto != null ? texto : "";
    }
}
