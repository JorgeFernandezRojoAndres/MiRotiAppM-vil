package com.jorge.mirotimobile.ui.login;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.jorge.mirotimobile.databinding.ActivityResetPasswordBinding;

public class ResetPasswordActivity extends AppCompatActivity {

    private ActivityResetPasswordBinding binding;
    private ResetPasswordViewModel vm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResetPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        vm = new ViewModelProvider(this).get(ResetPasswordViewModel.class);

        // Observar visibilidad directamente
        vm.getProgressVisibility().observe(this, binding.progress::setVisibility);
        vm.getErrorVisibility().observe(this, binding.txtError::setVisibility);
        vm.getSuccessVisibility().observe(this, binding.txtSuccess::setVisibility);
        
        // Observar textos directamente
        vm.getErrorText().observe(this, binding.txtError::setText);
        vm.getSuccessText().observe(this, binding.txtSuccess::setText);

        vm.getNavigateToLogin().observe(this, unused ->
                binding.getRoot().postDelayed(this::volverAlLogin, 800));

        binding.btnConfirmar.setOnClickListener(v ->
                vm.resetPassword(
                        binding.etEmail.getText().toString(),
                        binding.etNuevaContrasenia.getText().toString(),
                        binding.etRepetirContrasenia.getText().toString()
                ));
    }

    private void volverAlLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
