package com.jorge.mirotimobile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import android.content.res.ColorStateList;
import android.graphics.Color;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.jorge.mirotimobile.localdata.SessionManager;
import com.jorge.mirotimobile.ui.login.LoginActivity;
import com.jorge.mirotimobile.ui.pedidos.PedidosViewModel;

public class MainActivity extends AppCompatActivity {

    private SessionManager session;
    private boolean tienePedidosActivos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        setContentView(R.layout.activity_main);



        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setItemIconTintList(ColorStateList.valueOf(Color.BLACK));
        bottomNav.setItemTextColor(ColorStateList.valueOf(Color.BLACK));
        
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(bottomNav, navController);

            PedidosViewModel pedidosViewModel = new ViewModelProvider(this).get(PedidosViewModel.class);
            pedidosViewModel.getPedidos().observe(this, pedidos -> {
                tienePedidosActivos = pedidos != null && !pedidos.isEmpty();
            });

            bottomNav.setOnItemSelectedListener(item -> {
                Log.d("NAV", "BottomNav tap: " + item.getItemId());
                boolean handled = NavigationUI.onNavDestinationSelected(item, navController);
                if (item.getItemId() == R.id.trackingFragment && !tienePedidosActivos) {
                    Toast.makeText(this, "No tenés pedidos en seguimiento", Toast.LENGTH_SHORT).show();
                }
                // FIX: let NavigationUI drive navigation while still warning about tracking.
                return handled;
            });
            bottomNav.setOnItemReselectedListener(item -> {
                if (item.getItemId() == R.id.pedidosFragment) {
                    navController.navigate(R.id.pedidosFragment);
                }
            });
        }

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
