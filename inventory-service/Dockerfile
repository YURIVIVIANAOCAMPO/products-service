# Etapa 1: Compilación
FROM maven:3.9.6-eclipse-temurin-22-jammy AS build
WORKDIR /app
# Copiamos el pom y descargamos dependencias (cache)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiamos el código fuente y compilamos
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Imagen de ejecución
FROM eclipse-temurin:22-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Configuración de puerto por defecto de Render
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
