package es.aitorgc.tfm.pedidos.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.aitorgc.tfm.pedidos.dto.PaginaResponse;
import es.aitorgc.tfm.pedidos.dto.UsuarioResponse;
import es.aitorgc.tfm.pedidos.service.UsuarioService;

/** Endpoints de administración. Solo puede usarlos el rol ADMIN (regla en {@code SecurityConfig}). */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UsuarioService usuarioService;

    public AdminController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /** Lista paginada ({@code ?page=0&size=20}, máximo 50), ordenada por id. */
    @GetMapping("/usuarios")
    public PaginaResponse<UsuarioResponse> listarUsuarios(Pageable pageable) {
        return PaginaResponse.desde(usuarioService.listarTodos(pageable));
    }
}
