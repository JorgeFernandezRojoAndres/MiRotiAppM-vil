package com.jorge.mirotimobile.model;

import com.google.gson.annotations.SerializedName;

/**
 * 📤 DTO para POST api/ingredientes.
 * Coincide con IngredienteCrearRequest del backend .NET, donde unidadMedida
 * es la ABREVIATURA como string ("kg", "g", "u", "L").
 * (El GET devuelve unidadMedida como objeto anidado; por eso este request
 * es una clase separada y no reutiliza Ingrediente.)
 */
public class IngredienteCrearRequest {

    @SerializedName(value = "nombre", alternate = {"Nombre"})
    private String nombre;

    @SerializedName(value = "costoUnitario", alternate = {"CostoUnitario"})
    private double costoUnitario;

    @SerializedName(value = "unidadMedida", alternate = {"UnidadMedida"})
    private String unidadMedida; // abreviatura: "kg", "g", "u", "L"

    public IngredienteCrearRequest() {}

    public IngredienteCrearRequest(String nombre, double costoUnitario, String unidadMedida) {
        this.nombre = nombre;
        this.costoUnitario = costoUnitario;
        this.unidadMedida = unidadMedida;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getCostoUnitario() {
        return costoUnitario;
    }

    public void setCostoUnitario(double costoUnitario) {
        this.costoUnitario = costoUnitario;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }
}
