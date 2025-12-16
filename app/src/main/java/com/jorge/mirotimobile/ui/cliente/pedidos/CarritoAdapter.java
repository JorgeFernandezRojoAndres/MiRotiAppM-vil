package com.jorge.mirotimobile.ui.cliente.pedidos;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
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
import com.jorge.mirotimobile.databinding.ItemDetalleCarritoBinding;
import com.jorge.mirotimobile.model.DetallePedidoInfoDTO;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CarritoAdapter extends RecyclerView.Adapter<CarritoAdapter.CarritoViewHolder> {

    public interface OnCantidadChangeListener {
        void onCantidadChanged();
    }

    private final List<DetallePedidoInfoDTO> items = new ArrayList<>();
    private final OnCantidadChangeListener listener;

    public CarritoAdapter(OnCantidadChangeListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CarritoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDetalleCarritoBinding binding = ItemDetalleCarritoBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new CarritoViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CarritoViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(List<DetallePedidoInfoDTO> nuevos) {
        items.clear();
        if (nuevos != null) {
            items.addAll(nuevos);
        }
        notifyDataSetChanged();
        notifyCantidadChange();
    }

    public double getSubtotal() {
        double total = 0;
        for (DetallePedidoInfoDTO item : items) {
            total += item.getSubtotal();
        }
        return total;
    }

    private void notifyCantidadChange() {
        if (listener != null) listener.onCantidadChanged();
    }

    class CarritoViewHolder extends RecyclerView.ViewHolder {
        private final ItemDetalleCarritoBinding binding;
        private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));

        CarritoViewHolder(@NonNull ItemDetalleCarritoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(DetallePedidoInfoDTO item) {
            binding.txtDetalleNombre.setText(item.getPlato());
            binding.txtDetalleCategoria.setText("Sin categoría");
            binding.txtCantidad.setText(String.valueOf(item.getCantidad()));
            double unitPrice = item.getCantidad() == 0 ? 0 : item.getSubtotal() / item.getCantidad();
            binding.txtDetallePrecio.setText(currencyFormat.format(unitPrice));

            binding.btnSumar.setOnClickListener(v -> {
                item.setCantidad(item.getCantidad() + 1);
                recalcularSubtotal(item, unitPrice);
                binding.txtCantidad.setText(String.valueOf(item.getCantidad()));
                notifyCantidadChange();
            });

        binding.btnRestar.setOnClickListener(v -> {
            if (item.getCantidad() > 1) {
                item.setCantidad(item.getCantidad() - 1);
                recalcularSubtotal(item, unitPrice);
                binding.txtCantidad.setText(String.valueOf(item.getCantidad()));
                notifyCantidadChange();
            }
        });

        binding.imgDetallePlato.setBackground(null);
        String url = makeAbsoluteUrl(item.getImagenUrl());
        String safeUrl = encodeUrl(url);
        if (safeUrl != null && !safeUrl.trim().isEmpty()) {
            Glide.with(binding.imgDetallePlato.getContext())
                    .load(safeUrl)
                    .placeholder(R.drawable.logo_miroti)
                    .error(R.drawable.logo_miroti)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .addListener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                    Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model,
                                                       Target<Drawable> target, DataSource dataSource,
                                                       boolean isFirstResource) {
                            return false;
                        }
                    })
                    .into(binding.imgDetallePlato);
        } else {
            Glide.with(binding.imgDetallePlato.getContext())
                    .load(R.drawable.logo_miroti)
                    .centerCrop()
                    .into(binding.imgDetallePlato);
        }
        }

        private void recalcularSubtotal(DetallePedidoInfoDTO item, double unitPrice) {
            item.setSubtotal(unitPrice * item.getCantidad());
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
