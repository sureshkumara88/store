# Build stage (uses Gradle wrapper)
FROM gradle:8.9-jdk17 AS build
WORKDIR /store
# Copy Gradle wrapper (both script and wrapper JAR/properties)
COPY gradlew ./
COPY gradle/wrapper/ ./gradle/wrapper/
# Copy build files first for better caching (supports .kts too)
COPY settings.gradle* build.gradle* ./
# Copy sources
COPY src ./src
RUN chmod +x gradlew && ./gradlew clean bootJar --no-daemon

# Run stage
FROM openjdk:17
WORKDIR /store
COPY --from=build /store/build/libs/*.jar store.jar
EXPOSE 8080
ENV JAVA_OPTS=""
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar store.jar"]