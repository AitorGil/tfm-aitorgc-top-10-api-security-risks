package es.aitorgc.tfm.pedidos.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** Datos para crear o modificar un producto. El id no se recibe: lo asigna la base de datos. */
public record ProductoRequest(
        @NotBlank @Size(max = 100) String nombre,
        @Size(max = 500) String descripcion,
        @NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal precio,
        @NotNull @PositiveOrZero @Max(100_000) Integer stock) {
}
