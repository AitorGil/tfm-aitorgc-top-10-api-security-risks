package es.aitorgc.tfm.pedidos.controller;

import java.net.URI;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.aitorgc.tfm.pedidos.dto.PaginaResponse;
import es.aitorgc.tfm.pedidos.dto.ProductoRequest;
import es.aitorgc.tfm.pedidos.dto.ProductoResponse;
import es.aitorgc.tfm.pedidos.service.ProductoService;
import jakarta.validation.Valid;

/**
 * Catálogo de productos. Cualquier usuario autenticado puede consultarlo;
 * crear, modificar y eliminar solo puede hacerlo ADMIN (reglas en {@code SecurityConfig}).
 */
@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    /**
     * Lista paginada: {@code ?page=0&size=20&sort=precio,desc}.
     * Si el cliente pide más de 50 elementos por página, se aplican 50.
     */
    @GetMapping
    public PaginaResponse<ProductoResponse> listar(@PageableDefault(sort = "id") Pageable pageable) {
        return PaginaResponse.desde(productoService.listar(pageable));
    }

    @GetMapping("/{id}")
    public ProductoResponse obtener(@PathVariable Long id) {
        return productoService.obtener(id);
    }

    /** Responde 201 con la cabecera {@code Location} del producto creado. */
    @PostMapping
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest request) {
        ProductoResponse creado = productoService.crear(request);
        return ResponseEntity.created(URI.create("/api/productos/" + creado.id())).body(creado);
    }

    @PutMapping("/{id}")
    public ProductoResponse actualizar(@PathVariable Long id, @Valid @RequestBody ProductoRequest request) {
        return productoService.actualizar(id, request);
    }

    /** Responde 204 sin cuerpo. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
