# Build stage
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

# Copy Maven wrapper and pom.xml for dependency caching
COPY pom.xml .

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN apk add --no-cache maven && \
    mvn dependency:go-offline

# Copy source code
COPY src ./src

# Build the application (skip tests for faster builds)
RUN mvn clean package -Dmaven.test.skip=true

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the built JAR from build stage (using wildcard to avoid hardcoding version)
COPY --from=build /app/target/*.jar app.jar

# Expose the application port
EXPOSE 8082

# Run the application
CMD ["java", "-jar", "app.jar"]

