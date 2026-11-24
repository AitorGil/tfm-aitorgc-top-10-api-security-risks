package es.aitorgc.tfm.pedidos.dto;

import es.aitorgc.tfm.pedidos.domain.Rol;
import es.aitorgc.tfm.pedidos.domain.Usuario;

/** Datos públicos de un usuario. No incluye el hash de la contraseña. */
public record UsuarioResponse(Long id, String username, String nombre, String email, Rol rol) {

    public static UsuarioResponse desde(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRol());
    }
}
