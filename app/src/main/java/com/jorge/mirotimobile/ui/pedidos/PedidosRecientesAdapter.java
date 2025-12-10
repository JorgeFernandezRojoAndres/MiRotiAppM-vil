package com.jorge.mirotimobile.ui.pedidos;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.jorge.mirotimobile.R;
import com.jorge.mirotimobile.databinding.ItemPedidoRecienteBinding;
import com.jorge.mirotimobile.model.PedidoResumen;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PedidosRecientesAdapter extends RecyclerView.Adapter<PedidosRecientesAdapter.PedidoViewHolder> {

    private final List<PedidoResumen> pedidos = new ArrayList<>();

    @NonNull
    @Override
    public PedidoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPedidoRecienteBinding binding = ItemPedidoRecienteBinding.inflate(
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

    public void actualizarLista(List<PedidoResumen> nuevos) {
        pedidos.clear();
        if (nuevos != null) {
            pedidos.addAll(nuevos);
        }
        notifyDataSetChanged();
    }

    static class PedidoViewHolder extends RecyclerView.ViewHolder {
        private final ItemPedidoRecienteBinding binding;

        PedidoViewHolder(@NonNull ItemPedidoRecienteBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(PedidoResumen pedido) {
            binding.txtPedidoTitulo.setText(String.format(Locale.getDefault(), "Pedido %s", pedido.getNumero()));
            binding.txtPedidoFecha.setText("Fecha: " + pedido.getFecha());
            binding.txtPedidoEstado.setText("Estado: " + pedido.getEstado());
            int colorRes;
            switch (pedido.getEstado().toLowerCase(Locale.ROOT)) {
                case "entregado":
                    colorRes = R.color.estado_entregado;
                    break;
                case "en proceso":
                    colorRes = R.color.estado_proceso;
                    break;
                case "cancelado":
                    colorRes = R.color.estado_cancelado;
                    break;
                default:
                    colorRes = R.color.text_light;
                    break;
            }
            binding.txtPedidoEstado.setTextColor(ContextCompat.getColor(binding.getRoot().getContext(), colorRes));
        }
    }
}
