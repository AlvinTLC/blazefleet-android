package do.blaze.fleet.data.remote

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import do.blaze.fleet.data.local.TokenManager
import do.blaze.fleet.data.model.LivePosition
import do.blaze.fleet.data.model.NotificationItem
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.*
import java.util.concurrent.TimeUnit

class WebSocketClient(
    private val tokenManager: TokenManager,
    private val api: BlazeFleetApi
) {
    private val client = OkHttpClient.Builder()
        .pingInterval(25, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val gson = Gson()

    private val _isConnected = MutableStateFlow(false)
    val isConnected = _isConnected.asStateFlow()

    private val _livePositions = MutableStateFlow<Map<String, LivePosition>>(emptyMap())
    val livePositions = _livePositions.asStateFlow()

    private val _alerts = MutableSharedFlow<NotificationItem>()
    val alerts = _alerts.asSharedFlow()

    private var shouldReconnect = true
    private var reconnectAttempt = 0

    fun connect() {
        shouldReconnect = true
        scope.launch {
            try {
                val ticketResp = api.getWSTicket()
                if (ticketResp.isSuccessful && ticketResp.body() != null) {
                    val ticket = ticketResp.body()!!.ticket
                    val serverUrl = tokenManager.getServerUrl()
                    val wsUrl = serverUrl.replace("http://", "ws://")
                        .replace("https://", "wss://") + "/ws/live?ticket=$ticket&v=2"

                    val request = Request.Builder()
                        .url(wsUrl)
                        .header("User-Agent", "BlazeFleet-Android/1.0")
                        .build()

                    webSocket = client.newWebSocket(request, createListener())
                } else {
                    scheduleReconnect()
                }
            } catch (e: Exception) {
                Log.e("BlazeFleetWS", "Connect error", e)
                scheduleReconnect()
            }
        }
    }

    private fun createListener() = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            _isConnected.value = true
            reconnectAttempt = 0
            Log.d("BlazeFleetWS", "Connected to live stream")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            try {
                val element = JsonParser.parseString(text)
                if (element.isJsonObject) {
                    val obj = element.asJsonObject
                    val type = obj.get("type")?.asString

                    if (type == "notification") {
                        val alert = gson.fromJson(text, NotificationItem::class.java)
                        scope.launch { _alerts.emit(alert) }
                    } else {
                        val pos = gson.fromJson(text, LivePosition::class.java)
                        if (pos != null && pos.trackerId.isNotEmpty()) {
                            val current = _livePositions.value.toMutableMap()
                            current[pos.trackerId] = pos
                            _livePositions.value = current
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("BlazeFleetWS", "Error parsing frame: $text", e)
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            _isConnected.value = false
            Log.w("BlazeFleetWS", "Connection failed", t)
            if (shouldReconnect) {
                scheduleReconnect()
            }
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            _isConnected.value = false
            Log.d("BlazeFleetWS", "Closed: $code / $reason")
            if (shouldReconnect) {
                scheduleReconnect()
            }
        }
    }

    private fun scheduleReconnect() {
        reconnectAttempt++
        val delayMs = (Math.min(Math.pow(2.0, reconnectAttempt.toDouble()), 30.0) * 1000).toLong()
        scope.launch {
            delay(delayMs)
            if (shouldReconnect) {
                connect()
            }
        }
    }

    fun disconnect() {
        shouldReconnect = false
        webSocket?.close(1000, "Normal closure")
        webSocket = null
        _isConnected.value = false
    }
}
