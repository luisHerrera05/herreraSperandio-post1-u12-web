# ── Etapa 1: compilación con Maven ──────────────────────────────────
# Se usa una imagen que YA trae Maven + JDK 21 (eclipse-temurin:21-jdk-alpine no incluye Maven).
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copiar pom.xml primero para aprovechar la caché de capas de Docker.
# Si las dependencias no cambian, Maven no las descarga en builds siguientes.
COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn clean package -DskipTests -q

# ── Etapa 2: imagen de ejecución (solo JRE) ──────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Usuario no root: buena práctica de seguridad en contenedores
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

# Copiar únicamente el JAR compilado desde la etapa de builder
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

# Healthcheck local de Docker (busybox wget viene en Alpine)
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget -qO- http://localhost:${PORT:-8080}/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
