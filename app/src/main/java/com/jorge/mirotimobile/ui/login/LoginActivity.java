package com.jorge.mirotimobile.ui.login;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.jorge.mirotimobile.MainActivity;
import com.jorge.mirotimobile.databinding.ActivityLoginBinding;

/**
 * 🔐 LoginActivity — Vista 100 % pasiva según MVVM estricto.
 * No tiene lógica ni condicionales: reacciona solo a LiveData del ViewModel.
 */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private LoginViewModel vm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        vm = new ViewModelProvider(this).get(LoginViewModel.class);

        // Observadores del ViewModel — sin condicionales
        vm.getNavigateToMain().observe(this, unused -> navegarAlMain());
        vm.getShowError().observe(this,
                show -> binding.txtError.setVisibility(show ? android.view.View.VISIBLE : android.view.View.GONE));
        vm.getMensajeError().observe(this, binding.txtError::setText);
        vm.getLoading().observe(this,
                loading -> binding.progress.setVisibility(loading ? android.view.View.VISIBLE : android.view.View.GONE));

        // Eventos de UI delegados al ViewModel
        binding.btnLogin.setOnClickListener(v ->
                vm.onLoginClicked(binding.etEmail.getText().toString().trim(),
                        binding.etPassword.getText().toString().trim()));
        binding.btnForgot.setOnClickListener(v -> vm.onForgotPasswordClicked());
        binding.btnRegistrarse.setOnClickListener(v -> vm.onRegisterClicked());
        binding.btnHuella.setOnClickListener(v -> vm.onHuellaClicked());
    }

    private void navegarAlMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("abrirFragment", "platos");
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
