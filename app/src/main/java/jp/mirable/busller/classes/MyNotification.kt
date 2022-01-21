package jp.mirable.busller.classes

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import jp.mirable.busller.R

class MyNotification {
    companion object {
        fun sendDepNotification(context: Context,
                       destination: String,
                       hour: Int, minute: Int
        ) {
            val channelId = "jp.mirable.busller.dep_notification_id"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "発車前の通知",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply { description = "あらかじめ設定したバスの発車時刻5分前に通知します。" }
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.createNotificationChannel(channel)
            }
            val builder = NotificationCompat.Builder(context, channelId).apply {
                setSmallIcon(R.drawable.bus_vec24_blue)
                setContentTitle(String.format("%s行き まもなく発車",destination))
                setContentText(String.format("%d:%02d の発車まであと5分です。", hour, minute))
                priority = NotificationCompat.PRIORITY_HIGH
            }

            val id = hour * 100 + minute
            NotificationManagerCompat.from(context).notify(id, builder.build())
        }
    }
}