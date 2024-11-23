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

#docker exec -it order-service sh
#apk add curl

echo "All services are running."
