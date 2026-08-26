# 🎣 FishForecast

Android-приложение, которое отвечает на один вопрос: **ехать сегодня на рыбалку или нет.**
Погода, оценка активности рыбы, офлайн-карты и журнал уловов — всё работает без сети,
потому что на воде её обычно нет.

Дорожная карта и решения по архитектуре — в [app/docs/Roadmap.md](app/docs/Roadmap.md).

## Сборка

Нужен JDK 17+ (подойдёт тот, что идёт с Android Studio) и Android SDK.

```bash
./gradlew :app:assembleDebug
```

Если Gradle не находит JDK или SDK, укажите их явно:

```bash
ANDROID_HOME="C:/Users/ИМЯ/AppData/Local/Android/Sdk" ./gradlew :app:assembleDebug -Dorg.gradle.java.home="D:/android/jbr"
```

Тесты:

```bash
./gradlew :app:testDebugUnitTest
```

## Ключи карт

Скопируйте шаблон и заполните его — файл `local.properties` в `.gitignore`,
ключи в репозиторий не попадают:

```bash
cp local.properties.example local.properties
```

### Схема карты

Работает сразу, ключ не нужен: OpenFreeMap (данные OpenStreetMap).
Адрес стиля — константа `STYLE_URL` в
[`ui/map/MapConfig.kt`](app/src/main/java/com/example/fishforecast/ui/map/MapConfig.kt).

### Спутниковые снимки

| Источник | Разрешение | Ключ | Офлайн |
|---|---|---|---|
| Sentinel-2 cloudless (EOX) | 10 м/пиксель, до 14-го масштаба | не нужен | разрешён лицензией |
| MapTiler Satellite | около метра, до 20-го масштаба | `MAPTILER_KEY` | запрещён тарифом |
| Mapbox Satellite | около метра, до 20-го масштаба | `MAPBOX_TOKEN` | запрещён тарифом |

Без ключа приложение показывает Sentinel-2 и объясняет это на экране карты —
отсутствие ключа не ломает работу. Достаточно **одного** ключа из двух:

```properties
MAPTILER_KEY=ваш_ключ
```

Ключи берутся на [cloud.maptiler.com](https://cloud.maptiler.com) (регистрация без карты,
на карте остаётся логотип MapTiler) или [account.mapbox.com](https://account.mapbox.com)
(50 000 загрузок в месяц, коммерческое использование разрешено тарифом).

После правки `local.properties` пересоберите приложение: ключ подставляется на этапе
сборки через `buildConfigField`.

## Перед публикацией

- Подтвердить право на предзагрузку тайлов у источника схемы либо перейти на
  собственный хостинг PMTiles. Политика `tile.openstreetmap.org` предзагрузку запрещает.
- Сохранить атрибуцию OpenStreetMap и EOX — этого требуют лицензии.
- Обновить зависимости: Kotlin, AGP и Compose в проекте от осени 2024 года.
