# 🎬 OTT Service Backend API Engine

> **Spring Boot 3 / Java 17** 기반의 OTT 서비스 백엔드 엔진 프로젝트입니다.  
> 객체지향적 도메인 설계, 전역 예외 처리 체계(`@RestControllerAdvice`), `JUnit5 & MockMvc` 단위 테스트, 그리고 **Docker 및 GitHub Actions 기반의 자동화된 CI/CD 파이프라인**을 갖추고 있습니다.

---

## 🏗️ 1. 백엔드 아키텍처 (Backend Architecture)

프로젝트는 계층형 아키텍처(Layered Architecture)를 준수하며, 역할과 책임을 명확히 분리하여 유지보수성과 확장성을 극대화했습니다.

[ Client Request ]
│
▼
┌───────────────────────────────┐
│       Controller Layer        │ ➔ HTTP 요청 입출력 검증 및 DTO 반환
└──────────────┬────────────────┘
│
▼
┌───────────────────────────────┐
│        Service Layer          │ ➔ 비즈니스 로직 처리 및 예외 검증
└──────────────┬────────────────┘
│
▼
┌───────────────────────────────┐
│       Repository Layer        │ ➔ 데이터베이스(JPA/H2) 접근 및 쿼리 수행
└───────────────────────────────┘

### 🛠️ Tech Stack

* **Language:** Java 17
* **Framework:** Spring Boot 3.x, Spring Data JPA
* **Database:** H2 Database (Dev/Test), MySQL (Production)
* **Testing:** JUnit5, AssertJ, MockMvc
* **DevOps & Infrastructure:** Docker, Docker Hub, GitHub Actions

---

## 🛡️ 2. 전역 예외 처리 전략 (Global Exception Handling)

애플리케이션 전반에서 발생하는 비즈니스 예외와 검증 오류를 일관된 형식으로 처리하기 위해 **`@RestControllerAdvice`**와 커스텀 예외 체계를 구축했습니다.

### 📌 Exception Flow

1. 비즈니스 로직 검증 실패 시 `CustomException(ErrorCode)` 발생
2. `@RestControllerAdvice`가 예외를 캡처
3. 규격화된 `ErrorResponse` DTO로 변환하여 표준 HTTP Status Code 및 JSON 반환

```json
{
  "timestamp": "2026-08-05T16:00:00.000000",
  "status": 404,
  "code": "MEDIA_NOT_FOUND",
  "message": "해당 미디어 콘텐츠를 찾을 수 없습니다."
}

🧪 단위 및 통합 테스트 (MockMvc)
JUnit5 및 MockMvc를 활용해 정상 흐름뿐만 아니라 예외 상황(400 Bad Request, 404 Not Found 등)이 발생했을 때 정확한 ErrorResponse 구조를 반환하는지 검증하는 테스트 코드를 작성했습니다.

🐳 3. Docker 컨테이너화 (Containerization)
운영 환경과의 차이를 방지하고 어떤 환경에서든 동일하게 작동하도록 멀티 스테이지 빌드(Multi-stage build) 방식의 Dockerfile을 작성했습니다.

# 1단계: Gradle 빌드 환경
FROM gradle:8.5-jdk17 AS builder
WORKDIR /app

COPY gradle gradle
COPY gradlew build.gradle settings.gradle ./
COPY src src

# gradlew 실행 권한 부여 후 빌드 진행
RUN chmod +x ./gradlew
RUN ./gradlew build -x test --no-daemon

# 2단계: 최적화된 실행 환경 (경량화 JRE)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

🚀 4. CI/CD 파이프라인 (GitHub Actions & Docker Hub)
main 브랜치에 코드가 푸시되면 GitHub Actions 로봇이 가상 서버에서 테스트 및 빌드를 검증한 후, Docker Hub 중앙 창고로 컨테이너 이미지를 자동 배포합니다.

[git push main] 
      │
      ▼
┌──────────────────────────────────────────────┐
│  GitHub Actions Runner (Ubuntu 가상 환경)      │
│  ├─ 1. 소스 코드 체크아웃                      │
│  ├─ 2. JDK 17 및 Gradle 캐시 설정             │
│  ├─ 3. ./gradlew test build (단위 테스트 검증) │
│  └─ 4. Docker Buildx 빌드 & Docker Hub 로그인│
└──────────────────────┬───────────────────────┘
                       │
                       ▼ (push success)
┌──────────────────────────────────────────────┐
│             Docker Hub Repository            │
│       gyseon/ott-service-app:latest          │
└──────────────────────────────────────────────┘

⚡ 주요 CI/CD 특징
Fail-Fast 검증: 단위 테스트(JUnit)가 하나라도 실패할 경우 Docker 빌드 단계로 넘어가지 않고 즉시 빌드가 차단되어, 오류가 포함된 소스가 운영 창고로 흘러 들어가는 것을 완전히 방지합니다.

레이어 캐싱 (GHA Cache): Gradle 및 Docker 레이어 캐시를 활용하여 CI/CD 실행 시간을 단축했습니다.

롤백(Rollback) 친화적: 태그 기반 이미지 관리를 통해 장애 발생 시 수 초 만에 이전 정상 버전 이미지로 컨테이너를 원복할 수 있습니다.

🏃 Quick Start (로컬 실행 방법)
Docker 환경에서 즉시 실행하기
Docker Hub에 올려진 최신 배포 이미지를 불러와 서버를 실행합니다.

# 1. 최신 이미지 끌어오기
docker pull gyseon/ott-service-app:latest

# 2. 컨테이너 띄우기 (8080 포트)
docker run -d -p 8080:8080 --name ott-backend gyseon/ott-service-app:latest

# 3. 실시간 서버 로그 확인
docker logs -f ott-backend
