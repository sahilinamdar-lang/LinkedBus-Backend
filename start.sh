#!/bin/bash
set -e

# Move into the Spring Boot project directory
cd redbus-backend

# Use the Maven wrapper if available, otherwise use system Maven
if [ -f "./mvnw" ]; then
  ./mvnw clean package -DskipTests
else
  mvn clean package -DskipTests
fi

# Run Spring Boot JAR on port 8080
JAR_FILE=$(ls target/*.jar | head -n 1)
java -jar "$JAR_FILE" --server.port=8080
