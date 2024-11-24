package com.example.orderservice.service;

import com.example.orderservice.model.OrderRequest;


public interface OrderService {

    /**
     * 下单
     *
     * @param orderRequest 订单请求
     * @param userId       用户ID
     * @return 订单ID
     */
    String placeOrder(OrderRequest orderRequest, Long userId);


}
