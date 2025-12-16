package com.jorge.mirotimobile.model;

import com.google.gson.annotations.SerializedName;

/**
 * Modelo Usuario — representa tanto las credenciales de login
 * como los datos básicos del usuario devueltos por la API.
 */
public class Usuario {

    // 🔹 Campos principales
    private int id;
    private String nombre;
    private String email;
    private String contrasenia;
    private String rol;
    @SerializedName(value = "Direccion", alternate = {"direccion"})
    private String direccion;
    @SerializedName(value = "Telefono", alternate = {"telefono"})
    private String telefono;

    // 🔹 Constructor vacío (necesario para Retrofit/Gson)
    public Usuario() {
    }

    // 🔹 Constructor usado por el ViewModel para enviar credenciales
    public Usuario(String email, String contrasenia) {
        this.email = email;
        this.contrasenia = contrasenia;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContrasenia() {
        return contrasenia;
    }

    public void setContrasenia(String contrasenia) {
        this.contrasenia = contrasenia;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
}
