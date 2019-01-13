package pl.memleak.mmos.homeassistantintegration

import android.content.Context
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class MqttPush(val context: Context, val topic: String) {
    private suspend fun connect(client: MqttAndroidClient, options: MqttConnectOptions): IMqttToken? {
        return suspendCoroutine {
            client.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    it.resume(asyncActionToken)
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    it.resumeWithException(exception!!)
                }
            })
        }
    }

    private suspend fun publish(client: MqttAndroidClient, topic: String, message: MqttMessage): IMqttToken? {
        return suspendCoroutine {
            client.publish(topic, message, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    it.resume(asyncActionToken)
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    it.resumeWithException(exception!!)
                }
            })
        }
    }

    private suspend fun disconnect(client: MqttAndroidClient): IMqttToken? {
        return suspendCoroutine {
            client.disconnect(null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    it.resume(asyncActionToken)
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    it.resumeWithException(exception!!)
                }
            })
        }
    }

    suspend fun pubSingle(value: String) {
        val mqttClient = MqttAndroidClient(context, "tcp://192.168.7.9:1883", "android")
        val options = MqttConnectOptions()
        options.userName = "homeassistant"
        options.password = "mqtt".toCharArray()

        connect(mqttClient, options)
        publish(mqttClient, topic, MqttMessage(value.toByteArray()))
        disconnect(mqttClient)
    }

    companion object {
        const val TAG = "MqttPush"
    }
}