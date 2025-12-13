package com.jorge.mirotimobile.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class PedidoDTO implements Serializable {

    @SerializedName(value = "Id", alternate = {"id"})
    private int id;

    @SerializedName(value = "FechaHora", alternate = {"fechaHora"})
    private String fechaHora;

    @SerializedName(value = "Estado", alternate = {"estado"})
    private String estado;

    @SerializedName(value = "Total", alternate = {"total"})
    private double total;

    @SerializedName(value = "Detalles", alternate = {"detalles"})
    private List<DetallePedidoInfoDTO> detalles;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(String fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public List<DetallePedidoInfoDTO> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetallePedidoInfoDTO> detalles) {
        this.detalles = detalles;
    }
}
