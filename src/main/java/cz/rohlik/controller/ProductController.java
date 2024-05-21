package cz.rohlik.controller;

import cz.rohlik.api.ProductApi;
import cz.rohlik.model.ProductCreateRequest;
import cz.rohlik.model.ProductCreateResponse;
import cz.rohlik.model.ProductUpdateRequest;
import cz.rohlik.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProductController implements ProductApi {
    private final ProductService productService;

    @Override
    public ResponseEntity<ProductCreateResponse> createProduct(ProductCreateRequest productCreateRequest) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ProductCreateResponse().id(productService.createProduct(productCreateRequest)));
    }

    @Override
    public ResponseEntity<Void> deleteProductById(Long productId) {
        productService.deleteProductById(productId);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> updateProduct(Long productId, ProductUpdateRequest productUpdateRequest) {
        productService.updateProduct(productId, productUpdateRequest);
        return ResponseEntity.ok().build();
    }
}
