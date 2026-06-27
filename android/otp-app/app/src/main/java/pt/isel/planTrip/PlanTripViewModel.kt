package pt.isel.planTrip

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PlanTripViewModel : ViewModel() {

    private val _selectedType = MutableStateFlow("")
    val selectedType: StateFlow<String> = _selectedType.asStateFlow()

    private val _selectedStation = MutableStateFlow("")
    val selectedStation: StateFlow<String> = _selectedStation.asStateFlow()

    private val _selectedHour = MutableStateFlow("")
    val selectedHour: StateFlow<String> = _selectedHour.asStateFlow()

    private val _selectedDate = MutableStateFlow("")
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    fun updateType(type: String) {
        _selectedType.value = type
        _selectedStation.value = ""
    }

    fun updateStation(station: String) {
        _selectedStation.value = station
    }

    fun updateHour(hour: String) {
        _selectedHour.value = hour
    }

    fun updateDate(date: String) {
        _selectedDate.value = date
    }

    fun checkOccupancyPrediction() {
        val type = _selectedType.value
        val station = _selectedStation.value
        val hour = _selectedHour.value
        val date = _selectedDate.value

        println("A pedir previsão para: $type na estação $station no dia $date às $hour")
    }
}