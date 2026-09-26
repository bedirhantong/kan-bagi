package com.ribuufing.bloodapp.feature.dmchat.data.websocket

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.ribuufing.bloodapp.core.base.BaseUrls
import com.ribuufing.bloodapp.feature.dmchat.data.di.WebSocketClient
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import okhttp3.*
import javax.inject.Inject
import javax.inject.Singleton
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.ribuufing.bloodapp.BuildConfig
import com.ribuufing.bloodapp.core.manager.AuthManager
import com.ribuufing.bloodapp.feature.dmchat.domain.model.LastMessage
import java.util.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Singleton
class ChatWebSocketService @Inject constructor(
    @WebSocketClient private val client: OkHttpClient,
    private val authManager: AuthManager,
    private val gson: Gson
) {
    private var webSocket: WebSocket? = null
    private val messageChannel = Channel<LastMessage>(Channel.BUFFERED)
    private val connectionStateFlow = MutableStateFlow<WebSocketConnectionState>(WebSocketConnectionState.Disconnected)
    private var currentRoomId: String? = null
    private var currentUserId: String? = null

    val messages: Flow<LastMessage> = messageChannel.receiveAsFlow()
    val connectionState: StateFlow<WebSocketConnectionState> = connectionStateFlow

    fun connect(roomId: String, userId: String) {
        // Disconnect existing connection if any
        disconnect()
        
        currentRoomId = roomId
        currentUserId = userId
        
        connectionStateFlow.value = WebSocketConnectionState.Connecting

        val ip = authManager.ipAddress

        /*
        bedirhantong@Bedirhan-MacBook-Air ~ % ipconfig getifaddr en0
         */

        val baseUrl = if (BaseUrls.isEmulator()) {
            "ws://10.0.2.2:8000/chat/ws"
        } else {
            "ws://192.168.1.125:8000/chat/ws"
        }

        Log.d("WebSocket", BuildConfig.PHYSICAL_IP)

        val wsUrl = "${baseUrl}/$roomId/$userId"
        Log.d("WebSocket", "Connecting to: $wsUrl")
        
        val request = Request.Builder()
            .url(wsUrl)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocket", "Connection opened")
                connectionStateFlow.value = WebSocketConnectionState.Connected
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    Log.d("WebSocket", "Received message: $text")
                    // Parse incoming message format "sender_user_id: content"
                    val parts = text.split(":", limit = 2)
                    if (parts.size == 2) {
                        val senderUserId = parts[0].trim()
                        val content = parts[1].trim()
                        
                        // Create LastMessage object
                        val message = LastMessage(
                            sender_user_id = senderUserId,
                            receiver_user_id = if (senderUserId == currentUserId) null else currentUserId,
                            content = content,
                            room_id = currentRoomId,
                            timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
                        )
                        messageChannel.trySend(message)
                    } else {
                        Log.e("WebSocket", "Invalid message format: $text")
                    }
                } catch (e: Exception) {
                    Log.e("WebSocket", "Message parsing failed", e)
                    connectionStateFlow.value = WebSocketConnectionState.Error("Message parsing failed: ${e.message}")
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "Connection closing: $reason")
                connectionStateFlow.value = WebSocketConnectionState.Disconnecting
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "Connection closed: $reason")
                connectionStateFlow.value = WebSocketConnectionState.Disconnected
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "Connection failed", t)
                connectionStateFlow.value = WebSocketConnectionState.Error(t.message ?: "Unknown error")
                // Try to reconnect after failure
                webSocket?.cancel()
                this@ChatWebSocketService.webSocket = null
            }
        })
    }

    fun sendMessage(content: String): Boolean {
        try {
            // Send message in the format expected by the server
            val messageJson = JsonObject().apply {
                addProperty("room_id", currentRoomId)
                addProperty("sender_user_id", currentUserId)
                addProperty("content", content)
            }
            val jsonString = gson.toJson(messageJson)
            Log.d("WebSocket", "Sending message: $jsonString")
            
            val success = webSocket?.send(jsonString) ?: false
            if (!success) {
                Log.e("WebSocket", "Failed to send message")
            }
            return success
        } catch (e: Exception) {
            Log.e("WebSocket", "Error creating message JSON", e)
            return false
        }
    }

    fun disconnect() {
        webSocket?.close(1000, "Normal closure")
        webSocket = null
        currentRoomId = null
        currentUserId = null
        connectionStateFlow.value = WebSocketConnectionState.Disconnected
    }
}

sealed class WebSocketConnectionState {
    object Connected : WebSocketConnectionState()
    object Connecting : WebSocketConnectionState()
    object Disconnecting : WebSocketConnectionState()
    object Disconnected : WebSocketConnectionState()
    data class Error(val message: String) : WebSocketConnectionState()
} 