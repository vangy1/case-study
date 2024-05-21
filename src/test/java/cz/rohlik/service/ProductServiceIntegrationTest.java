package cz.rohlik.service;

import cz.rohlik.exception.ProductNotFoundException;
import cz.rohlik.model.ProductCreateRequest;
import cz.rohlik.model.ProductUpdateRequest;
import cz.rohlik.persistence.entity.Product;
import cz.rohlik.persistence.entity.ProductInventory;
import cz.rohlik.persistence.repository.ProductInventoryRepository;
import cz.rohlik.persistence.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("it")
public class ProductServiceIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductInventoryRepository productInventoryRepository;

    @AfterEach
    public void cleanUp() {
        productInventoryRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    public void testCreateProduct_success() {
        ProductCreateRequest request = new ProductCreateRequest().name("Product").price(5.0).inventory(10L);

        Long productId = productService.createProduct(request);

        Product product = productRepository.findById(productId).orElseThrow();
        assertEquals(request.getName(), product.getName());
        assertEquals(request.getPrice(), product.getPrice());
        ProductInventory inventory = productInventoryRepository.findByProductId(product.getId()).orElseThrow();
        assertEquals(request.getInventory(), inventory.getQuantity());
    }

    @Test
    public void testDeleteProductById_success() {
        Product product = getMockProduct("Product");
        productRepository.save(product);

        productService.deleteProductById(product.getId());

        assertTrue(productRepository.findById(product.getId()).isPresent());
        assertTrue(productRepository.findById(product.getId()).get().isDeleted());
    }

    @Test
    public void testUpdateProduct_notFound() {
        assertThrows(ProductNotFoundException.class, () -> productService.updateProduct(1L, new ProductUpdateRequest()));
    }

    @Test
    public void testUpdateProduct_allFields() {
        Product product = getMockProduct("Product");
        productRepository.save(product);

        ProductUpdateRequest request = new ProductUpdateRequest().name("Name").price(15.0);
        productService.updateProduct(product.getId(), request);

        product = productRepository.findById(product.getId()).orElseThrow();
        assertEquals(request.getName(), product.getName());
        assertEquals(request.getPrice(), product.getPrice());
    }

    private Product getMockProduct(String name){
        Product product = new Product();
        product.setName(name);
        return product;
    }
}

