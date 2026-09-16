package com.unillanos.smartparking.dominio.autenticacion;

/** Estrategia de cifrado de contrasenas. */
public interface PasswordHasher {

    String hash(String plain, String salt);

    boolean verify(String plain, String salt, String expectedHash);

    /** Sal nueva para un usuario que se crea o cambia su contrasena. */
    String nuevaSal();
}
