package es.aitorgc.tfm.pedidos.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import es.aitorgc.tfm.pedidos.domain.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
}
