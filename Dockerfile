# syntax=docker/dockerfile:1.7

FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

COPY mvnw pom.xml ./
COPY .mvn .mvn

RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -q -DskipTests dependency:go-offline

COPY src src

RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -q -DskipTests clean package

FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring spring

COPY --from=build /workspace/target/*.jar app.jar

USER spring

EXPOSE 8081

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-XX:+ExitOnOutOfMemoryError", "-jar", "/app/app.jar"]
