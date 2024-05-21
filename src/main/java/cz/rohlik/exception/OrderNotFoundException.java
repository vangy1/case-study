package cz.rohlik.exception;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(Long orderId) {
        super(String.format("Order with id of '%d' was not found", orderId));
    }
}
