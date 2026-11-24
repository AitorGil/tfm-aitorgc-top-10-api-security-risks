package es.aitorgc.tfm.pedidos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Credenciales para iniciar sesión. */
public record LoginRequest(
        @NotBlank @Size(max = 50) String username,
        @NotBlank @Size(max = 100) String password) {
}
