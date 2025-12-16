package com.jorge.mirotimobile.ui.cliente.seguimiento;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;
import com.jorge.mirotimobile.R;
import com.jorge.mirotimobile.databinding.FragmentTrackingBinding;
import com.jorge.mirotimobile.model.PedidoDTO;
import com.jorge.mirotimobile.ui.cliente.pedidos.PedidosViewModel;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public class TrackingFragment extends Fragment {

    private FragmentTrackingBinding binding;
    private PedidosViewModel pedidosVm;
    private PedidoDTO pedidoActivo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTrackingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pedidosVm = new ViewModelProvider(requireActivity()).get(PedidosViewModel.class);
        pedidosVm.getPedidos().observe(getViewLifecycleOwner(), pedidos -> {
            PedidoDTO activo = (pedidos == null || pedidos.isEmpty()) ? null : pedidos.get(0);
            if (activo == null) {
                Snackbar.make(binding.getRoot(), "No hay pedido en seguimiento", Snackbar.LENGTH_SHORT).show();
                NavHostFragment.findNavController(this).popBackStack();
                return;
            }
            mostrarTracking(activo);
        });

        binding.btnContactarCadete.setOnClickListener(v -> contactarCadete());
        binding.btnVerDetalles.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.detallePedidoFragment));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void mostrarTracking(PedidoDTO pedido) {
        binding.txtTrackingTitle.setText("Pedido #" + pedido.getId());
        String fecha = formatearFecha(pedido.getFechaHora());
        String estado = pedido.getEstado() != null ? pedido.getEstado() : "Pendiente";
        if (fecha != null && !fecha.isEmpty()) {
            binding.txtTrackingSubtitle.setText(fecha + " · " + estado);
        } else {
            binding.txtTrackingSubtitle.setText("Estado: " + estado);
        }
        String llegada = LocalDateTime.now().plusMinutes(30)
                .format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()));
        binding.txtArrivalTime.setText("Llegada estimada: " + llegada);
        binding.lytEstadoAcciones.setVisibility(View.VISIBLE);
        binding.btnMarcarEntregado.setVisibility(View.VISIBLE);
        String cadeteTelefono = pedido.getCadeteTelefono();
        binding.txtCadetePhone.setText(cadeteTelefono != null && !cadeteTelefono.trim().isEmpty()
                ? "Tel: " + cadeteTelefono
                : "Tel: -");
        binding.txtCadeteName.setText("Cadete: " + (pedido.getCadete() != null ? pedido.getCadete() : "Sin cadete"));
        pedidoActivo = pedido;
        Log.d("TRACKING", "PedidosViewModel entrega dto: cadeteTelefono=" + cadeteTelefono);
    }

    private void contactarCadete() {
        if (pedidoActivo == null) {
            Snackbar.make(binding.getRoot(), R.string.mensaje_sin_pedido_activo, Snackbar.LENGTH_SHORT).show();
            return;
        }
        String telefono = pedidoActivo.getCadeteTelefono();
        if (telefono == null || telefono.trim().isEmpty()) {
            Snackbar.make(binding.getRoot(), R.string.mensaje_contactar_cadete_sin_telefono, Snackbar.LENGTH_SHORT).show();
            return;
        }
        Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + telefono.trim()));
        try {
            startActivity(dialIntent);
        } catch (ActivityNotFoundException ex) {
            Snackbar.make(binding.getRoot(), R.string.mensaje_error_dialer, Snackbar.LENGTH_SHORT).show();
        }
    }

    private String formatearFecha(String isoDateTime) {
        if (isoDateTime == null || isoDateTime.isEmpty()) return null;
        try {
            LocalDateTime parsed = LocalDateTime.parse(isoDateTime, DateTimeFormatter.ISO_DATE_TIME);
            return parsed.format(DateTimeFormatter.ofPattern("dd/MM/yyyy · HH:mm", Locale.getDefault()));
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }
}
