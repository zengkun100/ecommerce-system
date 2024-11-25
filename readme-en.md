# ecommerce-system

### Requirements
- macOS
- Docker and Docker Compose pre-installed
- Maven, version 3.8.1 or above pre-installed
- JDK, version 17 or above pre-installed

### How to run

1. Run the `build.sh` script. It will automatically install dependencies, build images, and start services.


### Architecture
![System Architecture](docs/system-architecture.png)

- **Gateway-Service** is the gateway service responsible for handling all requests and forwarding them to the corresponding service based on the URL.
- **Product-Service** is the product service responsible for handling requests related to products.
- **Order-Service** is the order service responsible for handling requests related to orders.
- **User-Service** is the user service responsible for handling user-related requests.
- **Eureka-Server** is the service registration and discovery component responsible for managing all service instances and providing registration and discovery features.
- **Zipkin-Server** is the distributed tracing component responsible for collecting and displaying the invocation chains between services.
- **Config-Server** is the configuration center component responsible for managing the configuration files of all services.

### How to use

### API Response Format
#### All APIs use a unified response format:
```json
{
    "code": 0,        // 0 indicates success, non-0 indicates an error
    "message": "success/error message",
    "data": {}        // Specific business data
}
```

#### API Authentication
Some APIs require the user to log in before accessing them. For these APIs, authentication information needs to be added to the request headers:
```
Authorization: Bearer <token>
```
Where `<token>` is the token returned after the user successfully logs in.

#### 1. Product Service
Supports creating, updating, deleting products, and fetching product details.

#### Create Product
- Endpoint: `http://localhost:8080/products`
- Method: POST
- Request Parameters: Product information
```json
{
    "name": "Sample Product",
    "price": 99.99,
    "stock": 100
}
```
- Response Parameters: Product ID
```json
{
    "code": 0,        // 0 indicates success
    "message": "success",
    "data": "Product ID"  // ID of the created product
}
```

#### Update Product
- Endpoint: `http://localhost:8080/products/{id}`
- Method: PUT
- Request Parameters: Product ID, product information
```json
{
    "name": "Sample Product",
    "price": 99.99,
    "stock": 100
}
```
- Response Parameters: Updated information
```json
{
    "code": 0,        // 0 indicates success
    "message": "success"
}
```

#### Delete Product
- Endpoint: `http://localhost:8080/products/{id}`
- Method: DELETE
- Request Parameters: Product ID
- Response Parameters: Successful deletion message
```json
{
    "code": 0,        // 0 indicates success
    "message": "success"
}
```

#### Fetch Product Details
- Endpoint: `http://localhost:8080/products/{id}`
- Method: GET
- Request Parameters: Product ID
- Response Parameters: Product details
```json
{
    "code": 0,
    "message": "success",
    "data": {
        "id": 1,
        "name": "Sample Product",
        "price": 99.99,
        "stock": 100
    }
}
```

#### 2. Order Service
Supports creating orders and fetching order details.

#### Create Order
- Endpoint: `http://localhost:8080/orders/place`
- Method: POST
- Authentication Required: Yes
- Request Parameters:
```json
{
    "orderItems": [
        {
            "productId": "Product ID",
            "quantity": "Quantity"
        }
    ]
}
```
- Response Parameters: Order ID
```json
{
    "code": 0,        // 0 indicates success
    "message": "success",
    "data": "Order ID"  // ID of the created order
}
```

#### Fetch Order Details
- Endpoint: `http://localhost:8080/orders/{orderId}`
- Method: GET
- Authentication Required: Yes
- Request Parameters: Order ID
- Response Parameters: Order details
```json
{
    "code": 0,
    "message": "success",
    "data": {
        "orderId": "Order ID",
        "userId": "User ID",
        "totalAmount": "Total Amount",
        "orderStatus": "Order Status",
        "createTime": "Creation Time",
        "items": [
            {
                "productId": "Product ID",
                "quantity": "Quantity",
                "price": "Unit Price",
                "subtotal": "Subtotal"
            }
        ]
    }
}
```

#### 3. User Service
Supports user registration, deregistration, login, and logout.

#### Register User
- Endpoint: `http://localhost:8080/users/register`
- Method: POST
- Request Parameters: User information
```json
{
    "username": "zhangsan", // Username
    "password": "password123", // Password
    "email": "zhangsan@example.com", // Email
    "role": "USER" // User role
}
```
- Response Parameters: User access token
```json
{
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", // Access token
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." // Refresh token
}
```

#### Deregister User
- Endpoint: `http://localhost:8080/users/unregister`
- Method: DELETE
- Request Parameters: None
- Authentication Required: Yes
- Response Parameters: Successful deregistration message
```json
{
    "code": 0,        // 0 indicates success
    "message": "success"
}
```

#### User Login
- Endpoint: `http://localhost:8080/users/login`
- Method: POST
- Request Parameters: User credentials
```json
{
    "username": "zhangsan",
    "password": "123456"
}
```
- Response Parameters: User access token
```json
{
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", // Access token
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." // Refresh token
}
```

#### User Logout
- Endpoint: `http://localhost:8080/users/logout`
- Method: POST
- Request Parameters: None
- Authentication Required: Yes
- Response Parameters: Successful logout message
```json
{
    "code": 0,        // 0 indicates success
    "message": "success"
}
```