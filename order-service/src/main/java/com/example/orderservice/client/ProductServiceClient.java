package com.example.orderservice.client;

import com.example.productservice.api.model.ProductInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "product-service")
public interface ProductServiceClient {
    
//    @GetMapping("/products")
//    List<ProductInfo> getAllProducts();
//
//    @GetMapping("/products/{id}")
//    ResponseEntity<ProductInfo> getProductById(@PathVariable("id") Long id);
//
//    @PostMapping("/products")
//    ProductInfo createProduct(@RequestBody ProductInfo product);
//
//    @PutMapping("/products/{id}")
//    ResponseEntity<ProductInfo> updateProduct(
//        @PathVariable("id") Long id,
//        @RequestBody ProductInfo productDetails
//    );
//
//    @DeleteMapping("/products/{id}")
//    ResponseEntity<Void> deleteProduct(@PathVariable("id") Long id);

    @GetMapping("/products/batch")
    ResponseEntity<List<ProductInfo>> getProductsByIds(@RequestParam("ids") List<Long> ids);
}