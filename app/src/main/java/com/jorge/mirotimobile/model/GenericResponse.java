package com.jorge.mirotimobile.model;

import com.google.gson.annotations.SerializedName;

public class GenericResponse {
    @SerializedName(value = "mensaje", alternate = {"message", "Mensaje"})
    private String mensaje;

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}

