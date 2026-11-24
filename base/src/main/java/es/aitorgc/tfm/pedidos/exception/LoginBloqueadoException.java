package es.aitorgc.tfm.pedidos.exception;

import java.time.Duration;

/**
 * Se han superado los intentos fallidos de login permitidos para un usuario. Se responde 429
 * con la cabecera {@code Retry-After}, que indica cuántos segundos faltan para poder reintentar.
 */
public class LoginBloqueadoException extends RuntimeException {

    private final Duration tiempoRestante;

    public LoginBloqueadoException(Duration tiempoRestante) {
        super("Demasiados intentos fallidos. Vuelve a intentarlo más tarde");
        this.tiempoRestante = tiempoRestante;
    }

    public Duration getTiempoRestante() {
        return tiempoRestante;
    }
}
