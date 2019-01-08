package pl.memleak.mmos.homeassistantintegration

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.support.v4.app.JobIntentService
import android.util.Log
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class PollService : JobIntentService() {
    companion object {
        const val TAG = "PollService"

        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(context, PollService::class.java, 0, intent)
        }
    }

    override fun onHandleWork(intent: Intent) {
        val clz = SensorPollWorker::class.java
        val pollWork = PeriodicWorkRequest.Builder(clz, 15L, TimeUnit.MINUTES).build()
        WorkManager.getInstance().enqueueUniquePeriodicWork("poll", ExistingPeriodicWorkPolicy.REPLACE,
            pollWork)

        val message = "Enqueued SensorPollWorker job %s".format(pollWork.id)
        Log.i(TAG, message)
    }
}