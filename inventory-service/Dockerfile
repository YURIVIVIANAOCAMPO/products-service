# Etapa 1: Compilación
FROM maven:3.9.6-eclipse-temurin-22-jammy AS build
WORKDIR /app

# Copiamos el POM padre y la carpeta del servicio
COPY pom.xml .
COPY inventory-service ./inventory-service

# Compilamos el servicio específico
RUN mvn clean package -pl inventory-service -am -DskipTests

# Etapa 2: Imagen de ejecución
FROM eclipse-temurin:22-jre-jammy
WORKDIR /app
COPY --from=build /app/inventory-service/target/*.jar app.jar

# Configuración de puerto por defecto de Render
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
