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

    @SerializedName(value = "Telefono", alternate = {"telefono", "ClienteTelefono", "clienteTelefono"})
    private String telefono;
    @SerializedName(value = "CadeteTelefono", alternate = {"cadeteTelefono", "CadetePhone", "cadetePhone"})
    private String cadeteTelefono;

    @SerializedName(
            value = "ClienteDireccion",
            alternate = {"clienteDireccion", "direccion", "Direccion"}
    )
    private String direccion;

    @SerializedName(value = "Cliente", alternate = {"cliente"})
    private String cliente;

    @SerializedName(value = "Cadete", alternate = {"cadete"})
    private String cadete;

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

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCadeteTelefono() {
        return cadeteTelefono;
    }

    public void setCadeteTelefono(String cadeteTelefono) {
        this.cadeteTelefono = cadeteTelefono;
    }

    public String getDireccion() {
        // Fallback para testing cuando la API no tiene dirección
        if (direccion == null || direccion.trim().isEmpty()) {
            return "Av. Corrientes 1234, CABA"; // Dirección de prueba
        }
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getCadete() {
        return cadete;
    }

    public void setCadete(String cadete) {
        this.cadete = cadete;
    }
}
