# Etapa 1: Compilación
FROM maven:3.9.6-eclipse-temurin-22-jammy AS build
WORKDIR /app

# Copiamos TODOS los poms para que Maven no se queje
COPY pom.xml .
COPY inventory-service/pom.xml ./inventory-service/
COPY products-service/pom.xml ./products-service/

# Copiamos solo el código fuente del inventario
COPY inventory-service/src ./inventory-service/src

# Compilamos
RUN mvn clean package -pl inventory-service -am -DskipTests

# Etapa 2: Imagen de ejecución
FROM eclipse-temurin:22-jre-jammy
WORKDIR /app
COPY --from=build /app/inventory-service/target/*.jar app.jar

# Configuración de puerto por defecto de Render
EXPOSE 8081

ENTRYPOINT ["java", "-Xmx384m", "-jar", "app.jar"]
