#!/bin/bash
set -e

# Spring Boot project folder me jao
cd redbus-backend

# -----------------------------
# IMPORTANT: Windows se aane par
# mvnw executable nahi hota.
# Isliye yahan force se executable bana rahe hain.
# -----------------------------
chmod +x mvnw
chmod -R +x .mvn/

# Build the Spring Boot JAR
if [ -f "./mvnw" ]; then
  ./mvnw clean package -DskipTests
else
  mvn clean package -DskipTests
fi

# Run the JAR on port 8080
JAR_FILE=$(ls target/*.jar | head -n 1)
java -jar "$JAR_FILE" --server.port=8080
