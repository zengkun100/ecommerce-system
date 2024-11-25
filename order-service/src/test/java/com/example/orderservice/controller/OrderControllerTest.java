package com.example.orderservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import com.example.common.response.ApiCode;
import com.example.orderservice.model.OrderRequest;
import com.example.orderservice.model.OrderResponse;
import com.example.orderservice.service.OrderService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;


    @Test
    void testPlaceOrder_Success() throws Exception {
        mockMvc.perform(post("/orders/place")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "123")
                        .content("{\"productId\": 1, \"quantity\": 2, \"price\": 100.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("Order placed successfully"));

        verify(orderService, times(1)).placeOrder(any(OrderRequest.class), eq(123L));
    }

    @Test
    void testPlaceOrder_Unauthenticated() throws Exception {
        mockMvc.perform(post("/orders/place")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\": 1, \"quantity\": 2, \"price\": 100.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("User not authenticated"));

        verify(orderService, times(0)).placeOrder(any(OrderRequest.class), anyLong());
    }

    @Test
    void testGetOrderDetail_UserNotAuthenticated() throws Exception {
        mockMvc.perform(get("/orders/{orderId}", 1)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(ApiCode.USER_NOT_AUTHORIZED))
            .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void testGetOrderDetail_Success() throws Exception {
        OrderResponse mockOrderResponse = new OrderResponse();
        mockOrderResponse.setOrderId("111");
        mockOrderResponse.setUserId(123L);
        mockOrderResponse.setTotalAmount(BigDecimal.ONE);

        Mockito.when(orderService.getOrderDetail(1L, 123L)).thenReturn(mockOrderResponse);

        mockMvc.perform(get("/orders/{orderId}", "111")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ApiCode.SUCCESS));
    }
}
