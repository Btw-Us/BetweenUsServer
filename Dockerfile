# Stage 1: Build the application
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Install Gradle
RUN apk add --no-cache gradle

# Copy build files and source
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradle.properties .
COPY .env .
COPY src src

# Build the application
RUN gradle installDist

# Stage 2: Create the final image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built application
COPY --from=build /app/build/install/BetweenUsServe .
COPY --from=build /app/.env .

EXPOSE 8080
CMD ["bin/BetweenUsServe"]