package pl.memleak.mmos.homeassistantintegration

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class PollService : Service() {
    companion object {
        const val TAG = "PollService"
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val clz = SensorPollWorker::class.java
        val pollWork = PeriodicWorkRequest.Builder(clz, 15L, TimeUnit.MINUTES).build()
        WorkManager.getInstance().enqueueUniquePeriodicWork("poll", ExistingPeriodicWorkPolicy.REPLACE,
            pollWork)
        Log.i(TAG, "Enqueued SensorPollWorker job %s".format(pollWork.id))
        return START_NOT_STICKY
    }
}