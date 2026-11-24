package es.aitorgc.tfm.pedidos.exception;

import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Gestor global de errores. Todas las respuestas de error usan el formato estándar
 * {@link ProblemDetail} (RFC 9457, {@code application/problem+json}):
 *
 * <pre>
 * { "title": "Not Found", "status": 404, "detail": "Pedido no encontrado" }
 * </pre>
 *
 * Nunca se devuelven trazas ni mensajes internos: los errores inesperados se registran
 * en el log y el cliente solo recibe un mensaje genérico.
 *
 * Al heredar de {@link ResponseEntityExceptionHandler}, los errores propios de Spring MVC
 * (JSON mal formado, método no permitido, etc.) también salen en este formato.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ProblemDetail recursoNoEncontrado(RecursoNoEncontradoException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(OperacionNoValidaException.class)
    public ProblemDetail operacionNoValida(OperacionNoValidaException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * Dos peticiones han modificado el mismo producto a la vez (por ejemplo, dos pedidos que compran
     * la última unidad). {@code @Version} en {@code Producto} detecta el conflicto y una de las dos falla.
     */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ProblemDetail modificacionConcurrente(ObjectOptimisticLockingFailureException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "Otra petición ha modificado los mismos datos a la vez. Vuelve a intentarlo");
    }

    /** Demasiados intentos de login fallidos: 429 e indicación de cuándo se puede reintentar. */
    @ExceptionHandler(LoginBloqueadoException.class)
    public ProblemDetail loginBloqueado(LoginBloqueadoException ex, HttpServletResponse response) {
        // Se redondea hacia arriba para no invitar a reintentar un segundo antes de tiempo
        long segundos = ex.getTiempoRestante().toSeconds() + 1;
        response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(segundos));
        return ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage());
    }

    /** Ordenación por un campo que no existe, por ejemplo {@code ?sort=inventado}. */
    @ExceptionHandler(PropertyReferenceException.class)
    public ProblemDetail ordenacionNoValida(PropertyReferenceException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Campo de ordenación no válido");
    }

    /** Usuario o contraseña incorrectos en el login. No se indica cuál de los dos falla. */
    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail credencialesIncorrectas(BadCredentialsException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos");
    }

    /**
     * Petición sin token o con un token no válido o caducado.
     * La configuración de seguridad redirige aquí estos errores (ver {@code SecurityConfig}).
     */
    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail noAutenticado(AuthenticationException ex, HttpServletResponse response) {
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED,
                "Se necesita un token de acceso válido");
    }

    /** Usuario autenticado sin permiso para la ruta (por ejemplo, un USER en una ruta de ADMIN). */
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail accesoDenegado(AccessDeniedException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "No tienes permiso para esta operación");
    }

    /** Cualquier otro error: se registra entero en el log, pero al cliente solo le llega un mensaje genérico. */
    @ExceptionHandler(Exception.class)
    public ProblemDetail errorInesperado(Exception ex) {
        log.error("Error no controlado", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");
    }

    /** Errores de Bean Validation: se añade la lista de campos no válidos con su mensaje. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> errores = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() == null ? "valor no válido" : error.getDefaultMessage(),
                        (primero, segundo) -> primero));

        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "La petición contiene datos no válidos");
        problema.setProperty("errores", errores);
        return handleExceptionInternal(ex, problema, headers, HttpStatus.BAD_REQUEST, request);
    }
}
