package com.jorge.mirotimobile.ui.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.jorge.mirotimobile.databinding.FragmentRegisterBinding;
import com.jorge.mirotimobile.utils.NavigationCommand;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private RegisterViewModel registerViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        registerViewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        setupObservers();
        setupListeners();
    }

    private void setupObservers() {
        registerViewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.loading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnRegister.setEnabled(!isLoading);
        });

        registerViewModel.getShowError().observe(getViewLifecycleOwner(), show -> {
            // Podríamos mostrar/ocultar elementos de error si fuera necesario
        });

        registerViewModel.getMensajeError().observe(getViewLifecycleOwner(), mensaje -> {
            if (mensaje != null && !mensaje.isEmpty()) {
                Toast.makeText(getContext(), mensaje, Toast.LENGTH_LONG).show();
            }
        });

        registerViewModel.getNavigateToMain().observe(getViewLifecycleOwner(), unused -> {
            // Navegar al MainActivity o Fragmento principal
            // Como no tengo el NavigationCommand real a mano, uso una navegación segura básica
            // Asumo que MainActivity maneja la navegación post-login
            if (getActivity() != null) {
                // Aquí podrías usar Navigation.findNavController(view).navigate(...) si usas Navigation Component
                // O simplemente cerrar la actividad si es un flujo de login separado
                // Por ahora, simularemos una navegación exitosa cerrando el fragmento o llamando a una acción
                 Toast.makeText(getContext(), "Registro exitoso", Toast.LENGTH_SHORT).show();
                 // Si estamos en un flujo de navegación:
                 // Navigation.findNavController(getView()).navigate(R.id.action_register_to_main);
                 // O si queremos usar el NavigationCommand que mencionaste (aunque no vi su implementación, asumo patrón)
                 // ((MainActivity) getActivity()).navegarAlHome(); 
                 
                 // Opción genérica segura:
                 getActivity().onBackPressed(); 
            }
        });
    }

    private void setupListeners() {
        binding.btnRegister.setOnClickListener(v -> {
            String nombre = binding.etNombre.getText().toString().trim();
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();
            String direccion = binding.etDireccion.getText().toString().trim();
            String telefono = binding.etTelefono.getText().toString().trim();

            registerViewModel.onRegisterClicked(nombre, email, password, direccion, telefono);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}