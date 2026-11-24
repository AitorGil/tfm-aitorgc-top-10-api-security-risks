package es.aitorgc.tfm.pedidos.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.aitorgc.tfm.pedidos.dto.ActualizarPerfilRequest;
import es.aitorgc.tfm.pedidos.dto.UsuarioResponse;
import es.aitorgc.tfm.pedidos.service.UsuarioService;
import jakarta.validation.Valid;

/** Perfil del usuario autenticado. Con {@code /me} no hace falta pasar ningún id de usuario. */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/me")
    public UsuarioResponse obtenerPerfil(@AuthenticationPrincipal Jwt jwt) {
        return usuarioService.obtenerPerfil(jwt.getSubject());
    }

    /** Solo cambia el nombre y el email; cualquier otro campo del JSON se ignora. */
    @PutMapping("/me")
    public UsuarioResponse actualizarPerfil(@Valid @RequestBody ActualizarPerfilRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return usuarioService.actualizarPerfil(jwt.getSubject(), request);
    }
}
