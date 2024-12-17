FROM maven:3-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Sử dụng image của Java làm base
FROM openjdk:17-jdk-slim

# Thiết lập thư mục làm việc bên trong container
WORKDIR /app

# Sao chép file JAR vào container
COPY target/DATN_FTL_N4-2-0.0.1-SNAPSHOT.jar /app/DATN_FTL_N4-2-0.0.1-SNAPSHOT.jar

EXPOSE 8080

# Câu lệnh để chạy ứng dụng
ENTRYPOINT ["java", "-jar", "/app/DATN_FTL_N4-2-0.0.1-SNAPSHOT.jar"]
