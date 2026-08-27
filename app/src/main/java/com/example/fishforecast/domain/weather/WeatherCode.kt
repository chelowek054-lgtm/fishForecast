package com.example.fishforecast.domain.weather

/**
 * Коды погоды WMO, которыми отвечает Open-Meteo.
 *
 * Число в интерфейсе бесполезно: рыболову нужно понимать, что за окном.
 * Кодов почти три десятка, но по смыслу они складываются в несколько
 * групп — по ним же выбирается значок.
 */
enum class Sky {
    CLEAR,
    PARTLY_CLOUDY,
    CLOUDY,
    FOG,
    DRIZZLE,
    RAIN,
    SHOWER,
    SNOW,
    THUNDER
}

fun skyOf(code: Int): Sky = when (code) {
    0 -> Sky.CLEAR
    1, 2 -> Sky.PARTLY_CLOUDY
    3 -> Sky.CLOUDY
    45, 48 -> Sky.FOG
    51, 53, 55, 56, 57 -> Sky.DRIZZLE
    61, 63, 65, 66, 67 -> Sky.RAIN
    71, 73, 75, 77, 85, 86 -> Sky.SNOW
    80, 81, 82 -> Sky.SHOWER
    95, 96, 99 -> Sky.THUNDER
    else -> Sky.CLOUDY
}

fun weatherCodeText(code: Int): String = when (code) {
    0 -> "Ясно"
    1 -> "Малооблачно"
    2 -> "Переменная облачность"
    3 -> "Пасмурно"
    45 -> "Туман"
    48 -> "Изморозь"
    51 -> "Слабая морось"
    53 -> "Морось"
    55 -> "Сильная морось"
    56, 57 -> "Ледяная морось"
    61 -> "Слабый дождь"
    63 -> "Дождь"
    65 -> "Сильный дождь"
    66, 67 -> "Ледяной дождь"
    71 -> "Слабый снег"
    73 -> "Снег"
    75 -> "Сильный снег"
    77 -> "Снежные зёрна"
    80 -> "Слабый ливень"
    81 -> "Ливень"
    82 -> "Сильный ливень"
    85 -> "Слабый снегопад"
    86 -> "Сильный снегопад"
    95 -> "Гроза"
    96, 99 -> "Гроза с градом"
    else -> "Облачно"
}

/**
 * Насколько погода «тяжелее» соседней. Нужен, чтобы за день показать не
 * самый частый код, а самый значимый: два часа грозы важнее двадцати
 * часов переменной облачности.
 */
fun weatherSeverity(code: Int): Int = when (skyOf(code)) {
    Sky.CLEAR -> 0
    Sky.PARTLY_CLOUDY -> 1
    Sky.CLOUDY -> 2
    Sky.FOG -> 3
    Sky.DRIZZLE -> 4
    Sky.SNOW -> 5
    Sky.RAIN -> 6
    Sky.SHOWER -> 7
    Sky.THUNDER -> 8
}
