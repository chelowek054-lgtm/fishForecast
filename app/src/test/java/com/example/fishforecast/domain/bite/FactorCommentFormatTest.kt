package com.example.fishforecast.domain.bite

import com.example.fishforecast.data.local.entities.DailySunEntity
import com.example.fishforecast.data.local.entities.FishEntity
import com.example.fishforecast.data.local.entities.WeatherEntity
import com.example.fishforecast.domain.knowledge.KnowledgeCodec
import com.example.fishforecast.domain.knowledge.StructureType
import com.example.fishforecast.domain.water.WaterHour
import com.example.fishforecast.domain.water.WaterState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.time.LocalDateTime

/**
 * Пояснения факторов — то, ради чего оценка вообще заслуживает доверия: без
 * причины число проверить нельзя. Поэтому у них один формат, и он проверяется
 * так же, как арифметика.
 *
 * Правило: сначала измеренное значение с единицей, потом « — » и что это
 * значит для этого вида. Имя фактора внутри пояснения не повторяется — оно
 * уже стоит слева от двоеточия на экране.
 */
class FactorCommentFormatTest {

    private val useCase = CalculateFishActivityUseCase()

    private val catalog = KnowledgeCodec
        .decode(File("src/main/assets/knowledge.json").readText())
        .getOrThrow()

    private val zander = FishEntity(
        id = 1,
        name = "Судак",
        guild = "predator",
        optMinTemp = 18f,
        optMaxTemp = 26f,
        absMinTemp = 2f,
        absMaxTemp = 30f,
        maxPressureDrop = 7f,
        maxPressureRise = 6f,
        pressureRecoveryHours = 14
    )

    /** Сутки с ходом давления, воды и ветра: чтобы высказались все факторы. */
    private fun conditions(): Triple<List<WeatherEntity>, WaterState, List<DailySunEntity>> {
        val hours = (0 until 30).map { index ->
            WeatherEntity(
                mapId = 1,
                time = LocalDateTime.parse("2026-09-03T00:00").plusHours(index.toLong()).toString(),
                temperature = 27.0,
                humidity = 70.0,
                pressure = (752.0 - index * 0.2) * 1.333224,
                windSpeed = 9.0,
                windDirection = 200.0,
                weatherCode = 0,
                latitude = 55.0,
                longitude = 37.0
            )
        }
        val water = WaterState(
            shallow = hours.mapIndexed { index, entity ->
                WaterHour(entity.time, 28.0 + index * 0.05)
            },
            deep = hours.map { WaterHour(it.time, 17.0) },
            shallowDepthM = 1.5,
            deepDepthM = 4.0,
            depthsAssumed = false,
            anchored = false,
            oxygen = hours.associate { it.time to 6.4 },
            oxygenDeep = hours.associate { it.time to 3.1 }
        )
        val sun = listOf(
            DailySunEntity(
                mapId = 1,
                date = "2026-09-03",
                sunrise = "2026-09-03T06:00",
                sunset = "2026-09-03T20:00"
            ),
            DailySunEntity(
                mapId = 1,
                date = "2026-09-04",
                sunrise = "2026-09-04T06:00",
                sunset = "2026-09-04T20:00"
            )
        )
        return Triple(hours, water, sun)
    }

    private fun allFactors(): List<BiteFactor> {
        val (forecast, water, sun) = conditions()
        val place = PlaceContext(
            layer = WaterLayerChoice.SHALLOW,
            structures = listOf(StructureType(id = "snags", name = "Коряжник", predatorBonus = 0.25)),
            title = "Мысок"
        )
        val noted = listOf(
            ActiveObservation(
                type = catalog.observation("bait_fish_panic")!!,
                notedAt = LocalDateTime.parse("2026-09-04T04:00")
            )
        )
        return useCase(zander, forecast, 758.0, water, sun, place, noted).last().factors
    }

    @Test
    fun `высказываются все факторы, которые могут высказаться`() {
        val names = allFactors().map { it.name }

        assertTrue(
            "должны быть и ограничители, и условия, и отметка: $names",
            names.containsAll(
                listOf(
                    "Температура воды", "Кислород", "Расслоение", "Замечено", "Место",
                    "Давление", "Тенденция", "Ход за сутки", "Ход воды", "Ветер", "Свет"
                )
            )
        )
    }

    @Test
    fun `каждое пояснение отделяет значение от смысла`() {
        allFactors().forEach { factor ->
            assertTrue(
                "«${factor.name}: ${factor.comment}» — нет разделителя",
                factor.comment.contains(" — ")
            )
        }
    }

    @Test
    fun `пояснение не повторяет имя своего фактора`() {
        // На экране имя уже стоит слева от двоеточия: «Тенденция: Давление
        // стабильно за 2 ч» читалось как запинка.
        allFactors().forEach { factor ->
            assertFalse(
                "«${factor.name}: ${factor.comment}» — имя повторено",
                factor.comment.lowercase().startsWith(factor.name.lowercase())
            )
        }
    }

    @Test
    fun `пояснение начинается со значения, а не с фразы`() {
        // Значением бывает и румб — «Ю 2.5 м/с», — поэтому запрещено не любое
        // заглавное, а заглавное слово: с него начинается фраза, а не число.
        allFactors().forEach { factor ->
            val firstWord = factor.comment.takeWhile { it.isLetter() }
            assertFalse(
                "«${factor.name}: ${factor.comment}» — начинается фразой",
                firstWord.length > 2 && firstWord.first().isUpperCase()
            )
        }
    }

    @Test
    fun `у нуля не бывает знака`() {
        // «+0 мм за 24 ч» выглядело опиской.
        allFactors().forEach { factor ->
            assertFalse(
                "«${factor.name}: ${factor.comment}» — плюс у нуля",
                factor.comment.contains("+0 ") || factor.comment.contains("−0 ")
            )
        }
    }

    @Test
    fun `пороги вида названы цифрой, а не на словах`() {
        val factors = allFactors().associateBy { it.name }

        assertTrue(
            "кислород должен назвать порог вида: ${factors["Кислород"]?.comment}",
            factors["Кислород"]!!.comment.contains("5,0") ||
                factors["Кислород"]!!.comment.contains("5.0")
        )
        assertTrue(
            "давление должно назвать допуск вида: ${factors["Давление"]?.comment}",
            factors["Давление"]!!.comment.contains("терпит")
        )
        assertTrue(
            "температура должна назвать полосу: ${factors["Температура воды"]?.comment}",
            factors["Температура воды"]!!.comment.contains("–")
        )
    }
}
