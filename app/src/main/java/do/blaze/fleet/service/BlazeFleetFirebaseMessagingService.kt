package do.blaze.fleet.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import do.blaze.fleet.R
import do.blaze.fleet.data.local.TokenManager
import do.blaze.fleet.data.model.MobilePushRegisterRequest
import do.blaze.fleet.data.remote.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BlazeFleetFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val tokenManager = TokenManager(applicationContext)
        val api = RetrofitClient(applicationContext).create()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val deviceName = "${Build.MANUFACTURER} ${Build.MODEL}"
                api.registerPushToken(
                    MobilePushRegisterRequest(
                        platform = "android",
                        token = token,
                        deviceName = deviceName,
                        appVersion = "1.0.0"
                    )
                )
            } catch (e: Exception) {
                // Log failure
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.data["title"] ?: message.notification?.title ?: "Alerta BlazeFleet"
        val body = message.data["body"] ?: message.notification?.body ?: "Nueva notificación recibida"
        val kind = message.data["kind"] ?: "alert"
        val entityId = message.data["entity_id"]

        showNotification(title, body, kind, entityId)
    }

    private fun showNotification(title: String, body: String, kind: String, entityId: String?) {
        val channelId = if (kind == "sos" || kind == "panic") "blazefleet_critical" else "blazefleet_alerts"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = if (channelId == "blazefleet_critical") {
                NotificationManager.IMPORTANCE_HIGH
            } else {
                NotificationManager.IMPORTANCE_DEFAULT
            }
            val channel = NotificationChannel(
                channelId,
                if (channelId == "blazefleet_critical") "Alertas Críticas (SOS)" else "Alertas de Flota",
                importance
            ).apply {
                description = "Notificaciones operativas de la plataforma GPS"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("entity_id", entityId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
