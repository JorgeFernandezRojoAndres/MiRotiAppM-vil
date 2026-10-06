package com.jorge.mirotimobile.ui.insumos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.jorge.mirotimobile.databinding.FragmentIngredienteFormularioBinding;
import com.jorge.mirotimobile.model.Ingrediente;

/**
 * 📝 IngredienteFormularioFragment — BottomSheet para cargar un ingrediente nuevo
 * o corregir el precio de uno existente.
 * - Alta: envía { nombre, costoUnitario, unidadMedida: "kg" } (DTO del backend).
 * - Edición: envía { costoUnitario } con el id en la URL.
 */
public class IngredienteFormularioFragment extends BottomSheetDialogFragment {

    private static final String ARG_INGREDIENTE = "arg_ingrediente";

    private FragmentIngredienteFormularioBinding binding;
    private InsumosViewModel viewModel;
    private Ingrediente ingrediente; // null = alta, != null = edición
    private boolean esEdicion;

    public static IngredienteFormularioFragment newInstance(@Nullable Ingrediente ingrediente) {
        IngredienteFormularioFragment fragment = new IngredienteFormularioFragment();
        Bundle args = new Bundle();
        if (ingrediente != null) {
            args.putSerializable(ARG_INGREDIENTE, ingrediente);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            ingrediente = (Ingrediente) getArguments().getSerializable(ARG_INGREDIENTE);
        }
        esEdicion = ingrediente != null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentIngredienteFormularioBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Compartir el ViewModel con InsumosFragment (misma sesión/API)
        viewModel = new ViewModelProvider(requireParentFragment()).get(InsumosViewModel.class);

        binding.txtTituloFormulario.setText(esEdicion
                ? "Corregir precio del ingrediente"
                : "Nuevo ingrediente");
        binding.btnGuardar.setText(esEdicion ? "Actualizar precio" : "Cargar ingrediente");

        if (esEdicion) {
            binding.editNombreIngrediente.setText(ingrediente.getNombre());
            binding.editNombreIngrediente.setEnabled(false); // no se renombra al corregir precio
            binding.editUnidadIngrediente.setText(ingrediente.getUnidadMedidaTexto());
            binding.editUnidadIngrediente.setEnabled(false);
            binding.editPrecioIngrediente.setText(precioATexto(ingrediente.getPrecio()));
            binding.editPrecioIngrediente.requestFocus();
        }

        binding.btnCancelar.setOnClickListener(v -> dismiss());
        binding.btnGuardar.setOnClickListener(v -> guardar());
    }

    private void guardar() {
        String nombre = texto(binding.editNombreIngrediente);
        String precioTexto = texto(binding.editPrecioIngrediente);

        // 🔒 Validaciones
        if (!esEdicion && nombre.isEmpty()) {
            binding.layoutNombreIngrediente.setError("Ingresá el nombre del ingrediente");
            return;
        }
        Double precio = parsearPrecio(precioTexto);
        if (precio == null || precio < 0) {
            binding.layoutPrecioIngrediente.setError("Ingresá un precio válido (mayor o igual a 0)");
            return;
        }

        binding.txtErrorFormulario.setVisibility(View.GONE);
        binding.btnGuardar.setEnabled(false);

        Ingrediente aGuardar = new Ingrediente();
        if (esEdicion) {
            // PUT: solo importa id + precio
            aGuardar.setId(ingrediente.getId());
            aGuardar.setNombre(ingrediente.getNombre());
            aGuardar.setPrecio(precio);
        } else {
            // POST: { nombre, costoUnitario, unidadMedida: abreviatura }
            String unidad = texto(binding.editUnidadIngrediente);
            Ingrediente.UnidadMedidaInfo unidadInfo = new Ingrediente.UnidadMedidaInfo();
            unidadInfo.setAbreviatura(unidad.isEmpty() ? "kg" : unidad);

            aGuardar.setNombre(nombre);
            aGuardar.setPrecio(precio);
            aGuardar.setUnidadMedida(unidadInfo);
        }

        viewModel.guardarIngrediente(aGuardar);

        // Único consumidor del evento: éxito → cerrar; error → mostrar y permitir reintentar
        viewModel.getEventoGuardado().observe(getViewLifecycleOwner(), evento -> {
            if (evento == null) return;
            InsumosViewModel.EventoGuardado resultado = evento.getContentIfNotHandled();
            if (resultado == null) return;
            if (resultado.exitoso) {
                dismiss();
            } else {
                binding.btnGuardar.setEnabled(true);
                binding.txtErrorFormulario.setVisibility(View.VISIBLE);
                binding.txtErrorFormulario.setText(resultado.mensaje != null
                        ? resultado.mensaje : "No se pudo guardar");
            }
        });
    }

    private String texto(com.google.android.material.textfield.TextInputEditText edit) {
        return edit.getText() != null ? edit.getText().toString().trim() : "";
    }

    private String precioATexto(double precio) {
        if (precio == (long) precio) {
            return String.valueOf((long) precio);
        }
        return String.valueOf(precio);
    }

    private Double parsearPrecio(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(texto.replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
