package com.otpiitpoc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.ActivityRecognitionResult

class ActivityRecognitionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (ActivityRecognitionResult.hasResult(intent)) {
            val result = ActivityRecognitionResult.extractResult(intent)
            result?.mostProbableActivity?.let { activity ->
                DetectorViewModel.getInstance().onActivityUpdate(activity.type, activity.confidence)
            }
        }
    }
}
