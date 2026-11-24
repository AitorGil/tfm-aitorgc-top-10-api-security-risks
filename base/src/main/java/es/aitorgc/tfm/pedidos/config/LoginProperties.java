package es.aitorgc.tfm.pedidos.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Propiedades {@code app.login.*} de application.yml.
 *
 * @param maxIntentos intentos fallidos seguidos que se permiten antes de bloquear el usuario
 * @param bloqueo     tiempo que dura el bloqueo; también es el plazo tras el cual se olvidan los fallos
 */
@Validated
@ConfigurationProperties(prefix = "app.login")
public record LoginProperties(
        @Min(1) int maxIntentos,
        @NotNull Duration bloqueo) {
}
