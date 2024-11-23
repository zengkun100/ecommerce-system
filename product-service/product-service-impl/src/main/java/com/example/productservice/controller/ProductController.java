package com.example.productservice.controller;

import com.example.common.response.ApiCode;
import com.example.common.response.ApiResponse;
import com.example.productservice.api.model.ProductInfo;
import com.example.productservice.service.ProductService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/products")
public class ProductController {
    @Autowired
    private ProductService productService;
//
//    @GetMapping
//    public List<ProductInfo> getAllProducts() {
//        return productService.getAllProducts();
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<ProductInfo> getProductById(@PathVariable Long id) {
//        Optional<ProductInfo> product = productService.getProductById(id);
//        return product.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
//    }

    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createProduct(@RequestBody ProductInfo product) {

        return ResponseEntity.ok(ApiResponse.success(productService.saveProduct(product).getId()));

        //return productService.saveProduct(product);
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
    public ResponseEntity<List<ProductInfo>> getProductsByIds(@RequestParam List<Long> ids) {
        List<ProductInfo> products = productService.getProductsByIds(ids);
        return products.isEmpty() ? 
            ResponseEntity.notFound().build() : 
            ResponseEntity.ok(products);
    }

    @PutMapping("/reduce-stock")
    public String reduceStock(@RequestBody Map<Long, Integer> productQuantities) {
        try {
            productService.reduceStock(productQuantities);
            return "Stock reduced successfully for all products.";
        } catch (RuntimeException e) {
            return e.getMessage();
        }
    }
}
