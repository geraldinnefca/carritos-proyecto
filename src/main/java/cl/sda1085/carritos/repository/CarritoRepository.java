package cl.sda1085.carritos.repository;

import cl.sda1085.carritos.model.Carrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarritoRepository extends JpaRepository<Carrito, Long> {

    //Buscar los todos los items activos de un usuario
    List<Carrito> findByIdUsuarioAndEstado(Long idUsuario, String estado);

    //Contar cuántos items tiene el usuario en su carrito
    long countByIdUsuarioAndEstado(Long idUsuario, String estado);

    //Vaciar por completo todos los productos del carrito de un usuario
    public void deleteByIdUsuario(Long idUsuario);

    //Encontar un item específico activo
    Optional<Carrito> findByIdUsuarioAndIdProductoAndEstado(Long idUsuario, Long idProducto, String estado);
}
