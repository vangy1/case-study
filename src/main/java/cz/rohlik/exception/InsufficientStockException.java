package cz.rohlik.exception;

import cz.rohlik.model.OrderItemInsufficient;
import lombok.Getter;

import java.util.List;

@Getter
public class InsufficientStockException extends RuntimeException {
    private final List<OrderItemInsufficient> orderItemInsufficientList;

    public InsufficientStockException(List<OrderItemInsufficient> orderItemInsufficientList) {
        super(String.format("Insufficient inventory for %d items", orderItemInsufficientList.size()));
        this.orderItemInsufficientList = orderItemInsufficientList;
    }

}
