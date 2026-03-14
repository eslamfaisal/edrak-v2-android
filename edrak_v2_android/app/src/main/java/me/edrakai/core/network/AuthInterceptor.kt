package me.edrakai.core.network

import me.edrakai.core.security.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Attaches Bearer JWT token to every outgoing request.
 * On 401 Unauthorized, attempts to refresh the token once, then retries.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenManager.getAccessToken()

        val request = chain.request().newBuilder().apply {
            if (token != null) {
                header("Authorization", "Bearer $token")
            }
        }.build()

        val response = chain.proceed(request)

        // On 401: token expired → attempt refresh → retry once
        if (response.code == 401) {
            response.close()
            val refreshed = tokenManager.refreshToken()
            if (refreshed) {
                val newToken = tokenManager.getAccessToken()
                val retryRequest = chain.request().newBuilder()
                    .header("Authorization", "Bearer $newToken")
                    .build()
                return chain.proceed(retryRequest)
            }
        }

        return response
    }
}
