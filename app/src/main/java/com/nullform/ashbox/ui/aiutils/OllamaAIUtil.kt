package com.nullform.ashbox.ui.aiutils


import kotlinx.coroutines.flow.Flow
import android.util.Log
import com.nullform.ashbox.data.entity.SenderType
import com.nullform.ashbox.data.entity.ChatMessage as AppChatMessage
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.text.isNotBlank

@OptIn(markerClass = arrayOf(InternalSerializationApi::class))
@Serializable
private data class OllamaRequest(
    val model: String,
    val messages: List<OllamaMessage>,
    val stream: Boolean = false
)

@OptIn(markerClass = arrayOf(InternalSerializationApi::class))
@Serializable
private data class OllamaMessage(
    val role: String,
    val content: String
)

@OptIn(markerClass = arrayOf(InternalSerializationApi::class))
@Serializable
private data class OllamaResponse(
    val message: OllamaMessage
)

/**
 * A utility object for interacting with a local Ollama API.
 */
@InternalSerializationApi
object OllamaUtil {

    private const val TAG = "OllamaUtil"
    // Use 10.0.2.2 to connect to the host machine's localhost from the Android Emulator.
    private const val OLLAMA_API_URL = "http://192.168.5.225:11434/api/chat"
    private const val OLLAMA_MODEL = "tinyllama"

    private val jsonStreamParser = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    // Ktor HttpClient setup for JSON communication
    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(jsonStreamParser)
        }
    }

    /**
     * Converts the application's ChatMessage model to the Ollama library's model.
     */
    private fun mapToOllamaMessages(appMessages: List<AppChatMessage>): List<OllamaMessage> {
        return appMessages.map { appMessage ->
            val role = when (appMessage.sender) {
                SenderType.USER -> "user"
                SenderType.AI -> "assistant"
            }
            OllamaMessage(
                role = role,
                content = appMessage.text
            )
        }
    }

    /**
     * Fetches a chat completion response from the local Ollama API.
     *
     * @param messages A list of the app's internal ChatMessage objects representing the conversation history.
     * @return The content of the AI's response as a String, or null if an error occurs.
     */
    suspend fun getAiResponseStream(messages: List<AppChatMessage>): Flow<String>? = flow {
        try {
            // Ensure stream is true for this endpoint
            Log.d(TAG, "Starting stream request to Ollama API at $OLLAMA_API_URL")

            val ollamaMessages = mapToOllamaMessages(messages)

            val request =
                OllamaRequest(model = OLLAMA_MODEL, messages = ollamaMessages, stream = true)

            // Use preparePost to handle the streaming response
            client.preparePost(OLLAMA_API_URL) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.execute { httpResponse ->
                val channel = httpResponse.bodyAsChannel()
                while (!channel.isClosedForRead) {
                    // Ollama streaming sends newline-delimited JSON objects
                    val line = channel.readUTF8Line()
                    if (line != null && line.isNotBlank()) {
                        // Each line is a JSON object, parse it
                        val ollamaResponse = jsonStreamParser.decodeFromString<OllamaResponse>(line)
                        // Emit only the content part of the message
                        emit(ollamaResponse.message.content)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during Ollama stream: ${e.message}", e)
        }
    }

}