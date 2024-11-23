package com.example.orderservice.service;

import com.example.common.response.ApiResponse;
import com.example.orderservice.client.ProductServiceClient;
import com.example.orderservice.enums.OrderStatus;
import com.example.orderservice.exception.OrderException;
import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderItemRequest;
import com.example.orderservice.model.OrderRequest;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.service.impl.OrderServiceImpl;
import com.example.productservice.api.model.ProductInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductServiceClient productService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private OrderRequest orderRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        OrderItemRequest o1 = new OrderItemRequest();
        o1.setProductId(1L);
        o1.setQuantity(5);

        orderRequest = new OrderRequest();
        orderRequest.setOrderItems(Arrays.asList(
                o1
        ));
    }

    @Test
    void testPlaceOrder_Success() {
        ProductInfo product1 = new ProductInfo();
        product1.setId(1L);
        product1.setName("Product 1");
        product1.setPrice(new BigDecimal("100.00"));
        product1.setStock(10);

        ProductInfo product2 = new ProductInfo();
        product2.setId(2L);
        product2.setName("Product 2");
        product2.setPrice(new BigDecimal("50.00"));
        product2.setStock(5);

        when(productService.getProductsByIds(anyList()))
                .thenReturn(new ResponseEntity<>(new ApiResponse<>(0, "Success", Arrays.asList(product1, product2)), HttpStatus.OK));

        when(productService.reduceStock(anyMap()))
                .thenReturn(new ResponseEntity<>(new ApiResponse<>(0, "Success", null), HttpStatus.OK));

        Order savedOrder = new Order();
        savedOrder.setId(123L);
        savedOrder.setCreateTime(LocalDateTime.now());
        savedOrder.setStatus(OrderStatus.PENDING);
        savedOrder.setTotalAmount(new BigDecimal("250.00"));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        String orderId = orderService.placeOrder(orderRequest, 1L);

        assertNotNull(orderId);
        assertEquals("123", orderId);

        verify(productService, times(1)).getProductsByIds(anyList());
        verify(productService, times(1)).reduceStock(anyMap());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void testPlaceOrder_FailedToGetProductInfo() {
        when(productService.getProductsByIds(anyList()))
                .thenReturn(new ResponseEntity<>(new ApiResponse<>(1, "Failure", null), HttpStatus.INTERNAL_SERVER_ERROR));

        OrderException exception = assertThrows(OrderException.class, () -> {
            orderService.placeOrder(orderRequest, 1L);
        });

        assertEquals("获取商品信息失败", exception.getMessage());
    }

    @Test
    void testPlaceOrder_InsufficientStock() {
        ProductInfo product1 = new ProductInfo();
        product1.setId(1L);
        product1.setName("Product 1");
        product1.setPrice(new BigDecimal("100.00"));
        product1.setStock(6);

        ProductInfo product2 = new ProductInfo();
        product2.setId(2L);
        product2.setName("Product 2");
        product2.setPrice(new BigDecimal("50.00"));
        product2.setStock(8);

        when(productService.getProductsByIds(anyList()))
                .thenReturn(new ResponseEntity<>(new ApiResponse<>(0, "Success", Arrays.asList(product1, product2)), HttpStatus.OK));

        when(productService.reduceStock(anyMap()))
                .thenReturn(new ResponseEntity<>(new ApiResponse<>(0, "Failure", null), HttpStatus.BAD_REQUEST));

        OrderException exception = assertThrows(OrderException.class, () -> {
            orderService.placeOrder(orderRequest, 1L);
        });

        assertEquals("Failure", exception.getMessage());
    }

    @Test
    void testPlaceOrder_FailedToReduceStock() {
        ProductInfo product1 = new ProductInfo();
        product1.setId(1L);
        product1.setName("Product 1");
        product1.setPrice(new BigDecimal("100.00"));
        product1.setStock(10);

        ProductInfo product2 = new ProductInfo();
        product2.setId(2L);
        product2.setName("Product 2");
        product2.setPrice(new BigDecimal("50.00"));
        product2.setStock(5);

        when(productService.getProductsByIds(anyList()))
                .thenReturn(new ResponseEntity<>(new ApiResponse<>(0, "Success", Arrays.asList(product1, product2)), HttpStatus.OK));

        when(productService.reduceStock(anyMap()))
                .thenReturn(new ResponseEntity<>(new ApiResponse<>(1, "Failure", null), HttpStatus.BAD_REQUEST));

        OrderException exception = assertThrows(OrderException.class, () -> {
            orderService.placeOrder(orderRequest, 1L);
        });

        assertEquals("Failure", exception.getMessage());
    }

    @Test
    void testPlaceOrder_Failed() {
        ProductInfo product1 = new ProductInfo();
        product1.setId(1L);
        product1.setName("Product 1");
        product1.setPrice(new BigDecimal("100.00"));
        product1.setStock(1);

        ProductInfo product2 = new ProductInfo();
        product2.setId(2L);
        product2.setName("Product 2");
        product2.setPrice(new BigDecimal("50.00"));
        product2.setStock(5);

        when(productService.getProductsByIds(anyList()))
                .thenReturn(new ResponseEntity<>(new ApiResponse<>(0, "Success", Arrays.asList(product1, product2)), HttpStatus.OK));

        when(productService.reduceStock(anyMap()))
                .thenReturn(new ResponseEntity<>(new ApiResponse<>(1, "Failure", null), HttpStatus.BAD_REQUEST));

        OrderException exception = assertThrows(OrderException.class, () -> {
            orderService.placeOrder(orderRequest, 1L);
        });

        assertEquals("商品[Product 1]库存不足，当前库存:1，需求数量:5", exception.getMessage());
    }
}
