package com.jorge.mirotimobile.ui.insumos;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.jorge.mirotimobile.R;
import com.jorge.mirotimobile.databinding.FragmentInsumosBinding;
import com.jorge.mirotimobile.model.Ingrediente;

/**
 * 🥕 InsumosFragment — Pantalla para el rol "Administrador de Insumos":
 * lista de ingredientes con precios, búsqueda, y alta/edición mediante BottomSheet.
 */
public class InsumosFragment extends Fragment implements IngredienteAdapter.OnIngredienteClickListener {

    private FragmentInsumosBinding binding;
    private InsumosViewModel viewModel;
    private IngredienteAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentInsumosBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(InsumosViewModel.class);

        adapter = new IngredienteAdapter(this);

        binding.recyclerInsumos.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerInsumos.setAdapter(adapter);

        // 🔎 Búsqueda
        binding.editBuscarInsumo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.filtrar(s != null ? s.toString() : "");
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        // ➕ Nuevo ingrediente
        binding.fabNuevoInsumo.setOnClickListener(v -> {
            IngredienteFormularioFragment sheet = IngredienteFormularioFragment.newInstance(null);
            sheet.show(getChildFragmentManager(), "IngredienteFormularioFragment");
        });

        observarViewModel();
        viewModel.cargarIngredientes();

        // Refresca los stocks al volver a la pantalla (los requiere el usuario fijo).
        getViewLifecycleOwner().getLifecycle().addObserver(new androidx.lifecycle.DefaultLifecycleObserver() {
            public void onResume(@NonNull LifecycleOwner owner) {
                viewModel.cargarIngredientes(true);
            }
        });
    }

    private void observarViewModel() {
        viewModel.getIngredientesFiltered().observe(getViewLifecycleOwner(), lista -> {
            adapter.setIngredientes(lista);
            boolean vacio = lista == null || lista.isEmpty();
            binding.txtSinInsumos.setVisibility(vacio ? View.VISIBLE : View.GONE);
        });

        viewModel.getProgressVisibility().observe(getViewLifecycleOwner(), visible ->
                binding.progressInsumos.setVisibility(visible != null ? visible : View.GONE));

        viewModel.getErrorVisibility().observe(getViewLifecycleOwner(), visible ->
                binding.txtErrorInsumos.setVisibility(visible != null ? visible : View.GONE));

        viewModel.getMensajeError().observe(getViewLifecycleOwner(), mensaje -> {
            if (mensaje != null) {
                binding.txtErrorInsumos.setText(mensaje);
            }
        });

        viewModel.getMensajeStock().observe(getViewLifecycleOwner(), mensaje -> {
            if (mensaje != null) {
                binding.txtStockResumen.setText(mensaje);
                binding.txtStockResumen.setVisibility(android.view.View.VISIBLE);
            }
        });

    }

    @Override
    public void onIngredienteClick(@NonNull Ingrediente ingrediente) {
        IngredienteFormularioFragment sheet = IngredienteFormularioFragment.newInstance(ingrediente);
        sheet.show(getChildFragmentManager(), "IngredienteFormularioFragment");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
