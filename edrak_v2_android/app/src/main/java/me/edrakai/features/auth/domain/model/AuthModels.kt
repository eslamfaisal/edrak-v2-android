package me.edrakai.features.auth.domain.model

data class AuthTokens(
    val accessToken: String,
    val refreshToken: String
)

data class User(
    val id: Long,
    val name: String,
    val email: String
)
