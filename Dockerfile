# =================================================================
# STAGE 1: Build Stage - JDK를 사용하여 애플리케이션을 빌드합니다.
# =================================================================
FROM openjdk:21-jdk-slim as builder

# 작업 디렉토리 설정
WORKDIR /workspace/app

# Gradle Wrapper 파일들을 먼저 복사합니다.
COPY gradlew .
COPY gradle gradle

# Gradle 의존성 캐싱을 위해 빌드 파일들을 먼저 복사합니다.
COPY build.gradle settings.gradle ./

# 소스 코드를 복사하기 전에 의존성을 먼저 다운로드합니다.
# 이렇게 하면 소스 코드만 변경되었을 때 이 레이어는 캐시되어 빌드 속도가 빨라집니다.
RUN ./gradlew dependencies

# 소스 코드를 복사합니다.
COPY src ./src

# Gradle을 사용하여 애플리케이션을 빌드합니다. (테스트는 CI 단계에서 수행하므로 제외)
RUN ./gradlew build -x test


# =================================================================
# STAGE 2: Final Stage - 빌드된 JAR 파일만으로 최종 이미지를 만듭니다.
# =================================================================
FROM openjdk:21-jdk-slim

# 작업 디렉토리 설정
WORKDIR /app

# builder 스테이지에서 빌드된 JAR 파일만 복사해옵니다.
COPY --from=builder /workspace/app/build/libs/*.jar app.jar

# 스프링 부트 기본 포트 8080 노출
EXPOSE 8080

# 컨테이너 실행 시 애플리케이션 JAR 파일을 실행합니다.
ENTRYPOINT ["java", "-jar", "app.jar"]