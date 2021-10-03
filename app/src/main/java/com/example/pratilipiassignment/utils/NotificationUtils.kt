package com.example.pratilipiassignment.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.pratilipiassignment.MainActivity
import com.example.pratilipiassignment.R
import com.example.pratilipiassignment.model.Contact


fun NotificationManagerCompat.sendNotification(context: Context, matchingNumber: Contact) {
    Log.d("NotificationUtil", "sendNotification: number = $matchingNumber")

    if (Build.VERSION.SDK_INT >= 26) {
        val channel = NotificationChannel(
            "default", context.getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = "Rejected Call"
        createNotificationChannel(channel)
    }

    val notify: Notification = NotificationCompat.Builder(context, "default")
        .setContentTitle("Blocked call")
        .setContentText(matchingNumber.name ?: matchingNumber.number)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setCategory(NotificationCompat.CATEGORY_CALL)
        .setShowWhen(true)
        .setAutoCancel(true)
        .setColor(ContextCompat.getColor(context, R.color.lt_sunburst))
        .setContentIntent(
            PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        )
        .addPerson("tel:${matchingNumber.number}")
        .setGroup("rejected")
        .setChannelId("default")
        .setGroupSummary(true)
        .build()

    val tag = matchingNumber.number
    notify(tag, 1, notify)
}

fun NotificationManagerCompat.sendNotificationForPermissions(context: Context){

    if (Build.VERSION.SDK_INT >= 26) {
        val channel = NotificationChannel(
            "default", context.getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = "Default"
        createNotificationChannel(channel)
    }

    val notify: Notification = NotificationCompat.Builder(context, "default")
        .setContentTitle("Call blocking is disabled")
        .setContentText("Click here to give permissions")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setCategory(NotificationCompat.CATEGORY_CALL)
        .setShowWhen(true)
        .setAutoCancel(true)
        .setContentIntent(
            PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        )
        .setChannelId("default")
        .setGroupSummary(true)
        .build()

    notify("perms", 1, notify)

}


