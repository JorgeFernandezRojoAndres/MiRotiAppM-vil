package com.jorge.mirotimobile.ui.cadete.seguimiento;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.snackbar.Snackbar;
import com.jorge.mirotimobile.R;
import com.jorge.mirotimobile.databinding.FragmentMapaEntregaBinding;
import com.jorge.mirotimobile.ui.cadete.entregas.EntregasViewModel;

import java.util.Arrays;

public class MapaEntregaFragment extends Fragment implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private GoogleMap mMap;
    private FragmentMapaEntregaBinding binding;
    private MapaEntregaViewModel viewModel;
    private EntregasViewModel entregasViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentMapaEntregaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            viewModel = new ViewModelProvider(this).get(MapaEntregaViewModel.class);
            entregasViewModel = new ViewModelProvider(requireActivity()).get(EntregasViewModel.class);

            viewModel.setEntregasViewModel(entregasViewModel);

            String direccionCliente = getArguments() != null ? getArguments().getString("direccionCliente") : null;
            viewModel.setDireccionCliente(direccionCliente);

            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                    .findFragmentById(R.id.mapContainer);
            if (mapFragment != null) {
                mapFragment.getMapAsync(this);
            }

            // Ocultar acciones si no es cadete
            com.jorge.mirotimobile.localdata.SessionManager session = new com.jorge.mirotimobile.localdata.SessionManager(
                    requireContext());
            String role = session.getUserRole();
            if (role == null || !role.trim().equalsIgnoreCase("Cadete")) {
                binding.layoutMapaAcciones.setVisibility(View.GONE);
            }

            observarViewModel();

            binding.btnMarcarMomoEntregado.setOnClickListener(v -> viewModel.marcarEntrega());
            binding.btnLlamarCliente.setOnClickListener(v -> viewModel.llamarCliente());
        } catch (Exception e) {
            // Manejar error de inicialización
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        viewModel.onMapaListo();
    }

    private void observarViewModel() {
        viewModel.getMarcadorDestino().observe(getViewLifecycleOwner(), marcador -> {
            if (mMap != null && marcador != null) {
                mMap.addMarker(new MarkerOptions()
                        .position(marcador.posicion)
                        .title(marcador.titulo)
                        .snippet(marcador.descripcion)
                        .icon(BitmapDescriptorFactory.defaultMarker(marcador.color)));
            }
        });

        viewModel.getMarcadorOrigen().observe(getViewLifecycleOwner(), marcador -> {
            if (mMap != null && marcador != null) {
                mMap.addMarker(new MarkerOptions()
                        .position(marcador.posicion)
                        .title(marcador.titulo)
                        .icon(BitmapDescriptorFactory.defaultMarker(marcador.color)));
            }
        });

        viewModel.getRutaData().observe(getViewLifecycleOwner(), ruta -> {
            if (mMap != null && ruta != null && ruta.puntos != null && !ruta.puntos.isEmpty()) {
                PolylineOptions options = new PolylineOptions()
                        .addAll(ruta.puntos)
                        .width(ruta.esLineaRecta ? 8 : 10)
                        .color(ruta.esLineaRecta ? 0xFFFF5722 : 0xFFFF9800)
                        .geodesic(!ruta.esLineaRecta);

                if (ruta.esLineaRecta) {
                    options.pattern(Arrays.asList(
                            new com.google.android.gms.maps.model.Dash(20),
                            new com.google.android.gms.maps.model.Gap(10)));
                }

                mMap.addPolyline(options);

                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (com.google.android.gms.maps.model.LatLng punto : ruta.puntos) {
                    builder.include(punto);
                }
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
            }
        });

        viewModel.getCamaraPosition().observe(getViewLifecycleOwner(), position -> {
            if (mMap != null && position != null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
            }
        });

        viewModel.getBotonEntregadoEnabled().observe(getViewLifecycleOwner(),
                binding.btnMarcarMomoEntregado::setEnabled);
        viewModel.getBotonLlamarEnabled().observe(getViewLifecycleOwner(),
                binding.btnLlamarCliente::setEnabled);

        viewModel.getMensajeError().observe(getViewLifecycleOwner(), mensaje -> {
            if (mensaje != null && !mensaje.isEmpty()) {
                Snackbar.make(binding.getRoot(), mensaje, Snackbar.LENGTH_SHORT).show();
            }
        });
        viewModel.getMensajeExito().observe(getViewLifecycleOwner(), mensaje -> {
            if (mensaje != null && !mensaje.isEmpty()) {
                Snackbar.make(binding.getRoot(), mensaje, Snackbar.LENGTH_SHORT).show();
            }
        });

        viewModel.getEventoLlamarTelefono().observe(getViewLifecycleOwner(), event -> {
            if (event != null) {
                String telefono = event.getContentIfNotHandled();
                if (telefono != null && !telefono.isEmpty()) {
                    android.content.Intent dialIntent = new android.content.Intent(android.content.Intent.ACTION_DIAL,
                            android.net.Uri.parse("tel:" + telefono));
                    startActivity(dialIntent);
                }
            }
        });

        entregasViewModel.getEventoNavegarEntregas().observe(getViewLifecycleOwner(), event -> {
            if (event.getContentIfNotHandled() != null) {
                androidx.navigation.fragment.NavHostFragment.findNavController(this)
                        .popBackStack(R.id.entregasFragment, false);
            }
        });

        viewModel.getEventoSolicitarPermisos().observe(getViewLifecycleOwner(), event -> {
            if (event != null) {
                Boolean solicitar = event.getContentIfNotHandled();
                if (solicitar != null && solicitar) {
                    requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                            LOCATION_PERMISSION_REQUEST_CODE);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean permisoOtorgado = grantResults.length > 0 &&
                grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED;

        if (permisoOtorgado) {
            viewModel.onPermisosOtorgados();
        } else {
            viewModel.onPermisosDenegados();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
