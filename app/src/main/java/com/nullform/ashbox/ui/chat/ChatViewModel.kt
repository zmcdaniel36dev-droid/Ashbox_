package com.nullform.ashbox.ui.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nullform.ashbox.data.Model
import com.nullform.ashbox.data.entity.ChatMessage
import com.nullform.ashbox.data.entity.SenderType
import com.nullform.ashbox.ui.aiutils.OllamaUtil
// Import your ChatRepository here once you create it
// import com.nullform.ashbox.data.repository.ChatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.InternalSerializationApi
import javax.inject.Inject

//import androidx.databinding.BindingAdapter

@OptIn(markerClass = arrayOf(InternalSerializationApi::class))
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val openAIUtil: OllamaUtil
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

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
    /*fun sendUserMessage() {
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

                val aiMessage = ChatMessage(content = "", sender = SenderType.AI, sessionId = activeSessionId)
                _messages.update { currentMessage -> currentMessage + aiMessage }

                val messageHistory = _messages.value
                OllamaUtil.getAiResponseStream(messageHistory)?.collect { chunk ->


                    /*_messages.update { currentMessages ->
                        val lastMessage  = currentMessages.last()
                        val updatedMessages = lastMessage.copy(content = lastMessage.content + chunk)
                        currentMessages.dropLast(1) + updatedMessages
                    }*/
                }

            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error getting AI response", e)
                e.printStackTrace() // Important for debugging
                _uiState.update { currentState ->

                    currentState.copy(
                        errorMessage = "Error: Could not get response from AI.",
                        isSendingMessage = false // Always hide the loading indicator on completion/error
                    )
                }
            }
        }
    }*/

    // In ChatViewModel.kt

    fun selectModel(model: Model) {
        _uiState.update { it.copy(selectedModel = model) }
    }

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

        // 1. Create the user's message
        val newUserMessage = ChatMessage(
            sessionId = activeSessionId,
            content = messageContent,
            sender = SenderType.USER,
            id = UUID.randomUUID().toString()
        )

        // 2. Immediately update the UI with the user's message and enter the "sending" state.
        _uiState.update { currentState ->
            currentState.copy(
                messages = currentState.messages + newUserMessage, // Append the new message
                isSendingMessage = true,
                currentInputText = "" // Clear the input field
            )
        }

        // 3. Launch a coroutine to handle the streaming AI response.
        viewModelScope.launch {
            try {
                // 4. Add an empty placeholder message for the AI response.
                val aiMessageId = UUID.randomUUID().toString()
                val aiPlaceholderMessage = ChatMessage(
                    id = aiMessageId,
                    content = "", // Start with empty content
                    sender = SenderType.AI,
                    sessionId = activeSessionId
                )

                _uiState.update { currentState ->
                    currentState.copy(
                        messages = currentState.messages + aiPlaceholderMessage
                    )
                }

                // 5. Get the current message history and start collecting the stream.
                // Note: We get the state's value *after* adding the placeholder.
                val messageHistory = _uiState.value.messages

                // Assuming OllamaUtil.getAiResponseStream exists and returns a Flow<String>
                OllamaUtil.getAiResponseStream(messageHistory)?.collect { chunk ->
                    // 6. For each chunk, update the UI state again using an immutable approach.
                    _uiState.update { currentState ->
                        val currentMessages = currentState.messages.toMutableList()
                        val lastMessageIndex = currentMessages.lastIndex

                        if (lastMessageIndex != -1) {
                            val lastMessage = currentMessages[lastMessageIndex]
                            // Create a NEW message object with the updated content
                            val updatedMessage = lastMessage.copy(content = lastMessage.content + chunk)
                            // Replace the old message with the new one
                            currentMessages[lastMessageIndex] = updatedMessage
                        }

                        // Update the state with the new list containing the new message object
                        currentState.copy(messages = currentMessages.toList())
                    }
                }

            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error getting AI response stream", e)
                _uiState.update { currentState ->
                    currentState.copy(errorMessage = "Error: Could not get response from AI.")
                }
            } finally {
                // 7. IMPORTANT: Once the stream is finished (or an error occurs),
                // update the state to exit the "sending" mode.
                _uiState.update { currentState ->
                    currentState.copy(isSendingMessage = false)
                }
            }
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