# 물려받기 백엔드

문 닫을 대구 노포의 맛과 단골을 청년이 이어받게 하는 승계 서비스 **물려받기**의 API 서버예요. 2026 단디톤 출품작이에요.

## 기술 스택

- Java 21, Spring Boot 4.1, Gradle (Kotlin DSL)
- Spring Web MVC, Spring Data JPA, Spring Security, Validation
- MySQL 8.4 (Docker Compose), Flyway
- Spring AI + Claude (API 키를 넣으면 켜짐)
- 테스트: JUnit 5, H2 (MySQL 모드)

## 실행 방법

1. Docker Desktop을 켜요.
2. 서버를 실행해요. MySQL 컨테이너가 자동으로 함께 떠요.

```bash
./gradlew bootRun
```

- 서버: http://localhost:8080
- 상태 확인: http://localhost:8080/actuator/health
- MySQL: `localhost:3307` (DB·사용자·비밀번호 모두 `mullyeobatgi`). 이 PC의 3306은 다른 MySQL이 쓰고 있어서 3307을 써요.
- 서버를 꺼도 MySQL 컨테이너는 계속 떠 있어요. 끄려면 `docker compose down`, 데이터까지 지우려면 `docker compose down -v`.

DB가 비어 있으면 예시 데이터가 들어가요. 개발용 로그인에서 아래 이름과 역할로 들어가면 돼요. 모두 가상의 예시예요.

| 이름 | 역할 | 내용 |
|---|---|---|
| 김순자 | OWNER | 남문 손칼국수 (1987년, 중구, 인수 금액 4,800만 원) |
| 이도윤 | YOUTH | 한식 주방 2년, 자금 3,000만 원 + 대출 계획 |
| 박서연 | YOUTH | 분식집 운영 4년, 자금 5,000만 원 |
| 최민재 | YOUTH | 경력 1년 미만, 자금 1,500만 원 |

## 환경 변수

| 이름 | 기본값 | 설명 |
|---|---|---|
| `DB_HOST` / `DB_PORT` / `DB_NAME` | `localhost` / `3307` / `mullyeobatgi` | Docker 없이 다른 MySQL에 붙을 때 |
| `DB_USERNAME` / `DB_PASSWORD` | `mullyeobatgi` / `mullyeobatgi` | |
| `AI_CHAT_MODEL` | `none` | `anthropic` 으로 바꾸면 Claude를 써요 |
| `ANTHROPIC_API_KEY` | (없음) | Claude API 키 |
| `ANTHROPIC_MODEL` | `claude-sonnet-5-5` | |
| `FRONTEND_URL` | `http://localhost:5173` | 카카오 로그인 뒤 돌아갈 주소 |
| `CORS_ORIGINS` | `http://localhost:5173,http://localhost:3000` | 쉼표로 구분 |
| `DEV_LOGIN_ENABLED` | `true` | 개발용 로그인 켜기 |
| `DEMO_SEED` | `true` | 빈 DB에 예시 데이터 넣기 |
| `KAKAO_CLIENT_ID` / `KAKAO_CLIENT_SECRET` | (없음) | `kakao` 프로필에서만 필요 |

비밀값은 코드에 넣지 말고 환경 변수나 `.env`, `application-local.yml`에 넣으세요. 이 파일들은 `.gitignore`에 들어 있어요.

### AI 켜기

키가 없으면 매칭 점수와 이유는 규칙으로, 승계 계획은 템플릿으로 만들어요. 키를 넣으면 Claude가 매칭 한마디(`aiComment`)를 붙이고 승계 계획 초안을 다듬어요. AI 응답이 실패하면 템플릿으로 돌아가요.

```bash
AI_CHAT_MODEL=anthropic ANTHROPIC_API_KEY=sk-ant-... ./gradlew bootRun
```

### 카카오 로그인 켜기

```bash
KAKAO_CLIENT_ID=... KAKAO_CLIENT_SECRET=... ./gradlew bootRun --args='--spring.profiles.active=kakao'
```

프론트에서 `/oauth2/authorization/kakao`로 보내면, 로그인 후 `{FRONTEND_URL}/oauth/callback#token=...`으로 돌아와요. 처음 들어온 사용자는 `PUT /api/me/role`로 역할을 골라야 해요.

## 인증

로그인하면 토큰을 받아요. 이후 요청마다 `Authorization: Bearer {token}` 헤더를 붙여요.

```http
POST /api/auth/dev-login
{"name": "김순자", "role": "OWNER"}
```

같은 이름과 역할이면 같은 사용자로 들어가요. 없으면 새로 만들어요.

## API

금액은 모두 **만 원 단위** 정수예요. 오류는 `{"code": "...", "message": "화면에 그대로 보여 줄 한국어 문장"}` 형식이에요.

### 시작

| 메서드 | 경로 | 설명 |
|---|---|---|
| POST | `/api/auth/dev-login` | 개발용 로그인 |
| POST | `/api/auth/logout` | 로그아웃 |
| GET | `/api/me` | 내 정보, 가게·청년 정보 등록 여부, 진행 중인 승계 |
| PUT | `/api/me/role` | 역할 선택 `{"role": "OWNER" \| "YOUTH"}` |

### ① 가게·청년 등록

| 메서드 | 경로 | 누가 | 설명 |
|---|---|---|---|
| GET, PUT | `/api/owner/shop` | 사장님 | 가게 정보. 은퇴 희망일이 너무 가까우면 `RETIRE_DATE_TOO_SOON` |
| GET, PUT | `/api/youth/profile` | 청년 | 경력(`NONE`, `UNDER_1Y`, `ONE_TO_3Y`, `OVER_3Y`), 자금, 대출, 희망 업종·지역 |

### ② 매칭

| 메서드 | 경로 | 누가 | 설명 |
|---|---|---|---|
| GET | `/api/owner/matches` | 사장님 | 청년 후보, 적합도 점수(경력 30 + 자금 30 + 조건 40), 항목별 수준, 이유 |
| GET | `/api/youth/matches` | 청년 | 가게 후보 (둘러보기용) |

### ③ 제안·수락

| 메서드 | 경로 | 누가 | 설명 |
|---|---|---|---|
| POST | `/api/offers` | 사장님 | `{"youthId": 2, "message": "..."}` 제안 보내기. 14일 안에 답해야 해요 |
| GET | `/api/offers` | 둘 다 | 사장님은 보낸 제안, 청년은 받은 제안 |
| GET | `/api/offers/{id}` | 둘 다 | |
| POST | `/api/offers/{id}/accept` | 청년 | 수락하면 승계가 열리고 가일정·체크리스트가 만들어져요. 응답의 `successionId` 사용 |
| POST | `/api/offers/{id}/reject` | 청년 | |
| POST | `/api/offers/{id}/cancel` | 사장님 | |

### 승계 요약 (홈 화면)

| 메서드 | 경로 | 설명 |
|---|---|---|
| GET | `/api/successions/current` | 내 최근 승계 요약, 진행률, **내가 할 일(`pendingActions`)** |
| GET | `/api/successions/{id}` | 같은 요약 |

`pendingActions[].type`: `CHECKLIST`, `PLAN_CREATE`, `PLAN_AGREE`, `ROADMAP_APPROVE`, `ROADMAP_RETRY`, `PAYMENT_CONFIRM`, `PAYMENT_DUE`, `SCHEDULE_CHANGE`. `targetId`는 바로 처리할 대상의 id예요.

아래 경로는 모두 `/api/successions/{id}`로 시작해요.

### ④ 가일정

| 메서드 | 경로 | 설명 |
|---|---|---|
| GET | `/schedule` | 은퇴 희망일에서 거꾸로 계산한 일정, 남은 날, 경고, 변경 요청 |
| POST | `/schedule/change-requests` | `{"retireDate": "2027-09-30", "reason": "..."}` 변경 요청 |
| POST | `/schedule/change-requests/{reqId}/approve` / `reject` | 상대방만 답할 수 있어요. 승인하면 일정·로드맵·납입 날짜가 다시 맞춰져요 |

역산 규칙: 은퇴일 → 운영권 넘기기 2개월 → 수습 N개월(3단계) → 계획 동의·계약금(수습 12일 전) → 체크리스트(그 5일 전).

### ⑤ 체크리스트

| 메서드 | 경로 | 설명 |
|---|---|---|
| GET | `/checklist` | 6개 항목과 양쪽 확인 상태 |
| POST | `/checklist/{itemId}/confirm` | 내 쪽 확인. `{"note": "..."}` 선택 |
| DELETE | `/checklist/{itemId}/confirm` | 확인 취소 |

모든 항목을 두 사람이 확인하면 승계가 `PLANNING`으로 넘어가요.

### ⑥ AI 승계 계획

| 메서드 | 경로 | 설명 |
|---|---|---|
| POST | `/plan` | 초안 만들기(다시 만들기). `generatedBy`가 `AI` 또는 `TEMPLATE` |
| GET | `/plan` | 계획과 두 사람의 동의 상태 |
| PATCH | `/plan/sections/{sectionId}` | `{"content": "..."}` 고치기. 고치면 상대방 동의가 풀려요 |
| POST | `/plan/agree` | 동의. 두 사람 모두 동의하면 수습 로드맵과 납입 일정이 만들어져요 |

### ⑦ 수습 로드맵

| 메서드 | 경로 | 누가 | 설명 |
|---|---|---|---|
| GET | `/roadmap` | 둘 다 | 3단계, 단계별 일 9개, 상태(`DONE`, `NOW`, `LOCKED`) |
| POST | `/roadmap/tasks/{taskId}/request` | 청년 | 완료 요청. `{"memo": "..."}` 선택. 지금 단계의 일만 가능 |
| POST | `/roadmap/tasks/{taskId}/approve` | 사장님 | 승인 |
| POST | `/roadmap/tasks/{taskId}/send-back` | 사장님 | `{"reason": "..."}` 다시 해 보기 |

모든 일이 승인되면 `HANDOVER`로 넘어가요.

### ⑧ 납입 관리

| 메서드 | 경로 | 누가 | 설명 |
|---|---|---|---|
| GET | `/payments` | 둘 다 | 전체·확인된·남은 금액, 계약금 10%, 중도금 30% 두 번, 잔금 |
| POST | `/payments/{installmentId}/report` | 청년 | 냈다고 기록. `{"paidDate": "2027-03-02", "memo": "..."}` 선택 |
| POST | `/payments/{installmentId}/confirm` | 사장님 | 받았다고 확인 |

물려받기는 돈을 직접 옮기지 않고 기록과 확인만 해요. 수습이 끝나고 모든 납입이 확인되면 승계가 `COMPLETED`가 돼요.

## 승계 단계

`CHECKLIST` → `PLANNING` → `APPRENTICE` → `HANDOVER` → `COMPLETED`

중요한 결정은 한쪽이 혼자 끝내지 않아요. 제안과 수락, 체크리스트 양쪽 확인, 계획 동의, 단계 승인, 납입 확인처럼 두 사람이 각자 확인해야 다음으로 넘어가요.

## 테스트

```bash
./gradlew test
```

Docker 없이 H2(MySQL 모드)로 돌아가요. `SuccessionFlowTest`가 ①~⑧을 API로 처음부터 끝까지 따라가요.

## 폴더 구조

```
src/main/java/com/danditon/mullyeobatgi
├── auth         로그인, 토큰, 보안 설정, 카카오 로그인
├── user         사용자, 역할, 내 정보
├── shop         ① 가게
├── youth        ① 청년
├── matching     ② 적합도 점수와 이유
├── offer        ③ 제안·수락
├── succession   승계, 가일정 역산, 단계 전환, 홈 요약
├── schedule     ④ 가일정과 변경 요청
├── checklist    ⑤ 체크리스트
├── plan         ⑥ 승계 계획 (템플릿 + AI)
├── roadmap      ⑦ 수습 로드맵
├── payment      ⑧ 납입 관리
├── ai           Claude 연결
├── demo         예시 데이터
└── common       예외, 조사 처리, 금액 표시
```
