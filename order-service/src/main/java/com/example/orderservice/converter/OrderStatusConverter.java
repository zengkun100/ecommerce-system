package com.example.orderservice.converter;

//import com.example.orderservice.OrderStatus;

import com.example.orderservice.enums.OrderStatus;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class OrderStatusConverter implements AttributeConverter<OrderStatus, Integer> {
    
    @Override
    public Integer convertToDatabaseColumn(OrderStatus status) {
        return status == null ? null : status.getCode();
    }

    @Override
    public OrderStatus convertToEntityAttribute(Integer code) {
        return OrderStatus.fromCode(code);
    }
}