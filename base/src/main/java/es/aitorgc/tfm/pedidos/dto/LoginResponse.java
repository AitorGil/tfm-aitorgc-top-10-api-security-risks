package es.aitorgc.tfm.pedidos.dto;

/**
 * Respuesta del inicio de sesión.
 *
 * @param token            JWT firmado que el cliente envía en la cabecera {@code Authorization: Bearer ...}
 * @param tipo             siempre {@code Bearer}
 * @param expiraEnSegundos segundos que faltan para que caduque el token
 */
public record LoginResponse(String token, String tipo, long expiraEnSegundos) {
}
