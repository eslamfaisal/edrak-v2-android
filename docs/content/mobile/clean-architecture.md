# 📱 Android — Clean Architecture Overview

## Package Structure

```
me.edrakai/
├── core/
│   ├── di/              (AppModule, NetworkModule, DatabaseModule)
│   ├── network/         (ApiService, AuthInterceptor, NetworkResult)
│   ├── database/        (AppDatabase, DAOs)
│   ├── services/        (EdrakListeningService)
│   ├── notifications/   (NotificationHelper)
│   └── extensions/      (common Kotlin extensions)
├── features/
│   ├── auth/            (login, register)
│   ├── onboarding/      (voice fingerprint setup)
│   ├── home/            (dashboard, action cards)
│   ├── listening/       (live transcript view)
│   └── digest/          (daily AI digest)
└── ui/
    ├── theme/           (Color, Type, Theme)
    └── components/      (shared composables)
```

## Feature Structure (per feature)

```
feature/
├── domain/
│   ├── model/           (pure Kotlin data classes)
│   ├── repository/      (interfaces only)
│   └── usecase/         (one class, one job)
├── data/
│   ├── remote/          (API service + DTOs)
│   ├── local/           (Room entities + DAOs)
│   └── repository/      (implements domain repository)
└── presentation/
    ├── ViewModel        (Hilt, MVI pattern)
    ├── Screen           (Composable)
    ├── State            (sealed class)
    └── Event            (sealed class)
```

## MVI Pattern

```kotlin
// State — what the UI renders
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

// Event — what the UI sends to the ViewModel
sealed class AuthEvent {
    data class Login(val email: String, val password: String) : AuthEvent()
    data class Register(val name: String, val email: String, val password: String) : AuthEvent()
}

// ViewModel
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {
    val state: StateFlow<AuthState> = ...
    fun onEvent(event: AuthEvent) { ... }
}
```

## Room DB (Single Source of Truth)

All data is written to Room first, before any network operation.

| Entity | Purpose |
|---|---|
| `ConversationEntity` | Groups transcript chunks into conversations |
| `TranscriptChunkEntity` | One row per sentence captured |
| `DetectedActionEntity` | AI-detected actions (meetings, alarms, tasks) |

`SyncStatus` field on each entity: `LOCAL_ONLY → SYNCING → SYNCED → SYNC_FAILED`
