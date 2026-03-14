# 🎮 Smart Controls Feature

## Overview

Controls for managing the listening service without opening the app.

## Notification Controls (Android)

The persistent notification provides direct action buttons:

| Button | Action |
|--------|--------|
| ▶ **Start** | Begin listening (shown when paused/stopped) |
| ⏸ **Pause** | Temporarily pause listening |
| ⏹ **Stop** | Stop listening and sync buffer |

## Settings Page

| Setting | Description | Default |
|---------|-------------|---------|
| **Listening Schedule** | Auto start/stop times | Off |
| **Active Days** | Mon-Sun checkboxes | Mon-Fri |
| **Sync Frequency** | Buffer word threshold | 500 words |
| **STT Model** | Download/manage Arabic model | Vosk small |
| **Daily Digest** | Enable/disable nightly summary | On |
| **Theme** | Light / Dark / System | System |
| **Language** | Arabic / English | Arabic |
| **Account** | Email, display name, timezone | — |
| **Privacy** | View/delete all data, export | — |

## Data Management

Users have full control over their data:

- **View History** — browse all captured memories by date
- **Delete Item** — remove individual insights
- **Delete Day** — remove all data for a specific day
- **Delete All** — nuclear option — wipe all data from server
- **Export** — download all data as JSON
