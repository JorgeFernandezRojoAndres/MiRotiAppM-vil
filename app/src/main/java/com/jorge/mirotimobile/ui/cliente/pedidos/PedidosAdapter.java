package com.jorge.mirotimobile.ui.cliente.pedidos;

import android.os.Bundle;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.jorge.mirotimobile.R;
import com.jorge.mirotimobile.databinding.ItemPedidoBinding;
import com.jorge.mirotimobile.model.EstadoPedido;
import com.jorge.mirotimobile.model.PedidoDTO;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.util.Log;

public class PedidosAdapter extends RecyclerView.Adapter<PedidosAdapter.PedidoViewHolder> {

    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final List<PedidoDTO> pedidos = new ArrayList<>();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));

    @NonNull
    @Override
    public PedidoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPedidoBinding binding = ItemPedidoBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new PedidoViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PedidoViewHolder holder, int position) {
        if (position < 0 || position >= pedidos.size()) {
            Log.w("MAIN_FLOW","onBindViewHolder invalid position: " + position);
            return;
        }
        PedidoDTO pedido = pedidos.get(position);
        try {
            holder.bind(pedido);
        } catch (Exception e) {
            Log.w("MAIN_FLOW","Error binding pedido at position " + position, e);
        }
    }

    @Override
    public int getItemCount() {
        return pedidos.size();
    }

    public void setPedidos(List<PedidoDTO> nuevosPedidos) {
        pedidos.clear();
        if (nuevosPedidos != null) {
            pedidos.addAll(nuevosPedidos);
        }
        notifyDataSetChanged();
    }

    class PedidoViewHolder extends RecyclerView.ViewHolder {
        private final ItemPedidoBinding binding;

        PedidoViewHolder(@NonNull ItemPedidoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(PedidoDTO pedido) {
            if (pedido == null) {
                binding.txtNumeroPedido.setText("Pedido #");
                binding.txtFechaPedido.setText("");
                binding.txtTotalPedido.setText(currencyFormat.format(0));
                binding.chipEstadoPedido.setText("");
                return;
            }

            try {
                binding.txtNumeroPedido.setText("Pedido #" + pedido.getId());
            } catch (Exception e) {
                Log.w("MAIN_FLOW","Error setting pedido id", e);
                binding.txtNumeroPedido.setText("Pedido #");
            }

            try {
                binding.txtFechaPedido.setText(formatearFecha(pedido.getFechaHora()));
            } catch (Exception e) {
                Log.w("MAIN_FLOW","Error formatting fecha", e);
                binding.txtFechaPedido.setText("");
            }

            try {
                binding.txtTotalPedido.setText(currencyFormat.format(pedido.getTotal()));
            } catch (Exception e) {
                Log.w("MAIN_FLOW","Error formatting total", e);
                binding.txtTotalPedido.setText(currencyFormat.format(0));
            }

            try {
                EstadoPedido estado = EstadoPedido.fromString(pedido.getEstado());
                if (estado != null) {
                    binding.chipEstadoPedido.setText(estado.getLabel());
                    binding.chipEstadoPedido.setChipBackgroundColorResource(estado.getColorRes());
                } else {
                    binding.chipEstadoPedido.setText("");
                }
            } catch (Exception e) {
                Log.w("MAIN_FLOW","Error setting estado", e);
                binding.chipEstadoPedido.setText("");
            }

            binding.cardPedido.setOnClickListener(v -> {
                if (v == null) return;
                try {
                    Bundle bundle = new Bundle();
                    bundle.putInt("pedidoId", pedido.getId());
                    Navigation.findNavController(v).navigate(R.id.detallePedidoFragment, bundle);
                } catch (Exception e) {
                    Log.w("MAIN_FLOW","Navigation to detallePedidoFragment failed", e);
                }
            });
        }

        private String formatearFecha(String fechaIso) {
            if (fechaIso == null || fechaIso.isEmpty()) return "";
            try {
                LocalDateTime parsed = LocalDateTime.parse(fechaIso, DateTimeFormatter.ISO_DATE_TIME);
                return DISPLAY_FORMATTER.format(parsed);
            } catch (DateTimeParseException ignored) {
                return fechaIso;
            }
        }

    }
}
