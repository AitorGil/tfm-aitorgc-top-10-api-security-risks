package es.aitorgc.tfm.pedidos.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.aitorgc.tfm.pedidos.domain.Usuario;
import es.aitorgc.tfm.pedidos.dto.ActualizarPerfilRequest;
import es.aitorgc.tfm.pedidos.dto.UsuarioResponse;
import es.aitorgc.tfm.pedidos.exception.RecursoNoEncontradoException;
import es.aitorgc.tfm.pedidos.repository.UsuarioRepository;

/** Perfil del usuario autenticado y listado de usuarios para administración. */
@Service
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public UsuarioResponse obtenerPerfil(String username) {
        return UsuarioResponse.desde(buscar(username));
    }

    /** Solo se modifican el nombre y el email; el rol y el resto de campos no se pueden cambiar. */
    @Transactional
    public UsuarioResponse actualizarPerfil(String username, ActualizarPerfilRequest request) {
        Usuario usuario = buscar(username);
        usuario.setNombre(request.nombre());
        usuario.setEmail(request.email());
        return UsuarioResponse.desde(usuario);
    }

    /**
     * Listado paginado de todos los usuarios, ordenado por id. Solo para ADMIN (lo exige {@code SecurityConfig}).
     * La ordenación es fija, para que el cliente no pueda ordenar por campos internos como {@code passwordHash}.
     */
    public Page<UsuarioResponse> listarTodos(Pageable pageable) {
        Pageable conOrdenFijo = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("id"));
        return usuarioRepository.findAll(conOrdenFijo).map(UsuarioResponse::desde);
    }

    private Usuario buscar(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }
}
