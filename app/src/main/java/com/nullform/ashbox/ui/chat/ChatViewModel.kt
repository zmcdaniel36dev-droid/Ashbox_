package com.nullform.ashbox.ui.chat

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nullform.ashbox.data.ChatRepository
import com.nullform.ashbox.data.Model
import com.nullform.ashbox.data.entity.ChatMessage
import com.nullform.ashbox.data.entity.SenderType
import com.nullform.ashbox.ui.aiutils.OllamaUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi
import javax.inject.Inject

// Define ChatUiState at the top level or ensure it's accessible
data class ChatUiState(
    val currentChatSessionId: Long? = null,
    val messages: List<ChatMessage> = mutableListOf(),
    val currentInputText: String = "",
    val isLoadingMessages: Boolean = false,
    val isSendingMessage: Boolean = false,
    val errorMessage: String? = null,
    val selectedModel: Model? = null, // Keep if direct model object is needed
    val currentModelNameForSubtitle: String = ChatViewModel.DEFAULT_MODEL_NAME // For toolbar
)

@OptIn(InternalSerializationApi::class)
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val app: Application,
    private val chatRepository: ChatRepository
    // It seems OllamaUtil is an object, so direct injection might not be Hilt's typical use.
    // If OllamaUtil is a true singleton object, direct usage is fine.
    // private val ollamaUtil: OllamaUtil // Assuming OllamaUtil is an object, direct use is fine.
) : AndroidViewModel(app) {

    companion object {
        private const val APP_PREFS_FILE_NAME = "ashbox_app_preferences"
        private const val KEY_SELECTED_MODEL_NAME = "key_selected_model_name"
        const val DEFAULT_MODEL_NAME = "tinyllama" // Made public for ChatUiState default
    }

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    // _messages seems redundant if ChatUiState.messages is the source of truth.
    // Consider removing _messages and messages StateFlow if uiState.messages is always up-to-date.
    // private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    // val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private var messageLoadingJob: Job? = null
    private var tempIdCounter = -1L

    init {
        val prefs = app.getSharedPreferences(APP_PREFS_FILE_NAME, Context.MODE_PRIVATE)
        val selectedModelName = prefs.getString(KEY_SELECTED_MODEL_NAME, DEFAULT_MODEL_NAME) ?: DEFAULT_MODEL_NAME
        _uiState.value = ChatUiState(
            currentModelNameForSubtitle = selectedModelName,
            messages = mutableListOf() // Ensure messages list is initialized
            // Initialize other fields as before
        )
    }

    fun loadChatSession(sessionId: Long) {
        messageLoadingJob?.cancel()
        // Read model name when loading a session too, in case it changed.
        val prefs = app.getSharedPreferences(APP_PREFS_FILE_NAME, Context.MODE_PRIVATE)
        val selectedModelName = prefs.getString(KEY_SELECTED_MODEL_NAME, DEFAULT_MODEL_NAME) ?: DEFAULT_MODEL_NAME
        _uiState.value = ChatUiState( // Resetting state, ensure all relevant fields are here
            currentChatSessionId = sessionId,
            isLoadingMessages = true,
            messages = mutableListOf(),
            currentModelNameForSubtitle = selectedModelName
        )
    }

    fun onUserMessageChanged(text: String) {
        _uiState.update { it.copy(currentInputText = text) }
        Log.d("onUserMessageChanged", _uiState.value.currentInputText)
    }

    // This function might need rethinking. If model selection happens in ModelsFragment,
    // this might be for a different purpose or could be removed if ChatViewModel
    // always relies on SharedPreferences for the model name.
    fun selectModel(model: Model) {
        _uiState.update { it.copy(selectedModel = model, currentModelNameForSubtitle = model.name) }
        // If you save to SharedPreferences here, it would be an alternative flow.
        // For now, it just updates UI state if a model object is directly passed.
    }

    fun sendUserMessage() {
        val TAG = "sendUserMessage"
        Log.d(TAG, "Attempting to send message. Current text: '${_uiState.value.currentInputText}', Sending: ${_uiState.value.isSendingMessage}")

        val userMessageContent = _uiState.value.currentInputText
        if (userMessageContent.isBlank() || _uiState.value.isSendingMessage) {
            Log.w(TAG, "Exiting: Message is blank or already sending.")
            return
        }

        var activeSessionId = _uiState.value.currentChatSessionId
        val isNewChatui = activeSessionId == null

        if (isNewChatui) {
            activeSessionId = tempIdCounter--
            _uiState.update { it.copy(activeSessionId) }
        }

        val newUserMessage = ChatMessage(
            id = tempIdCounter--,
            sessionId = activeSessionId,
            text = userMessageContent,
            sender = SenderType.USER,
            timestamp = System.currentTimeMillis()
        )

        _uiState.update { currentState ->
            currentState.copy(
                messages = currentState.messages + newUserMessage,
                isSendingMessage = true,
                currentInputText = ""
            )
        }
        Log.d(TAG, "UI updated with user message and isSendingMessage=true.")

        // In ChatViewModel.kt, within sendUserMessage()

        viewModelScope.launch {
            Log.d(TAG, "Coroutine launched for AI response.")
            // aiMessageId is the temporary ID for the AI's message in the UI list
            val aiMessageUiId: Long = tempIdCounter-- // Renamed for clarity from your aiMessageId
            var accumulatedAiResponse = "" // Used to build the full AI response text

            try {
                // Placeholder for AI message in UI
                val aiPlaceholderMessage = ChatMessage(
                    id = aiMessageUiId, // Use the UI-specific ID
                    text = "", // Initially empty
                    sender = SenderType.AI,
                    sessionId = activeSessionId, // This is the UI session ID (temp if new chat)
                    timestamp = System.currentTimeMillis()
                )
                _uiState.update { it.copy(messages = it.messages + aiPlaceholderMessage) }
                Log.d(TAG, "UI updated with AI placeholder message with ID: $aiMessageUiId.")

                // Prepare message history for API call, excluding the empty AI placeholder
                val messageHistoryForApi = _uiState.value.messages.filter { it.id != aiMessageUiId }

                val prefs = getApplication<Application>().getSharedPreferences(APP_PREFS_FILE_NAME, Context.MODE_PRIVATE)
                val currentModelName = prefs.getString(KEY_SELECTED_MODEL_NAME, DEFAULT_MODEL_NAME) ?: DEFAULT_MODEL_NAME

                if (_uiState.value.currentModelNameForSubtitle != currentModelName) {
                    _uiState.update { it.copy(currentModelNameForSubtitle = currentModelName) }
                }

                val stream = OllamaUtil.getAiResponseStream(getApplication(), messageHistoryForApi, currentModelName)

                if (stream == null) {
                    Log.e(TAG, "OllamaUtil.getAiResponseStream returned null!")
                    throw IllegalStateException("AI response stream was null.")
                }

                Log.d(TAG, "Collecting AI response stream...")
                // No need for chunkBuffer if we update UI directly and accumulate the full response
                stream.collect { chunk ->
                    accumulatedAiResponse += chunk // Append current chunk to the full response
                    _uiState.update { currentState ->
                        val updatedMessages = currentState.messages.map { msg ->
                            if (msg.id == aiMessageUiId) { // Find the AI message by its temp UI ID
                                msg.copy(text = accumulatedAiResponse) // Update with the so-far-accumulated text
                            } else {
                                msg
                            }
                        }
                        currentState.copy(messages = updatedMessages)
                    }
                }
                // ----- AI Stream Collection Finished -----
                Log.d(TAG, "Stream collection finished. Full AI response: '$accumulatedAiResponse'")

                // ----- DATABASE SAVE LOGIC (Now outside and after stream.collect) -----
                if (prefs.getBoolean("save_chat_history", true)) {
                    var dbSessionIdToUse = _uiState.value.currentChatSessionId // Get current session ID from UI state

                    if (isNewChatui || (dbSessionIdToUse != null && dbSessionIdToUse < 0) || dbSessionIdToUse == null) {
                        val sessionTitle = userMessageContent.take(40) + if (userMessageContent.length > 40) "..." else ""
                        Log.d(TAG, "Creating new DB session with title: $sessionTitle")
                        dbSessionIdToUse = chatRepository.createNewSession(sessionTitle)
                        // Update UI state with the REAL database session ID
                        _uiState.update { it.copy(currentChatSessionId = dbSessionIdToUse) }
                        Log.d(TAG, "New session created in DB with ID: $dbSessionIdToUse")
                    }

                    if (dbSessionIdToUse != null && dbSessionIdToUse >= 0) { // Ensure we have a valid positive DB session ID
                        try {
                            // Save User Message (using original 'userMessageContent')
                            chatRepository.addMessageToSession(
                                dbSessionIdToUse,
                                userMessageContent, // This was captured at the start of sendUserMessage
                                SenderType.USER
                            )
                            Log.d(TAG, "User message saved to DB session ID: $dbSessionIdToUse")

                            // Save AI Message (if not blank)
                            if (accumulatedAiResponse.isNotBlank()) {
                                chatRepository.addMessageToSession(
                                    dbSessionIdToUse,
                                    accumulatedAiResponse, // This is the full AI response
                                    SenderType.AI
                                )
                                Log.d(TAG, "AI message saved to DB session ID: $dbSessionIdToUse")
                            }
                        } catch (ex: Exception) {
                            Log.e(TAG, "Error saving messages to DB: ${ex.message}", ex)
                        }
                    } else {
                        Log.w(TAG, "Invalid or null DB session ID ($dbSessionIdToUse), cannot save messages.")
                    }
                } else {
                    Log.d(TAG, "Save chat history is disabled.")
                }

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

    fun startNewChatFromFab() {
        messageLoadingJob?.cancel()
        val prefs = app.getSharedPreferences(APP_PREFS_FILE_NAME, Context.MODE_PRIVATE)
        val selectedModelName = prefs.getString(KEY_SELECTED_MODEL_NAME, DEFAULT_MODEL_NAME) ?: DEFAULT_MODEL_NAME
        _uiState.value =
            ChatUiState(
                currentChatSessionId = null,
                messages = emptyList(),
                currentInputText = "",
                isLoadingMessages = false,
                isSendingMessage = false,
                errorMessage = null,
                currentModelNameForSubtitle = selectedModelName
            )
    }

    fun closeChatSession() {
        messageLoadingJob?.cancel()
        val prefs = app.getSharedPreferences(APP_PREFS_FILE_NAME, Context.MODE_PRIVATE)
        val selectedModelName = prefs.getString(KEY_SELECTED_MODEL_NAME, DEFAULT_MODEL_NAME) ?: DEFAULT_MODEL_NAME
        _uiState.value =
            ChatUiState(
                currentChatSessionId = null,
                messages = mutableListOf<ChatMessage>(),
                currentInputText = "",
                isLoadingMessages = false,
                isSendingMessage = false,
                errorMessage = null,
                currentModelNameForSubtitle = selectedModelName
            )
        println("Chat session closed, UI state reset.")
    }

    fun clearErrorMessage() {
        // _uiState.update { it.copy(errorMessage = null) } // Implement if needed
        return
    }
}
