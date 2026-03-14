# 🏁 Setup Guide

## Prerequisites

| Tool | Version | Install |
|------|---------|---------|
| Java JDK | 17+ | `brew install openjdk@17` |
| Docker | Latest | [docker.com](https://docker.com) |
| Android Studio | Latest | [developer.android.com](https://developer.android.com/studio) |
| Xcode | 15+ | Mac App Store |
| Python | 3.x | `brew install python3` |
| Git | Latest | `brew install git` |

## Backend Setup

### 1. Start PostgreSQL

```bash
cd second_brain_backend
docker compose -f docker-compose.local.yml up -d
```

This starts PostgreSQL 16 with pgvector extension, pre-configured for Edrak.

### 2. Configure Environment

```bash
cp .env.example .env.local
# Edit .env.local with your Gemini API key
```

### 3. Run Backend

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

### 4. Verify

- Swagger UI: [https://edrak-backend-386734725162.us-central1.run.app/swagger-ui.html](https://edrak-backend-386734725162.us-central1.run.app/swagger-ui.html)
- Health: [https://edrak-backend-386734725162.us-central1.run.app/actuator/health](https://edrak-backend-386734725162.us-central1.run.app/actuator/health)

## Android Setup

### 1. Open Project

Open `edrak_android/` in **Android Studio**. Gradle sync will start automatically.

### 2. Configure SDK

Ensure Android SDK 35 and Build Tools are installed via **SDK Manager**.

### 3. Run App

Select a device/emulator and press **Shift+F10** or click the Run button.

### 4. Download STT Model

On first launch, go to **Settings → STT Model → Download Arabic Model**.

## iOS Setup

### 1. Open Project

```bash
open edrak_ios/Edrak.xcodeproj
```

### 2. Select Target

Select an iPhone simulator or connected device in the scheme selector.

### 3. Run App

Press **Cmd+R** to build and run.

### 4. Download STT Model

On first launch, go to **Settings → STT Model → Download Arabic Model**.

## Documentation Setup

```bash
pip install mkdocs-material
cd docs
python3 -m mkdocs serve
# Open http://127.0.0.1:8000
```
