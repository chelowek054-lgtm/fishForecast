package com.example.fishforecast.domain.sensor

import kotlinx.coroutines.flow.Flow

/**
 * Локальный барометр. Показания точнее сетевого прогноза для конкретной
 * точки, поэтому используются как уточнение, а не как замена.
 */
interface PressureProvider {

    /** Есть ли на устройстве датчик давления. */
    val isAvailable: Boolean

    /** Давление в гПа. Поток пуст, если датчика нет. */
    fun pressureFlow(): Flow<Float>
}
