package com.jorge.mirotimobile.ui.nav;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.jorge.mirotimobile.databinding.FragmentBienvenidaBinding;
import com.jorge.mirotimobile.localdata.SessionManager;
import com.jorge.mirotimobile.model.PedidoResumen;
import com.jorge.mirotimobile.model.Plato;
import com.jorge.mirotimobile.ui.cliente.pedidos.PedidosRecientesAdapter;
import com.jorge.mirotimobile.ui.platos.PlatosDestacadosAdapter;
import com.jorge.mirotimobile.ui.platos.PlatosViewModel;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Piso de bienvenida tras login del cliente.
 */
public class BienvenidaFragment extends Fragment {

    private FragmentBienvenidaBinding binding;
    private PlatosViewModel vm;
    private PedidosRecientesAdapter pedidosAdapter;
    private PlatosDestacadosAdapter destacadosAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentBienvenidaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        vm = new ViewModelProvider(requireActivity()).get(PlatosViewModel.class);
        pedidosAdapter = new PedidosRecientesAdapter();
        destacadosAdapter = new PlatosDestacadosAdapter();

        binding.recyclerDestacados.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerDestacados.setAdapter(destacadosAdapter);

        binding.recyclerPedidosRecientes.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerPedidosRecientes.setAdapter(pedidosAdapter);
        pedidosAdapter.actualizarLista(obtenerPedidosDemo());

        vm.getPlatos().observe(getViewLifecycleOwner(), platos -> {
            List<Plato> destacados = (platos == null) ? Collections.emptyList() : platos;
            destacadosAdapter.actualizarLista(destacados);
            actualizarMetricas(platos);
        });

        vm.getLoading().observe(getViewLifecycleOwner(),
                visible -> binding.progressBar.setVisibility(visible ? View.VISIBLE : View.GONE));

        vm.getMensajeError().observe(getViewLifecycleOwner(), error -> {
            binding.txtError.setText(error);
            binding.txtError.setVisibility(error != null ? View.VISIBLE : View.GONE);
        });

        vm.cargarPlatos();
        configurarSaludo();
    }

    private void configurarSaludo() {
        SessionManager session = new SessionManager(requireContext());
        String email = session.getUserEmail();
        String nombre = "Cliente";
        if (email != null && email.contains("@")) {
            nombre = email.substring(0, email.indexOf('@'));
        }
        binding.txtGreetingTitle.setText("¡Bienvenido, " + nombre + "!");
    }

    private void actualizarMetricas(List<Plato> lista) {
        NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));
        if (lista == null || lista.isEmpty()) {
            binding.metricTotalValue.setText("0");
            binding.metricSaldoValue.setText(currency.format(0));
            return;
        }

        int total = lista.size();
        double sum = 0;
        for (Plato p : lista) {
            sum += p.getPrecioVenta();
        }

        binding.metricTotalValue.setText(String.valueOf(total));
        binding.metricSaldoValue.setText(currency.format(sum));
    }

    private List<PedidoResumen> obtenerPedidosDemo() {
        List<PedidoResumen> demo = new ArrayList<>();
        demo.add(new PedidoResumen("#1234", "2024-07-26", "Entregado"));
        demo.add(new PedidoResumen("#1233", "2024-07-26", "En Proceso"));
        demo.add(new PedidoResumen("#1232", "2024-07-26", "Entregado"));
        return demo;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
