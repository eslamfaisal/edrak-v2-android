package me.edrakai.features.digest.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

interface DigestApiService {
    @GET("api/v2/digests/today")
    suspend fun getTodayDigest(): DigestResponse

    @GET("api/v2/digests")
    suspend fun getDigestByDate(@Query("date") date: String): DigestResponse
}

@Serializable
data class DigestResponse(
    @SerialName("date")           val date: String,
    @SerialName("totalWords")     val totalWords: Int,
    @SerialName("totalTopics")    val totalTopics: Int,
    @SerialName("conversations")  val conversations: List<DigestConversationDto>
)

@Serializable
data class DigestConversationDto(
    @SerialName("id")        val id: String,
    @SerialName("title")     val title: String,
    @SerialName("timeRange") val timeRange: String,
    @SerialName("category")  val category: String,
    @SerialName("summary")   val summary: String,
    @SerialName("actions")   val actions: List<String>
)
