package pl.memleak.mmos.homeassistantintegration

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.ConnectivityManager.CONNECTIVITY_ACTION
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.WIFI_MODE_FULL
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SensorPollWorker(val context: Context, private val workerParameters: WorkerParameters) :
    Worker(context, workerParameters) {

    private fun isConnected(): Boolean? {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        return activeNetwork?.isConnected
    }

    override fun doWork(): Result {
        Log.i(TAG, "Started %s".format(workerParameters.id))

        val readout = SensorPoll(context).poll()

        if(readout == Double.NaN) {
            Log.w(TAG, "NaN readout, skipping MQTT push")
            return Result.failure()
        } else {
            Log.i(TAG, "Publishing sensor readout %f".format(readout))
            val mqttPush = MqttPush(context)

            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiLock = wifiManager.createWifiLock(WIFI_MODE_FULL, TAG)

            wifiLock.acquire()

            val connected = isConnected()!!
            if(connected) {
                Log.i(TAG, "Network is connected")
            } else
            if(!connected) {
                Log.w(TAG, "Network is disconnected, awaiting network...")

                runBlocking { withTimeout(60 * 1000) {
                    suspendCoroutine<Unit> {
                        context.registerReceiver(object : BroadcastReceiver() {
                            override fun onReceive(context: Context?, intent: Intent?) {
                                it.resume(Unit)
                            }
                        }, IntentFilter(CONNECTIVITY_ACTION))
                    }
                } }
            }

            return try {
                runBlocking { mqttPush.pubSingle(readout.toString()) }
                Log.i(TAG, "MQTT publish succeeded")

                Result.success()
            } catch (e: Exception) {
                Log.e(TAG, "MQTT publish failed", e)
                Result.retry()
            } finally {
                wifiLock.release()
                Log.w(TAG, "WiFi lock released")
            }
        }
    }

    companion object {
        const val TAG = "SensorPollWorker"
    }
}