package com.example.productservice.controller;

import com.example.productservice.api.model.ProductInfo;
import com.example.productservice.service.intf.ProductService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/products")
public class ProductController {
    @Autowired
    private ProductService productService;

    @GetMapping
    public List<ProductInfo> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductInfo> getProductById(@PathVariable Long id) {
        Optional<ProductInfo> product = productService.getProductById(id);
        return product.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ProductInfo createProduct(@RequestBody ProductInfo product) {
        return productService.saveProduct(product);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductInfo> updateProduct(@PathVariable Long id, @RequestBody ProductInfo productDetails) {
        Optional<ProductInfo> product = productService.getProductById(id);
        if (product.isPresent()) {
            ProductInfo updatedProduct = product.get();
            updatedProduct.setName(productDetails.getName());
            updatedProduct.setPrice(productDetails.getPrice());
            updatedProduct.setStock(productDetails.getStock());
//            updatedProduct.setDescription(productDetails.getDescription());
            return ResponseEntity.ok(productService.saveProduct(updatedProduct));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/batch")
    public ResponseEntity<List<ProductInfo>> getProductsByIds(@RequestParam List<Long> ids) {
        List<ProductInfo> products = productService.getProductsByIds(ids);
        return products.isEmpty() ? 
            ResponseEntity.notFound().build() : 
            ResponseEntity.ok(products);
    }
}
