package com.otpiitpoc

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionRequest
import com.google.android.gms.location.DetectedActivity

class ActivityRecognitionManager(private val context: Context) {

    private val activityRecognitionClient = ActivityRecognition.getClient(context)

    private val activityPendingIntent: PendingIntent by lazy {
        val intent = Intent(context, ActivityTransitionReceiver::class.java)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        PendingIntent.getBroadcast(context, 1, intent, flags)
    }

    @SuppressLint("MissingPermission")
    fun requestActivityUpdates() {
        val transitions = mutableListOf<ActivityTransition>()

        val activities = listOf(
            DetectedActivity.WALKING,
            DetectedActivity.STILL,
            DetectedActivity.IN_VEHICLE
        )

        for (activity in activities) {
            transitions.add(
                ActivityTransition.Builder()
                    .setActivityType(activity)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                    .build()
            )
            transitions.add(
                ActivityTransition.Builder()
                    .setActivityType(activity)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                    .build()
            )
        }

        val request = ActivityTransitionRequest(transitions)
        activityRecognitionClient.requestActivityTransitionUpdates(request, activityPendingIntent)
    }

    fun removeActivityUpdates() {
        activityRecognitionClient.removeActivityTransitionUpdates(activityPendingIntent)
    }
}
