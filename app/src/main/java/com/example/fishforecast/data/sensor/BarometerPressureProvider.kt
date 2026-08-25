package com.example.fishforecast.data.sensor

import android.app.Application
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.fishforecast.domain.sensor.PressureProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BarometerPressureProvider @Inject constructor(
    private val application: Application
) : PressureProvider {

    private val sensorManager: SensorManager by lazy {
        application.getSystemService(SensorManager::class.java)
    }

    private val pressureSensor: Sensor? by lazy {
        sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
    }

    override val isAvailable: Boolean
        get() = pressureSensor != null

    override fun pressureFlow(): Flow<Float> = callbackFlow {
        val sensor = pressureSensor
        if (sensor == null) {
            close()
            return@callbackFlow
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                event.values.firstOrNull()?.let { trySend(it) }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        // Давление меняется медленно: держим самый редкий режим ради батареи.
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)

        awaitClose { sensorManager.unregisterListener(listener) }
    }
}
