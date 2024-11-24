package com.example.orderservice.client;

import com.example.common.response.ApiResponse;
import com.example.productservice.api.model.ProductInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "product-service")
public interface ProductServiceClient {

    @GetMapping("/products/batch")
    ResponseEntity<ApiResponse<List<ProductInfo>>> getProductsByIds(@RequestParam("ids") List<Long> ids);

    @PutMapping("/products/reduce-stock")
    ResponseEntity<ApiResponse<Void>> reduceStock(@RequestBody Map<Long, Integer> productQuantities);

}