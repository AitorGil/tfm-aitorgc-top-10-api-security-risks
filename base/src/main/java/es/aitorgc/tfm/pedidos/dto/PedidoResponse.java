package es.aitorgc.tfm.pedidos.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import es.aitorgc.tfm.pedidos.domain.EstadoPedido;
import es.aitorgc.tfm.pedidos.domain.Pedido;

public record PedidoResponse(
        Long id,
        Instant fecha,
        EstadoPedido estado,
        BigDecimal total,
        List<LineaPedidoResponse> lineas) {

    public static PedidoResponse desde(Pedido pedido) {
        return new PedidoResponse(
                pedido.getId(),
                pedido.getFecha(),
                pedido.getEstado(),
                pedido.getTotal(),
                pedido.getLineas().stream().map(LineaPedidoResponse::desde).toList());
    }
}
