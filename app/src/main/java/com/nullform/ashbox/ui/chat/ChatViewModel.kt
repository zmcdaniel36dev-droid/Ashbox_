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
    private var tempIdCounter = -1L


    init {
        _uiState.value =
            ChatUiState(
                currentChatSessionId = null,
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
    fun loadChatSession(sessionId: Long) {
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

    fun selectModel(model: Model) {
        _uiState.update { it.copy(selectedModel = model) }
    }

fun sendUserMessage() {
    val TAG = "sendUserMessage"
    Log.d(TAG, "Attempting to send message. Current text: '${_uiState.value.currentInputText}', Sending: ${_uiState.value.isSendingMessage}")

    val messageContent = _uiState.value.currentInputText
    if (messageContent.isBlank() || _uiState.value.isSendingMessage) {
        Log.w(TAG, "Exiting: Message is blank or already sending.")
        return
    }

    var activeSessionId = _uiState.value.currentChatSessionId
    if (activeSessionId == null) {
        Log.i(TAG, "No active session ID. Creating a new one.")
        activeSessionId = System.currentTimeMillis()
        _uiState.update { it.copy(currentChatSessionId = activeSessionId) }
    }

    val newUserMessage = ChatMessage(
        id = tempIdCounter--,
        sessionId = activeSessionId,
        text = messageContent,
        sender = SenderType.USER
    )

    _uiState.update { currentState ->
        currentState.copy(
            messages = currentState.messages + newUserMessage,
            isSendingMessage = true,
            currentInputText = ""
        )
    }
    Log.d(TAG, "UI updated with user message and isSendingMessage=true.")

    viewModelScope.launch {
        Log.d(TAG, "Coroutine launched for AI response.")
        var aiMessageId: Long? = null
        try {
            val aiPlaceholderMessage = ChatMessage(
                id = tempIdCounter--,
                text = "",
                sender = SenderType.AI,
                sessionId = activeSessionId
            )
            aiMessageId = aiPlaceholderMessage.id // Store the unique ID

            _uiState.update { it.copy(messages = it.messages + aiPlaceholderMessage) }
            Log.d(TAG, "UI updated with AI placeholder message with ID: $aiMessageId.")

            val messageHistory = _uiState.value.messages
            val stream = OllamaUtil.getAiResponseStream(messageHistory)

            if (stream == null) {
                Log.e(TAG, "OllamaUtil.getAiResponseStream returned null!")
                throw IllegalStateException("AI response stream was null.")
            }

            Log.d(TAG, "Collecting AI response stream with AGGRESSIVE batching...")
            val chunkBuffer = StringBuilder()
            stream.collect { chunk ->
                chunkBuffer.append(chunk)
                // --- AGGRESSIVE BATCHING ---
                // Look for sentence-ending punctuation or newlines.
                val lastDelimiter = chunkBuffer.lastIndexOfAny(charArrayOf('.', '!', '?', '\n'))
                if (lastDelimiter != -1) {
                    val textToAppend = chunkBuffer.substring(0, lastDelimiter + 1)
                    chunkBuffer.delete(0, lastDelimiter + 1)

                    // Correct, ID-based immutable update
                    _uiState.update { currentState ->
                        val updatedMessages = currentState.messages.map { msg ->
                            if (msg.id == aiMessageId) {
                                msg.copy(text = msg.text + textToAppend)
                            } else {
                                msg
                            }
                        }
                        currentState.copy(messages = updatedMessages)
                    }
                }
            }

            // After the stream is finished, flush any remaining text in the buffer
            if (chunkBuffer.isNotEmpty()) {
                _uiState.update { currentState ->
                    val updatedMessages = currentState.messages.map { msg ->
                        if (msg.id == aiMessageId) {
                            msg.copy(text = msg.text + chunkBuffer.toString())
                        } else {
                            msg
                        }
                    }
                    currentState.copy(messages = updatedMessages)
                }
            }
            Log.d(TAG, "Stream collection finished.")

        } catch (e: Exception) {
            Log.e(TAG, "An error occurred in the AI response coroutine.", e)
            _uiState.update { it.copy(errorMessage = "Error: ${e.message}") }
        } finally {
            Log.d(TAG, "Coroutine finished. Setting isSendingMessage=false.")
            _uiState.update { it.copy(isSendingMessage = false) }
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
                currentChatSessionId = null,
                messages = emptyList(),
                currentInputText = "",
                isLoadingMessages = false,
                isSendingMessage = false,
                errorMessage = null
            )
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
