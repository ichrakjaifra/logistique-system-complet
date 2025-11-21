# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /workspace/app

# Copy pom.xml
COPY pom.xml .
# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src
# Build the application
RUN mvn clean package -DskipTests

# Runtime stage (A slim JRE is used for a smaller, more secure image)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create a non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy JAR from builder stage
COPY --from=builder /workspace/app/target/*.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Expose port
EXPOSE 8080

# Entry point
ENTRYPOINT ["java", "-jar", "/app/app.jar"]