package pt.ipp.estg.cmugrupo8

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.compose.runtime.mutableStateOf

object LightSensorManager : SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var lightSensor: Sensor? = null
    val isDarkMode = mutableStateOf(false) // Mutable state for theme change

    fun initialize(context: Context) {
        if (sensorManager == null) {
            sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            lightSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)

            lightSensor?.let {
                sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        Log.d("LightSensorManager", "Light sensor value: ${event?.values?.get(0)}")
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            val ambientLight = event.values.get(0)
            isDarkMode.value = ambientLight < 1000 // Update the state based on light
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No action needed
    }

    fun unregister() {
        sensorManager?.unregisterListener(this)
    }
}
