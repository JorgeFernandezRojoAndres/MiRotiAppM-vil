package com.jorge.mirotimobile.ui.nav;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.jorge.mirotimobile.R;
import com.jorge.mirotimobile.databinding.FragmentBienvenidaBinding;
import com.jorge.mirotimobile.localdata.SessionManager;
import com.jorge.mirotimobile.ui.cliente.pedidos.PedidosRecientesAdapter;
import com.jorge.mirotimobile.ui.cliente.pedidos.PedidosViewModel;
import com.jorge.mirotimobile.ui.platos.PlatosDestacadosAdapter;
import com.jorge.mirotimobile.ui.platos.PlatosViewModel;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Piso de bienvenida tras login del cliente.
 */
public class BienvenidaFragment extends Fragment {

    private FragmentBienvenidaBinding binding;
    private PlatosViewModel vm;
    private PedidosViewModel pedidosVm;
    private PedidosRecientesAdapter pedidosAdapter;
    private PlatosDestacadosAdapter destacadosAdapter;
    private final Handler notificationHandler = new Handler(Looper.getMainLooper());
    private final List<String> notificationMessages = Arrays.asList(
            "Última actualización: tu pedido #1234 está saliendo del restaurante.",
            "Tienes un pedido en camino con el cadete Juan.",
            "Con tu compra de hoy tienes un 5% de reintegro en saldo a favor dentro de la app.",
            "Recuerda que puedes seguir tu pedido en la pestaña de seguimiento en vivo."
    );
    private static final long NOTIFICATION_ROTATION_INTERVAL_MS = 6000;
    private final Runnable notificationUpdater = new Runnable() {
        @Override
        public void run() {
            if (binding == null || notificationMessages.isEmpty()) {
                return;
            }
            updateNotificationText(notificationRotationIndex);
            notificationHandler.postDelayed(this, NOTIFICATION_ROTATION_INTERVAL_MS);
        }
    };
    private int notificationRotationIndex;
    private int lastNotificationIndex;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentBienvenidaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        configurarRecyclerViews();
        configurarViewModel();
        configurarPedidosViewModel();
        observarViewModel();
        configurarBotonAlertas();
        setInitialNotificationMessage();
        
        vm.cargarPlatos();
    }

    private void configurarRecyclerViews() {
        pedidosAdapter = new PedidosRecientesAdapter();
        destacadosAdapter = new PlatosDestacadosAdapter();

        binding.recyclerDestacados.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerDestacados.setAdapter(destacadosAdapter);

        binding.recyclerPedidosRecientes.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerPedidosRecientes.setAdapter(pedidosAdapter);
        binding.recyclerPedidosRecientes.setNestedScrollingEnabled(false);
        binding.recyclerPedidosRecientes.setHasFixedSize(true);
        
    }
    
    private void configurarViewModel() {
        vm = new ViewModelProvider(requireActivity()).get(PlatosViewModel.class);
        
        SessionManager session = new SessionManager(requireContext());
        String email = session.getUserEmail();
        vm.configurarSaludo(email);
    }
    
    private void configurarPedidosViewModel() {
        pedidosVm = new ViewModelProvider(requireActivity()).get(PedidosViewModel.class);
        pedidosVm.getPedidosCompletadosCount().observe(getViewLifecycleOwner(), count -> {
            int value = (count == null) ? 0 : count;
            binding.metricTotalValue.setText(String.valueOf(value));
        });
        pedidosVm.getPedidosRecientes().observe(getViewLifecycleOwner(), pedidosAdapter::actualizarLista);
        pedidosVm.getSaldoFavor().observe(getViewLifecycleOwner(), saldo ->
                binding.metricSaldoValue.setText(saldo));
        if (pedidosVm.getPedidos().getValue() == null) {
            pedidosVm.cargarMisPedidos();
        }
    }
    
    private void observarViewModel() {
        vm.getPlatos().observe(getViewLifecycleOwner(), platos -> {
            List<com.jorge.mirotimobile.model.Plato> destacados = (platos == null) ? Collections.emptyList() : platos;
            destacadosAdapter.actualizarLista(destacados);
            vm.actualizarMetricas(platos);
        });

        vm.getProgressVisibility().observe(getViewLifecycleOwner(), binding.progressBar::setVisibility);
        vm.getErrorVisibility().observe(getViewLifecycleOwner(), binding.txtError::setVisibility);
        vm.getMensajeError().observe(getViewLifecycleOwner(), binding.txtError::setText);
        
        vm.getGreetingTitle().observe(getViewLifecycleOwner(), binding.txtGreetingTitle::setText);
    }

    private void configurarBotonAlertas() {
        binding.btnAlerts.setOnClickListener(view -> mostrarDialogoHistorialNotificaciones());
    }

    private void mostrarDialogoHistorialNotificaciones() {
        if (notificationMessages.isEmpty()) {
            Toast.makeText(requireContext(), "Todavía no hay notificaciones nuevas", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] items = notificationMessages.toArray(new String[0]);
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.notification_history_title)
                .setItems(items, null)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void setInitialNotificationMessage() {
        if (binding == null || notificationMessages.isEmpty()) {
            return;
        }
        lastNotificationIndex = 0;
        notificationRotationIndex = 1 % notificationMessages.size();
        binding.txtNotificationMessage.setText(notificationMessages.get(0));
    }

    private void updateNotificationText(int index) {
        if (binding == null || notificationMessages.isEmpty()) {
            return;
        }
        binding.txtNotificationMessage.setText(notificationMessages.get(index));
        lastNotificationIndex = index;
        notificationRotationIndex = (index + 1) % notificationMessages.size();
    }

    private void startNotificationRotation() {
        if (notificationMessages.isEmpty() || binding == null) {
            return;
        }
        notificationHandler.removeCallbacks(notificationUpdater);
        updateNotificationText(0);
        notificationHandler.postDelayed(notificationUpdater, NOTIFICATION_ROTATION_INTERVAL_MS);
    }

    private void stopNotificationRotation() {
        notificationHandler.removeCallbacks(notificationUpdater);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        startNotificationRotation();
    }

    @Override
    public void onPause() {
        stopNotificationRotation();
        super.onPause();
    }
}
