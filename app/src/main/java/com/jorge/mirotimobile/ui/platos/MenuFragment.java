package com.jorge.mirotimobile.ui.platos;

import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.jorge.mirotimobile.R;
import com.jorge.mirotimobile.databinding.FragmentMenuBinding;
import com.jorge.mirotimobile.ui.cliente.pedidos.PedidosViewModel;
import android.util.Log;

/**
 * Fragmento que muestra el listado del menú real (categorías, platos y botón
 * agregar).
 */
public class MenuFragment extends Fragment {

    private FragmentMenuBinding binding;
    private PlatosViewModel vm;
    private PedidosViewModel pedidosViewModel;
    private PlatosAdapter adapter;
    private BadgeDrawable carritoBadge;
    private View carritoMenuItemView;

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

        configurarRecyclerView();
        configurarViewModels();
        configurarBadge();
        observarViewModel();
        configurarListeners();

        vm.cargarPlatos();
    }

    private void configurarRecyclerView() {
        binding.recyclerMenu.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void configurarViewModels() {
        vm = new ViewModelProvider(requireActivity()).get(PlatosViewModel.class);
        pedidosViewModel = new ViewModelProvider(requireActivity()).get(PedidosViewModel.class);

        adapter = new PlatosAdapter(pedidosViewModel::agregarPlatoAlDetalle);
        binding.recyclerMenu.setAdapter(adapter);
    }

    private void configurarBadge() {
        BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_nav);
        carritoMenuItemView = bottomNav.findViewById(R.id.pedidosFragment);
        carritoBadge = bottomNav.getOrCreateBadge(R.id.pedidosFragment);
    }

    private void observarViewModel() {
        vm.getPlatosFiltered().observe(getViewLifecycleOwner(), adapter::actualizarLista);
        vm.getProgressVisibility().observe(getViewLifecycleOwner(), binding.menuProgress::setVisibility);
        vm.getErrorVisibility().observe(getViewLifecycleOwner(), binding.menuError::setVisibility);
        vm.getMensajeError().observe(getViewLifecycleOwner(), binding.menuError::setText);

        vm.getBadgeData().observe(getViewLifecycleOwner(), data -> {
            carritoBadge.setNumber(data.count);
            carritoBadge.setVisible(data.visible);
        });

        vm.getShouldAnimateBadge().observe(getViewLifecycleOwner(), shouldAnimate -> {
            // El ViewModel decide cuándo animar. Si es true, ejecutamos la acción.
            // (Usamos un helper interno para mantener la vista limpia de lógica if
            // compleja)
            ejecutarAnimacionBadge(shouldAnimate);
        });

        pedidosViewModel.getPedidos().observe(getViewLifecycleOwner(), vm::updateBadgeFromPedidos);
    }

    private void ejecutarAnimacionBadge(Boolean shouldAnimate) {
        if (!Boolean.TRUE.equals(shouldAnimate))
            return;

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
        carritoMenuItemView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
    }

    private void configurarListeners() {
        binding.chipTodos.setOnCheckedChangeListener((button, isChecked) -> vm.filtrar("todos"));
        binding.chipPollos.setOnCheckedChangeListener((button, isChecked) -> vm.filtrar("pollos"));
        binding.chipEnsaladas.setOnCheckedChangeListener((button, isChecked) -> vm.filtrar("ensaladas"));
        binding.chipPostres.setOnCheckedChangeListener((button, isChecked) -> vm.filtrar("postres"));
        binding.chipCarnes.setOnCheckedChangeListener((button, isChecked) -> vm.filtrar("carnes"));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
