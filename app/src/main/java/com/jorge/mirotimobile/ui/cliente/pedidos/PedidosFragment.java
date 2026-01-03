package com.jorge.mirotimobile.ui.cliente.pedidos;

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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.jorge.mirotimobile.R;
import com.jorge.mirotimobile.databinding.FragmentPedidosBinding;

public class PedidosFragment extends Fragment {

    private FragmentPedidosBinding binding;
    private PedidosViewModel viewModel;
    private PedidosAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPedidosBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        configurarRecyclerView();
        viewModel = new ViewModelProvider(requireActivity()).get(PedidosViewModel.class);
        observarViewModel();
        configurarListeners();
        viewModel.cargarMisPedidos();
    }

    private void configurarRecyclerView() {
        adapter = new PedidosAdapter();
        binding.recyclerPedidos.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerPedidos.setAdapter(adapter);
    }
    
    private void observarViewModel() {
        viewModel.getPedidos().observe(getViewLifecycleOwner(), adapter::setPedidos);
        
        viewModel.getProgressVisibility().observe(getViewLifecycleOwner(), binding.progressPedidos::setVisibility);
        viewModel.getErrorVisibility().observe(getViewLifecycleOwner(), binding.txtPedidosError::setVisibility);
        viewModel.getErrorText().observe(getViewLifecycleOwner(), binding.txtPedidosError::setText);
        
        viewModel.getBtnCarritoVisibility().observe(getViewLifecycleOwner(), binding.btnCarrito::setVisibility);
        viewModel.getBtnCarritoText().observe(getViewLifecycleOwner(), binding.btnCarrito::setText);
        viewModel.getBtnSeguimientoVisibility().observe(getViewLifecycleOwner(), binding.btnSeguimiento::setVisibility);
        
        viewModel.getEventoNavegacion().observe(getViewLifecycleOwner(), event -> {
            Integer destinationId = event.getContentIfNotHandled();
            Navigation.findNavController(requireView()).navigate(destinationId);
        });
    }
    
    private void configurarListeners() {
        binding.btnSeguimiento.setOnClickListener(v -> {
            java.util.List<com.jorge.mirotimobile.model.PedidoDTO> pedidosActivos =
                    viewModel.getPedidos().getValue();
            if (pedidosActivos == null || pedidosActivos.isEmpty()) {
                Snackbar.make(binding.getRoot(), "No tenés pedidos en seguimiento", Snackbar.LENGTH_SHORT).show();
                return;
            }
            NavHostFragment.findNavController(this).navigate(R.id.trackingFragment);
        });
        binding.btnCarrito.setOnClickListener(v -> {
            Integer destinationId = viewModel.getEventoNavegacion().getValue().peekContent();
            Navigation.findNavController(v).navigate(destinationId);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
