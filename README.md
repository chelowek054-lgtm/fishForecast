# 🎣 FishForecast

Android-приложение, которое отвечает на один вопрос: **ехать сегодня на рыбалку или нет.**
Погода, оценка активности рыбы, офлайн-карты и журнал уловов — всё работает без сети,
потому что на воде её обычно нет.

Дорожная карта и решения по архитектуре — в [app/docs/Roadmap.md](app/docs/Roadmap.md).

Как считается клёв, какие данные для этого нужны и чего модели пока не хватает —
в [app/docs/architecture/client/bite-score.md](app/docs/architecture/client/bite-score.md).

Как рыболовы обмениваются районами и каким будет общий сервер —
в [app/docs/RegionPack.md](app/docs/RegionPack.md).

Как ведётся разработка: **документ ведёт код**. Изменение начинается с правки
документации, она подтверждается, и только потом меняется код — правила в
[CLAUDE.md](CLAUDE.md), решение и причины в
[ADR-0010](app/docs/architecture/adr/0010-documentation-driven-development.md).

Границы системы, потоки данных, контракты API и решения с их причинами —
в [app/docs/architecture/](app/docs/architecture/README.md). Оттуда же ведутся
фазы 6–11: модульность, модель клёва как данные, сервер с админкой, соцчасть и
батиметрия.

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

## Карты

Ключи и регистрация не нужны — оба источника открытые.

| Слой | Источник | Разрешение | Офлайн |
|---|---|---|---|
| Схема | OpenFreeMap (данные OpenStreetMap) | векторная | да |
| Спутник | Sentinel-2 cloudless (EOX) | 10 м/пиксель, до 14-го масштаба | нет, только при сети |

Адреса источников — константы в
[`ui/map/MapConfig.kt`](app/src/main/java/com/example/fishforecast/ui/map/MapConfig.kt).

## Перед публикацией

- Подтвердить право на предзагрузку тайлов у источника схемы либо перейти на
  собственный хостинг PMTiles. Политика `tile.openstreetmap.org` предзагрузку запрещает.
- Сохранить атрибуцию OpenStreetMap и EOX — этого требуют лицензии.
- Обновить зависимости: Kotlin, AGP и Compose в проекте от осени 2024 года.
