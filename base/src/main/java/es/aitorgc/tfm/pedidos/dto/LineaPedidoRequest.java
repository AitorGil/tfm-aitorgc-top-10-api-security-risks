package es.aitorgc.tfm.pedidos.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Una línea del pedido: qué producto y cuántas unidades. El precio lo pone el servidor. */
public record LineaPedidoRequest(
        @NotNull Long productoId,
        @NotNull @Min(1) @Max(100) Integer cantidad) {
}
