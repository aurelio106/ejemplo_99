# Usar Java 21
FROM eclipse-temurin:21-jdk-jammy

# Carpeta de trabajo dentro del contenedor
WORKDIR /app

# Copiar todo el proyecto al contenedor
COPY . .

# Dar permiso al wrapper de Maven
RUN chmod +x mvnw

# Compilar la aplicación
RUN ./mvnw clean package -DskipTests

# Ejecutar la aplicación
CMD ["java","-jar","target/*.jar"]