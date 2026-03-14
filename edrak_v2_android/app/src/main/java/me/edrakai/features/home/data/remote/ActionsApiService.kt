package me.edrakai.features.home.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.PUT
import retrofit2.http.Path

interface ActionsApiService {
    @PUT("api/v2/actions/{id}/executed")
    suspend fun markExecuted(@Path("id") actionId: String)
}

@Serializable
data class DetectedActionResponse(
    @SerialName("id")             val id: String,
    @SerialName("conversationId") val conversationId: String,
    @SerialName("type")           val type: String,
    @SerialName("title")          val title: String,
    @SerialName("payload")        val payload: String?,
    @SerialName("detectedAt")     val detectedAt: Long,
    @SerialName("executed")       val executed: Boolean
)
