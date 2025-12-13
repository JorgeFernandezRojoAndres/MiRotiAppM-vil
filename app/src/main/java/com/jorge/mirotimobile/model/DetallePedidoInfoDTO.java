package com.jorge.mirotimobile.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class DetallePedidoInfoDTO implements Serializable {

    @SerializedName(value = "Plato", alternate = {"plato"})
    private String plato;

    @SerializedName(value = "PlatoId", alternate = {"platoId"})
    private int platoId;

    @SerializedName(value = "Cantidad", alternate = {"cantidad"})
    private int cantidad;

    @SerializedName(value = "Subtotal", alternate = {"subtotal"})
    private double subtotal;

    @SerializedName(value = "ImagenUrl", alternate = {"imagenUrl"})
    private String imagenUrl;

    public String getPlato() {
        return plato;
    }

    public void setPlato(String plato) {
        this.plato = plato;
    }

    public int getPlatoId() {
        return platoId;
    }

    public void setPlatoId(int platoId) {
        this.platoId = platoId;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }
}
