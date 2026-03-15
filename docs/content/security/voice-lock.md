# Voice Passphrase Lock

Edrak uses a **dual-factor voice authentication** system to gate app access on every cold start. The user must say their personal passphrase, and the system verifies both *what* they said (passphrase prompt) and *who* is saying it (voice biometrics).

---

## How It Works

```
App opens
   │
   ├─ Not logged in → Login screen
   ├─ Logged in, no voice setup → Onboarding
   └─ Logged in + voice ready → 🔒 Lock screen
                                      │
                              User holds mic button
                              and says passphrase
                                      │
                              ┌───────────────────┐
                              │ Backend MFCC       │
                              │ /api/v1/voice/verify│
                              │ cosine ≥ 0.85      │
                              └───────────────────┘
                                      │
                              Match? → Home ✅
                              No match? → Retry (max 3)
                                      │
                              3 failures → Logout + Login
```

---

## Two Factors

| Factor | Mechanism | Where validated |
|---|---|---|
| **Voice biometric** | MFCC embedding cosine similarity (≥ 0.85) | Backend `/api/v1/voice/verify` |
| **Passphrase prompt** | Phrase is shown on screen — user must say it to produce a match | UI + biometric model trained on that phrase |

> [!NOTE]
> The passphrase the user sets during onboarding **becomes enrollment phrase #1**. This means the backend's MFCC model was trained on audio of the user saying that exact phrase — making biometric match accuracy highest for the lock screen scenario.

---

## Security Properties

- **Passphrase stored encrypted** — `EncryptedSharedPreferences` with AES-256-GCM via Android Keystore. Never stored in plain text.
- **3-attempt lockout** — After 3 consecutive failures, all tokens are cleared and the user is redirected to the credential login screen.
- **No bypass path** — The Lock route is the sole destination after `isVoiceSetupComplete()` is true. Pressing Back exits the app (backstack is cleared).
- **Voice training on passphrase** — Because enrollment phrase[0] = passphrase, the stored embedding captures the acoustic characteristics of the user speaking that specific phrase. A replay attack with someone else's voice saying the phrase will fail the MFCC cosine comparison.

---

## Onboarding Flow (Updated)

```
WELCOME
  ↓
PASSPHRASE_SETUP   ← user types/chooses their voice PIN phrase
  ↓
RECORDING          ← 3 phrases: [passphrase, Arabic phrase 1, Arabic phrase 2]
  ↓
VERIFICATION       ← real-time streaming verify (2 consecutive matches needed)
  ↓
SUCCESS            ← tokenManager.setVoiceSetupComplete(true)
                      → app navigates to Lock screen next time
```

---

## Lock Screen UI

| Element | Description |
|---|---|
| Lock icon | Changes to LockOpen (green) on success |
| Greeting | "Welcome back, [name]" from `TokenManager.getUserDisplayName()` |
| Passphrase card | Shows the stored passphrase in quotes as a prompt |
| Hold-to-record button | AudioRecord captures PCM while held |
| Confidence ring | Circular arc red→amber→green after analysis |
| Animated waveform | 4 bars pulsate red while recording |
| Attempt dots | 3 red dots fill as attempts are used |
| Retry button | Shown after a failed attempt |
| "Forgot voice?" link | Clears session, goes to Login |

---

## Key Files

| File | Role |
|---|---|
| `LockContract.kt` | MVI state/events/effects |
| `LockViewModel.kt` | AudioRecord loop, WAV write, backend verify, lockout logic |
| `LockScreen.kt` | Glassmorphism UI — hold-to-record, confidence ring, waveform |
| `TokenManager.kt` | `savePassphrase()` / `getPassphrase()` — AES-256-GCM encrypted |
| `EdrakNavHost.kt` | Routes to `Lock` instead of `Home` after voice setup |
| `OnboardingViewModel.kt` | `confirmPassphrase()` → saves passphrase + builds enrollment phrase list |
| `WavWriter.kt` | Converts raw PCM-16 to valid WAV for backend submission |

---

## Backend — Voice Verify Endpoint

```
POST /api/v1/voice/verify
Content-Type: multipart/form-data
Authorization: Bearer <jwt>

audio: <WAV file — 16kHz, mono, 16-bit PCM>
```

**Response:**
```json
{
  "message": "Voice verified",
  "data": {
    "match": true,
    "confidence": 0.91,
    "userId": "abc123"
  }
}
```

The backend (`VoiceVerifyService.java`) extracts MFCC embeddings from the audio, normalizes them, and computes cosine similarity against the stored user embedding. Match threshold is configurable (default: 0.85).

---

## Configuration

| Parameter | Default | Location |
|---|---|---|
| Confidence threshold | 0.85 | `VoiceVerifyService.java` (backend) |
| Max lock attempts | 3 | `LockViewModel.kt` `MAX_ATTEMPTS` |
| Passphrase min length | 3 chars | `OnboardingViewModel.kt` |

---

## Sequence Diagram

```
User           LockScreen       LockViewModel        Backend
  │                │                  │                 │
  │   Open app     │                  │                 │
  │──────────────> │                  │                 │
  │                │   IDLE state     │                 │
  │   Hold mic     │                  │                 │
  │──────────────> │  StartRecording  │                 │
  │                │────────────────> │ AudioRecord.start│
  │                │                  │                 │
  │   Release mic  │                  │                 │
  │──────────────> │  StopAndVerify  │                 │
  │                │────────────────>│ write WAV        │
  │                │                 │─── POST /verify ─>│
  │                │                 │<── {match, conf} ─│
  │                │  MATCHED ✅     │                 │
  │<────────────── │  Navigate Home  │                 │
```
