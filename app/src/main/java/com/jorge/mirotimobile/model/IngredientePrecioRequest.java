package com.jorge.mirotimobile.model;

import com.google.gson.annotations.SerializedName;

/**
 * 💲 DTO para PUT api/ingredientes/{id}.
 * Coincide con IngredientePrecioRequest del backend .NET: solo envía el precio nuevo.
 */
public class IngredientePrecioRequest {

    @SerializedName(value = "costoUnitario", alternate = {"CostoUnitario"})
    private double costoUnitario;

    public IngredientePrecioRequest() {}

    public IngredientePrecioRequest(double costoUnitario) {
        this.costoUnitario = costoUnitario;
    }

    public double getCostoUnitario() {
        return costoUnitario;
    }

    public void setCostoUnitario(double costoUnitario) {
        this.costoUnitario = costoUnitario;
    }
}
