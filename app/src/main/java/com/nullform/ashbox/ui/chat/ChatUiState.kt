package com.nullform.ashbox.ui.chat

import android.view.View
import com.nullform.ashbox.data.entity.ChatMessage

data class ChatUiState(
    val currentChatSessionId: String? = null, // Null if no chat is active/new chat hasn't been saved
    val messages: List<ChatMessage> = emptyList(),
    val currentInputText: String = "",
    val isLoadingMessages: Boolean = false, // Initially true when loading a chat
    val isSendingMessage: Boolean = false, // True when user message is being processed
    val errorMessage: String? = null,
)