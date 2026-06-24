package pt.isel.otp.service.impl

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service
import pt.isel.otp.domain.dto.request.TelemetryRequest
import pt.isel.otp.domain.entity.Station
import pt.isel.otp.service.InferenceService
import weka.classifiers.Classifier
import weka.core.Attribute
import weka.core.DenseInstance
import weka.core.Instances
import weka.core.SerializationHelper
import java.util.Calendar

@Service
class InferenceServiceImpl(
    private val resourceLoader: ResourceLoader,
) : InferenceService {
    private val log = LoggerFactory.getLogger(javaClass)

    private var completeClassifier: Classifier? = null
    private var limitedClassifier: Classifier? = null
    private var completeHeader: Instances? = null
    private var limitedHeader: Instances? = null

    @PostConstruct
    fun init() {
        loadCompleteModel()
        loadLimitedModel()
    }

    private fun loadCompleteModel() {
        try {
            val resource = resourceLoader.getResource("classpath:models/complete.model")
            if (!resource.exists()) {
                log.warn("COMPLETE model not found at models/complete.model")
                return
            }
            completeClassifier = SerializationHelper.read(resource.inputStream) as Classifier
            completeHeader = createCompleteHeader()
            log.info("COMPLETE model loaded successfully")
        } catch (e: Exception) {
            log.error("Failed to load COMPLETE model", e)
        }
    }

    private fun loadLimitedModel() {
        try {
            val resource = resourceLoader.getResource("classpath:models/limited.model")
            if (!resource.exists()) {
                log.warn("LIMITED model not found at models/limited.model")
                return
            }
            limitedClassifier = SerializationHelper.read(resource.inputStream) as Classifier
            limitedHeader = createLimitedHeader()
            log.info("LIMITED model loaded successfully")
        } catch (e: Exception) {
            log.error("Failed to load LIMITED model", e)
        }
    }

    private fun createCompleteHeader(): Instances {
        val transportTypeValues = ArrayList<String>().apply {
            add("METRO")
            add("TRAIN")
        }
        val dayOfWeekValues = ArrayList<String>().apply {
            for (i in 1..7) add(i.toString())
        }
        val ratingValues = ArrayList<String>().apply {
            for (i in 1..5) add(i.toString())
        }

        val attrs = ArrayList<Attribute>().apply {
            add(Attribute("transportType", transportTypeValues))
            add(Attribute("timestamp"))
            add(Attribute("dayOfWeek", dayOfWeekValues))
            add(Attribute("hour"))
            add(Attribute("latitude"))
            add(Attribute("longitude"))
            add(Attribute("bluetoothCount"))
            add(Attribute("bt_signal_1"))
            add(Attribute("bt_signal_2"))
            add(Attribute("bt_signal_3"))
            add(Attribute("bt_signal_4"))
            add(Attribute("bt_signal_5"))
            add(Attribute("wifiCount"))
            add(Attribute("wf_signal_1"))
            add(Attribute("wf_signal_2"))
            add(Attribute("wf_signal_3"))
            add(Attribute("wf_signal_4"))
            add(Attribute("wf_signal_5"))
            add(Attribute("latencyAvg"))
            add(Attribute("latencyStdDev"))
            add(Attribute("packetLoss"))
            add(Attribute("rsrp"))
            add(Attribute("rssnr"))
            add(Attribute("rsrq"))
            add(Attribute("subjectiveRating", ratingValues))
        }

        val header = Instances("subjective_occupancy_prediction", attrs, 0)
        header.setClassIndex(header.numAttributes() - 1)
        return header
    }

    private fun createLimitedHeader(): Instances {
        val ratingValues = ArrayList<String>().apply {
            for (i in 1..5) add(i.toString())
        }

        val attrs = ArrayList<Attribute>().apply {
            add(Attribute("timestamp"))
            add(Attribute("latitude"))
            add(Attribute("longitude"))
            add(Attribute("subjectiveRating", ratingValues))
        }

        val header = Instances("limited_occupancy_prediction", attrs, 0)
        header.setClassIndex(header.numAttributes() - 1)
        return header
    }

    override fun predictComplete(request: TelemetryRequest, station: Station): Int {
        val classifier = completeClassifier ?: return fallbackPrediction()
        val header = completeHeader ?: return fallbackPrediction()

        val cal = Calendar.getInstance().apply {
            timeInMillis = request.timestamp
        }
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK).toDouble()
        val hour = cal.get(Calendar.HOUR_OF_DAY).toDouble()

        val instance = DenseInstance(header.numAttributes())
        instance.setDataset(header)
        instance.setValue(header.attribute("transportType"), station.transportType.name)
        instance.setValue(header.attribute("timestamp"), request.timestamp.toDouble())
        instance.setValue(header.attribute("dayOfWeek"), dayOfWeek)
        instance.setValue(header.attribute("hour"), hour)
        instance.setValue(header.attribute("latitude"), request.latitude)
        instance.setValue(header.attribute("longitude"), request.longitude)
        instance.setValue(header.attribute("bluetoothCount"), request.bluetoothCount.toDouble())
        instance.setValue(header.attribute("bt_signal_1"), request.bluetoothSignals[0].toDouble())
        instance.setValue(header.attribute("bt_signal_2"), request.bluetoothSignals[1].toDouble())
        instance.setValue(header.attribute("bt_signal_3"), request.bluetoothSignals[2].toDouble())
        instance.setValue(header.attribute("bt_signal_4"), request.bluetoothSignals[3].toDouble())
        instance.setValue(header.attribute("bt_signal_5"), request.bluetoothSignals[4].toDouble())
        instance.setValue(header.attribute("wifiCount"), request.wifiCount.toDouble())
        instance.setValue(header.attribute("wf_signal_1"), request.wifiSignals[0].toDouble())
        instance.setValue(header.attribute("wf_signal_2"), request.wifiSignals[1].toDouble())
        instance.setValue(header.attribute("wf_signal_3"), request.wifiSignals[2].toDouble())
        instance.setValue(header.attribute("wf_signal_4"), request.wifiSignals[3].toDouble())
        instance.setValue(header.attribute("wf_signal_5"), request.wifiSignals[4].toDouble())
        instance.setValue(header.attribute("latencyAvg"), request.latencyAvg)
        instance.setValue(header.attribute("latencyStdDev"), request.latencyStdDev)
        instance.setValue(header.attribute("packetLoss"), request.packetLoss)
        instance.setValue(header.attribute("rsrp"), request.rsrp.toDouble())
        instance.setValue(header.attribute("rssnr"), request.rssnr.toDouble())
        instance.setValue(header.attribute("rsrq"), request.rsrq.toDouble())

        val predictionIdx = classifier.classifyInstance(instance).toInt()
        return header.classAttribute().value(predictionIdx).toInt()
    }

    override fun predictLimited(station: Station, timestamp: Long): Int {
        val classifier = limitedClassifier ?: return fallbackPrediction()
        val header = limitedHeader ?: return fallbackPrediction()

        val instance = DenseInstance(header.numAttributes())
        instance.setDataset(header)
        instance.setValue(header.attribute("timestamp"), timestamp.toDouble())
        instance.setValue(header.attribute("latitude"), station.latitude)
        instance.setValue(header.attribute("longitude"), station.longitude)

        val predictionIdx = classifier.classifyInstance(instance).toInt()
        return header.classAttribute().value(predictionIdx).toInt()
    }

    private fun fallbackPrediction(): Int = 3
}
