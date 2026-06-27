package pt.isel.services

import android.app.Service
import android.content.Intent
import android.icu.util.Calendar
import android.location.Location
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import pt.isel.OTPCDApplication
import pt.isel.api.BackendApi
import pt.isel.api.TelemetryRequest
import pt.isel.datascan.viewmodel.state.DEFAULT_INTERVAL
import pt.isel.datascan.viewmodel.state.DEFAULT_SUBJ_RATING
import pt.isel.datascan.viewmodel.state.DEFAULT_TIMEOUT
import pt.isel.datascan.viewmodel.state.IS_TEST_TRIP
import pt.isel.datascan.viewmodel.state.NOTIFICATION_REMINDER_INTERVAL
import pt.isel.helpers.NotificationHelper
import pt.isel.settings.domain.repository.SettingsRepository
import weka.classifiers.Classifier
import weka.core.DenseInstance
import weka.core.Instances
import weka.core.SerializationHelper
import java.io.StringReader
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class RideService : Service() {
    private lateinit var locationService: LocationService
    private lateinit var bluetoothService: BluetoothService
    private lateinit var wifiService: WifiService
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var networkService: NetworkService
    private lateinit var cellularService: CellularService
    private lateinit var settingsRepository: SettingsRepository

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private var currentTimeout = DEFAULT_TIMEOUT
    private var currentScanInterval = DEFAULT_INTERVAL
    private var currentNotifInterval = NOTIFICATION_REMINDER_INTERVAL
    private var isTestTrip = IS_TEST_TRIP

    private lateinit var classifier: Classifier
    private lateinit var dataset: Instances

    companion object {
        val secondsRemaining = MutableStateFlow(DEFAULT_TIMEOUT)
        val isServiceRunning = MutableStateFlow(false)
        val isPaused = MutableStateFlow(false)
        val currentLocation = MutableStateFlow<Location?>(null)
        val currentBluetoothCount = MutableStateFlow(0)
        val currentWifiCount = MutableStateFlow(0)
        val finishedTripId = MutableStateFlow<String?>(null)
        var currentTripId: String? = null
        val currentPrediction = MutableStateFlow<String?>(null)
    }

    override fun onCreate() {
        super.onCreate()

        initMachineLearningModel()
        locationService = LocationService(this)
        serviceScope.launch {
            locationService.currentLocation.collect { location ->
                currentLocation.value = location
            }
        }
        bluetoothService = BluetoothService(this)
        serviceScope.launch {
            bluetoothService.deviceCount.collect { count ->
                currentBluetoothCount.value = count
            }
        }
        wifiService = WifiService(this)
        serviceScope.launch {
            wifiService.wifiCount.collect { count ->
                currentWifiCount.value = count
            }
        }
        notificationHelper = NotificationHelper(this)
        cellularService = CellularService(this)

        networkService = NetworkService()

        settingsRepository = (application as OTPCDApplication).settingsRepository
    }

    private fun initMachineLearningModel() {
        try {
            val modelStream = assets.open("RandomForest.model")
            classifier = SerializationHelper.read(modelStream) as Classifier

            val arffData = """
                @RELATION subjective_occupancy_prediction

                @ATTRIBUTE transportType {METRO,TRAIN}
                @ATTRIBUTE timestamp NUMERIC
                @ATTRIBUTE dayOfWeek {1,2,3,4,5,6,7}
                @ATTRIBUTE hour NUMERIC
                @ATTRIBUTE latitude NUMERIC
                @ATTRIBUTE longitude NUMERIC
                @ATTRIBUTE bluetoothCount NUMERIC
                @ATTRIBUTE bt_signal_1 NUMERIC
                @ATTRIBUTE bt_signal_2 NUMERIC
                @ATTRIBUTE bt_signal_3 NUMERIC
                @ATTRIBUTE bt_signal_4 NUMERIC
                @ATTRIBUTE bt_signal_5 NUMERIC
                @ATTRIBUTE wifiCount NUMERIC
                @ATTRIBUTE wf_signal_1 NUMERIC
                @ATTRIBUTE wf_signal_2 NUMERIC
                @ATTRIBUTE wf_signal_3 NUMERIC
                @ATTRIBUTE wf_signal_4 NUMERIC
                @ATTRIBUTE wf_signal_5 NUMERIC
                @ATTRIBUTE latencyAvg NUMERIC
                @ATTRIBUTE latencyStdDev NUMERIC
                @ATTRIBUTE packetLoss NUMERIC
                @ATTRIBUTE rsrp NUMERIC
                @ATTRIBUTE rssnr NUMERIC
                @ATTRIBUTE rsrq NUMERIC
                @ATTRIBUTE subjectiveRating {1,2,3,4,5}

                @DATA
                METRO,1776776548770,2,14,38.7,-9.1,192,-55,-55,-55,-56,-58,2,-38,-84,-40,-40,-40,45.8,8.9,0.0,-96,25,-9,3
            """.trimIndent()

            dataset = Instances(StringReader(arffData))
            dataset.setClassIndex(dataset.numAttributes() - 1)
            Log.d("RideService", "ML Model Loaded Successfully!")
        } catch (e: Exception) {
            Log.e("RideService", "Error loading ML Model from assets", e)
        }
    }

    private var currentRating = DEFAULT_SUBJ_RATING

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "DELETE_TRIP", "INVALIDATE_TRIP" -> {
                stopSelf()
                return START_NOT_STICKY
            }
            "UPDATE_RATING" -> {
                currentRating = intent.getIntExtra("NEW_RATING", DEFAULT_SUBJ_RATING)
            }
            "PAUSE" -> {
                isPaused.value = true
                locationService.stopLocationUpdates()
                bluetoothService.stopScan()
                wifiService.stopScan()
            }
            "RESUME" -> {
                isPaused.value = false
                locationService.startLocationUpdates()
                bluetoothService.startScan()
                wifiService.startScan()
            }
            else -> {
                currentTimeout = intent?.getIntExtra("TIMEOUT", DEFAULT_TIMEOUT) ?: DEFAULT_TIMEOUT
                currentScanInterval = intent?.getIntExtra("INTERVAL", DEFAULT_INTERVAL) ?: DEFAULT_INTERVAL
                currentNotifInterval = intent?.getIntExtra("NOTIF_INTERVAL", NOTIFICATION_REMINDER_INTERVAL) ?: NOTIFICATION_REMINDER_INTERVAL
                isTestTrip = intent?.getBooleanExtra("IS_TEST", IS_TEST_TRIP) ?: IS_TEST_TRIP

                val tripId = intent?.getStringExtra("TRIP_ID") ?: "unknown_${System.currentTimeMillis()}"
                currentTripId = tripId
                val transportType = intent?.getStringExtra("TRANSPORT_TYPE") ?: "METRO"
                currentRating = intent?.getIntExtra("RATING", DEFAULT_SUBJ_RATING) ?: DEFAULT_SUBJ_RATING

                secondsRemaining.value = currentTimeout

                startForeground(1, notificationHelper.createTimerNotification(currentTimeout))

                if(!isPaused.value) {
                    locationService.startLocationUpdates()
                    bluetoothService.startScan()
                    wifiService.startScan()
                }

                startRideTicker(tripId)
            }
        }
        return START_NOT_STICKY
    }

    private fun startRideTicker(tripId: String) {
        isServiceRunning.value = true
        isPaused.value = false
        serviceScope.launch {
            var seconds = currentTimeout

            while (seconds >= 0) {
                if (isPaused.value) {
                    delay(500.milliseconds)
                    continue
                }

                secondsRemaining.value = seconds
                notificationHelper.updateTimerNotification(seconds)

                val elapsedTime = currentTimeout - seconds

                if (elapsedTime % currentScanInterval == 0 && seconds != currentTimeout) {
                    performDataScanAndPredict()
                }

                if (elapsedTime % currentNotifInterval == 0 && seconds != currentTimeout) {
                    notificationHelper.sendRatingReminder()
                }

                delay(1.seconds)
                seconds--
            }
            notificationHelper.sendTripFinishedNotification()

            stopSelf()
        }
    }

    private fun performDataScanAndPredict() {
        serviceScope.launch {
            Log.d("RideService", "Performing data scan for ML Prediction...")

            val location = locationService.currentLocation.value
            val bluetoothCount = bluetoothService.deviceCount.value
            val signalIntensitiesBT = bluetoothService.strongestSignals.value
            val signalIntensitiesWF = wifiService.strongestSignals.value
            val wifiCount = wifiService.wifiCount.value
            val cellularMetrics = cellularService.getCurrentMetrics()

            bluetoothService.clearScan()
            bluetoothService.startScan()
            wifiService.clearScan()
            wifiService.requestNewScan()

            val networkMetricsDeferred = async { networkService.measureNetworkMetrics() }
            val networkMetrics = networkMetricsDeferred.await()

            val calendar = Calendar.getInstance()
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK).toDouble()
            val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY).toDouble()
            val timestamp = System.currentTimeMillis().toDouble()

            val bt1 = signalIntensitiesBT.getOrNull(0)?.toDouble() ?: Double.NaN
            val bt2 = signalIntensitiesBT.getOrNull(1)?.toDouble() ?: Double.NaN
            val bt3 = signalIntensitiesBT.getOrNull(2)?.toDouble() ?: Double.NaN
            val bt4 = signalIntensitiesBT.getOrNull(3)?.toDouble() ?: Double.NaN
            val bt5 = signalIntensitiesBT.getOrNull(4)?.toDouble() ?: Double.NaN

            val wf1 = signalIntensitiesWF.getOrNull(0)?.toDouble() ?: Double.NaN
            val wf2 = signalIntensitiesWF.getOrNull(1)?.toDouble() ?: Double.NaN
            val wf3 = signalIntensitiesWF.getOrNull(2)?.toDouble() ?: Double.NaN
            val wf4 = signalIntensitiesWF.getOrNull(3)?.toDouble() ?: Double.NaN
            val wf5 = signalIntensitiesWF.getOrNull(4)?.toDouble() ?: Double.NaN

            val predictedRating = predictOccupancy(
                transportType = "METRO",
                timestamp = timestamp,
                dayOfWeek = dayOfWeek,
                hour = hourOfDay,
                latitude = location?.latitude ?: Double.NaN,
                longitude = location?.longitude ?: Double.NaN,
                bluetoothCount = bluetoothCount.toDouble(),
                btSignal1 = bt1,
                btSignal2 = bt2,
                btSignal3 = bt3,
                btSignal4 = bt4,
                btSignal5 = bt5,
                wifiCount = wifiCount.toDouble(),
                wfSignal1 = wf1,
                wfSignal2 = wf2,
                wfSignal3 = wf3,
                wfSignal4 = wf4,
                wfSignal5 = wf5,
                latencyAvg = networkMetrics.latencyAvg,
                latencyStdDev = networkMetrics.latencyStdDev,
                packetLoss = networkMetrics.packetLoss,
                rsrp = cellularMetrics.rsrp?.toDouble() ?: Double.NaN,
                rssnr = cellularMetrics.rssnr?.toDouble() ?: Double.NaN,
                rsrq = cellularMetrics.rsrq?.toDouble() ?: Double.NaN
            )

            currentPrediction.value = predictedRating
            Log.d("RideService", " LOCAL ML PREDICTION: Occupancy Level $predictedRating ")

            val telemetryRequest = TelemetryRequest(
                timestamp = System.currentTimeMillis(),
                latitude = location?.latitude ?: 0.0,
                longitude = location?.longitude ?: 0.0,
                bluetoothCount = bluetoothCount,
                bluetoothSignals = signalIntensitiesBT,
                wifiCount = wifiCount,
                wifiSignals = signalIntensitiesWF,
                rsrp = cellularMetrics.rsrp ?: 0,
                rssnr = cellularMetrics.rssnr ?: 0,
                rsrq = cellularMetrics.rsrq ?: 0,
                latencyAvg = networkMetrics.latencyAvg,
                latencyStdDev = networkMetrics.latencyStdDev,
                packetLoss = networkMetrics.packetLoss,
            )
            val response = BackendApi.sendTelemetry(telemetryRequest)
            if (response != null) {
                Log.d("RideService", "Backend response: station=${response.stationName}, occupancy=${response.occupancyLevel}")
            }
        }
    }

    private fun predictOccupancy(
        transportType: String, timestamp: Double, dayOfWeek: Double, hour: Double,
        latitude: Double, longitude: Double, bluetoothCount: Double,
        btSignal1: Double, btSignal2: Double, btSignal3: Double, btSignal4: Double, btSignal5: Double,
        wifiCount: Double,
        wfSignal1: Double, wfSignal2: Double, wfSignal3: Double, wfSignal4: Double, wfSignal5: Double,
        latencyAvg: Double, latencyStdDev: Double, packetLoss: Double,
        rsrp: Double, rssnr: Double, rsrq: Double
    ): String {

        if (!::classifier.isInitialized || !::dataset.isInitialized) {
            return "Model Error"
        }

        val newInstance = DenseInstance(dataset.numAttributes())
        newInstance.setDataset(dataset)

        try { newInstance.setValue(dataset.attribute("transportType"), transportType) } catch (e:Exception) {}

        newInstance.setValue(dataset.attribute("timestamp"), timestamp)
        newInstance.setValue(dataset.attribute("dayOfWeek"), dayOfWeek)
        newInstance.setValue(dataset.attribute("hour"), hour)
        newInstance.setValue(dataset.attribute("latitude"), latitude)
        newInstance.setValue(dataset.attribute("longitude"), longitude)

        newInstance.setValue(dataset.attribute("bluetoothCount"), bluetoothCount)
        newInstance.setValue(dataset.attribute("bt_signal_1"), btSignal1)
        newInstance.setValue(dataset.attribute("bt_signal_2"), btSignal2)
        newInstance.setValue(dataset.attribute("bt_signal_3"), btSignal3)
        newInstance.setValue(dataset.attribute("bt_signal_4"), btSignal4)
        newInstance.setValue(dataset.attribute("bt_signal_5"), btSignal5)

        newInstance.setValue(dataset.attribute("wifiCount"), wifiCount)
        newInstance.setValue(dataset.attribute("wf_signal_1"), wfSignal1)
        newInstance.setValue(dataset.attribute("wf_signal_2"), wfSignal2)
        newInstance.setValue(dataset.attribute("wf_signal_3"), wfSignal3)
        newInstance.setValue(dataset.attribute("wf_signal_4"), wfSignal4)
        newInstance.setValue(dataset.attribute("wf_signal_5"), wfSignal5)

        newInstance.setValue(dataset.attribute("latencyAvg"), latencyAvg)
        newInstance.setValue(dataset.attribute("latencyStdDev"), latencyStdDev)
        newInstance.setValue(dataset.attribute("packetLoss"), packetLoss)
        newInstance.setValue(dataset.attribute("rsrp"), rsrp)
        newInstance.setValue(dataset.attribute("rssnr"), rssnr)
        newInstance.setValue(dataset.attribute("rsrq"), rsrq)

        val predictionIndex = classifier.classifyInstance(newInstance)
        return dataset.classAttribute().value(predictionIndex.toInt())
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()

        if (currentTripId != null) {
            locationService.stopLocationUpdates()
            bluetoothService.stopScan()
            wifiService.stopScan()

            finishedTripId.value = currentTripId
            currentTripId = null
        }

        isServiceRunning.value = false
        isPaused.value = false
    }

    override fun onBind(intent: Intent?): IBinder? = null
}