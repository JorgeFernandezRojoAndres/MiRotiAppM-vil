package com.jorge.mirotimobile.ui.cliente.pedidos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.jorge.mirotimobile.databinding.FragmentDetallePedidoBinding;
import com.jorge.mirotimobile.R;
import com.google.android.material.snackbar.Snackbar;

import java.util.Collections;

public class DetallePedidoFragment extends Fragment {

    private FragmentDetallePedidoBinding binding;
    private CarritoAdapter adapter;
    private PedidosViewModel vm;
    private boolean modoCadete;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentDetallePedidoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        configurarRecyclerView();
        configurarViewModel();
        observarViewModel();
        configurarListeners();
    }

    private void configurarRecyclerView() {
        adapter = new CarritoAdapter(() -> vm.actualizarResumen(adapter.getSubtotal()));
        binding.recyclerCarrito.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerCarrito.setAdapter(adapter);
    }

    private void configurarViewModel() {
        vm = new ViewModelProvider(requireActivity()).get(PedidosViewModel.class);
        Bundle args = getArguments();
        vm.aplicarModoDesdeArgs(args);
    }

    private void observarViewModel() {
        vm.getPedidos().observe(getViewLifecycleOwner(), this::actualizarLista);

        vm.getProgressVisibility().observe(getViewLifecycleOwner(), binding.menuProgress::setVisibility);
        vm.getErrorText().observe(getViewLifecycleOwner(), this::mostrarError);

        vm.getPedidoNumero().observe(getViewLifecycleOwner(), binding.txtPedidoNumero::setText);
        vm.getPedidoFecha().observe(getViewLifecycleOwner(), binding.txtPedidoFecha::setText);
        vm.getEstadoTexto().observe(getViewLifecycleOwner(), binding.chipEstado::setText);
        vm.getEstadoColorRes().observe(getViewLifecycleOwner(), binding.chipEstado::setChipBackgroundColorResource);
        vm.getEstadoTextColorRes().observe(getViewLifecycleOwner(),
                colorRes -> binding.chipEstado.setTextColor(getResources().getColor(colorRes, null)));

        vm.getSubtotalTexto().observe(getViewLifecycleOwner(), binding.txtSubtotal::setText);
        vm.getEnvioTexto().observe(getViewLifecycleOwner(), binding.txtEnvio::setText);
        vm.getTotalTexto().observe(getViewLifecycleOwner(), binding.txtTotal::setText);

        vm.getBtnConfirmarTexto().observe(getViewLifecycleOwner(), binding.btnConfirmar::setText);
        vm.getBtnConfirmarEnabled().observe(getViewLifecycleOwner(), binding.btnConfirmar::setEnabled);

        vm.getBtnVolverSeguimientoVisibility().observe(getViewLifecycleOwner(),
                binding.btnVolverSeguimiento::setVisibility);
        vm.getBtnConfirmarVisibility().observe(getViewLifecycleOwner(), binding.btnConfirmar::setVisibility);
        vm.getBtnCancelarVisibility().observe(getViewLifecycleOwner(), binding.btnCancelar::setVisibility);
        vm.getEdtNotasEnabled().observe(getViewLifecycleOwner(), binding.edtNotas::setEnabled);

        vm.getEventoMostrarSnackbar().observe(getViewLifecycleOwner(), event -> {
            String mensaje = event.getContentIfNotHandled();
            reaccionarMensaje(mensaje);
        });

        vm.getNavegarASeguimiento().observe(getViewLifecycleOwner(), this::reaccionarNavegacionSeguimiento);

        vm.getEventoNavegar().observe(getViewLifecycleOwner(), event -> {
            Integer destinationId = event.getContentIfNotHandled();
            reaccionarNavegacion(destinationId);
        });
    }

    private void actualizarLista(java.util.List<com.jorge.mirotimobile.model.PedidoDTO> pedidos) {
        // Obtenemos el primero o vacío sin lógica condicional compleja en la vista
        com.jorge.mirotimobile.model.PedidoDTO pedido = (pedidos == null || pedidos.isEmpty()) ? null : pedidos.get(0);
        java.util.List<com.jorge.mirotimobile.model.DetallePedidoInfoDTO> detalles = (pedido == null)
                ? Collections.emptyList()
                : pedido.getDetalles();
        adapter.setItems(detalles);
        vm.procesarPedidoParaDetalle(pedido, adapter.getSubtotal());
    }

    private void mostrarError(String error) {
        if (error == null)
            return;
        Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_LONG).show();
    }

    private void reaccionarMensaje(String mensaje) {
        if (mensaje == null)
            return;
        Snackbar.make(binding.getRoot(), mensaje, Snackbar.LENGTH_SHORT).show();
    }

    private void reaccionarNavegacionSeguimiento(Boolean navegar) {
        if (!Boolean.TRUE.equals(navegar))
            return;
        Navigation.findNavController(requireView()).navigate(R.id.trackingFragment);
        vm.clearNavegarASeguimiento();
    }

    private void reaccionarNavegacion(Integer destinationId) {
        if (destinationId == null)
            return;
        if (destinationId == -1) {
            NavHostFragment.findNavController(this).popBackStack();
        } else {
            Navigation.findNavController(requireView()).navigate(destinationId);
        }
    }

    private void configurarListeners() {
        binding.toolbarDetalle
                .setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
        binding.btnConfirmar.setOnClickListener(v -> vm.confirmarPedido());
        binding.btnCancelar.setOnClickListener(v -> vm.cancelarYNavegar());
        binding.btnVolverSeguimiento.setOnClickListener(v -> vm.volverAlSeguimiento());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
