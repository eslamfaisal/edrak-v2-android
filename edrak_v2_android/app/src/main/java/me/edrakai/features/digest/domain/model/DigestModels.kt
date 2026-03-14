package me.edrakai.features.digest.domain.model

data class DailyDigest(
    val date: String,
    val totalWords: Int,
    val totalTopics: Int,
    val conversations: List<DigestConversation>
)

data class DigestConversation(
    val id: String,
    val title: String,
    val timeRange: String,
    val category: String,
    val summary: String,
    val actions: List<String>
)
