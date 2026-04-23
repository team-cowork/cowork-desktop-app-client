# 팀 아이콘 업로드 API 설계

팀 아이콘 업로드는 프로필 이미지 업로드와 동일하게 **3단계 Presigned URL** 방식으로 처리합니다.

---

## 흐름 요약

```
클라이언트                          서버                         S3
   │                                 │                           │
   │  POST /teams/icon/presigned      │                           │
   │  { contentType: "image/jpeg" }  │                           │
   │ ──────────────────────────────► │                           │
   │  { uploadUrl, iconUrl }         │  GeneratePresignedPutUrl  │
   │ ◄────────────────────────────── │ ──────────────────────── ►│
   │                                 │                           │
   │  PUT {uploadUrl}                │                           │
   │  (바이트 직접 전송)               │                           │
   │ ─────────────────────────────────────────────────────────── ►│
   │  200 OK                         │                           │
   │ ◄ ──────────────────────────────────────────────────────────│
   │                                 │                           │
   │  POST /teams                    │                           │
   │  { name, description, iconUrl } │                           │
   │ ──────────────────────────────► │                           │
```

---

## 1. Presigned URL 발급

### Request

```http
POST /teams/icon/presigned
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "contentType": "image/jpeg"
}
```

`contentType` 허용값: `image/jpeg`, `image/png`, `image/webp`

### Response

```json
{
  "code": 200,
  "message": "OK",
  "data": {
    "uploadUrl": "https://{bucket}.s3.{region}.amazonaws.com/team-icons/{uuid}.jpg?X-Amz-...",
    "iconUrl": "https://cdn.cowork.team/team-icons/{uuid}.jpg"
  }
}
```

| 필드 | 설명 |
|------|------|
| `uploadUrl` | S3 Presigned PUT URL (유효시간 10분) |
| `iconUrl` | 업로드 완료 후 실제로 사용할 CDN URL |

> `iconUrl`은 업로드 전에 미리 확정된 경로입니다. 클라이언트는 업로드 성공 후 이 URL을 팀 생성 요청에 바로 사용합니다. 별도 confirm 엔드포인트는 필요하지 않습니다.

---

## 2. S3 직접 업로드

```http
PUT {uploadUrl}
Content-Type: image/jpeg

(바이너리 이미지 데이터)
```

성공 시 HTTP 200. 실패 시 403 (URL 만료) 또는 400.

---

## 3. 팀 생성 시 iconUrl 포함

```http
POST /teams
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "name": "백엔드팀",
  "description": "서버 개발 팀",
  "iconUrl": "https://cdn.cowork.team/team-icons/{uuid}.jpg"
}
```

기존 `POST /teams` 요청과 동일하며, `iconUrl` 필드만 추가됩니다. 아이콘을 업로드하지 않은 경우 `iconUrl`은 `null`로 전달합니다.

---

## 서버 구현 체크리스트

- [ ] `POST /teams/icon/presigned` 엔드포인트 추가 (cowork-team 서비스)
- [ ] S3 객체 키 형식: `team-icons/{uuid}.{ext}`
- [ ] Presigned URL 유효시간: 10분
- [ ] 허용 contentType 검증: `image/jpeg`, `image/png`, `image/webp`
- [ ] `iconUrl` CDN 도메인 일관성 확보 (프로필 이미지와 동일 도메인 권장)
- [ ] `POST /teams`에서 `iconUrl` null 허용 유지 (선택 항목)

---

## 클라이언트 연결 지점

`composeApp/src/commonMain/.../data/remote/TeamApi.kt`에 스텁이 준비되어 있습니다:

```kotlin
// TODO: 서버 구현 후 주석 해제
// suspend fun generateIconPresignedUrl(accessToken: String, contentType: String): IconPresignedUploadResponse
// suspend fun putIconToS3(uploadUrl: String, bytes: ByteArray, contentType: String)
```

`MainStoreFactory.createTeam()`에서 `iconUrl = null` 하드코딩 부분을 실제 업로드 흐름으로 교체하면 됩니다.
