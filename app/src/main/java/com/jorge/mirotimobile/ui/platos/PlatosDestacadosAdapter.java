package com.jorge.mirotimobile.ui.platos;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jorge.mirotimobile.BuildConfig;
import com.jorge.mirotimobile.databinding.ItemPlatoDestacadoBinding;
import com.jorge.mirotimobile.model.Plato;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

/**
 * Adaptador horizontal para los platos destacados de la pantalla de inicio.
 */
public class PlatosDestacadosAdapter extends RecyclerView.Adapter<PlatosDestacadosAdapter.DestacadoViewHolder> {

    private final List<Plato> destacados = new ArrayList<>();

    @NonNull
    @Override
    public DestacadoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPlatoDestacadoBinding binding = ItemPlatoDestacadoBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new DestacadoViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DestacadoViewHolder holder, int position) {
        holder.bind(destacados.get(position));
    }

    @Override
    public int getItemCount() {
        return destacados.size();
    }

    public void actualizarLista(List<Plato> nuevos) {
        destacados.clear();
        if (nuevos != null) {
            destacados.addAll(nuevos);
        }
        notifyDataSetChanged();
    }

    static class DestacadoViewHolder extends RecyclerView.ViewHolder {
        private final ItemPlatoDestacadoBinding binding;
        private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));

        DestacadoViewHolder(@NonNull ItemPlatoDestacadoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Plato plato) {
            binding.txtNombrePlatoDestacado.setText(plato.getNombre());
            binding.txtDescripcionPlatoDestacado.setText(plato.getDescripcion());
            binding.txtPrecioPlatoDestacado.setText(currencyFormat.format(plato.getPrecioVenta()));

            String url = makeAbsoluteUrl(plato.getImagenUrl());
            if (url != null && !url.trim().isEmpty()) {
                binding.txtInicialPlato.setVisibility(android.view.View.GONE);
                binding.imgPlatoDestacado.setVisibility(android.view.View.VISIBLE);

                Glide.with(binding.getRoot().getContext())
                        .load(url)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .into(binding.imgPlatoDestacado);
            } else {
                binding.imgPlatoDestacado.setImageDrawable(null);
                binding.imgPlatoDestacado.setVisibility(android.view.View.GONE);
                binding.txtInicialPlato.setVisibility(android.view.View.VISIBLE);
                String nombre = plato.getNombre() != null ? plato.getNombre() : "";
                String inicial = nombre.isEmpty() ? "🍽️" : nombre.substring(0, 1).toUpperCase(Locale.ROOT);
                binding.txtInicialPlato.setText(inicial);
            }
        }

        private String makeAbsoluteUrl(String url) {
            if (url == null || url.trim().isEmpty()) return null;
            String trimmed = url.trim();
            if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                return trimmed;
            }
            // BuildConfig.BASE_URL suele terminar en /api/ → quitamos /api para apuntar a wwwroot
            String base = BuildConfig.BASE_URL;
            if (base.endsWith("/")) base = base.substring(0, base.length() - 1);
            if (base.endsWith("/api")) base = base.substring(0, base.length() - 4);
            if (base.endsWith("/api/")) base = base.substring(0, base.length() - 5);
            if (!trimmed.startsWith("/")) {
                trimmed = "/" + trimmed;
            }
            return base + trimmed;
        }
    }
}
