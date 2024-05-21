package cz.rohlik.controller;

import cz.rohlik.api.OrderApi;
import cz.rohlik.model.OrderCreateRequest;
import cz.rohlik.model.OrderCreateResponse;
import cz.rohlik.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController implements OrderApi {

    private final OrderService orderService;

    @Override
    public ResponseEntity<OrderCreateResponse> createOrder(OrderCreateRequest orderCreateRequest) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new OrderCreateResponse().id(orderService.createOrder(orderCreateRequest)));
    }

    @Override
    public ResponseEntity<Void> cancelOrder(Long orderId) {
        orderService.cancelOrder(orderId);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> payOrder(Long orderId) {
        orderService.payOrder(orderId);
        return ResponseEntity.ok().build();
    }
}
