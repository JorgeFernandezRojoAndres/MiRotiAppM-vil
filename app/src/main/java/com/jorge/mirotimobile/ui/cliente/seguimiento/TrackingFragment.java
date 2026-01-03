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
    private com.jorge.mirotimobile.model.PedidoDTO pedidoActivo;

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void observarViewModel() {
        pedidosVm.getPedidos().observe(getViewLifecycleOwner(), pedidos -> {
            com.jorge.mirotimobile.model.PedidoDTO activo = (pedidos == null || pedidos.isEmpty()) ? null : pedidos.get(0);
            pedidoActivo = activo;
            pedidosVm.procesarPedidoParaTracking(activo);
        });
        
        pedidosVm.getTrackingTitle().observe(getViewLifecycleOwner(), binding.txtTrackingTitle::setText);
        pedidosVm.getTrackingSubtitle().observe(getViewLifecycleOwner(), binding.txtTrackingSubtitle::setText);
        pedidosVm.getTrackingArrivalTime().observe(getViewLifecycleOwner(), binding.txtArrivalTime::setText);
        pedidosVm.getCadeteName().observe(getViewLifecycleOwner(), nombre -> {
            if (nombre != null) {
                binding.txtCadeteName.setText("Cadete: " + nombre);
                binding.txtCadeteName.setVisibility(View.VISIBLE);
            } else {
                binding.txtCadeteName.setText("Cadete: Sin asignar");
                binding.txtCadeteName.setVisibility(View.VISIBLE);
            }
        });
        
        pedidosVm.getCadetePhone().observe(getViewLifecycleOwner(), telefono -> {
            if (telefono != null) {
                binding.txtCadetePhone.setText("Tel: " + telefono);
                binding.txtCadetePhone.setVisibility(View.VISIBLE);
                binding.btnContactarCadete.setVisibility(View.VISIBLE);
            } else {
                binding.txtCadetePhone.setText("Tel: -");
                binding.btnContactarCadete.setVisibility(View.GONE);
            }
        });
        pedidosVm.getEstadoTexto().observe(getViewLifecycleOwner(), binding.txtEstadoTitle::setText);
        pedidosVm.getTrackingSubtitle().observe(getViewLifecycleOwner(), binding.txtEstadoDescription::setText);
        
        pedidosVm.getEventoMostrarMensaje().observe(getViewLifecycleOwner(), event -> {
            String mensaje = event.getContentIfNotHandled();
            Snackbar.make(binding.getRoot(), mensaje, Snackbar.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).popBackStack();
        });
        
        pedidosVm.getEventoContactarCadete().observe(getViewLifecycleOwner(), event -> {
            String telefono = event.getContentIfNotHandled();
            if (telefono == null || telefono.trim().isEmpty()) {
                return;
            }
            Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + telefono));
            startActivity(dialIntent);
        });

        binding.btnAbrirMapaCliente.setOnClickListener(v -> {
            if (pedidoActivo == null) {
                Snackbar.make(binding.getRoot(), "No hay pedido para mostrar en el mapa", Snackbar.LENGTH_SHORT).show();
                return;
            }
            Bundle args = new Bundle();
            args.putString("direccionCliente", pedidoActivo.getDireccion());
            Navigation.findNavController(requireView()).navigate(R.id.action_trackingFragment_to_mapaEntregaFragment, args);
        });
    }
    
    private void configurarListeners() {
        binding.btnContactarCadete.setOnClickListener(v -> pedidosVm.contactarCadete(pedidoActivo));
        binding.btnVerDetalles.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.detallePedidoFragment));
    }
}
