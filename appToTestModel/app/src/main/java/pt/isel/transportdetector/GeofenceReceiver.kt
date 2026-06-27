package pt.isel.transportdetector

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.GeofencingEvent

class GeofenceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent == null || geofencingEvent.hasError()) {
            Log.e("GeofenceReceiver", "Error in geofence event")
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition
        val triggeringGeofences = geofencingEvent.triggeringGeofences
        val ids = triggeringGeofences?.map { it.requestId } ?: emptyList()
        val transitionName = when (geofenceTransition) {
            1 -> "ENTER"
            2 -> "EXIT"
            else -> "UNKNOWN($geofenceTransition)"
        }
        Log.d("GeofenceReceiver", "Geofence $transitionName: $ids")
        TransportDetector.getInstance().onGeofenceTransition(geofenceTransition)
    }
}
