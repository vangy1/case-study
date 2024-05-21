package cz.rohlik.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(Long productId) {
        super(String.format("Product with id of '%d' was not found", productId));
    }
}
