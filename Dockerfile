FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY target/real-time-network-performance-app.jar real-time-network-performance-app.jar

ENTRYPOINT ["java", "-jar", "real-time-network-performance-app.jar"]
