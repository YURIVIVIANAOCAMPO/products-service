# Etapa 1: Compilación
FROM maven:3.9.6-eclipse-temurin-22-jammy AS build
WORKDIR /app

# Copiamos TODOS los poms para que Maven no se queje
COPY pom.xml .
COPY inventory-service/pom.xml ./inventory-service/
COPY products-service/pom.xml ./products-service/

# Copiamos solo el código fuente de productos
COPY products-service/src ./products-service/src

# Compilamos
RUN mvn clean package -pl products-service -am -DskipTests

# Etapa 2: Imagen de ejecución
FROM eclipse-temurin:22-jre-jammy
WORKDIR /app
COPY --from=build /app/products-service/target/*.jar app.jar

# Configuración de puerto por defecto de Render
EXPOSE 8080

ENTRYPOINT ["java", "-Xmx384m", "-jar", "app.jar"]
