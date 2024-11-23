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

    public static final int BIZ_ERROR = 2001;   // 业务错误

    public static final int SYS_ERROR = 5001;   // 系统错误

    public static final int PRODUCT_NOT_FOUND = 4040;


    // 错误消息定义
    public static class Message {
        public static final String PRODUCT_NOT_FOUND = "Product not found";
        // 其他错误消息...
    }
}
