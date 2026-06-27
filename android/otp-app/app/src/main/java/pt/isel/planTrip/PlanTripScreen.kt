package pt.isel.planTrip

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Commute
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pt.isel.R
import pt.isel.domain.metroStations
import pt.isel.domain.trainStations
//import pt.isel.map.trainStations
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanTripScreen(viewModel: PlanTripViewModel) {
    val selectedType by viewModel.selectedType.collectAsState()
    val selectedStation by viewModel.selectedStation.collectAsState()
    val selectedHour by viewModel.selectedHour.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    val typeMetro = stringResource(id = R.string.transport_type_metro)
    val typeTrain = stringResource(id = R.string.transport_type_train)

    var expandedType by remember { mutableStateOf(false) }
    val transportTypes = listOf(typeMetro, typeTrain)

    var expandedStation by remember { mutableStateOf(false) }

    val currentStations = when (selectedType) {
        typeMetro -> metroStations.map { it.name }.sorted()
        typeTrain -> trainStations.map { it.name }.sorted()
        else -> emptyList()
    }


    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(initialHour = 18, initialMinute = 0)

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfToday = calendar.timeInMillis

                calendar.add(Calendar.DAY_OF_YEAR, 7)
                val endOfNextWeek = calendar.timeInMillis

                return utcTimeMillis in startOfToday..endOfNextWeek
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.title_occupancy_prediction),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expandedType,
            onExpandedChange = { expandedType = it }
        ) {
            OutlinedTextField(
                value = selectedType,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(id = R.string.label_transport_type)) },
                leadingIcon = { Icon(Icons.Default.Commute, contentDescription = null) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expandedType,
                onDismissRequest = { expandedType = false }
            ) {
                transportTypes.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type) },
                        onClick = {
                            viewModel.updateType(type)
                            expandedType = false
                        }
                    )
                }
            }
        }

        val isStationEnabled = selectedType.isNotEmpty()

        Box(modifier = Modifier.fillMaxWidth()) {
            ExposedDropdownMenuBox(
                expanded = if (isStationEnabled) expandedStation else false,
                onExpandedChange = { if (isStationEnabled) expandedStation = it }
            ) {
                OutlinedTextField(
                    value = selectedStation,
                    onValueChange = {},
                    readOnly = true,
                    enabled = isStationEnabled,
                    label = {
                        Text(
                            if (isStationEnabled) stringResource(id = R.string.label_select_station)
                            else stringResource(id = R.string.label_choose_transport_first)
                        )
                    },
                    leadingIcon = { Icon(Icons.Default.Train, contentDescription = null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStation) },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                        disabledBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                        disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedStation,
                    onDismissRequest = { expandedStation = false },
                    modifier = Modifier.heightIn(max = 300.dp)
                ) {
                    currentStations.forEach { station ->
                        DropdownMenuItem(
                            text = { Text(station) },
                            onClick = {
                                viewModel.updateStation(station)
                                expandedStation = false
                            }
                        )
                    }
                }
            }
        }

        OutlinedTextField(
            value = if (selectedHour.isNotEmpty()) selectedHour else stringResource(id = R.string.time_placeholder),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(id = R.string.label_select_hour)) },
            leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showTimePicker = true },
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        if (showTimePicker) {
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        val minute = timePickerState.minute
                        val roundedMinute = ((minute / 15.0).roundToInt() * 15) % 60
                        val extraHour = if (roundedMinute == 0 && minute > 45) 1 else 0
                        val finalHour = (timePickerState.hour + extraHour) % 24

                        val formattedTime = String.format(
                            Locale.getDefault(),
                            "%02d:%02d",
                            finalHour,
                            roundedMinute
                        )

                        viewModel.updateHour(formattedTime)
                        showTimePicker = false
                    }) {
                        Text(stringResource(id = R.string.btn_ok))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) {
                        Text(stringResource(id = R.string.btn_cancel))
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                    ) {
                        TimePicker(state = timePickerState)
                    }
                }
            )
        }

        OutlinedTextField(
            value = selectedDate,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(id = R.string.label_select_day)) },
            leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        showDatePicker = false
                        datePickerState.selectedDateMillis?.let { millis ->
                            // Os formatos não vão para o XML
                            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                            calendar.timeInMillis = millis
                            viewModel.updateDate(formatter.format(calendar.time))
                        }
                    }) {
                        Text(stringResource(id = R.string.btn_ok))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text(stringResource(id = R.string.btn_cancel))
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        val occupancyResult by viewModel.occupancyResult.collectAsState()

        Button(
            onClick = {
                Log.d("API_DEBUG", "Prever ocpuacao")
                viewModel.checkOccupancyPrediction() },
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedType.isNotEmpty() && selectedStation.isNotEmpty() && selectedHour.isNotEmpty() && selectedDate.isNotEmpty()
        ) {
            Text(stringResource(id = R.string.btn_verify_occupancy))
        }

        occupancyResult?.let { result ->
            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = result,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}