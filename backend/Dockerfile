# =============================================================================
# MAFIOSO Backend - Multi-stage Build (Maven + Java 8 -> Apache Tomcat 9)
# =============================================================================

FROM maven:3.9.9-eclipse-temurin-8 AS builder
WORKDIR /build

# Resolve dependencies first to optimize Docker layer caching
COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline

# Copy application source, run unit tests, and package ROOT.war
COPY src ./src
RUN mvn -B clean package

FROM tomcat:9.0.124-jdk8-temurin-noble

# Install curl for container healthcheck, remove default sample webapps,
# and prepare private storage directory for future document uploads.
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && rm -rf /usr/local/tomcat/webapps/* \
    && mkdir -p /var/mafioso/storage/documents \
    && chmod 750 /var/mafioso/storage/documents

COPY --from=builder /build/target/ROOT.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=3 \
    CMD curl -fsS http://127.0.0.1:8080/api/v1/health || exit 1

CMD ["catalina.sh", "run"]
