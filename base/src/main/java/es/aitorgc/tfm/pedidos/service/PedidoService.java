package es.aitorgc.tfm.pedidos.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.aitorgc.tfm.pedidos.domain.EstadoPedido;
import es.aitorgc.tfm.pedidos.domain.LineaPedido;
import es.aitorgc.tfm.pedidos.domain.Pedido;
import es.aitorgc.tfm.pedidos.domain.Producto;
import es.aitorgc.tfm.pedidos.domain.Usuario;
import es.aitorgc.tfm.pedidos.dto.LineaPedidoRequest;
import es.aitorgc.tfm.pedidos.dto.PedidoRequest;
import es.aitorgc.tfm.pedidos.dto.PedidoResponse;
import es.aitorgc.tfm.pedidos.exception.OperacionNoValidaException;
import es.aitorgc.tfm.pedidos.exception.RecursoNoEncontradoException;
import es.aitorgc.tfm.pedidos.repository.PedidoRepository;
import es.aitorgc.tfm.pedidos.repository.ProductoRepository;
import es.aitorgc.tfm.pedidos.repository.UsuarioRepository;

/**
 * Pedidos del usuario autenticado.
 *
 * <p>Todos los métodos reciben el {@code username} del usuario autenticado (el {@code sub} del JWT),
 * nunca un id de usuario enviado por el cliente. Las operaciones sobre un pedido concreto lo buscan
 * por id <strong>y</strong> propietario a la vez: si el pedido es de otro usuario, la respuesta es 404,
 * igual que si no existiera.
 */
@Service
@Transactional(readOnly = true)
public class PedidoService {

    /** Orden fijo de los listados: del pedido más reciente al más antiguo. */
    private static final Sort ORDEN = Sort.by(Sort.Direction.DESC, "fecha", "id");

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    public PedidoService(PedidoRepository pedidoRepository, ProductoRepository productoRepository,
            UsuarioRepository usuarioRepository) {
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Una página de pedidos del usuario. Se respetan la página y el tamaño que pide el cliente
     * (como máximo 50), pero no su ordenación: con {@code ?sort=} podría ordenar por cualquier
     * campo, incluso de entidades relacionadas como {@code usuario.passwordHash}.
     */
    public Page<PedidoResponse> listarMisPedidos(String username, Pageable pageable) {
        Pageable conOrdenFijo = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), ORDEN);
        return pedidoRepository.findByUsuarioUsername(username, conOrdenFijo).map(PedidoResponse::desde);
    }

    public PedidoResponse obtener(Long id, String username) {
        return PedidoResponse.desde(buscarPedidoDelUsuario(id, username));
    }

    /**
     * Crea un pedido para el usuario autenticado. El precio de cada línea se toma del catálogo,
     * el total lo calcula el servidor y el pedido empieza siempre en estado PENDIENTE.
     */
    @Transactional
    public PedidoResponse crear(PedidoRequest request, String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        Pedido pedido = new Pedido(usuario);
        for (LineaPedidoRequest lineaRequest : request.lineas()) {
            Producto producto = productoRepository.findById(lineaRequest.productoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "Producto no encontrado: " + lineaRequest.productoId()));

            if (producto.getStock() < lineaRequest.cantidad()) {
                throw new OperacionNoValidaException("No hay stock suficiente de: " + producto.getNombre());
            }
            producto.retirarStock(lineaRequest.cantidad());
            pedido.anadirLinea(producto, lineaRequest.cantidad());
        }

        return PedidoResponse.desde(pedidoRepository.save(pedido));
    }

    /** Cancela un pedido del usuario, solo si está PENDIENTE, y devuelve las unidades al stock. */
    @Transactional
    public PedidoResponse cancelar(Long id, String username) {
        Pedido pedido = buscarPedidoDelUsuario(id, username);

        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            throw new OperacionNoValidaException("Solo se pueden cancelar pedidos pendientes");
        }
        for (LineaPedido linea : pedido.getLineas()) {
            linea.getProducto().devolverStock(linea.getCantidad());
        }
        pedido.cancelar();

        return PedidoResponse.desde(pedido);
    }

    /** Comprobación del propietario: busca por id y usuario a la vez; si no coincide, 404. */
    private Pedido buscarPedidoDelUsuario(Long id, String username) {
        return pedidoRepository.findByIdAndUsuarioUsername(id, username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado"));
    }
}
