package pl.memleak.mmos.homeassistantintegration

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.SensorManager.*
import android.os.IBinder
import android.util.Log
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics

class SensorPollService : Service(), SensorEventListener {
    lateinit var mSensorReadings: DescriptiveStatistics
    lateinit var mSensorManager: SensorManager
    lateinit var mSensor: Sensor


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        mSensorReadings = DescriptiveStatistics(5)
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(javaClass.name, "onStartCommand()")
        mSensorManager.registerListener(this, mSensor, SENSOR_DELAY_FASTEST)
        return START_STICKY
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent) {
        val measurement = event.values[0]
        mSensorReadings.addValue(measurement.toDouble())
        Log.d(javaClass.name, "New sensor value: %f, current mean: %f".format(measurement, mSensorReadings.mean))
    }

}