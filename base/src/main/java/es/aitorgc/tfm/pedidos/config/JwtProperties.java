package es.aitorgc.tfm.pedidos.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Propiedades {@code app.jwt.*} de application.yml.
 *
 * @param secreto   clave para firmar los tokens con HS256; debe tener al menos 32 caracteres (256 bits).
 *                  Se lee de la variable de entorno {@code JWT_SECRETO}.
 * @param caducidad tiempo de validez de cada token (por defecto, 30 minutos)
 */
@Validated
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        @NotBlank @Size(min = 32) String secreto,
        @NotNull Duration caducidad) {
}
