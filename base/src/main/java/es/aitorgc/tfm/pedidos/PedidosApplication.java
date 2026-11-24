package es.aitorgc.tfm.pedidos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Punto de entrada de la API de gestión de pedidos.
 * {@code @ConfigurationPropertiesScan} registra las clases de propiedades propias, como {@code JwtProperties}.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class PedidosApplication {

    public static void main(String[] args) {
        SpringApplication.run(PedidosApplication.class, args);
    }
}
