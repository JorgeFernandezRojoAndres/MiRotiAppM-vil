package com.jorge.mirotimobile.model;

public class PedidoResumen {
    private final String numero;
    private final String fecha;
    private final String estado;
    private final String cadete;
    private final String total;

    public PedidoResumen(String numero, String fecha, String estado, String cadete, String total) {
        this.numero = numero;
        this.fecha = fecha;
        this.estado = estado;
        this.cadete = cadete;
        this.total = total;
    }

    public String getNumero() {
        return numero;
    }

    public String getFecha() {
        return fecha;
    }

    public String getEstado() {
        return estado;
    }

    public String getCadete() {
        return cadete;
    }

    public String getTotal() {
        return total;
    }
}
