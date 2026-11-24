package es.aitorgc.tfm.pedidos.dto;

import java.util.List;

import org.springframework.data.domain.Page;

/**
 * Página de resultados con un formato JSON estable, en lugar de serializar {@link Page} directamente.
 *
 * @param contenido      elementos de la página
 * @param pagina         número de página (empieza en 0)
 * @param tamano         tamaño de página aplicado
 * @param totalElementos número total de elementos
 * @param totalPaginas   número total de páginas
 */
public record PaginaResponse<T>(
        List<T> contenido,
        int pagina,
        int tamano,
        long totalElementos,
        int totalPaginas) {

    public static <T> PaginaResponse<T> desde(Page<T> page) {
        return new PaginaResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}
