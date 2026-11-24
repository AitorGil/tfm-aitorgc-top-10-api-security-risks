package es.aitorgc.tfm.pedidos.exception;

/**
 * La operación no se puede hacer en el estado actual de los datos
 * (por ejemplo, cancelar un pedido ya enviado o pedir más unidades de las que hay). Se responde 409.
 */
public class OperacionNoValidaException extends RuntimeException {

    public OperacionNoValidaException(String mensaje) {
        super(mensaje);
    }
}
