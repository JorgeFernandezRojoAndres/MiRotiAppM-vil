package com.jorge.mirotimobile.ui.platos;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.jorge.mirotimobile.R;
import com.jorge.mirotimobile.databinding.FragmentMenuBinding;
import com.jorge.mirotimobile.model.DetallePedidoInfoDTO;
import com.jorge.mirotimobile.model.PedidoDTO;
import com.jorge.mirotimobile.model.Plato;
import com.jorge.mirotimobile.ui.pedidos.PedidosViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Fragmento que muestra el listado del menú real (categorías, platos y botón agregar).
 */
public class MenuFragment extends Fragment {

    private FragmentMenuBinding binding;
    private PlatosViewModel vm;
    private PedidosViewModel pedidosViewModel;
    private PlatosAdapter adapter;
    private SharedPreferences carritoPrefs;
    private static final String PREF_BADGE_COUNT = "badge_count";
    private BadgeDrawable carritoBadge;
    private View carritoMenuItemView;
    private int prevBadgeCount;
    private boolean restoringBadge;
    private final List<Plato> allPlatos = new ArrayList<>();
    private String currentFilter = "todos";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMenuBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_nav);
        carritoPrefs = requireActivity().getSharedPreferences("carrito_prefs", Context.MODE_PRIVATE);
        pedidosViewModel = new ViewModelProvider(requireActivity()).get(PedidosViewModel.class);
        carritoMenuItemView = bottomNav.findViewById(R.id.pedidosFragment);
        adapter = new PlatosAdapter(plato -> {
            if (pedidosViewModel != null) {
                pedidosViewModel.agregarPlatoAlDetalle(plato);
            }
        });
        binding.recyclerMenu.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerMenu.setAdapter(adapter);

        setupFilterListeners();

        vm = new ViewModelProvider(requireActivity()).get(PlatosViewModel.class);
        vm.getPlatos().observe(getViewLifecycleOwner(), platos -> {
            allPlatos.clear();
            if (platos != null) {
                allPlatos.addAll(platos);
            }
            applyFilter();
        });

        restoringBadge = true;
        restoreBadgeFromPrefs(bottomNav);
        restoringBadge = false;
        if (carritoPrefs != null) {
            prevBadgeCount = carritoPrefs.getInt(PREF_BADGE_COUNT, 0);
        }
        // FIX: rely on the default BottomNavigationView navigation instead of overriding the listener.
        pedidosViewModel.getPedidos().observe(getViewLifecycleOwner(), pedidos -> {
            updateCartBadge(bottomNav, pedidos);
        });

        vm.getLoading().observe(getViewLifecycleOwner(),
                visible -> binding.menuProgress.setVisibility(visible ? View.VISIBLE : View.GONE));

        vm.getMensajeError().observe(getViewLifecycleOwner(), error -> {
            binding.menuError.setText(error);
            binding.menuError.setVisibility(error != null ? View.VISIBLE : View.GONE);
        });

        vm.cargarPlatos();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("FRAGMENT", "MenuFragment onResume");
    }

    private void updateCartBadge(BottomNavigationView bottomNav, List<PedidoDTO> pedidos) {
        if (bottomNav == null) return;
        int cantidad = 0;
        if (pedidos != null && !pedidos.isEmpty()) {
            PedidoDTO pedido = pedidos.get(0);
            if (pedido.getDetalles() != null) {
                for (DetallePedidoInfoDTO detalle : pedido.getDetalles()) {
                    cantidad += detalle.getCantidad();
                }
            }
        }

        int anterior = prevBadgeCount;
        setBadgeCount(bottomNav, cantidad);
        boolean shouldIncrement = !restoringBadge && cantidad > anterior;
        if (shouldIncrement) {
            triggerHapticFeedback();
        }
        maybeAnimateBadge(cantidad, anterior);
        prevBadgeCount = cantidad;
        if (carritoPrefs != null) {
            carritoPrefs.edit().putInt(PREF_BADGE_COUNT, cantidad).apply();
        }
    }

    private void restoreBadgeFromPrefs(BottomNavigationView bottomNav) {
        if (bottomNav == null || carritoPrefs == null) return;
        int guardado = carritoPrefs.getInt(PREF_BADGE_COUNT, 0);
        setBadgeCount(bottomNav, guardado);
    }

    private void setBadgeCount(BottomNavigationView bottomNav, int cantidad) {
        if (bottomNav == null) return;
        if (cantidad > 0) {
            carritoBadge = bottomNav.getOrCreateBadge(R.id.pedidosFragment);
            carritoBadge.setNumber(cantidad);
            carritoBadge.setVisible(true);
        } else {
            if (carritoBadge == null) {
                carritoBadge = bottomNav.getBadge(R.id.pedidosFragment);
            }
            if (carritoBadge != null) {
                carritoBadge.clearNumber();
                carritoBadge.setVisible(false);
            }
        }
    }

    private void maybeAnimateBadge(int newCount, int oldCount) {
        if (restoringBadge || carritoMenuItemView == null) return;
        if (newCount <= oldCount) return;
        carritoMenuItemView.animate()
                .scaleX(1.15f)
                .scaleY(1.15f)
                .setDuration(100)
                .withEndAction(() -> carritoMenuItemView.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start())
                .start();
    }

    private void triggerHapticFeedback() {
        if (carritoMenuItemView != null) {
            carritoMenuItemView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
        }
    }

    private void setupFilterListeners() {
        binding.chipTodos.setOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                currentFilter = "todos";
                applyFilter();
            }
        });
        binding.chipPollos.setOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                currentFilter = "pollos";
                applyFilter();
            }
        });
        binding.chipEnsaladas.setOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                currentFilter = "ensaladas";
                applyFilter();
            }
        });
        binding.chipPostres.setOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                currentFilter = "postres";
                applyFilter();
            }
        });
    }

    private void applyFilter() {
        if (allPlatos.isEmpty()) {
            adapter.actualizarLista(Collections.emptyList());
            return;
        }

        if ("todos".equals(currentFilter)) {
            adapter.actualizarLista(new ArrayList<>(allPlatos));
            return;
        }

        List<Plato> filtered = new ArrayList<>();
        String query = currentFilter.toLowerCase(Locale.ROOT);
        for (Plato plato : allPlatos) {
            String text = (plato.getNombre() + " " + plato.getDescripcion()).toLowerCase(Locale.ROOT);
            if (text.contains(query)) {
                filtered.add(plato);
            }
        }

        adapter.actualizarLista(filtered);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
