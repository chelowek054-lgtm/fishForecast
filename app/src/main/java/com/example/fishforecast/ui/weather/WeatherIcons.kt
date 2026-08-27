package com.example.fishforecast.ui.weather

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.FilterDrama
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.fishforecast.domain.weather.Sky

/** Один значок на группу погоды: по нему колонка дня читается без текста. */
fun Sky.icon(): ImageVector = when (this) {
    Sky.CLEAR -> Icons.Default.WbSunny
    Sky.PARTLY_CLOUDY -> Icons.Default.FilterDrama
    Sky.CLOUDY -> Icons.Default.WbCloudy
    Sky.FOG -> Icons.Default.Cloud
    Sky.DRIZZLE -> Icons.Default.WaterDrop
    Sky.RAIN -> Icons.Default.Umbrella
    Sky.SHOWER -> Icons.Default.Grain
    Sky.SNOW -> Icons.Default.AcUnit
    Sky.THUNDER -> Icons.Default.Thunderstorm
}
