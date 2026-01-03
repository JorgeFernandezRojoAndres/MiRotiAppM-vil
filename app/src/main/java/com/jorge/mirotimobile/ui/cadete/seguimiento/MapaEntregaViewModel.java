package com.jorge.mirotimobile.ui.cadete.seguimiento;

import android.Manifest;
import android.app.Application;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.jorge.mirotimobile.model.PedidoDTO;
import com.jorge.mirotimobile.ui.cadete.entregas.EntregasViewModel;
import com.jorge.mirotimobile.util.Event;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MapaEntregaViewModel extends AndroidViewModel {

    public static class RutaData {
        public final List<LatLng> puntos;
        public final boolean esLineaRecta;
        
        public RutaData(List<LatLng> puntos, boolean esLineaRecta) {
            this.puntos = puntos;
            this.esLineaRecta = esLineaRecta;
        }
    }

    public static class MarcadorData {
        public final LatLng posicion;
        public final String titulo;
        public final String descripcion;
        public final float color;
        
        public MarcadorData(LatLng posicion, String titulo, String descripcion, float color) {
            this.posicion = posicion;
            this.titulo = titulo;
            this.descripcion = descripcion;
            this.color = color;
        }
    }

    private static final String DIRECTIONS_API_KEY = "AIzaSyC8gfZthW14xhNGprWrJ_mUHsTUh2MNwg8";
    private static final LatLng DESTINO_DEFAULT = new LatLng(-33.301726, -66.337752);

    private final MutableLiveData<String> direccionCliente = new MutableLiveData<>();
    private final MutableLiveData<MarcadorData> marcadorDestino = new MutableLiveData<>();
    private final MutableLiveData<MarcadorData> marcadorOrigen = new MutableLiveData<>();
    private final MutableLiveData<RutaData> rutaData = new MutableLiveData<>();
    private final MutableLiveData<LatLng> camaraPosition = new MutableLiveData<>();
    private final MutableLiveData<Boolean> botonEntregadoEnabled = new MutableLiveData<>();
    private final MutableLiveData<Boolean> botonLlamarEnabled = new MutableLiveData<>();
    private final MutableLiveData<String> mensajeError = new MutableLiveData<>();
    private final MutableLiveData<String> mensajeExito = new MutableLiveData<>();
    private final MutableLiveData<Event<String>> eventoLlamarTelefono = new MutableLiveData<>();
    private final MutableLiveData<Event<Boolean>> eventoSolicitarPermisos = new MutableLiveData<>();

    private final FusedLocationProviderClient fusedLocationClient;
    private LatLng ubicacionActual;
    private EntregasViewModel entregasViewModel;

    public MapaEntregaViewModel(@NonNull Application application) {
        super(application);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(application);
        inicializarMarcadorDestino();
    }

    public LiveData<String> getDireccionCliente() {
        return direccionCliente;
    }

    public LiveData<MarcadorData> getMarcadorDestino() {
        return marcadorDestino;
    }

    public LiveData<MarcadorData> getMarcadorOrigen() {
        return marcadorOrigen;
    }

    public LiveData<RutaData> getRutaData() {
        return rutaData;
    }

    public LiveData<LatLng> getCamaraPosition() {
        return camaraPosition;
    }

    public LiveData<Boolean> getBotonEntregadoEnabled() {
        return botonEntregadoEnabled;
    }

    public LiveData<Boolean> getBotonLlamarEnabled() {
        return botonLlamarEnabled;
    }

    public LiveData<String> getMensajeError() {
        return mensajeError;
    }

    public LiveData<String> getMensajeExito() {
        return mensajeExito;
    }

    public LiveData<Event<String>> getEventoLlamarTelefono() {
        return eventoLlamarTelefono;
    }

    public LiveData<Event<Boolean>> getEventoSolicitarPermisos() {
        return eventoSolicitarPermisos;
    }

    public void setDireccionCliente(String direccion) {
        direccionCliente.setValue(direccion);
        actualizarMarcadorDestino(direccion);
    }

    public void setEntregasViewModel(EntregasViewModel viewModel) {
        entregasViewModel = viewModel;
        actualizarEstadoBotones();
    }

    public void onMapaListo() {
        verificarPermisos();
    }

    public void onPermisosOtorgados() {
        obtenerUbicacionActual();
    }

    public void onPermisosDenegados() {
        camaraPosition.setValue(DESTINO_DEFAULT);
    }

    public void marcarEntrega() {
        if (entregasViewModel == null) {
            mensajeError.setValue("Error: ViewModel no inicializado");
            return;
        }
        
        entregasViewModel.marcarEntregaCompletada();
        mensajeExito.setValue("Entrega completada");
    }

    public void llamarCliente() {
        if (entregasViewModel == null) {
            mensajeError.setValue("Error: ViewModel no inicializado");
            return;
        }
        
        PedidoDTO pedido = entregasViewModel.getPedidoActual().getValue();
        if (pedido == null) {
            mensajeError.setValue("No hay pedido activo");
            return;
        }
        
        String telefono = pedido.getTelefono();
        if (telefono == null || telefono.trim().isEmpty()) {
            mensajeError.setValue("Teléfono no disponible");
            return;
        }
        
        eventoLlamarTelefono.setValue(new Event<>(telefono.trim()));
    }

    private void inicializarMarcadorDestino() {
        MarcadorData marcador = new MarcadorData(
            DESTINO_DEFAULT,
            "Dirección de entrega",
            "Dirección no disponible",
            com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED
        );
        marcadorDestino.setValue(marcador);
    }

    private void actualizarMarcadorDestino(String direccion) {
        String descripcion = direccion != null ? direccion : "Dirección no disponible";
        MarcadorData marcador = new MarcadorData(
            DESTINO_DEFAULT,
            "Dirección de entrega",
            descripcion,
            com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED
        );
        marcadorDestino.setValue(marcador);
    }

    private void verificarPermisos() {
        boolean tienePermisos = ActivityCompat.checkSelfPermission(
            getApplication(), 
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;
        
        eventoSolicitarPermisos.setValue(new Event<>(!tienePermisos));
        
        if (tienePermisos) {
            obtenerUbicacionActual();
        }
    }

    private void obtenerUbicacionActual() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            procesarUbicacion(location);
        });
    }

    private void procesarUbicacion(Location location) {
        if (location == null) {
            camaraPosition.setValue(DESTINO_DEFAULT);
            return;
        }
        
        ubicacionActual = new LatLng(location.getLatitude(), location.getLongitude());
        
        MarcadorData marcadorOrig = new MarcadorData(
            ubicacionActual,
            "Mi ubicación",
            "",
            com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_BLUE
        );
        marcadorOrigen.setValue(marcadorOrig);
        
        new ObtenerRutaTask().execute();
        camaraPosition.setValue(ubicacionActual);
    }

    private void actualizarEstadoBotones() {
        if (entregasViewModel == null) {
            botonEntregadoEnabled.setValue(false);
            botonLlamarEnabled.setValue(false);
            return;
        }
        
        PedidoDTO pedido = entregasViewModel.getPedidoActual().getValue();
        boolean tienePedido = pedido != null;
        botonEntregadoEnabled.setValue(tienePedido);
        
        boolean tieneTelefono = tienePedido && 
            pedido.getTelefono() != null && 
            !pedido.getTelefono().trim().isEmpty();
        botonLlamarEnabled.setValue(tieneTelefono);
    }

    private String llamarDirectionsAPI() {
        try {
            String origin = ubicacionActual.latitude + "," + ubicacionActual.longitude;
            String destination = DESTINO_DEFAULT.latitude + "," + DESTINO_DEFAULT.longitude;
            String urlString = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "origin=" + origin +
                    "&destination=" + destination +
                    "&mode=driving" +
                    "&avoid=highways" +
                    "&key=" + DIRECTIONS_API_KEY;

            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();
            connection.disconnect();
            return response.toString();

        } catch (Exception e) {
            return null;
        }
    }

    private List<LatLng> parsearRespuestaJSON(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            String status = jsonObject.getString("status");
            
            if (!"OK".equals(status)) {
                return crearLineaRecta();
            }

            JSONArray routes = jsonObject.getJSONArray("routes");
            JSONObject route = routes.getJSONObject(0);
            JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
            String encodedPoints = overviewPolyline.getString("points");

            return decodificarPolyline(encodedPoints);

        } catch (Exception e) {
            return crearLineaRecta();
        }
    }

    private List<LatLng> crearLineaRecta() {
        List<LatLng> puntos = new ArrayList<>();
        puntos.add(ubicacionActual);
        puntos.add(DESTINO_DEFAULT);
        return puntos;
    }

    private List<LatLng> decodificarPolyline(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;

            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);

            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;

            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);

            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng punto = new LatLng((double) lat / 1E5, (double) lng / 1E5);
            poly.add(punto);
        }

        return poly;
    }

    private class ObtenerRutaTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            return llamarDirectionsAPI();
        }

        @Override
        protected void onPostExecute(String jsonResponse) {
            List<LatLng> rutaPuntos = parsearRespuestaJSON(jsonResponse);
            boolean esLineaRecta = jsonResponse == null;
            rutaData.setValue(new RutaData(rutaPuntos, esLineaRecta));
        }
    }
}