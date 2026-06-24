package pt.isel.otp.service.impl

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import weka.classifiers.Classifier
import weka.classifiers.trees.RandomForest
import weka.classifiers.trees.RandomTree
import weka.core.Attribute
import weka.core.DenseInstance
import weka.core.Instance
import weka.core.Instances
import weka.core.SerializationHelper
import java.io.*
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.util.UUID

class InferenceServiceModelInspectionTest {

    private val lisbonZone = ZoneId.of("Europe/Lisbon")

    @Test
    fun `COMPLETE model predicts correctly`() {
        val model = loadModel("complete.model")
        val header = extractInternalHeader(model)
        println("COMPLETE attributes: ${header.numAttributes()}")
        assertTrue(header.numAttributes() == 27) { "Expected 27, got ${header.numAttributes()}" }

        val inst = makeTestInstance(header, true)
        val result = safePredict(model, inst, header)
        println("COMPLETE predicted: $result")
        assertTrue(result in 1..5)
    }

    @Test
    fun `LIMITED model structure`() {
        val model = loadModel("limited.model")
        val header = extractInternalHeader(model)
        println("LIMITED attributes: ${header.numAttributes()}")
        val cname = header.classAttribute().name()
        println("Class: $cname (idx ${header.classIndex()}) values=${header.classAttribute().numValues()}")
        for (i in 0 until header.numAttributes()) {
            val a = header.attribute(i)
            println("  [$i] ${a.name()}: ${typeStr(a)} (${a.numValues()} values)")
        }
    }

    @Test
    fun `LIMITED model predicts`() {
        val model = loadModel("limited.model")
        val header = extractInternalHeader(model)
        println("LIMITED attributes: ${header.numAttributes()}")

        val inst = makeTestInstance(header, false)
        val result = safePredict(model, inst, header)
        println("LIMITED predicted: $result")
        assertTrue(result in 1..5)
    }

    private fun makeTestInstance(header: Instances, useAllAttrs: Boolean): Instance {
        val now = System.currentTimeMillis()
        val zdt = Instant.ofEpochMilli(now).atZone(lisbonZone)

        val inst = DenseInstance(header.numAttributes())
        inst.setDataset(header)

        for (i in 0 until header.numAttributes()) {
            if (i == header.classIndex()) continue
            val a = header.attribute(i)
            val value = when (a.name()) {
                "transportType" -> nomIdx(a, "METRO")
                "timestamp"     -> now.toDouble()
                "dayOfWeek"     -> if (useAllAttrs) zdt.dayOfWeek.value.toDouble() else nomIdx(a, (zdt.dayOfWeek.value % 7 + 1).toString())
                "hour"          -> zdt.hour.toDouble()
                "isWeekend"     -> if (useAllAttrs) (if (zdt.dayOfWeek == DayOfWeek.SATURDAY || zdt.dayOfWeek == DayOfWeek.SUNDAY) 1.0 else 0.0) else nomIdx(a, if (zdt.dayOfWeek == DayOfWeek.SATURDAY || zdt.dayOfWeek == DayOfWeek.SUNDAY) "1" else "0")
                "isRushHour"    -> if (useAllAttrs) (if (zdt.hour in 7..10 || zdt.hour in 17..20) 1.0 else 0.0) else nomIdx(a, if (zdt.hour in 7..10 || zdt.hour in 17..20) "1" else "0")
                "latitude"      -> 38.7371344
                "longitude"     -> -9.1328835
                "bluetoothCount" -> 50.0
                "bt_signal_1"   -> -60.0
                "bt_signal_2"   -> -65.0
                "bt_signal_3"   -> -70.0
                "bt_signal_4"   -> -72.0
                "bt_signal_5"   -> -75.0
                "wifiCount"     -> 10.0
                "wf_signal_1"   -> -55.0
                "wf_signal_2"   -> -60.0
                "wf_signal_3"   -> -65.0
                "wf_signal_4"   -> -68.0
                "wf_signal_5"   -> -70.0
                "latencyAvg"    -> 100.0
                "latencyStdDev" -> 50.0
                "packetLoss"    -> 0.0
                "rsrp"          -> -95.0
                "rssnr"         -> 20.0
                "rsrq"          -> -8.0
                else            -> throw IllegalArgumentException("Unknown attr: ${a.name()}")
            }
            inst.setValue(i, value)
        }
        return inst
    }

    private fun nomIdx(a: Attribute, value: String): Double {
        val idx = a.indexOfValue(value)
        assertTrue(idx >= 0) { "Value '$value' not found in ${a.name()}" }
        return idx.toDouble()
    }

    private fun safePredict(model: Classifier, instance: Instance, header: Instances): Int {
        return try {
            val idx = model.classifyInstance(instance).toInt()
            header.classAttribute().value(idx).toInt()
        } catch (e: Exception) {
            println("FAILED: $e")
            throw e
        }
    }

    private fun extractInternalHeader(model: Classifier): Instances {
        if (model is RandomForest) {
            var cls: Class<*> = model.javaClass
            while (cls != Any::class.java) {
                try {
                    val f = cls.getDeclaredField("m_Classifiers")
                    f.isAccessible = true
                    val classifiers = f.get(model) as Array<Classifier>
                    val first = classifiers[0] as RandomTree
                    val infoField = RandomTree::class.java.getDeclaredField("m_Info")
                    infoField.isAccessible = true
                    return infoField.get(first) as Instances
                } catch (_: NoSuchFieldException) {
                    cls = cls.superclass
                }
            }
        }
        throw IllegalStateException("Cannot extract header from ${model.javaClass.name}")
    }

    private fun typeStr(a: Attribute): String = when (a.type()) {
        Attribute.NOMINAL -> "NOMINAL"
        Attribute.NUMERIC -> "NUMERIC"
        Attribute.STRING -> "STRING"
        Attribute.DATE -> "DATE"
        else -> "?${a.type()}"
    }

    private fun loadModel(name: String): Classifier {
        val file = File("src/test/resources/models/" + name)
        assertTrue(file.exists()) { "Model not found: " + file.absolutePath }
        return SerializationHelper.read(file.absolutePath) as Classifier
    }
}
