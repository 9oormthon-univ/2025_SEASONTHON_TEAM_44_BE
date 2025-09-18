<img width="2048" height="1152" alt="image" src="https://github.com/user-attachments/assets/139de1e6-0e32-4371-8c2e-789099d4d245" />


# 다시온 Backend (TEAM 44)

Java Spring Boot Gradle MySQL S3 Kakao OAuth GitHub Actions

구름톤 유니브 시즌톤을 위해 개발된 **다시온 서비스 백엔드 애플리케이션**입니다.  
매장 관리, 단골 관리, 스탬프 적립/조회, 알림/쿠폰 발급 등 사장님 전용 기능을 제공하며,  
AI 모듈과 연동하여 업종 추천, 챗봇 기능까지 확장됩니다.

---

## ✨ 주요 기능
- **회원/인증**
  - Kakao OAuth 로그인 지원
  - JWT 기반 인증/인가
- **매장 관리**
  - 사장님의 매장 등록, 조회, 수정
  - 메뉴판 이미지 업로드 (AWS S3 Presigned URL 활용)
- **단골 관리**
  - 스탬프 적립/조회/사용 API
  - 단골 고객 방문 이력 관리
- **알림 및 쿠폰**
  - 공지 발송 및 확인 여부 조회
  - 쿠폰 발급/사용 관리
- **대시보드**
  - 사장님 전용 대시보드 데이터 제공 (방문 현황, 단골 통계 등)
- **AI 연동**
  - AI 서버와 통신하여 업종 추천/챗봇 서비스 제공

---

## 🛠️ 기술 스택

| 구분        | 기술 |
| ----------- | ---- |
| **Backend** | ![Java](https://img.shields.io/badge/Java_21-007396?logo=openjdk&logoColor=white) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?logo=springboot&logoColor=white) ![Gradle](https://img.shields.io/badge/Gradle-02303A?logo=gradle&logoColor=white) |
| **Database** | ![MySQL](https://img.shields.io/badge/MySQL-4479A1?logo=mysql&logoColor=white) |
| **Storage** | ![AWS S3](https://img.shields.io/badge/AWS_S3-569A31?logo=amazonaws&logoColor=white) |
| **Auth** | ![Kakao OAuth](https://img.shields.io/badge/Kakao_OAuth-FFCD00?logo=kakao&logoColor=black) ![JWT](https://img.shields.io/badge/JWT-000000?logo=jsonwebtokens&logoColor=white) |
| **DevOps** | ![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?logo=githubactions&logoColor=white) |

---

## 🗂️ 프로젝트 구조
```bash
/
├── src/ # Spring Boot 소스 코드 (Controller, Service, Repository, Entity 등)
├── build.gradle # Gradle 빌드 스크립트
├── settings.gradle # 프로젝트 설정
├── gradlew / gradlew.bat # Gradle 실행 스크립트
├── gradle/ # Gradle Wrapper
├── Dockerfile # 컨테이너 빌드 설정
├── .dockerignore # Docker 빌드시 제외할 파일
└── .github/workflows/ # CI/CD 설정 (GitHub Actions)
```
---

## 🚀 시작하기

### 로컬 실행
```bash
./gradlew bootRun
```

### 환경 변수 (application.yml 예시)
```bash
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/dasion
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: your-db-username
    password: your-db-password
  jpa:
    database-platform: org.hibernate.dialect.MySQLDialect
    properties:
      hibernate:
        show-sql: true
        format-sql: true
    hibernate:
      ddl-auto: update

kakao:
  client_id: your-kakao-client-id
  redirect_uri: http://localhost:8080/callback

swagger:
  server:
    url: http://localhost:8080

jwt:
  secret: your-jwt-secret-key

aws:
  region: ap-northeast-2
  s3:
    bucket: your-s3-bucket-name
    presignExpireSec: 300   
    viewExpireSec: 86400

cloud:
  aws:
    s3:
      bucket: your-s3-bucket-name
    region:
      static: ap-northeast-2
    credentials:
      access-key: your-aws-access-key
      secret-key: your-aws-secret-key

google:
  maps:
    api:
      key: your-google-maps-api-key

server:
  port: 8080
```

---

## 📌 특징

- **Kakao OAuth 로그인**으로 간편한 회원 인증
- **AWS S3 Presigned URL**을 통한 안전한 이미지 업로드/조회
- **JWT 기반 인증/인가**로 보안 강화
- **도메인별 레이어드 아키텍처**로 유지보수 용이
- **GitHub Actions** 기반 CI/CD 자동화 지원
- **AI 서버 연동** (업종 추천, 챗봇)

