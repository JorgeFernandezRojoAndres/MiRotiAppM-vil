package com.jorge.mirotimobile.model;

import com.google.gson.annotations.SerializedName;

/**
 * 🍽️ Modelo Plato — representa los datos de un plato obtenidos desde la API MiRoti.
 * Refleja fielmente la estructura del backend (.NET) incluyendo precioVenta, costoTotal e imagenUrl.
 */
public class Plato {

    @SerializedName(value = "id", alternate = {"Id"})
    private int id;

    @SerializedName(value = "nombre", alternate = {"Nombre"})
    private String nombre;

    @SerializedName(value = "descripcion", alternate = {"Descripcion"})
    private String descripcion;

    // ✅ Acepta variantes de casing desde el backend (.NET suele enviar PascalCase y alias "Precio")
    @SerializedName(value = "Precio", alternate = {"precio", "precioVenta", "PrecioVenta"})
    private double precioVenta;   // ✅ igual que en el backend

    @SerializedName(value = "costoTotal", alternate = {"CostoTotal"})
    private double costoTotal;

    @SerializedName(value = "disponible", alternate = {"Disponible"})
    private boolean disponible;

    @SerializedName(value = "imagenUrl", alternate = {"ImagenUrl", "imagenURL"})
    private String imagenUrl;     // ✅ nueva propiedad opcional para imagen

    // 🔹 Constructor vacío (requerido por Retrofit/Gson)
    public Plato() {}

    // 🔹 Constructor opcional
    public Plato(int id, String nombre, String descripcion, double precioVenta, double costoTotal, boolean disponible, String imagenUrl) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precioVenta = precioVenta;
        this.costoTotal = costoTotal;
        this.disponible = disponible;
        this.imagenUrl = imagenUrl;
    }

    // 🔹 Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(double precioVenta) {
        this.precioVenta = precioVenta;
    }

    public double getCostoTotal() {
        return costoTotal;
    }

    public void setCostoTotal(double costoTotal) {
        this.costoTotal = costoTotal;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }
}
