# Backend Dockerfile for Spring Boot API (uses official Maven builder)
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace

# copy only pom first for utilising Docker layer caching
COPY pom.xml ./
RUN mvn -B -q dependency:go-offline

# copy sources and build
COPY src ./src
RUN mvn -B clean package -DskipTests -DskipDocs -DskipITs

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
