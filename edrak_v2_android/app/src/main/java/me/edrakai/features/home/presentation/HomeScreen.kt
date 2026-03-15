package me.edrakai.features.home.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import me.edrakai.core.database.entity.ActionType
import me.edrakai.core.database.entity.SyncStatus
import me.edrakai.features.home.domain.model.ConversationSummary
import me.edrakai.features.home.domain.model.DetectedAction
import me.edrakai.ui.theme.EdrakColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    onNavigateToListening: () -> Unit,
    onNavigateToDigest: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val actions by viewModel.pendingActions.collectAsState()
    val conversations by viewModel.todayConversations.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = EdrakColors.DeepMidnightBlue,
    ) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {
            // ── Header ───────────────────────────────────────────────────────
            item {
                HomeHeader(displayName = viewModel.userDisplayName)
            }

            // ── Sync status warning ──────────────────────────────────────────
            val failedConvs = conversations.count { it.syncStatus == SyncStatus.SYNC_FAILED }
            if (failedConvs > 0) {
                item {
                    SyncWarningBanner(failedCount = failedConvs)
                }
            }

            // ── Pending Actions ──────────────────────────────────────────────
            if (actions.isNotEmpty()) {
                item {
                    SectionTitle("🎯 Pending Actions", badgeCount = actions.size)
                }
                items(actions, key = { it.id }) { action ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 2 },
                    ) {
                        ActionCard(action = action, onDone = { viewModel.markActionDone(action.id) })
                    }
                }
                item { Spacer(Modifier.height(8.dp)) }
            }

            // ── Today's Conversations ────────────────────────────────────────
            item {
                SectionTitle("🗓️ Today's Conversations", badgeCount = conversations.size.takeIf { it > 0 })
            }

            if (conversations.isEmpty()) {
                item { EmptyConversationsHint(onNavigateToListening) }
            } else {
                items(conversations, key = { it.id }) { convo ->
                    ConversationCard(conversation = convo)
                }
            }
        }
    }
}

// ─── Header ──────────────────────────────────────────────────────────────────

@Composable
private fun HomeHeader(displayName: String) {
    val greeting = when (java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)) {
        in 5..11  -> "Good morning"
        in 12..17 -> "Good afternoon"
        else      -> "Good evening"
    }
    val dateStr = SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp),
    ) {
        Text(
            text = "$greeting, ${displayName.split(" ").firstOrNull() ?: displayName} 👋",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
        Spacer(Modifier.height(4.dp))
        Text(text = dateStr, fontSize = 14.sp, color = EdrakColors.Slate400)
    }
}

// ─── Section Title ────────────────────────────────────────────────────────────

@Composable
private fun SectionTitle(label: String, badgeCount: Int? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        if (badgeCount != null && badgeCount > 0) {
            Box(
                modifier = Modifier
                    .background(EdrakColors.NeonCyan, RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 3.dp),
            ) {
                Text("$badgeCount", fontSize = 12.sp, color = EdrakColors.DeepMidnightBlue, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ─── Action Card ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionCard(action: DetectedAction, onDone: () -> Unit) {
    Card(
        onClick = onDone,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2540)),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Action type icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(action.type.color().copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = action.type.icon(),
                    contentDescription = null,
                    tint = action.type.color(),
                    modifier = Modifier.size(24.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = action.type.label(),
                    fontSize = 11.sp,
                    color = action.type.color(),
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp,
                )
                Text(
                    text = action.title,
                    fontSize = 14.sp,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                imageVector = Icons.Filled.AssignmentTurnedIn,
                contentDescription = "Mark done",
                tint = EdrakColors.Slate400,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

// ─── Conversation Card ────────────────────────────────────────────────────────

@Composable
private fun ConversationCard(conversation: ConversationSummary) {
    val startFmt = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(conversation.startTime))
    val endFmt = conversation.endTime?.let { SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(it)) } ?: "ongoing"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2540)),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Outlined.AccessTime, contentDescription = null, tint = EdrakColors.Slate400, modifier = Modifier.size(14.dp))
                    Text("$startFmt – $endFmt", fontSize = 12.sp, color = EdrakColors.Slate400)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Mic, contentDescription = null, tint = EdrakColors.NeonCyan, modifier = Modifier.size(12.dp))
                    Text("${conversation.speakerCount} speakers", fontSize = 12.sp, color = EdrakColors.Slate400)
                }
            }
            if (conversation.previewText.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = conversation.previewText,
                    fontSize = 13.sp,
                    color = EdrakColors.Slate300,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 19.sp,
                )
            }
        }
    }
}

// ─── Sync Warning ─────────────────────────────────────────────────────────────

@Composable
private fun SyncWarningBanner(failedCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .background(EdrakColors.Warning.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("⚠️", fontSize = 16.sp)
        Text("$failedCount conversation${if (failedCount > 1) "s" else ""} pending sync", fontSize = 13.sp, color = EdrakColors.Warning)
    }
}

// ─── Empty State ──────────────────────────────────────────────────────────────

@Composable
private fun EmptyConversationsHint(onStartListening: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("🎙️", fontSize = 48.sp)
        Spacer(Modifier.height(16.dp))
        Text("No conversations today yet", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = EdrakColors.Slate300)
        Spacer(Modifier.height(8.dp))
        Text("Tap the Listening tab to start recording", fontSize = 14.sp, color = EdrakColors.Slate500)
    }
}

// ─── Extension helpers ────────────────────────────────────────────────────────

private fun ActionType.icon(): ImageVector = when (this) {
    ActionType.MEETING -> Icons.Default.CalendarToday
    ActionType.ALARM   -> Icons.Default.EventNote
    ActionType.TASK    -> Icons.Default.AssignmentTurnedIn
    ActionType.NOTE    -> Icons.Default.Note
}

private fun ActionType.color(): Color = when (this) {
    ActionType.MEETING -> EdrakColors.NeonCyan
    ActionType.ALARM   -> EdrakColors.Orange400
    ActionType.TASK    -> EdrakColors.Blue400
    ActionType.NOTE    -> EdrakColors.Purple400
}

private fun ActionType.label(): String = when (this) {
    ActionType.MEETING -> "MEETING"
    ActionType.ALARM   -> "ALARM"
    ActionType.TASK    -> "TASK"
    ActionType.NOTE    -> "NOTE"
}
