package com.jorge.mirotimobile.model;

import androidx.annotation.ColorRes;

import com.jorge.mirotimobile.R;

public enum EstadoPedido {

    PENDIENTE("Pendiente", R.color.nav_icon_inactive),
    EN_PREPARACION("En preparación", R.color.estado_proceso),
    EN_CAMINO("En camino", R.color.estado_proceso),
    COMPLETADO("Completado", R.color.estado_entregado),
    CANCELADO("Cancelado", R.color.estado_cancelado);

    private final String label;
    private final int colorRes;

    EstadoPedido(String label, @ColorRes int colorRes) {
        this.label = label;
        this.colorRes = colorRes;
    }

    public String getLabel() {
        return label;
    }

    @ColorRes
    public int getColorRes() {
        return colorRes;
    }

    public static EstadoPedido fromString(String value) {
        if (value == null) return PENDIENTE;
        for (EstadoPedido estado : values()) {
            if (estado.label.equalsIgnoreCase(value.trim())) {
                return estado;
            }
        }
        return PENDIENTE;
    }
}
