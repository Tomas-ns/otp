package com.otpiitpoc

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.google.android.gms.location.ActivityRecognition

class ActivityRecognitionManager(private val context: Context) {

    private val activityRecognitionClient = ActivityRecognition.getClient(context)

    private val activityPendingIntent: PendingIntent by lazy {
        val intent = Intent(context, ActivityRecognitionReceiver::class.java)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        PendingIntent.getBroadcast(context, 1, intent, flags)
    }

    @SuppressLint("MissingPermission")
    fun requestActivityUpdates() {
        activityRecognitionClient.requestActivityUpdates(3000, activityPendingIntent)
    }

}
