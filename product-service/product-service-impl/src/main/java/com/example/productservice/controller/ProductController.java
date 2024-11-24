package com.example.productservice.controller;

import com.example.common.response.ApiCode;
import com.example.common.response.ApiResponse;
import com.example.productservice.api.model.ProductInfo;
import com.example.productservice.exception.TooManyRequestsException;
import com.example.productservice.service.ProductService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
//import com.example.common.exception.TooManyRequestsException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/products")
@Slf4j
public class ProductController {
    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }
//
//    @GetMapping
//    public List<ProductInfo> getAllProducts() {
//        return productService.getAllProducts();
//    }

    @GetMapping("/{id}")
    @RateLimiter(name = "productServiceRateLimiter", fallbackMethod = "getProductByIdFallback")
    public ResponseEntity<ProductInfo> getProductById(@PathVariable Long id) {
        log.info("Received request to get product with id: " + id);

        Optional<ProductInfo> product = productService.getProductById(id);
        return product.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    public ResponseEntity<ProductInfo> getProductByIdFallback(Long id, Throwable throwable) {
        throw new TooManyRequestsException("请求太频繁，请稍后再试");
    }

    @PostMapping
    @RateLimiter(name = "productServiceRateLimiter", fallbackMethod = "createProductFallback")
    public ResponseEntity<ApiResponse<Long>> createProduct(@RequestBody ProductInfo product) {

        return ResponseEntity.ok(ApiResponse.success(productService.saveProduct(product).getId()));

        //return productService.saveProduct(product);
    }

    public ResponseEntity<ApiResponse<Long>> createProductFallback(ProductInfo product, Throwable throwable) {
        throw new TooManyRequestsException("请求太频繁，请稍后再试");
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> updateProduct(@PathVariable Long id, @RequestBody ProductInfo productDetails) {

        Optional<ProductInfo> product = productService.getProductById(id);
        if (product.isPresent()) {
            ProductInfo updatedProduct = product.get();
            updatedProduct.setName(productDetails.getName());
            updatedProduct.setPrice(productDetails.getPrice());
            updatedProduct.setStock(productDetails.getStock());
            productService.saveProduct(updatedProduct);
            return ResponseEntity.ok(ApiResponse.success(true));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(ApiCode.PRODUCT_NOT_FOUND, ApiCode.Message.PRODUCT_NOT_FOUND));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> deleteProduct(@PathVariable Long id) {
        Optional<ProductInfo> product = productService.getProductById(id);
        if (product.isPresent()) {
            productService.deleteProduct(id);
            return ResponseEntity.ok(ApiResponse.success(true));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(ApiCode.PRODUCT_NOT_FOUND, ApiCode.Message.PRODUCT_NOT_FOUND));
        }
    }

    @GetMapping("/batch")
    public ResponseEntity<ApiResponse<List<ProductInfo>>> getProductsByIds(@RequestParam List<Long> ids) {
        List<ProductInfo> products = productService.getProductsByIds(ids);
        return products.isEmpty()
                ? ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ApiCode.PRODUCT_NOT_FOUND, ApiCode.Message.PRODUCT_NOT_FOUND))
                : ResponseEntity.ok(ApiResponse.success(products));
    }

    @PutMapping("/reduce-stock")
    public ResponseEntity<ApiResponse<Void>> reduceStock(@RequestBody Map<Long, Integer> productQuantities) {
        try {
            productService.reduceStock(productQuantities);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(
                            ApiCode.INSUFFICIENT_STOCK,
                            e.getMessage())
                    );
        }
    }
}
