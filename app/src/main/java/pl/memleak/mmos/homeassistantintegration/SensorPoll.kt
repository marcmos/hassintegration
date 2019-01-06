package pl.memleak.mmos.homeassistantintegration

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.SensorManager.SENSOR_DELAY_FASTEST
import android.util.Log
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import java.util.*

class SensorPoll(val context: Context) {
    companion object {
        const val TAG = "SensorPoll"
    }

    val mSensorReadings: DescriptiveStatistics = DescriptiveStatistics(20)
    var mMeasurement: Float? = null

    inner class SensorMeasurementListener : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        }

        override fun onSensorChanged(event: SensorEvent?) {
            mMeasurement = event!!.values[0]
            Log.i(TAG, "New sensor value %f".format(mMeasurement))
        }

    }

    inner class MeasurementTask : TimerTask() {
        override fun run() {
            if(mMeasurement != null) {
                mSensorReadings.addValue(mMeasurement!!.toDouble())
                Log.i(TAG, "Adding measurement %f".format(mMeasurement))
            }
        }
    }

    fun poll(): Double {
        val timer = Timer()
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        val sensorListener = SensorMeasurementListener()
        sensorManager.registerListener(sensorListener, sensor, SENSOR_DELAY_FASTEST)

        timer.scheduleAtFixedRate(MeasurementTask(), 0, 1000)

        Thread.sleep(30 * 1000)
        Log.i(TAG, "Measurement session over, unregistering sensor")
        timer.cancel()
        sensorManager.unregisterListener(sensorListener)

        return mSensorReadings.mean
    }
}