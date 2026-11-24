package es.aitorgc.tfm.pedidos.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import es.aitorgc.tfm.pedidos.repository.UsuarioRepository;

/** Carga los usuarios de la base de datos para que Spring Security compruebe la contraseña en el login. */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .map(usuario -> User.withUsername(usuario.getUsername())
                        .password(usuario.getPasswordHash())
                        .roles(usuario.getRol().name())
                        .build())
                // Spring Security convierte esta excepción en BadCredentialsException,
                // para no revelar si el usuario existe
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }
}
