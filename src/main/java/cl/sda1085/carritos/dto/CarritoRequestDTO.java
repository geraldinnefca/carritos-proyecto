package cl.sda1085.carritos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CarritoRequestDTO {

        @NotNull(message = "ID de usuario es obligatorio")
        private Long idUsuario;

        @NotNull(message = "ID de producto es obligatorio")
        private Long idProducto;

        @Min(value = 1, message = "La cantidad mínima es 1")
        private Integer cantidad;

        @NotNull(message = "El precio es obligatorio")
        @Positive(message = "El precio debe ser mayor a cero")
        private BigDecimal precioUnitario;
}
