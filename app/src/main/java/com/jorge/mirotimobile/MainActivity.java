package com.jorge.mirotimobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.jorge.mirotimobile.localdata.SessionManager;
import com.jorge.mirotimobile.ui.login.LoginActivity;

/**
 * 🏠 MainActivity — Punto de entrada principal tras el login.
 * Si no hay sesión guardada, redirige automáticamente al Login.
 * Carga directamente la vista de Platos.
 */
public class MainActivity extends AppCompatActivity {

    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true); // ✅ Soporte de VectorDrawable
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // ✅ Quitar franja negra del sistema (barra inferior y superior)
        Window window = getWindow();
        window.setNavigationBarColor(getResources().getColor(android.R.color.transparent));
        window.setStatusBarColor(getResources().getColor(android.R.color.transparent));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 🔹 Inicializar SessionManager
        session = new SessionManager(getApplicationContext());

        // 🔹 Verificar si hay token guardado (sesión activa)
        String token = session.getToken();
        if (token == null || token.isEmpty()) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return;
        }

        // 🔹 Cargar directamente la vista de Platos
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main, new com.jorge.mirotimobile.ui.platos.PlatosFragment())
                .commit();
    }

    /**
     * 🔹 Cerrar sesión limpiando el token y volviendo al login.
     */
    public void logout() {
        session.clear();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
