package cz.rohlik.mapper;

import cz.rohlik.model.ProductCreateRequest;
import cz.rohlik.model.ProductInventoryChange;
import cz.rohlik.persistence.entity.Product;
import cz.rohlik.persistence.entity.ProductInventory;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProductMapperTest {
    private static final ProductMapper productMapper = Mappers.getMapper(ProductMapper.class);

    @Test
    public void testProductCreateRequestToProductMapping() {
        ProductCreateRequest request = new ProductCreateRequest().name("Product").price(10.99).inventory(10L);

        Product product = productMapper.toProduct(request);

        assertEquals(request.getName(), product.getName());
        assertEquals(request.getPrice(), product.getPrice());
        assertEquals(request.getInventory(), product.getInventory());
    }

    @Test
    public void testProductInventoryChangeToProductInventoryMapping() {
        ProductInventoryChange change = new ProductInventoryChange().quantity(5L).description("Description");

        ProductInventory inventory = productMapper.toProductInventory(change);

        assertEquals(change.getQuantity(), inventory.getQuantity());
        assertEquals(change.getDescription(), inventory.getDescription());
    }
}
