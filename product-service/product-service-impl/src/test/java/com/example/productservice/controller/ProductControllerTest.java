package com.example.productservice.controller;

import com.example.common.response.ApiCode;
import com.example.productservice.api.model.ProductInfo;
import com.example.productservice.service.impl.ProductServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import com.example.productservice.model.Product;
import com.example.productservice.service.ProductService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
public class ProductControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private ProductInfo productInfo;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        productInfo = new ProductInfo();
        productInfo.setId(1L);
        productInfo.setName("Test Product");
        productInfo.setPrice(BigDecimal.TEN);
        productInfo.setStock(50);
    }

    @Test
    void testCreateProduct() throws Exception {
        when(productService.saveProduct(any(ProductInfo.class))).thenReturn(productInfo);

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productInfo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(1L))
                .andExpect(jsonPath("$.code").value(0)) // 假设 ApiResponse.success 返回 code 200
                .andExpect(jsonPath("$.message").value("success"));

        verify(productService, times(1)).saveProduct(any(ProductInfo.class));
    }

    @Test
    void testUpdateProduct_Success() throws Exception {
        // 模拟查找和更新产品的行为
        when(productService.getProductById(1L)).thenReturn(Optional.of(productInfo));
        ProductInfo updatedProduct = new ProductInfo();
        updatedProduct.setId(1L);
        updatedProduct.setName("Updated Product");
        updatedProduct.setPrice(BigDecimal.TEN);
        updatedProduct.setStock(40);

        when(productService.saveProduct(any(ProductInfo.class))).thenReturn(updatedProduct);

        mockMvc.perform(put("/products/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true))  // 成功时返回 true
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("success"));

        verify(productService, times(1)).getProductById(1L);
        verify(productService, times(1)).saveProduct(any(ProductInfo.class));
    }

    @Test
    void testUpdateProduct_NotFound() throws Exception {
        when(productService.getProductById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/products/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productInfo)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ApiCode.PRODUCT_NOT_FOUND))
                .andExpect(jsonPath("$.message").value(ApiCode.Message.PRODUCT_NOT_FOUND));
    }

    @Test
    void testDeleteProduct_Success() throws Exception {
        when(productService.getProductById(1L)).thenReturn(Optional.of(productInfo));

        mockMvc.perform(delete("/products/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true)) // 成功时返回 true
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("success"));

        verify(productService, times(1)).getProductById(1L);
        verify(productService, times(1)).deleteProduct(1L);
    }

    @Test
    void testDeleteProduct_NotFound() throws Exception {
        when(productService.getProductById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/products/{id}", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ApiCode.PRODUCT_NOT_FOUND))
                .andExpect(jsonPath("$.message").value(ApiCode.Message.PRODUCT_NOT_FOUND));
    }

    @Test
    void testGetProductsByIds() throws Exception {
        List<ProductInfo> productList = Arrays.asList(productInfo);
        when(productService.getProductsByIds(anyList())).thenReturn(productList);

        mockMvc.perform(get("/products/batch")
                        .param("ids", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Test Product"))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("success"));

        verify(productService, times(1)).getProductsByIds(anyList());
    }

    @Test
    void testReduceStock_Success() throws Exception {
        Map<Long, Integer> productQuantities = Map.of(1L, 10);
        doNothing().when(productService).reduceStock(productQuantities);

        mockMvc.perform(put("/products/reduce-stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productQuantities)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("success"));

        verify(productService, times(1)).reduceStock(productQuantities);
    }

    @Test
    void testReduceStock_Failure() throws Exception {
        Map<Long, Integer> productQuantities = Map.of(1L, 10);
        doThrow(new RuntimeException("Not enough stock for product ID: 1")).when(productService).reduceStock(productQuantities);

        mockMvc.perform(put("/products/reduce-stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productQuantities)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ApiCode.INSUFFICIENT_STOCK))
                .andExpect(jsonPath("$.message").value("Not enough stock for product ID: 1"));

        verify(productService, times(1)).reduceStock(productQuantities);
    }
}
