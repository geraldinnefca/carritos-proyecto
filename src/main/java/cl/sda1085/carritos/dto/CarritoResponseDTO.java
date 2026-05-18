package cl.sda1085.carritos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CarritoResponseDTO {

    private Long id;
    private Long idUsuario;
    private Long idProducto;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;  //Campo calculado
    private String estado;
    private LocalDateTime fechaAgregado;
}
