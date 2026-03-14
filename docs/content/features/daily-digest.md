# 📝 Daily Digest Feature

## Overview

An automated end-of-day AI summary delivered via push notification.

## User Experience

1. At **11:50 PM** (user's timezone), the backend cron job processes today's data
2. Gemini generates a cohesive daily report
3. Push notification: `📊 ملخص يومك جاهز`
4. User taps notification → opens the Daily Digest page

## Digest Page UI

- **Day Overview** — 3-sentence summary of the day
- **Key Events** — Bullet list of important moments
- **Pending Tasks** — Tasks that need attention tomorrow
- **Markdown Report** — Beautiful formatted version with emojis

## History

Users can browse past digests by date using a calendar picker.
