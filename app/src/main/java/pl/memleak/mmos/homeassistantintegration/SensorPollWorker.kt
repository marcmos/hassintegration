package pl.memleak.mmos.homeassistantintegration

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.POWER_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.ConnectivityManager.CONNECTIVITY_ACTION
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.WIFI_MODE_FULL
import android.os.PowerManager
import android.support.v4.app.NotificationCompat
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

    private fun <Result> withNotification(f: () -> Result): Result {
        val notificationManager = (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)

        val notification = NotificationCompat.Builder(context)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(context.getString(R.string.ongoing_measurement))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .build()

        notificationManager.notify(0, notification)

        return try {
            f()
        } finally {
            notificationManager.cancel(0)
        }
    }

    override fun doWork(): Result {
        return withNotification {
            val pm = context.getSystemService(POWER_SERVICE) as PowerManager
            val wl = pm.newWakeLock(
                PowerManager.FULL_WAKE_LOCK or
                        PowerManager.ACQUIRE_CAUSES_WAKEUP, "homeassistantintegration:measurementwakelock")

            wl.acquire(60 * 1000)

            Log.i(TAG, "Started %s".format(workerParameters.id))

            val readout = SensorPoll(context).poll()

            if (readout == Double.NaN) {
                Log.w(TAG, "NaN readout, skipping MQTT push")
                wl.release()
                Result.failure()
            } else {
                Log.i(TAG, "Publishing sensor readout %f".format(readout))
                val mqttPush = MqttPush(context)

                val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                val wifiLock = wifiManager.createWifiLock(WIFI_MODE_FULL, TAG)

                wifiLock.acquire()

                val connected = isConnected()!!
                if (connected) {
                    Log.i(TAG, "Network is connected")
                } else
                    if (!connected) {
                        Log.w(TAG, "Network is disconnected, awaiting network...")

                        runBlocking {
                            withTimeout(60 * 1000) {
                                suspendCoroutine<Unit> {
                                    context.registerReceiver(object : BroadcastReceiver() {
                                        override fun onReceive(context: Context?, intent: Intent?) {
                                            it.resume(Unit)
                                        }
                                    }, IntentFilter(CONNECTIVITY_ACTION))
                                }
                            }
                        }
                    }

                try {
                    runBlocking { mqttPush.pubSingle(readout.toString()) }
                    Log.i(TAG, "MQTT publish succeeded")

                    Result.success()
                } catch (e: Exception) {
                    Log.e(TAG, "MQTT publish failed", e)
                    Result.retry()
                } finally {
                    wifiLock.release()
                    wl.release()
                    Log.w(TAG, "WiFi lock released")
                }
            }
        }

    }

    companion object {
        const val TAG = "SensorPollWorker"
    }
}