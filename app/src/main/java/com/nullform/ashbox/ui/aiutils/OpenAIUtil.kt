package com.nullform.ashbox.ui.aiutils

import android.util.Log // For Android logging
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.nullform.ashbox.BuildConfig
import com.nullform.ashbox.data.entity.SenderType
import com.nullform.ashbox.data.entity.ChatMessage as AppChatMessage
// Import exceptions from the OpenAI library
import com.aallam.openai.api.exception.OpenAIAPIException
import com.aallam.openai.api.exception.AuthenticationException
import com.aallam.openai.api.exception.RateLimitException
import com.aallam.openai.api.exception.InvalidRequestException
import java.io.IOException // For general network issues

/**
 * A stateless utility object for interacting with the OpenAI API.
 * It provides a simple interface to get AI responses based on a list of application-specific chat messages.
 */
object OpenAIUtil {

    private const val TAG = "OpenAIUtil" // Tag for logging

    // Fetches the API key from BuildConfig. This is where the actual key must be defined.
    private val API_KEY_FROM_CONFIG: String = BuildConfig.API_KEY

    // Lazily initialize the OpenAI client.
    // This allows us to check the API key at the point of first use and log if it's missing.
    private val openAI: OpenAI by lazy {
        if (API_KEY_FROM_CONFIG.isBlank()) {
            Log.e(TAG, "OpenAI API Key from BuildConfig is blank. Please ensure 'API_KEY' is correctly defined in your app's build.gradle file and contains your actual OpenAI API key.")
      } else {
            Log.i(TAG, "OpenAI client is being initialized with API key from BuildConfig.")
        }
        OpenAI(token = API_KEY_FROM_CONFIG) // Pass the key using the 'token' named argument
    }

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
     * @return The content of the AI's response as a String, or null if an error occurs or no response is received.
     */
    suspend fun getAiResponse(messages: List<AppChatMessage>): String? {
        // The 'openAI' client will be initialized here on its first access if not already.
        // If API_KEY_FROM_CONFIG was blank, an AuthenticationException will likely be thrown by the OpenAI library.
        return try {
            val openAiMessages = mapToOpenAiMessages(messages)

            val request = ChatCompletionRequest(
                model = ModelId("gpt-3.5-turbo"),
                messages = openAiMessages
            )

            val completion = openAI.chatCompletion(request) // Accessing openAI here triggers lazy init
            val responseContent = completion.choices.firstOrNull()?.message?.content
            
            if (responseContent == null) {
                Log.w(TAG, "OpenAI response was successful but content was null or empty.")
            }
            responseContent
        } catch (e: AuthenticationException) {
            Log.e(TAG, "OpenAI Authentication Error: ${e.message}", e)
            null
        } catch (e: RateLimitException) {
            Log.e(TAG, "OpenAI Rate Limit Exceeded: ${e.message}", e)
            null
        } catch (e: InvalidRequestException) {
            Log.e(TAG, "OpenAI Invalid Request Error: ${e.message}", e)
            null
        } catch (e: OpenAIAPIException) { // Catches other specific OpenAI API errors
            Log.e(TAG, "OpenAI API Error: ${e.message}", e)
            null
        } catch (e: IOException) { // For network connectivity issues (Ktor might throw this)
            Log.e(TAG, "Network I/O Error during OpenAI request: ${e.message}", e)
            null
        } catch (e: Exception) { // Fallback for any other unexpected errors
            Log.e(TAG, "Unexpected error fetching AI response: ${e.message}", e)
            null
        }
    }
}
