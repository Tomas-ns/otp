package pt.isel.otp.service.impl

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service
import pt.isel.otp.domain.dto.request.TelemetryRequest
import pt.isel.otp.domain.entity.Station
import pt.isel.otp.service.InferenceService
import weka.classifiers.Classifier
import weka.classifiers.trees.RandomForest
import weka.classifiers.trees.RandomTree
import weka.core.Attribute
import weka.core.DenseInstance
import weka.core.Instance
import weka.core.Instances
import weka.core.SerializationHelper
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.util.UUID

@Service
class InferenceServiceImpl(
    private val resourceLoader: ResourceLoader,
) : InferenceService {
    private val log = LoggerFactory.getLogger(javaClass)

    private var completeClassifier: Classifier? = null
    private var limitedClassifier: Classifier? = null
    private var completeHeader: Instances? = null
    private var limitedHeader: Instances? = null

    companion object {
        private val LISBON_ZONE = ZoneId.of("Europe/Lisbon")
    }

    @PostConstruct
    fun init() {
        loadCompleteModel()
        loadLimitedModel()
    }

    private fun loadCompleteModel() {
        try {
            val resource = resourceLoader.getResource("classpath:models/complete.model")
            if (!resource.exists()) { log.warn("COMPLETE model not found"); return }
            val model = SerializationHelper.read(resource.inputStream) as Classifier
            completeClassifier = model
            completeHeader = extractInternalHeader(model)
            log.info("COMPLETE model loaded (${completeHeader?.numAttributes()} attrs)")
        } catch (e: Exception) {
            log.error("Failed to load COMPLETE model", e)
        }
    }

    private fun loadLimitedModel() {
        try {
            val resource = resourceLoader.getResource("classpath:models/limited.model")
            if (!resource.exists()) { log.warn("LIMITED model not found"); return }
            val model = SerializationHelper.read(resource.inputStream) as Classifier
            limitedClassifier = model
            limitedHeader = extractInternalHeader(model)
            log.info("LIMITED model loaded (${limitedHeader?.numAttributes()} attrs)")
        } catch (e: Exception) {
            log.error("Failed to load LIMITED model", e)
        }
    }

    private fun extractInternalHeader(model: Classifier): Instances {
        if (model is RandomForest) {
            var cls: Class<*> = model.javaClass
            while (cls != Any::class.java) {
                try {
                    val field = cls.getDeclaredField("m_Classifiers")
                    field.isAccessible = true
                    val classifiers = field.get(model) as Array<Classifier>
                    val firstTree = classifiers[0] as RandomTree
                    val infoField = RandomTree::class.java.getDeclaredField("m_Info")
                    infoField.isAccessible = true
                    return infoField.get(firstTree) as Instances
                } catch (_: NoSuchFieldException) {
                    cls = cls.superclass
                }
            }
        }
        throw IllegalStateException("Cannot extract header from ${model.javaClass.name}")
    }

    private fun setNominal(inst: Instance, attr: Attribute, value: String) {
        val idx = attr.indexOfValue(value)
        if (idx >= 0) inst.setValue(attr, idx.toDouble())
    }

    override fun predictComplete(request: TelemetryRequest, station: Station, userId: UUID): Int {
        val classifier = completeClassifier ?: return fallbackPrediction()
        val header = completeHeader ?: return fallbackPrediction()

        val zdt = Instant.ofEpochMilli(request.timestamp).atZone(LISBON_ZONE)
        val inst = DenseInstance(header.numAttributes())
        inst.setDataset(header)

        for (i in 0 until header.numAttributes()) {
            if (i == header.classIndex()) continue
            val a = header.attribute(i)
            when (a.name()) {
                "transportType" -> setNominal(inst, a, station.transportType.name)
                "timestamp" -> inst.setValue(i, request.timestamp.toDouble())
                "dayOfWeek" -> inst.setValue(i, zdt.dayOfWeek.value.toDouble())
                "hour" -> inst.setValue(i, zdt.hour.toDouble())
                "isWeekend" -> inst.setValue(i,
                    if (zdt.dayOfWeek == DayOfWeek.SATURDAY || zdt.dayOfWeek == DayOfWeek.SUNDAY) 1.0 else 0.0)
                "isRushHour" -> inst.setValue(i,
                    if (zdt.hour in 7..10 || zdt.hour in 17..20) 1.0 else 0.0)
                "latitude" -> inst.setValue(i, request.latitude)
                "longitude" -> inst.setValue(i, request.longitude)
                "bluetoothCount" -> inst.setValue(i, request.bluetoothCount.toDouble())
                "bt_signal_1" -> inst.setValue(i, request.bluetoothSignals[0].toDouble())
                "bt_signal_2" -> inst.setValue(i, request.bluetoothSignals[1].toDouble())
                "bt_signal_3" -> inst.setValue(i, request.bluetoothSignals[2].toDouble())
                "bt_signal_4" -> inst.setValue(i, request.bluetoothSignals[3].toDouble())
                "bt_signal_5" -> inst.setValue(i, request.bluetoothSignals[4].toDouble())
                "wifiCount" -> inst.setValue(i, request.wifiCount.toDouble())
                "wf_signal_1" -> inst.setValue(i, request.wifiSignals[0].toDouble())
                "wf_signal_2" -> inst.setValue(i, request.wifiSignals[1].toDouble())
                "wf_signal_3" -> inst.setValue(i, request.wifiSignals[2].toDouble())
                "wf_signal_4" -> inst.setValue(i, request.wifiSignals[3].toDouble())
                "wf_signal_5" -> inst.setValue(i, request.wifiSignals[4].toDouble())
                "latencyAvg" -> inst.setValue(i, request.latencyAvg)
                "latencyStdDev" -> inst.setValue(i, request.latencyStdDev)
                "packetLoss" -> inst.setValue(i, request.packetLoss)
                "rsrp" -> inst.setValue(i, request.rsrp.toDouble())
                "rssnr" -> inst.setValue(i, request.rssnr.toDouble())
                "rsrq" -> inst.setValue(i, request.rsrq.toDouble())
            }
        }

        return try {
            val idx = classifier.classifyInstance(inst).toInt()
            header.classAttribute().value(idx).toInt()
        } catch (e: Exception) {
            log.warn("COMPLETE prediction failed: ${e.message}")
            fallbackPrediction()
        }
    }

    override fun predictLimited(station: Station, timestamp: Long): Int {
        val classifier = limitedClassifier ?: return fallbackPrediction()
        val header = limitedHeader ?: return fallbackPrediction()

        val zdt = Instant.ofEpochMilli(timestamp).atZone(LISBON_ZONE)
        val inst = DenseInstance(header.numAttributes())
        inst.setDataset(header)

        for (i in 0 until header.numAttributes()) {
            if (i == header.classIndex()) continue
            val a = header.attribute(i)
            when (a.name()) {
                "transportType" -> setNominal(inst, a, station.transportType.name)
                "timestamp" -> inst.setValue(i, timestamp.toDouble())
                "dayOfWeek" -> inst.setValue(i, zdt.dayOfWeek.value.toDouble())
                "hour" -> inst.setValue(i, zdt.hour.toDouble())
                "isWeekend" -> inst.setValue(i,
                    if (zdt.dayOfWeek == DayOfWeek.SATURDAY || zdt.dayOfWeek == DayOfWeek.SUNDAY) 1.0 else 0.0)
                "isRushHour" -> inst.setValue(i,
                    if (zdt.hour in 7..10 || zdt.hour in 17..20) 1.0 else 0.0)
                "latitude" -> inst.setValue(i, station.latitude)
                "longitude" -> inst.setValue(i, station.longitude)
            }
        }

        return try {
            val idx = classifier.classifyInstance(inst).toInt()
            header.classAttribute().value(idx).toInt()
        } catch (e: Exception) {
            log.warn("LIMITED prediction failed: ${e.message}")
            fallbackPrediction()
        }
    }

    private fun fallbackPrediction(): Int = 3
}
