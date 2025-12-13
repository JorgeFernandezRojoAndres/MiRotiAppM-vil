package com.jorge.mirotimobile.ui.pedidos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.jorge.mirotimobile.R;
import com.jorge.mirotimobile.databinding.FragmentPedidosBinding;
import com.jorge.mirotimobile.model.PedidoDTO;

import java.util.List;

public class PedidosFragment extends Fragment {

    private FragmentPedidosBinding binding;
    private PedidosViewModel viewModel;
    private PedidosAdapter adapter;
    private List<PedidoDTO> cachedPedidos;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPedidosBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new PedidosAdapter();
        binding.recyclerPedidos.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerPedidos.setAdapter(adapter);

        viewModel = new ViewModelProvider(requireActivity()).get(PedidosViewModel.class);
        viewModel.getPedidos().observe(getViewLifecycleOwner(), pedidos -> {
            cachedPedidos = pedidos;
            adapter.setPedidos(pedidos);
            binding.txtPedidosError.setVisibility(View.GONE);
            PedidoDTO activo = (pedidos == null || pedidos.isEmpty()) ? null : pedidos.get(0);
            if (activo != null && activo.getId() == 0) {
                NavController navController = NavHostFragment.findNavController(this);
                if (navController.getCurrentDestination() != null
                        && navController.getCurrentDestination().getId() != R.id.detallePedidoFragment) {
                    navController.navigate(R.id.detallePedidoFragment);
                }
                return;
            }
            updatePedidoControls(activo);
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(),
                visible -> binding.progressPedidos.setVisibility(visible ? View.VISIBLE : View.GONE));

        viewModel.getMensajeError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                binding.txtPedidosError.setText(error);
                binding.txtPedidosError.setVisibility(View.VISIBLE);
            } else {
                binding.txtPedidosError.setVisibility(View.GONE);
            }
        });

        viewModel.cargarMisPedidos();

        binding.btnSeguimiento.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.trackingFragment));
    }

    private void updatePedidoControls(PedidoDTO pedido) {
        if (pedido == null) {
            binding.txtPedidosError.setText("No tenés pedidos aún");
            binding.txtPedidosError.setVisibility(View.VISIBLE);
            binding.btnCarrito.setVisibility(View.VISIBLE);
            binding.btnCarrito.setText("Ir al menú");
            binding.btnCarrito.setOnClickListener(v ->
                    Navigation.findNavController(v).navigate(R.id.menuFragment));
            binding.btnSeguimiento.setVisibility(View.GONE);
        } else {
            binding.txtPedidosError.setVisibility(View.GONE);
            binding.btnCarrito.setVisibility(View.GONE);
            binding.btnSeguimiento.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
