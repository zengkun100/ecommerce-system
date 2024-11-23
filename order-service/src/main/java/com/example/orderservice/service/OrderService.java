package com.example.orderservice.service;

//import com.example.orderservice.OrderStatus;
import com.example.orderservice.enums.OrderStatus;
import com.example.orderservice.exception.OrderException;
import com.example.orderservice.client.ProductServiceClient;
import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderDetail;
import com.example.orderservice.model.OrderItemRequest;
import com.example.orderservice.model.OrderRequest;
//import com.example.orderservice.model.Product;
import com.example.orderservice.repository.OrderRepository;
import com.example.productservice.api.model.ProductInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.function.Function;


public interface OrderService {

    /**
     * 下单
     * @param orderRequest 订单请求
     * @param userId 用户ID
     * @return 订单ID
     */
    String placeOrder(OrderRequest orderRequest, Long userId);


}
