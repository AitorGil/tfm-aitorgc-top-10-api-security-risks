package es.aitorgc.tfm.pedidos.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;

import jakarta.servlet.DispatcherType;

/**
 * Configuración de Spring Security.
 *
 * <ul>
 *   <li>API sin estado: sin sesiones y sin CSRF, porque solo se autentica con un token Bearer.</li>
 *   <li>Servidor de recursos OAuth 2.0: cada petición debe llevar un JWT válido (ver {@link JwtConfig}).</li>
 *   <li>Reglas por ruta y rol para todos los endpoints, y <strong>denegación por defecto</strong>
 *       de cualquier ruta que no aparezca en la lista.</li>
 * </ul>
 *
 * Las comprobaciones que dependen de los datos (que un pedido sea del usuario) no van aquí,
 * sino en los servicios.
 */
@Configuration
public class SecurityConfig {

    /** Nombre del claim del JWT que guarda el rol del usuario. */
    public static final String CLAIM_ROL = "rol";

    private static final String USER = "USER";
    private static final String ADMIN = "ADMIN";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) throws Exception {

        // Los errores 401 y 403 de Spring Security se envían al GlobalExceptionHandler,
        // para que tengan el mismo formato JSON que el resto de errores.
        AuthenticationEntryPoint sinAutenticar = (request, response, ex) ->
                resolver.resolveException(request, response, null, ex);
        AccessDeniedHandler sinPermiso = (request, response, ex) ->
                resolver.resolveException(request, response, null, ex);

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Reenvíos internos a /error: se permiten para no ocultar el error original con un 403
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()

                        // Rutas públicas
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()

                        // Productos: consulta para cualquier usuario; altas, cambios y bajas solo ADMIN
                        .requestMatchers(HttpMethod.GET, "/api/productos", "/api/productos/*").hasAnyRole(USER, ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/productos").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/api/productos/*").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.DELETE, "/api/productos/*").hasRole(ADMIN)

                        // Pedidos: cualquier usuario autenticado; el servicio comprueba el propietario
                        .requestMatchers(HttpMethod.GET, "/api/pedidos", "/api/pedidos/*").hasAnyRole(USER, ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/pedidos", "/api/pedidos/*/cancelacion").hasAnyRole(USER, ADMIN)

                        // Perfil propio
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/me").hasAnyRole(USER, ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/api/usuarios/me").hasAnyRole(USER, ADMIN)

                        // Administración
                        .requestMatchers(HttpMethod.GET, "/api/admin/usuarios").hasRole(ADMIN)

                        // Denegación por defecto: todo lo que no está arriba se rechaza
                        .anyRequest().denyAll())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                        .authenticationEntryPoint(sinAutenticar)
                        .accessDeniedHandler(sinPermiso))
                .exceptionHandling(excepciones -> excepciones
                        .authenticationEntryPoint(sinAutenticar)
                        .accessDeniedHandler(sinPermiso));

        return http.build();
    }

    /**
     * Convierte el claim {@code rol} del JWT (por ejemplo, {@code "ADMIN"}) en la authority
     * {@code ROLE_ADMIN}, que es la que comprueban {@code hasRole} y {@code hasAnyRole}.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName(CLAIM_ROL);
        authorities.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }

    /** Las contraseñas se guardan y se comprueban con BCrypt. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Comprueba usuario y contraseña en el login, con los usuarios de la base de datos. */
    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }
}
