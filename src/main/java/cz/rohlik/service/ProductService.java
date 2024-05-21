package cz.rohlik.service;

import cz.rohlik.exception.ProductNotFoundException;
import cz.rohlik.mapper.ProductMapper;
import cz.rohlik.model.ProductCreateRequest;
import cz.rohlik.model.ProductUpdateRequest;
import cz.rohlik.persistence.entity.Product;
import cz.rohlik.persistence.entity.ProductInventory;
import cz.rohlik.persistence.repository.ProductInventoryRepository;
import cz.rohlik.persistence.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.StaleStateException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductInventoryRepository productInventoryRepository;
    private final ProductMapper productMapper;

    @Transactional
    public Long createProduct(ProductCreateRequest productCreateRequest) {
        log.info("Starting to create a new product with request: {}", productCreateRequest);
        Product product = productMapper.toProduct(productCreateRequest);
        productRepository.save(product);

        ProductInventory productInventory = new ProductInventory();
        productInventory.setQuantity(productCreateRequest.getInventory() != null ? productCreateRequest.getInventory() : 0);
        productInventory.setDescription("Initial inventory");
        productInventory.setProduct(product);
        productInventoryRepository.save(productInventory);

        log.info("Product saved with ID: {}", product);
        return product.getId();
    }

    @Transactional
    public void deleteProductById(Long productId) {
        log.info("Attempting to delete product with ID: {}", productId);
        Product product = productRepository.findById(productId).orElseThrow(() -> new ProductNotFoundException(productId));
        product.setDeleted(true);
        log.info("Product with ID: {} marked as deleted", productId);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    @Retryable(retryFor = StaleStateException.class)
    public void updateProduct(Long productId, ProductUpdateRequest productUpdateRequest) {
        log.info("Starting to update product with ID: {} with request: {}", productId, productUpdateRequest);
        Product product = productRepository.findByIdAndDeletedIsFalse(productId).orElseThrow(() -> new ProductNotFoundException(productId));

        if(productUpdateRequest.getName() != null) product.setName(productUpdateRequest.getName());
        if(productUpdateRequest.getPrice() != null) product.setPrice(productUpdateRequest.getPrice());
        if(productUpdateRequest.getInventoryChange() != null) {
            product.setInventory(product.getInventory() + productUpdateRequest.getInventoryChange().getQuantity());
            ProductInventory productInventory = productMapper.toProductInventory(productUpdateRequest.getInventoryChange());
            productInventory.setProduct(product);
            productInventoryRepository.save(productInventory);
        }

        log.info("Product with ID updated: {}", productId);
    }
}
