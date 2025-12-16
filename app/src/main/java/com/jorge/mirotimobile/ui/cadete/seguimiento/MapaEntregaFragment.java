package com.jorge.mirotimobile.ui.cadete.seguimiento;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.snackbar.Snackbar;
import com.jorge.mirotimobile.R;
import com.jorge.mirotimobile.model.PedidoDTO;
import com.jorge.mirotimobile.ui.cadete.entregas.EntregasViewModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapaEntregaFragment extends Fragment implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final String DIRECTIONS_API_KEY = "AIzaSyC8gfZthW14xhNGprWrJ_mUHsTUh2MNwg8";
    private GoogleMap mMap;
    private String direccionCliente;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng ubicacionActual;
    private LatLng destino;
    private Button btnMarcarEntregado;
    private Button btnLlamarCliente;
    private EntregasViewModel entregasViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d("MAPA_DEBUG", "=== onCreateView INICIADO ===");
        
        View view = inflater.inflate(R.layout.fragment_mapa_entrega, container, false);
        Log.d("MAPA_DEBUG", "Layout inflado correctamente");

        MapaEntregaViewModel viewModel = new ViewModelProvider(this).get(MapaEntregaViewModel.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        Log.d("MAPA_DEBUG", "ViewModel y LocationClient creados");
        
        if (getArguments() != null) {
            direccionCliente = getArguments().getString("direccionCliente");
            Log.d("MAPA_DEBUG", "Dirección recibida: '" + direccionCliente + "'");
            viewModel.setDireccionCliente(direccionCliente);
        } else {
            Log.w("MAPA_DEBUG", "getArguments() es null - no hay dirección");
        }
        
        Log.d("MAPA_DEBUG", "Buscando SupportMapFragment...");
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.mapContainer);
        if (mapFragment != null) {
            Log.d("MAPA_DEBUG", "SupportMapFragment encontrado, llamando getMapAsync");
            mapFragment.getMapAsync(this);
        } else {
            Log.e("MAPA_DEBUG", "ERROR: SupportMapFragment es null");
        }

        Log.d("MAPA_DEBUG", "=== onCreateView COMPLETADO ===");
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        entregasViewModel = new ViewModelProvider(requireActivity()).get(EntregasViewModel.class);
        btnMarcarEntregado = view.findViewById(R.id.btn_marcar_momo_entregado);
        btnLlamarCliente = view.findViewById(R.id.btn_llamar_cliente);

        btnMarcarEntregado.setOnClickListener(v -> marcarEntrega());
        btnLlamarCliente.setOnClickListener(v -> llamarCliente());

        entregasViewModel.getPedidoActual().observe(getViewLifecycleOwner(), this::actualizarBotones);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        Log.d("MAPA_DEBUG", "onMapReady llamado - mapa inicializado");
        
        // Coordenadas del destino (San Luis)
        destino = new LatLng(-33.301726, -66.337752);
        
        // Agregar marcador del destino
        mMap.addMarker(new MarkerOptions()
                .position(destino)
                .title("Dirección de entrega")
                .snippet(direccionCliente != null ? direccionCliente : "Dirección no disponible")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        
        // Obtener ubicación actual
        obtenerUbicacionActual();
    }
    
    private void obtenerUbicacionActual() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                ubicacionActual = new LatLng(location.getLatitude(), location.getLongitude());
                Log.d("MAPA_DEBUG", "Ubicación actual: " + ubicacionActual);
                
                // Agregar marcador de ubicación actual
                mMap.addMarker(new MarkerOptions()
                        .position(ubicacionActual)
                        .title("Mi ubicación")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                
                // Dibujar ruta
                dibujarRuta();
                
                // Ajustar cámara para mostrar ambos puntos
                ajustarCamara();
            } else {
                Log.w("MAPA_DEBUG", "No se pudo obtener la ubicación");
                // Mostrar solo el destino
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destino, 15));
            }
        });
    }
    
    private void dibujarRuta() {
        if (ubicacionActual == null || destino == null) {
            Log.e("DIRECTIONS_API", "No se puede dibujar ruta - ubicacionActual: " + ubicacionActual + ", destino: " + destino);
            return;
        }
        
        Log.d("DIRECTIONS_API", "=== INICIANDO DIRECTIONS API ===");
        Log.d("DIRECTIONS_API", "Origen: " + ubicacionActual);
        Log.d("DIRECTIONS_API", "Destino: " + destino);
        
        // Llamar a Google Directions API para obtener ruta real
        new ObtenerRutaTask().execute();
    }
    
    private void ajustarCamara() {
        if (ubicacionActual == null || destino == null) return;
        
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(ubicacionActual);
        builder.include(destino);
        
        LatLngBounds bounds = builder.build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        Log.d("MAPA_DEBUG", "Cámara ajustada para mostrar ambos puntos");
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                obtenerUbicacionActual();
            } else {
                Log.w("MAPA_DEBUG", "Permiso de ubicación denegado");
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destino, 15));
            }
        }
    }

    private void actualizarBotones(PedidoDTO pedido) {
        boolean tienePedido = pedido != null;
        btnMarcarEntregado.setEnabled(tienePedido);
        btnLlamarCliente.setEnabled(tienePedido && tieneTelefonoValido(pedido));
    }

    private boolean tieneTelefonoValido(PedidoDTO pedido) {
        if (pedido == null) return false;
        String telefono = pedido.getTelefono();
        return telefono != null && !telefono.trim().isEmpty();
    }

    private void marcarEntrega() {
        PedidoDTO pedido = entregasViewModel.getPedidoActual().getValue();
        if (pedido == null) {
            Snackbar.make(requireView(), R.string.mensaje_sin_pedido_activo, Snackbar.LENGTH_SHORT).show();
            return;
        }
        entregasViewModel.marcarEntregaCompletada();
        Snackbar.make(requireView(), R.string.mensaje_entrega_completada, Snackbar.LENGTH_SHORT).show();
    }

    private void llamarCliente() {
        PedidoDTO pedido = entregasViewModel.getPedidoActual().getValue();
        if (pedido == null) {
            Snackbar.make(requireView(), R.string.mensaje_sin_pedido_activo, Snackbar.LENGTH_SHORT).show();
            return;
        }
        String telefono = pedido.getTelefono();
        if (telefono == null || telefono.trim().isEmpty()) {
            Snackbar.make(requireView(), R.string.mensaje_sin_telefono, Snackbar.LENGTH_SHORT).show();
            return;
        }
        Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + telefono.trim()));
        try {
            startActivity(dialIntent);
        } catch (Exception e) {
            Snackbar.make(requireView(), R.string.mensaje_error_dialer, Snackbar.LENGTH_SHORT).show();
        }
    }


    
    // 1. Llamada HTTP a Google Directions API
    private String llamarDirectionsAPI() {
        try {
            // Construir URL con parámetros
            String origin = ubicacionActual.latitude + "," + ubicacionActual.longitude;
            String destination = destino.latitude + "," + destino.longitude;
            String urlString = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "origin=" + origin +
                    "&destination=" + destination +
                    "&mode=driving" +
                    "&avoid=highways" +
                    "&key=" + DIRECTIONS_API_KEY;
            
            Log.d("DIRECTIONS_API", "URL: " + urlString);
            
            // Crear conexión HTTP
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            
            // Leer respuesta
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            
            reader.close();
            connection.disconnect();
            
            Log.d("DIRECTIONS_API", "Respuesta recibida");
            return response.toString();
            
        } catch (Exception e) {
            Log.e("DIRECTIONS_API", "Error en llamada HTTP: " + e.getMessage());
            return null;
        }
    }
    
    // 2. Parsear respuesta JSON y extraer overview_polyline
    private List<LatLng> parsearRespuestaJSON(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            
            // Verificar status
            String status = jsonObject.getString("status");
            if (!"OK".equals(status)) {
                Log.e("DIRECTIONS_API", "Error en respuesta: " + status);
                
                // Fallback: dibujar línea recta
                requireActivity().runOnUiThread(() -> {
                    Log.d("DIRECTIONS_API", "Usando fallback - línea recta");
                    dibujarLineaRecta();
                });
                
                return null;
            }
            
            // Obtener routes array
            JSONArray routes = jsonObject.getJSONArray("routes");
            if (routes.length() == 0) {
                Log.e("DIRECTIONS_API", "No se encontraron rutas");
                return null;
            }
            
            // Obtener primera ruta
            JSONObject route = routes.getJSONObject(0);
            JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
            String encodedPoints = overviewPolyline.getString("points");
            
            Log.d("DIRECTIONS_API", "Polyline obtenido: " + encodedPoints.substring(0, Math.min(50, encodedPoints.length())) + "...");
            
            // Decodificar polyline
            return decodificarPolyline(encodedPoints);
            
        } catch (Exception e) {
            Log.e("DIRECTIONS_API", "Error parseando JSON: " + e.getMessage());
            return null;
        }
    }
    
    // 3. Decodificar overview_polyline.points a List<LatLng>
    private List<LatLng> decodificarPolyline(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        
        while (index < len) {
            int b, shift = 0, result = 0;
            
            // Decodificar latitud
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            
            shift = 0;
            result = 0;
            
            // Decodificar longitud
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;
            
            // Convertir a coordenadas y agregar a lista
            LatLng punto = new LatLng((double) lat / 1E5, (double) lng / 1E5);
            poly.add(punto);
        }
        
        Log.d("DIRECTIONS_API", "Polyline decodificado: " + poly.size() + " puntos");
        return poly;
    }
    
    // 4. Dibujar ruta real en el mapa
    private void dibujarRutaEnMapa(List<LatLng> rutaPuntos) {
        Log.d("DIRECTIONS_API", "dibujarRutaEnMapa llamado");
        
        if (mMap == null) {
            Log.e("DIRECTIONS_API", "mMap es null");
            return;
        }
        
        if (rutaPuntos == null || rutaPuntos.isEmpty()) {
            Log.e("DIRECTIONS_API", "rutaPuntos vacío o null");
            return;
        }
        
        Log.d("DIRECTIONS_API", "Creando polyline con " + rutaPuntos.size() + " puntos");
        
        // Crear polyline con los puntos de la ruta real
        PolylineOptions polylineOptions = new PolylineOptions()
                .addAll(rutaPuntos)
                .width(10)
                .color(0xFFFF9800) // Naranja
                .geodesic(false);
        
        // Agregar al mapa
        Polyline polyline = mMap.addPolyline(polylineOptions);
        
        Log.d("DIRECTIONS_API", "Ruta real dibujada exitosamente - Polyline ID: " + polyline.getId());
    }
    
    // Fallback: dibujar línea recta cuando Directions API falla
    private void dibujarLineaRecta() {
        if (ubicacionActual == null || destino == null || mMap == null) return;
        
        PolylineOptions polylineOptions = new PolylineOptions()
                .add(ubicacionActual)
                .add(destino)
                .width(8)
                .color(0xFFFF5722) // Naranja para distinguir del azul
                .pattern(Arrays.asList(new com.google.android.gms.maps.model.Dash(20), new com.google.android.gms.maps.model.Gap(10))); // Línea punteada
        
        mMap.addPolyline(polylineOptions);
        Log.d("DIRECTIONS_API", "Línea recta dibujada como fallback");
    }
    
    // AsyncTask para llamada HTTP a Google Directions API
    private class ObtenerRutaTask extends AsyncTask<Void, Void, String> {
        
        @Override
        protected String doInBackground(Void... voids) {
            return llamarDirectionsAPI();
        }
        
        @Override
        protected void onPostExecute(String jsonResponse) {
            Log.d("DIRECTIONS_API", "onPostExecute - jsonResponse: " + (jsonResponse != null ? "recibido" : "null"));
            
            if (jsonResponse != null) {
                Log.d("DIRECTIONS_API", "JSON length: " + jsonResponse.length());
                List<LatLng> rutaPuntos = parsearRespuestaJSON(jsonResponse);
                
                if (rutaPuntos != null && !rutaPuntos.isEmpty()) {
                    Log.d("DIRECTIONS_API", "Puntos de ruta obtenidos: " + rutaPuntos.size());
                    dibujarRutaEnMapa(rutaPuntos);
                } else {
                    Log.e("DIRECTIONS_API", "No se obtuvieron puntos de ruta");
                }
            } else {
                Log.e("DIRECTIONS_API", "jsonResponse es null");
            }
        }
    }
}
