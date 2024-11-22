package com.example.orderservice.model;

//import com.example.orderservice.OrderStatus;
import com.example.orderservice.converter.OrderStatusConverter;
import com.example.orderservice.enums.OrderStatus;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "t_order")
@Data
public class Order {

    @Id
    @Column(name = "order_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Convert(converter = OrderStatusConverter.class)
    @Column(name = "status")
    private OrderStatus status;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private List<OrderDetail> orderDetails;

}
