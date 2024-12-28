package com.example.myapplication.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun LightSensorScreen(onBackToHome: () -> Unit, isDebug: Boolean = false) {
    val context = LocalContext.current
    val sensorManager = if (!isDebug) context.getSystemService(Context.SENSOR_SERVICE) as SensorManager else null
    val lightSensor = if (!isDebug) sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT) else null

    var lightLevel by remember { mutableStateOf(0f) }

    if (isDebug) {
        // Simulate light sensor values for testing
        LaunchedEffect(Unit) {
            lightLevel = 300f // Replace with test value
        }
    } else {
        val sensorEventListener = remember {
            object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    lightLevel = event?.values?.get(0) ?: 0f
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                    // No action needed for this example
                }
            }
        }

        DisposableEffect(Unit) {
            if (lightSensor != null) {
                sensorManager?.registerListener(sensorEventListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
            }
            onDispose {
                sensorManager?.unregisterListener(sensorEventListener)
            }
        }
    }

    val backgroundColor = when {
        lightLevel < 100 -> Color.DarkGray
        lightLevel < 500 -> Color.Gray
        lightLevel < 1000 -> Color.LightGray
        else -> Color.White
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Light Level: $lightLevel lux",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBackToHome) {
            Text("Back to Home")
        }
    }
}
