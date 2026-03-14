# Build & Signing

## Release Keystore

| Property | Value |
|----------|-------|
| File | `app/edrak-release.jks` |
| Alias | `edrak` |
| Store password | `eslam1` |
| Key password | `eslam1` |
| Algorithm | RSA 2048 |
| Validity | 10,000 days |

### Gradle Configuration

```kotlin
signingConfigs {
    create("release") {
        storeFile = file("edrak-release.jks")
        storePassword = "eslam1"
        keyAlias = "edrak"
        keyPassword = "eslam1"
    }
}

buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = true
        signingConfig = signingConfigs.getByName("release")
    }
}
```

## Build Commands

```bash
# Debug
./gradlew assembleDebug

# Release (signed)
./gradlew assembleRelease
```

**Output:** `app/build/outputs/apk/release/app-release.apk`

## Firebase SHA Keys

Add both fingerprints to **Firebase Console → Project Settings → Your App → SHA certificate fingerprints**.

### Debug

```
SHA-1:   F1:31:9C:87:83:50:58:EC:BF:C9:7A:D2:02:82:31:C9:04:31:76:E4
SHA-256: B1:2B:5C:BC:8B:3B:7E:16:ED:C7:1E:A7:7A:4E:62:32:FA:B9:69:25:7D:5C:07:09:69:C1:7B:68:01:C3:88:1D
```

### Release

```
SHA-1:   F7:EE:BE:9C:D4:1E:B6:B3:75:BA:2E:23:AC:E8:F1:7B:C8:BD:3A:46
SHA-256: 2B:1F:34:C6:D7:69:70:87:78:80:A7:D7:2B:A4:9E:ED:41:38:99:ED:C0:35:A4:D9:D0:14:DA:AE:42:16:D2:A8
```

### How to Regenerate SHA Keys

```bash
# Debug
keytool -list -v -keystore ~/.android/debug.keystore \
  -alias androiddebugkey -storepass android | grep SHA

# Release
keytool -list -v -keystore app/edrak-release.jks \
  -alias edrak -storepass eslam1 | grep SHA
```

## ProGuard / R8

Release builds use R8 minification:

- `isMinifyEnabled = true` — code shrinking
- `isShrinkResources = true` — resource shrinking
- ProGuard rules: `app/proguard-rules.pro`

## Room Database

Version: **3** with migration chain: v1 → v2 → v3

| Migration | Changes |
|-----------|---------|
| v1 → v2 | Added `speech_entries`, `listening_schedules`, `pending_actions` tables |
| v2 → v3 | Added `snooze_count`, `ringtone_uri`, `is_full_screen`, `source` columns to `pending_actions` |
