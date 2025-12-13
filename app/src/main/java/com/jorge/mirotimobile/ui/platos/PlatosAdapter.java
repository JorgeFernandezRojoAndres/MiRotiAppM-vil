package com.jorge.mirotimobile.ui.platos;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.jorge.mirotimobile.BuildConfig;
import com.jorge.mirotimobile.R;
import com.jorge.mirotimobile.databinding.ItemPlatoBinding;
import com.jorge.mirotimobile.model.Plato;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 🍽️ PlatosAdapter — Muestra la lista de platos disponibles.
 * Utiliza ViewBinding y sigue el patrón MVVM (sin lógica de negocio en la vista).
 */
public class PlatosAdapter extends RecyclerView.Adapter<PlatosAdapter.PlatosViewHolder> {

    public interface OnAgregarPlatoListener {
        void onAgregar(Plato plato);
    }

    private final List<Plato> listaPlatos = new ArrayList<>();
    private final OnAgregarPlatoListener listener;

    public PlatosAdapter(@Nullable OnAgregarPlatoListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlatosViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPlatoBinding binding = ItemPlatoBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new PlatosViewHolder(binding, listener);
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

        private final OnAgregarPlatoListener listener;

        public PlatosViewHolder(@NonNull ItemPlatoBinding binding,
                                @Nullable OnAgregarPlatoListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
        }

        // 🔹 Vincula los datos del plato con la vista
        public void bind(Plato plato) {
            binding.txtNombrePlato.setText(plato.getNombre());
            binding.txtDescripcionPlato.setText(plato.getDescripcion());

            NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));
            String precio = format.format(plato.getPrecioVenta());
            binding.txtPrecioPlato.setText(precio);

            String url = makeAbsoluteUrl(plato.getImagenUrl());
            String safeUrl = encodeUrl(url);
            Log.d("PlatosAdapter", "Carga imagen: " + safeUrl);
            binding.imgPlato.setBackground(null);
            if (safeUrl != null && !safeUrl.trim().isEmpty()) {
                Glide.with(binding.imgPlato.getContext())
                        .load(safeUrl)
                        .placeholder(R.drawable.logo_miroti)
                        .error(R.drawable.logo_miroti)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                        Target<Drawable> target, boolean isFirstResource) {
                                Log.w("PlatosAdapter", "Error cargando imagen: " + safeUrl, e);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model,
                                                           Target<Drawable> target, DataSource dataSource,
                                                           boolean isFirstResource) {
                                Log.d("PlatosAdapter", "Imagen cargada correctamente: " + safeUrl);
                                return false;
                            }
                        })
                        .into(binding.imgPlato);
            } else {
                Glide.with(binding.imgPlato.getContext())
                        .load(R.drawable.logo_miroti)
                        .into(binding.imgPlato);
            }

            binding.btnAgregarPlato.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAgregar(plato);
                }
            });
        }

        private String makeAbsoluteUrl(String url) {
            if (url == null || url.trim().isEmpty()) return null;
            String trimmed = url.trim();
            if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                return trimmed;
            }
            String base = BuildConfig.BASE_URL;
            if (base.endsWith("/")) base = base.substring(0, base.length() - 1);
            if (base.endsWith("/api")) base = base.substring(0, base.length() - 4);
            if (base.endsWith("/api/")) base = base.substring(0, base.length() - 5);
            if (!trimmed.startsWith("/")) {
                trimmed = "/" + trimmed;
            }
            return base + trimmed;
        }

        private String encodeUrl(String rawUrl) {
            if (rawUrl == null || rawUrl.trim().isEmpty()) return rawUrl;
            try {
                URL parsed = new URL(rawUrl);
                URI uri = new URI(
                        parsed.getProtocol(),
                        parsed.getUserInfo(),
                        parsed.getHost(),
                        parsed.getPort(),
                        parsed.getPath(),
                        parsed.getQuery(),
                        parsed.getRef());
                return uri.toASCIIString();
            } catch (MalformedURLException | URISyntaxException e) {
                return rawUrl.replace(" ", "%20");
            }
        }
    }
}
