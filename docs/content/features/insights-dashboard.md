# 📊 Insights Dashboard Feature

## Overview

The main screen showing today's AI-classified insights organized by category.

## UI Components

| Component | Description |
|-----------|-------------|
| **Category Chips** | Horizontal scrollable filter (All, Work, Study, Personal...) |
| **Stats Bar** | Today's counts: Tasks (5), Notes (12), Reminders (3) |
| **Insight Cards** | Scrollable list of extracted items |
| **FAB** | Floating action button to start/stop listening |

## Insight Card Design

Each card displays:
- **Type indicator** — Task 📋, Note 📝, Reminder ⏰
- **Category badge** — Color-coded chip (Work 🔵, Health 🔴...)
- **Content** — The extracted text
- **Time** — When it was captured
- **Actions** — Mark as done (tasks), set reminder, delete

## Filters

| Filter | Values |
|--------|--------|
| Date | Today (default), Yesterday, Pick date |
| Category | All, WORK, STUDY, PERSONAL, FINANCE, HEALTH, IDEAS, FAMILY |
| Type | All, Tasks, Notes, Reminders |

## Empty States

| Scenario | Message |
|----------|---------|
| No data today | "Your day's insights will appear here. Start listening to capture memories." |
| No matching filter | "No {category} items found for {date}." |
| First time user | Onboarding card explaining the feature |
