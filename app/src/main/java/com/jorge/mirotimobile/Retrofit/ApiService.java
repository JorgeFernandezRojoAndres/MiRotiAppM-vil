package com.jorge.mirotimobile.Retrofit;

import com.jorge.mirotimobile.model.CrearPedidoRequest;
import com.jorge.mirotimobile.model.GenericResponse;
import com.jorge.mirotimobile.model.PedidoDTO;
import com.jorge.mirotimobile.model.Plato;
import com.jorge.mirotimobile.model.RegisterRequest;
import com.jorge.mirotimobile.model.ResetPasswordRequest;
import com.jorge.mirotimobile.model.Usuario;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {

    @POST("AuthApi/login")
    Call<TokenResponse> login(@Body Usuario usuario);

    @POST("AuthApi/register")
    Call<TokenResponse> register(@Body RegisterRequest request);

    @POST("AuthApi/reset-password")
    Call<GenericResponse> resetPassword(@Body ResetPasswordRequest request);

    @GET("platosapi")
    Call<List<Plato>> obtenerPlatos();

    @GET("pedidos/mis-pedidos")
    Call<List<PedidoDTO>> obtenerMisPedidos();

    @POST("pedidos")
    Call<PedidoDTO> crearPedido(@Body CrearPedidoRequest request);

    // Cadete
    @GET("pedidos/disponibles")
    Call<List<PedidoDTO>> obtenerPedidosDisponibles();

    @PUT("pedidos/{id}/tomar")
    Call<PedidoDTO> tomarPedido(@Path("id") int id);

    @PUT("pedidos/{id}/entregar")
    Call<PedidoDTO> marcarPedidoEntregado(@Path("id") int id);

    @GET("usuarios/perfil")
    Call<Usuario> obtenerPerfil();

    class TokenResponse {
        private String token;
        private int id;
        private String email;
        private String rol;

        public String getToken() { return token; }
        public int getId() { return id; }
        public String getEmail() { return email; }
        public String getRol() { return rol; }

        public void setToken(String token) { this.token = token; }
        public void setId(int id) { this.id = id; }
        public void setEmail(String email) { this.email = email; }
        public void setRol(String rol) { this.rol = rol; }
    }
}
