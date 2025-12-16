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

        vm.getLoading().observe(this,
                loading -> binding.progress.setVisibility(loading ? android.view.View.VISIBLE : android.view.View.GONE));

        vm.getMensajeError().observe(this, msg -> {
            binding.txtError.setText(msg);
            binding.txtError.setVisibility(msg == null || msg.isEmpty() ? android.view.View.GONE : android.view.View.VISIBLE);
        });

        vm.getMensajeExito().observe(this, msg -> {
            binding.txtSuccess.setText(msg);
            binding.txtSuccess.setVisibility(msg == null || msg.isEmpty() ? android.view.View.GONE : android.view.View.VISIBLE);
        });

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
