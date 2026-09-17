# --- сборка ---
FROM eclipse-temurin:25-jdk AS build
WORKDIR /build

# Сначала только то, от чего зависят зависимости — слой переиспользуется,
# пока не меняется pom.xml, и пересборка не качает Maven-репозиторий заново.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

COPY src/ src/
RUN ./mvnw -B -q clean package -DskipTests

# --- запуск ---
FROM eclipse-temurin:25-jre
WORKDIR /app

# Не под root: если приложение скомпрометируют, прав в контейнере будет меньше
RUN useradd --system --create-home --uid 10001 app
USER app

COPY --from=build /build/target/*.jar app.jar

# Хостинг сам назначает порт и передаёт его в PORT
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
