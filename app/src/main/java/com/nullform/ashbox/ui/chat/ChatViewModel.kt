package com.nullform.ashbox.ui.chat

import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nullform.ashbox.data.entity.ChatMessage
import com.nullform.ashbox.data.entity.ChatSession
import com.nullform.ashbox.data.entity.SenderType
import com.nullform.ashbox.ui.aiutils.OpenAIUtil
// Import your ChatRepository here once you create it
// import com.nullform.ashbox.data.repository.ChatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import javax.inject.Inject

//import androidx.databinding.BindingAdapter

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val openAIUtil: OpenAIUtil
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState

    private var messageLoadingJob: Job? = null

    init {
        _uiState.value =
            ChatUiState(
                currentChatSessionId = UUID.randomUUID().toString(),
                messages = mutableListOf<ChatMessage>(),
                currentInputText = "",
                isLoadingMessages = false,
                isSendingMessage = false,
                errorMessage = null
            )
    }

    /**
     * Call this when a user selects an existing chat or a new chat is explicitly started.
     */
    fun loadChatSession(sessionId: String) {
        messageLoadingJob?.cancel() // Cancel any ongoing message loading
        _uiState.value =
            ChatUiState(
                currentChatSessionId = sessionId,
                isLoadingMessages = true,
                messages = mutableListOf<ChatMessage>() // Clear previous messages
            )
        /*
        // --- THIS PART REQUIRES ChatRepository and Room setup ---
        messageLoadingJob = viewModelScope.launch {
            try {
                // Example: Fetch messages from repository
                // chatRepository.getMessagesForSession(sessionId).collect { messages ->
                //     _uiState.update { it.copy(messages = messages, isLoadingMessages = false) }
                // }

                // For now, simulate loading and then show empty (or mock) messages
                kotlinx.coroutines.delay(500) // Simulate network/DB delay
                 _uiState.update { it.copy(messages = emptyList(), isLoadingMessages = false) }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Failed to load messages: ${e.message}", isLoadingMessages = false)
                }
            }
        }
        */
        // For now, without repository, just update state:
    }

    fun onUserMessageChanged(text: String) {
        _uiState.update { it.copy(currentInputText = text) }
        Log.d("onUserMessageChanged", _uiState.value.currentInputText)
    }

    /**
     * Corrected: Takes the message content as a parameter and updates the ViewModel's state.
     * It does NOT interact with the adapter or any UI component, and no AI simulation.
     */
    fun sendUserMessage() {
        val messageContent: String = _uiState.value.currentInputText
        if (messageContent.isBlank() || _uiState.value.isSendingMessage) {
            return
        }

        val activeSessionId = _uiState.value.currentChatSessionId
        if (activeSessionId == null) {
            Log.e("ChatViewModel", "Cannot send message: currentChatSessionId is null.")
            return
        }

        val newUserMessage = ChatMessage(
            sessionId = activeSessionId,
            content = messageContent,
            sender = SenderType.USER,
             id = UUID.randomUUID().toString()
        )

        updateMessages(newUserMessage, true)

        viewModelScope.launch {
            try {
                val aiResponseContent = OpenAIUtil.getAiResponse(_uiState.value.messages)

                val aiMessage = ChatMessage(
                    sessionId = activeSessionId,
                    content = aiResponseContent.toString(),
                    sender = SenderType.AI,
                    id = UUID.randomUUID().toString()
                )

               updateMessages(aiMessage, false)

            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error getting AI response", e)
                e.printStackTrace() // Important for debugging
                _uiState.update { currentState ->

                    currentState.copy(
                        errorMessage = "Error: Could not get response from AI.",
                        isSendingMessage = false // Always hide the loading indicator on completion/error
                    )
                }            }
        }

    }

    fun updateMessages(message: ChatMessage, sending: Boolean) {
        _uiState.update { currentState ->
            val updatedMessages = currentState.messages.toMutableList().apply {
                add(message)
            }.toList()

            currentState.copy(
                messages = updatedMessages,
                isSendingMessage = sending,
                currentInputText = ""
            )
        }
    }

    /**
     * Called when the FAB is pressed to explicitly start a new chat.
     */
    fun startNewChatFromFab() {
        messageLoadingJob?.cancel()
        _uiState.value =
            ChatUiState( // Reset to a clean new chat state
                currentChatSessionId = UUID.randomUUID().toString(),
                messages = mutableListOf<ChatMessage>(),
                currentInputText = "",
                isLoadingMessages = false,
                isSendingMessage = false,
                errorMessage = null
            )
        // Optionally, you could create and persist a ChatSession here immediately
        // and set its ID in currentChatSessionId.
        // For now, we'll let sendUserMessage() handle creating the session on the first message.
        println("FAB clicked, prepared for new chat.")
    }

    /**
     * Resets the UI state to effectively close the current chat session.
     * This clears all chat-related data and makes the FAB visible again.
     */
    fun closeChatSession() {
        messageLoadingJob?.cancel() // Cancel any ongoing message loading
        _uiState.value =
            ChatUiState( // Reset to a clean, initial state
                currentChatSessionId = null,
                messages = mutableListOf<ChatMessage>(),
                currentInputText = "",
                isLoadingMessages = false,
                isSendingMessage = false,
                errorMessage = null
            )
        println("Chat session closed, UI state reset.")
    }

    fun clearErrorMessage() {
        //_uiState.update { it.copy(errorMessage = null) }
        return
    }
}
