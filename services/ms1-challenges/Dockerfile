# --- Build stage -------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B -q dependency:go-offline || true
COPY src ./src
RUN mvn -B -q -DskipTests package

# --- Runtime stage -----------------------------------------------------------
FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S enlice && adduser -S enlice -G enlice
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
RUN chown -R enlice:enlice /app
USER enlice
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]