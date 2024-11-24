package com.example.common.response;

public class ApiCode {

    // 建议按模块划分错误码
//   1xxx: 参数相关错误
//   2xxx: 用户相关错误
//   3xxx: 订单相关错误
//   4xxx: 商品相关错误
//   5xxx: 系统错误

    public static final int SUCCESS = 0;        // 成功

    public static final int PARAM_ERROR = 1001; // 参数错误

    public static final int TOKEN_EXPIRED = 2002;

    public static final int TOKEN_INVALID = 2003;

    public static final int ACCESS_DENIED = 2004;

    public static final int USER_NOT_AUTHORIZED = 2005;

    public static final int ORDER_NOT_FOUND = 3004;

    public static final int PRODUCT_NOT_FOUND = 4040;
    public static final int INSUFFICIENT_STOCK = 4041;


    public static final int SYS_ERROR = 5001;   // 系统错误


    // 错误消息定义
    public static class Message {
        public static final String PRODUCT_NOT_FOUND = "Product not found";

        public static final String INSUFFICIENT_STOCK = "Insufficient stock";
        // 其他错误消息...
    }
}
