package com.jorge.mirotimobile.model;

import com.google.gson.annotations.SerializedName;

/**
 * 🥕 Modelo Ingrediente — representa un insumo/ingrediente con su precio
 * y su STOCK actual, obtenidos desde la API MiRoti (backend .NET).
 *
 * <p>Formato real del GET api/ingredientes (camelCase):
 * { "id": 1, "nombre": "Papa", "costoUnitario": 950.00,
 *   "unidadMedida": { "id": 1, "nombre": "Kilogramo", "abreviatura": "kg" },
 *   "stockActual": 10000 }
 *
 * <p>El stock es decimal(10,2) en la base de datos, por eso lo decimos
 * como double y lo mostramos con 2 decimales (sin coma como separador).
 */
public class Ingrediente implements java.io.Serializable {

    @SerializedName(value = "id", alternate = {"Id"})
    private int id;

    @SerializedName(value = "nombre", alternate = {"Nombre"})
    private String nombre;

    @SerializedName(value = "descripcion", alternate = {"Descripcion"})
    private String descripcion;

    // 💲 Precio de compra actual en el supermercado (decimal(10,2) en la DB)
    @SerializedName(value = "costoUnitario", alternate = {"CostoUnitario", "precio", "Precio"})
    private double precio;

    // 📦 Stock disponible actual (decimal(10,2) en la DB)
    @SerializedName(value = "stockActual", alternate = {"StockActual", "stock", "Stock"})
    private double stockActual;

    // 📦 Unidad de medida anidada, como la devuelve el backend
    @SerializedName(value = "unidadMedida", alternate = {"UnidadMedida"})
    private UnidadMedidaInfo unidadMedida;

    @SerializedName(value = "activo", alternate = {"Activo"})
    private boolean activo;

    /** Unidad de medida como la envía el backend (objeto anidado). */
    public static class UnidadMedidaInfo implements java.io.Serializable {
        @SerializedName(value = "id", alternate = {"Id"})
        private int id;

        @SerializedName(value = "nombre", alternate = {"Nombre"})
        private String nombre;

        @SerializedName(value = "abreviatura", alternate = {"Abreviatura"})
        private String abreviatura;

        public int getId() { return id; }

        public void setId(int id) { this.id = id; }

        public String getNombre() { return nombre; }

        public void setNombre(String nombre) { this.nombre = nombre; }

        public String getAbreviatura() { return abreviatura; }

        public void setAbreviatura(String abreviatura) { this.abreviatura = abreviatura; }
    }

    // 🔹 Constructor vacío (requerido por Retrofit/Gson)
    public Ingrediente() {}

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

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public double getStockActual() {
        return stockActual;
    }

    public void setStockActual(double stockActual) {
        this.stockActual = stockActual;
    }

    public UnidadMedidaInfo getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(UnidadMedidaInfo unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    /** Abreviatura de la unidad para mostrar ("kg"), con fallback al nombre. */
    public String getUnidadMedidaTexto() {
        if (unidadMedida != null) {
            if (unidadMedida.getAbreviatura() != null && !unidadMedida.getAbreviatura().isEmpty()) {
                return unidadMedida.getAbreviatura();
            }
            if (unidadMedida.getNombre() != null) {
                return unidadMedida.getNombre();
            }
        }
        return "";
    }

    /** Verifica que el stock sea mayor a 0 (el ingrediente está disponible). */
    public boolean tieneStock() {
        return stockActual > 0;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
