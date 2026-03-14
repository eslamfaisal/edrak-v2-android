# ⏰ Scheduling

Users can configure automatic listening schedules so Edrak activates during work hours and sleeps at night.

## Schedule Configuration

| Setting | Description | Default |
|---------|-------------|---------|
| **Active Days** | Which days of the week to listen | Mon–Fri |
| **Start Time** | When to start the service | 09:00 |
| **End Time** | When to stop the service | 17:00 |
| **Auto-start** | Enable/disable scheduling | Off |

## Implementation

=== "Android (AlarmManager + WorkManager)"

    ```kotlin
    class ScheduleManager @Inject constructor(
        @ApplicationContext private val context: Context,
        private val workManager: WorkManager,
    ) {
        fun setSchedule(schedule: ListeningSchedule) {
            // Cancel existing schedules
            workManager.cancelAllWorkByTag(SCHEDULE_TAG)

            if (!schedule.isEnabled) return

            for (day in schedule.activeDays) {
                // Schedule start
                val startRequest = OneTimeWorkRequestBuilder<StartListeningWorker>()
                    .setInitialDelay(delayUntil(day, schedule.startTime), TimeUnit.MILLISECONDS)
                    .addTag(SCHEDULE_TAG)
                    .build()
                workManager.enqueue(startRequest)

                // Schedule stop
                val stopRequest = OneTimeWorkRequestBuilder<StopListeningWorker>()
                    .setInitialDelay(delayUntil(day, schedule.endTime), TimeUnit.MILLISECONDS)
                    .addTag(SCHEDULE_TAG)
                    .build()
                workManager.enqueue(stopRequest)
            }
        }

        companion object {
            private const val SCHEDULE_TAG = "edrak_schedule"
        }
    }

    @HiltWorker
    class StartListeningWorker @AssistedInject constructor(
        @Assisted context: Context,
        @Assisted workerParams: WorkerParameters,
    ) : CoroutineWorker(context, workerParams) {
        override suspend fun doWork(): Result {
            val intent = Intent(applicationContext, EdrakListeningService::class.java).apply {
                action = EdrakListeningService.ACTION_START
            }
            applicationContext.startForegroundService(intent)
            return Result.success()
        }
    }
    ```

=== "iOS (BGTaskScheduler)"

    ```swift
    final class ScheduleManager {
        static let startIdentifier = "com.edrak.app.startListening"
        static let stopIdentifier = "com.edrak.app.stopListening"

        func setSchedule(_ schedule: ListeningSchedule) {
            // Cancel existing
            BGTaskScheduler.shared.cancel(taskRequestWithIdentifier: Self.startIdentifier)
            BGTaskScheduler.shared.cancel(taskRequestWithIdentifier: Self.stopIdentifier)

            guard schedule.isEnabled else { return }

            for day in schedule.activeDays {
                scheduleTask(
                    identifier: Self.startIdentifier,
                    at: nextOccurrence(day: day, time: schedule.startTime)
                )
                scheduleTask(
                    identifier: Self.stopIdentifier,
                    at: nextOccurrence(day: day, time: schedule.endTime)
                )
            }
        }

        private func scheduleTask(identifier: String, at date: Date) {
            let request = BGProcessingTaskRequest(identifier: identifier)
            request.earliestBeginDate = date
            request.requiresNetworkConnectivity = false
            try? BGTaskScheduler.shared.submit(request)
        }
    }
    ```

## User Experience

1. User opens **Settings → Listening Schedule**
2. Toggles days and sets time range
3. App schedules background work via `WorkManager` (Android) / `BGTaskScheduler` (iOS)
4. At the scheduled time, the Foreground Service (Android) starts automatically or the BGTask triggers listening (iOS)
5. At the end time, the service stops and syncs remaining buffer
