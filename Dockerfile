# --- Aşama 1: derleme ---
# Maven + JDK 21 imajında projeyi derleyip JAR üret.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Önce pom.xml'i kopyalayıp bağımlılıkları indir (katman önbelleği için).
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Kaynağı kopyala ve derle (testleri atla — derleme hızlansın).
COPY src ./src
RUN mvn clean package -DskipTests -B

# --- Aşama 2: çalıştırma ---
# Yalnızca JRE içeren küçük imaj; üretilen JAR'ı buraya alıp çalıştır.
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Render, dinlenecek portu PORT ortam değişkeniyle verir;
# application.yml bunu ${PORT:8080} ile okuyor.
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]