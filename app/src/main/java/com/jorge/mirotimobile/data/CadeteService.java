package com.jorge.mirotimobile.data;

import com.jorge.mirotimobile.model.PedidoDTO;

import retrofit2.Call;
import retrofit2.http.GET;

public interface CadeteService {

    @GET("cadetes/entregas/actual")
    Call<PedidoDTO> getEntregaActual();
}
