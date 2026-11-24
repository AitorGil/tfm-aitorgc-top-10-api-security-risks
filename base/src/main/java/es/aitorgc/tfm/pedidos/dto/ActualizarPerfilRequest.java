package es.aitorgc.tfm.pedidos.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Campos del perfil que puede modificar el propio usuario: solo el nombre y el email. */
public record ActualizarPerfilRequest(
        @NotBlank @Size(max = 100) String nombre,
        @NotBlank @Email @Size(max = 150) String email) {
}
