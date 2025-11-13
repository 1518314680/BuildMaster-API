FROM openjdk:21-jdk-slim

WORKDIR /app

# 复制 Maven 配置文件
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# 下载依赖
RUN ./mvnw dependency:go-offline -B

# 复制源代码
COPY src ./src

# 构建应用
RUN ./mvnw clean package -DskipTests

# 运行应用
EXPOSE 8080
CMD ["java", "-jar", "target/BuildMaster-API-1.0.0.jar"]
