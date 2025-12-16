package com.jorge.mirotimobile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import android.content.res.ColorStateList;
import android.graphics.Color;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.jorge.mirotimobile.localdata.SessionManager;
import com.jorge.mirotimobile.ui.login.LoginActivity;
import com.jorge.mirotimobile.ui.cliente.pedidos.PedidosViewModel;

public class MainActivity extends AppCompatActivity {

    private SessionManager session;
    private boolean tienePedidosActivos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MAIN_FLOW", "MainActivity onCreate START");
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        setContentView(R.layout.activity_main);
        Log.d("MAIN_FLOW", "setContentView DONE");

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        Log.d("MAIN_FLOW", "WindowCompat DONE");
        applySystemBarInsets();
        Log.d("MAIN_FLOW", "applySystemBarInsets DONE");

        session = new SessionManager(getApplicationContext());
        Log.d("MAIN_FLOW", "SessionManager created");
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setItemIconTintList(ColorStateList.valueOf(Color.BLACK));
        bottomNav.setItemTextColor(ColorStateList.valueOf(Color.BLACK));
        
        Log.d("MAIN_FLOW", "Getting NavHostFragment");
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            Log.d("MAIN_FLOW", "NavHostFragment found");
            NavController navController = navHostFragment.getNavController();
            Log.d("MAIN_FLOW", "NavController obtained");
            NavigationUI.setupWithNavController(bottomNav, navController);
            Log.d("MAIN_FLOW", "NavigationUI setup DONE");
            String role = session.getUserRole();
            Log.d("MAIN_FLOW", "User role: " + role);
            if ("Cadete".equalsIgnoreCase(role)) {
                Log.d("MAIN_FLOW", "Setting up Cadete menu");
                bottomNav.getMenu().clear();
                bottomNav.inflateMenu(R.menu.menu_cadete);
                Log.d("MAIN_FLOW", "Navigating to entregasFragment");
                navController.navigate(R.id.entregasFragment);
                Log.d("MAIN_FLOW", "Navigation to entregasFragment DONE");
            }
            bottomNav.setOnItemSelectedListener(item -> {
                if (item.getItemId() == R.id.entregasFragment) {
                    NavOptions options = new NavOptions.Builder()
                            .setPopUpTo(R.id.entregasFragment, true)
                            .setLaunchSingleTop(true)
                            .build();
                    navController.navigate(R.id.entregasFragment, null, options);
                    return true;
                }
                return NavigationUI.onNavDestinationSelected(item, navController);
            });

            Log.d("MAIN_FLOW", "Creating PedidosViewModel");
            PedidosViewModel pedidosViewModel = new ViewModelProvider(this).get(PedidosViewModel.class);
            Log.d("MAIN_FLOW", "PedidosViewModel created, setting up observer");
            pedidosViewModel.getPedidos().observe(this, pedidos -> {
                Log.d("MAIN_FLOW", "PedidosViewModel observer triggered");
                tienePedidosActivos = pedidos != null && !pedidos.isEmpty();
            });
            Log.d("MAIN_FLOW", "PedidosViewModel observer setup DONE");

            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                if (destination.getId() == R.id.trackingFragment && !tienePedidosActivos) {
                    Toast.makeText(this, "No tenés pedidos en seguimiento", Toast.LENGTH_SHORT).show();
                }
            });
        }

        Log.d("MAIN_FLOW", "Checking token");
        String token = session.getToken();

        if (token == null || token.isEmpty()) {
            Log.d("MAIN_FLOW", "No token, redirecting to login");
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return;
        }
        Log.d("MAIN_FLOW", "MainActivity onCreate COMPLETE");
    }

    private void applySystemBarInsets() {
        final android.view.View navHost = findViewById(R.id.nav_host_fragment);
        final android.view.View bottomNav = findViewById(R.id.bottom_nav);

        if (navHost != null) {
            final int initialPaddingLeft = navHost.getPaddingLeft();
            final int initialPaddingTop = navHost.getPaddingTop();
            final int initialPaddingRight = navHost.getPaddingRight();
            final int initialPaddingBottom = navHost.getPaddingBottom();

            ViewCompat.setOnApplyWindowInsetsListener(navHost, (v, insets) -> {
                Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(
                        initialPaddingLeft + bars.left,
                        initialPaddingTop + bars.top,
                        initialPaddingRight + bars.right,
                        initialPaddingBottom
                );
                return insets;
            });
            ViewCompat.requestApplyInsets(navHost);
        }

        if (bottomNav != null) {
            final int initialPaddingLeft = bottomNav.getPaddingLeft();
            final int initialPaddingTop = bottomNav.getPaddingTop();
            final int initialPaddingRight = bottomNav.getPaddingRight();
            final int initialPaddingBottom = bottomNav.getPaddingBottom();

            ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
                Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(
                        initialPaddingLeft + bars.left,
                        initialPaddingTop,
                        initialPaddingRight + bars.right,
                        initialPaddingBottom + bars.bottom
                );
                return insets;
            });
            ViewCompat.requestApplyInsets(bottomNav);
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
