FROM maven:3.9-eclipse-temurin-25 AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline --no-transfer-progress

COPY src ./src
RUN mvn package -DskipTests --no-transfer-progress

FROM eclipse-temurin:25-jre-noble

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && mkdir -p logs

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
