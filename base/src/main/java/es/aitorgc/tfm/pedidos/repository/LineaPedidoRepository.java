package es.aitorgc.tfm.pedidos.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import es.aitorgc.tfm.pedidos.domain.LineaPedido;

public interface LineaPedidoRepository extends JpaRepository<LineaPedido, Long> {

    /** Indica si algún pedido incluye el producto (en ese caso no se puede eliminar). */
    boolean existsByProductoId(Long productoId);
}
