# Use an official OpenJDK runtime as the base image
FROM openjdk:17

# Set the working directory
WORKDIR /app

# Copy the JAR file from the target directory into the Docker image
COPY target/ATLAS-1.0.0-SNAPSHOT.jar /app/ATLAS-1.0.0-SNAPSHOT.jar

# Specify the entry point command for the container
ENTRYPOINT ["java", "-jar", "/app/ATLAS-1.0.0-SNAPSHOT.jar"]