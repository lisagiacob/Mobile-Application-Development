package com.example.lab2

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Notifiche Viaggi"
        val descriptionText = "Canale per le notifiche di viaggio"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel("travel_notifications", name, importance).apply {
            description = descriptionText
        }

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

fun showSystemNotification(context: Context, notification: NotificationModel) {
    Log.d("SYSTEM_NOTIFICATION", "Showing system notification: ${notification.message}")

    val builder = NotificationCompat.Builder(context, "travel_notifications")
        .setSmallIcon(R.drawable.ic_launcher_foreground) // usa un'icona adatta!
        .setContentTitle("Nuova notifica")
        .setContentText(notification.message)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)

    val notificationManager = NotificationManagerCompat.from(context)

    notificationManager.notify(notification.id.hashCode(), builder.build())
}