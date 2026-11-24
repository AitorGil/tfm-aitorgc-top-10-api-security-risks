package es.aitorgc.tfm.pedidos.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import es.aitorgc.tfm.pedidos.config.LoginProperties;

/**
 * Contador sencillo de intentos fallidos de login, para frenar los ataques de fuerza bruta.
 *
 * <p>Funcionamiento, con los valores por defecto (5 intentos, 15 minutos):
 * <ul>
 *   <li>Cada fallo de un nombre de usuario suma uno al contador.</li>
 *   <li>Al quinto fallo, ese usuario queda bloqueado durante 15 minutos, aunque después
 *       envíe la contraseña correcta.</li>
 *   <li>Un login correcto pone el contador a cero.</li>
 *   <li>Los fallos se olvidan si pasan 15 minutos sin llegar al límite.</li>
 * </ul>
 *
 * <p>Se cuentan también los fallos de usuarios que no existen. Si no, un atacante podría
 * averiguar qué usuarios existen viendo cuáles llegan a bloquearse.
 *
 * <p>Limitaciones, aceptables en una aplicación con una sola instancia:
 * <ul>
 *   <li>El contador está en memoria: se pierde al reiniciar y no se comparte entre instancias.</li>
 *   <li>Bloquear por usuario permite que alguien bloquee a propósito la cuenta de otra persona
 *       durante el tiempo de bloqueo. Es el precio de una solución sencilla.</li>
 * </ul>
 */
@Component
public class LimitadorIntentosLogin {

    /** A partir de este número de entradas se borran las caducadas, para que el mapa no crezca sin límite. */
    private static final int ENTRADAS_ANTES_DE_LIMPIAR = 10_000;

    /**
     * Fallos acumulados de un usuario.
     *
     * @param fallos  número de fallos seguidos
     * @param caducan momento en que se olvidan los fallos (o termina el bloqueo, si se llegó al límite)
     */
    private record Intentos(int fallos, Instant caducan) {

        boolean caducados(Instant ahora) {
            return !ahora.isBefore(caducan);
        }
    }

    private final Map<String, Intentos> intentosPorUsuario = new ConcurrentHashMap<>();
    private final LoginProperties propiedades;

    public LimitadorIntentosLogin(LoginProperties propiedades) {
        this.propiedades = propiedades;
    }

    /** Tiempo que le queda de bloqueo al usuario, o {@link Duration#ZERO} si no está bloqueado. */
    public Duration tiempoBloqueoRestante(String username) {
        Instant ahora = Instant.now();
        Intentos intentos = intentosPorUsuario.get(username);
        if (intentos == null || intentos.caducados(ahora) || intentos.fallos() < propiedades.maxIntentos()) {
            return Duration.ZERO;
        }
        return Duration.between(ahora, intentos.caducan());
    }

    public void registrarFallo(String username) {
        Instant ahora = Instant.now();
        if (intentosPorUsuario.size() >= ENTRADAS_ANTES_DE_LIMPIAR) {
            intentosPorUsuario.values().removeIf(intentos -> intentos.caducados(ahora));
        }

        // compute() actualiza la entrada de forma atómica, aunque lleguen varias peticiones a la vez
        intentosPorUsuario.compute(username, (clave, anteriores) -> {
            if (anteriores == null || anteriores.caducados(ahora)) {
                return new Intentos(1, ahora.plus(propiedades.bloqueo()));
            }
            int fallos = anteriores.fallos() + 1;
            // Al llegar al límite, el bloqueo dura el tiempo completo desde este último fallo
            Instant caducan = fallos >= propiedades.maxIntentos()
                    ? ahora.plus(propiedades.bloqueo())
                    : anteriores.caducan();
            return new Intentos(fallos, caducan);
        });
    }

    public void registrarExito(String username) {
        intentosPorUsuario.remove(username);
    }
}
