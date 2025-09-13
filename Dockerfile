# Stage 1: Build the application
FROM --platform=$BUILDPLATFORM eclipse-temurin:17-jdk-alpine AS build

# Set up build arguments for cross-compilation
ARG BUILDPLATFORM
ARG TARGETPLATFORM
ARG TARGETOS
ARG TARGETARCH

WORKDIR /app

# Install Gradle (will automatically install the correct version for the architecture)
RUN apk add --no-cache gradle

# Copy build files
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradle.properties .
COPY .env .

# Copy the rest of the source code
COPY src src

# Build the application using system gradle
RUN gradle installDist

# Stage 2: Create the final image
FROM eclipse-temurin:17-jre-alpine

# Set up platform arguments for runtime
ARG TARGETPLATFORM
ARG TARGETOS
ARG TARGETARCH

WORKDIR /app

# Copy the built application from the build stage
COPY --from=build /app/build/install/BetweenUsServe .
COPY --from=build /app/.env .

# Expose the port the application runs on
EXPOSE 8080

# Run the application
CMD ["bin/BetweenUsServe"]