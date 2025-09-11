package com.nullform.ashbox.ui.aiutils

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.nullform.ashbox.data.entity.SenderType
import com.nullform.ashbox.data.entity.ChatMessage as AppChatMessage

/**
 * A stateless utility object for interacting with the OpenAI API.
 * It provides a simple interface to get AI responses based on a list of application-specific chat messages.
 */
object OpenAIUtil {

    // TODO: Hardcoding API keys is insecure. This should be moved to a secure location
    // TODO: like BuildConfig fields, loaded from a properties file not in version control.
    private const val API_KEY: String =
    private val openAI = OpenAI(API_KEY)

    /**
     * Converts the application's ChatMessage model to the OpenAI library's model.
     * @param appMessages The list of messages from the app's data layer.
     * @return A list of ChatMessage objects compatible with the OpenAI API.
     */
    private fun mapToOpenAiMessages(appMessages: List<AppChatMessage>): List<ChatMessage> {
        return appMessages.map { appMessage ->
            val role = when (appMessage.sender) {
                SenderType.USER -> ChatRole.User
                SenderType.AI -> ChatRole.Assistant
            }
            ChatMessage(
                role = role,
                content = appMessage.content
            )
        }
    }

    /**
     * Fetches a chat completion response from the OpenAI API.
     * This function is stateless and depends only on the input message list.
     *
     * @param messages A list of the app's internal ChatMessage objects representing the conversation history.
     * @return The content of the AI's response as a String, or null if no response is received.
     */
    suspend fun getAiResponse(messages: List<AppChatMessage>): String? {
        val openAiMessages = mapToOpenAiMessages(messages)

        val request = ChatCompletionRequest(
            // Using a more modern and suitable model for chat
            model = ModelId("gpt-3.5-turbo"),
            messages = openAiMessages
        )

        val completion = openAI.chatCompletion(request)
        return completion.choices.firstOrNull()?.message?.content
    }
}
