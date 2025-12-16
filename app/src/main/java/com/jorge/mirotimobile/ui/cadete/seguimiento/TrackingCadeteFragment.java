package com.jorge.mirotimobile.ui.cadete.seguimiento;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.button.MaterialButton;
import com.jorge.mirotimobile.R;
import com.jorge.mirotimobile.databinding.FragmentTrackingCadeteBinding;
import com.jorge.mirotimobile.model.PedidoDTO;
import com.jorge.mirotimobile.ui.cadete.entregas.EntregasViewModel;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class TrackingCadeteFragment extends Fragment {

    private FragmentTrackingCadeteBinding binding;
    private EntregasViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d("CADETE_FLOW", "TrackingCadeteFragment onCreateView");
        binding = FragmentTrackingCadeteBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("CADETE_FLOW", "TrackingCadeteFragment onViewCreated");
        viewModel = new ViewModelProvider(requireActivity()).get(EntregasViewModel.class);
        viewModel.getPedidoActual().observe(getViewLifecycleOwner(), pedido -> {
            Log.d("CADETE_FLOW", "OBS pedidoActual -> " + (pedido != null ? pedido.getId() : "null"));
            if (pedido == null) {
                mostrarTrackingCadeteVacio();
            } else {
                mostrarTracking(pedido);
            }
        });
        viewModel.getEstadoEntrega().observe(getViewLifecycleOwner(), estado -> {
            Log.d("CADETE_FLOW", "OBS estadoEntrega -> " + estado);
            renderEstado(estado);
        });
        viewModel.getEventoAbrirMapa().observe(getViewLifecycleOwner(), event -> {
            if (event != null) {
                String direccion = event.getContentIfNotHandled();
                if (direccion != null) {
                    handleEventoAbrirMapa(direccion);
                }
            }
        });

        binding.btnAbrirMapa.setOnClickListener(v -> viewModel.abrirMapa());
        binding.btnMarcarEntregado.setOnClickListener(v -> handleMarcarEntrega());
        binding.btnContactarCliente.setOnClickListener(v -> handleContactarCliente());
        
        // Configurar información del pedido
        configurarInfoPedido();
    }
    
    private void configurarInfoPedido() {
        viewModel.getPedidoActual().observe(getViewLifecycleOwner(), pedido -> {
            if (pedido != null) {
                // Construir lista de productos
                StringBuilder productos = new StringBuilder("Productos: ");
                if (pedido.getDetalles() != null && !pedido.getDetalles().isEmpty()) {
                    for (int i = 0; i < pedido.getDetalles().size(); i++) {
                        com.jorge.mirotimobile.model.DetallePedidoInfoDTO detalle = pedido.getDetalles().get(i);
                        productos.append(detalle.getCantidad()).append("x ").append(detalle.getPlato());
                        if (i < pedido.getDetalles().size() - 1) {
                            productos.append(", ");
                        }
                    }
                } else {
                    productos.append("Sin detalles");
                }
                
                // Setear textos
                binding.txtProductos.setText(productos.toString());
                binding.txtTotal.setText("Total: $" + String.format("%.0f", pedido.getTotal()));
                binding.txtNotas.setText("Notas: Entregar en " + (pedido.getDireccion() != null ? pedido.getDireccion() : "dirección no disponible"));
            }
        });
    }

    private void renderEstado(EntregasViewModel.EstadoEntrega estado) {
        if (estado == null) estado = EntregasViewModel.EstadoEntrega.EN_ESPERA;
        MaterialCardView card = binding.cardEstadoPedido;
        MaterialButton btnMaps = binding.btnAbrirMapa;

        int colorRes;
        int iconRes;
        String title;
        String subtitle;
        switch (estado) {
            case EN_CAMINO:
                colorRes = ContextCompat.getColor(requireContext(), R.color.cadete_state_espera);
                iconRes = android.R.drawable.ic_dialog_map;
                title = "Entrega en camino";
                subtitle = "Abrí el mapa para llegar al domicilio del cliente";
                break;
            default:
                colorRes = ContextCompat.getColor(requireContext(), R.color.cadete_state_espera);
                iconRes = android.R.drawable.ic_popup_sync;
                title = "Sin entrega activa";
                subtitle = "Tomá un pedido desde Próximas entregas";
                break;
        }

        card.setCardBackgroundColor(colorRes);
        int orange = ContextCompat.getColor(requireContext(), R.color.miroti_orange);
        int strokeWidth = Math.round(getResources().getDisplayMetrics().density * 2);
        boolean enCamino = estado == EntregasViewModel.EstadoEntrega.EN_CAMINO;
        card.setStrokeColor(enCamino ? orange : colorRes);
        card.setStrokeWidth(enCamino ? strokeWidth : 0);
        binding.txtEstadoTitle.setText(title);
        binding.txtEstadoDescription.setText(subtitle);
        binding.imgEstadoIcon.setImageResource(iconRes);
        binding.imgEstadoIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white), PorterDuff.Mode.SRC_IN);

        btnMaps.setVisibility(enCamino ? View.VISIBLE : View.GONE);
        ColorStateList tint = ColorStateList.valueOf(colorRes);
        btnMaps.setBackgroundTintList(tint);

        if (enCamino) {
            ColorStateList stroke = ColorStateList.valueOf(orange);
            btnMaps.setStrokeColor(stroke);
            btnMaps.setStrokeWidth(strokeWidth);
        } else {
            btnMaps.setStrokeWidth(0);
        }
        Log.d("CADETE_FLOW", "renderEstado DONE");
        
        // Logs adicionales para detectar crash
        Log.d("CADETE_FLOW", "renderEstado - checking for post-render issues");
        try {
            Log.d("CADETE_FLOW", "renderEstado - all UI operations completed successfully");
        } catch (Exception e) {
            Log.e("CADETE_FLOW", "renderEstado - Exception: " + e.getMessage());
        }
    }

    private void mostrarTracking(PedidoDTO pedido) {
        binding.txtTrackingTitle.setText("Entrega #" + pedido.getId());
        binding.txtTrackingSubtitle.setText(formatFechayEstado(pedido));
        String llegada = LocalDateTime.now().plusMinutes(20)
                .format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()));
        binding.txtArrivalTime.setText("Llegada estimada: " + llegada);
    }

    private void mostrarTrackingCadeteVacio() {
        binding.txtTrackingTitle.setText("Seguimiento del Cadete");
        binding.txtTrackingSubtitle.setText("Esperando asignación de pedido");
        binding.txtArrivalTime.setText("Se mostrará la llegada una vez asignado");
    }

    private String formatFechayEstado(PedidoDTO pedido) {
        String fecha = pedido.getFechaHora();
        String estado = pedido.getEstado() != null ? pedido.getEstado() : "Pendiente";
        if (fecha != null && !fecha.isEmpty()) {
            return fecha + " · " + estado;
        }
        return "Estado: " + estado;
    }

    private void handleEventoAbrirMapa(String direccionCliente) {
        Log.d("CADETE_FLOW", "handleEventoAbrirMapa called");
        if (direccionCliente == null || direccionCliente.trim().isEmpty()) {
            Snackbar.make(binding.getRoot(), "Dirección del cliente no disponible", Snackbar.LENGTH_SHORT).show();
            viewModel.limpiarEventos();
            return;
        }
        Bundle args = new Bundle();
        args.putString("direccionCliente", direccionCliente);
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_trackingCadeteFragment_to_mapaEntregaFragment, args);
        viewModel.limpiarEventos();
    }

    private void handleMarcarEntrega() {
        PedidoDTO pedido = viewModel.getPedidoActual().getValue();
        if (pedido == null) {
            Snackbar.make(binding.getRoot(), R.string.mensaje_sin_pedido_activo, Snackbar.LENGTH_SHORT).show();
            return;
        }
        viewModel.marcarEntregaCompletada();
        Snackbar.make(binding.getRoot(), R.string.mensaje_entrega_completada, Snackbar.LENGTH_SHORT).show();
    }

    private void handleContactarCliente() {
        PedidoDTO pedido = viewModel.getPedidoActual().getValue();
        if (pedido == null) {
            Snackbar.make(binding.getRoot(), R.string.mensaje_sin_pedido_activo, Snackbar.LENGTH_SHORT).show();
            return;
        }
        String telefono = pedido.getTelefono();
        if (telefono == null || telefono.trim().isEmpty()) {
            Snackbar.make(binding.getRoot(), R.string.mensaje_sin_telefono, Snackbar.LENGTH_SHORT).show();
            return;
        }
        Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + telefono.trim()));
        try {
            startActivity(dialIntent);
        } catch (Exception e) {
            Snackbar.make(binding.getRoot(), R.string.mensaje_error_dialer, Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        Log.d("CADETE_FLOW", "TrackingCadeteFragment onDestroyView");
        super.onDestroyView();
        binding = null;
        Log.d("CADETE_FLOW", "TrackingCadeteFragment onDestroyView COMPLETE");
    }
}
