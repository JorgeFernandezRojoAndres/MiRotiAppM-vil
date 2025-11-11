package com.jorge.mirotimobile.ui.nav;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * ViewModel del menú principal.
 * Se encarga de exponer las opciones del menú y manejar navegación.
 */
public class MenuViewModel extends AndroidViewModel {

    private final MutableLiveData<String> _opcionSeleccionada = new MutableLiveData<>();
    public LiveData<String> opcionSeleccionada = _opcionSeleccionada;

    public MenuViewModel(@NonNull Application application) {
        super(application);
    }

    // 🔹 Se invoca desde la vista cuando el usuario elige una opción
    public void seleccionarOpcion(String opcion) {
        _opcionSeleccionada.setValue(opcion);
    }
}
