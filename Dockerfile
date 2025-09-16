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

# Create non-root user for security
RUN addgroup -g 1001 appgroup && \
    adduser -u 1001 -G appgroup -s /bin/sh -D appuser

# Copy the built application
COPY --from=build /app/build/install/BetweenUsServe .
COPY --from=build /app/.env .

# Change ownership to app user
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

EXPOSE 8080

# Memory-optimized JVM startup with container awareness
ENV JAVA_OPTS="-Xmx256m -Xms128m \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=100 \
    -XX:+UseStringDeduplication \
    -XX:MaxDirectMemorySize=64m \
    -XX:MaxRAMPercentage=75.0 \
    -XX:+UseContainerSupport \
    -Dio.netty.allocator.maxOrder=9 \
    -Dio.netty.leakDetection.level=disabled \
    -Dio.netty.noUnsafe=true"

# Create startup script with memory optimization
RUN echo '#!/bin/sh' > start.sh && \
    echo 'exec java $JAVA_OPTS -cp "lib/*" com.aatech.ApplicationKt "$@"' >> start.sh && \
    chmod +x start.sh

CMD ["./start.sh"]
