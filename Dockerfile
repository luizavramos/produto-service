# Build stage
FROM maven:3.9.6-amazoncorretto-21 AS build
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests -Dmaven.compiler.source=21 -Dmaven.compiler.target=21

# Runtime stage
FROM amazoncorretto:21-alpine
WORKDIR /app

# Install curl for health checks
RUN apk --no-cache add curl

# Create non-root user
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Copy jar from build stage
COPY --from=build /app/target/produto-service-*.jar app.jar

# Change ownership to non-root user
RUN chown -R appuser:appgroup /app
USER appuser

# Environment variables for Kafka and database
ENV SPRING_PROFILES_ACTIVE=docker
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV KAFKA_BOOTSTRAP_SERVERS=kafka:9092
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/produto_db
ENV SPRING_DATASOURCE_USERNAME=produto_user
ENV SPRING_DATASOURCE_PASSWORD=produto_pass

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=5 \
    CMD curl -f http://localhost:80823/actuator/health || exit 1

EXPOSE 8082
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]