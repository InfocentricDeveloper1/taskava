# Build stage
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY */pom.xml ./*/
RUN mvn dependency:go-offline -B
COPY . .
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -g 1000 taskava && adduser -u 1000 -G taskava -s /bin/sh -D taskava
COPY --from=builder /app/taskava-api-gateway/target/*.jar app.jar
RUN chown -R taskava:taskava /app
USER taskava
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]