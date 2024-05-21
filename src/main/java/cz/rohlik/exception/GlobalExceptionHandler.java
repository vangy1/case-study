package cz.rohlik.exception;

import cz.rohlik.model.OrderCreateInsufficientInventoryResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<OrderCreateInsufficientInventoryResponse> handleProductNotFoundException(InsufficientStockException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new OrderCreateInsufficientInventoryResponse().orderItems(exception.getOrderItemInsufficientList()));
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<String> handleProductNotFoundException(OrderNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<String> handleProductNotFoundException(ProductNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }
}
