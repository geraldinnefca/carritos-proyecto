package cl.sda1085.carritos.controller;

import cl.sda1085.carritos.dto.CarritoRequestDTO;
import cl.sda1085.carritos.dto.CarritoResponseDTO;
import cl.sda1085.carritos.service.CarritoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/carritos")
@RequiredArgsConstructor

public class CarritoController {

    private final CarritoService carritoService;

    @GetMapping
    public ResponseEntity<List<CarritoResponseDTO>> obtenerTodos() {
        return ResponseEntity.ok(carritoService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarritoResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(carritoService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<CarritoResponseDTO> agregarItem(@Valid @RequestBody CarritoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(carritoService.guardar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CarritoResponseDTO> actualizarItem(@PathVariable Long id, @Valid @RequestBody CarritoRequestDTO dto) {
        return ResponseEntity.ok(carritoService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarItem(@PathVariable Long id) {
        carritoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    //Métodos personalizados

    //Ver el carrito de un usuario específico
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<CarritoResponseDTO>> obtenerCarritoPorUsuario(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(carritoService.obtenerCarritoPorIdUsuario(idUsuario));
    }

    //Vaciar todo el carrito de un usuario
    @DeleteMapping("/usuario/{idUsuario}")
    public ResponseEntity<Void> vaciarCarrito(@PathVariable Long idUsuario) {
        carritoService.vaciarCarrito(idUsuario);
        return ResponseEntity.noContent().build();
    }

    //Obtener el monto total a pagar por el usuario
    @GetMapping("/usuario/{idUsuario}/total")
    public ResponseEntity<BigDecimal> obtenerTotalCarrito(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(carritoService.calcularTotalCarrito(idUsuario));
    }


}

