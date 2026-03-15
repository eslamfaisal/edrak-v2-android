# 🔌 API Design

## Conventions

| Convention | Rule |
|-----------|------|
| **Base prefix** | `/api/v1` |
| **Resources** | Plural nouns (`/users`, `/memory`) |
| **Methods** | GET (read), POST (create), PUT (update), DELETE (remove) |
| **Pagination** | `?page=0&size=20` (Spring defaults — ready for future use) |
| **Response** | Always wrapped in `ApiResponse<T>` |
| **Auth** | Bearer JWT token in `Authorization` header |

> [!NOTE]
> All new Edrak V2 features are added under the same `/api/v1` prefix — no versioning split. The entire API will be refactored together when needed.

## Endpoint Registry

### Authentication (`/api/v1/auth`)

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| `POST` | `/register` | Create new user account | ❌ Public |
| `POST` | `/login` | Authenticate with email/password | ❌ Public |
| `POST` | `/login/social` | Authenticate with Firebase ID Token (Google/Apple) | ❌ Public |
| `POST` | `/refresh-token` | Get new tokens via refresh token | ❌ Public |
| `PUT` | `/update-fcm-token` | Update device push notification token | ✅ Required |
| `GET` | `/profile` | Get authenticated user's profile | ✅ Required |
| `PUT` | `/profile` | Update display name and/or timezone | ✅ Required |
| `PUT` | `/change-password` | Change password (email users only) | ✅ Required |

### Memory (`/api/v1/memory`)

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| `POST` | `/ingest` | Upload batched transcript text | ✅ Required |
| `GET` | `/insights` | Get classified items (tasks, notes, reminders) | ✅ Required |
| `GET` | `/insights?date=YYYY-MM-DD` | Get insights for a specific date | ✅ Required |
| `GET` | `/insights?category=WORK` | Filter insights by category | ✅ Required |
| `GET` | `/insights?type=TASK` | Filter insights by item type | ✅ Required |
| `PATCH` | `/insights/{itemId}/status` | Mark insight item as DONE or PENDING | ✅ Required |
| `DELETE` | `/insights/{itemId}` | Delete a single insight item | ✅ Required |
| `DELETE` | `/insights?date=YYYY-MM-DD` | Delete all insights for a specific date | ✅ Required |
| `POST` | `/chat/sessions` | Create a new chat session | ✅ Required |
| `GET` | `/chat/sessions` | List user's chat sessions (paginated) | ✅ Required |
| `GET` | `/chat/sessions/{sessionId}` | Get a specific chat session | ✅ Required |
| `PUT` | `/chat/sessions/{sessionId}` | Update session (title, archive) | ✅ Required |
| `DELETE` | `/chat/sessions/{sessionId}` | Delete session + all messages | ✅ Required |
| `GET` | `/chat/sessions/{sessionId}/messages` | Get session messages (paginated) | ✅ Required |
| `POST` | `/chat` | Chat with memory in a session (RAG) | ✅ Required |
| `GET` | `/digest` | Get today's daily digest | ✅ Required |
| `GET` | `/digest?date=YYYY-MM-DD` | Get daily digest for specific date | ✅ Required |
| `DELETE` | `/data` | Delete ALL user data (irreversible) | ✅ Required |
| `GET` | `/stats` | Get user data statistics | ✅ Required |

### Sharing (`/api/v1/sharing`)

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| `POST` | `/share` | Create share link (any content type) | ✅ Required |
| `GET` | `/share/{shareToken}` | Get shared content (access-aware) | ❌ Public |
| `GET` | `/share/{shareToken}/comments` | Get comment thread | ❌ Public |
| `POST` | `/share/{shareToken}/comments` | Add comment (auth optional) | ⚡ Optional |
| `GET` | `/my-shares` | List user's shared content | ✅ Required |
| `PUT` | `/share/{shareId}` | Update share settings | ✅ Required |
| `DELETE` | `/share/{shareId}` | Revoke share link | ✅ Required |
| `POST` | `/share/{shareToken}/request-access` | Request access to private share | ✅ Required |
| `PUT` | `/share/{shareId}/access-requests/{requestId}` | Approve/deny access request | ✅ Required |
| `GET` | `/share/{shareId}/access-requests` | List access requests (owner) | ✅ Required |
| `GET` | `/share/{shareId}/access-grants` | List granted users (owner) | ✅ Required |
| `DELETE` | `/share/{shareId}/access-grants/{granteeId}` | Revoke user access | ✅ Required |

---

## 🆕 Edrak V2 — New Endpoints (under /api/v1)

> [!IMPORTANT]
> All new V2 features are added under the same `/api/v1` prefix. The backend package structure uses `features/v2/` internally to group the new code, but routes are unified. A full API refactor/clean-up will happen in a future sprint.

### STT Token Vending Machine (`/api/v1/stt`)

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| `GET` | `/token` | Get short-lived Google Cloud OAuth2 token for direct device-to-STT streaming | ✅ Required |

**Response:**
```json
{
  "success": true,
  "message": "STT token generated",
  "data": {
    "accessToken": "ya29.c.b0...",
    "expiresAtEpochMs": 1741996800000,
    "scope": "https://www.googleapis.com/auth/cloud-platform"
  }
}
```

> [!NOTE]
> No audio ever touches this backend. The Android app uses this token to stream directly to Google Speech-to-Text V2.

### Transcript Chunk Ingestion (`/api/v1/transcripts`)

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| `POST` | `/chunk` | Ingest a transcript chunk from device (multi-speaker) | ✅ Required |
| `GET` | `/actions/pending` | Get pending AI-detected actions for the user | ✅ Required |
| `GET` | `/actions/{id}` | Get a specific detected action by ID | ✅ Required |
| `PATCH` | `/actions/{id}/execute` | Mark a detected action as executed | ✅ Required |
| `POST` | `/fcm/register` | Register FCM device token (multi-device support) | ✅ Required |

**Chunk Ingestion Request:**
```json
POST /api/v1/transcripts/chunk
Authorization: Bearer <token>
Content-Type: application/json

{
  "conversationId": "conv_2026-03-15_abc123",
  "chunkIndex": 5,
  "startedAt": "2026-03-15T00:00:00Z",
  "endedAt": null,
  "transcript": [
    { "speaker": "SPEAKER_1", "text": "Let's schedule the meeting for Thursday", "tsMs": 5000 },
    { "speaker": "SPEAKER_2", "text": "Thursday works for me", "tsMs": 7800 }
  ]
}
```

After ingestion, Gemini asynchronously detects actions (MEETING/TASK/ALARM/NOTE) and stores them. FCM push is sent on new detections.

### Voice Enrollment (`/api/v1/voice`)

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| `POST` | `/enroll` | Upload a voice phrase recording (multipart/form-data, field: `audio`) | ✅ Required |
| `GET` | `/status` | Get enrollment status (phraseCount, fullyEnrolled flag) | ✅ Required |

> [!NOTE]
> Audio is **never stored** in the backend. Only enrollment metadata (phrase count, timestamp) is persisted. The Android app also deletes audio after upload.

---

!!! tip "Combining Filters"
    Insight filters can be combined: `/insights?date=2026-03-11&type=TASK` returns only tasks for that date.
    Available `type` values: `TASK`, `REMINDER`, `NOTE`.
    Available `category` values: `WORK`, `STUDY`, `PERSONAL`, `FINANCE`, `HEALTH`, `IDEAS`, `FAMILY`.

## Error Responses

All errors follow a consistent structure with `ErrorDetail`:

```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_FAILED",
    "message": "email: must be a valid email address",
    "timestamp": "2026-03-11T10:30:00.000Z",
    "path": "/api/v1/auth/register"
  },
  "timestamp": "2026-03-11T10:30:00.000Z"
}
```

### Error Code Reference

| HTTP Status | Error Code | Description |
|-------------|-----------|-------------|
| 400 | `BAD_REQUEST` | Generic bad request |
| 400 | `INVALID_CREDENTIALS` | Wrong email or password on login |
| 400 | `INVALID_TOKEN` | Invalid or expired refresh token |
| 400 | `VALIDATION_FAILED` | Bean Validation errors (field-level) |
| 401 | `UNAUTHORIZED` | Missing or invalid JWT access token |
| 403 | `FORBIDDEN` | Insufficient permissions |
| 404 | `NOT_FOUND` | Resource not found |
| 409 | `DUPLICATE` | Email already registered |
| 500 | `INTERNAL_ERROR` | Unhandled server error |
