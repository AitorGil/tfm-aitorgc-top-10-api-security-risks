package es.aitorgc.tfm.pedidos.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import es.aitorgc.tfm.pedidos.domain.Pedido;

/** Las consultas de pedidos filtran siempre por el propietario. */
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    /**
     * Busca un pedido por id <strong>y</strong> propietario a la vez.
     * Si el pedido existe pero es de otro usuario, el resultado es vacío,
     * igual que si no existiera.
     * {@code @EntityGraph} carga las líneas y sus productos en la misma consulta.
     */
    @EntityGraph(attributePaths = {"lineas", "lineas.producto"})
    Optional<Pedido> findByIdAndUsuarioUsername(Long id, String username);

    /**
     * Una página de pedidos de un usuario.
     *
     * <p>Aquí no se usa {@code @EntityGraph}: si la consulta trajera también las líneas, Hibernate
     * no podría paginar en la base de datos y cargaría <em>todos</em> los pedidos en memoria.
     * Las líneas se cargan después, por lotes ({@code hibernate.default_batch_fetch_size}).
     */
    Page<Pedido> findByUsuarioUsername(String username, Pageable pageable);
}
