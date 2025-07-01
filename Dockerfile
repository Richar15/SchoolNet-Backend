FROM eclipse-temurin:21.0.7_6-jdk

# Expone el puerto 8080 (Spring Boot)
EXPOSE 8080

# Directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiar archivos necesarios para construir la app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# ✅ Dar permisos de ejecución al wrapper de Maven
RUN chmod +x mvnw

# Descargar dependencias (sin compilar aún)
RUN ./mvnw dependency:go-offline

# Copiar el resto del código fuente
COPY src/ src/

# Compilar la aplicación
RUN ./mvnw clean package -DskipTests

# Ejecutar el .jar generado
ENTRYPOINT ["java", "-jar", "target/SchoolNet-0.0.1-SNAPSHOT.jar"]
