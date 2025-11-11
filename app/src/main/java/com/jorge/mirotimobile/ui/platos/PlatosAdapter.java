package com.jorge.mirotimobile.ui.platos;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jorge.mirotimobile.databinding.ItemPlatoBinding;
import com.jorge.mirotimobile.model.Plato;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 🍽️ PlatosAdapter — Muestra la lista de platos disponibles.
 * Utiliza ViewBinding y sigue el patrón MVVM (sin lógica de negocio en la vista).
 */
public class PlatosAdapter extends RecyclerView.Adapter<PlatosAdapter.PlatosViewHolder> {

    private final List<Plato> listaPlatos = new ArrayList<>();

    @NonNull
    @Override
    public PlatosViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPlatoBinding binding = ItemPlatoBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new PlatosViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PlatosViewHolder holder, int position) {
        holder.bind(listaPlatos.get(position));
    }

    @Override
    public int getItemCount() {
        return listaPlatos.size();
    }

    // ✅ Método usado por el Fragment para actualizar la lista observada
    public void actualizarLista(List<Plato> nuevosPlatos) {
        listaPlatos.clear();
        if (nuevosPlatos != null) {
            listaPlatos.addAll(nuevosPlatos);
        }
        notifyDataSetChanged();
    }

    static class PlatosViewHolder extends RecyclerView.ViewHolder {
        private final ItemPlatoBinding binding;

        public PlatosViewHolder(@NonNull ItemPlatoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        // 🔹 Vincula los datos del plato con la vista
        public void bind(Plato plato) {
            binding.txtNombrePlato.setText(plato.getNombre());
            binding.txtDescripcionPlato.setText(plato.getDescripcion());

            // ✅ Formateo de precio (con formato AR)
            NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));
            String precio = format.format(plato.getPrecioVenta());
            binding.txtPrecioPlato.setText("💲 " + precio);
        }
    }
}
