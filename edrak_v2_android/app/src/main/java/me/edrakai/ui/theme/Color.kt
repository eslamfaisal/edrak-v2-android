package me.edrakai.ui.theme

import androidx.compose.ui.graphics.Color

object EdrakColors {
    // Primary — Neon Cyan
    val Primary = Color(0xFF45F0DF)
    val PrimaryDark = Color(0xFF00C9B5)
    val PrimaryLight = Color(0xFF7AF5EA)

    // Backgrounds
    val BackgroundLight = Color(0xFFF6F8F8)
    val BackgroundDark = Color(0xFF102220)

    // Surfaces
    val CardDark = Color(0xFF111D38)
    val NavDark = Color(0xFF060A18)
    val Surface = Color(0xFFFDFDFD)
    val SurfaceDark = Color(0xFF0F1729)
    val SurfaceVariant = Color(0xFFF0F2F5)
    val SurfaceVariantDark = Color(0xFF1A2440)

    // Text — Slate scale
    val Slate100 = Color(0xFFF1F5F9)
    val Slate300 = Color(0xFFCBD5E1)
    val Slate400 = Color(0xFF94A3B8)
    val Slate500 = Color(0xFF64748B)
    val Slate800 = Color(0xFF1E293B)
    val OnPrimary = Color(0xFFFFFFFF)
    val OnSurface = Color(0xFF1A1A2E)
    val OnSurfaceDark = Color(0xFFE0E6ED)
    val OnSurfaceVariant = Color(0xFF6B7280)

    // Accent & Speaker Colors
    val Blue400 = Color(0xFF60A5FA)
    val Orange400 = Color(0xFFFB923C)
    val Purple400 = Color(0xFFA78BFA)
    val ChatUser = Color(0xFF1E3058)

    // Speaker label colors (for transcript view)
    val SpeakerUser = Primary          // Neon cyan — the device owner
    val SpeakerA = Color(0xFFFFFFFF)   // White — Person A
    val SpeakerB = Slate400            // Gray — Person B
    val SpeakerC = Blue400             // Blue — Person C
    val SpeakerD = Orange400           // Orange — Person D

    // Status
    val Success = Color(0xFF10B981)
    val Warning = Color(0xFFF59E0B)
    val Error = Color(0xFFEF4444)
    val Info = Color(0xFF3B82F6)

    // Live indicator
    val LiveRed = Color(0xFFEF4444)
}
