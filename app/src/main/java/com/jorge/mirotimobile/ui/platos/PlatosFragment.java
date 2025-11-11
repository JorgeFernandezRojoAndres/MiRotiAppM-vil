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

/**
 * 🍽️ PlatosFragment — Muestra la lista de platos disponibles.
 * No contiene lógica, validaciones ni condiciones.
 * Observa LiveData del ViewModel y actualiza la UI.
 */
public class PlatosFragment extends Fragment {

    private FragmentPlatosBinding binding;
    private PlatosViewModel vm;
    private PlatosAdapter adapter;

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

        // Configurar RecyclerView
        adapter = new PlatosAdapter();
        binding.recyclerPlatos.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerPlatos.setAdapter(adapter);

        // Observa lista de platos → actualiza la vista
        vm.getPlatos().observe(getViewLifecycleOwner(), adapter::actualizarLista);

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
}
