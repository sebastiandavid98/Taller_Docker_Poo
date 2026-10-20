# ============================================================
# TALLER DOCKER - eco-air-backend
# Estudiante ID: 908407
# Dockerfile multietapa: builder + runtime
# ============================================================


# ============================================================
# ETAPA 1: builder
# Usamos Maven con Temurin 17 sobre Alpine para compilar
# ============================================================
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

# Argumento de construccion obligatorio: ID del estudiante
# Se inyecta con --build-arg STUDENT_ID="908407"
ARG STUDENT_ID

# Mostramos el ID en los logs de construccion para auditoria
RUN echo "Compilando para estudiante: ${STUDENT_ID}"

# Directorio de trabajo dentro del contenedor builder
WORKDIR /build

# -------------------------------------------------------
# TRUCO DE CACHE: copiamos SOLO el pom.xml primero.
# Asi Docker descarga las dependencias una sola vez
# y las reutiliza si el codigo fuente no cambia.
# -------------------------------------------------------
COPY pom.xml .

# Descargamos todas las dependencias en modo offline (sin internet real)
# La flag -B significa "batch mode": sin preguntas interactivas
RUN mvn dependency:go-offline -B

# Ahora si copiamos el codigo fuente
COPY src/ ./src/

# Compilamos y empaquetamos el JAR, saltando los tests
# -DskipTests evita que fallen tests en tiempo de build de Docker
RUN mvn package -DskipTests -B


# ============================================================
# ETAPA 2: runtime
# Imagen minima: solo el JRE (no el JDK completo, ni Maven)
# Esto reduce el tamano final y la superficie de ataque
# ============================================================
FROM eclipse-temurin:17-jre-alpine

# Reutilizamos el argumento para grabarlo como variable de entorno
ARG STUDENT_ID

# Variable de entorno obligatoria del taller
ENV OPERATOR_ID=${STUDENT_ID}

# Variables ergonomicas para la JVM:
# -XX:+UseContainerSupport  -> la JVM lee los limites de memoria del cgroup
# -XX:MaxRAMPercentage=75.0 -> usa maximo el 75% de la RAM del contenedor
# -Djava.security.egd      -> arranca mas rapido usando /dev/urandom
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -Djava.security.egd=file:/dev/./urandom"

# -------------------------------------------------------
# SEGURIDAD: creamos un usuario sin privilegios
# UID 10001 es un numero alto para evitar colisiones con el host
# No tiene shell (-s /bin/false) ni directorio home real
# -------------------------------------------------------
RUN addgroup -S sensorgroup && \
    adduser -S -u 10001 -G sensorgroup -s /bin/false sensor_user

# Directorio donde vivira el JAR en el contenedor final
WORKDIR /app

# Copiamos SOLO el JAR desde la etapa builder
# El wildcard *.jar captura el nombre sin importar la version exacta
COPY --from=builder /build/target/*.jar app.jar

# Le damos propiedad del directorio al usuario sin privilegios
RUN chown -R sensor_user:sensorgroup /app

# Exponemos el puerto de la aplicacion
EXPOSE 8080

# Cambiamos al usuario sin privilegios ANTES del punto de entrada
# A partir de aqui, nada corre como root
USER sensor_user

# Punto de entrada: arranca la JVM con las opciones configuradas
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
