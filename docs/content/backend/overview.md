# ☁️ Backend — Overview (V2)

## Responsibilities

The Spring Boot backend is the **AI brain**. It never touches audio — only text.

| Responsibility | Detail |
|---|---|
| **Token Vending Machine** | Issues short-lived Google OAuth tokens to the Android app for STT |
| **Transcript Ingestion** | Receives diarized text chunks from the app |
| **AI Context Assembly** | Fetches last 3-5 min of conversation history before calling Gemini |
| **Gemini Fast Analysis** | Detects actions (meetings, alarms, tasks) from each chunk |
| **FCM Delivery** | Pushes detected actions to the user's device immediately |
| **Hourly Sweep** | Re-analyzes the past hour's chunks for missed actions |
| **Nightly 12 AM Cron** | Deep full-day analysis, topic extraction, digest generation |
| **Auth + User Management** | JWT auth, voice enrollment status |

## API Endpoints (V2)

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/auth/login` | Login (unchanged from V1) |
| POST | `/api/v1/auth/register` | Register (unchanged from V1) |
| GET | `/api/v2/stt/token` | Get short-lived OAuth token for Google STT |
| POST | `/api/v2/voice/enroll` | Upload voice enrollment audio |
| GET | `/api/v2/voice/status` | Check if voice setup is complete |
| POST | `/api/v2/transcripts/chunk` | Ingest diarized transcript chunk |
| GET | `/api/v2/actions/{id}` | Fetch a specific detected action |
| PUT | `/api/v2/actions/{id}/executed` | Mark action as executed |
| GET | `/api/v2/digests/today` | Get today's nightly digest |
| GET | `/api/v2/digests?date=YYYY-MM-DD` | Get digest for a specific date |

## AI Pipelines

### Fast Pipeline (per chunk, real-time)
- Triggered on each `POST /api/v2/transcripts/chunk`
- Runs @Async after returning 202 Accepted
- Includes last 3-5 min of context from DB
- Detects: MEETING, ALARM, TASK, NOTE
- Pushes result via FCM immediately

### Nightly Pipeline (12 AM Cron)
- Analyzes full day's transcript per user
- Corrects misrecognized words
- Extracts topics, categories, who-said-what
- Generates Daily Digest JSON
- Uses random jitter (0-5 min) to prevent server stampede

### Hourly Sweep
- Catches any actions missed by the fast pipeline
- Analyzes the past 60 minutes of transcript
- Sends FCM for any newly found actions

## Database Schema (New V2 Tables)

```sql
voice_signatures    (id, user_id, enrolled_at)
conversations       (id, user_id, started_at, ended_at, sync_status)
transcript_chunks   (id, conversation_id, speaker_tag, text, chunk_timestamp)
detected_actions    (id, conversation_id, type, title, payload, detected_at, executed)
daily_digests       (id, user_id, digest_date, digest_json, generated_at)
```

All V1 tables remain unchanged (additive migration only).
