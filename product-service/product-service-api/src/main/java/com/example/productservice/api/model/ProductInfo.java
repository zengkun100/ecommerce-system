package com.example.productservice.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductInfo {

    private Long id;
    private String name;
    private BigDecimal price;
    private int stock;

}
