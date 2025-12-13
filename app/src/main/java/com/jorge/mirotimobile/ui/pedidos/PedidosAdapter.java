package com.jorge.mirotimobile.ui.pedidos;

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
        holder.bind(pedidos.get(position));
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
            binding.txtNumeroPedido.setText("Pedido #" + pedido.getId());
            binding.txtFechaPedido.setText(formatearFecha(pedido.getFechaHora()));
            binding.txtTotalPedido.setText(currencyFormat.format(pedido.getTotal()));
            EstadoPedido estado = EstadoPedido.fromString(pedido.getEstado());
            binding.chipEstadoPedido.setText(estado.getLabel());
            binding.chipEstadoPedido.setChipBackgroundColorResource(estado.getColorRes());

            binding.cardPedido.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putInt("pedidoId", pedido.getId());
                Navigation.findNavController(v)
                        .navigate(R.id.detallePedidoFragment, bundle);
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
