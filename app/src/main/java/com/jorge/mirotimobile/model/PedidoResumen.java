package com.jorge.mirotimobile.model;

public class PedidoResumen {
    private final String numero;
    private final String fecha;
    private final String estado;

    public PedidoResumen(String numero, String fecha, String estado) {
        this.numero = numero;
        this.fecha = fecha;
        this.estado = estado;
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
}
