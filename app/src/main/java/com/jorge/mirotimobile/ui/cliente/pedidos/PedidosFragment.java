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
import com.jorge.mirotimobile.util.Event;
import android.util.Log;

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
        Log.d("MAIN_FLOW", "PedidosFragment onViewCreated");

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

        // Observar evento de Snackbar
        viewModel.getEventoMostrarSnackbar().observe(getViewLifecycleOwner(), event -> {
            String mensaje = event.getContentIfNotHandled();
            if (mensaje != null) {
                Snackbar.make(binding.getRoot(), mensaje, Snackbar.LENGTH_SHORT).show();
            }
        });

        // Observar navegación al menú
        viewModel.getShouldNavigateToMenu().observe(getViewLifecycleOwner(), shouldNavigate -> {
            if (Boolean.TRUE.equals(shouldNavigate)) {
                Navigation.findNavController(requireView()).navigate(R.id.menuFragment);
                viewModel.resetNavigateToMenu();
            }
        });

        // Observar navegación genérica
        viewModel.getEventoNavegar().observe(getViewLifecycleOwner(), event -> {
            Integer destinationId = event.getContentIfNotHandled();
            if (destinationId != null) {
                Navigation.findNavController(requireView()).navigate(destinationId);
            }
        });
    }

    private void configurarListeners() {
        binding.btnSeguimiento.setOnClickListener(v -> viewModel.navegarASeguimiento());
        binding.btnCarrito.setOnClickListener(v -> viewModel.navegarAlMenu());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
