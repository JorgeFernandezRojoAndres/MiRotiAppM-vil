package com.jorge.mirotimobile.ui.platos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.jorge.mirotimobile.databinding.FragmentPlatosBinding;
import com.jorge.mirotimobile.localdata.SessionManager;
import com.jorge.mirotimobile.model.PedidoResumen;
import com.jorge.mirotimobile.model.Plato;
import com.jorge.mirotimobile.ui.pedidos.PedidosRecientesAdapter;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * 🍽️ PlatosFragment — Muestra la lista de platos disponibles.
 * No contiene lógica, validaciones ni condiciones.
 * Observa LiveData del ViewModel y actualiza la UI.
 */
public class PlatosFragment extends Fragment {

    private FragmentPlatosBinding binding;
    private PlatosViewModel vm;
    private PlatosAdapter adapter;
    private PlatosDestacadosAdapter destacadosAdapter;
    private PedidosRecientesAdapter pedidosAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPlatosBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        vm = new ViewModelProvider(this).get(PlatosViewModel.class);

        // Configurar saludo
        configurarSaludo();

        // Configurar RecyclerViews
        adapter = new PlatosAdapter();
        destacadosAdapter = new PlatosDestacadosAdapter();
        pedidosAdapter = new PedidosRecientesAdapter();

        binding.recyclerPlatos.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerPlatos.setAdapter(adapter);

        binding.recyclerDestacados.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerDestacados.setAdapter(destacadosAdapter);

        binding.recyclerPedidosRecientes.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerPedidosRecientes.setAdapter(pedidosAdapter);
        pedidosAdapter.actualizarLista(obtenerPedidosDemo());

        // Observa lista de platos → actualiza la vista
        vm.getPlatos().observe(getViewLifecycleOwner(), platos -> {
            adapter.actualizarLista(platos);

            // Mostrar todos los platos en destacados (scroll horizontal)
            List<Plato> destacados = (platos == null) ? Collections.emptyList() : platos;
            destacadosAdapter.actualizarLista(destacados);
            actualizarMetricas(platos);
        });

        // Observa estado de carga → muestra u oculta el progress bar
        vm.getLoading().observe(getViewLifecycleOwner(),
                visible -> binding.progressBar.setVisibility(visible ? View.VISIBLE : View.GONE));

        // Observa errores → muestra u oculta el mensaje
        vm.getMensajeError().observe(getViewLifecycleOwner(), error -> {
            binding.txtError.setText(error);
            binding.txtError.setVisibility(error != null ? View.VISIBLE : View.GONE);
        });

        // Inicia la carga (ViewModel maneja la lógica y llamadas Retrofit)
        vm.cargarPlatos();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
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
        if (lista == null || lista.isEmpty()) {
            binding.metricTotalValue.setText("0");
            binding.metricPromedioValue.setText("$0");
            binding.metricTopValue.setText("$0");
            return;
        }

        double max = 0;
        double sum = 0;
        for (Plato p : lista) {
            double precio = p.getPrecioVenta();
            sum += precio;
            if (precio > max) {
                max = precio;
            }
        }

        int total = lista.size();
        double promedio = sum / total;
        NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));

        binding.metricTotalValue.setText(String.valueOf(total));
        binding.metricPromedioValue.setText(currency.format(promedio));
        binding.metricTopValue.setText(currency.format(max));
    }

    private List<PedidoResumen> obtenerPedidosDemo() {
        List<PedidoResumen> demo = new ArrayList<>();
        demo.add(new PedidoResumen("#1234", "2024-07-26", "Entregado"));
        demo.add(new PedidoResumen("#1233", "2024-07-26", "En Proceso"));
        demo.add(new PedidoResumen("#1232", "2024-07-26", "Entregado"));
        return demo;
    }
}
