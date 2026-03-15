package me.edrakai.features.auth.domain.model

data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val firebaseCustomToken: String? = null,
)

data class LoggedInUser(
    val id: String,
    val email: String,
    val displayName: String,
    val timezone: String? = null,
)

/** Combined result returned to the ViewModel after a successful login/register. */
data class AuthResult(
    val tokens: AuthTokens,
    val user: LoggedInUser,
)
