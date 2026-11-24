package es.aitorgc.tfm.pedidos.service;

import java.time.Duration;
import java.time.Instant;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import es.aitorgc.tfm.pedidos.config.JwtProperties;
import es.aitorgc.tfm.pedidos.config.SecurityConfig;
import es.aitorgc.tfm.pedidos.dto.LoginRequest;
import es.aitorgc.tfm.pedidos.dto.LoginResponse;
import es.aitorgc.tfm.pedidos.exception.LoginBloqueadoException;

/** Inicio de sesión: comprueba las credenciales y emite un JWT firmado. */
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;
    private final LimitadorIntentosLogin limitadorIntentos;

    public AuthService(AuthenticationManager authenticationManager, JwtEncoder jwtEncoder,
            JwtProperties jwtProperties, LimitadorIntentosLogin limitadorIntentos) {
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
        this.jwtProperties = jwtProperties;
        this.limitadorIntentos = limitadorIntentos;
    }

    /**
     * Si el usuario está bloqueado por demasiados fallos, responde 429 sin comprobar la contraseña.
     * Si el usuario o la contraseña no son correctos, {@link AuthenticationManager}
     * lanza {@code BadCredentialsException}, se cuenta el fallo y el gestor de errores responde 401.
     */
    public LoginResponse login(LoginRequest request) {
        Duration bloqueoRestante = limitadorIntentos.tiempoBloqueoRestante(request.username());
        if (!bloqueoRestante.isZero()) {
            throw new LoginBloqueadoException(bloqueoRestante);
        }

        Authentication autenticacion;
        try {
            autenticacion = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        } catch (BadCredentialsException ex) {
            limitadorIntentos.registrarFallo(request.username());
            throw ex;
        }
        limitadorIntentos.registrarExito(request.username());

        return new LoginResponse(generarToken(autenticacion), "Bearer", jwtProperties.caducidad().toSeconds());
    }

    private String generarToken(Authentication autenticacion) {
        // El rol llega como authority "ROLE_USER" o "ROLE_ADMIN"; en el token se guarda sin el prefijo
        String rol = autenticacion.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring("ROLE_".length()))
                .findFirst()
                .orElseThrow();

        Instant ahora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(autenticacion.getName())                  // sub: nombre de usuario
                .claim(SecurityConfig.CLAIM_ROL, rol)              // rol: USER o ADMIN
                .issuedAt(ahora)                                   // iat
                .expiresAt(ahora.plus(jwtProperties.caducidad()))  // exp
                .build();
        JwsHeader cabecera = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(cabecera, claims)).getTokenValue();
    }
}
