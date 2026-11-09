FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY infrastructure/pom.xml infrastructure/
COPY infrastructure/src infrastructure/src
RUN mvn -q -pl infrastructure -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/infrastructure/target/*.jar app.jar
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
