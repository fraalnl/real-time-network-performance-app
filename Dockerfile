# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=builder /build/target/real-time-network-performance-app.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
