package com.jorge.mirotimobile.ui.cadete.seguimiento;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.model.LatLng;

public class MapaEntregaViewModel extends ViewModel {

    private final MutableLiveData<String> direccionCliente = new MutableLiveData<>();
    private final MutableLiveData<LatLng> coordenadasCliente = new MutableLiveData<>();

    public void setDireccionCliente(String direccion) {
        direccionCliente.setValue(direccion);
        // Por ahora usar coordenadas fijas de Buenos Aires
        coordenadasCliente.setValue(new LatLng(-34.6037, -58.3816));
    }

    public LiveData<String> getDireccionCliente() {
        return direccionCliente;
    }

    public LiveData<LatLng> getCoordenadasCliente() {
        return coordenadasCliente;
    }
}