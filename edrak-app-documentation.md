# 🧠 Project: Edrak (إدراك) - The AI Second Brain

**Version:** 1.0.0
**Platforms:** Native Android (Kotlin + Jetpack Compose) & Native iOS (Swift + SwiftUI) + Spring Boot 3.x Backend
**AI Engine:** Google Gemini 1.5 Flash (Cloud API) & On-Device Offline STT.

## 1. Brand Identity

* **App Name:** Edrak | إدراك (Meaning: Cognition, Realization, and deep understanding in Arabic).
* **Tagline:** "Your silent memory. It listens, organizes, and remembers so you don't have to."
* **Core Values:**
  1. **Absolute Privacy:** Audio NEVER leaves the device. Only transcribed text is securely synced.
  2. **Battery Excellence:** Zero-drain background processing using low-level C++ Voice Activity Detection via native JNI/NDK (Android) and C Interop (iOS).
  3. **Seamless Automation:** Requires minimal user intervention after initial setup.
* **Color Palette:** Deep Midnight Blue (`#0B132B`) for stealth/background mode, Neon Cyan (`#45F0DF`) for AI activity indicators.

## 2. Core Features

1. **Always-On Smart Listening:** Runs as a native Foreground Service (Android) / Background Audio (iOS).
2. **On-Device STT:** Converts speech to text locally and instantly destroys audio buffers from RAM.
3. **Cloud AI Classification:** Spring Boot backend uses Gemini 1.5 Flash to classify text into Tasks, Notes, Reminders, and Categories.
4. **Smart Controls & Scheduling:** Users can control the service via a Sticky OS Notification or set an automatic daily schedule (e.g., Active Mon-Fri, 9 AM - 5 PM).
5. **Nightly Digest (Cron Job):** A backend scheduled job that generates a comprehensive daily summary and sends a push notification at the end of the day.
6. **Chat with Memory (RAG):** Conversational UI to ask the AI about past events (e.g., "What did Ahmed say about the project yesterday?").

## 3. Why Native (not Flutter)?

Due to the performance-critical nature of always-on audio processing, the project uses **native development** for maximum OS integration:

| Concern | Native Advantage |
|---------|-----------------|
| **Background Audio** | Direct ForegroundService (Android) / AVAudioSession (iOS) — no MethodChannel overhead |
| **Battery** | JNI/NDK for C++ VAD (Android), C Interop (iOS) — zero abstraction tax |
| **Low-Level STT** | Direct JNI bindings (Vosk) — no Dart FFI overhead |
| **OS Integration** | Native notifications, scheduling, permissions — zero cross-platform layers |

## 4. Mobile Architecture

### Android: Kotlin + Jetpack Compose
* **Architecture:** MVI (Model-View-Intent) with Clean Architecture
* **DI:** Hilt (compile-time)
* **Async:** Kotlin Coroutines + Flow
* **State:** ViewModel + StateFlow + sealed Events
* **Networking:** Retrofit + OkHttp
* **Local DB:** Room
* **Navigation:** Compose Navigation (type-safe routes)

### iOS: Swift + SwiftUI
* **Architecture:** MVVM-C (Model-View-ViewModel-Coordinator) with Clean Architecture
* **DI:** DIContainer (protocol-based)
* **Async:** Swift Concurrency (async/await)
* **State:** @Observable + ViewState enum
* **Networking:** URLSession
* **Local DB:** SwiftData
* **Navigation:** NavigationStack + Coordinator pattern

## 5. Backend Architecture (Spring Boot 3.x)

Spring Boot acts as the central orchestrator. It receives raw text, communicates securely with Google Gemini API, structures the data, and serves it to both native mobile apps.

* **Framework:** Java 21 + Spring Boot 3.x.
* **Security:** Spring Security + JWT Authentication. Strict Data Isolation (Multi-tenant logic per User ID).
* **AI Client:** Spring AI or `RestClient` configured for Google Gemini (`gemini-1.5-flash` for classification, `text-embedding-004` for RAG).

### Core API Endpoints
* `POST /api/v1/auth/register` & `/login`
* `POST /api/v1/memory/ingest` (Async Processing — returns `202 Accepted`)
* `GET /api/v1/memory/insights?date=YYYY-MM-DD` (Fetches tasks, notes, categories)
* `POST /api/v1/memory/chat` (RAG Chatbot)

## 6. Database Schema (PostgreSQL + pgvector)

See the detailed schema in the [Database Schema documentation](docs/content/backend/database-schema.md).

## 7. AI Prompts Engineering

See the detailed prompts in the [AI Prompts documentation](docs/content/backend/ai-prompts.md).

## 8. Full Documentation

📖 **Complete technical documentation:** Run the MkDocs server locally:
```bash
cd docs && python3 -m mkdocs serve
# Open http://127.0.0.1:8000
```