# Dockerfile

FROM eclipse-temurin:17-jdk-alpine as builder

WORKDIR /app

# Copia o projeto
COPY . .

# Compila o app
RUN ./gradlew build --no-daemon

# --------------------------------------------------

FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
