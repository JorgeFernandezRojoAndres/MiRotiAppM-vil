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
        // Observar visibilidad y estado sin condicionales
        registerViewModel.getLoadingVisibility().observe(this, binding.loading::setVisibility);
        registerViewModel.getButtonEnabled().observe(this, binding.btnRegister::setEnabled);
        
        // Observar mensajes toast
        registerViewModel.getToastMessage().observe(this, mensaje -> {
            Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
        });

        registerViewModel.getNavigateToMain().observe(this, unused -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finishAffinity();
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