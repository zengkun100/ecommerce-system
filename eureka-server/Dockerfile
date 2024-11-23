# 使用 OpenJDK 作为基础镜像
FROM amazoncorretto:17-alpine

# 设置工作目录
WORKDIR /app

# 复制 jar 包到容器中
COPY target/eureka-server-0.0.1-SNAPSHOT.jar eureka-server.jar

# 设置 JVM 参数
ENV JAVA_OPTS="-Xmx512m -Xms512m"

# 暴露端口
EXPOSE 8761

# 启动命令
ENTRYPOINT ["java", "-jar", "eureka-server.jar"]
