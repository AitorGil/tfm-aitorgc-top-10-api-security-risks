package es.aitorgc.tfm.pedidos.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.aitorgc.tfm.pedidos.domain.Producto;
import es.aitorgc.tfm.pedidos.dto.ProductoRequest;
import es.aitorgc.tfm.pedidos.dto.ProductoResponse;
import es.aitorgc.tfm.pedidos.exception.OperacionNoValidaException;
import es.aitorgc.tfm.pedidos.exception.RecursoNoEncontradoException;
import es.aitorgc.tfm.pedidos.repository.LineaPedidoRepository;
import es.aitorgc.tfm.pedidos.repository.ProductoRepository;

/** Catálogo de productos. Quién puede crear, modificar o eliminar lo decide {@code SecurityConfig}. */
@Service
@Transactional(readOnly = true)
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final LineaPedidoRepository lineaPedidoRepository;

    public ProductoService(ProductoRepository productoRepository, LineaPedidoRepository lineaPedidoRepository) {
        this.productoRepository = productoRepository;
        this.lineaPedidoRepository = lineaPedidoRepository;
    }

    /** El tamaño de página está limitado por {@code spring.data.web.pageable.max-page-size}. */
    public Page<ProductoResponse> listar(Pageable pageable) {
        return productoRepository.findAll(pageable).map(ProductoResponse::desde);
    }

    public ProductoResponse obtener(Long id) {
        return ProductoResponse.desde(buscar(id));
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        Producto producto = new Producto(request.nombre(), request.descripcion(), request.precio(), request.stock());
        return ProductoResponse.desde(productoRepository.save(producto));
    }

    @Transactional
    public ProductoResponse actualizar(Long id, ProductoRequest request) {
        Producto producto = buscar(id);
        producto.setNombre(request.nombre());
        producto.setDescripcion(request.descripcion());
        producto.setPrecio(request.precio());
        producto.setStock(request.stock());
        return ProductoResponse.desde(producto);
    }

    @Transactional
    public void eliminar(Long id) {
        Producto producto = buscar(id);
        if (lineaPedidoRepository.existsByProductoId(id)) {
            throw new OperacionNoValidaException("No se puede eliminar un producto que aparece en pedidos");
        }
        productoRepository.delete(producto);
    }

    private Producto buscar(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));
    }
}
