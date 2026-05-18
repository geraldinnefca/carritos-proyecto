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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CarritoService {

    //Conexión con 'repository'
    private final CarritoRepository carritoRepository;

    private CarritoResponseDTO convertirADTO(Carrito carrito) {
        BigDecimal cantidadDecimal = new BigDecimal(carrito.getCantidad());
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


    //------------------------------
    //CRUD estándar
    //------------------------------

    //Obtener todos los carritos
    public List<CarritoResponseDTO> obtenerTodos(){
        log.info("Solicitud para obtener todos los items de todos los carritos registrados.");
        return carritoRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    //Obtener carrito por ID
    public CarritoResponseDTO obtenerPorId(Long id) {
        log.info("Buscando item de carrito con ID: {}", id);

        return carritoRepository.findById(id)
                .map(this::convertirADTO)
                .orElseThrow(() -> {
            log.warn("Búsqueda fallida: No se encontró el item de carrito con ID: {}", id);
            return new CarritoNotFoundException(id);
        });
    }

    //Guardar (crear) nuevo carrito
    @Transactional
    public CarritoResponseDTO guardar(CarritoRequestDTO dto) {
        log.info("Agregando producto ID {} al carrito del usuario ID {}", dto.getIdProducto(), dto.getIdUsuario());

        return carritoRepository.findByIdUsuarioAndIdProductoAndEstado(
                        dto.getIdUsuario(), dto.getIdProducto(), "ACTIVO")
                .map(existente -> {
                    int nuevaCantidad = existente.getCantidad() + (dto.getCantidad() != null ? dto.getCantidad() : 1);
                    existente.setCantidad(nuevaCantidad);
                    log.info("El producto ya existía en el carrito. Incrementando cantidad a: {}", nuevaCantidad);
                    return convertirADTO(carritoRepository.save(existente));
                })
                .orElseGet(() -> {
                    Carrito nuevo = new Carrito();
                    nuevo.setIdUsuario(dto.getIdUsuario());
                    nuevo.setIdProducto(dto.getIdProducto());
                    nuevo.setCantidad(dto.getCantidad() != null ? dto.getCantidad() : 1);
                    nuevo.setPrecioUnitario(dto.getPrecioUnitario());

                    //Asegurar estado inicial por defecto
                    if (nuevo.getEstado() == null) {
                        nuevo.setEstado("ACTIVO");
                    }

                    Carrito guardado = carritoRepository.save(nuevo);
                    log.info("Producto nuevo agregado exitosamente al carrito. Item ID asignado: {}", guardado.getId());
                    return convertirADTO(guardado);
                });
    }

    //Actualizar carrito existente
    @Transactional
    public CarritoResponseDTO actualizar(Long id,CarritoRequestDTO dto) {
        log.info("Solicitud de actualización manual para el item de carrito ID: {}", id);

        return carritoRepository.findById(id).map(existente -> {
            existente.setCantidad(dto.getCantidad());
            existente.setPrecioUnitario(dto.getPrecioUnitario());

            Carrito actualizado = carritoRepository.save(existente);
            log.info("Éxito: Item de carrito ID {} actualizado a cantidad: {}", id, dto.getCantidad());
            return convertirADTO(actualizado);
        })
                .orElseThrow(() -> {log.warn("Actualización denegada: El item de carrito ID {} no existe en la base de datos", id);
                    return new CarritoNotFoundException(id);
                });
    }

    //Eliminar carrito
    @Transactional
    public void eliminar(Long id) {
        log.warn("Eliminando item ID {} del carrito", id);

        if(!carritoRepository.existsById(id)) {
            throw new CarritoNotFoundException(id);
        }

        carritoRepository.deleteById(id);
        log.info("Confirmación: Item ID {} eliminado correctamente de la base de datos", id);
    }


    //------------------------------
    //CRUD personalizado
    //------------------------------

    //Buscar el carrito activo de un usuario específico
    public List<CarritoResponseDTO> obtenerCarritoPorIdUsuario(Long idUsuario, String estado) {
        log.info("Consultando historial del carrito para el usuario ID: {} con filtro de estado: '{}'", idUsuario, estado);

        return carritoRepository.findByIdUsuarioAndEstado(idUsuario, estado)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    //Contar cuántos ítems tiene el usuario en su carrito según el estado
    public long contarPorUsuarioYEstado(Long idUsuario, String estado) {
        log.info("Contando volumen de unidades en el carrito para el usuario ID: {} con estado: '{}'", idUsuario, estado);
        return carritoRepository.countByIdUsuarioAndEstado(idUsuario, estado);
    }

    //Vaciar por completo todos los productos del carrito de un usuario
    @Transactional
    public void vaciarCarritoPorIdUsuario(Long idUsuario) {
        log.warn("¡Operación Crítica! Vaciando completamente todos los registros del usuario ID: {}", idUsuario);
        carritoRepository.deleteByIdUsuario(idUsuario);
        log.info("El carrito completo del usuario ID: {} ha sido vaciado del sistema", idUsuario);
    }

    //Encontrar un ítem específico activo/estado para un usuario
    public CarritoResponseDTO buscarItemEspecifico(Long idUsuario, Long idProducto, String estado) {
        log.info("Buscando existencia de ítem único -> Usuario: {}, Producto: {}, Estado: {}", idUsuario, idProducto, estado);
        return carritoRepository.findByIdUsuarioAndIdProductoAndEstado(idUsuario, idProducto, estado)
                .map(this::convertirADTO)
                .orElseThrow(() -> {
                    log.warn("Consulta vacía: El usuario {} no posee el producto {} en estado '{}'", idUsuario, idProducto, estado);
                    return new RuntimeException("No se encontró el artículo específico solicitado dentro del carrito.");
                });
    }

    //Calcular el monto total en dinero del carrito activo
    public BigDecimal calcularTotalCarrito(Long idUsuario) {
        log.info("Calculando el monto total del carrito para el usuario ID: {}", idUsuario);

        return carritoRepository.findByIdUsuarioAndEstado(idUsuario, "ACTIVO")
                .stream()
                .map(item -> item.getPrecioUnitario().multiply(new BigDecimal(item.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
