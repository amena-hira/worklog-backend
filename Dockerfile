# ==========================================
# Stage 1: Build the Application
# ==========================================
FROM eclipse-temurin:21-jdk-alpine AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven wrapper and pom.xml first to cache dependencies
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make the Maven wrapper executable
RUN chmod +x ./mvnw

# Download dependencies (this step is cached unless pom.xml changes)
RUN ./mvnw dependency:go-offline -B

# Copy the actual project source code
COPY src src

# Build the application, skipping tests to speed up the build on Render
RUN ./mvnw clean package -DskipTests

# ==========================================
# Stage 2: Run the Application
# ==========================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built JAR file from the 'build' stage
COPY --from=build /app/target/worklog-0.0.1-SNAPSHOT.jar app.jar

# Render dynamically assigns a port via the $PORT environment variable.
# We map it to Spring Boot's SERVER_PORT. Default to 8080 if not provided.
ENV SERVER_PORT=${PORT:-8080}

# Expose the port so the outside world can reach it
EXPOSE ${SERVER_PORT}

# Run the Spring Boot application
ENTRYPOINT ["sh", "-c", "java -jar app.jar"]
