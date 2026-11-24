package es.aitorgc.tfm.pedidos.dto;

import java.math.BigDecimal;

import es.aitorgc.tfm.pedidos.domain.LineaPedido;

public record LineaPedidoResponse(
        Long productoId,
        String nombreProducto,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal) {

    public static LineaPedidoResponse desde(LineaPedido linea) {
        return new LineaPedidoResponse(
                linea.getProducto().getId(),
                linea.getProducto().getNombre(),
                linea.getCantidad(),
                linea.getPrecioUnitario(),
                linea.getSubtotal());
    }
}
