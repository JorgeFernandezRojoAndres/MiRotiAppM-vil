package com.jorge.mirotimobile.Retrofit;

import com.jorge.mirotimobile.model.Plato;
import com.jorge.mirotimobile.model.RegisterRequest;
import com.jorge.mirotimobile.model.Usuario;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * 🌐 ApiService — Define todos los endpoints disponibles en la API de MiRoti.
 * Incluye autenticación JWT y recursos del cliente (platos, pedidos, etc.).
 */
public interface ApiService {

    // ---------------------------------------------------------
    // 🔐 LOGIN — autenticación de usuarios (Cliente / Cadete)
    // ---------------------------------------------------------
    // Endpoint real: /api/AuthApi/login (respeta mayúsculas del backend)
    @POST("AuthApi/login")
    Call<TokenResponse> login(@Body Usuario usuario);

    // ---------------------------------------------------------
    // 📝 REGISTER — registro de nuevos clientes
    // ---------------------------------------------------------
    // Endpoint real: /api/AuthApi/register
    @POST("AuthApi/register")
    Call<TokenResponse> register(@Body RegisterRequest request);

    // ---------------------------------------------------------
    // 🍽️ PLATOS — listado de platos disponibles
    // ---------------------------------------------------------
    // Endpoint real: /api/platosapi (con autorización Bearer)
    @GET("platosapi")
    Call<List<Plato>> obtenerPlatos(@Header("Authorization") String token);

    // ---------------------------------------------------------
    // 🧩 Clase interna: respuesta del login
    // ---------------------------------------------------------
    /**
     * 📦 TokenResponse — Representa la respuesta del endpoint /authapi/login y /authapi/register.
     * Estructura esperada desde el backend:
     * {
     *   "token": "JWT...",
     *   "id": 2,
     *   "email": "carolina@mail.com",
     *   "rol": "Cliente"
     * }
     */
    class TokenResponse {
        private String token;
        private int id;
        private String email;
        private String rol;

        // ✅ Getters
        public String getToken() {
            return token;
        }

        public int getId() {
            return id;
        }

        public String getEmail() {
            return email;
        }

        public String getRol() {
            return rol;
        }

        // ✅ Setters (requeridos por Gson)
        public void setToken(String token) {
            this.token = token;
        }

        public void setId(int id) {
            this.id = id;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public void setRol(String rol) {
            this.rol = rol;
        }
    }
}
