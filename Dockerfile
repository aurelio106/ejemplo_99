FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# Copiar el proyecto
COPY . .

# Dar permisos a Maven wrapper
RUN chmod +x mvnw

# Compilar la aplicación
RUN ./mvnw clean package -DskipTests

# Ejecutar la aplicación
CMD java -jar target/*.jar