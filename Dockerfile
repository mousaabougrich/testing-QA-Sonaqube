# -------------------------------------------------
# Stage 1 – Build the JAR (multi-stage)
# -------------------------------------------------
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copy Maven wrapper + pom
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Make wrapper executable (just in case)
RUN chmod +x mvnw

# Download dependencies (caches layers)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the fat JAR (skip tests)
RUN ./mvnw clean package -DskipTests -B

# -------------------------------------------------
# Stage 2 – Runtime image (JRE only)
# -------------------------------------------------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install curl for health checks
RUN apk add --no-cache curl

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/biochain-*.jar app.jar

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Expose ports used by the node
EXPOSE 8080 8545

# Healthcheck (matches your compose file)
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the app
ENTRYPOINT ["java", "-jar", "/app/app.jar"]