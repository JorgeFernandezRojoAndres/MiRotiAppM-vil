package com.jorge.mirotimobile.ui.cadete.entregas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.jorge.mirotimobile.R;
import com.jorge.mirotimobile.databinding.FragmentEntregasBinding;

public class EntregasFragment extends Fragment {

    private FragmentEntregasBinding binding;
    private EntregasViewModel viewModel;
    private EntregasAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentEntregasBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(EntregasViewModel.class);
        
        configurarRecyclerView();
        observarViewModel();
        configurarListeners();
    }



    private void configurarRecyclerView() {
        binding.recyclerProximasEntregas.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new EntregasAdapter(viewModel::tomarPedido);
        binding.recyclerProximasEntregas.setAdapter(adapter);
    }
    
    private void observarViewModel() {
        viewModel.getNombreCadete().observe(getViewLifecycleOwner(), binding.txtCadeteNombre::setText);
        viewModel.getCadeteEnServicio().observe(getViewLifecycleOwner(), binding.switchCadeteServicio::setChecked);
        viewModel.getEstadoCadete().observe(getViewLifecycleOwner(), binding.switchCadeteServicio::setText);
        viewModel.getTiempoPromedio().observe(getViewLifecycleOwner(), binding.txtTiempoPromedio::setText);
        
        viewModel.getPedidoActualId().observe(getViewLifecycleOwner(), binding.txtPedidoActualId::setText);
        viewModel.getPedidoActualDireccion().observe(getViewLifecycleOwner(), binding.txtPedidoActualDireccion::setText);
        viewModel.getPedidoActualCliente().observe(getViewLifecycleOwner(), binding.txtPedidoActualCliente::setText);
        
        viewModel.getProximasEntregas().observe(getViewLifecycleOwner(), proximas -> {
            adapter.submitList(proximas);
            viewModel.onProximasEntregasChanged(proximas);
        });
        viewModel.getSinEntregasVisibility().observe(getViewLifecycleOwner(), binding.txtSinEntregas::setVisibility);
        
        viewModel.getCardEntregaActualVisibility().observe(getViewLifecycleOwner(), binding.cardEntregaActual::setVisibility);
        viewModel.getCardSinEntregaVisibility().observe(getViewLifecycleOwner(), binding.cardSinEntrega::setVisibility);
        viewModel.getCardEstadoBackgroundColor().observe(getViewLifecycleOwner(), binding.cardEstadoEntrega::setCardBackgroundColor);
        viewModel.getEstadoEntregaTitulo().observe(getViewLifecycleOwner(), binding.txtEstadoEntregaTitulo::setText);
        viewModel.getEstadoEntregaDescripcion().observe(getViewLifecycleOwner(), binding.txtEstadoEntregaDescripcion::setText);
        viewModel.getBtnTomarPedidoVisibility().observe(getViewLifecycleOwner(), binding.btnTomarPedido::setVisibility);
        viewModel.getBtnIniciarEntregaVisibility().observe(getViewLifecycleOwner(), binding.btnIniciarEntrega::setVisibility);
        
        viewModel.getHistorialEntregas().observe(getViewLifecycleOwner(), viewModel::onHistorialChanged);
        viewModel.getHistorialTituloVisibility().observe(getViewLifecycleOwner(), binding.txtHistorialTitulo::setVisibility);
        
        viewModel.getEventoIrTracking().observe(getViewLifecycleOwner(), event -> {
            event.getContentIfNotHandled();
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_entregasFragment_to_trackingCadeteFragment);
        });
    }
    
    private void configurarListeners() {
        binding.switchCadeteServicio.setOnCheckedChangeListener((buttonView, isChecked) ->
                viewModel.setCadeteEnServicio(isChecked));
        binding.btnTomarPedido.setOnClickListener(v -> viewModel.tomarPedido());
        binding.btnIniciarEntrega.setOnClickListener(v -> viewModel.iniciarEntrega());
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
