package com.example.productservice.controller;

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
import java.util.Collections;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
public class ProductControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Test
    public void getAllProducts_success() throws Exception {
        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("100.0"));
        product.setQuantity(10);

        Mockito.when(productService.getAllProducts()).thenReturn(Collections.singletonList(product));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Product"));
    }

    @Test
    public void getProductById_success() throws Exception {
        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("100.0"));
        product.setQuantity(10);

        Mockito.when(productService.getProductById(1L)).thenReturn(Optional.of(product));

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    public void createProduct_success() throws Exception {
        Product product = new Product();
        product.setName("New Product");
        product.setPrice(new BigDecimal("150.0"));
        product.setQuantity(5);

        Mockito.when(productService.saveProduct(Mockito.any(Product.class))).thenReturn(product);

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New Product\",\"price\":150.0,\"quantity\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Product"));
    }

    @Test
    public void updateProduct_success() throws Exception {
        Product product = new Product();
        product.setId(1L);
        product.setName("Old Product");
        product.setPrice(new BigDecimal("100.0"));
        product.setQuantity(10);

        Product updatedProduct = new Product();
        updatedProduct.setId(1L);
        updatedProduct.setName("Updated Product");
        updatedProduct.setPrice(new BigDecimal("120.0"));
        updatedProduct.setQuantity(15);

        Mockito.when(productService.getProductById(1L)).thenReturn(Optional.of(product));
        Mockito.when(productService.saveProduct(Mockito.any(Product.class))).thenReturn(updatedProduct);

        mockMvc.perform(put("/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated Product\",\"price\":120.0,\"quantity\":15}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Product"));
    }

    @Test
    public void deleteProduct_success() throws Exception {
        Mockito.doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/products/1"))
                .andExpect(status().isNoContent());
    }
}
