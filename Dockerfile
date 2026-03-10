FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# Copiar todo el proyecto
COPY . .

# Dar permisos a Maven
RUN chmod +x mvnw

# Compilar el proyecto
RUN ./mvnw clean package -DskipTests

# Ejecutar el jar generado
CMD java -jar target/*.jar