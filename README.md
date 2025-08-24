# FlyKids Backend 🛩️

이 저장소는 **FlyKids** 서비스의 백엔드 API 서버입니다.  
FlyKids는 초등학생을 위한 **태블릿 기반 드론 조작 교육 서비스**로, 드론 비행 미션, 튜토리얼, AI 음성 피드백 등의 기능을 제공합니다.

---

## ✨ 주요 기능

### 🎯 미션 관리
- 드론 비행 미션 생성 및 관리
- 미션 진행 상황 추적
- 성과 분석 및 점수 계산
- 리더보드 및 순위 시스템

### 🎓 튜토리얼 시스템
- 단계별 드론 조작 가이드
- 음성 안내 및 피드백
- 실패 시 맞춤형 도움말 제공

### 🤖 AI 음성 서비스
- TTS(Text-to-Speech) 기능
- 실시간 음성 피드백
- 다양한 상황별 음성 안내

### 🚁 드론 제어 및 모니터링
- 실시간 드론 위치 추적
- 경로 설정 및 관리
- 비행 로그 및 경로 이탈 기록

### 👤 사용자 관리
- JWT 기반 인증 시스템
- 사용자 프로필 관리
- 미션 진행도 추적

---

## 📦 기술 스택

- **언어**: Java 17
- **프레임워크**: Spring Boot 3.5.0
- **보안**: Spring Security + JWT
- **데이터베이스**: Spring Data JPA + PostgreSQL
- **API 문서**: SpringDoc OpenAPI (Swagger)
- **HTTP 클라이언트**: WebFlux, RestTemplate
- **빌드 도구**: Gradle
- **개발 도구**: Lombok, Spring DevTools

---

## 🚀 시작하기

### 필수 요구사항
- Java 17 이상
- PostgreSQL 데이터베이스
- Gradle 7.x 이상

### 환경 설정

1. **저장소 클론**
   ```bash
   git clone <repository-url>
   cd flykids-backend
   ```

2. **데이터베이스 설정**
   - PostgreSQL 서버가 실행 중인지 확인
   - `application.yml`의 데이터베이스 설정 확인

3. **애플리케이션 실행**
   ```bash
   # Gradle Wrapper 사용
   ./gradlew bootRun
   
   # 또는 직접 빌드 후 실행
   ./gradlew build
   java -jar build/libs/flykids-backend-0.0.1-SNAPSHOT.jar
   ```

4. **개발 모드 실행**
   ```bash
   ./gradlew bootRun --args='--spring.profiles.active=dev'
   ```

### 테스트 실행
```bash
./gradlew test
```

---

## 🏗️ 프로젝트 구조

```
src/main/java/com/mtvs/flykidsbackend/
├── FlykidsBackendApplication.java          # 메인 애플리케이션 클래스
├── config/                                 # 설정 클래스들
│   ├── SecurityConfig.java                # Spring Security 설정
│   ├── JwtAuthenticationFilter.java       # JWT 인증 필터
│   ├── CorsConfig.java                    # CORS 설정
│   └── SwaggerConfig.java                 # API 문서 설정
├── domain/                                # 도메인별 패키지
│   ├── user/                             # 사용자 관리
│   │   ├── controller/                   # 컨트롤러
│   │   ├── service/                      # 서비스 로직
│   │   ├── repository/                   # 데이터 접근
│   │   ├── entity/                       # JPA 엔티티
│   │   └── dto/                          # 데이터 전송 객체
│   ├── mission/                          # 미션 관리
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/
│   │   └── dto/
│   ├── drone/                            # 드론 제어
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/
│   │   └── dto/
│   ├── tutorial/                         # 튜토리얼 시스템
│   │   ├── controller/
│   │   ├── service/
│   │   └── dto/
│   └── ai/                              # AI 음성 서비스
│       ├── controller/
│       ├── service/
│       └── dto/
└── common/                              # 공통 유틸리티
    └── AudioFilePath.java              # 오디오 파일 경로 상수
```

---

## 📄 API 문서

개발 서버가 실행되면 다음 URL에서 API 문서를 확인할 수 있습니다:

👉 **[Swagger UI](http://localhost:8080/swagger-ui/index.html)**

### 주요 API 엔드포인트

#### 🔐 인증 (`/api/auth`)
- `POST /login` - 사용자 로그인
- `POST /signup` - 사용자 회원가입
- `POST /refresh` - 토큰 갱신

#### 👤 사용자 (`/api/users`)
- `GET /me` - 내 정보 조회
- `PUT /nickname` - 닉네임 변경
- `PUT /password` - 비밀번호 변경

#### 🎯 미션 (`/api/missions`)
- `GET /` - 미션 목록 조회
- `POST /` - 새 미션 생성
- `GET /{id}` - 특정 미션 조회
- `POST /results` - 미션 결과 제출

#### 🚁 드론 (`/api/drone`)
- `POST /position` - 드론 위치 업데이트
- `POST /routes` - 경로 설정
- `GET /routes` - 경로 조회

#### 🎓 튜토리얼 (`/api/tutorial`)
- `GET /audio/{step}` - 튜토리얼 음성 가이드

#### 🤖 AI 음성 (`/api/voice`)
- `POST /feedback` - 음성 피드백 생성

---

## 🔧 환경 변수

주요 설정값들은 `application.yml`에서 관리됩니다:

```yaml
# 데이터베이스 설정
spring.datasource.url: jdbc:postgresql://호스트:포트/데이터베이스명
spring.datasource.username: 사용자명
spring.datasource.password: 비밀번호

# JWT 설정
jwt.secret: JWT_시크릿_키
jwt.token-validity-in-seconds: 토큰_유효시간(초)

# AI 서비스 URL
ai.tts.url: TTS_서비스_URL
```

---

## 📝 개발 가이드

### 코드 스타일
- 모든 엔티티는 Lombok을 사용하여 보일러플레이트 코드 최소화
- 컨트롤러는 RESTful API 설계 원칙 준수
- 서비스 레이어에서 비즈니스 로직 처리
- DTO를 사용한 계층 간 데이터 전송

### 보안 고려사항
- JWT 토큰 기반 인증
- CORS 설정으로 크로스 도메인 요청 제어
- SQL Injection 방지를 위한 JPA 사용
- 비밀번호 암호화 저장

---

## 🤝 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📞 문의

프로젝트에 대한 문의사항이 있으시면 이슈를 생성해 주세요.

---

**FlyKids Backend** - 초등학생을 위한 드론 교육의 미래를 만들어갑니다 🚁✨
