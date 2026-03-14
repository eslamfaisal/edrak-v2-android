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

!!! tip "Combining Filters"
    Insight filters can be combined: `/insights?date=2026-03-11&type=TASK` returns only tasks for that date.
    Available `type` values: `TASK`, `REMINDER`, `NOTE`.
    Available `category` values: `WORK`, `STUDY`, `PERSONAL`, `FINANCE`, `HEALTH`, `IDEAS`, `FAMILY`.

## Request/Response Examples

### Register

=== "Request"

    ```json
    POST /api/v1/auth/register
    Content-Type: application/json

    {
      "email": "user@example.com",
      "password": "SecurePass123!",
      "displayName": "Ahmed",
      "timezone": "Africa/Cairo"
    }
    ```

=== "Response (201)"

    ```json
    {
      "success": true,
      "message": "Registration successful",
      "data": {
        "accessToken": "eyJhbGciOiJIUzI1NiIs...",
        "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
        "expiresIn": 86400,
        "tokenType": "Bearer",
        "firebaseCustomToken": "eyJhbGciOiJSUzI1NiIs...",
        "user": {
          "id": "550e8400-e29b-41d4-a716-446655440000",
          "email": "user@example.com",
          "displayName": "Ahmed",
          "timezone": "Africa/Cairo",
          "photoUrl": null,
          "authProvider": "EMAIL"
        }
      },
      "timestamp": "2026-03-11T10:30:00.000Z"
    }
    ```

### Login

=== "Request"

    ```json
    POST /api/v1/auth/login
    Content-Type: application/json

    {
      "email": "user@example.com",
      "password": "SecurePass123!"
    }
    ```

=== "Response (200)"

    ```json
    {
      "success": true,
      "message": "Login successful",
      "data": {
        "accessToken": "eyJhbGciOiJIUzI1NiIs...",
        "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
        "expiresIn": 86400,
        "tokenType": "Bearer",
        "firebaseCustomToken": "eyJhbGciOiJSUzI1NiIs...",
        "user": {
          "id": "550e8400-e29b-41d4-a716-446655440000",
          "email": "user@example.com",
          "displayName": "Ahmed",
          "timezone": "Africa/Cairo",
          "photoUrl": null,
          "authProvider": "EMAIL"
        }
      },
      "timestamp": "2026-03-11T10:30:00.000Z"
    }
    ```

### Social Login

=== "Request"

    ```json
    POST /api/v1/auth/login/social
    Content-Type: application/json

    {
      "firebaseIdToken": "eyJhbGciOiJSUzI1NiIs..."
    }
    ```

=== "Response (200)"

    ```json
    {
      "success": true,
      "message": "Login successful",
      "data": {
        "accessToken": "eyJhbGciOiJIUzI1NiIs...",
        "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
        "expiresIn": 86400,
        "tokenType": "Bearer",
        "firebaseCustomToken": "eyJhbGciOiJSUzI1NiIs...",
        "user": {
          "id": "550e8400-e29b-41d4-a716-446655440000",
          "email": "user@gmail.com",
          "displayName": "Ahmed Mohamed",
          "timezone": "UTC",
          "photoUrl": "https://lh3.googleusercontent.com/a/...",
          "authProvider": "GOOGLE"
        }
      },
      "timestamp": "2026-03-11T10:30:00.000Z"
    }
    ```

### Refresh Token

=== "Request"

    ```json
    POST /api/v1/auth/refresh-token
    Content-Type: application/json

    {
      "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
    }
    ```

=== "Response (200)"

    ```json
    {
      "success": true,
      "message": "Token refreshed",
      "data": {
        "accessToken": "eyJhbGciOiJIUzI1NiIs...",
        "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
        "expiresIn": 86400,
        "tokenType": "Bearer",
        "firebaseCustomToken": "eyJhbGciOiJSUzI1NiIs...",
        "user": {
          "id": "550e8400-e29b-41d4-a716-446655440000",
          "email": "user@example.com",
          "displayName": "Ahmed",
          "timezone": "Africa/Cairo",
          "photoUrl": null,
          "authProvider": "EMAIL"
        }
      },
      "timestamp": "2026-03-11T10:30:00.000Z"
    }
    ```

### Update FCM Token

=== "Request"

    ```json
    PUT /api/v1/auth/update-fcm-token
    Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
    Content-Type: application/json

    {
      "fcmToken": "dXY3_device_fcm_token_abc123"
    }
    ```

=== "Response (200)"

    ```json
    {
      "success": true,
      "message": "FCM token updated",
      "timestamp": "2026-03-11T10:30:00.000Z"
    }
    ```

### Ingest Memory

=== "Request"

    ```json
    POST /api/v1/memory/ingest
    Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
    Content-Type: application/json

    {
      "texts": [
        {
          "content": "لازم أبعت الريبورت لأحمد بكرة الصبح",
          "timestamp": "2026-03-11T10:30:00Z"
        },
        {
          "content": "الميتنج مع الفريق يوم الأربعاء الساعة ٣",
          "timestamp": "2026-03-11T10:32:00Z"
        }
      ]
    }
    ```

=== "Response (202)"

    ```json
    {
      "success": true,
      "message": "Ingestion accepted. Processing in background.",
      "data": {
        "chunkId": "660e8400-e29b-41d4-a716-446655440001",
        "textsReceived": 2
      },
      "timestamp": "2026-03-11T10:30:01.000Z"
    }
    ```

### Chat with Memory

=== "Request"

    ```json
    POST /api/v1/memory/chat
    Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
    Content-Type: application/json

    {
      "sessionId": "770e8400-e29b-41d4-a716-446655440099",
      "query": "أنا كنت بتكلم مع مين عن مشروع React؟"
    }
    ```

=== "Response (200)"

    ```json
    {
      "success": true,
      "message": "Query processed",
      "data": {
        "answer": "كنت بتتكلم مع أحمد يوم الثلاثاء عن مشروع React. ذكر إن الـ deadline يوم الجمعة وإنه محتاج مساعدة في الـ authentication.",
        "sources": [
          {
            "chunkId": "...",
            "text": "أحمد بيقول إن مشروع React...",
            "timestamp": "2026-03-09T14:22:00Z",
            "similarity": 0.92
          }
        ]
      },
      "timestamp": "2026-03-11T10:35:00.000Z"
    }
    ```

### Get Profile

=== "Request"

    ```
    GET /api/v1/auth/profile
    Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
    ```

=== "Response (200)"

    ```json
    {
      "success": true,
      "message": "Profile retrieved",
      "data": {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "email": "user@example.com",
        "displayName": "Ahmed",
        "timezone": "Africa/Cairo",
        "photoUrl": null,
        "authProvider": "EMAIL",
        "createdAt": "2026-03-01T08:00:00.000Z"
      },
      "timestamp": "2026-03-11T10:30:00.000Z"
    }
    ```

### Update Profile

=== "Request"

    ```json
    PUT /api/v1/auth/profile
    Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
    Content-Type: application/json

    {
      "displayName": "Ahmed Faisal",
      "timezone": "Asia/Riyadh"
    }
    ```

=== "Response (200)"

    ```json
    {
      "success": true,
      "message": "Profile updated",
      "data": {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "email": "user@example.com",
        "displayName": "Ahmed Faisal",
        "timezone": "Asia/Riyadh",
        "photoUrl": null,
        "authProvider": "EMAIL",
        "createdAt": "2026-03-01T08:00:00.000Z"
      },
      "timestamp": "2026-03-11T10:30:00.000Z"
    }
    ```

### Change Password

=== "Request"

    ```json
    PUT /api/v1/auth/change-password
    Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
    Content-Type: application/json

    {
      "currentPassword": "OldPass123!",
      "newPassword": "NewSecurePass456!"
    }
    ```

=== "Response (200)"

    ```json
    {
      "success": true,
      "message": "Password changed successfully",
      "timestamp": "2026-03-11T10:30:00.000Z"
    }
    ```

### Update Insight Status

=== "Request"

    ```json
    PATCH /api/v1/memory/insights/550e8400-e29b-41d4-a716-446655440001/status
    Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
    Content-Type: application/json

    {
      "status": "DONE"
    }
    ```

=== "Response (200)"

    ```json
    {
      "success": true,
      "message": "Item status updated",
      "data": {
        "id": "550e8400-e29b-41d4-a716-446655440001",
        "itemType": "TASK",
        "category": "WORK",
        "content": "إرسال الريبورت لأحمد",
        "priority": "HIGH",
        "dueDate": "2026-03-12T10:00:00Z",
        "status": "DONE",
        "createdAt": "2026-03-11T10:30:00Z"
      },
      "timestamp": "2026-03-11T11:00:00.000Z"
    }
    ```

### Create Chat Session

=== "Request"

    ```json
    POST /api/v1/memory/chat/sessions
    Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
    Content-Type: application/json

    {
      "title": "React project discussion"
    }
    ```

=== "Response (201)"

    ```json
    {
      "success": true,
      "message": "Chat session created",
      "data": {
        "id": "770e8400-e29b-41d4-a716-446655440099",
        "title": "React project discussion",
        "status": "ACTIVE",
        "messageCount": 0,
        "lastMessage": null,
        "createdAt": "2026-03-11T10:30:00Z",
        "updatedAt": "2026-03-11T10:30:00Z"
      },
      "timestamp": "2026-03-11T10:30:00.000Z"
    }
    ```

### Get Session Messages

=== "Request"

    ```
    GET /api/v1/memory/chat/sessions/770e8400-e29b-41d4-a716-446655440099/messages?page=0&size=50
    Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
    ```

=== "Response (200)"

    ```json
    {
      "success": true,
      "message": "Messages retrieved",
      "data": {
        "content": [
          {
            "id": "...",
            "sessionId": "770e8400-e29b-41d4-a716-446655440099",
            "role": "USER",
            "content": "أنا كنت بتكلم مع مين عن مشروع React؟",
            "createdAt": "2026-03-11T10:35:00Z"
          },
          {
            "id": "...",
            "sessionId": "770e8400-e29b-41d4-a716-446655440099",
            "role": "ASSISTANT",
            "content": "كنت بتتكلم مع أحمد يوم الثلاثاء عن مشروع React.",
            "createdAt": "2026-03-11T10:35:01Z"
          }
        ],
        "totalElements": 42,
        "totalPages": 1,
        "number": 0,
        "size": 50
      },
      "timestamp": "2026-03-11T10:36:00.000Z"
    }
    ```

### Get User Stats

=== "Request"

    ```
    GET /api/v1/memory/stats
    Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
    ```

=== "Response (200)"

    ```json
    {
      "success": true,
      "message": "User statistics retrieved",
      "data": {
        "totalChunks": 156,
        "totalTasks": 23,
        "totalReminders": 8,
        "totalNotes": 45,
        "totalChats": 42,
        "memberSince": "2026-03-01T08:00:00.000Z"
      },
      "timestamp": "2026-03-11T10:37:00.000Z"
    }
    ```

### Create Share

=== "Request"

    ```json
    POST /api/v1/sharing/share
    Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
    Content-Type: application/json

    {
      "contentType": "INSIGHT",
      "contentId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
      "visibility": "PRIVATE",
      "title": "Important meeting notes",
      "description": "Notes from the product sync meeting"
    }
    ```

=== "Response (201)"

    ```json
    {
      "success": true,
      "message": "Share link created",
      "data": {
        "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
        "shareToken": "aB3xK9mZ",
        "shareUrl": "https://edrak.app/share/aB3xK9mZ",
        "title": "Important meeting notes",
        "description": "Notes from the product sync meeting",
        "visibility": "PRIVATE",
        "contentType": "INSIGHT",
        "viewCount": 0,
        "commentCount": 0,
        "item": {
          "itemType": "NOTE",
          "category": "WORK",
          "content": "لازم ابعت الريبورت لاحمد بكرة الصبح",
          "priority": null,
          "status": "PENDING"
        },
        "sharedBy": {
          "userId": "550e8400-e29b-41d4-a716-446655440000",
          "displayName": "Ahmed",
          "photoUrl": null
        },
        "accessStatus": "OWNER",
        "canRequestAccess": false,
        "createdAt": "2026-03-11T10:30:00.000Z",
        "expiresAt": null
      },
      "timestamp": "2026-03-11T10:30:00.000Z"
    }
    ```

### Get Shared Content

=== "Public Share"

    ```json
    GET /api/v1/sharing/share/aB3xK9mZ
    ```

=== "Private Share (No Access)"

    ```json
    GET /api/v1/sharing/share/pR1vAtEx
    Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
    ```

=== "Response — Public (200)"

    ```json
    {
      "success": true,
      "message": "Shared content retrieved",
      "data": {
        "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
        "shareToken": "aB3xK9mZ",
        "visibility": "PUBLIC",
        "contentType": "INSIGHT",
        "viewCount": 5,
        "item": { ... },
        "accessStatus": "NONE",
        "canRequestAccess": false
      }
    }
    ```

=== "Response — Private No Access (200)"

    ```json
    {
      "success": true,
      "message": "Shared content retrieved",
      "data": {
        "shareToken": "pR1vAtEx",
        "title": "Meeting Notes",
        "visibility": "PRIVATE",
        "item": null,
        "accessStatus": "NONE",
        "canRequestAccess": true
      }
    }
    ```

### Request Access

=== "Request"

    ```json
    POST /api/v1/sharing/share/pR1vAtEx/request-access
    Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
    Content-Type: application/json

    {
      "message": "I'd like to review these notes for the project."
    }
    ```

=== "Response (201)"

    ```json
    {
      "success": true,
      "message": "Access request submitted",
      "data": {
        "id": "c3d4e5f6-a7b8-9012-cdef-123456789012",
        "requesterId": "660e8400-e29b-41d4-a716-446655440000",
        "requesterName": "Omar",
        "status": "PENDING",
        "message": "I'd like to review these notes for the project.",
        "createdAt": "2026-03-11T11:30:00.000Z"
      }
    }
    ```

### Add Comment

=== "Request (Authenticated)"

    ```json
    POST /api/v1/sharing/share/aB3xK9mZ/comments
    Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
    Content-Type: application/json

    {
      "content": "Great notes! Don't forget the deadline."
    }
    ```

=== "Request (Guest)"

    ```json
    POST /api/v1/sharing/share/aB3xK9mZ/comments
    Content-Type: application/json

    {
      "content": "Thanks for sharing!",
      "guestName": "Omar"
    }
    ```

=== "Response (201)"

    ```json
    {
      "success": true,
      "message": "Comment added",
      "data": {
        "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
        "content": "Great notes! Don't forget the deadline.",
        "authorName": "Ahmed",
        "authorPhotoUrl": null,
        "isGuest": false,
        "createdAt": "2026-03-11T11:00:00.000Z"
      },
      "timestamp": "2026-03-11T11:00:00.000Z"
    }
    ```

## Error Responses

All errors follow a consistent structure with `ErrorDetail`:

```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_FAILED",
    "message": "email: must be a valid email address, password: must be at least 8 characters",
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
