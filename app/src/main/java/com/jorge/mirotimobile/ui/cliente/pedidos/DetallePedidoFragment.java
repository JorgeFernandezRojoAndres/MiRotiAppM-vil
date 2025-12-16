package com.jorge.mirotimobile.ui.cliente.pedidos;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.jorge.mirotimobile.databinding.FragmentDetallePedidoBinding;
import com.jorge.mirotimobile.R;
import com.jorge.mirotimobile.model.EstadoPedido;
import com.jorge.mirotimobile.model.PedidoDTO;
import com.google.android.material.snackbar.Snackbar;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class DetallePedidoFragment extends Fragment {

    private FragmentDetallePedidoBinding binding;
    private CarritoAdapter adapter;
    private PedidosViewModel vm;
    private boolean confirmandoPedido;
    private String ultimoError;
    private String textoConfirmarOriginal;
    private PedidoDTO pedidoActual;
    private boolean modoCadete;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDetallePedidoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            modoCadete = args.getBoolean("modoCadete", false);
        }

        adapter = new CarritoAdapter(this::actualizarResumen);
        binding.recyclerCarrito.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerCarrito.setAdapter(adapter);

        vm = new ViewModelProvider(requireActivity()).get(PedidosViewModel.class);
        vm.getPedidos().observe(getViewLifecycleOwner(), pedidos -> {
            PedidoDTO primer = (pedidos == null || pedidos.isEmpty()) ? null : pedidos.get(0);
            mostrarPedido(primer);
        });

        textoConfirmarOriginal = binding.btnConfirmar.getText().toString();
        vm.getLoading().observe(getViewLifecycleOwner(), visible -> {
            binding.menuProgress.setVisibility(visible ? View.VISIBLE : View.GONE);
            updateConfirmButtonState(visible);
            if (confirmandoPedido
                    && pedidoActual != null
                    && pedidoActual.getId() == 0
                    && !visible
                    && (ultimoError == null || ultimoError.isEmpty())) {
                confirmandoPedido = false;
                updateConfirmButtonState(false);
                Snackbar.make(binding.getRoot(), "Pedido confirmado correctamente", Snackbar.LENGTH_SHORT).show();
            }
        });

        vm.getMensajeError().observe(getViewLifecycleOwner(), error -> {
            ultimoError = error;
            if (error != null) {
                confirmandoPedido = false;
                updateConfirmButtonState(false);
                Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_LONG).show();
            }
        });

        binding.toolbarDetalle.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
        binding.toolbarDetalle.setOnMenuItemClickListener(item -> {
            Toast.makeText(requireContext(), "Editar pedido", Toast.LENGTH_SHORT).show();
            return true;
        });

        actualizarBotonConfirmar(null);
        binding.btnCancelar.setOnClickListener(v -> {
                vm.cancelarPedidoLocal();
                Toast.makeText(requireContext(), "Pedido cancelado", Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(this).popBackStack();
        });
        binding.btnSeguirComprando.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.menuFragment));

        vm.getNavegarASeguimiento().observe(getViewLifecycleOwner(), navegar -> {
            if (Boolean.TRUE.equals(navegar)) {
                Navigation.findNavController(requireView()).navigate(R.id.trackingFragment);
                vm.clearNavegarASeguimiento();
            }
        });

        applyModoCadete();

        // FIX: skip a refresh here so the pedido en armado no se limpia al volver.
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("FRAGMENT", "DetallePedidoFragment onResume");
    }

    private void mostrarPedido(PedidoDTO pedido) {
        pedidoActual = pedido;
        binding.txtPedidoNumero.setText(pedido != null ? "Pedido #" + pedido.getId() : "Pedido #0000");

        if (pedido != null) {
            String fechaFormateada = formatFecha(pedido.getFechaHora());
            binding.txtPedidoFecha.setText(fechaFormateada != null ? fechaFormateada : pedido.getFechaHora());
            EstadoPedido estado = EstadoPedido.fromString(pedido.getEstado());
            binding.chipEstado.setText(estado.getLabel());
            binding.chipEstado.setChipBackgroundColorResource(estado.getColorRes());
            adapter.setItems(pedido.getDetalles());
        } else {
            binding.txtPedidoFecha.setText("-");
            binding.chipEstado.setText("En armado");
            binding.chipEstado.setChipBackgroundColorResource(R.color.nav_icon_inactive);
            adapter.setItems(Collections.emptyList());
        }
        actualizarResumen();
        actualizarBotonConfirmar(pedido);
    }

    private void actualizarResumen() {
        NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));
        double subtotal = adapter.getSubtotal();
        double envio = subtotal > 0 ? 200 : 0;
        double total = subtotal + envio;
        binding.txtSubtotal.setText(currency.format(subtotal));
        binding.txtEnvio.setText(currency.format(envio));
        binding.txtTotal.setText(currency.format(total));
    }

    private String formatFecha(String isoDateTime) {
        if (isoDateTime == null || isoDateTime.isEmpty()) return null;
        try {
            LocalDateTime parsed = LocalDateTime.parse(isoDateTime, DateTimeFormatter.ISO_DATE_TIME);
            return parsed.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private void actualizarBotonConfirmar(PedidoDTO pedido) {
        pedidoActual = pedido;
        if (pedido != null && pedido.getId() > 0) {
            EstadoPedido estado = EstadoPedido.fromString(pedido.getEstado());
            if (estado == EstadoPedido.PENDIENTE) {
                binding.btnConfirmar.setText("Pagar (contra entrega)");
                binding.btnConfirmar.setOnClickListener(v -> {
                    Snackbar.make(binding.getRoot(), "El pago se realiza al recibir el pedido", Snackbar.LENGTH_LONG).show();
                    Navigation.findNavController(requireView()).navigate(R.id.trackingFragment);
                });
                confirmandoPedido = false;
                updateConfirmButtonState(false);
                return;
            }
            binding.btnConfirmar.setText("SEGUIR PEDIDO");
            binding.btnConfirmar.setOnClickListener(v -> vm.confirmarPedido());
            confirmandoPedido = false;
            updateConfirmButtonState(false);
        } else {
            binding.btnConfirmar.setText("Confirmar Pedido");
            binding.btnConfirmar.setOnClickListener(v -> {
                confirmandoPedido = true;
                ultimoError = null;
                updateConfirmButtonState(true);
                vm.confirmarPedido();
            });
        }
    }

    private void updateConfirmButtonState(boolean loading) {
        if (confirmandoPedido) {
            binding.btnConfirmar.setEnabled(!loading);
            binding.btnConfirmar.setText(loading ? "Enviando..." : textoConfirmarOriginal);
        } else {
            binding.btnConfirmar.setEnabled(true);
            if (pedidoActual == null || pedidoActual.getId() == 0) {
                binding.btnConfirmar.setText(textoConfirmarOriginal);
            }
        }
    }

    private void applyModoCadete() {
        if (!modoCadete) {
            binding.btnVolverSeguimiento.setVisibility(View.GONE);
            return;
        }

        binding.btnConfirmar.setVisibility(View.GONE);
        binding.btnCancelar.setVisibility(View.GONE);
        binding.btnSeguirComprando.setVisibility(View.GONE);
        binding.edtNotas.setEnabled(false);
        binding.btnVolverSeguimiento.setVisibility(View.VISIBLE);
        binding.btnVolverSeguimiento.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.trackingCadeteFragment));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
