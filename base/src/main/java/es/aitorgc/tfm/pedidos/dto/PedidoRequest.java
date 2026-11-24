package es.aitorgc.tfm.pedidos.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Datos para crear un pedido. Solo incluye las líneas: el propietario es el usuario autenticado,
 * y el estado, la fecha y el total los calcula el servidor.
 */
public record PedidoRequest(
        @NotEmpty @Size(max = 20) List<@NotNull @Valid LineaPedidoRequest> lineas) {
}
