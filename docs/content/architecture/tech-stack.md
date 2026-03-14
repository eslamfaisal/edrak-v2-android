# 🏗️ Edrak V2 — Tech Stack

## Mobile (Android)

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose |
| Architecture | MVI + Clean Architecture |
| DI | Hilt |
| Async | Coroutines + Flow |
| Navigation | Compose Navigation |
| Local DB | Room |
| Network | Retrofit + OkHttp |
| Background | Foreground Service + WorkManager |
| Push | Firebase Cloud Messaging (FCM) |
| Auth Storage | EncryptedSharedPreferences |
| STT | Google Cloud Speech-to-Text V2 (gRPC streaming) |

## Backend (Spring Boot)

| Layer | Technology |
|---|---|
| Framework | Java 21 + Spring Boot 3.x |
| DB | PostgreSQL 16 |
| Migrations | Flyway |
| AI | Google Gemini 1.5 Flash |
| Auth | JWT (Access + Refresh) |
| Push | Firebase Admin SDK (FCM) |
| Scheduler | Spring @Scheduled |
| Async | @Async + ThreadPoolExecutor |

## Why NOT Firestore for real-time?

Room DB + Kotlin Flow + FCM + WorkManager achieves the same result with:
- Zero extra cost
- Cleaner architecture (single source of truth in Room)
- Works fully when app UI is killed (WorkManager)
- No Firestore SDK overhead
