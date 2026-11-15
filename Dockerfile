# ==========================================
# Etapa 1: Construcción del Backend (Java)
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21-jammy AS build
WORKDIR /build

# Optimizamos la caché de Docker copiando primero solo el POM
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiamos el código y compilamos
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# Etapa 2: Entorno de Producción Híbrido
# ==========================================
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# 1. Evitar prompts interactivos de apt y configurar zona horaria
ENV DEBIAN_FRONTEND=noninteractive
ENV TZ=America/Argentina/La_Rioja

# 2. Instalar Python 3, PIP y GDAL (Core y Bindings)
RUN apt-get update && apt-get install -y \
    python3 \
    python3-pip \
    python3-venv \
    gdal-bin \
    python3-gdal \
    && rm -rf /var/lib/apt/lists/*

# 3. Crear entorno virtual (Recomendación PEP 668) e instalar librerías
RUN python3 -m venv --system-site-packages /opt/venv
ENV PATH="/opt/venv/bin:$PATH"
COPY requirements.txt .
RUN pip3 install --no-cache-dir -r requirements.txt

# 4. Copiar los scripts de Python al contenedor
COPY scripts/ /app/scripts/

# 5. Copiar el artefacto de Spring Boot desde la Etapa 1
COPY --from=build /build/target/*.jar app.jar

# 6. Exponer el puerto de Spring Boot
EXPOSE 8080

# 7. Iniciar la aplicación
CMD ["java", "-jar", "app.jar"]