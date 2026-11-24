# Etapa 1: compilar la aplicación con Maven
FROM maven:3.10.0-eclipse-temurin-21 AS build
WORKDIR /app

# Primero solo el pom.xml, para que Docker reutilice la caché de dependencias
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q package -DskipTests

# Etapa 2: ejecutar el JAR con una imagen que solo tiene el JRE
FROM eclipse-temurin:21.0.13.1_2-jre
WORKDIR /app

# La aplicación no se ejecuta como root
RUN groupadd --system app && useradd --system --gid app app
USER app

COPY --from=build /app/target/pedidos.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
