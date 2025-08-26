# Use official OpenJDK 17 image
FROM eclipse-temurin:17-jdk-jammy as builder

# Set working directory
WORKDIR /app

# Copy Maven files
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
COPY src src

# Give execute permission to mvnw and build the application
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

# Second stage to create a smaller runtime image
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy the built jar from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the port your app runs on
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
