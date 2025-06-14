FROM maven:3.9-eclipse-temurin-23-alpine AS build
WORKDIR /app
COPY .. .
RUN mvn clean install -DskipTests
FROM eclipse-temurin:23-jre-alpine
WORKDIR /app
COPY --from=build /app/bot/target/*.jar app.jar
EXPOSE 8081 8201
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
