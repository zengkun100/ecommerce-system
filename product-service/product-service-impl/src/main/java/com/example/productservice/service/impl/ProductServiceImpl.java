package com.example.productservice.service.impl;

import com.example.productservice.api.model.ProductInfo;
import com.example.productservice.model.Product;
import com.example.productservice.repository.ProductRepository;
import com.example.productservice.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductRepository productRepository;

    @Override
    public List<ProductInfo> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::convertToProductInfo)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ProductInfo> getProductById(Long id) {
        return productRepository.findById(id)
                .map(this::convertToProductInfo);
    }

    @Override
    public ProductInfo saveProduct(ProductInfo productInfo) {
        Product product = convertToProduct(productInfo);
        Product savedProduct = productRepository.save(product);
        return convertToProductInfo(savedProduct);
    }

    @Override
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    @Override
    public List<ProductInfo> getProductsByIds(List<Long> ids) {
        return productRepository.findAllById(ids).stream()
                .map(this::convertToProductInfo)
                .collect(Collectors.toList());
    }

    // 转换方法
    private ProductInfo convertToProductInfo(Product product) {
        ProductInfo productInfo = new ProductInfo();
        productInfo.setId(product.getId());
        productInfo.setName(product.getName());
        productInfo.setPrice(product.getPrice());
//        productInfo.setDescription(product.getDescription());
        productInfo.setStock(product.getQuantity());
        // 根据实际字段继续设置...
        return productInfo;
    }

    private Product convertToProduct(ProductInfo productInfo) {
        Product product = new Product();
        product.setId(productInfo.getId());
        product.setName(productInfo.getName());
        product.setPrice(productInfo.getPrice());
//        product.setDescription(productInfo.getDescription());
        product.setQuantity(productInfo.getStock());
        // 根据实际字段继续设置...
        return product;
    }
}