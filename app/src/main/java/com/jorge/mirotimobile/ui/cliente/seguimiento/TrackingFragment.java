package com.jorge.mirotimobile.ui.cliente.seguimiento;

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
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;
import com.jorge.mirotimobile.R;
import com.jorge.mirotimobile.databinding.FragmentTrackingBinding;
import com.jorge.mirotimobile.ui.cliente.pedidos.PedidosViewModel;

public class TrackingFragment extends Fragment {

    private FragmentTrackingBinding binding;
    private PedidosViewModel pedidosVm;

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
        observarViewModel();
        configurarListeners();
    }

    private void observarViewModel() {
        pedidosVm.getPedidos().observe(getViewLifecycleOwner(), this::procesarPedidoSeguimiento);

        pedidosVm.getTrackingTitle().observe(getViewLifecycleOwner(), binding.txtTrackingTitle::setText);
        pedidosVm.getTrackingSubtitle().observe(getViewLifecycleOwner(), binding.txtTrackingSubtitle::setText);
        pedidosVm.getTrackingArrivalTime().observe(getViewLifecycleOwner(), binding.txtArrivalTime::setText);

        pedidosVm.getCadeteName().observe(getViewLifecycleOwner(), this::actualizarNombreCadete);
        pedidosVm.getCadetePhone().observe(getViewLifecycleOwner(), this::actualizarTelefonoCadete);

        pedidosVm.getEstadoTexto().observe(getViewLifecycleOwner(), binding.txtEstadoTitle::setText);
        pedidosVm.getTrackingSubtitle().observe(getViewLifecycleOwner(), binding.txtEstadoDescription::setText);

        pedidosVm.getBtnAbrirMapaText().observe(getViewLifecycleOwner(), binding.btnAbrirMapaCliente::setText);
        pedidosVm.getBtnAbrirMapaEnabled().observe(getViewLifecycleOwner(), binding.btnAbrirMapaCliente::setEnabled);

        pedidosVm.getEventoMostrarMensaje().observe(getViewLifecycleOwner(), event -> {
            String mensaje = event.getContentIfNotHandled();
            reaccionarMensaje(mensaje);
        });

        pedidosVm.getEventoMostrarSnackbar().observe(getViewLifecycleOwner(), event -> {
            String mensaje = event.getContentIfNotHandled();
            reaccionarSnackbar(mensaje);
        });

        pedidosVm.getEventoContactarCadete().observe(getViewLifecycleOwner(), event -> {
            String telefono = event.getContentIfNotHandled();
            realizarLlamada(telefono);
        });

        pedidosVm.getEventoVerMapa().observe(getViewLifecycleOwner(), event -> {
            String direccion = event.getContentIfNotHandled();
            navegarAlMapa(direccion);
        });
    }

    private void procesarPedidoSeguimiento(java.util.List<com.jorge.mirotimobile.model.PedidoDTO> pedidos) {
        com.jorge.mirotimobile.model.PedidoDTO activo = (pedidos == null || pedidos.isEmpty()) ? null : pedidos.get(0);
        pedidosVm.procesarPedidoParaTracking(activo);
    }

    private void actualizarNombreCadete(String nombre) {
        binding.txtCadeteName.setText(nombre != null ? "Cadete: " + nombre : "Cadete: Sin asignar");
    }

    private void actualizarTelefonoCadete(String telefono) {
        if (telefono != null) {
            binding.txtCadetePhone.setText("Tel: " + telefono);
            binding.btnContactarCadete.setVisibility(View.VISIBLE);
        } else {
            binding.txtCadetePhone.setText("Tel: -");
            binding.btnContactarCadete.setVisibility(View.GONE);
        }
    }

    private void reaccionarMensaje(String mensaje) {
        if (mensaje == null)
            return;
        Snackbar.make(binding.getRoot(), mensaje, Snackbar.LENGTH_SHORT).show();
        NavHostFragment.findNavController(this).popBackStack();
    }

    private void reaccionarSnackbar(String mensaje) {
        if (mensaje == null)
            return;
        Snackbar.make(binding.getRoot(), mensaje, Snackbar.LENGTH_SHORT).show();
    }

    private void realizarLlamada(String telefono) {
        if (telefono == null || telefono.trim().isEmpty())
            return;
        Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + telefono));
        startActivity(dialIntent);
    }

    private void navegarAlMapa(String direccion) {
        if (direccion == null)
            return;
        Bundle args = new Bundle();
        args.putString("direccionCliente", direccion);
        Navigation.findNavController(requireView()).navigate(R.id.action_trackingFragment_to_mapaEntregaFragment, args);
    }

    private void configurarListeners() {
        binding.btnContactarCadete.setOnClickListener(v -> {
            java.util.List<com.jorge.mirotimobile.model.PedidoDTO> pedidos = pedidosVm.getPedidos().getValue();
            com.jorge.mirotimobile.model.PedidoDTO activo = (pedidos == null || pedidos.isEmpty()) ? null
                    : pedidos.get(0);
            pedidosVm.contactarCadete(activo);
        });
        binding.btnVerDetalles
                .setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.detallePedidoFragment));

        binding.btnAbrirMapaCliente.setOnClickListener(v -> {
            java.util.List<com.jorge.mirotimobile.model.PedidoDTO> pedidos = pedidosVm.getPedidos().getValue();
            com.jorge.mirotimobile.model.PedidoDTO activo = (pedidos == null || pedidos.isEmpty()) ? null
                    : pedidos.get(0);
            pedidosVm.solicitarVerMapa(activo);
        });

        binding.cardEstadoPedido.setOnClickListener(v -> pedidosVm.cargarMisPedidos());
    }

    @Override
    public void onResume() {
        super.onResume();
        pedidosVm.cargarMisPedidos();
        pedidosVm.iniciarPollingSeguimiento();
    }

    @Override
    public void onPause() {
        super.onPause();
        pedidosVm.detenerPollingSeguimiento();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
