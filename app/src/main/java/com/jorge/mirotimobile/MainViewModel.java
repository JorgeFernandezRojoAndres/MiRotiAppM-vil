package com.jorge.mirotimobile;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jorge.mirotimobile.localdata.SessionManager;
import com.jorge.mirotimobile.util.Event;

public class MainViewModel extends AndroidViewModel {

    private final MutableLiveData<Boolean> esCadete = new MutableLiveData<>();
    private final MutableLiveData<Boolean> tienePedidosActivos = new MutableLiveData<>();
    private final MutableLiveData<Event<Boolean>> eventoRedirectLogin = new MutableLiveData<>();
    private final MutableLiveData<String> mensajeToast = new MutableLiveData<>();
    private final MutableLiveData<Event<Integer>> eventoConfigurarMenu = new MutableLiveData<>();
    private final MutableLiveData<Event<Integer>> eventoNavegacion = new MutableLiveData<>();
    
    private final SessionManager sessionManager;

    public MainViewModel(@NonNull Application application) {
        super(application);
        sessionManager = new SessionManager(getApplication().getApplicationContext());
        verificarSesion();
    }

    public LiveData<Boolean> getEsCadete() {
        return esCadete;
    }

    public LiveData<Boolean> getTienePedidosActivos() {
        return tienePedidosActivos;
    }

    public LiveData<Event<Boolean>> getEventoRedirectLogin() {
        return eventoRedirectLogin;
    }

    public LiveData<String> getMensajeToast() {
        return mensajeToast;
    }

    public LiveData<Event<Integer>> getEventoConfigurarMenu() {
        return eventoConfigurarMenu;
    }

    public LiveData<Event<Integer>> getEventoNavegacion() {
        return eventoNavegacion;
    }

    public void onPedidosChanged(java.util.List<?> pedidos) {
        tienePedidosActivos.setValue(pedidos != null && !pedidos.isEmpty());
    }

    public void onNavigateToTracking() {
        if (!tienePedidosActivos()) {
            mensajeToast.setValue("No tenés pedidos en seguimiento");
        }
    }

    public void logout() {
        sessionManager.logout();
        eventoRedirectLogin.setValue(new Event<>(true));
    }

    private boolean tienePedidosActivos() {
        Boolean tieneActivos = tienePedidosActivos.getValue();
        return Boolean.TRUE.equals(tieneActivos);
    }
    
    private void verificarSesion() {
        if (!esTokenValido()) {
            eventoRedirectLogin.setValue(new Event<>(true));
            return;
        }
        
        determinarTipoUsuario();
    }
    
    private boolean esTokenValido() {
        String token = sessionManager.getToken();
        return token != null && !token.isEmpty();
    }
    
    private void determinarTipoUsuario() {
        String role = sessionManager.getUserRole();
        boolean isCadete = "Cadete".equalsIgnoreCase(role);
        esCadete.setValue(isCadete);
        
        configurarMenuSegunUsuario(isCadete);
    }
    
    private void configurarMenuSegunUsuario(boolean isCadete) {
        int menu = isCadete ? R.menu.menu_cadete : R.menu.test_menu;
        int destination = isCadete ? R.id.entregasFragment : R.id.bienvenidaFragment;
        
        eventoConfigurarMenu.setValue(new Event<>(menu));
        eventoNavegacion.setValue(new Event<>(destination));
    }
    
    public void onMenuItemSelected(int itemId) {
        validarYNavegar(itemId);
    }
    
    private void validarYNavegar(int itemId) {
        eventoNavegacion.setValue(new Event<>(itemId));
    }
}