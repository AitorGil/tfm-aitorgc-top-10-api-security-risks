package es.aitorgc.tfm.pedidos.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.aitorgc.tfm.pedidos.dto.LoginRequest;
import es.aitorgc.tfm.pedidos.dto.LoginResponse;
import es.aitorgc.tfm.pedidos.service.AuthService;
import jakarta.validation.Valid;

/** Inicio de sesión. Es el único endpoint de negocio que no necesita token. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** Devuelve un JWT si el usuario y la contraseña son correctos; si no, 401. */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
