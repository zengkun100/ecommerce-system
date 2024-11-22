package com.example.orderservice.service;

//import com.example.orderservice.OrderStatus;
import com.example.orderservice.enums.OrderStatus;
import com.example.orderservice.exception.OrderException;
import com.example.orderservice.feign.ProductService;
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
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.function.Function;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductService productService;

    // @Autowired
    // private RestTemplate restTemplate;

    public String placeOrder(OrderRequest orderRequest, Long userId) {

        // 1. 调用product-service获取商品信息
        ResponseEntity<List<ProductInfo>> productResponse = productService.getProductsByIds(
                orderRequest.getOrderItems().stream()
                        .map(OrderItemRequest::getProductId)
                        .collect(Collectors.toList())
        );

        if (!productResponse.getStatusCode().is2xxSuccessful() || productResponse.getBody() == null) {
            throw new OrderException("获取商品信息失败");
        }

        List<ProductInfo> products = productResponse.getBody();

        // 2. 验证商品数量是否完整
        if (products.size() < orderRequest.getOrderItems().size()) {
            throw new OrderException("部分商品不存在");
        }

        // 3. 将商品信息转换为Map，方便后续查询
        Map<Long, ProductInfo> productMap = products.stream()
                .collect(Collectors.toMap(ProductInfo::getId, Function.identity()));

        // 4. 验证每个商品的库存是否足够
        for (OrderItemRequest orderItem : orderRequest.getOrderItems()) {
            ProductInfo product = productMap.get(orderItem.getProductId());
            if (product.getStock() < orderItem.getQuantity()) {
                throw new OrderException(
                        String.format("商品[%s]库存不足，当前库存:%d，需求数量:%d",
                                product.getName(),
                                product.getStock(),
                                orderItem.getQuantity())
                );
            }
        }
// 5. 创建订单对象
        Order order = new Order();
        order.setUserId(userId);
        order.setCreateTime(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        // 6. 创建订单明细并计算总金额
        List<OrderDetail> orderDetails = orderRequest.getOrderItems().stream()
                .map(item -> {
                    ProductInfo product = productMap.get(item.getProductId());
                    OrderDetail detail = new OrderDetail();
                    detail.setProductId(product.getId());
                    detail.setOrder(order);
//                    detail.(product.getName());
                    detail.setPrice(product.getPrice());
                    detail.setQuantity(item.getQuantity());
                    detail.setTotalPrice(product.getPrice().multiply(new BigDecimal(item.getQuantity())));
                    return detail;
                })
                .collect(Collectors.toList());

        // 计算订单总金额
        BigDecimal totalAmount = orderDetails.stream()
                .map(OrderDetail::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalAmount(totalAmount);
        order.setOrderDetails(orderDetails);

        // 7. 保存订单（会自动保存订单明细）
        Order savedOrder = orderRepository.save(order);

        return savedOrder.getId().toString();

    }

}
