package com.jorge.mirotimobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
// Imports para Insets ya no son necesarios sin el listener de padding personalizado
// import androidx.core.graphics.Insets;
// import androidx.core.view.ViewCompat;
// import androidx.core.view.WindowInsetsCompat;

import com.jorge.mirotimobile.localdata.SessionManager;
import com.jorge.mirotimobile.ui.login.LoginActivity;

public class MainActivity extends AppCompatActivity {

    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        EdgeToEdge.enable(this); // Mantener EdgeToEdge habilitado

        setContentView(R.layout.activity_main);

        // 🔹 Hacer barras del sistema transparentes
        Window window = getWindow();
        window.setNavigationBarColor(getResources().getColor(android.R.color.transparent));
        window.setStatusBarColor(getResources().getColor(android.R.color.transparent));

        // 🔹 ELIMINADO: El listener de insets personalizado que establecía padding inferior a 0.
        // Ahora la BottomNavigationView con android:fitsSystemWindows="true" manejará esto.
        /*
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });
        */

        // 🔹 Verificar sesión
        session = new SessionManager(getApplicationContext());
        String token = session.getToken();

        if (token == null || token.isEmpty()) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return;
        }
    }

    public void logout() {
        session.clear();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
