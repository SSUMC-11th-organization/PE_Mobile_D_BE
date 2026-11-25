# ---------- 1) 빌드 단계: 소스코드 → jar ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN ./gradlew bootJar -x test --no-daemon

# ---------- 2) 실행 단계: jar만 가볍게 실행 ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
