package com.example.productservice.service;

import com.example.productservice.api.model.ProductInfo;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ProductService {
//    List<ProductInfo> getAllProducts();

    Optional<ProductInfo> getProductById(Long id);
    
    ProductInfo saveProduct(ProductInfo productInfo);
    
    void deleteProduct(Long id);
    
    List<ProductInfo> getProductsByIds(List<Long> ids);

    void reduceStock(Map<Long, Integer> productQuantities);
}
