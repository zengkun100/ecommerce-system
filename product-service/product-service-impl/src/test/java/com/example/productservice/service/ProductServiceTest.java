package com.example.productservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.productservice.api.model.ProductInfo;
import com.example.productservice.model.Product;
import com.example.productservice.repository.ProductRepository;

import com.example.productservice.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class ProductServiceTest {

    @Autowired
    private ProductServiceImpl productService;

    @MockBean
    private ProductRepository productRepository;

    private Product product;
    private ProductInfo productInfo;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(BigDecimal.TEN);
        product.setStock(50);

        productInfo = new ProductInfo();
        productInfo.setId(1L);
        productInfo.setName("Test Product");
        productInfo.setPrice(BigDecimal.TEN);
        productInfo.setStock(50);
    }

    @Test
    void testGetProductById() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Optional<ProductInfo> result = productService.getProductById(1L);

        assertTrue(result.isPresent());
        assertEquals("Test Product", result.get().getName());

        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void testSaveProduct() {
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductInfo result = productService.saveProduct(productInfo);

        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        assertEquals(50, result.getStock());

        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testDeleteProduct() {
        productService.deleteProduct(1L);

        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    void testGetProductsByIds() {
        List<Product> products = Arrays.asList(product);
        when(productRepository.findAllById(Arrays.asList(1L))).thenReturn(products);

        List<ProductInfo> result = productService.getProductsByIds(Arrays.asList(1L));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).getName());

        verify(productRepository, times(1)).findAllById(Arrays.asList(1L));
    }

    @Test
    @Transactional
    void testReduceStock_Success() {
        when(productRepository.reduceStock(1L, 10)).thenReturn(1);

        Map<Long, Integer> productQuantities = new HashMap<>();
        productQuantities.put(1L, 10);

        productService.reduceStock(productQuantities);

        verify(productRepository, times(1)).reduceStock(1L, 10);
    }

    @Test
    void testReduceStock_Failure() {
        when(productRepository.reduceStock(1L, 10)).thenReturn(0);

        Map<Long, Integer> productQuantities = new HashMap<>();
        productQuantities.put(1L, 10);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productService.reduceStock(productQuantities);
        });

        assertEquals("Not enough stock for product ID: 1", exception.getMessage());

        verify(productRepository, times(1)).reduceStock(1L, 10);
    }
}
