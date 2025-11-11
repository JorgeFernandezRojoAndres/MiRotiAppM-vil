package com.jorge.mirotimobile.localdata;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * 📦 SessionManager — Manejo completo de sesión y persistencia local.
 * Administra token JWT, datos de usuario y credenciales seguras para login biométrico.
 */
public class SessionManager {

    private static final String PREF_NAME = "MiRotiPrefs";

    // 🔐 Claves principales
    private static final String TOKEN_KEY = "token";
    private static final String USER_ID_KEY = "user_id";
    private static final String USER_EMAIL_KEY = "user_email";
    private static final String USER_ROLE_KEY = "user_role";

    // 🔹 Claves adicionales para login con huella
    private static final String SAVED_EMAIL_KEY = "saved_email";
    private static final String SAVED_PASSWORD_KEY = "saved_password";

    private final SharedPreferences prefs;

    // ======================================
    // 🔸 Constructor
    // ======================================
    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // ======================================
    // 🔸 Token JWT
    // ======================================
    @SuppressLint("ApplySharedPref")
    public void saveToken(String token) {
        prefs.edit().putString(TOKEN_KEY, token).apply();
    }

    public String getToken() {
        return prefs.getString(TOKEN_KEY, null);
    }

    // ======================================
    // 🔸 Datos del usuario autenticado
    // ======================================
    @SuppressLint("ApplySharedPref")
    public void saveUserData(int id, String email, String role) {
        prefs.edit()
                .putInt(USER_ID_KEY, id)
                .putString(USER_EMAIL_KEY, email)
                .putString(USER_ROLE_KEY, role)
                .apply();
    }

    public int getUserId() {
        return prefs.getInt(USER_ID_KEY, -1);
    }

    public String getUserEmail() {
        return prefs.getString(USER_EMAIL_KEY, null);
    }

    public String getUserRole() {
        return prefs.getString(USER_ROLE_KEY, null);
    }

    public boolean isLoggedIn() {
        return getToken() != null;
    }

    @SuppressLint("ApplySharedPref")
    public void clear() {
        prefs.edit().clear().apply();
    }

    // ======================================
    // 🔸 Credenciales para login con huella
    // ======================================

    /**
     * Guarda las credenciales (email + password) para HomeClienteFragment automático con huella digital.
     * ⚠️ Se almacenan en SharedPreferences de forma simple (no cifrada).
     * En un entorno productivo se recomienda usar EncryptedSharedPreferences.
     */
    @SuppressLint("ApplySharedPref")
    public void saveCredentials(String email, String password) {
        prefs.edit()
                .putString(SAVED_EMAIL_KEY, email)
                .putString(SAVED_PASSWORD_KEY, password)
                .apply();
    }

    public String getSavedEmail() {
        return prefs.getString(SAVED_EMAIL_KEY, null);
    }

    public String getSavedPassword() {
        return prefs.getString(SAVED_PASSWORD_KEY, null);
    }

    /**
     * Limpia las credenciales guardadas (por seguridad).
     */
    @SuppressLint("ApplySharedPref")
    public void clearCredentials() {
        prefs.edit()
                .remove(SAVED_EMAIL_KEY)
                .remove(SAVED_PASSWORD_KEY)
                .apply();
    }
}
