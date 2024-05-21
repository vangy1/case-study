package cz.rohlik.mapper;

import cz.rohlik.model.OrderItemInsufficient;
import cz.rohlik.model.OrderItemRequest;
import cz.rohlik.persistence.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(source = "productId", target = "product.id")
    OrderItem toOrderItem(OrderItemRequest orderItems);

    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "missingQuantity", source = "futureInventory")
    OrderItemInsufficient toOrderItemInsufficient(Long productId, long futureInventory);
}
