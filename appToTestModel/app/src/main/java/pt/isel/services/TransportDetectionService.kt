package pt.isel.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import pt.isel.api.BackendApi
import pt.isel.api.TelemetryRequest
import pt.isel.helpers.NotificationHelper
import pt.isel.transportdetector.ActivityRecognitionManager
import pt.isel.transportdetector.GeofenceManager
import pt.isel.transportdetector.TransportDetector
import pt.isel.transportdetector.TransportState
import kotlin.time.Duration.Companion.seconds

class TransportDetectionService : Service() {

    private lateinit var locationService: LocationService
    private lateinit var bluetoothService: BluetoothService
    private lateinit var wifiService: WifiService
    private lateinit var cellularService: CellularService
    private lateinit var networkService: NetworkService
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var geofenceManager: GeofenceManager
    private lateinit var activityRecognitionManager: ActivityRecognitionManager

    private var telemetryJob: Job? = null
    private var isCollecting = false

    override fun onCreate() {
        super.onCreate()
        locationService = LocationService(this)
        bluetoothService = BluetoothService(this)
        wifiService = WifiService(this)
        cellularService = CellularService(this)
        networkService = NetworkService()
        notificationHelper = NotificationHelper(this)
        geofenceManager = GeofenceManager(this)
        activityRecognitionManager = ActivityRecognitionManager(this)

        serviceScope.launch {
            TransportDetector.getInstance().state.collect { state ->
                Log.d("TransportDetection", "State changed to: $state")
                when (state) {
                    TransportState.IN_TRANSIT -> startTelemetryCollection()
                    TransportState.DESTINATION_REACHED,
                    TransportState.EXTERIOR -> stopTelemetryCollection()
                    TransportState.AT_STATION -> { }
                }
            }
        }

        Log.d("TransportDetection", "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(4, notificationHelper.createMonitoringNotification())

        locationService.startLocationUpdates()
        try {
            geofenceManager.addGeofences()
        } catch (e: SecurityException) {
            Log.e("TransportDetection", "Location permission denied for geofences", e)
        }
        activityRecognitionManager.requestActivityUpdates()

        serviceScope.launch {
            try {
                val loc = kotlinx.coroutines.withTimeout(10000L) {
                    locationService.currentLocation.filterNotNull().first()
                }
                val nearest = geofenceManager.findNearestStation(loc.latitude, loc.longitude)
                if (nearest != null) {
                    Log.d("TransportDetection", "Already near station: ${nearest.name}, triggering enter")
                    TransportDetector.getInstance().onGeofenceTransition(1)
                } else {
                    Log.d("TransportDetection", "Not near any station (lat=${loc.latitude}, lng=${loc.longitude})")
                }
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                Log.w("TransportDetection", "Proximity check timed out")
            }
        }

        return START_STICKY
    }

    private val serviceScope = kotlinx.coroutines.CoroutineScope(Job())

    private fun startTelemetryCollection() {
        if (isCollecting) return
        isCollecting = true
        Log.d("TransportDetection", "Starting telemetry collection")

        serviceScope.launch {
            BackendApi.authenticate()
        }

        bluetoothService.startScan()
        wifiService.startScan()

        telemetryJob = serviceScope.launch {
            delay(10.seconds)

            while (isCollecting) {
                val state = TransportDetector.getInstance().state.value
                if (state != TransportState.IN_TRANSIT) {
                    stopTelemetryCollection()
                    break
                }

                performTelemetryScan()
                delay(15.seconds)
            }
        }
    }

    private fun stopTelemetryCollection() {
        if (!isCollecting) return
        isCollecting = false
        telemetryJob?.cancel()
        telemetryJob = null
        bluetoothService.stopScan()
        wifiService.stopScan()
        Log.d("TransportDetection", "Stopped telemetry collection")
    }

    private suspend fun performTelemetryScan() {
        try {
            val location = locationService.currentLocation.value
            val bluetoothCount = bluetoothService.deviceCount.value
            val signalIntensitiesBT = bluetoothService.strongestSignals.value
            val wifiCount = wifiService.wifiCount.value
            val signalIntensitiesWF = wifiService.strongestSignals.value
            val cellularMetrics = cellularService.getCurrentMetrics()

            bluetoothService.clearScan()
            bluetoothService.startScan()
            wifiService.clearScan()
            wifiService.requestNewScan()

            val networkMetrics = networkService.measureNetworkMetrics()

            val request = TelemetryRequest(
                timestamp = System.currentTimeMillis(),
                latitude = location?.latitude ?: 0.0,
                longitude = location?.longitude ?: 0.0,
                bluetoothCount = bluetoothCount,
                bluetoothSignals = (signalIntensitiesBT + List(5) { 0 }).take(5),
                wifiCount = wifiCount,
                wifiSignals = (signalIntensitiesWF + List(5) { 0 }).take(5),
                rsrp = cellularMetrics.rsrp ?: 0,
                rssnr = cellularMetrics.rssnr ?: 0,
                rsrq = cellularMetrics.rsrq ?: 0,
                latencyAvg = networkMetrics.latencyAvg,
                latencyStdDev = networkMetrics.latencyStdDev,
                packetLoss = networkMetrics.packetLoss,
            )

            val response = BackendApi.sendTelemetry(request)
            if (response != null) {
                Log.d("TransportDetection", "Telemetry sent: station=${response.stationName}, occupancy=${response.occupancyLevel}")
            }
        } catch (e: Exception) {
            Log.e("TransportDetection", "Telemetry scan error: ${e.message}", e)
        }
    }

    override fun onDestroy() {
        stopTelemetryCollection()
        locationService.stopLocationUpdates()
        bluetoothService.stopScan()
        wifiService.stopScan()
        try {
            com.google.android.gms.location.ActivityRecognition.getClient(this).removeActivityUpdates(
                activityRecognitionManager.getPendingIntent()
            )
        } catch (_: SecurityException) { }
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
