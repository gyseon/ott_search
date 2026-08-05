#FROM ubuntu:latest
#LABEL authors="Seon gayeong"
#
#ENTRYPOINT ["top", "-b"]

# 1단계: Gradle 빌드용 베이스 이미지 (JDK 17)
FROM gradle:8.5-jdk17 AS builder
WORKDIR /app

# Gradle 의존성 라이브러리 캐싱 및 소스 코드 복사
COPY gradle gradle
COPY gradlew build.gradle settings.gradle ./
COPY src src

#gradlew 파일에 실행 권한(+x) 부여
RUN chmod +x ./gradlew
# 애플리케이션 빌드 (테스트 제외로 빠른 빌드)
RUN ./gradlew build -x test --no-daemon

# 2단계: 실행용 경량 베이스 이미지 (JDK 17 Slim)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 빌드 단계에서 생성된 jar 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 8080 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]