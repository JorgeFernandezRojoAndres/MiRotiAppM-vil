package com.jorge.mirotimobile.ui.cadete.entregas;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.jorge.mirotimobile.R;
import com.jorge.mirotimobile.databinding.FragmentEntregasBinding;
import com.jorge.mirotimobile.model.PedidoDTO;
import com.jorge.mirotimobile.util.Event;

import java.util.List;

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
        Log.d("CADETE_FLOW", "EntregasFragment onViewCreated");
        viewModel = new ViewModelProvider(requireActivity()).get(EntregasViewModel.class);
        binding.recyclerProximasEntregas.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new EntregasAdapter(viewModel::tomarPedido);
        binding.recyclerProximasEntregas.setAdapter(adapter);

        viewModel.getNombreCadete().observe(getViewLifecycleOwner(), binding.txtCadeteNombre::setText);
        viewModel.getCadeteEnServicio().observe(getViewLifecycleOwner(), binding.switchCadeteServicio::setChecked);
        viewModel.getEstadoCadete().observe(getViewLifecycleOwner(), binding.switchCadeteServicio::setText);
        viewModel.getTiempoPromedio().observe(getViewLifecycleOwner(), binding.txtTiempoPromedio::setText);
        viewModel.getPedidoActual().observe(getViewLifecycleOwner(), this::bindEntregaActual);
        viewModel.getProximasEntregas().observe(getViewLifecycleOwner(), this::bindProximasEntregas);
        viewModel.getEstadoEntregaUi().observe(getViewLifecycleOwner(), this::bindEstadoEntregaUi);
        viewModel.getHistorialEntregas().observe(getViewLifecycleOwner(), this::bindHistorialEntregas);
        viewModel.getEventoIrTracking().observe(getViewLifecycleOwner(), this::handleEventoIrTracking);

        binding.switchCadeteServicio.setOnCheckedChangeListener((buttonView, isChecked) ->
                viewModel.setCadeteEnServicio(isChecked)
        );

        binding.btnTomarPedido.setOnClickListener(v -> viewModel.tomarPedido());
        binding.btnIniciarEntrega.setOnClickListener(v -> {
            viewModel.iniciarEntrega();
        });
    }

    private void bindEntregaActual(PedidoDTO pedido) {
        if (pedido == null) return;
        binding.txtPedidoActualId.setText("Pedido #" + pedido.getId());
        binding.txtPedidoActualDireccion.setText(pedido.getDireccion());
        binding.txtPedidoActualCliente.setText(pedido.getCliente());
    }

    private void bindProximasEntregas(List<PedidoDTO> proximas) {
        adapter.submitList(proximas);
        binding.txtSinEntregas.setVisibility(proximas == null || proximas.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void bindEstadoEntregaUi(EntregasViewModel.EstadoEntregaUiState ui) {
        if (ui == null) return;
        binding.cardEntregaActual.setVisibility(
                ui.mostrarEntregaActual ? View.VISIBLE : View.GONE
        );
        binding.cardSinEntrega.setVisibility(
                ui.mostrarEntregaActual ? View.GONE : View.VISIBLE
        );
        binding.cardEstadoEntrega.setCardBackgroundColor(ContextCompat.getColor(requireContext(), ui.backgroundColorRes));
        binding.txtEstadoEntregaTitulo.setText(ui.titulo);
        binding.txtEstadoEntregaDescripcion.setText(ui.descripcion);

        binding.btnTomarPedido.setVisibility(ui.mostrarTomarPedido ? View.VISIBLE : View.GONE);
        binding.btnIniciarEntrega.setVisibility(ui.mostrarIniciarEntrega ? View.VISIBLE : View.GONE);
    }

    private void bindHistorialEntregas(List<PedidoDTO> historial) {
        binding.layoutHistorial.removeAllViews();
        if (historial == null || historial.isEmpty()) {
            binding.txtHistorialTitulo.setVisibility(View.GONE);
            return;
        }
        binding.txtHistorialTitulo.setVisibility(View.VISIBLE);
        for (PedidoDTO pedido : historial) {
            TextView item = new TextView(requireContext());
            item.setTextColor(ContextCompat.getColor(requireContext(), R.color.cadete_text_secondary));
            item.setText(String.format("Pedido #%d · %s", pedido.getId(),
                    pedido.getEstado() != null ? pedido.getEstado() : ""));
            item.setTextSize(14f);
            binding.layoutHistorial.addView(item);
        }
    }

    private void handleEventoIrTracking(Event<Integer> event) {
        Log.d("CADETE_FLOW", "handleEventoIrTracking CALLED");

        if (event == null) {
            Log.d("CADETE_FLOW", "event == null");
            return;
        }
        Integer pedidoId = event.getContentIfNotHandled();
        Log.d("CADETE_FLOW", "pedidoId = " + pedidoId);
        if (pedidoId == null) return;

        Log.d("CADETE_FLOW", "NAVIGATE -> TrackingCadeteFragment");
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_entregasFragment_to_trackingCadeteFragment);
        Log.d("CADETE_FLOW", "navigate DONE");
        viewModel.limpiarEventos();
        Log.d("CADETE_FLOW", "limpiarEventos DONE");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
