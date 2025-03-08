FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Bağımlılıkları kopyalayın ve yükleyin
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

# Kaynak kodunu kopyalayın ve build edin
COPY src ./src
RUN ./mvnw package -DskipTests

# Çalıştırma aşaması
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/talepyonetimbackend-0.0.1-SNAPSHOT.jar app.jar

# Port'u açıkça belirtin
ENV SERVER_PORT=8080
EXPOSE 8080

# Uygulamayı başlatın ve port'u açıkça belirtin
CMD ["java", "-jar", "-Dserver.port=8080", "app.jar"]