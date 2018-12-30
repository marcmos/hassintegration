package pl.memleak.mmos.homeassistantintegration

import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.MqttMessage

class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var mSensorManager: SensorManager
    private var mLight: Sensor? = null
    private var mMqttClient: MqttAndroidClient? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startService(Intent(this, SensorPollService::class.java))

//        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
//        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

//        mMqttClient = MqttAndroidClient(this, "tcp://192.168.7.9:1883", "android")

//        val options = MqttConnectOptions()
//        options.userName = "homeassistant"
//        options.password = "mqtt".toCharArray()

//        try {
//            println("connect")
//            mMqttClient!!.connect(options, null, object : IMqttActionListener {
//                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
//                    println("connection failed")
//                    exception!!.printStackTrace()
//                }

//                override fun onSuccess(asyncActionToken: IMqttToken?) {
//                    println("connected")
//                }

//            })
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }

//        println("ende")
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Do something here if sensor accuracy changes.
    }

    override fun onSensorChanged(event: SensorEvent) {
        val measurement = event.values[0]
        val message = MqttMessage(measurement.toString().toByteArray())

        if(mMqttClient?.isConnected == true) {
            mMqttClient?.publish("home-assistant/light", message)
        }

        println(measurement)
    }

    override fun onResume() {
        // Register a listener for the sensor.
        super.onResume()
//        mSensorManager.registerListener(this, mPressure, SensorManager.SENSOR_DELAY_NORMAL)
//        mSensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        // Be sure to unregister the sensor when the activity pauses.
        super.onPause()
//        mSensorManager.unregisterListener(this)
    }
}
