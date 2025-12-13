package com.jorge.mirotimobile.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class CrearDetallePedidoRequest implements Serializable {

    @SerializedName("PlatoId")
    private int platoId;

    @SerializedName("Cantidad")
    private int cantidad;

    @SerializedName("Subtotal")
    private double subtotal;

    public CrearDetallePedidoRequest() {
    }

    public CrearDetallePedidoRequest(int platoId, int cantidad, double subtotal) {
        this.platoId = platoId;
        this.cantidad = cantidad;
        this.subtotal = subtotal;
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
}
