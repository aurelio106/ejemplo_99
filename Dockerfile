# Usar Java 21
FROM eclipse-temurin:21-jdk-jammy

# Carpeta de trabajo
WORKDIR /app

# Copiar el jar
COPY target/*.jar app.jar

# Ejecutar la aplicación
ENTRYPOINT ["java","-jar","app.jar"]