package com.jorge.mirotimobile.ui.cadete.entregas;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.jorge.mirotimobile.R;
import com.jorge.mirotimobile.databinding.ItemEntregaBinding;
import com.jorge.mirotimobile.model.PedidoDTO;

public class EntregasAdapter extends ListAdapter<PedidoDTO, EntregasAdapter.EntregaViewHolder> {

    public interface OnPedidoClickListener {
        void onPedidoClick(int pedidoId);
    }

    private final OnPedidoClickListener listener;

    public EntregasAdapter(OnPedidoClickListener listener) {
        super(new DiffUtil.ItemCallback<PedidoDTO>() {
            @Override
            public boolean areItemsTheSame(@NonNull PedidoDTO oldItem, @NonNull PedidoDTO newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull PedidoDTO oldItem, @NonNull PedidoDTO newItem) {
                return oldItem.getEstado().equals(newItem.getEstado());
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public EntregaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new EntregaViewHolder(
                ItemEntregaBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false),
                listener
        );
    }

    @Override
    public void onBindViewHolder(@NonNull EntregaViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class EntregaViewHolder extends RecyclerView.ViewHolder {
        private final ItemEntregaBinding binding;
        private final OnPedidoClickListener listener;

        EntregaViewHolder(ItemEntregaBinding binding, OnPedidoClickListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
        }

        void bind(PedidoDTO entrega) {
            Context context = itemView.getContext();
            binding.txtEntregaId.setText("Pedido #" + entrega.getId());
            binding.txtEntregaDireccion.setText(entrega.getDireccion());
            binding.txtEntregaCliente.setText(entrega.getCliente());
            binding.txtEntregaEstado.setText(entrega.getEstado());

            int estadoColor = colorForEstado(context, entrega.getEstado());
            binding.cardEntrega.setCardBackgroundColor(estadoColor);
            binding.txtEntregaEstado.setTextColor(estadoColor);
            binding.txtEntregaId.setTextColor(ContextCompat.getColor(context, R.color.cadete_text_primary));
            binding.txtEntregaDireccion.setTextColor(ContextCompat.getColor(context, R.color.cadete_text_secondary));

            binding.btnTomarPedido.setOnClickListener(v -> {
                if (listener != null) listener.onPedidoClick(entrega.getId());
            });
        }

        private int colorForEstado(Context context, String estado) {
            if (estado == null) {
                return ContextCompat.getColor(context, R.color.cadete_state_espera);
            }
            switch (estado.trim().toUpperCase()) {
                case "EN_CAMINO":
                    return ContextCompat.getColor(context, R.color.cadete_state_activo);
                case "ENTREGADO":
                    return ContextCompat.getColor(context, R.color.cadete_state_entregado);
                default:
                    return ContextCompat.getColor(context, R.color.cadete_state_espera);
            }
        }
    }
}
