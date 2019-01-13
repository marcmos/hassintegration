package pl.memleak.mmos.homeassistantintegration

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SCREEN_OFF
import android.content.Intent.ACTION_SCREEN_ON
import android.content.IntentFilter
import android.os.IBinder
import android.os.Vibrator
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.thread

class ShakeRecognizerService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        val shakeListener = ShakeListener(this)
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val mqttPush = MqttPush(this, "home-assistant/toggle")
        var switch = false

        shakeListener.setOnShakeListener(object : ShakeListener.OnShakeListener {
            override fun onShake() {
                vibrator.vibrate(500)
                switch = !switch
                thread {
                    runBlocking {
                        mqttPush.pubSingle(if (switch) "ON" else "OFF")
                    }
                }
            }
        })

        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                shakeListener.resume()
            }
        }, IntentFilter(ACTION_SCREEN_ON))

        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                shakeListener.pause()
            }
        }, IntentFilter(ACTION_SCREEN_OFF))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
}