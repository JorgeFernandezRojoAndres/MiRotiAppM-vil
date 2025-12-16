package com.jorge.mirotimobile.ui.cliente.perfil;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.jorge.mirotimobile.R;
import com.jorge.mirotimobile.databinding.FragmentPerfilBinding;
import com.jorge.mirotimobile.localdata.SessionManager;
import com.jorge.mirotimobile.ui.cliente.perfil.PerfilViewModel;
import com.jorge.mirotimobile.ui.login.LoginActivity;

/**
 * Fragment que muestra los datos del perfil del usuario autenticado.
 * Toda la lógica de red vive en {@link PerfilViewModel}.
 */
public class PerfilFragment extends Fragment {

    private FragmentPerfilBinding binding;
    private PerfilViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPerfilBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(PerfilViewModel.class);

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(0, top, 0, 0);
            return insets;
        });

        viewModel.getNombreLiveData().observe(getViewLifecycleOwner(),
                nombre -> binding.txtNombre.setText(nombre != null ? nombre : ""));
        viewModel.getEmailLiveData().observe(getViewLifecycleOwner(),
                email -> binding.txtEmail.setText(email != null ? email : ""));
        viewModel.getDireccionLiveData().observe(getViewLifecycleOwner(),
                direccion -> binding.txtDireccion.setText(direccion != null ? direccion : ""));
        viewModel.getTelefonoLiveData().observe(getViewLifecycleOwner(),
                telefono -> binding.txtTelefono.setText(telefono != null ? telefono : ""));
        viewModel.getRolLiveData().observe(getViewLifecycleOwner(),
                rol -> binding.txtRol.setText(rol != null ? rol : ""));

        viewModel.getLoadingLiveData().observe(getViewLifecycleOwner(), loading ->
                binding.progressPerfil.setVisibility(Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE));

        viewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            // opcional: podríamos mostrar un mensaje si la UI lo requiere
        });

        View.OnClickListener irHistorial = v ->
                Navigation.findNavController(v)
                        .navigate(R.id.historialPedidosFragment);
        binding.lytHistorialPedidos.setOnClickListener(irHistorial);
        binding.btnHistorial.setOnClickListener(irHistorial);

        viewModel.cargarPerfil();
        binding.btnCerrarSesion.setOnClickListener(v -> {
            SessionManager session = new SessionManager(requireContext());
            session.logout();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
        binding.imgAvatar.setImageResource(R.drawable.avatar_generico);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
