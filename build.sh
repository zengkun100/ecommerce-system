#!/bin/bash

mvn clean package -DskipTests || exit 1
docker pull amazoncorretto:17-alpine || exit 1

echo "Building and running services..."

# 进入 config-server 目录并构建
cd config-server
#mvn clean package -DskipTests || exit 1
docker build -t config-server .
cd ..

# 进入 eureka-server 目录并构建
cd eureka-server
#mvn clean package -DskipTests || exit 1
docker build -t eureka-server .
cd ..

# 进入 gateway-service 目录并构建
cd gateway-service
#mvn clean package -DskipTests || exit 1
docker build -t gateway-service .
cd ..

# 进入 user-service 目录并构建
cd user-service
#mvn clean package -DskipTests || exit 1
docker build -t user-service .
cd ..

# 进入 product-service 目录并构建
cd product-service
#mvn clean package -DskipTests || exit 1
docker build -t product-service .
cd ..

# 进入 order-service 目录并构建
cd order-service
#mvn clean package -DskipTests || exit 1
docker build -t order-service .
cd ..

echo "Finish packaging..."

# 启动容器
echo "Starting containers..."

# 停止并删除旧容器
echo "停止并删除旧容器..."
docker-compose down

# 启动服务
echo "启动服务..."
docker-compose up -d

echo "所有服务启动完成！"

# 输出服务状态
docker-compose ps

#docker network rm eureka-service.com zipkin.com config-server.com
#
#docker network create eureka-service.com
#docker network create zipkin.com
#docker network create config-server.com
#
#docker run -d --name zipkin --network zipkin.com -p 9411:9411   openzipkin/zipkin || exit 1
#
## 启动 Eureka Server
#docker run -d --name eureka-server --network eureka-service.com --network zipkin.com --network config-server.com \
#  -p 8761:8761 eureka-server || exit 1
#
## 启动 Config Server
#docker run -d --name config-server --network eureka-service.com --network zipkin.com --network config-server.com \
#  -p 8888:8888 config-server || exit 1
#
## 启动 API Gateway
#docker run -d --name gateway-service --network eureka-service.com --network zipkin.com --network config-server.com \
#  -p 8080:8080 -e EUREKA_SERVER_URL=http://eureka-server:8761/eureka/ \
#  -e CONFIG_SERVER_URL=http://config-server.com:8888 \
#  -e ZIPKIN_URL=zipkin.com:5672 \
#  gateway-service || exit 1
#
## 启动 User Service
#docker run -d --name user-service --network eureka-service.com --network zipkin.com --network config-server.com \
#  -p 8081:8081  -e EUREKA_SERVER_URL=http://eureka-server:8761/eureka/ \
#  -e CONFIG_SERVER_URL=http://config-server.com:8888 \
#  -e ZIPKIN_URL=127.0.0.1:5672 \
#  user-service
#
## 启动 Order Service
#docker run -d --name order-service --network eureka-service.com --network zipkin.com --network config-server.com \
#  -p 8082:8082  -e EUREKA_SERVER_URL=http://eureka-server:8761/eureka/ \
#  -e CONFIG_SERVER_URL=http://config-server.com:8888 \
#  -e ZIPKIN_URL=zipkin.com:5672 \
#  order-service
#
## 启动 Product Service
#docker run -d --name product-service --network eureka-service.com --network zipkin.com  --network config-server.com \
#  -p 8083:8083 -e EUREKA_SERVER_URL=http://eureka-server:8761/eureka/ \
#  -e CONFIG_SERVER_URL=http://config-server.com:8888 \
#  -e ZIPKIN_URL=zipkin.com:5672 \
#  product-service

echo "All services are running."
