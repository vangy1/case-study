package cz.rohlik.mapper;

import cz.rohlik.model.ProductCreateRequest;
import cz.rohlik.model.ProductInventoryChange;
import cz.rohlik.persistence.entity.Product;
import cz.rohlik.persistence.entity.ProductInventory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(target = "id", ignore = true)
    Product toProduct(ProductCreateRequest productCreateRequest);

    @Mapping(target = "id", ignore = true)
    ProductInventory toProductInventory(ProductInventoryChange productInventoryChange);
}
