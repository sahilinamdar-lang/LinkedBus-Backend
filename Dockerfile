# ---- Build stage ----
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

COPY redbus-backend/pom.xml ./pom.xml
COPY redbus-backend/src ./src

RUN mvn clean package -DskipTests

# ---- Run stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENV PORT=8080

CMD ["java", "-Dserver.port=${PORT}", "-jar", "app.jar"]
