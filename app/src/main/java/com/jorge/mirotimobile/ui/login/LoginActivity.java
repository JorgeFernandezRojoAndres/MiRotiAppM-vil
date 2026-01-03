package com.jorge.mirotimobile.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

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
        Log.d("LOGIN_FLOW", "LoginActivity onCreate");
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        vm = new ViewModelProvider(this).get(LoginViewModel.class);

        // Observadores del ViewModel — sin condicionales
        vm.getNavigateToMain().observe(this, event -> {
            Log.d("LOGIN_FLOW", "navigateToMain observer triggered");
            Boolean content = event.getContentIfNotHandled();
            if (content != null && content) {
                navegarAlMain();
            }
        });
        
        vm.getNavigateToRegister().observe(this, event -> {
            Boolean content = event.getContentIfNotHandled();
            if (content != null && content) {
                navegarAlRegistro();
            }
        });
        
        vm.getNavigateToResetPassword().observe(this, event -> {
            Boolean content = event.getContentIfNotHandled();
            if (content != null && content) {
                navegarAResetPassword();
            }
        });
        
        // Observar visibilidad directamente
        vm.getErrorVisibility().observe(this, binding.txtError::setVisibility);
        vm.getProgressVisibility().observe(this, binding.progress::setVisibility);
        vm.getMensajeError().observe(this, binding.txtError::setText);

        // Eventos de UI delegados al ViewModel
        binding.btnLogin.setOnClickListener(v ->
                vm.onLoginClicked(binding.etEmail.getText().toString().trim(),
                        binding.etPassword.getText().toString().trim()));
        binding.btnForgot.setOnClickListener(v -> vm.onForgotPasswordClicked());
        binding.btnRegistrarse.setOnClickListener(v -> vm.onRegisterClicked());
        binding.btnHuella.setOnClickListener(v -> vm.onHuellaClicked());
    }

    private void navegarAlMain() {
        Log.d("LOGIN_FLOW", "navegarAlMain() called");
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("abrirFragment", "platos");
        startActivity(intent);
        finish();
    }

    private void navegarAlRegistro() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    private void navegarAResetPassword() {
        Intent intent = new Intent(this, ResetPasswordActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
