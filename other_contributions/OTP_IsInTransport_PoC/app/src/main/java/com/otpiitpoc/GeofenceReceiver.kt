package com.otpiitpoc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.GeofencingEvent

class GeofenceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        //Com isto, conseguimos qual estação o utilizador entrou (GeofencingEvent.getTriggeringGeofences())
        if (geofencingEvent == null || geofencingEvent.hasError()) {
            Log.e("GeofenceReceiver", "Error in geofence event")
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition
        DetectorViewModel.getInstance().onGeofenceTransition(geofenceTransition)
    }
}
