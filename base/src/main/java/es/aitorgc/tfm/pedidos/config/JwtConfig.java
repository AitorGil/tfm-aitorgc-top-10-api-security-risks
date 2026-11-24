package es.aitorgc.tfm.pedidos.config;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/**
 * Codificador y decodificador de JWT con una clave simétrica (HS256).
 * La misma clave firma los tokens en el login y comprueba su firma en cada petición.
 */
@Configuration
public class JwtConfig {

    private final SecretKey clave;

    public JwtConfig(JwtProperties propiedades) {
        this.clave = new SecretKeySpec(propiedades.secreto().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    /** Firma los tokens que se emiten en el login. */
    @Bean
    public JwtEncoder jwtEncoder() {
        return NimbusJwtEncoder.withSecretKey(clave).algorithm(MacAlgorithm.HS256).build();
    }

    /**
     * Valida los tokens recibidos: comprueba la firma con HS256 y, por defecto, también la caducidad ({@code exp}).
     * Un token sin firma, con otro algoritmo o caducado se rechaza con 401.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(clave).macAlgorithm(MacAlgorithm.HS256).build();
    }
}
