package cl.sda1085.carritos.service;

import cl.sda1085.carritos.exception.CarritoNotFoundException;
import cl.sda1085.carritos.repository.CarritoRepository;
import cl.sda1085.carritos.dto.CarritoRequestDTO;
import cl.sda1085.carritos.dto.CarritoResponseDTO;
import cl.sda1085.carritos.model.Carrito;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j

public class CarritoService {

    private final CarritoRepository carritoRepository;

    private CarritoResponseDTO convertirADTO(Carrito carrito) {
        //Convertir cantidad Integer a BigDecimal para poder multiplicar
        BigDecimal cantidadDecimal = new BigDecimal(carrito.getCantidad());

        //cálculo subtotal (precio * cantidad)
        BigDecimal subtotal = carrito.getPrecioUnitario().multiply(cantidadDecimal);

        return new CarritoResponseDTO(
                carrito.getId(),
                carrito.getIdUsuario(),
                carrito.getIdProducto(),
                carrito.getCantidad(),
                carrito.getPrecioUnitario(),
                subtotal,
                carrito.getEstado(),
                carrito.getFechaAgregado()
        );
    }

    public List<CarritoResponseDTO> obtenerTodos(){
        log.info("Solicitud para obtener todos los items de todos los carritos");
        return carritoRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public CarritoResponseDTO obtenerPorId(Long id) {
        log.info("Buscando item de carrito con ID: {}", id);
        return carritoRepository.findById(id)
                .map(this::convertirADTO)
                .orElseThrow(() -> {
            log.warn("Búsqueda fallida: No se encontró el item de carrito con ID: {}", id);
            return new CarritoNotFoundException(id);
        });
    }

    public CarritoResponseDTO guardar(CarritoRequestDTO dto) {
        log.info("Agregando producto ID {} al carrito del usuario ID {}", dto.getIdProducto(), dto.getIdUsuario());

        //Buscar si el producto ya existe en el carrito ACTIVO del usuario
        Optional<Carrito> itemExistente = carritoRepository.findByIdUsuarioAndIdProductoAndEstado(
                dto.getIdUsuario(), dto.getIdProducto(), "ACTIVO");

        //Si exite, actualiza cantidad
        if (itemExistente.isPresent()) {
            Carrito existente = itemExistente.get();
            int nuevaCantidad = existente.getCantidad() + (dto.getCantidad() != null ? dto.getCantidad() : 1);
            existente.setCantidad(nuevaCantidad);

            log.info("El producto ya existía. Nueva cantidad: {}", nuevaCantidad);
            return convertirADTO(carritoRepository.save(existente));
        } else { //Si no existe, creamos uno nuevo

            Carrito nuevo = new Carrito();
            nuevo.setIdUsuario(dto.getIdUsuario());
            nuevo.setIdProducto(dto.getIdProducto());
            nuevo.setCantidad(dto.getCantidad() != null ? dto.getCantidad() : 1);
            nuevo.setPrecioUnitario(dto.getPrecioUnitario());

            Carrito guardado = carritoRepository.save(nuevo);
            log.info("Producto agregado exitosamente. Item ID: {}", guardado.getId());
            return convertirADTO(guardado);
        }
    }

    public CarritoResponseDTO actualizar(Long id,CarritoRequestDTO dto) {
        log.info("Solicitud de actualización para el item de carrito ID: {}", id);

        return carritoRepository.findById(id).map(existente -> {
            existente.setCantidad(dto.getCantidad());
            existente.setPrecioUnitario(dto.getPrecioUnitario());

            Carrito actualizado = carritoRepository.save(existente);
            log.info("Éxito: Item de carrito ID {} actualizado a cantidad: {}", id, dto.getCantidad());
            return convertirADTO(actualizado);})

                .orElseThrow(() -> {log.warn("Actualización denegada: El item de carrito ID {} no existe en la base de datos", id);
                    return new CarritoNotFoundException(id);
                });
    }


    public void eliminar(Long id) {
        log.warn("Eliminando item ID {} del carrito", id);
        if(!carritoRepository.existsById(id)) throw new CarritoNotFoundException(id);
        carritoRepository.deleteById(id);

        log.info("Confirmación: Item ID {} eliminado correctamente de la base de datos", id);
    }

    //CRUD personalizado
    public List<CarritoResponseDTO> obtenerCarritoPorIdUsuario(Long idUsuario) {
        log.info("Consultando el carrito ACTIVO del usuario ID: {}", idUsuario);
        return carritoRepository.findByIdUsuarioAndEstado(idUsuario, "ACTIVO")
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void vaciarCarrito(Long idUsuario) {
        log.warn("¡ATENCIÓN! Vaciando completamente el carrito del usuario ID: {}", idUsuario);
        carritoRepository.deleteByIdUsuario(idUsuario);
        log.info("El carrito del usuario ID: {} ha sido vaciado exitosamente", idUsuario);
    }

    //obtener el total en dinero
    public BigDecimal calcularTotalCarrito(Long idUsuario) {
        log.info("Calculando el monto total del carrito para el usuario ID: {}", idUsuario);

        BigDecimal total = carritoRepository.findByIdUsuarioAndEstado(idUsuario, "ACTIVO")
                .stream()
                .map(item -> item.getPrecioUnitario().multiply(new BigDecimal(item.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.info("Cálculo completado: El total para el usuario ID {} es de ${}", idUsuario, total);
        return total;
    }
}
