package pl.memleak.mmos.homeassistantintegration

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MainActivity"
    }

    private fun forceMeasurement() {
        PollService.enqueueWork(this, Intent())
        Toast.makeText(this, "Enqueued measurement", LENGTH_LONG).show()
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.force_measurement).setOnClickListener {
            forceMeasurement()
        }

        forceMeasurement()
    }
}
