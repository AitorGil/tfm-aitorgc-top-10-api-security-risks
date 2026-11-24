package es.aitorgc.tfm.pedidos.dto;

import java.math.BigDecimal;

import es.aitorgc.tfm.pedidos.domain.Producto;

public record ProductoResponse(Long id, String nombre, String descripcion, BigDecimal precio, Integer stock) {

    public static ProductoResponse desde(Producto producto) {
        return new ProductoResponse(
                producto.getId(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecio(),
                producto.getStock());
    }
}
