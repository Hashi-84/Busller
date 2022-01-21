package jp.mirable.busller.classes

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let { cont ->
            intent?.let { inte ->
                val dest = inte.getStringExtra("dest") ?: "未定義の行き先"
                val hour = inte.getIntExtra("n_hour", 0)
                val min = inte.getIntExtra("n_min", 0)
                MyNotification.sendDepNotification(cont, dest, hour, min)
            }
        }
    }
}