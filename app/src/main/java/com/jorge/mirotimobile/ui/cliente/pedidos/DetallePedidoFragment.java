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
        
        configurarArgumentos();
        configurarRecyclerView();
        configurarViewModel();
        observarViewModel();
        configurarListeners();
    }

    private void configurarArgumentos() {
        Bundle args = getArguments();
        modoCadete = args != null && args.getBoolean("modoCadete", false);
    }
    
    private void configurarRecyclerView() {
        adapter = new CarritoAdapter(() -> vm.procesarPedidoParaDetalle(null, adapter.getSubtotal()));
        binding.recyclerCarrito.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerCarrito.setAdapter(adapter);
    }
    
    private void configurarViewModel() {
        vm = new ViewModelProvider(requireActivity()).get(PedidosViewModel.class);
        vm.aplicarModoCadete(modoCadete);
    }
    
    private void observarViewModel() {
        vm.getPedidos().observe(getViewLifecycleOwner(), pedidos -> {
            com.jorge.mirotimobile.model.PedidoDTO primer = (pedidos == null) ? null :
                    pedidos.stream().findFirst().orElse(null);
            mostrarPedido(primer);
        });
        
        vm.getProgressVisibility().observe(getViewLifecycleOwner(), binding.menuProgress::setVisibility);
        vm.getErrorText().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_LONG).show();
            }
        });
        
        vm.getPedidoNumero().observe(getViewLifecycleOwner(), binding.txtPedidoNumero::setText);
        vm.getPedidoFecha().observe(getViewLifecycleOwner(), binding.txtPedidoFecha::setText);
        vm.getEstadoTexto().observe(getViewLifecycleOwner(), binding.chipEstado::setText);
        vm.getEstadoColorRes().observe(getViewLifecycleOwner(), binding.chipEstado::setChipBackgroundColorResource);
        vm.getEstadoTextColorRes().observe(getViewLifecycleOwner(), colorRes -> 
            binding.chipEstado.setTextColor(getResources().getColor(colorRes, null)));
        
        vm.getSubtotalTexto().observe(getViewLifecycleOwner(), binding.txtSubtotal::setText);
        vm.getEnvioTexto().observe(getViewLifecycleOwner(), binding.txtEnvio::setText);
        vm.getTotalTexto().observe(getViewLifecycleOwner(), binding.txtTotal::setText);
        
        vm.getBtnConfirmarTexto().observe(getViewLifecycleOwner(), binding.btnConfirmar::setText);
        vm.getBtnConfirmarEnabled().observe(getViewLifecycleOwner(), binding.btnConfirmar::setEnabled);
        
        vm.getBtnVolverSeguimientoVisibility().observe(getViewLifecycleOwner(), binding.btnVolverSeguimiento::setVisibility);
        vm.getBtnConfirmarVisibility().observe(getViewLifecycleOwner(), binding.btnConfirmar::setVisibility);
        vm.getBtnCancelarVisibility().observe(getViewLifecycleOwner(), binding.btnCancelar::setVisibility);
        vm.getEdtNotasEnabled().observe(getViewLifecycleOwner(), binding.edtNotas::setEnabled);
        
        vm.getEventoMostrarSnackbar().observe(getViewLifecycleOwner(), event -> {
            String mensaje = event.getContentIfNotHandled();
            if (mensaje == null) {
                return;
            }
            Snackbar.make(binding.getRoot(), mensaje, Snackbar.LENGTH_SHORT).show();
        });
        
        vm.getNavegarASeguimiento().observe(getViewLifecycleOwner(), navegar -> {
            if (Boolean.TRUE.equals(navegar)) {
                Navigation.findNavController(requireView()).navigate(R.id.trackingFragment);
                vm.clearNavegarASeguimiento();
            }
        });
    }
    
    private void configurarListeners() {
        binding.toolbarDetalle.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
        binding.toolbarDetalle.setOnMenuItemClickListener(item -> {
            Toast.makeText(requireContext(), "Editar pedido", Toast.LENGTH_SHORT).show();
            return true;
        });
        
        binding.btnConfirmar.setOnClickListener(v -> vm.confirmarPedido());
        binding.btnCancelar.setOnClickListener(v -> {
            vm.cancelarYNavegar();
            NavHostFragment.findNavController(this).popBackStack();
        });
        binding.btnVolverSeguimiento.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.trackingCadeteFragment));
    }
    
    private void mostrarPedido(com.jorge.mirotimobile.model.PedidoDTO pedido) {
        if (pedido != null) {
            adapter.setItems(pedido.getDetalles());
        } else {
            adapter.setItems(Collections.emptyList());
        }
        vm.procesarPedidoParaDetalle(pedido, adapter.getSubtotal());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
