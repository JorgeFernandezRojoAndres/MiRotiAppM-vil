package com.jorge.mirotimobile.ui.nav;
import com.jorge.mirotimobile.R;
import androidx.navigation.Navigation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.jorge.mirotimobile.databinding.FragmentMenuBinding;

public class MenuFragment extends Fragment {

    private FragmentMenuBinding binding;
    private MenuViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMenuBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(MenuViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 🔹 Observador LiveData del ViewModel
        viewModel.opcionSeleccionada.observe(getViewLifecycleOwner(), opcion -> {
            if (opcion == null) return;

            switch (opcion) {
                case "platos":
                    Navigation.findNavController(view).navigate(R.id.platosFragment);
                    break;
                case "pedidos":
                    Navigation.findNavController(view).navigate(R.id.pedidosFragment);
                    break;
                case "perfil":
                    Navigation.findNavController(view).navigate(R.id.perfilFragment);
                    break;
            }
        });

        // 🔹 Acciones del menú (sin lógica directa)
        binding.btnPlatos.setOnClickListener(v -> viewModel.seleccionarOpcion("platos"));
        binding.btnPedidos.setOnClickListener(v -> viewModel.seleccionarOpcion("pedidos"));
        binding.btnPerfil.setOnClickListener(v -> viewModel.seleccionarOpcion("perfil"));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
