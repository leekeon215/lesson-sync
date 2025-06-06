package com.lessonsync.app.noitification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.lessonsync.app.R

class LessonNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val scoreId = intent.getStringExtra("scoreId") ?: ""

        val channelId = "lesson_summary_channel"
        val notificationId = 1002

        if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val channel = NotificationChannel(
            channelId,
            "Lesson Summary Notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "알림으로 레슨 요약이 완료되었음을 알려줍니다."
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

        try {
            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("레슨 요약 완료")
                .setContentText("악보 #$scoreId 레슨 요약이 준비되었습니다.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
            Log.d("LessonNotification", "Notification triggered!")
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}
