package com.otpiitpoc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.DetectedActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetectorViewModel : ViewModel() {

    private val _state = MutableStateFlow(TransportState.EXTERIOR)
    val state: StateFlow<TransportState> = _state.asStateFlow()

    private var lastDetectedActivity = DetectedActivity.STILL
    private var isInsideGeofence = false

    fun onGeofenceTransition(transitionType: Int) {
        if (transitionType == 1) {
            isInsideGeofence = true
            evaluateState()
        } else {
            isInsideGeofence = false
        }
    }

    fun onActivityTransition(activityType: Int) {
        lastDetectedActivity = activityType
        evaluateState()
    }

    private fun evaluateState() {
        val currentState = _state.value
        val isPedestrian = lastDetectedActivity == DetectedActivity.WALKING || 
                           lastDetectedActivity == DetectedActivity.STILL

        when (currentState) {
            TransportState.EXTERIOR -> {
                if (isInsideGeofence && isPedestrian) {
                    _state.value = TransportState.AT_STATION
                }
            }
            TransportState.AT_STATION -> {
                if (lastDetectedActivity == DetectedActivity.IN_VEHICLE) {
                    _state.value = TransportState.IN_TRANSIT
                }
            }
            TransportState.IN_TRANSIT -> {
                if (isInsideGeofence && isPedestrian) {
                    _state.value = TransportState.DESTINATION_REACHED
                    resetAfterDelay()
                }
            }
            TransportState.DESTINATION_REACHED -> {
            }
        }
    }

    private fun resetAfterDelay() {
        viewModelScope.launch {
            delay(5000)
            _state.value = TransportState.EXTERIOR
            isInsideGeofence = false
        }
    }

    companion object {
        private var instance: DetectorViewModel? = null
        
        fun getInstance(): DetectorViewModel {
            if (instance == null) {
                instance = DetectorViewModel()
            }
            return instance!!
        }
    }
}
