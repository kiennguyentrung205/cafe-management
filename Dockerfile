# Giai đoạn 1: Build ứng dụng (dùng Maven và JDK 21)
FROM maven:3.9-eclipse-temurin-21 AS build

# Tạo thư mục làm việc
WORKDIR /app

# Copy file pom.xml và tải thư viện trước để cache
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy toàn bộ source code và build
COPY src ./src
RUN mvn clean package -DskipTests

# Giai đoạn 2: Chạy ứng dụng (dùng JRE 21)
# Sử dụng một image JRE nhỏ gọn hơn
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy file .jar đã build từ giai đoạn 1
COPY --from=build /app/target/*.jar app.jar

# Lệnh để chạy ứng dụng
# Render sẽ tự động gán biến $PORT
ENTRYPOINT ["java", "-jar", "app.jar"]