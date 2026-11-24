package es.aitorgc.tfm.pedidos.domain;

/** Estado de un pedido. Solo los pedidos PENDIENTE se pueden cancelar. */
public enum EstadoPedido {
    PENDIENTE,
    ENVIADO,
    CANCELADO
}
