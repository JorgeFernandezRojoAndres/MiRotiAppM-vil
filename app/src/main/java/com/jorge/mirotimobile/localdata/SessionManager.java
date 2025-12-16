package com.jorge.mirotimobile.localdata;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.jorge.mirotimobile.model.PedidoDTO;

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
    private static final String USER_NAME_KEY = "user_name";

    // 🔹 Claves adicionales para login con huella
    private static final String SAVED_EMAIL_KEY = "saved_email";
    private static final String SAVED_PASSWORD_KEY = "saved_password";
    private static final String PENDING_PEDIDO_KEY = "pending_pedido";
    private static final String PENDING_PEDIDO_FLAG_KEY = "pending_pedido_flag";
    private static final String CADETE_EN_SERVICIO_KEY = "cadete_en_servicio";

    private final SharedPreferences prefs;
    private final Gson gson;

    // ======================================
    // 🔸 Constructor
    // ======================================
    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
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

    @SuppressLint("ApplySharedPref")
    public void saveUserName(String nombre) {
        prefs.edit()
                .putString(USER_NAME_KEY, nombre)
                .apply();
    }

    public String getUserName() {
        return prefs.getString(USER_NAME_KEY, null);
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

    @SuppressLint("ApplySharedPref")
    public void savePendingPedido(PedidoDTO pedido) {
        if (pedido == null) {
            clearPendingPedido();
            return;
        }
        String json = gson.toJson(pedido);
        prefs.edit()
                .putString(PENDING_PEDIDO_KEY, json)
                .apply();
    }

    public PedidoDTO getPendingPedido() {
        String json = prefs.getString(PENDING_PEDIDO_KEY, null);
        if (json == null) return null;
        try {
            return gson.fromJson(json, PedidoDTO.class);
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressLint("ApplySharedPref")
    public void savePedidoPendienteFlag(boolean pending) {
        prefs.edit()
                .putBoolean(PENDING_PEDIDO_FLAG_KEY, pending)
                .apply();
    }

    public boolean isPedidoPendientePersisted() {
        return prefs.getBoolean(PENDING_PEDIDO_FLAG_KEY, false);
    }

    @SuppressLint("ApplySharedPref")
    public void setCadeteEnServicio(boolean enServicio) {
        prefs.edit()
                .putBoolean(CADETE_EN_SERVICIO_KEY, enServicio)
                .apply();
    }

    public boolean isCadeteEnServicio() {
        return prefs.getBoolean(CADETE_EN_SERVICIO_KEY, true);
    }

    @SuppressLint("ApplySharedPref")
    public void clearPendingPedido() {
        prefs.edit()
            .remove(PENDING_PEDIDO_KEY)
                .remove(PENDING_PEDIDO_FLAG_KEY)
                .apply();
    }
}
