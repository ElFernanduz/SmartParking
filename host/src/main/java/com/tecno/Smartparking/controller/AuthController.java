package com.tecno.Smartparking.controller;

import com.tecno.Smartparking.dto.LoginRequest;
import com.tecno.Smartparking.dto.SesionResponse;
import com.tecno.Smartparking.model.User;
import com.tecno.Smartparking.security.SesionOperador;
import com.tecno.Smartparking.service.AuthenticationService;
import com.tecno.Smartparking.service.AuthorizationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationService autenticacion;
    private final AuthorizationService autorizacion;

    public AuthController(AuthenticationService autenticacion, AuthorizationService autorizacion) {
        this.autenticacion = autenticacion;
        this.autorizacion = autorizacion;
    }

    @PostMapping("/login")
    public ResponseEntity<SesionResponse> login(@Valid @RequestBody LoginRequest peticion,
                                                HttpServletRequest http) {
        Optional<User> autenticado = autenticacion.login(peticion.usuario(), peticion.contrasena());
        if (autenticado.isEmpty()) {
            // Mismo mensaje para usuario inexistente, clave mala o cuenta inactiva.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(SesionResponse.anonima());
        }

        User user = autenticado.get();
        SesionOperador.abrir(http, user);
        return ResponseEntity.ok(describir(user));
    }

    @PostMapping("/logout")
    public ResponseEntity<SesionResponse> logout(HttpServletRequest http) {
        SesionOperador.cerrar(http);
        return ResponseEntity.ok(SesionResponse.anonima());
    }

    @GetMapping("/sesion")
    public SesionResponse sesion(HttpServletRequest http) {
        User user = SesionOperador.usuarioDe(http);
        return user == null ? SesionResponse.anonima() : describir(user);
    }

    private SesionResponse describir(User user) {
        return new SesionResponse(true, user.getUsername(), user.getEmail(),
                autorizacion.rolesDe(user), autorizacion.permisosDe(user));
    }
}
