# 📏 Coding Standards

## Backend (Java / Spring Boot)

### Architecture Rules

- Follow **Hexagonal Architecture** — Domain layer has zero framework dependencies
- Every feature uses `domain/model/`, `domain/repository/`, `application/service/`, `presentation/controller/`, `presentation/dto/`
- **Constructor injection** via `@RequiredArgsConstructor` — never `@Autowired` on fields
- Services use **Interface + Impl** pattern (`AuthService` + `AuthServiceImpl`)

### Naming

| Type | Convention | Example |
|------|-----------|---------|
| Entity | `XxxEntity` | `UserEntity` |
| DTO (Request) | `XxxRequest` | `RegisterRequest` |
| DTO (Response) | `XxxResponse` | `AuthResponse` |
| Service | `XxxService` / `XxxServiceImpl` | `AuthService` |
| Controller | `XxxController` | `AuthController` |
| Mapper | `XxxMapper` | `UserMapper` |

### Code Quality

- All request DTOs use `@Valid` + Bean Validation annotations
- Never return JPA entities in API responses — always map to DTOs
- Every endpoint returns `ApiResponse<T>` wrapper
- Database changes use Flyway migrations (never `ddl-auto: update`)

---

## Android (Kotlin / Jetpack Compose)

### Architecture Rules

- **Feature-first** with Clean Architecture layers inside each feature
- Repository interfaces return `Result<T>` (kotlin.Result)
- State management with **ViewModel + StateFlow** (MVI pattern)
- DI via **Hilt** — constructor injection only

### Mandatory Patterns

| Pattern | Rule |
|---------|------|
| Text | `stringResource(R.string.xxx)` — NEVER hardcoded strings |
| Colors | `MaterialTheme.colorScheme.*` — NEVER `Color(0xFF...)` inline |
| Typography | `MaterialTheme.typography.*` — NEVER inline `TextStyle()` |
| Spacing | `EdrakSpacing.md` — NEVER `16.dp` inline |
| Lists | `LazyColumn` / `LazyRow` — NEVER `Column { forEach {} }` |
| Null Safety | Safe calls + `let` — NEVER `!!` |

### File Organization

- One public composable per file
- Screen files contain the Screen + private content composables
- ViewModel + UiState + Event in separate files
- Private helpers at the bottom of the file

### RTL Support

- Design for **RTL (Arabic) first**, then verify LTR
- Use `start`/`end` instead of `left`/`right` in padding/alignment
- Test with `ForceRTL` developer option enabled

---

## iOS (Swift / SwiftUI)

### Architecture Rules

- **Feature-first** with Clean Architecture layers inside each feature
- Repository protocols with `async throws` methods
- State management with **@Observable ViewModel** (MVVM-C pattern)
- DI via **DIContainer** — protocol-based constructor injection

### Mandatory Patterns

| Pattern | Rule |
|---------|------|
| Text | `String(localized:)` — NEVER hardcoded strings |
| Colors | `EdrakColors.*` — NEVER `Color.red` inline |
| Typography | `.font(.edrakBodyMedium)` — NEVER inline `.font(.system(size:))` |
| Spacing | `EdrakSpacing.md` — NEVER `16` inline |
| Null Safety | `guard let` / `if let` — NEVER `!` force-unwrap |
| Async | `async/await` — NEVER completion handlers |

### File Organization

- One View per file
- ViewModels in separate files from Views
- Protocols in separate files from implementations
- Extensions in `Core/Extensions/`

### RTL Support

- Design for **RTL (Arabic) first**, then verify LTR
- SwiftUI handles RTL automatically with `leading`/`trailing`
- Test with Arabic language selected in Xcode scheme
