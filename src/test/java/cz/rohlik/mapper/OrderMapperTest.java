package cz.rohlik.mapper;

import cz.rohlik.model.OrderItemInsufficient;
import cz.rohlik.model.OrderItemRequest;
import cz.rohlik.persistence.entity.OrderItem;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrderMapperTest {
    private static final OrderMapper orderMapper = Mappers.getMapper(OrderMapper.class);

    @Test
    void testToOrderItems() {
        OrderItemRequest request = new OrderItemRequest().productId(1L).quantity(5L);

        OrderItem orderItem = orderMapper.toOrderItem(request);

        assertEquals(1L, orderItem.getProduct().getId());
        assertEquals(5L, orderItem.getQuantity());
    }

    @Test
    void testToOrderItemInsufficient() {
        Long productId = 1L;
        long futureInventory = 50L;

        OrderItemInsufficient orderItemInsufficient = orderMapper.toOrderItemInsufficient(productId, futureInventory);

        assertEquals(productId, orderItemInsufficient.getProductId());
        assertEquals(futureInventory, orderItemInsufficient.getMissingQuantity());
    }
}
