# Stage 1: Build the spring boot application
FROM eclipse-temurin:25-jdk AS build

WORKDIR /workspace

# Copy maven wrappaer and project configuration
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN chmod +x mvnw

# Download dependencies before copying source code
# This improve docker layer caching
RUN ./mvnw -B dependency:go-offline


# Copy source code and package the application
COPY src/ src/

RUN ./mvnw -B -DskipTests package

RUN set -eux; \
    JAR="$(find target -maxdepth 1 -type f -name '*.jar' ! -name '*.original' | head -n 1)"; \
    test -n "$JAR"; \
    cp "$JAR" /workspace/app.jar


# Stage 2: Run the application
FROM eclipse-temurin:25-jre

# Install trusted CA certificated adn timezone data
RUN apt-get update \
    && apt-get install -y --no-install-recommends \
      ca-certificates \
      tzdata \
    && rm -rf /var/lib/apt/lists/*

# Create a non-root application user and give it ownership of /app
RUN groupadd --system app \
    && useradd --system --gid app --home-dir /app \
        --shell /usr/sbin/nologin app \
    && mkdir -p /app \
    && chown app:app /app

WORKDIR /app

COPY --from=build --chown=app:app /workspace/app.jar app.jar

USER app

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]