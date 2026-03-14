# 🚀 Edrak V2 — Execution Prompts

> Each prompt below is a **standalone task**. Execute one at a time, review the output, then move to the next.  
> The project is in `/Users/eslamfaisal/Desktop/work/second_brain/edrak_v2_android/`  
> Package ID: **`me.edrakai`** (same as V1 — same Firebase project)

---

## PHASE 1 — Android Foundation

---

### 📌 Prompt 1A — Project Skeleton + Clean Architecture

```
We are building Edrak V2 Android from scratch in the folder:
/Users/eslamfaisal/Desktop/work/second_brain/edrak_v2_android/

Package ID: me.edrakai (same Firebase project as V1)
Min SDK: 26 | Target SDK: 34 | Language: Kotlin + Jetpack Compose

Architecture: MVI + Clean Architecture (3 layers per feature: domain / data / presentation)
DI: Hilt (compile-time, no reflection)
Async: Kotlin Coroutines + Flow (NO RxJava)
Navigation: Compose Navigation with type-safe routes
Theme: Port the V1 theme exactly — Deep Midnight Blue (#0B132B) + Neon Cyan (#45F0DF), Space Grotesk headings, Inter body

Tasks:
1. Create the full Android project structure:
   - app/
     - core/
       - di/                  (AppModule, NetworkModule, DatabaseModule)
       - network/             (ApiService, AuthInterceptor, NetworkResult)
       - database/            (AppDatabase, all DAOs)
       - services/            (EdrakListeningService placeholder)
       - notifications/       (NotificationHelper)
       - extensions/          (common Kotlin extensions)
     - features/
       - auth/                (login, register — domain/data/presentation)
       - onboarding/          (voice setup — domain/data/presentation)
       - home/                (dashboard — domain/data/presentation)
       - listening/           (live transcript screen — domain/data/presentation)
       - digest/              (daily digest screen — domain/data/presentation)
     - ui/
       - theme/               (Color, Type, Theme composables)
       - components/          (shared reusable composables)

2. Set up build.gradle.kts (app level) with ALL dependencies:
   - Compose BOM (latest stable)
   - Hilt + hilt-navigation-compose
   - Room + KSP
   - Retrofit + OkHttp + Gson
   - Coroutines + Flow
   - Security Crypto KTX (for EncryptedSharedPreferences — JWT storage)
   - Navigation Compose
   - kotlinx-serialization-json (required for Compose Type-Safe Navigation)
   - Core KTX, Lifecycle, Activity Compose

   Also add the `kotlin.serialization` Gradle plugin — it is required for type-safe
   routes in Compose Navigation to compile correctly.

3. Create AndroidManifest.xml with permissions:
   RECORD_AUDIO, INTERNET, FOREGROUND_SERVICE, FOREGROUND_SERVICE_MICROPHONE,
   POST_NOTIFICATIONS, WAKE_LOCK, RECEIVE_BOOT_COMPLETED

4. Implement the Compose Theme (V1 colors, typography, dark background)

5. Create MainActivity with Compose NavHost shell (empty screens as placeholders)

6. Add EdrakApplication with @HiltAndroidApp

DO NOT implement any business logic. Only architecture skeleton, DI setup, and theme.
Reference the old app at: /Users/eslamfaisal/Desktop/work/second_brain/edrak_android/
for the theme and any useful patterns.
```

---

### 📌 Prompt 1B — Auth Feature (Login + Register)

```
In the Edrak V2 Android project at:
/Users/eslamfaisal/Desktop/work/second_brain/edrak_v2_android/

Implement the Auth Feature following Clean Architecture (MVI + Hilt):

Domain Layer:
- AuthRepository interface
- LoginUseCase(email, password) → Flow<Result<AuthTokens>>
- RegisterUseCase(name, email, password) → Flow<Result<AuthTokens>>
- AuthTokens data class (accessToken, refreshToken)

Data Layer:
- AuthRepositoryImpl (implements AuthRepository)
- AuthApiService (Retrofit interface):
  POST /api/v1/auth/login   → LoginResponse
  POST /api/v1/auth/register → LoginResponse
- LoginRequest, RegisterRequest, LoginResponse DTOs
- TokenManager: stores JWT in EncryptedSharedPreferences (NOT plain SharedPrefs)
- AuthInterceptor: attaches Bearer token to every request, refreshes on 401

Presentation Layer (MVI):
- AuthViewModel (Hilt ViewModel)
- LoginScreen Composable (email field, password field, login button, link to register)
- RegisterScreen Composable (name, email, password, confirm password, register button)
- AuthState sealed class: Idle, Loading, Success(user), Error(message)
- AuthEvent sealed class: OnLogin, OnRegister, OnNavigateToOnboarding

UI Requirements:
- Dark theme background (#0B132B)
- Neon Cyan (#45F0DF) accent on buttons and focus states
- Loading indicator during API call
- Error snackbar on failure
- On success: navigate to Onboarding if voice not set up, else to Home

After successful login, check SharedPrefs flag "voice_setup_complete":
- false → navigate to VoiceSetupScreen
- true  → navigate to HomeScreen

Note: Use the same backend API as V1 (auth endpoints unchanged in V2).
```

---

### 📌 Prompt 1C — Voice Fingerprint Onboarding Flow

```
In the Edrak V2 Android project at:
/Users/eslamfaisal/Desktop/work/second_brain/edrak_v2_android/

Implement the Voice Fingerprint Onboarding feature:

The user must enroll their voice after first registration so the system can identify them
as the primary speaker in future conversations.

Flow:
1. OnboardingWelcomeScreen: explain what voice setup is + "Start Setup" button
2. VoiceRecordingScreen: 
   - Show a prompt text for the user to read aloud (e.g., "مرحباً، هذا صوتي في Edrak")
   - Use 3 different phrases, record each one ~3 seconds
   - Show waveform animation while recording
   - Upload each recording to backend: POST /api/v2/voice/enroll (multipart audio)
3. VoiceSetupSuccessScreen: confirmation + "Start using Edrak" button → Home

Domain Layer:
- VoiceEnrollmentRepository interface
- EnrollVoiceUseCase(audioFile: File) → Flow<Result<Unit>>

Data Layer:
- VoiceApiService: POST /api/v2/voice/enroll (multipart/form-data)
- VoiceEnrollmentRepositoryImpl

Presentation Layer (MVI):
- OnboardingViewModel
- OnboardingState: Idle, Recording, Uploading, Success, Error
- OnboardingEvent: StartRecording, StopRecording, Retry, Continue

Android Audio:
- Use MediaRecorder to record audio as M4A/WAV
- File stored temporarily in cacheDir, deleted after upload

After success:
- Set SharedPrefs flag: "voice_setup_complete" = true
- Navigate to HomeScreen

Note: The backend /api/v2/voice/enroll will be built in Phase 2.
During development, mock the response with a 2 second delay and success result.
```

---

### 📌 Prompt 1D — Room Database Schema

```
In the Edrak V2 Android project at:
/Users/eslamfaisal/Desktop/work/second_brain/edrak_v2_android/

Implement the complete Room Database schema for V2.

This is the most critical component — Room DB is the Single Source of Truth (SSOT).
Everything is written to Room first, before any network call.

Entities:

1. ConversationEntity
   - id: String (UUID, primary key)
   - startTime: Long (epoch ms)
   - endTime: Long? (null if still active)
   - syncStatus: SyncStatus

2. TranscriptChunkEntity
   - id: String (UUID, primary key)
   - conversationId: String (foreign key → ConversationEntity)
   - speakerTag: String ("SPEAKER_1", "SPEAKER_2", etc.)
   - text: String
   - timestampMs: Long
   - syncStatus: SyncStatus

3. DetectedActionEntity
   - id: String (UUID, primary key)
   - conversationId: String
   - type: ActionType
   - title: String
   - payload: String (JSON blob)
   - detectedAt: Long
   - executed: Boolean (default false)
   - syncStatus: SyncStatus

Enums:
- SyncStatus: LOCAL_ONLY, SYNCING, SYNCED, SYNC_FAILED
- ActionType: MEETING, ALARM, TASK, NOTE

DAOs:
- ConversationDao: insert, update, getActiveConversation, getAllByDate
- TranscriptChunkDao: insert, updateSyncStatus, getUnsyncedChunks, getByConversation
- DetectedActionDao: insert, updateExecuted, getPendingActions, getByConversation

AppDatabase:
- @Database(entities = [...], version = 1)
- Singleton via Hilt (DatabaseModule)
- TypeConverters for SyncStatus and ActionType enums

Also set up DatabaseModule (Hilt) providing Room singleton.
Write basic unit tests for each DAO using in-memory Room.
```

---

## PHASE 2 — Backend Refactoring

---

### 📌 Prompt 2A — Backend DB Migration (Flyway)

```
In the Edrak V2 Spring Boot backend at:
/Users/eslamfaisal/Desktop/work/second_brain/second_brain_backend/

Create Flyway SQL migrations to add the new V2 tables.
Do NOT drop or modify any existing V1 tables — additive only.

New tables to create:

V2__add_voice_signatures.sql:
CREATE TABLE voice_signatures (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    enrolled_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(user_id)
);

V3__add_conversations.sql:
CREATE TABLE conversations (
    id UUID PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    started_at TIMESTAMP NOT NULL,
    ended_at TIMESTAMP,
    sync_status VARCHAR(20) NOT NULL DEFAULT 'RECEIVED',
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE transcript_chunks (
    id UUID PRIMARY KEY,
    conversation_id UUID NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    speaker_tag VARCHAR(20) NOT NULL,
    text TEXT NOT NULL,
    chunk_timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_chunks_conversation ON transcript_chunks(conversation_id);
CREATE INDEX idx_chunks_timestamp ON transcript_chunks(chunk_timestamp);

CREATE TABLE detected_actions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    type VARCHAR(20) NOT NULL,
    title VARCHAR(500) NOT NULL,
    payload JSONB,
    detected_at TIMESTAMP NOT NULL DEFAULT NOW(),
    notified_at TIMESTAMP,
    executed BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE daily_digests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    digest_date DATE NOT NULL,
    digest_json JSONB NOT NULL,
    generated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, digest_date)
);

Also add JPA Entities and Spring Data JPA Repositories for all new tables.
Follow the existing repository pattern in the V1 codebase.
```

---

### 📌 Prompt 2B — Backend: Token Vending Machine + Chunk Ingestion API

```
In the Edrak V2 Spring Boot backend at:
/Users/eslamfaisal/Desktop/work/second_brain/second_brain_backend/

Implement two new API controllers (V2):

1. Token Vending Machine — GET /api/v2/stt/token
   - Authenticated endpoint (JWT required)
   - Backend exchanges Google service account for a short-lived OAuth2 token
   - Scopes: https://www.googleapis.com/auth/cloud-platform
   - Token TTL: 55 minutes (refresh before the 60 min Google expiry)
   - Cache the Google OAuth token GLOBALLY in the backend (NOT per-user).
     This token represents the Backend's own Service Account identity, not any individual user.
     Distribute the same valid token to ALL authenticated users who request it until it expires.
     Creating a new token per user would exhaust Google's quota needlessly.
   - Return: { "token": "ya29.xxx", "expiresAt": "2026-03-15T12:00:00Z" }
   - The service account JSON key is loaded from environment variable GOOGLE_STT_SA_KEY
   - NEVER expose or log the service account key

2. Transcript Chunk Ingestion — POST /api/v2/transcripts/chunk
   Request body:
   {
     "conversationId": "uuid",
     "chunkIndex": 3,
     "startedAt": "ISO datetime",
     "endedAt": "ISO datetime",
     "transcript": [
       { "speaker": "SPEAKER_1", "text": "...", "ts": "HH:mm:ss" }
     ]
   }
   
   Processing:
   a. Upsert the Conversation record (create if new, update endedAt if exists)
   b. Save all TranscriptChunk rows to PostgreSQL
   c. Return 202 Accepted immediately (async processing below)
   
   Async processing (@Async after returning 202):
   a. Fetch the last 5 minutes of transcript for this conversationId from DB (for context)
   b. Build Gemini prompt:
      - Context: [previous 5 min transcript]
      - New chunk: [current transcript]
      - Task: Identify which SPEAKER_X is the device owner (most likely to give commands/reminders)
              Detect any MEETING, ALARM, TASK, or NOTE actions
              Return structured JSON
   c. Call Gemini Flash API
   d. Parse response → save DetectedAction records
   e. For each action: send FCM push to the user's device token
   
   FCM Push payload:
   {
     "type": "ACTION_DETECTED",
     "actionId": "uuid",
     "actionType": "MEETING",
     "title": "Action title"
   }

Follow existing service/controller patterns in the V1 codebase.
Use existing GeminiService and FcmService from V1 and extend them.
```

---

### 📌 Prompt 2C — Backend: Voice Enrollment API

```
In the Edrak V2 Spring Boot backend at:
/Users/eslamfaisal/Desktop/work/second_brain/second_brain_backend/

Implement the Voice Enrollment API:

POST /api/v2/voice/enroll (multipart/form-data)
- Authenticated (JWT required)
- Accept audio file (M4A or WAV, max 10MB)
- Call Google Cloud Speech-to-Text to verify the audio is valid
- Store a record in voice_signatures table for this user
- Return: { "enrolled": true, "enrolledAt": "ISO datetime" }

GET /api/v2/voice/status
- Returns whether the user has completed voice enrollment
- Return: { "enrolled": boolean, "enrolledAt": "ISO datetime or null" }

Note: In V2, we do NOT actually do voice biometric matching.
The voice enrollment is stored so that in V3 (offline mode) we can use it.
For now, enrolling simply marks the user as "voice setup complete".
The Gemini AI will infer who is the "User" from conversation context, not biometrics.

Add validation:
- If user already enrolled, allow re-enrollment (overwrite)
- Delete the uploaded file after processing (do not store audio)
```

---

## PHASE 3 — Android Foreground Service

---

### 📌 Prompt 3A — Foreground Service + VAD + Audio Focus

```
In the Edrak V2 Android project at:
/Users/eslamfaisal/Desktop/work/second_brain/edrak_v2_android/

Implement the EdrakListeningService (Foreground Service):

This is the most critical component. It must work when the app is fully backgrounded.

1. Service Setup:
   - Extend Service (not IntentService)
   - @AndroidEntryPoint for Hilt injection
   - foregroundServiceType="microphone" in manifest
   - Persistent notification with:
     - Title: "Edrak is listening"
     - Actions: Pause / Stop buttons
   - Acquire PARTIAL_WAKE_LOCK on start, release on stop

2. Audio Recording:
   - AudioRecord with:
     - Source: MediaRecorder.AudioSource.VOICE_COMMUNICATION
     - Sample rate: 16000 Hz (required by Google STT)
     - Channel: MONO
     - Format: PCM 16-bit
     - Buffer: 4096 bytes
   - Read in a continuous loop on Dispatchers.IO coroutine

3. VAD (Voice Activity Detection):
   - Simple but effective: calculate RMS energy of each buffer frame
   - Threshold: if RMS < VAD_THRESHOLD → silence (skip, don't send to STT)
   - When silence detected for 60 consecutive seconds → mark conversation as ended
   - Port and improve VAD from the V1 app at:
     /Users/eslamfaisal/Desktop/work/second_brain/edrak_android/

4. Audio Focus:
   - Request audio focus: AUDIOFOCUS_GAIN (continuous, background)
   - Implement OnAudioFocusChangeListener:
     - AUDIOFOCUS_LOSS → stop gRPC stream, pause recording
     - AUDIOFOCUS_LOSS_TRANSIENT → pause gRPC stream
     - AUDIOFOCUS_GAIN → auto-resume gRPC stream
   
5. Service Control via Broadcasts:
   - ACTION_START_LISTENING
   - ACTION_PAUSE_LISTENING
   - ACTION_STOP_LISTENING
   - Update notification on each state change

6. Conversation Management:
   - On service start → create new ConversationEntity in Room (SyncStatus: LOCAL_ONLY)
   - On 60s silence → update ConversationEntity.endTime, trigger sync
   - On service stop → update endTime, trigger sync

DO NOT implement gRPC / STT in this prompt — that is Phase 3B.
Focus only on the service lifecycle, audio capture, VAD, and audio focus.
```

---

### 📌 Prompt 3B — Google STT V2 gRPC Integration

```
In the Edrak V2 Android project at:
/Users/eslamfaisal/Desktop/work/second_brain/edrak_v2_android/

Integrate Google Cloud Speech-to-Text V2 into the Foreground Service.

IMPORTANT SECURITY RULE: The Android app must NEVER contain a Google service account key or API key.
Authentication flow:
1. Call our backend: GET /api/v2/stt/token (JWT authenticated)
2. Receive short-lived OAuth token (valid ~55 min)
3. Use this token to authenticate the gRPC stream to Google STT

Dependencies to add to build.gradle:
   - google-cloud-speech (grpc-based)
   - grpc-okhttp (for Android gRPC transport)
   - protobuf-java

   IMPORTANT — gRPC Build Conflict Fix:
   Add a `packaging` block inside the `android {}` block in app/build.gradle.kts to avoid
   META-INF file conflicts that will crash the build:
   ```
   packaging {
       resources {
           excludes += setOf(
               "META-INF/INDEX.LIST",
               "META-INF/DEPENDENCIES",
               "META-INF/io.netty.versions.properties"
           )
       }
   }
   ```
   Skip this and the app will NOT compile.

Implementation:

1. SttTokenManager (singleton, Hilt):
   - Holds the current OAuth token + expiry
   - Auto-refreshes 5 min before expiry
   - Thread-safe token refresh

2. SpeechStreamManager (Hilt, injected into service):
   - Opens streaming recognition session using current token
   - Config:
     - language_code: "ar-EG" (primary), ["ar", "en-US"] (alternatives)
     - enable_speaker_diarization: true
     - min_speaker_count: 1
     - max_speaker_count: 6
     - model: "latest_long"
     - enable_automatic_punctuation: true
   - Send audio frames received from VAD (speech frames only)
   - Receive StreamingRecognizeResponse
   - On final result → extract words with speakerTag → reconstruct sentences per speaker
   - Emit via StateFlow/SharedFlow to be collected in the Service

3. In EdrakListeningService:
   - When VAD detects speech → send PCM frame to SpeechStreamManager
   - Collect emitted diarized sentences
   - Write each sentence immediately to Room:
     TranscriptChunkEntity(speakerTag="SPEAKER_1", text="...", timestampMs=...)
   - After write → launch coroutine to sync: POST /api/v2/transcripts/chunk

4. Handle gRPC errors:
   - UNAUTHENTICATED → refresh token → retry
   - UNAVAILABLE → exponential backoff
   - On token expiry mid-session → refresh and open new session seamlessly
```

---

## PHASE 4 — Android Sync + FCM

---

### 📌 Prompt 4A — Sync Strategy (Coroutines + WorkManager Fallback)

```
In the Edrak V2 Android project at:
/Users/eslamfaisal/Desktop/work/second_brain/edrak_v2_android/

Implement the transcript sync strategy.

Primary Sync (while Foreground Service is active):
- After writing a TranscriptChunkEntity to Room → immediately launch a coroutine (Dispatchers.IO)
- Group chunks by conversationId
- Every 60s of silence OR 5 min of accumulated speech → batch and POST to:
  POST /api/v2/transcripts/chunk
- On success: update all sent chunks to SyncStatus.SYNCED
- On failure: set to SyncStatus.SYNC_FAILED (WorkManager picks it up)

WorkManager Sync (Fallback — for SYNC_FAILED rows):
- OneTimeWorkRequest triggered after network becomes available
- Reads all SYNC_FAILED and LOCAL_ONLY chunks from Room
- Sends them in batches (max 50 chunks per request)
- On success: mark SYNCED
- On failure: exponential backoff, max 3 retries

ConnectivityManager:
- Register network callback in the Foreground Service
- On network available → trigger WorkManager fallback job

TranscriptApiService (Retrofit):
- POST /api/v2/transcripts/chunk → returns 202 Accepted

SyncRepository:
- syncChunk(conversationId): suspend fun — used by primary coroutine sync
- scheduleFallbackSync(): used by connectivity callback

Make sure all sync operations have proper error handling and update SyncStatus correctly.
```

---

### 📌 Prompt 4B — FCM Integration + Action Handling

```
In the Edrak V2 Android project at:
/Users/eslamfaisal/Desktop/work/second_brain/edrak_v2_android/

Implement FCM push notification handling for real-time AI-detected actions.

1. FirebaseMessagingService (EdrakFcmService):
   - Extend FirebaseMessagingService
   - Save FCM token to backend on token refresh: PUT /api/v1/users/fcm-token
   - Handle incoming message type "ACTION_DETECTED":
     {
       "type": "ACTION_DETECTED",
       "actionId": "uuid",
       "actionType": "MEETING",
       "title": "Schedule meeting with Ahmed tomorrow 5 PM"
     }
   
2. On receiving FCM action:
   - Launch WorkManager OneTimeWorkRequest (guaranteed execution even if app killed)
   - Work: GET /api/v2/actions/{actionId} from backend
   - Parse response → save as DetectedActionEntity in Room
   - Show local notification: "📅 New action detected: [title]"
   - Kotlin Flow in HomeViewModel auto-updates UI when Room changes

3. ActionsApiService (Retrofit):
   - GET /api/v2/actions/{id} → DetectedActionResponse
   - PUT /api/v2/actions/{id}/executed → mark as done on backend

4. Action Execution:
   - User sees the action in Home screen (DetectedActionEntity from Room via Flow)
   - User taps "Set Alarm" → AlarmManager.setExactAndAllowWhileIdle(...)
   - User taps "Add to Calendar" → Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI)
   - On execution → update DetectedActionEntity.executed = true in Room + API call

5. In HomeViewModel:
   val pendingActions: StateFlow<List<DetectedActionEntity>> =
       detectedActionDao.getPendingActions().stateIn(...)
   
   This Flow automatically updates UI when WorkManager writes to Room.
   No polling, no Firestore needed.
```

---

## PHASE 5 — Android UI

---

### 📌 Prompt 5A — Home Screen + Bottom Navigation

```
In the Edrak V2 Android project at:
/Users/eslamfaisal/Desktop/work/second_brain/edrak_v2_android/

Build the main app shell with bottom navigation and the Home Dashboard screen.

Bottom Navigation (3 tabs):
1. 🏠 Home (HomeScreen)
2. 🎙️ Listening (ListeningScreen)
3. 📊 Digest (DigestScreen)

HomeScreen:
- Header: "Good morning, [name] 👋" with today's date
- Pending Actions card section (observe DetectedActionEntity via Flow):
  - Action card: icon by type (📅 meeting, ⏰ alarm, 📝 note)
  - Swipe to dismiss, tap to execute
  - Badge count on Home tab icon
- Today's Conversations list:
  - Each conversation shown as a card:
    - Time range (e.g., "10:32 AM - 11:15 AM")
    - Number of speakers detected
    - First 2 lines of transcript preview
    - Tap → opens Conversation Detail screen
- Sync status indicator (show if any chunks are SYNC_FAILED)

Conversation Detail Screen:
- Full transcript as a chat-style conversation
- Color coded: User (cyan), Person A (white), Person B (gray), etc.
- Scroll to bottom by default (most recent)

Style requirements:
- Dark background #0B132B
- Cards with slight elevation, dark surface #1A2540
- Neon cyan #45F0DF for active states and accents
- Smooth animations on list items
```

---

### 📌 Prompt 5B — Listening Screen (Live Transcript)

```
In the Edrak V2 Android project at:
/Users/eslamfaisal/Desktop/work/second_brain/edrak_v2_android/

Build the Listening Screen — the real-time transcript view.

This screen is shown while the Foreground Service is active.

UI Components:
1. Service Status Bar at top:
   - 🔴 LIVE indicator (pulsing dot animation) when active
   - ⏸️ PAUSED when audio focus lost
   - Speaker count badge

2. START/STOP button (centered, prominent):
   - When stopped: big "Start Listening" button (cyan gradient)
   - When active: "Stop Listening" button (red)
   - Bind to Foreground Service startService/stopService

3. Real-time transcript view (LazyColumn):
   - Observe TranscriptChunkDao.getByConversation(activeConversationId) via Flow
   - Auto-scroll to latest message
   - Each row: [speakerLabel] [text] [timestamp]
   - Color coding: SPEAKER_1 = cyan (User), SPEAKER_2 = white, SPEAKER_3 = gray, etc.
   - Typing animation on the latest incoming sentence

4. Session info footer:
   - Duration: "Session: 00:42:15"
   - Words captured today: "1,247 words"
   - Sync status: "✅ Synced" / "⚠️ 3 pending"

ViewModel:
- ListeningViewModel binds to the Foreground Service state via a ServiceConnection
- Exposes: serviceState (Active/Paused/Stopped), activeConversation, liveTranscripts (Flow)

Note: The transcript is read from Room DB via Flow — NOT directly from the Service.
The Service writes to Room → Flow emits → UI updates. This keeps the architecture clean.
```

---

### 📌 Prompt 5C — Daily Digest Screen

```
In the Edrak V2 Android project at:
/Users/eslamfaisal/Desktop/work/second_brain/edrak_v2_android/

Build the Daily Digest Screen.

This screen shows the AI-generated summary from the nightly 12 AM cron job.

API:
- GET /api/v2/digests/today → DigestResponse
- GET /api/v2/digests?date=YYYY-MM-DD → historical digest

DigestScreen UI:
1. Date header with navigation (← Yesterday | Today →)

2. Summary stats bar:
   - Total speaking time, number of topics, number of actions

3. Conversations section:
   Each conversation card shows:
   - Title (AI generated, e.g., "Morning Meeting with Ahmed")
   - Time range + Category badge (Work / Family / Health / Outing etc.)
   - Category color chip (Work=blue, Family=purple, Health=green, Outing=orange)
   - "Who said what" summary (2-3 lines)
   - Expand to see full topic list and who said what for each topic
   - List of extracted actions from this conversation

4. Empty state:
   - "Your digest will be ready at midnight ☀️"
   - If no data and today not yet processed

5. Pull to refresh

DigestViewModel:
- Fetches digest from API
- Caches in memory (no need to store in Room for now)

Style: consistent with Home screen dark theme.
Category colors as soft, muted chips — not bright/neon.
```

---

## PHASE 6 — Backend AI Pipelines

---

### 📌 Prompt 6A — Hourly Actions Sweep

```
In the Edrak V2 Spring Boot backend at:
/Users/eslamfaisal/Desktop/work/second_brain/second_brain_backend/

Implement the Hourly Actions Sweep scheduled job.

Every 1 hour:
1. For each active user (who has transcript data in the past hour):
   a. Fetch all transcript_chunks for this user from the past 60 minutes
   b. Skip if already processed (track with a "last_sweep_at" timestamp per user)
   c. Fetch the IDs and titles of actions ALREADY detected for this user's conversations
      from the detected_actions table (for the same time window).
   d. Build Gemini prompt with the full hour's transcript AND include the already-detected
      actions list in the prompt context with this instruction:
      "The following actions have already been detected and notified: [list]. IGNORE these —
       only identify NEW actions that are not already in this list."
   e. For each NEW action found (that wasn't already detected):
      - Save to detected_actions table
      - Send FCM push to user's device

This is a catch-all fallback for actions the real-time chunk analysis may have missed.
The duplicate-action guard in step (c)-(d) prevents sending a push for the same action twice.

Use @Scheduled(cron = "0 0 * * * *") // top of every hour
Run as @Async — never block the scheduler thread

Add tracking: last_actions_sweep_at column on users table (Flyway migration).
```

---

### 📌 Prompt 6B — Nightly Deep Analysis Cron (12 AM)

```
In the Edrak V2 Spring Boot backend at:
/Users/eslamfaisal/Desktop/work/second_brain/second_brain_backend/

Implement the Nightly 12 AM Deep Analysis Cron Job.

@Scheduled(cron = "0 0 0 * * *")  // runs at exactly midnight every day

CRITICAL: Do NOT use `#{new java.util.Random().nextInt(300)}` in the cron expression.
Spring evaluates SpEL expressions ONCE at startup, so the "random" offset would be fixed
for the lifetime of the server — not actually random.

Instead, add PROGRAMMATIC jitter at the START of the method body:
```java
// Jitter: wait 0–5 minutes before doing any work
Thread.sleep((long)(Math.random() * 300_000));
```
This makes each nightly run genuinely random within the 5-minute window.

For each user with transcript data today:
1. Fetch ALL transcript_chunks for the day (00:00 → 23:59)
2. Group by conversation_id (ordered by timestamp)

3. Call Gemini with the full day's transcript:
   Prompt tasks:
   a. Correct misrecognized words using conversational context
   b. Identify all distinct topics discussed (group sentences by topic)
   c. For each topic: who said what (User vs SPEAKER_X → give them descriptive names if possible)
   d. Classify each topic into a category: Work, Family, Health, Outing, Finance, Study, Personal
      → If topic doesn't fit any category, suggest a new category name
   e. Extract or validate all actions (MEETING, ALARM, TASK, NOTE)
   f. Generate a concise conversation title for each conversation
   g. Generate a 2-3 sentence summary for each conversation

4. Persist results:
   - Save to daily_digests table (user_id, digest_date, digest_json)
   - Upsert: if digest already exists for today, replace it
   - Upsert topic_categories for any new categories created

5. At 12:05 AM: send FCM push:
   { "type": "DAILY_DIGEST_READY", "date": "2026-03-15" }

The digest_json format:
{
  "date": "...",
  "totalWords": 1247,
  "totalTopics": 5,
  "conversations": [
    {
      "id": "...",
      "title": "Morning standup",
      "category": "Work",
      "startTime": "09:00",
      "endTime": "09:20",
      "topics": ["Sprint planning", "Blocker from Ahmed"],
      "summary": "Team discussed sprint goals...",
      "speakers": { "SPEAKER_1": "User", "SPEAKER_2": "Ahmed" },
      "actions": [{ "type": "TASK", "title": "Send report by 5 PM" }]
    }
  ]
}

Handle timeouts gracefully: if Gemini takes > 30 seconds, save partial result and log.
```

---

## DONE ✅

After completing all prompts above, the app will have:
- ✅ Auth (Login / Register)
- ✅ Voice Fingerprint Onboarding
- ✅ Always-on Foreground Service with VAD + Audio Focus
- ✅ Google STT V2 streaming (secure via Token Vending Machine)
- ✅ Multi-speaker diarization saved to Room DB instantly
- ✅ Real-time sync to Backend via Coroutines + WorkManager fallback
- ✅ AI action detection via Gemini Flash + FCM delivery
- ✅ Home Dashboard + Live Transcript + Daily Digest screens
- ✅ Nightly deep analysis with topic categorization and digest generation
