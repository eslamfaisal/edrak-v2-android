# 🚀 Getting Started

This guide will get you up and running with the Edrak development environment.

## Prerequisites

| Tool | Version | Purpose |
|------|---------|---------|
| **Java JDK** | 17+ | Spring Boot backend |
| **Gradle** | 8.x | Backend build |
| **Docker** | Latest | PostgreSQL + local services |
| **Android Studio** | Latest (Hedgehog+) | Android native development |
| **Kotlin** | 1.9+ | Android language |
| **Xcode** | 15+ | iOS native development |
| **Swift** | 5.9+ | iOS language |
| **Python** | 3.x | MkDocs documentation |
| **Git** | Latest | Version control |

## Project Structure

```
second_brain/
├── docs/                    # 📖 This documentation (MkDocs)
├── second_brain_backend/    # ⚙️ Spring Boot 3.x API
│   ├── src/main/java/com/edrak/api/
│   ├── src/main/resources/
│   ├── build.gradle.kts
│   ├── docker-compose.local.yml
│   └── Dockerfile
├── edrak_android/           # 🤖 Android (Kotlin + Compose)
│   ├── app/src/main/java/com/edrak/app/
│   ├── build.gradle.kts
│   └── gradle/libs.versions.toml
└── edrak_ios/               # 🍎 iOS (Swift + SwiftUI)
    ├── Edrak/
    ├── Edrak.xcodeproj
    └── EdrakTests/
```

## Step 1: Documentation Site

```bash
# Install MkDocs with Material theme
pip install mkdocs-material

# Serve documentation locally
cd /path/to/second_brain/docs
python3 -m mkdocs serve
# Open http://127.0.0.1:8000
```

## Step 2: Backend Setup

```bash
# 1. Start PostgreSQL with pgvector
cd second_brain_backend
docker compose -f docker-compose.local.yml up -d

# 2. Run Spring Boot
./gradlew bootRun --args='--spring.profiles.active=local'

# 3. Verify: Open Swagger UI
open https://edrak-backend-386734725162.us-central1.run.app/swagger-ui.html
```

## Step 3: Android Setup

```bash
# 1. Open in Android Studio
open -a "Android Studio" edrak_android/

# 2. Sync Gradle (automatic in Android Studio)
# 3. Run on emulator or connected device (Shift+F10)
```

## Step 4: iOS Setup

```bash
# 1. Open in Xcode
open edrak_ios/Edrak.xcodeproj

# 2. Select target device/simulator
# 3. Run (Cmd+R)
```

## Step 5: Download STT Model

On first launch (both platforms), go to **Settings → STT Model → Download Arabic Model**.

## Environment Variables

### Backend (`.env.local`)

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/edrak_db` |
| `DB_USERNAME` | Database user | `edrak_app` |
| `DB_PASSWORD` | Database password | `edrak_local_2026` |
| `JWT_SECRET` | JWT signing key | (generated) |
| `GEMINI_API_KEY` | Google Gemini API key | (required) |
| `FIREBASE_CREDENTIALS_PATH` | Path to Firebase service account JSON | (optional for local) |

!!! tip "Get a Gemini API Key"
    Visit [Google AI Studio](https://aistudio.google.com/apikey) to create a free API key for Gemini 1.5 Flash.
