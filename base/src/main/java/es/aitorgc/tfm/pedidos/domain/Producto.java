package es.aitorgc.tfm.pedidos.domain;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    @Column(nullable = false)
    private BigDecimal precio;

    @Column(nullable = false)
    private Integer stock;

    /**
     * Bloqueo optimista. Hibernate incrementa este número en cada modificación y solo guarda
     * los cambios si nadie ha modificado el producto desde que se leyó. Así, si dos pedidos
     * compran a la vez la última unidad, uno de los dos falla en lugar de dejar el stock mal.
     */
    @Version
    private Long version;

    /** Constructor vacío que exige JPA. */
    protected Producto() {
    }

    public Producto(String nombre, String descripcion, BigDecimal precio, Integer stock) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stock = stock;
    }

    /** Resta unidades del stock. El servicio comprueba antes que hay unidades suficientes. */
    public void retirarStock(int cantidad) {
        this.stock -= cantidad;
    }

    /** Devuelve unidades al stock, por ejemplo al cancelar un pedido. */
    public void devolverStock(int cantidad) {
        this.stock += cantidad;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }
}
