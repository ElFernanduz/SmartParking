package com.tecno.Smartparking.security;

import com.tecno.Smartparking.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/** Guarda y recupera el usuario autenticado dentro de la sesion HTTP. */
public final class SesionOperador {

    public static final String ATRIBUTO = "smartparking.usuario";

    private SesionOperador() {
    }

    public static void abrir(HttpServletRequest peticion, User user) {
        // Sesion nueva en cada login: evita fijacion de sesion.
        HttpSession anterior = peticion.getSession(false);
        if (anterior != null) {
            anterior.invalidate();
        }
        peticion.getSession(true).setAttribute(ATRIBUTO, user);
    }

    public static User usuarioDe(HttpServletRequest peticion) {
        HttpSession sesion = peticion.getSession(false);
        return sesion == null ? null : (User) sesion.getAttribute(ATRIBUTO);
    }

    public static void cerrar(HttpServletRequest peticion) {
        HttpSession sesion = peticion.getSession(false);
        if (sesion != null) {
            sesion.invalidate();
        }
    }
}
