package es.aitorgc.tfm.pedidos.controller;

import java.net.URI;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.aitorgc.tfm.pedidos.dto.PaginaResponse;
import es.aitorgc.tfm.pedidos.dto.PedidoRequest;
import es.aitorgc.tfm.pedidos.dto.PedidoResponse;
import es.aitorgc.tfm.pedidos.service.PedidoService;
import jakarta.validation.Valid;

/**
 * Pedidos del usuario autenticado.
 *
 * <p>El usuario se obtiene siempre del JWT ({@code jwt.getSubject()}), nunca de la URL ni del cuerpo.
 * El servicio usa ese nombre de usuario para comprobar que el pedido le pertenece.
 */
@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    /** Lista paginada ({@code ?page=0&size=20}, máximo 50), del pedido más reciente al más antiguo. */
    @GetMapping
    public PaginaResponse<PedidoResponse> listarMisPedidos(Pageable pageable, @AuthenticationPrincipal Jwt jwt) {
        return PaginaResponse.desde(pedidoService.listarMisPedidos(jwt.getSubject(), pageable));
    }

    /** Si el pedido no existe o es de otro usuario, 404. */
    @GetMapping("/{id}")
    public PedidoResponse obtener(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return pedidoService.obtener(id, jwt.getSubject());
    }

    /** Responde 201 con la cabecera {@code Location} del pedido creado. */
    @PostMapping
    public ResponseEntity<PedidoResponse> crear(@Valid @RequestBody PedidoRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        PedidoResponse creado = pedidoService.crear(request, jwt.getSubject());
        return ResponseEntity.created(URI.create("/api/pedidos/" + creado.id())).body(creado);
    }

    /** Cancela un pedido pendiente del usuario. Si ya está enviado o cancelado, 409. */
    @PostMapping("/{id}/cancelacion")
    public PedidoResponse cancelar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return pedidoService.cancelar(id, jwt.getSubject());
    }
}
