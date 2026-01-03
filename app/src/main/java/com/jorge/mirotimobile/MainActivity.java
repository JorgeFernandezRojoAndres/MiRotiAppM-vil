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

    private MainViewModel mainViewModel;

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

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        
        // Observar ViewModel
        observarViewModel();
        
        // Configurar UI
        configurarUI();
        
        Log.d("MAIN_FLOW", "MainActivity onCreate COMPLETE");
    }
    
    private void observarViewModel() {
        // Observar redirección a login
        mainViewModel.getEventoRedirectLogin().observe(this, event -> {
            Boolean shouldRedirect = event.getContentIfNotHandled();
            redirectToLogin();
        });
        
        // Observar mensajes toast
        mainViewModel.getMensajeToast().observe(this, mensaje -> {
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
        });
    }
    
    private void configurarUI() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setItemIconTintList(ColorStateList.valueOf(Color.BLACK));
        bottomNav.setItemTextColor(ColorStateList.valueOf(Color.BLACK));
        
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(bottomNav, navController);
        
        // Observar configuración del menú
        mainViewModel.getEventoConfigurarMenu().observe(this, event -> {
            Integer menuRes = event.getContentIfNotHandled();
            bottomNav.getMenu().clear();
            bottomNav.inflateMenu(menuRes);
        });
        
        // Observar eventos de navegación
        mainViewModel.getEventoNavegacion().observe(this, event -> {
            Integer destinationId = event.getContentIfNotHandled();
            NavOptions options = new NavOptions.Builder()
                    .setLaunchSingleTop(true)
                    .build();
            navController.navigate(destinationId, null, options);
        });
        
        bottomNav.setOnItemSelectedListener(item -> {
            mainViewModel.onMenuItemSelected(item.getItemId());
            return true;
        });

        PedidosViewModel pedidosViewModel = new ViewModelProvider(this).get(PedidosViewModel.class);
        pedidosViewModel.getPedidos().observe(this, mainViewModel::onPedidosChanged);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            mainViewModel.onNavigateToTracking();
        });
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
        mainViewModel.logout();
    }
    
    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
