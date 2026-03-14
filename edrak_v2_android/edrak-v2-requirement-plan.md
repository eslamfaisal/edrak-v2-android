# 🧠 Edrak V2 — Architecture & Requirements Plan (Final)

**Version:** 2.0.0 | **Platform:** Android (Kotlin + Compose) + Spring Boot Backend  
**Package ID:** Same as V1 (Firebase compatible, no new project needed)

---

## 🔑 The Core Reality

> The app can be **completely invisible** (screen off, UI killed) for hours.  
> The **Foreground Service** is the only reliable process running.  
> Room DB is the **Single Source of Truth (SSOT)** — network is best-effort.

---

## 🏛️ Roles — Final (After Technical Review)

### 📱 Foreground Service — The Backbone

| Task | Detail |
|---|---|
| **Open Mic** | `AudioRecord` — PCM stream, stays open until user stops |
| **Audio Focus** | `AudioFocusChangeListener` — **Pause gRPC stream** on call/WhatsApp, **Auto-Resume** after |
| **VAD** | Lightweight energy/ZCR check — skip silence frames |
| **Fetch STT Token** | Call Backend's **Token Vending Machine** → get short-lived OAuth token (60 min) |
| **Stream to Google STT V2** | gRPC session using the short-lived token directly from device |
| **Receive diarized text** | Google STT returns `{speakerTag: 1/2/3, text, timestamp}` |
| **Write to Room immediately** | One row per sentence — speaker labeled as `SPEAKER_1`, `SPEAKER_2` etc. |
| **Sync via Coroutines** | When conversation chunk ready → `launch(IO)` to POST to backend directly |
| **WorkManager as Fallback** | Only for `SYNC_FAILED` rows when internet was down |

> ⚠️ **No Firestore** — Room DB + Kotlin Flow is cleaner and free.  
> ⚠️ **No on-device voice embedding** — Gemini infers who's "User" from conversation context.

---

### ☁️ Backend (Spring Boot) — AI Brain

| Task | Detail |
|---|---|
| **Token Vending Machine** | `GET /api/v2/stt/token` → exchanges service account for short-lived OAuth token → returns to app |
| **Receive Transcript Chunks** | `POST /api/v2/transcripts/chunk` |
| **AI Context Assembly** | Before calling Gemini, fetch **last 3-5 min** of the same `conversationId` from DB and prepend as context |
| **Gemini Flash (Fast)** | Detects: MEETING / ALARM / TASK / NOTE — responds within seconds |
| **FCM Push** | Detected action → `POST to FCM` → app wakes up via WorkManager → fetches action → saves to Room → Kotlin Flow updates UI |
| **Gemini inferring "User"** | Backend prompt includes: *"Based on context, identify which speaker is the device owner"* |
| **Hourly sweep** | Re-analyze last hour's chunks — catch missed actions |
| **Nightly 12 AM Cron** | Deep analysis + digest generation + FCM delivery (with random 0-5 min jitter to prevent server stampede) |

---

## 🔄 Complete Data Flow

```
Mic (PCM)
  → VAD (skip silence)
  → [First run] GET /api/v2/stt/token → gRPC session opens
  → Google STT V2 → {speakerTag, text, timestamp}
  → Room DB (write instantly, per sentence, as SPEAKER_1/2/3)
  → Foreground Service Coroutine → POST /api/v2/transcripts/chunk
        │
        ├── Success → update SyncStatus = SYNCED
        └── Fail    → SyncStatus = SYNC_FAILED → WorkManager retries later
        
Backend receives chunk:
  → Fetch last 3-5 min context from DB (same conversationId)
  → Gemini Flash (chunk + context) → detect action + infer user speaker
  → Action found? → FCM push
  
App receives FCM:
  → WorkManager (guaranteed execution)
  → GET /api/v2/actions/{id}
  → Save to Room (detected_actions)
  → Kotlin Flow → UI updates automatically
```

---

## 🗄️ Room DB Schema

```kotlin
ConversationEntity   (id, startTime, endTime?, syncStatus)
TranscriptChunkEntity(id, conversationId, speakerTag, text, timestamp, syncStatus)
DetectedActionEntity (id, conversationId, type, payload, detectedAt, executed)
```

`SyncStatus`: `LOCAL_ONLY → SYNCING → SYNCED → SYNC_FAILED`  
`ActionType`: `MEETING | ALARM | TASK | NOTE`

---

## 📦 Chunk API Payload

```json
POST /api/v2/transcripts/chunk
{
  "conversationId": "conv_abc",
  "chunkIndex": 3,
  "startedAt": "2026-03-15T10:32:00Z",
  "endedAt":   "2026-03-15T10:37:00Z",
  "transcript": [
    { "speaker": "SPEAKER_1", "text": "مرحبا، عامل إيه؟",           "ts": "10:32:01" },
    { "speaker": "SPEAKER_2", "text": "تمام والحمدلله.",             "ts": "10:32:04" },
    { "speaker": "SPEAKER_3", "text": "أنا نسيت أتصل بيك امبارح.", "ts": "10:32:07" }
  ]
}
```

Backend will infer which `SPEAKER_X` is the device owner and label as "User" in the analysis.

---

## 📋 Execution Order (Corrected)

> Build the Backend API **before** the Android Service — it must exist before Android syncs to it.

| Phase | Who | Task |
|---|---|---|
| **Phase 1** | Android | Project skeleton + Auth + Room DB schema |
| **Phase 2** | Backend | DB migrations + Chunk API + Token Vending Machine + Gemini Fast Pipeline + FCM |
| **Phase 3** | Android | Foreground Service + VAD + Audio Focus + gRPC STT + Room write |
| **Phase 4** | Android | Sync (Coroutines) + WorkManager fallback + FCM receive + Room update |
| **Phase 5** | Android | UI: Home / Listening / Digest screens (observe Room via Kotlin Flow) |
| **Phase 6** | Backend | Hourly sweep + Nightly 12 AM Cron (with jitter) + Digest generation |

---

## ⚠️ Non-Negotiable Constraints

1. **No credentials in APK** — Token Vending Machine on Backend only
2. **No Firestore** — Room + Kotlin Flow + FCM + WorkManager replaces it
3. **No audio stored anywhere** — PCM in memory only, never persisted
4. **Room is SSOT** — backend sync is best-effort
5. **App streams directly to Google STT** using short-lived Backend-issued token
6. **Backend has AI context** — always fetches conversation history before calling Gemini
7. **WorkManager = fallback only** — Coroutines handle primary real-time sync
8. **Audio Focus handled** — Pause on incoming call, Auto-Resume after
9. **Same package ID as V1** — same Firebase project, no new setup
10. **Nightly cron has jitter** — random 0–5 min offset to avoid server stampede
