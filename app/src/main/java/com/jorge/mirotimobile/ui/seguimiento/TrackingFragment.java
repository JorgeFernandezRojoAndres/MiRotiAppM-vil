package com.jorge.mirotimobile.ui.seguimiento;

import android.os.Bundle;
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
import com.jorge.mirotimobile.ui.pedidos.PedidosViewModel;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public class TrackingFragment extends Fragment {

    private FragmentTrackingBinding binding;
    private PedidosViewModel vm;

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

        vm = new ViewModelProvider(requireActivity()).get(PedidosViewModel.class);
        vm.getPedidos().observe(getViewLifecycleOwner(), pedidos -> {
            PedidoDTO activo = (pedidos == null || pedidos.isEmpty()) ? null : pedidos.get(0);
            if (activo == null) {
                Snackbar.make(binding.getRoot(), "No hay pedido en seguimiento", Snackbar.LENGTH_SHORT).show();
                NavHostFragment.findNavController(this).popBackStack();
                return;
            }
            mostrarTracking(activo);
        });

        binding.btnContactarCadete.setOnClickListener(v ->
                Snackbar.make(binding.getRoot(), "Contacto con cadete listo", Snackbar.LENGTH_SHORT).show());

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
