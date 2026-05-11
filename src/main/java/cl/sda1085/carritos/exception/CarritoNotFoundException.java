package cl.sda1085.carritos.exception;

public class CarritoNotFoundException extends RuntimeException {
    public CarritoNotFoundException(Long id) {
        super("No se encontró el item en el carrito con el ID: " + id);
    }
}
