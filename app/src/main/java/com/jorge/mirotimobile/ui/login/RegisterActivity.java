package com.jorge.mirotimobile.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.jorge.mirotimobile.MainActivity;
import com.jorge.mirotimobile.databinding.FragmentRegisterBinding;

public class RegisterActivity extends AppCompatActivity {

    private FragmentRegisterBinding binding;
    private RegisterViewModel registerViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        registerViewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        setupObservers();
        setupListeners();
    }

    private void setupObservers() {
        registerViewModel.getLoading().observe(this, isLoading -> {
            binding.loading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnRegister.setEnabled(!isLoading);
        });

        registerViewModel.getMensajeError().observe(this, mensaje -> {
            if (mensaje != null && !mensaje.isEmpty()) {
                Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
            }
        });

        registerViewModel.getNavigateToMain().observe(this, unused -> {
            Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            // Dependiendo del flujo, podrías querer ir al MainActivity o volver al login y que el usuario entre
            // pero el requerimiento decía navegar al home tras éxito.
            startActivity(intent);
            finishAffinity(); // Cierra el activity actual y todos los padres (LoginActivity) para que no pueda volver atrás
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
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}