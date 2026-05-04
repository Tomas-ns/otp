package com.otpiitpoc

import android.util.Log
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

    private val _currentActivity = MutableStateFlow("STILL (0%)")
    val currentActivity: StateFlow<String> = _currentActivity.asStateFlow()

    private var lastDetectedActivity = DetectedActivity.STILL
    private var lastConfidence = 0
    private var isInsideGeofence = false

    private val activityBuffer = IntArray(5) { -1 }
    private var bufferIndex = 0

    fun onGeofenceTransition(transitionType: Int) {
        if (transitionType == 1) {
            isInsideGeofence = true
            evaluateState()
        } else if (transitionType == 2) {
            isInsideGeofence = false
            evaluateState()
        }
    }

    fun onActivityUpdate(activityType: Int, confidence: Int) {
        val previousActivity = lastDetectedActivity
        lastDetectedActivity = activityType
        lastConfidence = confidence

        activityBuffer[bufferIndex] = activityType
        bufferIndex = (bufferIndex + 1) % 5

        val activityName = getActivityName(activityType)
        _currentActivity.value = "$activityName ($confidence%)"

        Log.d("DetectorViewModel", "Activity: $activityName ($confidence%)")

        if (_state.value == TransportState.AT_STATION &&
            previousActivity == DetectedActivity.STILL &&
            activityType == DetectedActivity.UNKNOWN) {
            _state.value = TransportState.IN_TRANSIT
            Log.d("DetectorViewModel", "State changed to IN_TRANSIT (STILL -> UNKNOWN transition)")
        }

        evaluateState()
    }



    private fun evaluateState() {
        val currentState = _state.value
        val isPedestrian = lastDetectedActivity == DetectedActivity.WALKING ||
                lastDetectedActivity == DetectedActivity.STILL ||
                (lastDetectedActivity == DetectedActivity.ON_FOOT)

        when (currentState) {
            TransportState.EXTERIOR -> {
                if (isInsideGeofence && isPedestrian) {
                    _state.value = TransportState.AT_STATION
                }
            }
            TransportState.AT_STATION -> {
                if (lastDetectedActivity == DetectedActivity.IN_VEHICLE) {
                    _state.value = TransportState.IN_TRANSIT
                } else if (!isInsideGeofence && !isPedestrian) {
                    _state.value = TransportState.IN_TRANSIT
                } else if (activityBuffer.all { it == DetectedActivity.UNKNOWN }) {
                    _state.value = TransportState.IN_TRANSIT
                } else if (!isInsideGeofence && isPedestrian) {
                    _state.value = TransportState.EXTERIOR

                }
            }
            TransportState.IN_TRANSIT -> {
                if (isInsideGeofence && isPedestrian && lastConfidence > 50) {
                    _state.value = TransportState.DESTINATION_REACHED
                    resetAfterDelay()
                } else if (!isInsideGeofence && isPedestrian && lastConfidence > 70) {
                    _state.value = TransportState.DESTINATION_REACHED
                }
            }
            TransportState.DESTINATION_REACHED -> {
                resetAfterDelay()
            }
        }
    }

    private fun resetAfterDelay() {
        viewModelScope.launch {
            delay(10000)
            _state.value = TransportState.EXTERIOR
            activityBuffer.fill(-1)
        }
    }

    private fun getActivityName(type: Int): String {
        return when (type) {
            DetectedActivity.IN_VEHICLE -> "IN_VEHICLE"
            DetectedActivity.ON_BICYCLE -> "ON_BICYCLE"
            DetectedActivity.ON_FOOT -> "ON_FOOT"
            DetectedActivity.STILL -> "STILL"
            DetectedActivity.UNKNOWN -> "UNKNOWN"
            DetectedActivity.TILTING -> "TILTING"
            DetectedActivity.WALKING -> "WALKING"
            DetectedActivity.RUNNING -> "RUNNING"
            else -> "UNKNOWN ($type)"
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