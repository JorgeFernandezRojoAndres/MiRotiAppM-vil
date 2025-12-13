package com.jorge.mirotimobile.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class CrearPedidoRequest implements Serializable {

    @SerializedName("Total")
    private double total;

    @SerializedName("Detalles")
    private List<CrearDetallePedidoRequest> detalles;

    public CrearPedidoRequest() {
    }

    public CrearPedidoRequest(double total, List<CrearDetallePedidoRequest> detalles) {
        this.total = total;
        this.detalles = detalles;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public List<CrearDetallePedidoRequest> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<CrearDetallePedidoRequest> detalles) {
        this.detalles = detalles;
    }
}
