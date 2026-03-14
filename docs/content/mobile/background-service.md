# 🎙️ Background Service & Audio Pipeline

## Foreground Service — The Backbone

The `EdrakListeningService` is the heart of Edrak V2.
It runs even when the app UI is killed. The mic never closes while the service is active.

## Audio Pipeline

```
AudioRecord (PCM 16-bit, 16kHz, Mono)
     │
     ▼
VAD — Energy/ZCR check per frame
 ├── Silence → no-op (mic stays open, CPU rests)
 └── Speech → send frame to gRPC stream
                    │
                    ▼
         Google STT V2 (gRPC streaming)
         - Speaker diarization enabled
         - Returns: {speakerTag, text, timestamp}
                    │
                    ▼
         Room DB ← write immediately (one row per sentence)
                    │
                    ▼
         Coroutine → POST /api/v2/transcripts/chunk
```

## Audio Focus — Handling Calls & Other Apps

```kotlin
audioManager.requestAudioFocus(
    focusChangeListener,
    AudioManager.STREAM_MUSIC,
    AudioManager.AUDIOFOCUS_GAIN
)

// When WhatsApp or Phone takes the mic:
AUDIOFOCUS_LOSS          → pause gRPC, stop sending frames
AUDIOFOCUS_LOSS_TRANSIENT → pause gRPC
AUDIOFOCUS_GAIN          → resume gRPC automatically
```

## Conversation Segmentation

- Silence ≥ 60 seconds → current conversation ends, new one starts on next speech
- Each `ConversationEntity` has a `startTime` and `endTime`
- All `TranscriptChunkEntity` rows are linked via `conversationId`

## Authentication for Google STT

**NEVER embed credentials in the APK.**

Flow:
1. App calls: `GET /api/v2/stt/token` (JWT authenticated)
2. Backend returns a short-lived Google OAuth token (~55 min)
3. App uses this token for the gRPC session
4. `SttTokenManager` auto-refreshes 5 min before expiry

## Battery Optimization

| Technique | Effect |
|---|---|
| VAD before STT | No API calls during silence |
| PCM in memory only | No disk I/O on audio |
| PARTIAL_WAKE_LOCK | CPU stays on, screen can turn off |
| Coroutine sync (not polling) | No background timers |
| WorkManager | OS-optimized scheduling for fallback sync |
