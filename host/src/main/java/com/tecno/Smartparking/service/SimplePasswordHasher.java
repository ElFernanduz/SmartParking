package com.tecno.Smartparking.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;
import org.springframework.stereotype.Component;

/** SHA-256 sobre sal mas contrasena. Suficiente para el alcance academico. */
@Component
public class SimplePasswordHasher implements PasswordHasher {

    private static final int BYTES_DE_SAL = 16;

    private final SecureRandom aleatorio = new SecureRandom();

    @Override
    public String hash(String plain, String salt) {
        try {
            MessageDigest digestor = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digestor.digest((salt + plain).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no esta disponible en esta plataforma", e);
        }
    }

    @Override
    public boolean verify(String plain, String salt, String expectedHash) {
        // Comparacion de tiempo constante: no filtra cuantos caracteres coinciden.
        byte[] calculado = hash(plain, salt).getBytes(StandardCharsets.UTF_8);
        byte[] esperado = expectedHash.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(calculado, esperado);
    }

    @Override
    public String nuevaSal() {
        byte[] sal = new byte[BYTES_DE_SAL];
        aleatorio.nextBytes(sal);
        return HexFormat.of().formatHex(sal);
    }
}
