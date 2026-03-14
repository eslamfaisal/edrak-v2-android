# 📋 Android Overview

Edrak Android is built with **Kotlin** and **Jetpack Compose**, following **Clean Architecture** with **MVI** (Model-View-Intent) state management.

## Project Configuration

| Property | Value |
|----------|-------|
| **Language** | Kotlin 1.9+ |
| **Min SDK** | 24 (Android 7.0) |
| **Target SDK** | 35 |
| **Compile SDK** | 35 |
| **AGP** | 8.x |
| **Compose BOM** | Latest |
| **Build System** | Gradle (Kotlin DSL) |

## Key Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| **Jetpack Compose** | BOM latest | Declarative UI framework |
| **Hilt** | 2.51+ | Compile-time DI |
| **Kotlin Coroutines** | 1.8+ | Async, structured concurrency |
| **Kotlin Flow** | (in coroutines) | Reactive data streams |
| **Retrofit** | 2.11+ | Type-safe HTTP client |
| **OkHttp** | 4.12+ | HTTP engine + interceptors |
| **Room** | 2.6+ | Local SQLite persistence |
| **DataStore** | 1.1+ | Preferences + encrypted storage |
| **Compose Navigation** | 2.8+ | Type-safe navigation |
| **Material 3** | (in Compose BOM) | Design system components |
| **WorkManager** | 2.9+ | Background scheduling |
| **Lifecycle** | 2.8+ | `collectAsStateWithLifecycle` |
| **Kotlinx Serialization** | 1.7+ | JSON serialization |

## Project Structure

```
edrak_android/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/edrak/app/
│       │   ├── EdrakApplication.kt          # @HiltAndroidApp
│       │   ├── MainActivity.kt              # @AndroidEntryPoint
│       │   │
│       │   ├── core/                        # 🔧 Shared Infrastructure
│       │   │   ├── data/
│       │   │   │   ├── network/
│       │   │   │   │   ├── ApiService.kt        # Retrofit interface
│       │   │   │   │   ├── ApiResponse.kt       # Generic wrapper
│       │   │   │   │   ├── AuthInterceptor.kt   # JWT injection
│       │   │   │   │   └── NetworkModule.kt     # @Module
│       │   │   │   └── local/
│       │   │   │       ├── EdrakDatabase.kt     # Room database
│       │   │   │       ├── TokenManager.kt      # DataStore tokens
│       │   │   │       └── DatabaseModule.kt    # @Module
│       │   │   ├── domain/
│       │   │   │   └── model/
│       │   │   │       └── Failure.kt           # Error model
│       │   │   ├── presentation/
│       │   │   │   ├── theme/
│       │   │   │   │   ├── Color.kt
│       │   │   │   │   ├── Type.kt
│       │   │   │   │   ├── Spacing.kt
│       │   │   │   │   └── Theme.kt
│       │   │   │   ├── components/              # Reusable composables
│       │   │   │   │   ├── EdrakButton.kt
│       │   │   │   │   ├── EdrakTextField.kt
│       │   │   │   │   ├── EdrakCard.kt
│       │   │   │   │   ├── LoadingIndicator.kt
│       │   │   │   │   └── ErrorView.kt
│       │   │   │   └── navigation/
│       │   │   │       ├── EdrakNavHost.kt
│       │   │   │       └── Routes.kt
│       │   │   └── service/
│       │   │       ├── EdrakListeningService.kt # Foreground service
│       │   │       ├── AudioPipeline.kt         # VAD + STT orchestrator
│       │   │       ├── VadService.kt            # Silero VAD (JNI)
│       │   │       ├── SttService.kt            # Vosk STT (JNI)
│       │   │       └── SyncEngine.kt            # Batch uploader
│       │   │
│       │   └── features/                    # 🧩 Feature Modules
│       │       ├── auth/
│       │       │   ├── data/
│       │       │   │   ├── dto/
│       │       │   │   ├── mapper/
│       │       │   │   └── repository/
│       │       │   ├── domain/
│       │       │   │   ├── model/
│       │       │   │   ├── repository/
│       │       │   │   └── usecase/
│       │       │   └── presentation/
│       │       │       ├── LoginScreen.kt
│       │       │       ├── RegisterScreen.kt
│       │       │       ├── AuthViewModel.kt
│       │       │       ├── AuthUiState.kt
│       │       │       └── AuthEvent.kt
│       │       │
│       │       ├── dashboard/
│       │       ├── memory/
│       │       ├── chat/
│       │       └── settings/
│       │
│       └── res/
│           ├── values/
│           │   ├── strings.xml              # English
│           │   └── themes.xml
│           └── values-ar/
│               └── strings.xml              # Arabic
│
├── build.gradle.kts                         # Root build
├── settings.gradle.kts
├── gradle.properties
└── gradle/
    └── libs.versions.toml                   # Version catalog
```

## Naming Conventions

| Type | Pattern | Example |
|------|---------|---------|
| Domain Model | `XxxName` | `Insight`, `ChatResponse` |
| DTO | `XxxDto` | `InsightDto`, `ChatResponseDto` |
| Repository Interface | `XxxRepository` | `MemoryRepository` |
| Repository Impl | `XxxRepositoryImpl` | `MemoryRepositoryImpl` |
| UseCase | `VerbNounUseCase` | `GetInsightsUseCase` |
| ViewModel | `XxxViewModel` | `InsightsViewModel` |
| UiState | `XxxUiState` | `InsightsUiState` |
| Event | `XxxEvent` | `InsightsEvent` |
| Screen | `XxxScreen` | `InsightsScreen` |
| Mapper | `XxxMapper` | `InsightMapper` |
| Hilt Module | `XxxModule` | `NetworkModule`, `RepositoryModule` |
