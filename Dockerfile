FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app
COPY pom.xml .
RUN mvn --batch-mode dependency:go-offline

COPY src ./src
RUN mvn --batch-mode clean package

FROM eclipse-temurin:21-jre

WORKDIR /app
RUN mkdir -p /app/data
COPY --from=builder /app/target/uno-cli.jar /app/uno-cli.jar

VOLUME ["/app/data"]
ENTRYPOINT ["java", "-jar", "/app/uno-cli.jar"]
CMD ["--bots", "3", "--games", "1", "--quiet"]
