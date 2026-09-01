# ---- Build stage ----
FROM gradle:9.1.0-jdk21 AS build
WORKDIR /app

COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle ./gradle
COPY src ./src

RUN --mount=type=cache,target=/root/.gradle gradle build -x test --no-daemon

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

COPY --from=build /app/build/libs/app.jar app.jar

EXPOSE 8080
ENV JAVA_OPTS="-Xmx768m -XX:MaxMetaspaceSize=128m -XX:MaxDirectMemorySize=64m -XX:ReservedCodeCacheSize=64m -Xss512k -XX:G1HeapRegionSize=4m"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
