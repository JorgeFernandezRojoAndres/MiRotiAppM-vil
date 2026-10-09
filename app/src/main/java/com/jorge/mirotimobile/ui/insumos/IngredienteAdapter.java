package com.jorge.mirotimobile.ui.insumos;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jorge.mirotimobile.databinding.ItemIngredienteBinding;
import com.jorge.mirotimobile.model.Ingrediente;
import com.jorge.mirotimobile.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 📋 IngredienteAdapter — Lista de ingredientes con su precio actual.
 * Notifica toques sobre un ingrediente para abrir la edición de precio.
 */
public class IngredienteAdapter extends RecyclerView.Adapter<IngredienteAdapter.IngredienteViewHolder> {

    public interface OnIngredienteClickListener {
        void onIngredienteClick(Ingrediente ingrediente);
    }

    private final List<Ingrediente> ingredientes = new ArrayList<>();
    private final OnIngredienteClickListener listener;
    private final NumberFormat currencyFormat =
            NumberFormat.getCurrencyInstance(new Locale("es", "AR"));

    public IngredienteAdapter(OnIngredienteClickListener listener) {
        this.listener = listener;
    }

    public void setIngredientes(List<Ingrediente> lista) {
        this.ingredientes.clear();
        if (lista != null) {
            this.ingredientes.addAll(lista);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public IngredienteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemIngredienteBinding binding = ItemIngredienteBinding.inflate(inflater, parent, false);
        return new IngredienteViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredienteViewHolder holder, int position) {
        Ingrediente ingrediente = ingredientes.get(position);
        holder.bind(ingrediente);
    }

    @Override
    public int getItemCount() {
        return ingredientes.size();
    }

    class IngredienteViewHolder extends RecyclerView.ViewHolder {
        private final ItemIngredienteBinding binding;

        IngredienteViewHolder(@NonNull ItemIngredienteBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Ingrediente ingrediente) {
            binding.txtNombreIngrediente.setText(ingrediente.getNombre());
            String unidad = ingrediente.getUnidadMedidaTexto();
            binding.txtUnidadIngrediente.setText(unidad == null || unidad.isEmpty()
                    ? "" : "Por " + unidad);
            binding.txtPrecioIngrediente.setText(currencyFormat.format(ingrediente.getPrecio()));

            // 📊 Stock — solo lectura, no se puede tocar desde aquí.
            if (ingrediente.getStockActual() > 0) {
                binding.txtStockIngrediente.setText(String.format(Locale.US, "%.2f", ingrediente.getStockActual()));
                binding.txtStockIngrediente.setTextColor(binding.getRoot().getContext().getColor(R.color.text_light));
                binding.txtStockIngrediente.setVisibility(android.view.View.VISIBLE);
            } else {
                binding.txtStockIngrediente.setVisibility(android.view.View.GONE);
            }

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onIngredienteClick(ingrediente);
                }
            });
        }
    }
}
