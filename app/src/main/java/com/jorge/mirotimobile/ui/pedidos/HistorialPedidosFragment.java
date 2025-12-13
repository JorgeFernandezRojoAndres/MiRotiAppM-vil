package com.jorge.mirotimobile.ui.pedidos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.jorge.mirotimobile.databinding.FragmentHistorialPedidosBinding;

public class HistorialPedidosFragment extends Fragment {

    private FragmentHistorialPedidosBinding binding;
    private PedidosViewModel viewModel;
    private PedidosAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHistorialPedidosBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new PedidosAdapter();
        binding.recyclerHistorial.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerHistorial.setAdapter(adapter);

        viewModel = new ViewModelProvider(requireActivity()).get(PedidosViewModel.class);
        viewModel.getHistorialPedidos().observe(getViewLifecycleOwner(), pedidos -> {
            adapter.setPedidos(pedidos);
            binding.txtHistorialError.setVisibility(View.GONE);
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(),
                loading -> binding.progressHistorial.setVisibility(Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE));

        viewModel.getMensajeError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                binding.txtHistorialError.setText(error);
                binding.txtHistorialError.setVisibility(View.VISIBLE);
            } else {
                binding.txtHistorialError.setVisibility(View.GONE);
            }
        });

        viewModel.cargarMisPedidos();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
