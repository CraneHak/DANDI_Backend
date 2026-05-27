# DANDI 백엔드 서버

> **단디(DANDI) 백엔드** — 단국대학교 분실물 서비스의 인증, 분실물 API, 이미지 업로드, 알림/챗봇 정책을 처리하는 **Spring Boot 서버**입니다.

프론트(`capstone-front`)가 사용자 화면을 담당한다면, 이 저장소는 로그인 검증부터 분실물 데이터 저장까지 실제 동작 로직을 담당합니다.

---

## 이 서버는 어떤 역할을 하나요?

백엔드는 클라이언트 요청을 받아서 DB와 외부 서비스를 연결하는 중심 레이어입니다.

| 영역 | 하는 일 |
|------|---------|
| **인증/권한** | Firebase 토큰 검증, 도메인 제한, 관리자 권한 확인 |
| **분실물 API** | 조회/등록/삭제 등 분실물 관련 REST API 제공 |
| **파일 처리** | 멀티파트 업로드 처리 및 S3 연동 |
| **운영 정책** | 챗봇 호출 제한, CORS 허용 도메인 적용 |

---

## 공개 저장소 운영 기준

- 이 저장소는 Public입니다.
- `.env`, 인증 JSON, 개인 키 파일은 커밋하지 않습니다.
- 키/비밀번호 노출 의심 시 즉시 재발급(rotate) 후 교체합니다.

---

## 실행 환경

- Java 17
- Gradle Wrapper (`./gradlew`)
- Docker Desktop (선택: 로컬 MySQL 테스트 시)

---

## 로컬에서 실행 (개발자)

```bash
git clone https://github.com/CraneHak/DANDI_Backend.git
cd DANDI_Backend
```

### 1) `gradle.properties` 생성 (권장)

루트 경로에 `gradle.properties`를 생성하고 Java 17 경로를 지정합니다.

```properties
org.gradle.java.installations.paths=C:\\Users\\<your-user>\\AppData\\Local\\Programs\\Eclipse Adoptium\\jdk-17.0.x.x-hotspot
```

### 2) `.env` 생성

루트의 `.env.example`을 참고해 `.env`를 생성합니다.

핵심 변수:

- `DB_PASSWORD`
- `AWS_ACCESS_KEY`
- `AWS_SECRET_KEY`
- `S3_BUCKET_NAME`
- `FIREBASE_ADMIN_SDK_PATH`
- `FIREBASE_ALLOWED_DOMAIN`
- `FIREBASE_ADMIN_UIDS`
- `FIREBASE_ADMIN_EMAILS`
- `GOOGLE_APPLICATION_CREDENTIALS`
- `LLM_API_KEY`

Windows 경로 예시:

```dotenv
FIREBASE_ADMIN_SDK_PATH=C:\Users\<your-user>\.secrets\firebase\myauth.json
GOOGLE_APPLICATION_CREDENTIALS=C:\Users\<your-user>\.secrets\cloudvisionapi\cloudvisionapi_cash.json
```

### 3) 서버 실행

```bash
./gradlew bootRun
```

- 백엔드 기본 주소: `http://localhost:8080`
- 로그에 `Started Main`이 보이면 정상 실행입니다.

---

## 선택: 로컬 DB 테스트

운영은 AWS RDS 기준이며, 로컬에서만 DB를 별도로 띄워 테스트할 때 사용합니다.

```bash
docker-compose up -d
docker ps
```

---

## 주요 엔드포인트 예시

- `GET /api/lost-items`
- `GET /api/lost-items/{id}`
- `POST /api/lost-items` (multipart/form-data)
- `DELETE /api/lost-items/{id}`

---

## 트러블슈팅

### Java 17 인식 실패

`gradle.properties`의 Java 경로가 실제 설치 경로와 일치하는지 확인하세요.

### bash 환경에서 실행할 때

WSL/Git Bash에서는 `gradlew.bat` 대신 아래를 사용합니다.

```bash
./gradlew bootRun
```

---

## 협업 흐름

```bash
git pull origin main
git checkout -b feature/<topic>
# 작업
git push origin feature/<topic>
```

---

## 한 줄 요약

**단디 백엔드 = 인증, 분실물 API, 업로드, 운영 정책을 담당하는 Spring Boot 기반 서버입니다.**
