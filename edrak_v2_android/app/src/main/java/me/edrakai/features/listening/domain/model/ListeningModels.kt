package me.edrakai.features.listening.domain.model

data class LiveTranscriptEntry(
    val id: String,
    val speakerTag: String,
    val text: String,
    val timestampMs: Long
)

data class ListeningSession(
    val conversationId: String,
    val startTime: Long,
    val isActive: Boolean
)
