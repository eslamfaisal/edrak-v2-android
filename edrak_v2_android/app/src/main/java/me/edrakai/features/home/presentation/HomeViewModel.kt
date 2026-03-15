package me.edrakai.features.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.edrakai.core.security.TokenManager
import me.edrakai.features.home.domain.model.ConversationSummary
import me.edrakai.features.home.domain.model.DetectedAction
import me.edrakai.features.home.domain.usecase.MarkActionExecutedUseCase
import me.edrakai.features.home.domain.usecase.ObservePendingActionsUseCase
import me.edrakai.features.home.domain.usecase.ObserveTodayConversationsUseCase
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    observePendingActionsUseCase: ObservePendingActionsUseCase,
    observeTodayConversationsUseCase: ObserveTodayConversationsUseCase,
    private val markActionExecutedUseCase: MarkActionExecutedUseCase,
    private val tokenManager: TokenManager,
) : ViewModel() {

    val pendingActions: StateFlow<List<DetectedAction>> = observePendingActionsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val todayConversations: StateFlow<List<ConversationSummary>> = observeTodayConversationsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val userDisplayName: String
        get() = tokenManager.getUserDisplayName() ?: "there"

    fun markActionDone(actionId: String) {
        viewModelScope.launch { markActionExecutedUseCase(actionId) }
    }
}
