# 서버 구현 필요 항목

클라이언트와의 계약을 맞추기 위해 서버에서 추가·수정이 필요한 항목입니다.

---

## 1. MeetingNote `createdAt` / `updatedAt` 직렬화 형식 (cowork-channel)

### 문제
Spring Boot 기본 Jackson 설정에서 `LocalDateTime`은 JSON 배열 `[2024,1,1,12,0,0]`로 직렬화됩니다.
클라이언트는 ISO-8601 문자열 (`"2024-01-01T12:00:00"`) 형식을 기대합니다.

### 필요 작업
`application.yml`에 아래 설정 추가 (또는 `ObjectMapper` 빈 설정):

```yaml
spring:
  jackson:
    serialization:
      write-dates-as-timestamps: false
```

영향 범위: `MeetingNoteResponse.createdAt`, `MeetingNoteResponse.updatedAt`

---

## 2. Spring Cloud Gateway — 프로젝트 reorder 라우팅 누락 (cowork-gateway)

### 문제
`PATCH /api/teams/{teamId}/projects/reorder` 요청이 Gateway에서 `cowork-project` 서비스로 라우팅되지 않고
다른 서비스(또는 404)로 떨어집니다.

### 필요 작업
Gateway 라우팅 설정에 아래 경로 추가:

```yaml
- id: cowork-project-reorder
  uri: lb://cowork-project
  predicates:
    - Path=/api/teams/*/projects/reorder
  filters:
    - RewritePath=/api/(?<segment>.*), /${segment}
```

채널 reorder(`/api/teams/*/channels/reorder` → `cowork-channel`)도 동일하게 등록되어 있는지 확인 필요.

---

## 3. FileShare 채널 — 파일 목록 조회 API (cowork-chat)

### 배경
`FILE_SHARE` 뷰 타입 채널은 파일 업로드·다운로드 전용 채널입니다.
파일 업로드는 `POST /channels/{channelId}/files/presigned-url`로 가능하지만,
업로드된 파일 목록을 조회하는 API가 없어 클라이언트 UI를 완성할 수 없습니다.

### 필요 API

| 메서드 | 경로 | 설명 |
|-------|------|------|
| `GET` | `/channels/{channelId}/files` | 파일 목록 조회 |
| `DELETE` | `/channels/{channelId}/files/{fileId}` | 파일 삭제 |

**응답 예시 (`GET`)**
```json
[
  {
    "id": 1,
    "channelId": 10,
    "name": "기획서_v2.pdf",
    "size": 204800,
    "contentType": "application/pdf",
    "uploadedBy": 3,
    "uploadedAt": "2024-06-01T10:30:00",
    "downloadUrl": "https://..."
  }
]
```

---

## 4. AccountShare 채널 — 설계 및 구현 (신규 or cowork-user)

### 배경
`ACCOUNT_SHARE` 뷰 타입 채널은 팀원의 GitHub, Notion, Jira 등 외부 서비스 계정을 공유하는 채널입니다.
서버·클라이언트 모두 미구현 상태이며, 먼저 데이터 모델 설계가 필요합니다.

### 설계 필요 사항
- 공유 계정 타입 enum (`GITHUB`, `NOTION`, `JIRA`, `FIGMA`, `CUSTOM` 등)
- 계정 정보 저장 엔티티 (`AccountEntry`: id, channelId, type, label, value, addedBy)
- 민감 정보(비밀번호, 토큰) 암호화 저장 여부 결정

### 필요 API (설계 확정 후)

| 메서드 | 경로 | 설명 |
|-------|------|------|
| `GET` | `/channels/{channelId}/accounts` | 계정 목록 조회 |
| `POST` | `/channels/{channelId}/accounts` | 계정 추가 |
| `PATCH` | `/channels/{channelId}/accounts/{accountId}` | 계정 수정 |
| `DELETE` | `/channels/{channelId}/accounts/{accountId}` | 계정 삭제 |

---

## 요약

| # | 항목 | 서비스 | 우선순위 |
|---|------|--------|---------|
| 1 | `LocalDateTime` ISO-8601 직렬화 설정 | cowork-channel | 🔴 높음 |
| 2 | Gateway 프로젝트 reorder 라우팅 추가 | cowork-gateway | 🔴 높음 |
| 3 | FileShare 파일 목록 / 삭제 API | cowork-chat | 🟡 중간 |
| 4 | AccountShare 채널 설계 및 구현 | TBD | 🟢 낮음 |
