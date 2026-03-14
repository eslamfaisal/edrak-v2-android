# 📁 Folder Structure

## Backend (Spring Boot — Hexagonal/Clean Architecture)

```
second_brain_backend/
├── build.gradle.kts
├── settings.gradle.kts
├── Dockerfile
├── docker-compose.local.yml
├── .env.local
├── src/
│   └── main/
│       ├── java/com/edrak/api/
│       │   ├── EdrakApplication.java
│       │   │
│       │   ├── core/                          # 🔧 Shared Infrastructure
│       │   │   ├── common/
│       │   │   │   ├── ApiResponse.java       # Standard response wrapper
│       │   │   │   └── AuditableEntity.java   # Base entity with timestamps
│       │   │   ├── config/
│       │   │   │   ├── JpaConfig.java         # JPA Auditing
│       │   │   │   ├── CorsConfig.java        # CORS rules
│       │   │   │   ├── AsyncConfig.java       # Async executor
│       │   │   │   └── OpenApiConfig.java     # Swagger/OpenAPI
│       │   │   ├── exception/
│       │   │   │   ├── GlobalExceptionHandler.java
│       │   │   │   ├── ResourceNotFoundException.java
│       │   │   │   ├── BadRequestException.java
│       │   │   │   └── DuplicateResourceException.java
│       │   │   ├── security/
│       │   │   │   ├── SecurityConfig.java
│       │   │   │   ├── JwtService.java
│       │   │   │   └── JwtAuthenticationFilter.java
│       │   │   └── notification/
│       │   │       └── FcmService.java        # Firebase push
│       │   │
│       │   └── features/                      # 🧩 Feature Modules
│       │       ├── auth/
│       │       │   ├── domain/
│       │       │   │   ├── model/
│       │       │   │   │   └── UserEntity.java
│       │       │   │   └── repository/
│       │       │   │       └── UserRepository.java
│       │       │   ├── application/
│       │       │   │   └── service/
│       │       │   │       ├── AuthService.java
│       │       │   │       └── AuthServiceImpl.java
│       │       │   └── presentation/
│       │       │       ├── controller/
│       │       │       │   └── AuthController.java
│       │       │       ├── dto/
│       │       │       │   ├── RegisterRequest.java
│       │       │       │   ├── LoginRequest.java
│       │       │       │   ├── AuthResponse.java
│       │       │       │   └── UserResponse.java
│       │       │       └── mapper/
│       │       │           └── UserMapper.java
│       │       │
│       │       └── memory/
│       │           ├── domain/
│       │           │   ├── model/
│       │           │   │   ├── MemoryChunkEntity.java
│       │           │   │   ├── ExtractedItemEntity.java
│       │           │   │   └── DailyReportEntity.java
│       │           │   └── repository/
│       │           │       ├── MemoryChunkRepository.java
│       │           │       ├── ExtractedItemRepository.java
│       │           │       └── DailyReportRepository.java
│       │           ├── application/
│       │           │   └── service/
│       │           │       ├── MemoryService.java
│       │           │       ├── MemoryServiceImpl.java
│       │           │       ├── GeminiAiService.java
│       │           │       └── DailyDigestScheduler.java
│       │           └── presentation/
│       │               ├── controller/
│       │               │   └── MemoryController.java
│       │               └── dto/
│       │                   ├── IngestRequest.java
│       │                   ├── InsightResponse.java
│       │                   └── ChatRequest.java
│       │
│       └── resources/
│           ├── application.yml
│           ├── application-local.yml
│           ├── application-dev.yml
│           ├── application-prod.yml
│           └── db/migration/
│               ├── V1__create_users_table.sql
│               ├── V2__create_memory_chunks_table.sql
│               ├── V3__create_extracted_items_table.sql
│               └── V4__create_daily_reports_table.sql
```

## Mobile — Android (Kotlin + Jetpack Compose — Clean Architecture with MVI)

```
edrak_android/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── java/com/edrak/app/
│           ├── EdrakApplication.kt            # @HiltAndroidApp
│           ├── MainActivity.kt                # @AndroidEntryPoint
│           │
│           ├── core/                          # 🔧 Shared Infrastructure
│           │   ├── data/
│           │   │   ├── network/
│           │   │   │   ├── ApiService.kt      # Retrofit interface
│           │   │   │   ├── ApiResponse.kt     # Generic response wrapper
│           │   │   │   ├── AuthInterceptor.kt # JWT injection
│           │   │   │   └── NetworkModule.kt   # @Module Hilt
│           │   │   └── local/
│           │   │       ├── EdrakDatabase.kt   # Room database
│           │   │       ├── TokenManager.kt    # DataStore
│           │   │       └── DatabaseModule.kt  # @Module Hilt
│           │   ├── domain/
│           │   │   └── model/
│           │   │       └── Failure.kt         # Error model
│           │   ├── presentation/
│           │   │   ├── theme/
│           │   │   │   ├── Color.kt           # EdrakColors
│           │   │   │   ├── Type.kt            # EdrakTypography
│           │   │   │   ├── Spacing.kt         # EdrakSpacing
│           │   │   │   └── Theme.kt           # EdrakTheme composable
│           │   │   ├── components/            # Reusable composables
│           │   │   │   ├── EdrakButton.kt
│           │   │   │   ├── EdrakTextField.kt
│           │   │   │   ├── EdrakCard.kt
│           │   │   │   ├── LoadingIndicator.kt
│           │   │   │   └── ErrorView.kt
│           │   │   └── navigation/
│           │   │       ├── EdrakNavHost.kt
│           │   │       └── Routes.kt
│           │   └── service/
│           │       ├── EdrakListeningService.kt
│           │       ├── AudioPipeline.kt
│           │       ├── VadService.kt
│           │       ├── SttService.kt
│           │       └── SyncEngine.kt
│           │
│           └── features/                      # 🧩 Feature Modules
│               ├── auth/
│               │   ├── data/
│               │   │   ├── dto/
│               │   │   ├── mapper/
│               │   │   └── repository/
│               │   ├── domain/
│               │   │   ├── model/
│               │   │   ├── repository/
│               │   │   └── usecase/
│               │   └── presentation/
│               │       ├── LoginScreen.kt
│               │       ├── RegisterScreen.kt
│               │       ├── AuthViewModel.kt
│               │       ├── AuthUiState.kt
│               │       └── AuthEvent.kt
│               │
│               ├── dashboard/
│               ├── memory/
│               ├── chat/
│               └── settings/
│
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── gradle/
    └── libs.versions.toml                     # Version catalog
```

## Mobile — iOS (Swift + SwiftUI — Clean Architecture with MVVM-C)

```
edrak_ios/
├── Edrak.xcodeproj
├── Edrak/
│   ├── EdrakApp.swift                         # @main entry point
│   │
│   ├── App/
│   │   ├── DIContainer.swift                  # Dependency container
│   │   └── Config.swift                       # API URLs, keys
│   │
│   ├── Core/                                  # 🔧 Shared Infrastructure
│   │   ├── Networking/
│   │   │   ├── APIClient.swift                # URLSession wrapper
│   │   │   ├── APIEndpoint.swift              # Endpoint definitions
│   │   │   ├── APIError.swift                 # Error types
│   │   │   └── AuthInterceptor.swift          # JWT injection
│   │   ├── Storage/
│   │   │   ├── KeychainManager.swift          # Secure token storage
│   │   │   └── UserDefaultsManager.swift      # Preferences
│   │   ├── Extensions/
│   │   └── Utilities/
│   │
│   ├── Domain/                                # 📦 Pure Swift
│   │   ├── Entities/
│   │   ├── Repositories/                      # Protocols
│   │   └── UseCases/
│   │
│   ├── Data/                                  # 💾 Implementation
│   │   ├── DTOs/
│   │   ├── Mappers/
│   │   ├── Repositories/
│   │   └── DataSources/
│   │
│   ├── Presentation/                          # 🎨 UI
│   │   ├── Theme/
│   │   ├── Components/
│   │   ├── Coordinator/
│   │   └── Features/
│   │       ├── Auth/
│   │       ├── Dashboard/
│   │       ├── Memory/
│   │       ├── Chat/
│   │       └── Settings/
│   │
│   ├── Services/                              # 🔧 Background
│   │   ├── ListeningService.swift
│   │   ├── AudioPipeline.swift
│   │   ├── VADService.swift
│   │   ├── STTService.swift
│   │   └── SyncEngine.swift
│   │
│   └── Resources/
│       ├── Assets.xcassets
│       ├── Localizable.xcstrings
│       └── Info.plist
│
├── EdrakTests/
└── EdrakUITests/
```

## Naming Conventions

### Backend (Java)

| Type | Pattern | Example |
|------|---------|---------|
| Entity | `XxxEntity` | `UserEntity`, `MemoryChunkEntity` |
| Repository | `XxxRepository` | `UserRepository` |
| Service (Interface) | `XxxService` | `AuthService` |
| Service (Impl) | `XxxServiceImpl` | `AuthServiceImpl` |
| Controller | `XxxController` | `AuthController` |
| Request DTO | `XxxRequest` | `RegisterRequest` |
| Response DTO | `XxxResponse` | `AuthResponse` |
| Mapper | `XxxMapper` | `UserMapper` |

### Android (Kotlin)

| Type | Pattern | Example |
|------|---------|---------|
| Domain Model | `Name` | `Insight`, `ChatResponse` |
| DTO | `NameDto` | `InsightDto` |
| Repository Interface | `NameRepository` | `MemoryRepository` |
| Repository Impl | `NameRepositoryImpl` | `MemoryRepositoryImpl` |
| UseCase | `VerbNounUseCase` | `GetInsightsUseCase` |
| ViewModel | `NameViewModel` | `InsightsViewModel` |
| UiState | `NameUiState` | `InsightsUiState` |
| Event | `NameEvent` | `InsightsEvent` |
| Screen | `NameScreen` | `InsightsScreen` |

### iOS (Swift)

| Type | Pattern | Example |
|------|---------|---------|
| Domain Entity | `Name` | `Insight`, `ChatResponse` |
| DTO | `NameDTO` | `InsightDTO` |
| Repository Protocol | `NameRepositoryProtocol` | `MemoryRepositoryProtocol` |
| Repository Impl | `NameRepository` | `MemoryRepository` |
| UseCase | `VerbNounUseCase` | `GetInsightsUseCase` |
| ViewModel | `NameViewModel` | `InsightsViewModel` |
| View | `NameView` | `InsightsView` |
| Coordinator | `NameCoordinator` | `AppCoordinator` |
