# Media Service
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app

COPY gradlew .
COPY gradle ./gradle
COPY build.gradle settings.gradle ./
COPY src ./src

RUN chmod +x gradlew \
    && ./gradlew bootJar -x test --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app

# Media files will live in /app/media-storage (bind-mounted)
VOLUME ["/app/media-storage"]

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8087

ENTRYPOINT ["java","-jar","/app/app.jar"]
