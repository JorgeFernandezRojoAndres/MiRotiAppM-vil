package com.jorge.mirotimobile.model;

public class ResetPasswordRequest {
    private String email;
    private String nuevaContrasenia;
    private String repetirContrasenia;

    public ResetPasswordRequest(String email, String nuevaContrasenia, String repetirContrasenia) {
        this.email = email;
        this.nuevaContrasenia = nuevaContrasenia;
        this.repetirContrasenia = repetirContrasenia;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNuevaContrasenia() {
        return nuevaContrasenia;
    }

    public void setNuevaContrasenia(String nuevaContrasenia) {
        this.nuevaContrasenia = nuevaContrasenia;
    }

    public String getRepetirContrasenia() {
        return repetirContrasenia;
    }

    public void setRepetirContrasenia(String repetirContrasenia) {
        this.repetirContrasenia = repetirContrasenia;
    }
}

