package es.aitorgc.tfm.pedidos.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Propietario del pedido. Se usa para comprobar que el pedido pertenece al usuario autenticado. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private Instant fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPedido estado;

    /** Lo calcula el servidor a partir de las líneas; nunca lo envía el cliente. */
    @Column(nullable = false)
    private BigDecimal total;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<LineaPedido> lineas = new ArrayList<>();

    /** Constructor vacío que exige JPA. */
    protected Pedido() {
    }

    /** Crea un pedido nuevo, pendiente, sin líneas y con total cero. */
    public Pedido(Usuario usuario) {
        this.usuario = usuario;
        this.fecha = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        this.estado = EstadoPedido.PENDIENTE;
        this.total = BigDecimal.ZERO;
    }

    /** Añade una línea al pedido con el precio actual del producto y actualiza el total. */
    public void anadirLinea(Producto producto, int cantidad) {
        LineaPedido linea = new LineaPedido(this, producto, cantidad, producto.getPrecio());
        lineas.add(linea);
        total = total.add(linea.getSubtotal());
    }

    public void cancelar() {
        this.estado = EstadoPedido.CANCELADO;
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Instant getFecha() {
        return fecha;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public List<LineaPedido> getLineas() {
        return lineas;
    }
}
