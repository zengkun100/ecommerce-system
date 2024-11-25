# ecommerce-system

### Requirements
- mac os
- 预先安装Docker和Docker Compose
- 预先安装Maven，3.8.1 以上版本
- 预先安装JDK，17 以上版本

### How to run

1. 运行 build.sh 脚本，会自动安装依赖，构建镜像，并启动服务


### Architecture
![系统架构图](docs/system-architecture.png)

- Gateway-Service 是网关服务，负责处理所有请求，并根据请求的URL将请求转发到相应的服务。
- Product-Service 是商品服务，负责处理商品相关的请求。
- Order-Service 是订单服务，负责处理订单相关的请求。
- User-Service 是用户服务，负责处理用户相关的请求。
- Eureka-Server 是服务注册与发现的组件，负责管理所有服务实例，并提供服务注册与发现的功能。
- Zipkin-Server 是分布式链路追踪组件，负责收集和展示服务之间的调用链路。
- Config-Server 是配置中心组件，负责管理所有服务的配置文件。

### How to use

### API响应格式说明
#### 所有API都使用统一的响应格式：
```json
{
    "code": 0,        // 0 表示成功，非0表示错误
    "message": "success/error message",
    "data": {}        // 具体的业务数据
}
```

#### API认证说明
某些API需要用户登录后才能访问。对于这些API，需要在请求头中添加认证信息：
```
Authorization: Bearer <token>
```
其中 token 是用户登录成功后返回的令牌。

#### 1. 商品服务
可以创建商品，更新商品，删除商品，获取商品详情

#### 创建商品
- 访问路径：http://localhost:8080/products
- 访问方式：POST
- 请求参数：商品信息
```json
{
    "name": "示例商品",
    "price": 99.99,
    "stock": 100
}
```
- 返回参数：商品 id
```json
{
    "code": 0,        // 0 表示成功
    "message": "success",
    "data": "商品ID"  // 创建成功的商品ID
}
```

#### 更新商品
- 访问路径：http://localhost:8080/products/{id}
- 访问方式：PUT
- 请求参数：商品 id，商品信息
```json
{
    "name": "示例商品",
    "price": 99.99,
    "stock": 100
}
```
- 返回参数：成功更新的信息
```json
{
    "code": 0,        // 0 表示成功
    "message": "success",
}
```

#### 删除商品
- 访问路径：http://localhost:8080/products/{id}
- 访问方式：DELETE
- 请求参数：商品 id
- 返回参数：成功删除的信息
```json
{
    "code": 0,        // 0 表示成功
    "message": "success",
}
```

#### 获取商品详情
- 访问路径：http://localhost:8080/products/{id}
- 访问方式：GET
- 请求参数：商品 id
- 返回参数：商品详情
```json
{
    "code": 0,
    "message": "success",
    "data": {
        "id": 1,
        "name": "示例商品",
        "price": 99.99,
        "stock": 100
    }
}
```

#### 2. 访问订单服务
可以创建订单，获取订单详情

#### 创建订单
- 访问路径：http://localhost:8080/orders/place
- 访问方式：POST
- 认证要求：需要登录
- 请求参数：
```json
{
    "orderItems": [
        {
            "productId": "商品ID",
            "quantity": "购买数量"
        }
    ]
}
```
- 返回参数：订单列表
```json
{
    "code": 0,        // 0 表示成功
    "message": "success",
    "data": "订单ID"  // 创建成功的订单ID
}
```

#### 获取订单详情
- 访问路径：http://localhost:8080/orders/{orderId}
- 访问方式：GET
- 认证要求：需要登录
- 请求参数：orderId
- 返回参数：订单详情
```json
{
    "code": 0,
    "message": "success",
    "data": {
        "orderId": "订单ID",
        "userId": "用户ID",
        "totalAmount": "订单总金额",
        "orderStatus": "订单状态",
        "createTime": "创建时间",
        "items": [
            {
                "productId": "商品ID",
                "quantity": "购买数量",
                "price": "商品单价",
                "subtotal": "小计金额"
            }
        ]
    }
}
```

#### 3. 访问用户服务
可以创建用户，注销用户，登录，登出

#### 注册用户
- 访问路径：http://localhost:8080/users/register
- 访问方式：POST
- 请求参数：用户
```json
{
    "username": "zhangsan", // 用户名
    "password": "password123", // 密码
    "email": "zhangsan@example.com", // 电子邮箱
    "role": "USER" // 用户角色
}
```
- 返回参数：用户访问令牌
```json
{
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", // 访问令牌
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." // 刷新令牌
}
```

#### 注销用户
- 访问路径：http://localhost:8080/users/unregister
- 访问方式：DELETE
- 请求参数：无
- 认证要求：需要登录
- 返回参数：成功注销的信息
```json
{
    "code": 0,        // 0 表示成功
    "message": "success",
}
```

#### 用户登录
- 访问路径：http://localhost:8080/users/login
- 访问方式：POST
- 请求参数：用户信息
```json
{
    "username": "zhangsan",
    "password": "123456"
}
```
- 返回参数：用户访问令牌
```json
{
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", // 访问令牌
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." // 刷新令牌
}
```

#### 用户登出
- 访问路径：http://localhost:8080/users/logout
- 访问方式：POST
- 请求参数：无
- 认证要求：需要登录
- 返回参数：成功登出的信息
```json
{
    "code": 0,        // 0 表示成功
    "message": "success",
}
```