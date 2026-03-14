# 🧠 Edrak V2 — System Overview

**Version:** 2.0.0 | **Platforms:** Android (Kotlin + Compose) + Spring Boot Backend  
**Core:** Multi-speaker conversation capture, diarization, and AI-powered daily analysis.

---

## What Edrak V2 Does

Edrak V2 runs silently in the Android background, listening to real conversations around the user.
It identifies **who is speaking** and captures everything that is said — saving it locally first,
then syncing to a Spring Boot backend where Google Gemini AI analyzes it.

The AI understands:
- What topics were discussed
- Who said what
- What actions need to be taken (meetings, alarms, tasks, notes)
- Which life category the conversation belongs to (Work, Family, Health, Outing, etc.)

---

## Architecture in One Diagram

```
[Mic] → [VAD] → [Google STT V2 gRPC] → diarized text
                                              │
                                    [Room DB] ← write instantly
                                              │
                                    [Coroutine] → POST /api/v2/transcripts/chunk
                                                          │
                                                 [Spring Boot]
                                                 Gemini Flash → detect actions
                                                          │
                                                 FCM Push → Android wakes up
                                                          │
                                              [WorkManager] → fetch action → Room
                                                          │
                                              Kotlin Flow → UI updates

Nightly 12 AM:
  [Spring Boot Cron] → Gemini Deep Analysis → Daily Digest → FCM → App fetches
```

---

## Key Design Decisions

| Decision | Reasoning |
|---|---|
| **App streams directly to Google STT** | No double-hop latency through backend |
| **Short-lived token (Token Vending Machine)** | Never embed Google credentials in APK |
| **Room DB written first, always** | Zero data loss even with network failure |
| **Coroutines for sync, WorkManager as fallback** | WorkManager has 15-min minimum interval — Coroutines sync in real-time from the active service |
| **FCM + WorkManager (no Firestore)** | Simpler, cheaper, works when app UI is killed |
| **Gemini infers the "User" from context** | No on-device biometric model needed in V2 |
| **Same package ID as V1** | Reuses existing Firebase project |
