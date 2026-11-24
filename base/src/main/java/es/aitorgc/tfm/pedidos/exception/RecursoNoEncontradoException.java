package es.aitorgc.tfm.pedidos.exception;

/**
 * El recurso no existe o el usuario no tiene acceso a él. En los dos casos se responde 404,
 * para no revelar si un recurso ajeno existe.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
