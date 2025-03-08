FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Önce bağımlılıkları kopyalayın (önbelleği iyileştirmek için)
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

# Çevre değişkenlerini tanımlayın
ENV SPRING_DATASOURCE_URL=${JDBC_DATABASE_URL}
ENV SPRING_DATASOURCE_USERNAME=${JDBC_DATABASE_USERNAME}
ENV SPRING_DATASOURCE_PASSWORD=${JDBC_DATABASE_PASSWORD}
ENV SPRING_PROFILES_ACTIVE=prod
ENV PORT=8080

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]