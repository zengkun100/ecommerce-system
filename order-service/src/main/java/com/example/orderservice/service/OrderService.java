package com.example.orderservice.service;

import com.example.orderservice.model.OrderRequest;
import com.example.orderservice.model.OrderResponse;


public interface OrderService {

    /**
     * 下单
     *
     * @param orderRequest 订单请求
     * @param userId       用户ID
     * @return 订单ID
     */
    String placeOrder(OrderRequest orderRequest, Long userId);

    /**
     * 查询订单详情
     * @param orderId 订单ID
     * @param userId 当前用户ID
     * @return 订单详情
     * @throws AccessDeniedException 如果用户无权访问该订单
     * @throws OrderNotFoundException 如果订单不存在
     */
    OrderResponse getOrderDetail(Long orderId, Long userId);

}
