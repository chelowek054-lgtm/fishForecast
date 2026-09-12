---
id: M-0009
type: map
title: Карта проекта на 2026-09-12
status: approved
created: 2026-09-12
updated: 2026-09-12
links:
  supersedes: [M-0001, M-0002, M-0003, M-0005, M-0006, M-0007, M-0008]
---

# Карта проекта на 2026-09-12

Одна сводная карта вместо семи прежних. M-0001, M-0002, M-0003, M-0005, M-0006
и M-0007 были помечены заменёнными, но заменить их было нечем, а у M-0008
свидетельство уехало из-под кода. Общая картина проекта держалась на трёх
модулях.

Как собрана:

1. Подтверждённые M-0001–M-0003 и M-0005–M-0008 сложены в порядке номеров:
   добавленное добавлено, убранное убрано.
2. Каждое свидетельство найдено в нынешнем коде заново. Сдвинувшееся получило
   новую строку; то, чего в коде больше нет, снято вместе с утверждением.
3. Файлы, появившиеся после 02.09.2026, дописаны: модуль — по KDoc его
   главного объявления, связи — по строкам `import` и обращениям внутри
   пакета, потоки — по запросам DAO и хранилищам настроек.
4. Пакетные узлы прежних карт (`ui.map`, `data/repository`, `res.drawable`
   и подобные) сняты: каждый файл теперь описан сам. Экраны-двойники
   `fishList`, `library` и `addEditFish` сведены к `reference`,
   `saved-maps` и `add-edit-fish`. Слой назван одним словом по месту файла.

Черновик: пока карта не подтверждена, общая картина пуста — прежние карты
заменены ею. Прочитайте, поправьте то, что названо неточно, и подтвердите.

## Кодовая база

```docdd-codemap
{
  "added": {
    "modules": [
      {
        "id": "app/src/androidTest/java/com/example/fishforecast/ExampleInstrumentedTest.kt",
        "title": "Инструментальный тест-заглушка",
        "layer": "test"
      },
      {
        "id": "app/src/main/AndroidManifest.xml",
        "title": "Манифест: приложение, активити, провайдеры",
        "layer": "app"
      },
      {
        "id": "app/src/main/assets/iconApp.png",
        "title": "Растровая иконка (бинарный файл, не разбирался)",
        "layer": "assets"
      },
      {
        "id": "app/src/main/assets/initial_fish.json",
        "title": "Встроенный справочник видов",
        "layer": "assets"
      },
      {
        "id": "app/src/main/assets/knowledge.json",
        "title": "Встроенные словари знаний",
        "layer": "assets"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/data/local",
        "title": "Определение базы Room",
        "layer": "data"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/data/local/ActiveMapStore.kt",
        "title": "DataStore: активная карта, базовый слой, автор пакетов",
        "layer": "data"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/data/local/AlertStore.kt",
        "title": "Память уведомлений: когда звали в прошлый раз и о каком окне",
        "layer": "data"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/data/local/AppDatabase.kt",
        "title": "Room-база и все миграции 1→26",
        "layer": "data"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/data/local/CatalogStore.kt",
        "title": "DataStore: адреса и версии справочника и словарей",
        "layer": "data"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/data/local/dao",
        "title": "DAO Room",
        "layer": "data"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/data/local/dao/CatchDao.kt",
        "title": "DAO журнала трофеев",
        "layer": "data"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/data/local/dao/FishDao.kt",
        "title": "DAO справочника видов",
        "layer": "data"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/data/local/dao/FishingSessionDao.kt",
        "title": "DAO выездов",
        "layer": "data"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/data/local/dao/FishingSpotDao.kt",
        "title": "DAO секретных точек",
        "layer": "data"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/data/local/dao/ObservationDao.kt",
        "title": "DAO отметок с берега",
        "layer": "data"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/data/local/dao/PressureLogDao.kt",
        "title": "DAO истории барометра",
        "layer": "data"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/data/local/dao/SavedMapDao.kt",
        "title": "DAO сохранённых карт",
        "layer": "data"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/data/local/dao/WeatherDao.kt",
        "title": "DAO прогноза и восходов",
        "layer": "data"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/data/local/entities",
        "title": "Сущности Room",
        "layer": "data"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/data/local/entities/CatchEntity.kt",
        "title": "Запись улова со снимком условий",
        "layer": "data"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/data/local/entities/DailySunEntity.kt",
        "title": "Восход и закат по дням карты",
        "layer": "data"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/data/local/entities/FishEntity.kt",
        "title": "Вид рыбы: пороги, гильдия, стол",
        "layer": "data"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/data/local/entities/FishingSessionEntity.kt",
        "title": "Выезд от сборов до итога",
        "layer": "data"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/data/local/entities/FishingSpotEntity.kt",
        "title": "Секретная точка и её структуры",
        "layer": "data"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/data/local/entities/ObservationEntity.kt",
        "title": "Отметка о том, что рыболов увидел своими глазами",
        "layer": "data"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/data/local/entities/PressureLogEntity.kt",
        "title": "Часовое показание барометра устройства",
        "layer": "data"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/data/local/entities/SavedMapEntity.kt",
        "title": "Сохранённая карта — контекст приложения",
        "layer": "data"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/data/local/entities/WeatherEntity.kt",
        "title": "Час прогноза для карты",
        "layer": "data"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/data/location",
        "title": "Реализация трекера местоположения",
        "layer": "data"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/data/location/DefaultLocationTracker.kt",
        "title": "Последняя точка через FusedLocationProvider",
        "layer": "data"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/data/remote",
        "title": "Retrofit-клиенты Open-Meteo и справочника",
        "layer": "data"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/data/remote/dto/WeatherDto.kt",
        "title": "DTO ответов Open-Meteo",
        "layer": "data"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/data/remote/FishCatalogApi.kt",
        "title": "Retrofit: справочник и словари по полному URL",
        "layer": "data"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/data/remote/WeatherApi.kt",
        "title": "Retrofit: Open-Meteo прогноз и история давления",
        "layer": "data"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/data/repository",
        "title": "Репозитории (фасад над Room и сетью)",
        "layer": "data"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/data/repository/BarometerRepository.kt",
        "title": "История собственного барометра",
        "layer": "data"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/data/repository/CatchRepository.kt",
        "title": "Журнал трофеев со снимком погоды и Bite Score",
        "layer": "data"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/data/repository/FishingContextRepository.kt",
        "title": "Контекст рыбалки: активная карта и всё из неё",
        "layer": "data"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/data/repository/FishingSessionRepository.kt",
        "title": "Выезды: сборы, план, итог",
        "layer": "data"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/data/repository/FishingSpotRepository.kt",
        "title": "CRUD секретных точек",
        "layer": "data"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/data/repository/FishRepository.kt",
        "title": "Справочник видов: ассеты + сервер, слияние по uid",
        "layer": "data"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/data/repository/KnowledgeRepository.kt",
        "title": "Словари знаний: документ целиком",
        "layer": "data"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/data/repository/ObservationRepository.kt",
        "title": "Что рыболов видел своими глазами на этом водоёме",
        "layer": "data"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/data/repository/OfflineMapRepository.kt",
        "title": "Скачивание офлайн-областей MapLibre",
        "layer": "data"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/data/repository/RegionPackRepository.kt",
        "title": "Экспорт и импорт пакетов района",
        "layer": "data"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/data/repository/WeatherRepository.kt",
        "title": "Прогноз по карте и оценка нормы давления",
        "layer": "data"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/data/sensor",
        "title": "Реализация барометра",
        "layer": "data"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/data/sensor/BarometerPressureProvider.kt",
        "title": "Поток давления с датчика устройства",
        "layer": "data"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/data/worker/BiteAlertWorker.kt",
        "title": "Оповещение «пора на воду»",
        "layer": "data"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/data/worker/WeatherSyncWorker.kt",
        "title": "Фоновое обновление прогноза",
        "layer": "data"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/di/DatabaseModule.kt",
        "title": "DI: Room и DAO",
        "layer": "di"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/di/LocationModule.kt",
        "title": "DI: местоположение",
        "layer": "di"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/di/NetworkModule.kt",
        "title": "DI: сеть и Retrofit",
        "layer": "di"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/di/SensorModule.kt",
        "title": "DI: барометр",
        "layer": "di"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/domain/alert/BiteAlertPolicy.kt",
        "title": "Когда звать рыболова на воду",
        "layer": "domain"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/domain/bite/BiteForecast.kt",
        "title": "Оценка активности за час",
        "layer": "domain"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCase.kt",
        "title": "Расчёт активности рыбы",
        "layer": "domain"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/domain/bite/FindBiteWindowUseCase.kt",
        "title": "Поиск окна клёва для уведомления",
        "layer": "domain"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/domain/bite/NormalPressure.kt",
        "title": "Норма давления водоёма",
        "layer": "domain"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/domain/bite/ObservationContext.kt",
        "title": "Наблюдение с берега вместе со временем, когда его сделали",
        "layer": "domain"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/domain/bite/PlaceContext.kt",
        "title": "Место расчёта: слой и структуры",
        "layer": "domain"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/domain/bite/WeekActivity.kt",
        "title": "Клёв на неделю вперёд по частям суток",
        "layer": "domain"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/domain/fish/FishCatalog.kt",
        "title": "Справочник видов и перевод в Room",
        "layer": "domain"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/domain/fish/FishWords.kt",
        "title": "Слова справочника по-человечески: где вид держится и прочее",
        "layer": "domain"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/domain/fish/Guild.kt",
        "title": "Гильдия: хищник или мирная",
        "layer": "domain"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/domain/knowledge/KnowledgeCatalog.kt",
        "title": "Словари знаний о водоёмах",
        "layer": "domain"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/domain/light/LightPhase.kt",
        "title": "Фаза света и профиль активности",
        "layer": "domain"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/domain/location/LocationTracker.kt",
        "title": "Трекер местоположения (контракт)",
        "layer": "domain"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/domain/season/SeasonPhase.kt",
        "title": "Фаза сезона вида",
        "layer": "domain"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/domain/sensor/PressureProvider.kt",
        "title": "Барометр устройства (контракт)",
        "layer": "domain"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/domain/sensor/PressureUnits.kt",
        "title": "гПа ↔ мм рт. ст.",
        "layer": "domain"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/domain/session/FishingStrategy.kt",
        "title": "План на выезд",
        "layer": "domain"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/domain/share/GpxParser.kt",
        "title": "Чтение точек из GPX",
        "layer": "domain"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/domain/share/GpxWriter.kt",
        "title": "Выгрузка точек в GPX",
        "layer": "domain"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/domain/share/RegionPack.kt",
        "title": "Пакет района: формат обмена",
        "layer": "domain"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/domain/share/RegionPackCodec.kt",
        "title": "Кодек пакета района",
        "layer": "domain"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/domain/water/Oxygen.kt",
        "title": "Кислород по температуре воды",
        "layer": "domain"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/domain/water/WaterState.kt",
        "title": "Ход воды в двух слоях",
        "layer": "domain"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/domain/water/WaterTemperature.kt",
        "title": "Модель температуры воды",
        "layer": "domain"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/domain/weather/DailyForecast.kt",
        "title": "Сводка дня из почасового прогноза",
        "layer": "domain"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/domain/weather/HourWindow.kt",
        "title": "Окно часов вокруг «сейчас»",
        "layer": "domain"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/domain/weather/PressureTrend.kt",
        "title": "Тенденция давления",
        "layer": "domain"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/domain/weather/WeatherCode.kt",
        "title": "Коды погоды WMO",
        "layer": "domain"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/domain/weather/Wind.kt",
        "title": "Ветер: единицы и румбы",
        "layer": "domain"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/FishForecastApp.kt",
        "title": "Application: Hilt, MapLibre, планировщик воркеров",
        "layer": "app"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/MainActivity.kt",
        "title": "Точка входа приложения",
        "layer": "app"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/ui/addeditfish/AddEditFishEvent.kt",
        "title": "События экрана правки рыбы",
        "layer": "ui"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/ui/addeditfish/AddEditFishScreen.kt",
        "title": "Экран добавления/правки рыбы",
        "layer": "ui"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/ui/addeditfish/AddEditFishViewModel.kt",
        "title": "ViewModel экрана правки рыбы",
        "layer": "ui"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/ui/bite/BiteChartAxis.kt",
        "title": "Разметка оси времени у графика активности",
        "layer": "ui"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/ui/bite/BiteScreen.kt",
        "title": "Экран «Рыбалка»",
        "layer": "ui"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/ui/bite/BiteViewModel.kt",
        "title": "ViewModel клёва",
        "layer": "ui"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/ui/common/ActiveMapBar.kt",
        "title": "Шапка экрана: название раздела, а под ним — выбранный район",
        "layer": "ui"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/ui/common/ActiveMapViewModel.kt",
        "title": "Выбор района для шапки",
        "layer": "ui"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/ui/common/NoActiveMap.kt",
        "title": "Заглушка «нет района»",
        "layer": "ui"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/ui/journal/JournalScreen.kt",
        "title": "Экран журнала трофеев",
        "layer": "ui"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/ui/journal/JournalViewModel.kt",
        "title": "ViewModel журнала",
        "layer": "ui"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/ui/map/MapConfig.kt",
        "title": "Источники тайлов и масштабы",
        "layer": "ui"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/ui/map/MapScreen.kt",
        "title": "Экран карты: район, точки, офлайн-области, базовый слой",
        "layer": "ui"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/ui/map/MapViewModel.kt",
        "title": "ViewModel карты: точки, области, моё местоположение",
        "layer": "ui"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/ui/map/ShareActions.kt",
        "title": "Отдача наружу: пакет района, GPX, снимок карты",
        "layer": "ui"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/ui/map/TileEstimate.kt",
        "title": "Оценка числа тайлов офлайн-области",
        "layer": "ui"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/ui/maps/MapSizeFormat.kt",
        "title": "Размер района словами",
        "layer": "ui"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/ui/maps/SavedMapsScreen.kt",
        "title": "Хранилище: районы, их глубины и тип водоёма, обмен файлами",
        "layer": "ui"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/ui/maps/SavedMapsViewModel.kt",
        "title": "ViewModel хранилища районов и обмена файлами",
        "layer": "ui"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/ui/navigation",
        "title": "Маршруты навигации",
        "layer": "ui"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/ui/navigation/Screen.kt",
        "title": "Маршруты навигации",
        "layer": "ui"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/ui/reference/FishSection.kt",
        "title": "Раздел «Виды»: справочник рыб, сопоставленный с сегодняшней водой",
        "layer": "ui"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/ui/reference/FishTerms.kt",
        "title": "Перевод словарных значений справочника на человеческий",
        "layer": "ui"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/ui/reference/KnowledgeSections.kt",
        "title": "Раздел «Водоёмы»: почему течение и размер вообще важны",
        "layer": "ui"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/ui/reference/ReferenceScreen.kt",
        "title": "Справочник — база знаний приложения",
        "layer": "ui"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/ui/reference/ReferenceViewModel.kt",
        "title": "ViewModel справочника: виды и словари против сегодняшней воды",
        "layer": "ui"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/ui/session",
        "title": "Экран и ViewModel сессии рыбалки",
        "layer": "ui"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/ui/session/FishingSessionViewModel.kt",
        "title": "ViewModel сборов и активного выезда",
        "layer": "ui"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/ui/session/SessionPanel.kt",
        "title": "Сборы: анкета и план",
        "layer": "ui"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/ui/theme/Color.kt",
        "title": "Палитра темы",
        "layer": "ui"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/ui/theme/Theme.kt",
        "title": "Тема Compose: светлая и тёмная схемы",
        "layer": "ui"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/ui/theme/Type.kt",
        "title": "Типографика",
        "layer": "ui"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherCharts.kt",
        "title": "Самодельные графики погоды",
        "layer": "ui"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherIcons.kt",
        "title": "Один значок на группу погоды: по нему колонка дня читается без текста",
        "layer": "ui"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherScreen.kt",
        "title": "Экран погоды: давление, вода, ветер, неделя",
        "layer": "ui"
      },
      {
        "id": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherViewModel.kt",
        "title": "ViewModel погоды выбранного района",
        "layer": "ui"
      },
      {
        "id": "app/src/test/java/com/example/fishforecast/data/local/entities/SavedMapEntityTest.kt",
        "title": "SavedMapEntityTest",
        "layer": "test"
      },
      {
        "id": "app/src/test/java/com/example/fishforecast/domain/alert/BiteAlertPolicyTest.kt",
        "title": "Политика уведомлений",
        "layer": "test"
      },
      {
        "id": "app/src/test/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCaseTest.kt",
        "title": "CalculateFishActivityUseCaseTest",
        "layer": "test"
      },
      {
        "id": "app/src/test/java/com/example/fishforecast/domain/bite/FactorCommentFormatTest.kt",
        "title": "Пояснения факторов — то, ради чего оценка вообще заслуживает доверия: без причины число проверить нельзя",
        "summary": "Пояснения факторов — то, ради чего оценка вообще заслуживает доверия: без причины число проверить нельзя. Поэтому у них один формат, и он проверяется так же, как арифметика.",
        "layer": "test"
      },
      {
        "id": "app/src/test/java/com/example/fishforecast/domain/bite/FindBiteWindowUseCaseTest.kt",
        "title": "FindBiteWindowUseCaseTest",
        "layer": "test"
      },
      {
        "id": "app/src/test/java/com/example/fishforecast/domain/bite/NormalPressureTest.kt",
        "title": "NormalPressureTest",
        "layer": "test"
      },
      {
        "id": "app/src/test/java/com/example/fishforecast/domain/bite/ObservationContextTest.kt",
        "title": "ObservationContextTest",
        "layer": "test"
      },
      {
        "id": "app/src/test/java/com/example/fishforecast/domain/bite/PlaceContextTest.kt",
        "title": "PlaceContextTest",
        "layer": "test"
      },
      {
        "id": "app/src/test/java/com/example/fishforecast/domain/bite/WeekActivityTest.kt",
        "title": "Свёртка недели по частям суток",
        "layer": "test"
      },
      {
        "id": "app/src/test/java/com/example/fishforecast/domain/fish/FishCatalogTest.kt",
        "title": "FishCatalogTest",
        "layer": "test"
      },
      {
        "id": "app/src/test/java/com/example/fishforecast/domain/knowledge/KnowledgeCatalogTest.kt",
        "title": "KnowledgeCatalogTest",
        "layer": "test"
      },
      {
        "id": "app/src/test/java/com/example/fishforecast/domain/light/LightPhaseTest.kt",
        "title": "LightPhaseTest",
        "layer": "test"
      },
      {
        "id": "app/src/test/java/com/example/fishforecast/domain/season/SeasonPhaseTest.kt",
        "title": "Фаза сезона",
        "layer": "test"
      },
      {
        "id": "app/src/test/java/com/example/fishforecast/domain/session/FishingStrategyTest.kt",
        "title": "FishingStrategyTest",
        "layer": "test"
      },
      {
        "id": "app/src/test/java/com/example/fishforecast/domain/session/SeasonalPlanTest.kt",
        "title": "План на выезд под вид и сезон",
        "layer": "test"
      },
      {
        "id": "app/src/test/java/com/example/fishforecast/domain/share/GpxParserTest.kt",
        "title": "GpxParserTest",
        "layer": "test"
      },
      {
        "id": "app/src/test/java/com/example/fishforecast/domain/share/GpxWriterTest.kt",
        "title": "GpxWriterTest",
        "layer": "test"
      },
      {
        "id": "app/src/test/java/com/example/fishforecast/domain/share/RegionPackCodecTest.kt",
        "title": "RegionPackCodecTest",
        "layer": "test"
      },
      {
        "id": "app/src/test/java/com/example/fishforecast/domain/water/OxygenTest.kt",
        "title": "OxygenTest",
        "layer": "test"
      },
      {
        "id": "app/src/test/java/com/example/fishforecast/domain/water/WaterBodyTest.kt",
        "title": "WaterBodyTest",
        "layer": "test"
      },
      {
        "id": "app/src/test/java/com/example/fishforecast/domain/water/WaterTemperatureTest.kt",
        "title": "WaterTemperatureTest",
        "layer": "test"
      },
      {
        "id": "app/src/test/java/com/example/fishforecast/domain/water/WaterTrendTest.kt",
        "title": "Ход воды на ближайшие часы",
        "layer": "test"
      },
      {
        "id": "app/src/test/java/com/example/fishforecast/domain/weather/DailyForecastTest.kt",
        "title": "DailyForecastTest",
        "layer": "test"
      },
      {
        "id": "app/src/test/java/com/example/fishforecast/domain/weather/HourWindowTest.kt",
        "title": "HourWindowTest",
        "layer": "test"
      },
      {
        "id": "app/src/test/java/com/example/fishforecast/domain/weather/PressureTrendTest.kt",
        "title": "PressureTrendTest",
        "layer": "test"
      },
      {
        "id": "app/src/test/java/com/example/fishforecast/domain/weather/WindTest.kt",
        "title": "WindTest",
        "layer": "test"
      },
      {
        "id": "app/src/test/java/com/example/fishforecast/ui/bite/BiteChartAxisTest.kt",
        "title": "BiteChartAxisTest",
        "layer": "test"
      },
      {
        "id": "app/src/test/java/com/example/fishforecast/ui/map/MapConfigTest.kt",
        "title": "MapConfigTest",
        "layer": "test"
      },
      {
        "id": "app/src/test/java/com/example/fishforecast/ui/map/TileEstimateTest.kt",
        "title": "TileEstimateTest",
        "layer": "test"
      },
      {
        "id": "app/src/test/java/com/example/fishforecast/ui/maps/MapSizeFormatTest.kt",
        "title": "MapSizeFormatTest",
        "layer": "test"
      }
    ],
    "imports": [
      {
        "from": "app/src/main/AndroidManifest.xml",
        "to": "app/src/main/java/com/example/fishforecast/FishForecastApp.kt",
        "evidence": {
          "path": "app/src/main/AndroidManifest.xml",
          "line": 11,
          "fragment": "android:name=\".FishForecastApp\""
        }
      },
      {
        "from": "app/src/main/AndroidManifest.xml",
        "to": "app/src/main/java/com/example/fishforecast/MainActivity.kt",
        "evidence": {
          "path": "app/src/main/AndroidManifest.xml",
          "line": 21,
          "fragment": "android:name=\".MainActivity\""
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/local/AlertStore.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/ActiveMapStore.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/local/AlertStore.kt",
          "line": 25,
          "fragment": "settings"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/local/AlertStore.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/alert/BiteAlertPolicy.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/local/AlertStore.kt",
          "line": 6,
          "fragment": "import com.example.fishforecast.domain.alert.AlertHistory"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/local/AppDatabase.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/dao/CatchDao.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/local/AppDatabase.kt",
          "line": 7,
          "fragment": "import com.example.fishforecast.data.local.dao.CatchDao"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/local/AppDatabase.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/dao/FishDao.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/local/AppDatabase.kt",
          "line": 8,
          "fragment": "import com.example.fishforecast.data.local.dao.FishDao"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/local/AppDatabase.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/dao/FishingSessionDao.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/local/AppDatabase.kt",
          "line": 9,
          "fragment": "import com.example.fishforecast.data.local.dao.FishingSessionDao"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/local/AppDatabase.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/dao/FishingSpotDao.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/local/AppDatabase.kt",
          "line": 10,
          "fragment": "import com.example.fishforecast.data.local.dao.FishingSpotDao"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/local/AppDatabase.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/dao/ObservationDao.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/local/AppDatabase.kt",
          "line": 11,
          "fragment": "import com.example.fishforecast.data.local.dao.ObservationDao"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/local/AppDatabase.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/dao/PressureLogDao.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/local/AppDatabase.kt",
          "line": 12,
          "fragment": "import com.example.fishforecast.data.local.dao.PressureLogDao"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/local/AppDatabase.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/dao/SavedMapDao.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/local/AppDatabase.kt",
          "line": 13,
          "fragment": "import com.example.fishforecast.data.local.dao.SavedMapDao"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/local/AppDatabase.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/dao/WeatherDao.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/local/AppDatabase.kt",
          "line": 14,
          "fragment": "import com.example.fishforecast.data.local.dao.WeatherDao"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/local/AppDatabase.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/CatchEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/local/AppDatabase.kt",
          "line": 15,
          "fragment": "import com.example.fishforecast.data.local.entities.CatchEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/local/AppDatabase.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/DailySunEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/local/AppDatabase.kt",
          "line": 16,
          "fragment": "import com.example.fishforecast.data.local.entities.DailySunEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/local/AppDatabase.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/local/AppDatabase.kt",
          "line": 17,
          "fragment": "import com.example.fishforecast.data.local.entities.FishEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/local/AppDatabase.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishingSessionEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/local/AppDatabase.kt",
          "line": 18,
          "fragment": "import com.example.fishforecast.data.local.entities.FishingSessionEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/local/AppDatabase.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishingSpotEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/local/AppDatabase.kt",
          "line": 19,
          "fragment": "import com.example.fishforecast.data.local.entities.FishingSpotEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/local/AppDatabase.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/ObservationEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/local/AppDatabase.kt",
          "line": 20,
          "fragment": "import com.example.fishforecast.data.local.entities.ObservationEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/local/AppDatabase.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/PressureLogEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/local/AppDatabase.kt",
          "line": 21,
          "fragment": "import com.example.fishforecast.data.local.entities.PressureLogEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/local/AppDatabase.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/SavedMapEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/local/AppDatabase.kt",
          "line": 22,
          "fragment": "import com.example.fishforecast.data.local.entities.SavedMapEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/local/AppDatabase.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/WeatherEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/local/AppDatabase.kt",
          "line": 23,
          "fragment": "import com.example.fishforecast.data.local.entities.WeatherEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/local/dao/CatchDao.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/CatchEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/local/dao/CatchDao.kt",
          "line": 8,
          "fragment": "import com.example.fishforecast.data.local.entities.CatchEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/local/dao/FishDao.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/local/dao/FishDao.kt",
          "line": 4,
          "fragment": "import com.example.fishforecast.data.local.entities.FishEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/local/dao/FishingSessionDao.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishingSessionEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/local/dao/FishingSessionDao.kt",
          "line": 9,
          "fragment": "import com.example.fishforecast.data.local.entities.FishingSessionEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/local/dao/FishingSpotDao.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishingSpotEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/local/dao/FishingSpotDao.kt",
          "line": 8,
          "fragment": "import com.example.fishforecast.data.local.entities.FishingSpotEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/local/dao/ObservationDao.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/ObservationEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/local/dao/ObservationDao.kt",
          "line": 8,
          "fragment": "import com.example.fishforecast.data.local.entities.ObservationEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/local/dao/PressureLogDao.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/PressureLogEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/local/dao/PressureLogDao.kt",
          "line": 7,
          "fragment": "import com.example.fishforecast.data.local.entities.PressureLogEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/local/dao/SavedMapDao.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/SavedMapEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/local/dao/SavedMapDao.kt",
          "line": 8,
          "fragment": "import com.example.fishforecast.data.local.entities.SavedMapEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/local/dao/WeatherDao.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/DailySunEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/local/dao/WeatherDao.kt",
          "line": 7,
          "fragment": "import com.example.fishforecast.data.local.entities.DailySunEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/local/dao/WeatherDao.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/WeatherEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/local/dao/WeatherDao.kt",
          "line": 8,
          "fragment": "import com.example.fishforecast.data.local.entities.WeatherEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/local/entities/CatchEntity.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/local/entities/CatchEntity.kt",
          "line": 20,
          "fragment": "entity = FishEntity::class,"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/local/entities/CatchEntity.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishingSpotEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/local/entities/CatchEntity.kt",
          "line": 26,
          "fragment": "entity = FishingSpotEntity::class,"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/local/entities/FishingSpotEntity.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/local/entities/FishingSpotEntity.kt",
          "line": 40,
          "fragment": "entity = FishEntity::class,"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/local/entities/FishingSpotEntity.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/SavedMapEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/local/entities/FishingSpotEntity.kt",
          "line": 46,
          "fragment": "SavedMapEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/location/DefaultLocationTracker.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/location/LocationTracker.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/location/DefaultLocationTracker.kt",
          "line": 10,
          "fragment": "import com.example.fishforecast.domain.location.LocationTracker"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/remote/WeatherApi.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/remote/dto/WeatherDto.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/remote/WeatherApi.kt",
          "line": 3,
          "fragment": "import com.example.fishforecast.data.remote.dto.PressureHistoryDto"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/BarometerRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/dao/PressureLogDao.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/BarometerRepository.kt",
          "line": 3,
          "fragment": "import com.example.fishforecast.data.local.dao.PressureLogDao"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/BarometerRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/PressureLogEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/BarometerRepository.kt",
          "line": 4,
          "fragment": "import com.example.fishforecast.data.local.entities.PressureLogEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/CatchRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/dao/CatchDao.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/CatchRepository.kt",
          "line": 4,
          "fragment": "import com.example.fishforecast.data.local.dao.CatchDao"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/CatchRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/dao/FishingSessionDao.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/CatchRepository.kt",
          "line": 7,
          "fragment": "import com.example.fishforecast.data.local.dao.FishingSessionDao"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/CatchRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/CatchEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/CatchRepository.kt",
          "line": 5,
          "fragment": "import com.example.fishforecast.data.local.entities.CatchEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/CatchRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/CatchRepository.kt",
          "line": 6,
          "fragment": "import com.example.fishforecast.data.local.entities.FishEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/CatchRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/repository/FishingContextRepository.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/CatchRepository.kt",
          "line": 25,
          "fragment": "private val fishingContext: FishingContextRepository,"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/CatchRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCase.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/CatchRepository.kt",
          "line": 8,
          "fragment": "import com.example.fishforecast.domain.bite.CalculateFishActivityUseCase"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/CatchRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/sensor/PressureUnits.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/CatchRepository.kt",
          "line": 9,
          "fragment": "import com.example.fishforecast.domain.sensor.hPaToMmHg"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/FishingContextRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/ActiveMapStore.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/FishingContextRepository.kt",
          "line": 3,
          "fragment": "import com.example.fishforecast.data.local.ActiveMapStore"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/FishingContextRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/dao/SavedMapDao.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/FishingContextRepository.kt",
          "line": 4,
          "fragment": "import com.example.fishforecast.data.local.dao.SavedMapDao"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/FishingContextRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/DailySunEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/FishingContextRepository.kt",
          "line": 5,
          "fragment": "import com.example.fishforecast.data.local.entities.DailySunEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/FishingContextRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishingSpotEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/FishingContextRepository.kt",
          "line": 6,
          "fragment": "import com.example.fishforecast.data.local.entities.FishingSpotEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/FishingContextRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/SavedMapEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/FishingContextRepository.kt",
          "line": 7,
          "fragment": "import com.example.fishforecast.data.local.entities.SavedMapEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/FishingContextRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/WeatherEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/FishingContextRepository.kt",
          "line": 8,
          "fragment": "import com.example.fishforecast.data.local.entities.WeatherEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/FishingContextRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/repository/FishingSpotRepository.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/FishingContextRepository.kt",
          "line": 48,
          "fragment": "private val spotRepository: FishingSpotRepository"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/FishingContextRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/repository/KnowledgeRepository.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/FishingContextRepository.kt",
          "line": 47,
          "fragment": "private val knowledgeRepository: KnowledgeRepository,"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/FishingContextRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/repository/WeatherRepository.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/FishingContextRepository.kt",
          "line": 46,
          "fragment": "private val weatherRepository: WeatherRepository,"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/FishingContextRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/bite/NormalPressure.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/FishingContextRepository.kt",
          "line": 9,
          "fragment": "import com.example.fishforecast.domain.bite.standardPressureMmHg"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/FishingContextRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/knowledge/KnowledgeCatalog.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/FishingContextRepository.kt",
          "line": 10,
          "fragment": "import com.example.fishforecast.domain.knowledge.KnowledgeCatalog"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/FishingContextRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/water/WaterState.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/FishingContextRepository.kt",
          "line": 12,
          "fragment": "import com.example.fishforecast.domain.water.WaterState"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/FishingContextRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/map/MapConfig.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/FishingContextRepository.kt",
          "line": 20,
          "fragment": "import com.example.fishforecast.ui.map.BaseLayer"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/FishingSessionRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/dao/FishingSessionDao.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/FishingSessionRepository.kt",
          "line": 3,
          "fragment": "import com.example.fishforecast.data.local.dao.FishingSessionDao"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/FishingSessionRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishingSessionEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/FishingSessionRepository.kt",
          "line": 4,
          "fragment": "import com.example.fishforecast.data.local.entities.FishingSessionEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/FishingSessionRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/repository/FishingContextRepository.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/FishingSessionRepository.kt",
          "line": 34,
          "fragment": "private val fishingContext: FishingContextRepository,"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/FishingSessionRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/repository/FishRepository.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/FishingSessionRepository.kt",
          "line": 36,
          "fragment": "private val fishRepository: FishRepository,"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/FishingSessionRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/repository/KnowledgeRepository.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/FishingSessionRepository.kt",
          "line": 35,
          "fragment": "private val knowledgeRepository: KnowledgeRepository,"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/FishingSessionRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCase.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/FishingSessionRepository.kt",
          "line": 5,
          "fragment": "import com.example.fishforecast.domain.bite.CalculateFishActivityUseCase"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/FishingSessionRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/bite/PlaceContext.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/FishingSessionRepository.kt",
          "line": 6,
          "fragment": "import com.example.fishforecast.domain.bite.PlaceContext"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/FishingSessionRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/light/LightPhase.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/FishingSessionRepository.kt",
          "line": 8,
          "fragment": "import com.example.fishforecast.domain.light.lightPhaseAt"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/FishingSessionRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/season/SeasonPhase.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/FishingSessionRepository.kt",
          "line": 12,
          "fragment": "import com.example.fishforecast.domain.season.seasonPhaseOf"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/FishingSessionRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/sensor/PressureUnits.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/FishingSessionRepository.kt",
          "line": 9,
          "fragment": "import com.example.fishforecast.domain.sensor.hPaToMmHg"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/FishingSessionRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/session/FishingStrategy.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/FishingSessionRepository.kt",
          "line": 10,
          "fragment": "import com.example.fishforecast.domain.session.FishingStrategy"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/FishingSessionRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/weather/Wind.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/FishingSessionRepository.kt",
          "line": 16,
          "fragment": "import com.example.fishforecast.domain.weather.kmhToMs"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/FishingSpotRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/dao/FishingSpotDao.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/FishingSpotRepository.kt",
          "line": 3,
          "fragment": "import com.example.fishforecast.data.local.dao.FishingSpotDao"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/FishingSpotRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishingSpotEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/FishingSpotRepository.kt",
          "line": 4,
          "fragment": "import com.example.fishforecast.data.local.entities.FishingSpotEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/FishRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/CatalogStore.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/FishRepository.kt",
          "line": 4,
          "fragment": "import com.example.fishforecast.data.local.CatalogStore"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/FishRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/dao/FishDao.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/FishRepository.kt",
          "line": 5,
          "fragment": "import com.example.fishforecast.data.local.dao.FishDao"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/FishRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/FishRepository.kt",
          "line": 6,
          "fragment": "import com.example.fishforecast.data.local.entities.FishEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/FishRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/remote/FishCatalogApi.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/FishRepository.kt",
          "line": 7,
          "fragment": "import com.example.fishforecast.data.remote.FishCatalogApi"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/FishRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/fish/FishCatalog.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/FishRepository.kt",
          "line": 8,
          "fragment": "import com.example.fishforecast.domain.fish.CatalogFish"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/KnowledgeRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/CatalogStore.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/KnowledgeRepository.kt",
          "line": 4,
          "fragment": "import com.example.fishforecast.data.local.CatalogStore"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/KnowledgeRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/remote/FishCatalogApi.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/KnowledgeRepository.kt",
          "line": 5,
          "fragment": "import com.example.fishforecast.data.remote.FishCatalogApi"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/KnowledgeRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/knowledge/KnowledgeCatalog.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/KnowledgeRepository.kt",
          "line": 6,
          "fragment": "import com.example.fishforecast.domain.knowledge.KnowledgeCatalog"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/ObservationRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/dao/ObservationDao.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/ObservationRepository.kt",
          "line": 3,
          "fragment": "import com.example.fishforecast.data.local.dao.ObservationDao"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/ObservationRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/ObservationEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/ObservationRepository.kt",
          "line": 4,
          "fragment": "import com.example.fishforecast.data.local.entities.ObservationEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/ObservationRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/repository/FishingContextRepository.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/ObservationRepository.kt",
          "line": 29,
          "fragment": "FishingContextRepository"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/ObservationRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/bite/ObservationContext.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/ObservationRepository.kt",
          "line": 5,
          "fragment": "import com.example.fishforecast.domain.bite.ActiveObservation"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/ObservationRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/knowledge/KnowledgeCatalog.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/ObservationRepository.kt",
          "line": 6,
          "fragment": "import com.example.fishforecast.domain.knowledge.KnowledgeCatalog"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/OfflineMapRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/dao/SavedMapDao.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/OfflineMapRepository.kt",
          "line": 4,
          "fragment": "import com.example.fishforecast.data.local.dao.SavedMapDao"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/OfflineMapRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/SavedMapEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/OfflineMapRepository.kt",
          "line": 5,
          "fragment": "import com.example.fishforecast.data.local.entities.SavedMapEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/OfflineMapRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/map/MapConfig.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/OfflineMapRepository.kt",
          "line": 6,
          "fragment": "import com.example.fishforecast.ui.map.MapConfig"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/RegionPackRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/ActiveMapStore.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/RegionPackRepository.kt",
          "line": 5,
          "fragment": "import com.example.fishforecast.data.local.ActiveMapStore"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/RegionPackRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/dao/FishDao.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/RegionPackRepository.kt",
          "line": 6,
          "fragment": "import com.example.fishforecast.data.local.dao.FishDao"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/RegionPackRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/dao/FishingSpotDao.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/RegionPackRepository.kt",
          "line": 7,
          "fragment": "import com.example.fishforecast.data.local.dao.FishingSpotDao"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/RegionPackRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/dao/SavedMapDao.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/RegionPackRepository.kt",
          "line": 8,
          "fragment": "import com.example.fishforecast.data.local.dao.SavedMapDao"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/RegionPackRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/RegionPackRepository.kt",
          "line": 9,
          "fragment": "import com.example.fishforecast.data.local.entities.FishEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/RegionPackRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishingSpotEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/RegionPackRepository.kt",
          "line": 10,
          "fragment": "import com.example.fishforecast.data.local.entities.FishingSpotEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/RegionPackRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/share/RegionPack.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/RegionPackRepository.kt",
          "line": 11,
          "fragment": "import com.example.fishforecast.domain.share.PackAuthor"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/RegionPackRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/share/RegionPackCodec.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/RegionPackRepository.kt",
          "line": 13,
          "fragment": "import com.example.fishforecast.domain.share.RegionPackCodec"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/WeatherRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/dao/WeatherDao.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/WeatherRepository.kt",
          "line": 3,
          "fragment": "import com.example.fishforecast.data.local.dao.WeatherDao"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/WeatherRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/DailySunEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/WeatherRepository.kt",
          "line": 4,
          "fragment": "import com.example.fishforecast.data.local.entities.DailySunEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/WeatherRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/WeatherEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/WeatherRepository.kt",
          "line": 5,
          "fragment": "import com.example.fishforecast.data.local.entities.WeatherEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/WeatherRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/remote/WeatherApi.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/WeatherRepository.kt",
          "line": 6,
          "fragment": "import com.example.fishforecast.data.remote.WeatherApi"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/WeatherRepository.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/bite/NormalPressure.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/WeatherRepository.kt",
          "line": 7,
          "fragment": "import com.example.fishforecast.domain.bite.averagePressureMmHg"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/sensor/BarometerPressureProvider.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/sensor/PressureProvider.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/sensor/BarometerPressureProvider.kt",
          "line": 8,
          "fragment": "import com.example.fishforecast.domain.sensor.PressureProvider"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/worker/BiteAlertWorker.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/AlertStore.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/worker/BiteAlertWorker.kt",
          "line": 22,
          "fragment": "import com.example.fishforecast.data.local.AlertStore"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/worker/BiteAlertWorker.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/DailySunEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/worker/BiteAlertWorker.kt",
          "line": 25,
          "fragment": "import com.example.fishforecast.data.local.entities.DailySunEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/worker/BiteAlertWorker.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/worker/BiteAlertWorker.kt",
          "line": 26,
          "fragment": "import com.example.fishforecast.data.local.entities.FishEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/worker/BiteAlertWorker.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/SavedMapEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/worker/BiteAlertWorker.kt",
          "line": 27,
          "fragment": "import com.example.fishforecast.data.local.entities.SavedMapEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/worker/BiteAlertWorker.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/WeatherEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/worker/BiteAlertWorker.kt",
          "line": 28,
          "fragment": "import com.example.fishforecast.data.local.entities.WeatherEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/worker/BiteAlertWorker.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/repository",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/worker/BiteAlertWorker.kt",
          "line": 23,
          "fragment": "data.repository.FishRepository"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/worker/BiteAlertWorker.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/repository/FishingContextRepository.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/worker/BiteAlertWorker.kt",
          "line": 24,
          "fragment": "import com.example.fishforecast.data.repository.FishingContextRepository"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/worker/BiteAlertWorker.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/repository/FishRepository.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/worker/BiteAlertWorker.kt",
          "line": 23,
          "fragment": "import com.example.fishforecast.data.repository.FishRepository"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/worker/BiteAlertWorker.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/alert/BiteAlertPolicy.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/worker/BiteAlertWorker.kt",
          "line": 29,
          "fragment": "import com.example.fishforecast.domain.alert.AlertDecision"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/worker/BiteAlertWorker.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/bite/FindBiteWindowUseCase.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/worker/BiteAlertWorker.kt",
          "line": 33,
          "fragment": "domain.bite.FindBiteWindowUseCase"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/worker/BiteAlertWorker.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/location/LocationTracker.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/worker/BiteAlertWorker.kt",
          "line": 34,
          "fragment": "import com.example.fishforecast.domain.location.LocationTracker"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/worker/BiteAlertWorker.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/water/WaterState.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/worker/BiteAlertWorker.kt",
          "line": 35,
          "fragment": "import com.example.fishforecast.domain.water.WaterState"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/worker/BiteAlertWorker.kt",
        "to": "app/src/main/java/com/example/fishforecast/MainActivity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/worker/BiteAlertWorker.kt",
          "line": 20,
          "fragment": "com.example.fishforecast.MainActivity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/worker/WeatherSyncWorker.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/repository",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/worker/WeatherSyncWorker.kt",
          "line": 12,
          "fragment": "data.repository.FishingContextRepository"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/worker/WeatherSyncWorker.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/repository/FishingContextRepository.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/worker/WeatherSyncWorker.kt",
          "line": 12,
          "fragment": "import com.example.fishforecast.data.repository.FishingContextRepository"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/worker/WeatherSyncWorker.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/repository/WeatherRepository.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/worker/WeatherSyncWorker.kt",
          "line": 13,
          "fragment": "import com.example.fishforecast.data.repository.WeatherRepository"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/di/DatabaseModule.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/di/DatabaseModule.kt",
          "line": 5,
          "fragment": "data.local.AppDatabase"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/di/DatabaseModule.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/AppDatabase.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/di/DatabaseModule.kt",
          "line": 5,
          "fragment": "import com.example.fishforecast.data.local.AppDatabase"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/di/DatabaseModule.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/dao",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/di/DatabaseModule.kt",
          "line": 7,
          "fragment": "data.local.dao.FishDao"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/di/DatabaseModule.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/dao/CatchDao.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/di/DatabaseModule.kt",
          "line": 6,
          "fragment": "import com.example.fishforecast.data.local.dao.CatchDao"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/di/DatabaseModule.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/dao/FishDao.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/di/DatabaseModule.kt",
          "line": 7,
          "fragment": "import com.example.fishforecast.data.local.dao.FishDao"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/di/DatabaseModule.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/dao/FishingSessionDao.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/di/DatabaseModule.kt",
          "line": 9,
          "fragment": "import com.example.fishforecast.data.local.dao.FishingSessionDao"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/di/DatabaseModule.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/dao/FishingSpotDao.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/di/DatabaseModule.kt",
          "line": 8,
          "fragment": "import com.example.fishforecast.data.local.dao.FishingSpotDao"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/di/DatabaseModule.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/dao/ObservationDao.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/di/DatabaseModule.kt",
          "line": 10,
          "fragment": "import com.example.fishforecast.data.local.dao.ObservationDao"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/di/DatabaseModule.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/dao/PressureLogDao.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/di/DatabaseModule.kt",
          "line": 11,
          "fragment": "import com.example.fishforecast.data.local.dao.PressureLogDao"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/di/DatabaseModule.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/dao/SavedMapDao.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/di/DatabaseModule.kt",
          "line": 12,
          "fragment": "import com.example.fishforecast.data.local.dao.SavedMapDao"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/di/DatabaseModule.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/dao/WeatherDao.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/di/DatabaseModule.kt",
          "line": 13,
          "fragment": "import com.example.fishforecast.data.local.dao.WeatherDao"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/di/LocationModule.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/location",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/di/LocationModule.kt",
          "line": 4,
          "fragment": "data.location.DefaultLocationTracker"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/di/LocationModule.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/location/DefaultLocationTracker.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/di/LocationModule.kt",
          "line": 4,
          "fragment": "import com.example.fishforecast.data.location.DefaultLocationTracker"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/di/LocationModule.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/location/LocationTracker.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/di/LocationModule.kt",
          "line": 5,
          "fragment": "domain.location.LocationTracker"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/di/NetworkModule.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/remote",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/di/NetworkModule.kt",
          "line": 4,
          "fragment": "data.remote.WeatherApi"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/di/NetworkModule.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/remote/FishCatalogApi.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/di/NetworkModule.kt",
          "line": 3,
          "fragment": "import com.example.fishforecast.data.remote.FishCatalogApi"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/di/NetworkModule.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/remote/WeatherApi.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/di/NetworkModule.kt",
          "line": 4,
          "fragment": "import com.example.fishforecast.data.remote.WeatherApi"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/di/SensorModule.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/sensor",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/di/SensorModule.kt",
          "line": 3,
          "fragment": "data.sensor.BarometerPressureProvider"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/di/SensorModule.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/sensor/BarometerPressureProvider.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/di/SensorModule.kt",
          "line": 3,
          "fragment": "import com.example.fishforecast.data.sensor.BarometerPressureProvider"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/di/SensorModule.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/sensor/PressureProvider.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/di/SensorModule.kt",
          "line": 4,
          "fragment": "domain.sensor.PressureProvider"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/alert/BiteAlertPolicy.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/alert/BiteAlertPolicy.kt",
          "line": 3,
          "fragment": "import com.example.fishforecast.data.local.entities.FishEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCase.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCase.kt",
          "line": 3,
          "fragment": "data.local.entities.FishEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCase.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/DailySunEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCase.kt",
          "line": 5,
          "fragment": "import com.example.fishforecast.data.local.entities.DailySunEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCase.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCase.kt",
          "line": 3,
          "fragment": "import com.example.fishforecast.data.local.entities.FishEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCase.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/WeatherEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCase.kt",
          "line": 4,
          "fragment": "import com.example.fishforecast.data.local.entities.WeatherEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCase.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/bite/BiteForecast.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCase.kt",
          "line": 177,
          "fragment": "BiteForecast("
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCase.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/bite/ObservationContext.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCase.kt",
          "line": 59,
          "fragment": "ActiveObservation"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCase.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/bite/PlaceContext.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCase.kt",
          "line": 58,
          "fragment": "place: PlaceContext = PlaceContext()"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCase.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/fish/FishCatalog.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCase.kt",
          "line": 7,
          "fragment": "domain.fish.decodeLightActivity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCase.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/fish/Guild.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCase.kt",
          "line": 6,
          "fragment": "domain.fish.Guild"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCase.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/light/LightPhase.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCase.kt",
          "line": 8,
          "fragment": "domain.light.lightActivity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCase.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/season/SeasonPhase.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCase.kt",
          "line": 10,
          "fragment": "import com.example.fishforecast.domain.season.SeasonPhase"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCase.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/sensor/PressureUnits.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCase.kt",
          "line": 13,
          "fragment": "domain.sensor.hPaToMmHg"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCase.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/water/Oxygen.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCase.kt",
          "line": 22,
          "fragment": "domain.water.oxygenLevel"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCase.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/water/WaterState.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCase.kt",
          "line": 20,
          "fragment": "domain.water.WaterState"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCase.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/weather/Wind.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCase.kt",
          "line": 15,
          "fragment": "domain.weather.kmhToMs"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/bite/FindBiteWindowUseCase.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/bite/FindBiteWindowUseCase.kt",
          "line": 3,
          "fragment": "data.local.entities.FishEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/bite/FindBiteWindowUseCase.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/DailySunEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/bite/FindBiteWindowUseCase.kt",
          "line": 4,
          "fragment": "import com.example.fishforecast.data.local.entities.DailySunEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/bite/FindBiteWindowUseCase.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/bite/FindBiteWindowUseCase.kt",
          "line": 3,
          "fragment": "import com.example.fishforecast.data.local.entities.FishEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/bite/FindBiteWindowUseCase.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/WeatherEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/bite/FindBiteWindowUseCase.kt",
          "line": 5,
          "fragment": "import com.example.fishforecast.data.local.entities.WeatherEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/bite/FindBiteWindowUseCase.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/bite/BiteForecast.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/bite/FindBiteWindowUseCase.kt",
          "line": 13,
          "fragment": "BiteForecast"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/bite/FindBiteWindowUseCase.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCase.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/bite/FindBiteWindowUseCase.kt",
          "line": 24,
          "fragment": "calculateFishActivity: CalculateFishActivityUseCase"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/bite/FindBiteWindowUseCase.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/water/WaterState.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/bite/FindBiteWindowUseCase.kt",
          "line": 6,
          "fragment": "domain.water.WaterState"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/bite/NormalPressure.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/sensor/PressureUnits.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/bite/NormalPressure.kt",
          "line": 3,
          "fragment": "domain.sensor.hPaToMmHg"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/bite/ObservationContext.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/bite/BiteForecast.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/bite/ObservationContext.kt",
          "line": 36,
          "fragment": "BiteFactor"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/bite/ObservationContext.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/fish/Guild.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/bite/ObservationContext.kt",
          "line": 3,
          "fragment": "import com.example.fishforecast.domain.fish.Guild"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/bite/ObservationContext.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/knowledge/KnowledgeCatalog.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/bite/ObservationContext.kt",
          "line": 4,
          "fragment": "import com.example.fishforecast.domain.knowledge.ObservationType"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/bite/PlaceContext.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishingSpotEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/bite/PlaceContext.kt",
          "line": 3,
          "fragment": "import com.example.fishforecast.data.local.entities.FishingSpotEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/bite/PlaceContext.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/fish/FishCatalog.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/bite/PlaceContext.kt",
          "line": 5,
          "fragment": "import com.example.fishforecast.domain.fish.decodeBaits"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/bite/PlaceContext.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/fish/Guild.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/bite/PlaceContext.kt",
          "line": 4,
          "fragment": "domain.fish.Guild"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/bite/PlaceContext.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/knowledge/KnowledgeCatalog.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/bite/PlaceContext.kt",
          "line": 7,
          "fragment": "domain.knowledge.StructureType"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/bite/WeekActivity.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/bite/BiteForecast.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/bite/WeekActivity.kt",
          "line": 44,
          "fragment": "BiteLevel"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/fish/FishCatalog.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/fish/FishCatalog.kt",
          "line": 3,
          "fragment": "data.local.entities.FishEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/fish/FishCatalog.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/fish/FishCatalog.kt",
          "line": 3,
          "fragment": "import com.example.fishforecast.data.local.entities.FishEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/light/LightPhase.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/light/LightPhase.kt",
          "line": 3,
          "fragment": "data.local.entities.DailySunEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/light/LightPhase.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/DailySunEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/light/LightPhase.kt",
          "line": 3,
          "fragment": "import com.example.fishforecast.data.local.entities.DailySunEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/light/LightPhase.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/fish/Guild.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/light/LightPhase.kt",
          "line": 4,
          "fragment": "domain.fish.Guild"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/season/SeasonPhase.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/season/SeasonPhase.kt",
          "line": 3,
          "fragment": "import com.example.fishforecast.data.local.entities.FishEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/season/SeasonPhase.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/water/WaterTemperature.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/season/SeasonPhase.kt",
          "line": 4,
          "fragment": "import com.example.fishforecast.domain.water.WaterHour"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/session/FishingStrategy.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/session/FishingStrategy.kt",
          "line": 3,
          "fragment": "data.local.entities.FishEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/session/FishingStrategy.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/session/FishingStrategy.kt",
          "line": 3,
          "fragment": "import com.example.fishforecast.data.local.entities.FishEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/session/FishingStrategy.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/WeatherEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/session/FishingStrategy.kt",
          "line": 4,
          "fragment": "import com.example.fishforecast.data.local.entities.WeatherEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/session/FishingStrategy.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/bite/BiteForecast.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/session/FishingStrategy.kt",
          "line": 5,
          "fragment": "domain.bite.BiteForecast"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/session/FishingStrategy.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/bite/PlaceContext.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/session/FishingStrategy.kt",
          "line": 6,
          "fragment": "domain.bite.WaterLayerChoice"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/session/FishingStrategy.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/fish/FishCatalog.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/session/FishingStrategy.kt",
          "line": 8,
          "fragment": "domain.fish.decodeBaits"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/session/FishingStrategy.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/fish/FishWords.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/session/FishingStrategy.kt",
          "line": 16,
          "fragment": "import com.example.fishforecast.domain.fish.flavorText"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/session/FishingStrategy.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/fish/Guild.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/session/FishingStrategy.kt",
          "line": 7,
          "fragment": "domain.fish.Guild"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/session/FishingStrategy.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/knowledge/KnowledgeCatalog.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/session/FishingStrategy.kt",
          "line": 12,
          "fragment": "domain.knowledge.KnowledgeCatalog"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/session/FishingStrategy.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/light/LightPhase.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/session/FishingStrategy.kt",
          "line": 19,
          "fragment": "domain.light.LightPhase"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/session/FishingStrategy.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/season/SeasonPhase.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/session/FishingStrategy.kt",
          "line": 20,
          "fragment": "import com.example.fishforecast.domain.season.SeasonPhase"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/session/FishingStrategy.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/water/Oxygen.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/session/FishingStrategy.kt",
          "line": 22,
          "fragment": "domain.water.oxygenLevel"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/share/GpxParser.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/share/GpxParser.kt",
          "line": 3,
          "fragment": "data.local.entities.FishingSpotEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/share/GpxParser.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishingSpotEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/share/GpxParser.kt",
          "line": 3,
          "fragment": "import com.example.fishforecast.data.local.entities.FishingSpotEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/share/GpxWriter.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/share/GpxWriter.kt",
          "line": 3,
          "fragment": "data.local.entities.FishingSpotEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/share/GpxWriter.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishingSpotEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/share/GpxWriter.kt",
          "line": 3,
          "fragment": "import com.example.fishforecast.data.local.entities.FishingSpotEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/share/RegionPack.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/fish/FishCatalog.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/share/RegionPack.kt",
          "line": 3,
          "fragment": "domain.fish.CatalogFish"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/share/RegionPackCodec.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/share/RegionPackCodec.kt",
          "line": 5,
          "fragment": "data.local.entities.SavedMapEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/share/RegionPackCodec.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/share/RegionPackCodec.kt",
          "line": 3,
          "fragment": "import com.example.fishforecast.data.local.entities.FishEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/share/RegionPackCodec.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishingSpotEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/share/RegionPackCodec.kt",
          "line": 4,
          "fragment": "import com.example.fishforecast.data.local.entities.FishingSpotEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/share/RegionPackCodec.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/SavedMapEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/share/RegionPackCodec.kt",
          "line": 5,
          "fragment": "import com.example.fishforecast.data.local.entities.SavedMapEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/share/RegionPackCodec.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/fish/FishCatalog.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/share/RegionPackCodec.kt",
          "line": 8,
          "fragment": "domain.fish.toCatalogFish"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/share/RegionPackCodec.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/share/RegionPack.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/share/RegionPackCodec.kt",
          "line": 34,
          "fragment": "RegionPack.serializer()"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/water/Oxygen.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/knowledge/KnowledgeCatalog.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/water/Oxygen.kt",
          "line": 3,
          "fragment": "domain.knowledge.WaterBodyType"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/water/WaterState.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/water/WaterState.kt",
          "line": 3,
          "fragment": "data.local.entities.SavedMapEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/water/WaterState.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/SavedMapEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/water/WaterState.kt",
          "line": 3,
          "fragment": "import com.example.fishforecast.data.local.entities.SavedMapEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/water/WaterState.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/WeatherEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/water/WaterState.kt",
          "line": 4,
          "fragment": "import com.example.fishforecast.data.local.entities.WeatherEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/water/WaterState.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/bite/PlaceContext.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/water/WaterState.kt",
          "line": 5,
          "fragment": "domain.bite.WaterLayerChoice"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/water/WaterState.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/knowledge/KnowledgeCatalog.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/water/WaterState.kt",
          "line": 6,
          "fragment": "domain.knowledge.WaterBodyType"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/water/WaterState.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/water/Oxygen.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/water/WaterState.kt",
          "line": 103,
          "fragment": "hour.time to availableOxygenMgL("
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/water/WaterState.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/water/WaterTemperature.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/water/WaterState.kt",
          "line": 84,
          "fragment": "simulateWaterTemperature(forecast, shallowLayer, anchor, waterBody)"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/water/WaterState.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/weather/Wind.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/water/WaterState.kt",
          "line": 7,
          "fragment": "domain.weather.kmhToMs"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/water/WaterTemperature.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/water/WaterTemperature.kt",
          "line": 3,
          "fragment": "data.local.entities.WeatherEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/water/WaterTemperature.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/WeatherEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/water/WaterTemperature.kt",
          "line": 3,
          "fragment": "import com.example.fishforecast.data.local.entities.WeatherEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/water/WaterTemperature.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/knowledge/KnowledgeCatalog.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/water/WaterTemperature.kt",
          "line": 4,
          "fragment": "domain.knowledge.WaterBodyType"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/water/WaterTemperature.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/weather/Wind.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/water/WaterTemperature.kt",
          "line": 5,
          "fragment": "domain.weather.kmhToMs"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/weather/DailyForecast.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/weather/DailyForecast.kt",
          "line": 3,
          "fragment": "data.local.entities.WeatherEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/weather/DailyForecast.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/WeatherEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/weather/DailyForecast.kt",
          "line": 3,
          "fragment": "import com.example.fishforecast.data.local.entities.WeatherEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/weather/DailyForecast.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/sensor/PressureUnits.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/weather/DailyForecast.kt",
          "line": 4,
          "fragment": "domain.sensor.hPaToMmHg"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/weather/DailyForecast.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/weather/WeatherCode.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/weather/DailyForecast.kt",
          "line": 28,
          "fragment": "skyOf(weatherCode)"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/weather/PressureTrend.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/weather/PressureTrend.kt",
          "line": 3,
          "fragment": "data.local.entities.WeatherEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/weather/PressureTrend.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/WeatherEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/weather/PressureTrend.kt",
          "line": 3,
          "fragment": "import com.example.fishforecast.data.local.entities.WeatherEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/weather/PressureTrend.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/sensor/PressureUnits.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/weather/PressureTrend.kt",
          "line": 4,
          "fragment": "domain.sensor.hPaToMmHg"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/FishForecastApp.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/worker/BiteAlertWorker.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/FishForecastApp.kt",
          "line": 6,
          "fragment": "import com.example.fishforecast.data.worker.BiteAlertWorker"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/FishForecastApp.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/worker/WeatherSyncWorker.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/FishForecastApp.kt",
          "line": 7,
          "fragment": "import com.example.fishforecast.data.worker.WeatherSyncWorker"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/MainActivity.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/addeditfish/AddEditFishScreen.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/MainActivity.kt",
          "line": 36,
          "fragment": "import com.example.fishforecast.ui.addeditfish.AddEditFishScreen"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/MainActivity.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/bite/BiteScreen.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/MainActivity.kt",
          "line": 39,
          "fragment": "import com.example.fishforecast.ui.bite.BiteScreen"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/MainActivity.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/journal/JournalScreen.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/MainActivity.kt",
          "line": 40,
          "fragment": "import com.example.fishforecast.ui.journal.JournalScreen"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/MainActivity.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/map/MapScreen.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/MainActivity.kt",
          "line": 42,
          "fragment": "import com.example.fishforecast.ui.map.MapScreen"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/MainActivity.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/maps/SavedMapsScreen.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/MainActivity.kt",
          "line": 41,
          "fragment": "import com.example.fishforecast.ui.maps.SavedMapsScreen"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/MainActivity.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/navigation/Screen.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/MainActivity.kt",
          "line": 38,
          "fragment": "import com.example.fishforecast.ui.navigation.AddEditFishRoute"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/MainActivity.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/reference/ReferenceScreen.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/MainActivity.kt",
          "line": 37,
          "fragment": "import com.example.fishforecast.ui.reference.ReferenceScreen"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/MainActivity.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/theme/Theme.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/MainActivity.kt",
          "line": 49,
          "fragment": "import com.example.fishforecast.ui.theme.FishForecastTheme"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/MainActivity.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherScreen.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/MainActivity.kt",
          "line": 50,
          "fragment": "import com.example.fishforecast.ui.weather.WeatherScreen"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/addeditfish/AddEditFishScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/addeditfish/AddEditFishEvent.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/addeditfish/AddEditFishScreen.kt",
          "line": 61,
          "fragment": "AddEditFishEvent.SaveFish"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/addeditfish/AddEditFishScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/addeditfish/AddEditFishViewModel.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/addeditfish/AddEditFishScreen.kt",
          "line": 21,
          "fragment": "viewModel: AddEditFishViewModel = hiltViewModel()"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/addeditfish/AddEditFishViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/addeditfish/AddEditFishViewModel.kt",
          "line": 9,
          "fragment": "data.local.entities.FishEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/addeditfish/AddEditFishViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/addeditfish/AddEditFishViewModel.kt",
          "line": 9,
          "fragment": "import com.example.fishforecast.data.local.entities.FishEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/addeditfish/AddEditFishViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/repository",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/addeditfish/AddEditFishViewModel.kt",
          "line": 11,
          "fragment": "data.repository.FishRepository"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/addeditfish/AddEditFishViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/repository/FishRepository.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/addeditfish/AddEditFishViewModel.kt",
          "line": 11,
          "fragment": "import com.example.fishforecast.data.repository.FishRepository"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/addeditfish/AddEditFishViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/addeditfish/AddEditFishEvent.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/addeditfish/AddEditFishViewModel.kt",
          "line": 79,
          "fragment": "AddEditFishEvent"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/addeditfish/AddEditFishViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/navigation",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/addeditfish/AddEditFishViewModel.kt",
          "line": 10,
          "fragment": "ui.navigation.AddEditFishRoute"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/addeditfish/AddEditFishViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/navigation/Screen.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/addeditfish/AddEditFishViewModel.kt",
          "line": 10,
          "fragment": "import com.example.fishforecast.ui.navigation.AddEditFishRoute"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/bite/BiteScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/ObservationEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/bite/BiteScreen.kt",
          "line": 60,
          "fragment": "import com.example.fishforecast.data.local.entities.ObservationEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/bite/BiteScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/bite/BiteForecast.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/bite/BiteScreen.kt",
          "line": 66,
          "fragment": "domain.bite.BiteForecast"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/bite/BiteScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/bite/WeekActivity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/bite/BiteScreen.kt",
          "line": 72,
          "fragment": "import com.example.fishforecast.domain.bite.PartOfDay"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/bite/BiteScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/knowledge/KnowledgeCatalog.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/bite/BiteScreen.kt",
          "line": 61,
          "fragment": "import com.example.fishforecast.domain.knowledge.ObservationType"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/bite/BiteScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/bite/BiteChartAxis.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/bite/BiteScreen.kt",
          "line": 559,
          "fragment": "chartTicks"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/bite/BiteScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/bite/BiteViewModel.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/bite/BiteScreen.kt",
          "line": 87,
          "fragment": "viewModel: BiteViewModel = hiltViewModel()"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/bite/BiteScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/common/ActiveMapBar.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/bite/BiteScreen.kt",
          "line": 69,
          "fragment": "import com.example.fishforecast.ui.common.ActiveMapTitle"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/bite/BiteScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/common/NoActiveMap.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/bite/BiteScreen.kt",
          "line": 67,
          "fragment": "ui.common.NoActiveMapMessage"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/bite/BiteScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/session",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/bite/BiteScreen.kt",
          "line": 33,
          "fragment": "ui.session.FishingSessionViewModel"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/bite/BiteScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/session/FishingSessionViewModel.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/bite/BiteScreen.kt",
          "line": 33,
          "fragment": "import com.example.fishforecast.ui.session.FishingSessionViewModel"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/bite/BiteScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/session/SessionPanel.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/bite/BiteScreen.kt",
          "line": 32,
          "fragment": "import com.example.fishforecast.ui.session.ActiveSession"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/bite/BiteViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/bite/BiteViewModel.kt",
          "line": 8,
          "fragment": "data.local.entities.FishEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/bite/BiteViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/DailySunEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/bite/BiteViewModel.kt",
          "line": 7,
          "fragment": "import com.example.fishforecast.data.local.entities.DailySunEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/bite/BiteViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/bite/BiteViewModel.kt",
          "line": 8,
          "fragment": "import com.example.fishforecast.data.local.entities.FishEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/bite/BiteViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishingSpotEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/bite/BiteViewModel.kt",
          "line": 9,
          "fragment": "import com.example.fishforecast.data.local.entities.FishingSpotEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/bite/BiteViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/ObservationEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/bite/BiteViewModel.kt",
          "line": 16,
          "fragment": "import com.example.fishforecast.data.local.entities.ObservationEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/bite/BiteViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/SavedMapEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/bite/BiteViewModel.kt",
          "line": 10,
          "fragment": "import com.example.fishforecast.data.local.entities.SavedMapEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/bite/BiteViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/repository",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/bite/BiteViewModel.kt",
          "line": 12,
          "fragment": "data.repository.FishingContextRepository"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/bite/BiteViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/repository/FishingContextRepository.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/bite/BiteViewModel.kt",
          "line": 12,
          "fragment": "import com.example.fishforecast.data.repository.FishingContextRepository"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/bite/BiteViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/repository/FishRepository.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/bite/BiteViewModel.kt",
          "line": 11,
          "fragment": "import com.example.fishforecast.data.repository.FishRepository"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/bite/BiteViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/repository/KnowledgeRepository.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/bite/BiteViewModel.kt",
          "line": 17,
          "fragment": "import com.example.fishforecast.data.repository.KnowledgeRepository"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/bite/BiteViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/repository/ObservationRepository.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/bite/BiteViewModel.kt",
          "line": 18,
          "fragment": "import com.example.fishforecast.data.repository.ObservationRepository"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/bite/BiteViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/bite/BiteForecast.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/bite/BiteViewModel.kt",
          "line": 13,
          "fragment": "domain.bite.BiteForecast"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/bite/BiteViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCase.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/bite/BiteViewModel.kt",
          "line": 21,
          "fragment": "domain.bite.CalculateFishActivityUseCase"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/bite/BiteViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/bite/PlaceContext.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/bite/BiteViewModel.kt",
          "line": 22,
          "fragment": "import com.example.fishforecast.domain.bite.WaterLayerChoice"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/bite/BiteViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/bite/WeekActivity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/bite/BiteViewModel.kt",
          "line": 14,
          "fragment": "import com.example.fishforecast.domain.bite.DayActivity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/bite/BiteViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/knowledge/KnowledgeCatalog.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/bite/BiteViewModel.kt",
          "line": 20,
          "fragment": "import com.example.fishforecast.domain.knowledge.ObservationType"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/bite/BiteViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/water/WaterState.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/bite/BiteViewModel.kt",
          "line": 25,
          "fragment": "import com.example.fishforecast.domain.water.WaterState"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/bite/BiteViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/weather/HourWindow.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/bite/BiteViewModel.kt",
          "line": 26,
          "fragment": "domain.weather.hourWindow"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/bite/BiteViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/bite/BiteScreen.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/bite/BiteViewModel.kt",
          "line": 124,
          "fragment": "HOURS_BACK"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/common/ActiveMapBar.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/common/ActiveMapViewModel.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/common/ActiveMapBar.kt",
          "line": 48,
          "fragment": "ActiveMapViewModel"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/common/ActiveMapViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/SavedMapEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/common/ActiveMapViewModel.kt",
          "line": 5,
          "fragment": "import com.example.fishforecast.data.local.entities.SavedMapEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/common/ActiveMapViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/repository/FishingContextRepository.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/common/ActiveMapViewModel.kt",
          "line": 6,
          "fragment": "import com.example.fishforecast.data.repository.FishingContextRepository"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/journal/JournalScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/journal/JournalScreen.kt",
          "line": 59,
          "fragment": "data.local.entities.CatchEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/journal/JournalScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/CatchEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/journal/JournalScreen.kt",
          "line": 59,
          "fragment": "import com.example.fishforecast.data.local.entities.CatchEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/journal/JournalScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/journal/JournalScreen.kt",
          "line": 60,
          "fragment": "import com.example.fishforecast.data.local.entities.FishEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/journal/JournalScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishingSessionEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/journal/JournalScreen.kt",
          "line": 21,
          "fragment": "import com.example.fishforecast.data.local.entities.FishingSessionEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/journal/JournalScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishingSpotEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/journal/JournalScreen.kt",
          "line": 61,
          "fragment": "import com.example.fishforecast.data.local.entities.FishingSpotEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/journal/JournalScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/common/ActiveMapBar.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/journal/JournalScreen.kt",
          "line": 62,
          "fragment": "import com.example.fishforecast.ui.common.ActiveMapTitle"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/journal/JournalScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/journal/JournalViewModel.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/journal/JournalScreen.kt",
          "line": 73,
          "fragment": "viewModel: JournalViewModel = hiltViewModel()"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/journal/JournalViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/journal/JournalViewModel.kt",
          "line": 6,
          "fragment": "data.local.entities.CatchEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/journal/JournalViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/CatchEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/journal/JournalViewModel.kt",
          "line": 6,
          "fragment": "import com.example.fishforecast.data.local.entities.CatchEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/journal/JournalViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/journal/JournalViewModel.kt",
          "line": 7,
          "fragment": "import com.example.fishforecast.data.local.entities.FishEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/journal/JournalViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishingSessionEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/journal/JournalViewModel.kt",
          "line": 9,
          "fragment": "import com.example.fishforecast.data.local.entities.FishingSessionEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/journal/JournalViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishingSpotEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/journal/JournalViewModel.kt",
          "line": 8,
          "fragment": "import com.example.fishforecast.data.local.entities.FishingSpotEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/journal/JournalViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/repository",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/journal/JournalViewModel.kt",
          "line": 10,
          "fragment": "data.repository.CatchRepository"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/journal/JournalViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/repository/CatchRepository.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/journal/JournalViewModel.kt",
          "line": 10,
          "fragment": "import com.example.fishforecast.data.repository.CatchRepository"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/journal/JournalViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/repository/FishingSessionRepository.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/journal/JournalViewModel.kt",
          "line": 11,
          "fragment": "import com.example.fishforecast.data.repository.FishingSessionRepository"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/journal/JournalViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/repository/FishingSpotRepository.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/journal/JournalViewModel.kt",
          "line": 13,
          "fragment": "import com.example.fishforecast.data.repository.FishingSpotRepository"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/journal/JournalViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/repository/FishRepository.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/journal/JournalViewModel.kt",
          "line": 12,
          "fragment": "import com.example.fishforecast.data.repository.FishRepository"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/map/MapScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/map/MapScreen.kt",
          "line": 59,
          "fragment": "import com.example.fishforecast.data.local.entities.FishEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/map/MapScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishingSpotEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/map/MapScreen.kt",
          "line": 60,
          "fragment": "import com.example.fishforecast.data.local.entities.FishingSpotEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/map/MapScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/SavedMapEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/map/MapScreen.kt",
          "line": 66,
          "fragment": "import com.example.fishforecast.data.local.entities.SavedMapEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/map/MapScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/repository/OfflineMapRepository.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/map/MapScreen.kt",
          "line": 67,
          "fragment": "import com.example.fishforecast.data.repository.RegionDownloadState"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/map/MapScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/fish/FishCatalog.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/map/MapScreen.kt",
          "line": 64,
          "fragment": "import com.example.fishforecast.domain.fish.decodeBaits"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/map/MapScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/knowledge/KnowledgeCatalog.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/map/MapScreen.kt",
          "line": 65,
          "fragment": "import com.example.fishforecast.domain.knowledge.StructureType"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/map/MapScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/share/GpxWriter.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/map/MapScreen.kt",
          "line": 68,
          "fragment": "import com.example.fishforecast.domain.share.GpxWriter"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/map/MapScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/map/MapConfig.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/map/MapScreen.kt",
          "line": 114,
          "fragment": "BaseLayer"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/map/MapScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/map/MapViewModel.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/map/MapScreen.kt",
          "line": 85,
          "fragment": "MapViewModel"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/map/MapScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/map/ShareActions.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/map/MapScreen.kt",
          "line": 148,
          "fragment": "shareSpotsAsGpx"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/map/MapScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/map/TileEstimate.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/map/MapScreen.kt",
          "line": 307,
          "fragment": "estimateTileCount("
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/map/MapScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/maps/MapSizeFormat.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/map/MapScreen.kt",
          "line": 69,
          "fragment": "import com.example.fishforecast.ui.maps.formatDistance"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/map/MapViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/map/MapViewModel.kt",
          "line": 8,
          "fragment": "import com.example.fishforecast.data.local.entities.FishEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/map/MapViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishingSpotEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/map/MapViewModel.kt",
          "line": 9,
          "fragment": "import com.example.fishforecast.data.local.entities.FishingSpotEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/map/MapViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/SavedMapEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/map/MapViewModel.kt",
          "line": 11,
          "fragment": "import com.example.fishforecast.data.local.entities.SavedMapEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/map/MapViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/repository/FishingContextRepository.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/map/MapViewModel.kt",
          "line": 13,
          "fragment": "import com.example.fishforecast.data.repository.FishingContextRepository"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/map/MapViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/repository/FishingSpotRepository.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/map/MapViewModel.kt",
          "line": 19,
          "fragment": "import com.example.fishforecast.data.repository.FishingSpotRepository"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/map/MapViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/repository/FishRepository.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/map/MapViewModel.kt",
          "line": 12,
          "fragment": "import com.example.fishforecast.data.repository.FishRepository"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/map/MapViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/repository/KnowledgeRepository.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/map/MapViewModel.kt",
          "line": 15,
          "fragment": "import com.example.fishforecast.data.repository.KnowledgeRepository"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/map/MapViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/repository/OfflineMapRepository.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/map/MapViewModel.kt",
          "line": 20,
          "fragment": "import com.example.fishforecast.data.repository.OfflineMapRepository"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/map/MapViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/repository/RegionPackRepository.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/map/MapViewModel.kt",
          "line": 14,
          "fragment": "import com.example.fishforecast.data.repository.RegionPackRepository"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/map/MapViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/fish/FishCatalog.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/map/MapViewModel.kt",
          "line": 16,
          "fragment": "import com.example.fishforecast.domain.fish.encodeBaits"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/map/MapViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/knowledge/KnowledgeCatalog.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/map/MapViewModel.kt",
          "line": 17,
          "fragment": "import com.example.fishforecast.domain.knowledge.StructureType"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/map/MapViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/location/LocationTracker.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/map/MapViewModel.kt",
          "line": 22,
          "fragment": "import com.example.fishforecast.domain.location.LocationTracker"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/map/MapViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/map/MapConfig.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/map/MapViewModel.kt",
          "line": 58,
          "fragment": "BaseLayer"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/map/ShareActions.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishingSpotEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/map/ShareActions.kt",
          "line": 7,
          "fragment": "import com.example.fishforecast.data.local.entities.FishingSpotEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/map/ShareActions.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/share/RegionPack.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/map/ShareActions.kt",
          "line": 8,
          "fragment": "import com.example.fishforecast.domain.share.RegionPack"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/maps/SavedMapsScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/SavedMapEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/maps/SavedMapsScreen.kt",
          "line": 48,
          "fragment": "import com.example.fishforecast.data.local.entities.SavedMapEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/maps/SavedMapsScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/knowledge/KnowledgeCatalog.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/maps/SavedMapsScreen.kt",
          "line": 49,
          "fragment": "import com.example.fishforecast.domain.knowledge.WaterBodyType"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/maps/SavedMapsScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/map/ShareActions.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/maps/SavedMapsScreen.kt",
          "line": 50,
          "fragment": "import com.example.fishforecast.ui.map.shareRegionPack"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/maps/SavedMapsScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/maps/MapSizeFormat.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/maps/SavedMapsScreen.kt",
          "line": 267,
          "fragment": "formatDistance(map.widthKm)"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/maps/SavedMapsScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/maps/SavedMapsViewModel.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/maps/SavedMapsScreen.kt",
          "line": 61,
          "fragment": "SavedMapsViewModel"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/maps/SavedMapsViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishingSpotEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/maps/SavedMapsViewModel.kt",
          "line": 8,
          "fragment": "import com.example.fishforecast.data.local.entities.FishingSpotEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/maps/SavedMapsViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/SavedMapEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/maps/SavedMapsViewModel.kt",
          "line": 9,
          "fragment": "import com.example.fishforecast.data.local.entities.SavedMapEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/maps/SavedMapsViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/repository/FishingContextRepository.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/maps/SavedMapsViewModel.kt",
          "line": 11,
          "fragment": "import com.example.fishforecast.data.repository.FishingContextRepository"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/maps/SavedMapsViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/repository/FishingSpotRepository.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/maps/SavedMapsViewModel.kt",
          "line": 12,
          "fragment": "import com.example.fishforecast.data.repository.FishingSpotRepository"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/maps/SavedMapsViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/repository/FishRepository.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/maps/SavedMapsViewModel.kt",
          "line": 10,
          "fragment": "import com.example.fishforecast.data.repository.FishRepository"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/maps/SavedMapsViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/repository/KnowledgeRepository.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/maps/SavedMapsViewModel.kt",
          "line": 14,
          "fragment": "import com.example.fishforecast.data.repository.KnowledgeRepository"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/maps/SavedMapsViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/repository/OfflineMapRepository.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/maps/SavedMapsViewModel.kt",
          "line": 15,
          "fragment": "import com.example.fishforecast.data.repository.OfflineMapRepository"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/maps/SavedMapsViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/repository/RegionPackRepository.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/maps/SavedMapsViewModel.kt",
          "line": 13,
          "fragment": "import com.example.fishforecast.data.repository.RegionPackRepository"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/maps/SavedMapsViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/knowledge/KnowledgeCatalog.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/maps/SavedMapsViewModel.kt",
          "line": 16,
          "fragment": "import com.example.fishforecast.domain.knowledge.WaterBodyType"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/maps/SavedMapsViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/share/GpxParser.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/maps/SavedMapsViewModel.kt",
          "line": 18,
          "fragment": "import com.example.fishforecast.domain.share.GpxParser"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/maps/SavedMapsViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/share/GpxWriter.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/maps/SavedMapsViewModel.kt",
          "line": 19,
          "fragment": "import com.example.fishforecast.domain.share.GpxWriter"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/reference/FishSection.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/reference/FishSection.kt",
          "line": 66,
          "fragment": "import com.example.fishforecast.data.local.entities.FishEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/reference/FishSection.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/bite/PlaceContext.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/reference/FishSection.kt",
          "line": 67,
          "fragment": "import com.example.fishforecast.domain.bite.WaterLayerChoice"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/reference/FishSection.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/fish/FishCatalog.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/reference/FishSection.kt",
          "line": 70,
          "fragment": "import com.example.fishforecast.domain.fish.decodeBaits"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/reference/FishSection.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/fish/FishWords.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/reference/FishSection.kt",
          "line": 69,
          "fragment": "import com.example.fishforecast.domain.fish.horizonText"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/reference/FishSection.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/fish/Guild.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/reference/FishSection.kt",
          "line": 68,
          "fragment": "import com.example.fishforecast.domain.fish.Guild"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/reference/FishSection.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/reference/FishTerms.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/reference/FishSection.kt",
          "line": 184,
          "fragment": "guildText"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/reference/FishSection.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/reference/ReferenceViewModel.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/reference/FishSection.kt",
          "line": 84,
          "fragment": "FishCard"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/reference/FishTerms.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/fish/FishCatalog.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/reference/FishTerms.kt",
          "line": 3,
          "fragment": "import com.example.fishforecast.domain.fish.GroundbaitRule"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/reference/FishTerms.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/fish/FishWords.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/reference/FishTerms.kt",
          "line": 5,
          "fragment": "import com.example.fishforecast.domain.fish.flavorText"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/reference/FishTerms.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/fish/Guild.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/reference/FishTerms.kt",
          "line": 4,
          "fragment": "import com.example.fishforecast.domain.fish.Guild"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/reference/KnowledgeSections.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/knowledge/KnowledgeCatalog.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/reference/KnowledgeSections.kt",
          "line": 22,
          "fragment": "import com.example.fishforecast.domain.knowledge.BaitingPlan"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/reference/KnowledgeSections.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/reference/FishTerms.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/reference/KnowledgeSections.kt",
          "line": 272,
          "fragment": "guildText"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/reference/ReferenceScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/common/ActiveMapBar.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/reference/ReferenceScreen.kt",
          "line": 36,
          "fragment": "import com.example.fishforecast.ui.common.ActiveMapTitle"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/reference/ReferenceScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/reference/FishSection.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/reference/ReferenceScreen.kt",
          "line": 124,
          "fragment": "FishSection"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/reference/ReferenceScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/reference/KnowledgeSections.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/reference/ReferenceScreen.kt",
          "line": 132,
          "fragment": "WaterBodiesSection"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/reference/ReferenceScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/reference/ReferenceViewModel.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/reference/ReferenceScreen.kt",
          "line": 57,
          "fragment": "ReferenceViewModel"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/reference/ReferenceViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/DailySunEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/reference/ReferenceViewModel.kt",
          "line": 8,
          "fragment": "import com.example.fishforecast.data.local.entities.DailySunEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/reference/ReferenceViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/reference/ReferenceViewModel.kt",
          "line": 9,
          "fragment": "import com.example.fishforecast.data.local.entities.FishEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/reference/ReferenceViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/repository/FishingContextRepository.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/reference/ReferenceViewModel.kt",
          "line": 15,
          "fragment": "import com.example.fishforecast.data.repository.FishingContextRepository"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/reference/ReferenceViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/repository/FishRepository.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/reference/ReferenceViewModel.kt",
          "line": 12,
          "fragment": "import com.example.fishforecast.data.repository.FishRepository"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/reference/ReferenceViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/repository/KnowledgeRepository.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/reference/ReferenceViewModel.kt",
          "line": 13,
          "fragment": "import com.example.fishforecast.data.repository.KnowledgeRepository"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/reference/ReferenceViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCase.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/reference/ReferenceViewModel.kt",
          "line": 16,
          "fragment": "import com.example.fishforecast.domain.bite.CalculateFishActivityUseCase"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/reference/ReferenceViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/bite/PlaceContext.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/reference/ReferenceViewModel.kt",
          "line": 17,
          "fragment": "import com.example.fishforecast.domain.bite.PlaceContext"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/reference/ReferenceViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/knowledge/KnowledgeCatalog.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/reference/ReferenceViewModel.kt",
          "line": 14,
          "fragment": "import com.example.fishforecast.domain.knowledge.KnowledgeCatalog"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/reference/ReferenceViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/light/LightPhase.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/reference/ReferenceViewModel.kt",
          "line": 10,
          "fragment": "import com.example.fishforecast.domain.light.LightPhase"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/reference/ReferenceViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/water/Oxygen.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/reference/ReferenceViewModel.kt",
          "line": 20,
          "fragment": "import com.example.fishforecast.domain.water.oxygenSaturationMgL"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/reference/ReferenceViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/water/WaterState.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/reference/ReferenceViewModel.kt",
          "line": 19,
          "fragment": "import com.example.fishforecast.domain.water.WaterState"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/session/FishingSessionViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/session/FishingSessionViewModel.kt",
          "line": 8,
          "fragment": "import com.example.fishforecast.data.local.entities.FishEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/session/FishingSessionViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishingSessionEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/session/FishingSessionViewModel.kt",
          "line": 9,
          "fragment": "import com.example.fishforecast.data.local.entities.FishingSessionEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/session/FishingSessionViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/repository/FishingContextRepository.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/session/FishingSessionViewModel.kt",
          "line": 11,
          "fragment": "import com.example.fishforecast.data.repository.FishingContextRepository"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/session/FishingSessionViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/repository/FishingSessionRepository.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/session/FishingSessionViewModel.kt",
          "line": 12,
          "fragment": "import com.example.fishforecast.data.repository.FishingSessionRepository"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/session/FishingSessionViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/repository/FishRepository.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/session/FishingSessionViewModel.kt",
          "line": 10,
          "fragment": "import com.example.fishforecast.data.repository.FishRepository"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/session/FishingSessionViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/repository/KnowledgeRepository.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/session/FishingSessionViewModel.kt",
          "line": 13,
          "fragment": "import com.example.fishforecast.data.repository.KnowledgeRepository"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/session/FishingSessionViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/knowledge/KnowledgeCatalog.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/session/FishingSessionViewModel.kt",
          "line": 14,
          "fragment": "import com.example.fishforecast.domain.knowledge.FishingMethod"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/session/FishingSessionViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/session/FishingStrategy.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/session/FishingSessionViewModel.kt",
          "line": 15,
          "fragment": "import com.example.fishforecast.domain.session.CatchGoal"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/session/FishingSessionViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/water/WaterTemperature.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/session/FishingSessionViewModel.kt",
          "line": 18,
          "fragment": "import com.example.fishforecast.domain.water.DEFAULT_DEEP"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/session/SessionPanel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/session/SessionPanel.kt",
          "line": 31,
          "fragment": "import com.example.fishforecast.data.local.entities.FishEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/session/SessionPanel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishingSessionEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/session/SessionPanel.kt",
          "line": 32,
          "fragment": "import com.example.fishforecast.data.local.entities.FishingSessionEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/session/SessionPanel.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/bite/PlaceContext.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/session/SessionPanel.kt",
          "line": 33,
          "fragment": "import com.example.fishforecast.domain.bite.WaterLayerChoice"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/session/SessionPanel.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/knowledge/KnowledgeCatalog.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/session/SessionPanel.kt",
          "line": 34,
          "fragment": "import com.example.fishforecast.domain.knowledge.FishingMethod"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/session/SessionPanel.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/session/FishingStrategy.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/session/SessionPanel.kt",
          "line": 35,
          "fragment": "import com.example.fishforecast.domain.session.CatchGoal"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/session/SessionPanel.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/session/FishingSessionViewModel.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/session/SessionPanel.kt",
          "line": 54,
          "fragment": "SessionForm"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/theme/Theme.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/theme/Color.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/theme/Theme.kt",
          "line": 15,
          "fragment": "Purple80"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/theme/Theme.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/theme/Type.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/theme/Theme.kt",
          "line": 55,
          "fragment": "Typography"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherIcons.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/weather/WeatherCode.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherIcons.kt",
          "line": 14,
          "fragment": "import com.example.fishforecast.domain.weather.Sky"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/DailySunEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherScreen.kt",
          "line": 49,
          "fragment": "import com.example.fishforecast.data.local.entities.DailySunEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/PressureLogEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherScreen.kt",
          "line": 50,
          "fragment": "import com.example.fishforecast.data.local.entities.PressureLogEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/WeatherEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherScreen.kt",
          "line": 51,
          "fragment": "import com.example.fishforecast.data.local.entities.WeatherEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/sensor/PressureUnits.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherScreen.kt",
          "line": 52,
          "fragment": "import com.example.fishforecast.domain.sensor.hPaToMmHg"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/water/Oxygen.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherScreen.kt",
          "line": 55,
          "fragment": "import com.example.fishforecast.domain.water.oxygenLevel"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/water/WaterState.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherScreen.kt",
          "line": 53,
          "fragment": "import com.example.fishforecast.domain.water.WaterState"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/weather/DailyForecast.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherScreen.kt",
          "line": 60,
          "fragment": "import com.example.fishforecast.domain.weather.DailyForecast"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/weather/HourWindow.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherScreen.kt",
          "line": 62,
          "fragment": "import com.example.fishforecast.domain.weather.HourWindow"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/weather/PressureTrend.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherScreen.kt",
          "line": 61,
          "fragment": "import com.example.fishforecast.domain.weather.PressureDirection"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/weather/WeatherCode.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherScreen.kt",
          "line": 66,
          "fragment": "import com.example.fishforecast.domain.weather.skyOf"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/weather/Wind.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherScreen.kt",
          "line": 64,
          "fragment": "import com.example.fishforecast.domain.weather.kmhToMs"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/common/ActiveMapBar.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherScreen.kt",
          "line": 73,
          "fragment": "import com.example.fishforecast.ui.common.ActiveMapTitle"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/common/NoActiveMap.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherScreen.kt",
          "line": 72,
          "fragment": "import com.example.fishforecast.ui.common.NoActiveMapMessage"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherCharts.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherScreen.kt",
          "line": 231,
          "fragment": "MultiLineChart("
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherIcons.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherScreen.kt",
          "line": 355,
          "fragment": "icon"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherScreen.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherViewModel.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherScreen.kt",
          "line": 112,
          "fragment": "WeatherViewModel"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/DailySunEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherViewModel.kt",
          "line": 7,
          "fragment": "import com.example.fishforecast.data.local.entities.DailySunEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/PressureLogEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherViewModel.kt",
          "line": 10,
          "fragment": "import com.example.fishforecast.data.local.entities.PressureLogEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/SavedMapEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherViewModel.kt",
          "line": 8,
          "fragment": "import com.example.fishforecast.data.local.entities.SavedMapEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/WeatherEntity.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherViewModel.kt",
          "line": 9,
          "fragment": "import com.example.fishforecast.data.local.entities.WeatherEntity"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/repository/BarometerRepository.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherViewModel.kt",
          "line": 11,
          "fragment": "import com.example.fishforecast.data.repository.BarometerRepository"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/repository/FishingContextRepository.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherViewModel.kt",
          "line": 12,
          "fragment": "import com.example.fishforecast.data.repository.FishingContextRepository"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/sensor/PressureProvider.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherViewModel.kt",
          "line": 13,
          "fragment": "import com.example.fishforecast.domain.sensor.PressureProvider"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/water/WaterState.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherViewModel.kt",
          "line": 15,
          "fragment": "import com.example.fishforecast.domain.water.WaterState"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/data/local/entities/SavedMapEntityTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/SavedMapEntity.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/data/local/entities/SavedMapEntityTest.kt",
          "line": 11,
          "fragment": "SavedMapEntity"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/alert/BiteAlertPolicyTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishEntity.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/alert/BiteAlertPolicyTest.kt",
          "line": 3,
          "fragment": "import com.example.fishforecast.data.local.entities.FishEntity"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/alert/BiteAlertPolicyTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/alert/BiteAlertPolicy.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/alert/BiteAlertPolicyTest.kt",
          "line": 29,
          "fragment": "AlertHistory"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCaseTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/DailySunEntity.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCaseTest.kt",
          "line": 3,
          "fragment": "import com.example.fishforecast.data.local.entities.DailySunEntity"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCaseTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishEntity.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCaseTest.kt",
          "line": 4,
          "fragment": "import com.example.fishforecast.data.local.entities.FishEntity"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCaseTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/WeatherEntity.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCaseTest.kt",
          "line": 5,
          "fragment": "import com.example.fishforecast.data.local.entities.WeatherEntity"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCaseTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/bite/BiteForecast.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCaseTest.kt",
          "line": 217,
          "fragment": "BiteLevel"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCaseTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCase.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCaseTest.kt",
          "line": 15,
          "fragment": "CalculateFishActivityUseCase"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCaseTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/bite/PlaceContext.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCaseTest.kt",
          "line": 650,
          "fragment": "PlaceContext"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCaseTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/knowledge/KnowledgeCatalog.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCaseTest.kt",
          "line": 6,
          "fragment": "import com.example.fishforecast.domain.knowledge.StructureType"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCaseTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/water/WaterState.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCaseTest.kt",
          "line": 8,
          "fragment": "import com.example.fishforecast.domain.water.WaterState"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCaseTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/water/WaterTemperature.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCaseTest.kt",
          "line": 7,
          "fragment": "import com.example.fishforecast.domain.water.WaterHour"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/bite/FactorCommentFormatTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/DailySunEntity.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/bite/FactorCommentFormatTest.kt",
          "line": 3,
          "fragment": "import com.example.fishforecast.data.local.entities.DailySunEntity"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/bite/FactorCommentFormatTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishEntity.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/bite/FactorCommentFormatTest.kt",
          "line": 4,
          "fragment": "import com.example.fishforecast.data.local.entities.FishEntity"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/bite/FactorCommentFormatTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/WeatherEntity.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/bite/FactorCommentFormatTest.kt",
          "line": 5,
          "fragment": "import com.example.fishforecast.data.local.entities.WeatherEntity"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/bite/FactorCommentFormatTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/bite/BiteForecast.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/bite/FactorCommentFormatTest.kt",
          "line": 91,
          "fragment": "BiteFactor"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/bite/FactorCommentFormatTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCase.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/bite/FactorCommentFormatTest.kt",
          "line": 27,
          "fragment": "CalculateFishActivityUseCase"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/bite/FactorCommentFormatTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/bite/ObservationContext.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/bite/FactorCommentFormatTest.kt",
          "line": 99,
          "fragment": "ActiveObservation"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/bite/FactorCommentFormatTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/bite/PlaceContext.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/bite/FactorCommentFormatTest.kt",
          "line": 93,
          "fragment": "PlaceContext"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/bite/FactorCommentFormatTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/knowledge/KnowledgeCatalog.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/bite/FactorCommentFormatTest.kt",
          "line": 6,
          "fragment": "import com.example.fishforecast.domain.knowledge.KnowledgeCodec"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/bite/FactorCommentFormatTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/water/WaterState.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/bite/FactorCommentFormatTest.kt",
          "line": 9,
          "fragment": "import com.example.fishforecast.domain.water.WaterState"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/bite/FactorCommentFormatTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/water/WaterTemperature.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/bite/FactorCommentFormatTest.kt",
          "line": 8,
          "fragment": "import com.example.fishforecast.domain.water.WaterHour"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/bite/FindBiteWindowUseCaseTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishEntity.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/bite/FindBiteWindowUseCaseTest.kt",
          "line": 3,
          "fragment": "import com.example.fishforecast.data.local.entities.FishEntity"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/bite/FindBiteWindowUseCaseTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/WeatherEntity.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/bite/FindBiteWindowUseCaseTest.kt",
          "line": 4,
          "fragment": "import com.example.fishforecast.data.local.entities.WeatherEntity"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/bite/FindBiteWindowUseCaseTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCase.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/bite/FindBiteWindowUseCaseTest.kt",
          "line": 13,
          "fragment": "CalculateFishActivityUseCase"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/bite/FindBiteWindowUseCaseTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/bite/FindBiteWindowUseCase.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/bite/FindBiteWindowUseCaseTest.kt",
          "line": 13,
          "fragment": "FindBiteWindowUseCase"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/bite/NormalPressureTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/bite/NormalPressure.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/bite/NormalPressureTest.kt",
          "line": 12,
          "fragment": "MIN_SAMPLE_HOURS"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/bite/ObservationContextTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/bite/ObservationContext.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/bite/ObservationContextTest.kt",
          "line": 24,
          "fragment": "ActiveObservation"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/bite/ObservationContextTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/fish/Guild.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/bite/ObservationContextTest.kt",
          "line": 3,
          "fragment": "import com.example.fishforecast.domain.fish.Guild"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/bite/ObservationContextTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/knowledge/KnowledgeCatalog.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/bite/ObservationContextTest.kt",
          "line": 4,
          "fragment": "import com.example.fishforecast.domain.knowledge.KnowledgeCodec"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/bite/PlaceContextTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishingSpotEntity.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/bite/PlaceContextTest.kt",
          "line": 3,
          "fragment": "import com.example.fishforecast.data.local.entities.FishingSpotEntity"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/bite/PlaceContextTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/bite/PlaceContext.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/bite/PlaceContextTest.kt",
          "line": 30,
          "fragment": "placeOf"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/bite/PlaceContextTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/fish/Guild.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/bite/PlaceContextTest.kt",
          "line": 4,
          "fragment": "import com.example.fishforecast.domain.fish.Guild"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/bite/PlaceContextTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/knowledge/KnowledgeCatalog.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/bite/PlaceContextTest.kt",
          "line": 5,
          "fragment": "import com.example.fishforecast.domain.knowledge.KnowledgeCodec"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/bite/WeekActivityTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/bite/BiteForecast.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/bite/WeekActivityTest.kt",
          "line": 23,
          "fragment": "BiteForecast"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/bite/WeekActivityTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/bite/WeekActivity.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/bite/WeekActivityTest.kt",
          "line": 36,
          "fragment": "weekActivity"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/fish/FishCatalogTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishEntity.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/fish/FishCatalogTest.kt",
          "line": 3,
          "fragment": "import com.example.fishforecast.data.local.entities.FishEntity"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/fish/FishCatalogTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/repository/FishRepository.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/fish/FishCatalogTest.kt",
          "line": 4,
          "fragment": "import com.example.fishforecast.data.repository.matchesName"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/fish/FishCatalogTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/fish/FishCatalog.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/fish/FishCatalogTest.kt",
          "line": 18,
          "fragment": "FishCatalogCodec"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/knowledge/KnowledgeCatalogTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/knowledge/KnowledgeCatalog.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/knowledge/KnowledgeCatalogTest.kt",
          "line": 15,
          "fragment": "KnowledgeCodec"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/light/LightPhaseTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/DailySunEntity.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/light/LightPhaseTest.kt",
          "line": 3,
          "fragment": "import com.example.fishforecast.data.local.entities.DailySunEntity"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/light/LightPhaseTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/fish/Guild.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/light/LightPhaseTest.kt",
          "line": 4,
          "fragment": "import com.example.fishforecast.domain.fish.Guild"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/light/LightPhaseTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/light/LightPhase.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/light/LightPhaseTest.kt",
          "line": 21,
          "fragment": "LightPhase"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/season/SeasonPhaseTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishEntity.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/season/SeasonPhaseTest.kt",
          "line": 3,
          "fragment": "import com.example.fishforecast.data.local.entities.FishEntity"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/season/SeasonPhaseTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/season/SeasonPhase.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/season/SeasonPhaseTest.kt",
          "line": 66,
          "fragment": "seasonPhaseOf"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/season/SeasonPhaseTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/water/WaterTemperature.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/season/SeasonPhaseTest.kt",
          "line": 4,
          "fragment": "import com.example.fishforecast.domain.water.WaterHour"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/session/FishingStrategyTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/bite/PlaceContext.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/session/FishingStrategyTest.kt",
          "line": 3,
          "fragment": "import com.example.fishforecast.domain.bite.WaterLayerChoice"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/session/FishingStrategyTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/fish/FishCatalog.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/session/FishingStrategyTest.kt",
          "line": 5,
          "fragment": "import com.example.fishforecast.domain.fish.FishCatalogCodec"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/session/FishingStrategyTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/knowledge/KnowledgeCatalog.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/session/FishingStrategyTest.kt",
          "line": 7,
          "fragment": "import com.example.fishforecast.domain.knowledge.KnowledgeCodec"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/session/FishingStrategyTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/light/LightPhase.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/session/FishingStrategyTest.kt",
          "line": 8,
          "fragment": "import com.example.fishforecast.domain.light.LightPhase"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/session/FishingStrategyTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/session/FishingStrategy.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/session/FishingStrategyTest.kt",
          "line": 4,
          "fragment": "import com.example.fishforecast.domain.session.HourContext"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/session/SeasonalPlanTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/fish/FishCatalog.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/session/SeasonalPlanTest.kt",
          "line": 3,
          "fragment": "import com.example.fishforecast.domain.fish.FishCatalogCodec"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/session/SeasonalPlanTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/knowledge/KnowledgeCatalog.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/session/SeasonalPlanTest.kt",
          "line": 5,
          "fragment": "import com.example.fishforecast.domain.knowledge.KnowledgeCodec"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/session/SeasonalPlanTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/light/LightPhase.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/session/SeasonalPlanTest.kt",
          "line": 6,
          "fragment": "import com.example.fishforecast.domain.light.LightPhase"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/session/SeasonalPlanTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/season/SeasonPhase.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/session/SeasonalPlanTest.kt",
          "line": 7,
          "fragment": "import com.example.fishforecast.domain.season.SeasonPhase"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/session/SeasonalPlanTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/session/FishingStrategy.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/session/SeasonalPlanTest.kt",
          "line": 41,
          "fragment": "SessionConditions"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/share/GpxParserTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/share/GpxParser.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/share/GpxParserTest.kt",
          "line": 9,
          "fragment": "GpxParser"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/share/GpxParserTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/share/GpxWriter.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/share/GpxParserTest.kt",
          "line": 57,
          "fragment": "GpxWriter"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/share/GpxWriterTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishingSpotEntity.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/share/GpxWriterTest.kt",
          "line": 3,
          "fragment": "import com.example.fishforecast.data.local.entities.FishingSpotEntity"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/share/GpxWriterTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/share/GpxWriter.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/share/GpxWriterTest.kt",
          "line": 25,
          "fragment": "GpxWriter"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/share/RegionPackCodecTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishEntity.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/share/RegionPackCodecTest.kt",
          "line": 3,
          "fragment": "import com.example.fishforecast.data.local.entities.FishEntity"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/share/RegionPackCodecTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/FishingSpotEntity.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/share/RegionPackCodecTest.kt",
          "line": 4,
          "fragment": "import com.example.fishforecast.data.local.entities.FishingSpotEntity"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/share/RegionPackCodecTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/SavedMapEntity.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/share/RegionPackCodecTest.kt",
          "line": 5,
          "fragment": "import com.example.fishforecast.data.local.entities.SavedMapEntity"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/share/RegionPackCodecTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/share/RegionPack.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/share/RegionPackCodecTest.kt",
          "line": 63,
          "fragment": "PackAuthor"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/share/RegionPackCodecTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/share/RegionPackCodec.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/share/RegionPackCodecTest.kt",
          "line": 57,
          "fragment": "RegionPackCodec"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/water/OxygenTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/water/Oxygen.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/water/OxygenTest.kt",
          "line": 11,
          "fragment": "oxygenSaturationMgL"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/water/WaterBodyTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/WeatherEntity.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/water/WaterBodyTest.kt",
          "line": 3,
          "fragment": "import com.example.fishforecast.data.local.entities.WeatherEntity"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/water/WaterBodyTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/knowledge/KnowledgeCatalog.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/water/WaterBodyTest.kt",
          "line": 4,
          "fragment": "import com.example.fishforecast.domain.knowledge.KnowledgeCodec"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/water/WaterBodyTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/water/Oxygen.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/water/WaterBodyTest.kt",
          "line": 64,
          "fragment": "availableOxygenMgL"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/water/WaterBodyTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/water/WaterState.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/water/WaterBodyTest.kt",
          "line": 107,
          "fragment": "calculateWaterState"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/water/WaterBodyTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/water/WaterTemperature.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/water/WaterBodyTest.kt",
          "line": 51,
          "fragment": "WaterLayer"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/water/WaterTemperatureTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/WeatherEntity.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/water/WaterTemperatureTest.kt",
          "line": 3,
          "fragment": "import com.example.fishforecast.data.local.entities.WeatherEntity"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/water/WaterTemperatureTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/water/WaterTemperature.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/water/WaterTemperatureTest.kt",
          "line": 55,
          "fragment": "WaterLayer"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/water/WaterTrendTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/WeatherEntity.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/water/WaterTrendTest.kt",
          "line": 3,
          "fragment": "import com.example.fishforecast.data.local.entities.WeatherEntity"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/water/WaterTrendTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/water/WaterState.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/water/WaterTrendTest.kt",
          "line": 58,
          "fragment": "waterTrend"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/water/WaterTrendTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/water/WaterTemperature.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/water/WaterTrendTest.kt",
          "line": 43,
          "fragment": "WaterHour"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/weather/DailyForecastTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/WeatherEntity.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/weather/DailyForecastTest.kt",
          "line": 3,
          "fragment": "import com.example.fishforecast.data.local.entities.WeatherEntity"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/weather/DailyForecastTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/weather/DailyForecast.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/weather/DailyForecastTest.kt",
          "line": 39,
          "fragment": "toDailyForecast"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/weather/HourWindowTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/weather/HourWindow.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/weather/HourWindowTest.kt",
          "line": 17,
          "fragment": "hourWindow"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/weather/PressureTrendTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/local/entities/WeatherEntity.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/weather/PressureTrendTest.kt",
          "line": 3,
          "fragment": "import com.example.fishforecast.data.local.entities.WeatherEntity"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/weather/PressureTrendTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/weather/PressureTrend.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/weather/PressureTrendTest.kt",
          "line": 24,
          "fragment": "pressureTrend"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/weather/WindTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/weather/Wind.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/weather/WindTest.kt",
          "line": 12,
          "fragment": "windDirectionLabel"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/ui/bite/BiteChartAxisTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/bite/BiteChartAxis.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/ui/bite/BiteChartAxisTest.kt",
          "line": 21,
          "fragment": "chartTicks"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/ui/map/MapConfigTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/map/MapConfig.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/ui/map/MapConfigTest.kt",
          "line": 13,
          "fragment": "MapConfig"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/ui/map/TileEstimateTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/map/TileEstimate.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/ui/map/TileEstimateTest.kt",
          "line": 12,
          "fragment": "estimateTileCount"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/ui/maps/MapSizeFormatTest.kt",
        "to": "app/src/main/java/com/example/fishforecast/ui/maps/MapSizeFormat.kt",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/ui/maps/MapSizeFormatTest.kt",
          "line": 12,
          "fragment": "formatDistance"
        }
      }
    ]
  }
}
```

## Потоки данных

```docdd-dataflow
{
  "added": {
    "sources": [
      {
        "id": "asset-initial-fish",
        "kind": "file",
        "where": "app/src/main/assets/initial_fish.json",
        "title": "Встроенный справочник видов"
      },
      {
        "id": "asset-knowledge",
        "kind": "file",
        "where": "app/src/main/assets/knowledge.json",
        "title": "Встроенные словари знаний"
      },
      {
        "id": "assets/initial_fish.json",
        "kind": "file",
        "where": "app/src/main/assets/initial_fish.json",
        "title": "Встроенный справочник видов рыб"
      },
      {
        "id": "assets/knowledge.json",
        "kind": "file",
        "where": "app/src/main/assets/knowledge.json",
        "title": "Встроенные словари: водоёмы, структуры, схемы закорма"
      },
      {
        "id": "barometer",
        "kind": "queue",
        "where": "app/src/main/java/com/example/fishforecast/domain/sensor/PressureProvider.kt",
        "title": "Датчик давления устройства"
      },
      {
        "id": "catalog-server",
        "kind": "http",
        "where": "app/src/main/java/com/example/fishforecast/data/remote/FishCatalogApi.kt",
        "title": "Пользовательский URL справочника видов и словарей знаний"
      },
      {
        "id": "catch-photo",
        "kind": "file",
        "where": "app/src/main/java/com/example/fishforecast/ui/journal",
        "title": "Файлы фото уловов"
      },
      {
        "id": "catch-photo-files",
        "kind": "file",
        "where": "app/src/main/java/com/example/fishforecast/data/repository/CatchRepository.kt",
        "title": "Снимки трофеев во внутренней памяти (filesDir/catches)"
      },
      {
        "id": "catches-dir",
        "kind": "file",
        "where": "app/src/main/res/xml/shared_files.xml",
        "title": "Каталог <filesDir>/catches: снимки уловов для FileProvider"
      },
      {
        "id": "datastore-catalog",
        "kind": "file",
        "where": "app/src/main/java/com/example/fishforecast/data/local/CatalogStore.kt",
        "title": "DataStore fish_catalog — адреса/версии справочника и словарей, скачанный документ знаний"
      },
      {
        "id": "datastore-settings",
        "kind": "file",
        "where": "app/src/main/java/com/example/fishforecast/data/local/ActiveMapStore.kt",
        "title": "DataStore fish_forecast_settings — активная карта, слой, автор пакетов"
      },
      {
        "id": "device-barometer",
        "kind": "memory",
        "where": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherViewModel.kt",
        "title": "Поток давления с датчика устройства"
      },
      {
        "id": "device-location",
        "kind": "memory",
        "where": "app/src/main/java/com/example/fishforecast/di/LocationModule.kt",
        "title": "Местоположение (FusedLocationProvider)"
      },
      {
        "id": "fish-catalog-url",
        "kind": "http",
        "where": "app/src/main/java/com/example/fishforecast/data/remote",
        "title": "Справочник видов по ссылке рыболова"
      },
      {
        "id": "gpx-file",
        "kind": "file",
        "where": "app/src/main/java/com/example/fishforecast/domain/share",
        "title": "GPX с точками (навигатор, эхолот)"
      },
      {
        "id": "map-tiles",
        "kind": "http",
        "where": "app/src/main/java/com/example/fishforecast/ui/map/MapConfig.kt",
        "title": "Тайлы схемы OpenFreeMap"
      },
      {
        "id": "maplibre-offline",
        "kind": "db",
        "where": "app/src/main/java/com/example/fishforecast/data/repository/OfflineMapRepository.kt",
        "title": "Офлайн-тайлы MapLibre OfflineManager"
      },
      {
        "id": "notifications",
        "kind": "queue",
        "where": "app/src/main/java/com/example/fishforecast/data/worker/BiteAlertWorker.kt",
        "title": "Уведомления Android"
      },
      {
        "id": "open-meteo",
        "kind": "http",
        "where": "app/src/main/java/com/example/fishforecast/data/remote",
        "title": "Прогноз погоды Open-Meteo"
      },
      {
        "id": "picked-document",
        "kind": "file",
        "where": "app/src/main/java/com/example/fishforecast/ui/maps/SavedMapsScreen.kt",
        "title": "Файл из системного выбора: чужой GPX или пакет района"
      },
      {
        "id": "region-pack-file",
        "kind": "file",
        "where": "app/src/main/java/com/example/fishforecast/domain/share/RegionPackCodec.kt",
        "title": "Пакет района .ffpack"
      },
      {
        "id": "room-barometer-log",
        "kind": "db",
        "where": "app/src/main/java/com/example/fishforecast/data/local/entities/PressureLogEntity.kt",
        "title": "Таблица barometer_log — история датчика давления"
      },
      {
        "id": "room-catches",
        "kind": "db",
        "where": "app/src/main/java/com/example/fishforecast/data/local/entities/CatchEntity.kt",
        "title": "Таблица catches — журнал трофеев"
      },
      {
        "id": "room-daily-sun",
        "kind": "db",
        "where": "app/src/main/java/com/example/fishforecast/data/local/entities/DailySunEntity.kt",
        "title": "Таблица daily_sun — восход и закат по карте"
      },
      {
        "id": "room-db",
        "kind": "db",
        "where": "fish_forecast_db (Room)",
        "title": "База Room — единственный источник правды на клиенте"
      },
      {
        "id": "room-fish",
        "kind": "db",
        "where": "app/src/main/java/com/example/fishforecast/data/local/entities/FishEntity.kt",
        "title": "Таблица fish — виды справочника"
      },
      {
        "id": "room-fishing-sessions",
        "kind": "db",
        "where": "app/src/main/java/com/example/fishforecast/data/local/entities/FishingSessionEntity.kt",
        "title": "Таблица fishing_sessions — выезды"
      },
      {
        "id": "room-fishing-spots",
        "kind": "db",
        "where": "app/src/main/java/com/example/fishforecast/data/local/entities/FishingSpotEntity.kt",
        "title": "Таблица fishing_spots — секретные точки"
      },
      {
        "id": "room-observations",
        "kind": "db",
        "where": "app/src/main/java/com/example/fishforecast/data/local/entities/ObservationEntity.kt",
        "title": "Таблица observations — Отметка о том, что рыболов увидел своими глазами"
      },
      {
        "id": "room-saved-maps",
        "kind": "db",
        "where": "app/src/main/java/com/example/fishforecast/data/local/entities/SavedMapEntity.kt",
        "title": "Таблица saved_maps — районы рыбалки"
      },
      {
        "id": "room-weather-forecast",
        "kind": "db",
        "where": "app/src/main/java/com/example/fishforecast/data/local/entities/WeatherEntity.kt",
        "title": "Таблица weather_forecast — почасовой прогноз по карте"
      },
      {
        "id": "satellite-tiles",
        "kind": "http",
        "where": "app/src/main/java/com/example/fishforecast/ui/map/MapConfig.kt",
        "title": "Снимки Sentinel-2 cloudless (EOX)"
      },
      {
        "id": "share-cache",
        "kind": "file",
        "where": "app/src/main/java/com/example/fishforecast/ui/map/ShareActions.kt",
        "title": "Кэш обмена <cacheDir>/shared: GPX, снимок карты, пакет района"
      },
      {
        "id": "workmanager",
        "kind": "queue",
        "where": "app/src/main/java/com/example/fishforecast/data/worker",
        "title": "Планировщик фоновых задач WorkManager"
      }
    ],
    "flows": [
      {
        "from": "app/src/main/java/com/example/fishforecast/data/local/ActiveMapStore.kt",
        "to": "datastore-settings",
        "direction": "read",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/local/ActiveMapStore.kt",
          "line": 31,
          "fragment": "val activeMapId: Flow<Int?> = context.settings.data.map { preferences ->"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/local/ActiveMapStore.kt",
        "to": "datastore-settings",
        "direction": "write",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/local/ActiveMapStore.kt",
          "line": 41,
          "fragment": "context.settings.edit { preferences -> preferences[BASE_LAYER] = name }"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/local/CatalogStore.kt",
        "to": "datastore-catalog",
        "direction": "read",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/local/CatalogStore.kt",
          "line": 27,
          "fragment": "val catalogUrl: Flow<String?> = context.catalogSettings.data.map { it[CATALOG_URL] }"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/local/CatalogStore.kt",
        "to": "datastore-catalog",
        "direction": "write",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/local/CatalogStore.kt",
          "line": 32,
          "fragment": "context.catalogSettings.edit { preferences ->"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/local/dao/CatchDao.kt",
        "to": "room-catches",
        "direction": "both",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/local/dao/CatchDao.kt",
          "line": 14,
          "fragment": "SELECT * FROM catches ORDER BY caughtAt DESC"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/local/dao/FishDao.kt",
        "to": "room-fish",
        "direction": "both",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/local/dao/FishDao.kt",
          "line": 9,
          "fragment": "@Query(\"SELECT * FROM fish\")"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/local/dao/FishingSessionDao.kt",
        "to": "room-fishing-sessions",
        "direction": "both",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/local/dao/FishingSessionDao.kt",
          "line": 16,
          "fragment": "SELECT * FROM fishing_sessions WHERE finishedAt IS NULL"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/local/dao/FishingSpotDao.kt",
        "to": "room-fishing-spots",
        "direction": "both",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/local/dao/FishingSpotDao.kt",
          "line": 14,
          "fragment": "SELECT * FROM fishing_spots ORDER BY createdAt DESC"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/local/dao/ObservationDao.kt",
        "to": "room-observations",
        "direction": "read",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/local/dao/ObservationDao.kt",
          "line": 14,
          "fragment": "FROM observations"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/local/dao/PressureLogDao.kt",
        "to": "room-barometer-log",
        "direction": "both",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/local/dao/PressureLogDao.kt",
          "line": 13,
          "fragment": "SELECT * FROM barometer_log ORDER BY time ASC"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/local/dao/SavedMapDao.kt",
        "to": "room-saved-maps",
        "direction": "both",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/local/dao/SavedMapDao.kt",
          "line": 14,
          "fragment": "SELECT * FROM saved_maps ORDER BY createdAt DESC"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/local/dao/WeatherDao.kt",
        "to": "room-daily-sun",
        "direction": "both",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/local/dao/WeatherDao.kt",
          "line": 26,
          "fragment": "SELECT * FROM daily_sun WHERE mapId = :mapId"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/local/dao/WeatherDao.kt",
        "to": "room-weather-forecast",
        "direction": "both",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/local/dao/WeatherDao.kt",
          "line": 14,
          "fragment": "SELECT * FROM weather_forecast WHERE mapId = :mapId"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/location/DefaultLocationTracker.kt",
        "to": "device-location",
        "direction": "read",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/location/DefaultLocationTracker.kt",
          "line": 40,
          "fragment": "locationClient.lastLocation.apply {"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/remote/FishCatalogApi.kt",
        "to": "catalog-server",
        "direction": "read",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/remote/FishCatalogApi.kt",
          "line": 20,
          "fragment": "suspend fun getCatalog(@Url url: String): String"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/remote/WeatherApi.kt",
        "to": "open-meteo",
        "direction": "read",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/remote/WeatherApi.kt",
          "line": 22,
          "fragment": "\"v1/forecast?hourly=temperature_2m,relative_humidity_2m,weather_code,\""
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/BarometerRepository.kt",
        "to": "room-barometer-log",
        "direction": "read",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/BarometerRepository.kt",
          "line": 23,
          "fragment": "val log: Flow<List<PressureLogEntity>> = dao.getLog()"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/BarometerRepository.kt",
        "to": "room-barometer-log",
        "direction": "write",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/BarometerRepository.kt",
          "line": 27,
          "fragment": "dao.insert("
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/CatchRepository.kt",
        "to": "catch-photo-files",
        "direction": "both",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/CatchRepository.kt",
          "line": 79,
          "fragment": "return File(dir, \"catch_${System.currentTimeMillis()}.jpg\")"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/CatchRepository.kt",
        "to": "room-catches",
        "direction": "read",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/CatchRepository.kt",
          "line": 29,
          "fragment": "val catches: Flow<List<CatchEntity>> = dao.getCatches()"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/CatchRepository.kt",
        "to": "room-catches",
        "direction": "write",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/CatchRepository.kt",
          "line": 60,
          "fragment": "dao.insertCatch("
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/CatchRepository.kt",
        "to": "room-fishing-sessions",
        "direction": "read",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/CatchRepository.kt",
          "line": 58,
          "fragment": "val session = sessionDao.active()"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/FishingContextRepository.kt",
        "to": "datastore-settings",
        "direction": "read",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/FishingContextRepository.kt",
          "line": 58,
          "fragment": "combine(activeMapStore.activeMapId, savedMaps) { id, maps ->"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/FishingContextRepository.kt",
        "to": "datastore-settings",
        "direction": "write",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/FishingContextRepository.kt",
          "line": 125,
          "fragment": "suspend fun setActiveMap(id: Int) = activeMapStore.setActiveMapId(id)"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/FishingContextRepository.kt",
        "to": "room-saved-maps",
        "direction": "read",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/FishingContextRepository.kt",
          "line": 50,
          "fragment": "val savedMaps: Flow<List<SavedMapEntity>> = savedMapDao.getRegions()"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/FishingContextRepository.kt",
        "to": "room-saved-maps",
        "direction": "write",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/FishingContextRepository.kt",
          "line": 158,
          "fragment": "savedMapDao.updateWaterMeasurement(id, temperatureC, measuredAt)"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/FishingContextRepository.kt",
        "to": "room-weather-forecast",
        "direction": "read",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/FishingContextRepository.kt",
          "line": 64,
          "fragment": "weatherRepository.forecastForMap(map.id)"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/FishingSessionRepository.kt",
        "to": "room-fishing-sessions",
        "direction": "read",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/FishingSessionRepository.kt",
          "line": 39,
          "fragment": "val active: Flow<FishingSessionEntity?> = dao.observeActive()"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/FishingSessionRepository.kt",
        "to": "room-fishing-sessions",
        "direction": "write",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/FishingSessionRepository.kt",
          "line": 63,
          "fragment": "dao.insert("
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/FishingSpotRepository.kt",
        "to": "room-fishing-spots",
        "direction": "read",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/FishingSpotRepository.kt",
          "line": 13,
          "fragment": "val spots: Flow<List<FishingSpotEntity>> = dao.getSpots()"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/FishingSpotRepository.kt",
        "to": "room-fishing-spots",
        "direction": "write",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/FishingSpotRepository.kt",
          "line": 26,
          "fragment": "suspend fun addSpot(spot: FishingSpotEntity) = dao.insertSpot(spot)"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/FishRepository.kt",
        "to": "asset-initial-fish",
        "direction": "read",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/FishRepository.kt",
          "line": 111,
          "fragment": "context.assets.open(ASSET_NAME).bufferedReader()"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/FishRepository.kt",
        "to": "catalog-server",
        "direction": "read",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/FishRepository.kt",
          "line": 89,
          "fragment": "val text = api.getCatalog(url)"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/FishRepository.kt",
        "to": "datastore-catalog",
        "direction": "read",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/FishRepository.kt",
          "line": 46,
          "fragment": "val catalogUrl: Flow<String?> = store.catalogUrl"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/FishRepository.kt",
        "to": "datastore-catalog",
        "direction": "write",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/FishRepository.kt",
          "line": 50,
          "fragment": "suspend fun setCatalogUrl(url: String?) = store.setCatalogUrl(url)"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/FishRepository.kt",
        "to": "room-fish",
        "direction": "read",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/FishRepository.kt",
          "line": 38,
          "fragment": "fun getAllFish(): Flow<List<FishEntity>> = fishDao.getAllFish()"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/FishRepository.kt",
        "to": "room-fish",
        "direction": "write",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/FishRepository.kt",
          "line": 126,
          "fragment": "fishDao.insertFish(entity)"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/KnowledgeRepository.kt",
        "to": "asset-knowledge",
        "direction": "read",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/KnowledgeRepository.kt",
          "line": 81,
          "fragment": "context.assets.open(ASSET_NAME).bufferedReader().use { it.readText() }"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/KnowledgeRepository.kt",
        "to": "catalog-server",
        "direction": "read",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/KnowledgeRepository.kt",
          "line": 62,
          "fragment": "val text = api.getCatalog(url)"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/KnowledgeRepository.kt",
        "to": "datastore-catalog",
        "direction": "read",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/KnowledgeRepository.kt",
          "line": 42,
          "fragment": "val catalog: Flow<KnowledgeCatalog> = store.knowledgeDocument.map { saved ->"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/KnowledgeRepository.kt",
        "to": "datastore-catalog",
        "direction": "write",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/KnowledgeRepository.kt",
          "line": 70,
          "fragment": "store.setKnowledgeDocument(text, incoming.version)"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/OfflineMapRepository.kt",
        "to": "maplibre-offline",
        "direction": "write",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/OfflineMapRepository.kt",
          "line": 75,
          "fragment": "offlineManager.createOfflineRegion("
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/OfflineMapRepository.kt",
        "to": "room-saved-maps",
        "direction": "read",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/OfflineMapRepository.kt",
          "line": 34,
          "fragment": "val regions: Flow<List<SavedMapEntity>> = dao.getRegions()"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/OfflineMapRepository.kt",
        "to": "room-saved-maps",
        "direction": "write",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/OfflineMapRepository.kt",
          "line": 120,
          "fragment": "dao.insertRegion("
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/RegionPackRepository.kt",
        "to": "datastore-settings",
        "direction": "read",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/RegionPackRepository.kt",
          "line": 75,
          "fragment": "id = store.ensureAuthorId(),"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/RegionPackRepository.kt",
        "to": "region-pack-file",
        "direction": "read",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/RegionPackRepository.kt",
          "line": 97,
          "fragment": "application.contentResolver.openInputStream(uri)?.use { it.readBytes().decodeToString() }"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/RegionPackRepository.kt",
        "to": "region-pack-file",
        "direction": "write",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/RegionPackRepository.kt",
          "line": 83,
          "fragment": "file.writeText(RegionPackCodec.encode(pack))"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/RegionPackRepository.kt",
        "to": "room-fish",
        "direction": "read",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/RegionPackRepository.kt",
          "line": 63,
          "fragment": "val allFish = fishDao.allFish()"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/RegionPackRepository.kt",
        "to": "room-fish",
        "direction": "write",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/RegionPackRepository.kt",
          "line": 128,
          "fragment": "fishDao.insertFish(incoming)"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/RegionPackRepository.kt",
        "to": "room-fishing-spots",
        "direction": "read",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/RegionPackRepository.kt",
          "line": 62,
          "fragment": "spotDao.getSpotsForMap(mapId)"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/RegionPackRepository.kt",
        "to": "room-fishing-spots",
        "direction": "write",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/RegionPackRepository.kt",
          "line": 146,
          "fragment": "spotDao.insertSpot("
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/RegionPackRepository.kt",
        "to": "room-saved-maps",
        "direction": "read",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/RegionPackRepository.kt",
          "line": 61,
          "fragment": "val map = savedMapDao.getRegionById(mapId) ?: error(\"Район не найден\")"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/RegionPackRepository.kt",
        "to": "room-saved-maps",
        "direction": "write",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/RegionPackRepository.kt",
          "line": 110,
          "fragment": "savedMapDao.insertRegion("
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/WeatherRepository.kt",
        "to": "open-meteo",
        "direction": "read",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/WeatherRepository.kt",
          "line": 45,
          "fragment": "val response = api.getPressureHistory(lat, lon)"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/WeatherRepository.kt",
        "to": "room-daily-sun",
        "direction": "read",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/WeatherRepository.kt",
          "line": 34,
          "fragment": "dao.getSunTimesForMap(mapId)"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/WeatherRepository.kt",
        "to": "room-daily-sun",
        "direction": "write",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/WeatherRepository.kt",
          "line": 92,
          "fragment": "dao.insertSunTimes("
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/WeatherRepository.kt",
        "to": "room-weather-forecast",
        "direction": "read",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/WeatherRepository.kt",
          "line": 32,
          "fragment": "dao.getForecastForMap(mapId)"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/repository/WeatherRepository.kt",
        "to": "room-weather-forecast",
        "direction": "write",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/repository/WeatherRepository.kt",
          "line": 89,
          "fragment": "dao.insertForecast(entities)"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/sensor/BarometerPressureProvider.kt",
        "to": "device-barometer",
        "direction": "read",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/sensor/BarometerPressureProvider.kt",
          "line": 47,
          "fragment": "sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/worker/BiteAlertWorker.kt",
        "to": "notifications",
        "direction": "write",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/worker/BiteAlertWorker.kt",
          "line": 188,
          "fragment": "manager.notify("
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/worker/BiteAlertWorker.kt",
        "to": "room-db",
        "direction": "read",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/worker/BiteAlertWorker.kt",
          "line": 63,
          "fragment": "fishRepository.getAllFish().first()"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/worker/BiteAlertWorker.kt",
        "to": "workmanager",
        "direction": "write",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/worker/BiteAlertWorker.kt",
          "line": 234,
          "fragment": "enqueueUniquePeriodicWork("
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/worker/WeatherSyncWorker.kt",
        "to": "open-meteo",
        "direction": "read",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/worker/WeatherSyncWorker.kt",
          "line": 35,
          "fragment": "return fishingContext.refreshWeather().fold("
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/worker/WeatherSyncWorker.kt",
        "to": "room-db",
        "direction": "write",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/worker/WeatherSyncWorker.kt",
          "line": 37,
          "fragment": "repository.cleanOldData(map.id)"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/worker/WeatherSyncWorker.kt",
        "to": "workmanager",
        "direction": "write",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/worker/WeatherSyncWorker.kt",
          "line": 59,
          "fragment": "enqueueUniquePeriodicWork("
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/di/DatabaseModule.kt",
        "to": "room-db",
        "direction": "both",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/di/DatabaseModule.kt",
          "line": 28,
          "fragment": "return Room.databaseBuilder("
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/di/LocationModule.kt",
        "to": "device-location",
        "direction": "read",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/di/LocationModule.kt",
          "line": 26,
          "fragment": "fun provideFusedLocationProviderClient(application: Application): FusedLocationProviderClient {"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/di/NetworkModule.kt",
        "to": "fish-catalog-url",
        "direction": "read",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/di/NetworkModule.kt",
          "line": 46,
          "fragment": ".create(FishCatalogApi::class.java)"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/di/NetworkModule.kt",
        "to": "open-meteo",
        "direction": "read",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/di/NetworkModule.kt",
          "line": 60,
          "fragment": ".create(WeatherApi::class.java)"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/di/SensorModule.kt",
        "to": "barometer",
        "direction": "read",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/di/SensorModule.kt",
          "line": 17,
          "fragment": "abstract fun bindPressureProvider("
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/share/GpxParser.kt",
        "to": "gpx-file",
        "direction": "read",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/share/GpxParser.kt",
          "line": 32,
          "fragment": "factory.newDocumentBuilder().parse(input)"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/share/GpxWriter.kt",
        "to": "gpx-file",
        "direction": "write",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/share/GpxWriter.kt",
          "line": 13,
          "fragment": "appendLine(\"\"\"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\"\"\")"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/share/RegionPackCodec.kt",
        "to": "region-pack-file",
        "direction": "read",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/share/RegionPackCodec.kt",
          "line": 44,
          "fragment": "val pack = json.decodeFromString(RegionPack.serializer(), text)"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/share/RegionPackCodec.kt",
        "to": "region-pack-file",
        "direction": "write",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/share/RegionPackCodec.kt",
          "line": 34,
          "fragment": "fun encode(pack: RegionPack): String = json.encodeToString(RegionPack.serializer(), pack)"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/addeditfish/AddEditFishViewModel.kt",
        "to": "room-db",
        "direction": "read",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/addeditfish/AddEditFishViewModel.kt",
          "line": 64,
          "fragment": "repository.getFishById(fishId)?.also { fish ->"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/addeditfish/AddEditFishViewModel.kt",
        "to": "room-db",
        "direction": "write",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/addeditfish/AddEditFishViewModel.kt",
          "line": 94,
          "fragment": "repository.insertFish("
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/bite/BiteViewModel.kt",
        "to": "room-db",
        "direction": "read",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/bite/BiteViewModel.kt",
          "line": 85,
          "fragment": "fishRepository.getAllFish(),"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/journal/JournalScreen.kt",
        "to": "catch-photo",
        "direction": "read",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/journal/JournalScreen.kt",
          "line": 221,
          "fragment": "runCatching { BitmapFactory.decodeFile(path) }.getOrNull()"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/journal/JournalViewModel.kt",
        "to": "catch-photo",
        "direction": "write",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/journal/JournalViewModel.kt",
          "line": 66,
          "fragment": "fun createPhotoFile(): File = catchRepository.createPhotoFile()"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/journal/JournalViewModel.kt",
        "to": "room-db",
        "direction": "read",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/journal/JournalViewModel.kt",
          "line": 44,
          "fragment": "catchRepository.catches,"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/journal/JournalViewModel.kt",
        "to": "room-db",
        "direction": "write",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/journal/JournalViewModel.kt",
          "line": 77,
          "fragment": "catchRepository.addCatch("
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/map/MapConfig.kt",
        "to": "map-tiles",
        "direction": "read",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/map/MapConfig.kt",
          "line": 29,
          "fragment": "https://tiles.openfreemap.org/styles/liberty"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/map/MapConfig.kt",
        "to": "satellite-tiles",
        "direction": "read",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/map/MapConfig.kt",
          "line": 50,
          "fragment": "https://tiles.maps.eox.at/wmts/1.0.0/s2cloudless-2024_3857"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/map/ShareActions.kt",
        "to": "share-cache",
        "direction": "write",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/map/ShareActions.kt",
          "line": 55,
          "fragment": "bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/maps/SavedMapsViewModel.kt",
        "to": "picked-document",
        "direction": "read",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/maps/SavedMapsViewModel.kt",
          "line": 133,
          "fragment": "regionPackRepository.importPack(uri).fold("
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherViewModel.kt",
        "to": "device-barometer",
        "direction": "read",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherViewModel.kt",
          "line": 101,
          "fragment": "pressureProvider.pressureFlow()"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/session/FishingStrategyTest.kt",
        "to": "assets/initial_fish.json",
        "direction": "read",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/session/FishingStrategyTest.kt",
          "line": 23,
          "fragment": "File(\"src/main/assets/initial_fish.json\").readText()"
        }
      },
      {
        "from": "app/src/test/java/com/example/fishforecast/domain/water/WaterBodyTest.kt",
        "to": "assets/knowledge.json",
        "direction": "read",
        "evidence": {
          "path": "app/src/test/java/com/example/fishforecast/domain/water/WaterBodyTest.kt",
          "line": 14,
          "fragment": "File(\"src/main/assets/knowledge.json\").readText()"
        }
      }
    ]
  }
}
```

## Пользовательские пути

```docdd-userflow
{
  "added": {
    "screens": [
      {
        "id": "app-shell",
        "title": "Оболочка: Scaffold с нижней панелью навигации",
        "file": "app/src/main/java/com/example/fishforecast/MainActivity.kt"
      },
      {
        "id": "reference",
        "title": "Справочник",
        "file": "app/src/main/java/com/example/fishforecast/ui/reference/ReferenceScreen.kt"
      },
      {
        "id": "bite",
        "title": "Прогноз клёва",
        "file": "app/src/main/java/com/example/fishforecast/ui/bite/BiteScreen.kt"
      },
      {
        "id": "map",
        "title": "Карта",
        "file": "app/src/main/java/com/example/fishforecast/ui/map/MapScreen.kt"
      },
      {
        "id": "weather",
        "title": "Погода",
        "file": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherScreen.kt"
      },
      {
        "id": "journal",
        "title": "Дневник рыбалки",
        "file": "app/src/main/java/com/example/fishforecast/ui/journal/JournalScreen.kt"
      },
      {
        "id": "saved-maps",
        "title": "Сохранённые карты",
        "file": "app/src/main/java/com/example/fishforecast/ui/maps/SavedMapsScreen.kt"
      },
      {
        "id": "add-edit-fish",
        "title": "Добавить / править рыбу",
        "file": "app/src/main/java/com/example/fishforecast/ui/addeditfish/AddEditFishScreen.kt"
      },
      {
        "id": "session",
        "title": "Сборы и активный выезд",
        "file": "app/src/main/java/com/example/fishforecast/ui/session/SessionPanel.kt"
      }
    ],
    "transitions": [
      {
        "from": "app-shell",
        "to": "reference",
        "trigger": "запуск приложения (startDestination)",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/MainActivity.kt",
          "line": 139,
          "fragment": "startDestination = FishListRoute,"
        }
      },
      {
        "from": "app-shell",
        "to": "bite",
        "trigger": "нижняя панель навигации",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/MainActivity.kt",
          "line": 120,
          "fragment": "navController.navigate(item.route) {"
        }
      },
      {
        "from": "app-shell",
        "to": "map",
        "trigger": "нижняя панель навигации",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/MainActivity.kt",
          "line": 120,
          "fragment": "navController.navigate(item.route) {"
        }
      },
      {
        "from": "app-shell",
        "to": "weather",
        "trigger": "нижняя панель навигации",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/MainActivity.kt",
          "line": 120,
          "fragment": "navController.navigate(item.route) {"
        }
      },
      {
        "from": "app-shell",
        "to": "journal",
        "trigger": "нижняя панель навигации",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/MainActivity.kt",
          "line": 120,
          "fragment": "navController.navigate(item.route) {"
        }
      },
      {
        "from": "reference",
        "to": "add-edit-fish",
        "trigger": "onAddFish — кнопка добавления вида",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/MainActivity.kt",
          "line": 146,
          "fragment": "navController.navigate(AddEditFishRoute())"
        }
      },
      {
        "from": "reference",
        "to": "add-edit-fish",
        "trigger": "onEditFish — выбор вида в списке",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/MainActivity.kt",
          "line": 149,
          "fragment": "navController.navigate(AddEditFishRoute(fishId))"
        }
      },
      {
        "from": "add-edit-fish",
        "to": "reference",
        "trigger": "onBack — возврат",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/MainActivity.kt",
          "line": 176,
          "fragment": "onBack = { navController.popBackStack() }"
        }
      },
      {
        "from": "map",
        "to": "saved-maps",
        "trigger": "onOpenLibrary",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/MainActivity.kt",
          "line": 166,
          "fragment": "MapScreen(onOpenLibrary = { navController.navigate(LibraryRoute) })"
        }
      },
      {
        "from": "saved-maps",
        "to": "map",
        "trigger": "onOpenMap",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/MainActivity.kt",
          "line": 172,
          "fragment": "SavedMapsScreen(onOpenMap = { navController.navigate(MapRoute) })"
        }
      },
      {
        "from": "bite",
        "to": "map",
        "trigger": "кнопка «Открыть карту» в заглушке «нет района»",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/common/NoActiveMap.kt",
          "line": 44,
          "fragment": "Button(onClick = onOpenMap) {"
        }
      },
      {
        "from": "add-edit-fish",
        "to": "back",
        "trigger": "рыба сохранена (UiEvent.SaveFish)",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/addeditfish/AddEditFishScreen.kt",
          "line": 38,
          "fragment": "onBack()"
        }
      },
      {
        "from": "add-edit-fish",
        "to": "back",
        "trigger": "стрелка «Назад» в тулбаре",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/addeditfish/AddEditFishScreen.kt",
          "line": 53,
          "fragment": "IconButton(onClick = onBack) {"
        }
      },
      {
        "from": "map",
        "to": "saved-maps",
        "trigger": "кнопка «Хранилище файлов» / «Все карты»",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/map/MapScreen.kt",
          "line": 124,
          "fragment": "IconButton(onClick = onOpenLibrary) {"
        }
      },
      {
        "from": "saved-maps",
        "to": "map",
        "trigger": "кнопка «Открыть карту»",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/maps/SavedMapsScreen.kt",
          "line": 191,
          "fragment": "Button(onClick = onOpenMap) { Text(\"Открыть карту\") }"
        }
      },
      {
        "from": "weather",
        "to": "map",
        "trigger": "«Открыть карту», когда район не выбран",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherScreen.kt",
          "line": 175,
          "fragment": "onOpenMap = onOpenMap"
        }
      },
      {
        "from": "reference",
        "to": "add-edit-fish",
        "trigger": "FAB «Добавить вид»",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/reference/ReferenceScreen.kt",
          "line": 101,
          "fragment": "FloatingActionButton(onClick = onAddFish) {"
        }
      },
      {
        "from": "reference",
        "to": "add-edit-fish",
        "trigger": "«Изменить» в карточке вида",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/reference/ReferenceScreen.kt",
          "line": 128,
          "fragment": "onEditFish = onEditFish,"
        }
      },
      {
        "from": "weather",
        "to": "map",
        "trigger": "onOpenMap",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/MainActivity.kt",
          "line": 155,
          "fragment": "onOpenMap = { navController.navigate(MapRoute) }"
        }
      },
      {
        "from": "bite",
        "to": "map",
        "trigger": "onOpenMap",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/MainActivity.kt",
          "line": 161,
          "fragment": "onOpenMap = { navController.navigate(MapRoute) }"
        }
      },
      {
        "from": "reference",
        "to": "saved-maps",
        "trigger": "onOpenLibrary",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/MainActivity.kt",
          "line": 144,
          "fragment": "onOpenLibrary = { navController.navigate(LibraryRoute) }"
        }
      },
      {
        "from": "weather",
        "to": "saved-maps",
        "trigger": "onOpenLibrary",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/MainActivity.kt",
          "line": 156,
          "fragment": "onOpenLibrary = { navController.navigate(LibraryRoute) }"
        }
      },
      {
        "from": "bite",
        "to": "saved-maps",
        "trigger": "onOpenLibrary",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/MainActivity.kt",
          "line": 162,
          "fragment": "onOpenLibrary = { navController.navigate(LibraryRoute) }"
        }
      },
      {
        "from": "journal",
        "to": "saved-maps",
        "trigger": "onOpenLibrary",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/MainActivity.kt",
          "line": 169,
          "fragment": "onOpenLibrary = { navController.navigate(LibraryRoute) }"
        }
      }
    ],
    "calls": [
      {
        "from": "app-shell",
        "to": "android-runtime-permissions",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/MainActivity.kt",
          "line": 100,
          "fragment": "permissionLauncher.launch(permissions.toTypedArray())"
        }
      },
      {
        "from": "add-edit-fish",
        "to": "app/src/main/java/com/example/fishforecast/ui/addeditfish/AddEditFishViewModel.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/addeditfish/AddEditFishScreen.kt",
          "line": 61,
          "fragment": "viewModel.onEvent(AddEditFishEvent.SaveFish)"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/addeditfish/AddEditFishViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/repository",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/addeditfish/AddEditFishViewModel.kt",
          "line": 94,
          "fragment": "repository.insertFish("
        }
      },
      {
        "from": "bite",
        "to": "app/src/main/java/com/example/fishforecast/ui/bite/BiteViewModel.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/bite/BiteScreen.kt",
          "line": 185,
          "fragment": "onClick = { viewModel.selectFish(fish) },"
        }
      },
      {
        "from": "bite",
        "to": "app/src/main/java/com/example/fishforecast/ui/session",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/bite/BiteScreen.kt",
          "line": 172,
          "fragment": "onStart = { sessionViewModel.start() }"
        }
      },
      {
        "from": "bite",
        "to": "app/src/main/java/com/example/fishforecast/ui/common/NoActiveMap.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/bite/BiteScreen.kt",
          "line": 135,
          "fragment": "NoActiveMapMessage("
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/bite/BiteViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCase.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/bite/BiteViewModel.kt",
          "line": 117,
          "fragment": "calculateFishActivity("
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/bite/BiteViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/repository",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/bite/BiteViewModel.kt",
          "line": 85,
          "fragment": "fishRepository.getAllFish(),"
        }
      },
      {
        "from": "journal",
        "to": "app/src/main/java/com/example/fishforecast/ui/journal/JournalViewModel.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/journal/JournalScreen.kt",
          "line": 136,
          "fragment": "onDelete = { viewModel.deleteCatch(entry) }"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/ui/journal/JournalViewModel.kt",
        "to": "app/src/main/java/com/example/fishforecast/data/repository",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/journal/JournalViewModel.kt",
          "line": 77,
          "fragment": "catchRepository.addCatch("
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/worker/BiteAlertWorker.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/bite/FindBiteWindowUseCase.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/worker/BiteAlertWorker.kt",
          "line": 73,
          "fragment": "val window = findBiteWindow("
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/domain/bite/FindBiteWindowUseCase.kt",
        "to": "app/src/main/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCase.kt",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/domain/bite/FindBiteWindowUseCase.kt",
          "line": 41,
          "fragment": "calculateFishActivity(fish, forecast, normalPressureMmHg, water, sunTimes)"
        }
      },
      {
        "from": "reference",
        "to": "domain.bite.CalculateFishActivityUseCase",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/reference/ReferenceViewModel.kt",
          "line": 193,
          "fragment": "calculateFishActivity("
        }
      },
      {
        "from": "reference",
        "to": "data.repository.FishRepository.refreshCatalogFromServer",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/reference/ReferenceViewModel.kt",
          "line": 228,
          "fragment": "repository.refreshCatalogFromServer().fold("
        }
      },
      {
        "from": "reference",
        "to": "data.repository.KnowledgeRepository.refreshFromServer",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/reference/ReferenceViewModel.kt",
          "line": 248,
          "fragment": "knowledge.refreshFromServer().fold("
        }
      },
      {
        "from": "reference",
        "to": "ui.reference.FishSection",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/reference/ReferenceScreen.kt",
          "line": 124,
          "fragment": "ReferenceSection.FISH -> FishSection("
        }
      },
      {
        "from": "session",
        "to": "data.repository.FishingSessionRepository.previewStrategy",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/session/FishingSessionViewModel.kt",
          "line": 125,
          "fragment": "sessions.previewStrategy(_form.value.toInput(fish))"
        }
      },
      {
        "from": "session",
        "to": "data.repository.FishingSessionRepository.start",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/session/FishingSessionViewModel.kt",
          "line": 133,
          "fragment": "sessions.start(_form.value.toInput(fish)).fold("
        }
      },
      {
        "from": "map",
        "to": "data.repository.OfflineMapRepository.downloadRegion",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/map/MapViewModel.kt",
          "line": 144,
          "fragment": "offlineMapRepository.downloadRegion("
        }
      },
      {
        "from": "map",
        "to": "data.repository.RegionPackRepository.exportRegion",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/map/MapViewModel.kt",
          "line": 94,
          "fragment": "regionPackRepository.exportRegion(map.id).onSuccess"
        }
      },
      {
        "from": "map",
        "to": "domain.share.GpxWriter.write",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/map/MapScreen.kt",
          "line": 148,
          "fragment": "GpxWriter.write(spots, fishNames)"
        }
      },
      {
        "from": "saved-maps",
        "to": "domain.share.GpxParser.parse",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/maps/SavedMapsViewModel.kt",
          "line": 145,
          "fragment": "stream.use(GpxParser::parse)"
        }
      },
      {
        "from": "saved-maps",
        "to": "data.repository.RegionPackRepository.importPack",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/maps/SavedMapsViewModel.kt",
          "line": 133,
          "fragment": "regionPackRepository.importPack(uri).fold("
        }
      },
      {
        "from": "weather",
        "to": "data.repository.FishingContextRepository.refreshWeather",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherViewModel.kt",
          "line": 119,
          "fragment": "fishingContext.refreshWeather().onFailure {"
        }
      },
      {
        "from": "weather",
        "to": "domain.water.calculateWaterState",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/ui/weather/WeatherViewModel.kt",
          "line": 95,
          "fragment": "calculateWaterState(emptyList(), null)"
        }
      }
    ]
  }
}
```

## Не в карте

```docdd-skipped
{
  "files": [
    {
      "path": "app/src/main/res/drawable-nodpi/ic_launcher_image.png",
      "why": "растровая картинка лаунчера, не модуль"
    },
    {
      "path": "app/src/main/res/drawable/ic_launcher_background.xml",
      "why": "векторный слой значка лаунчера, не модуль"
    },
    {
      "path": "app/src/main/res/drawable/ic_launcher_foreground_image.xml",
      "why": "векторный слой значка лаунчера, не модуль"
    },
    {
      "path": "app/src/main/res/drawable/ic_launcher_foreground.xml",
      "why": "векторный слой значка лаунчера, не модуль"
    },
    {
      "path": "app/src/main/res/drawable/ic_launcher_monochrome.xml",
      "why": "векторный слой значка лаунчера, не модуль"
    },
    {
      "path": "app/src/main/res/drawable/ic_notification.xml",
      "why": "векторный силуэт значка уведомлений, не модуль"
    },
    {
      "path": "app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml",
      "why": "описание адаптивного значка лаунчера, не модуль"
    },
    {
      "path": "app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml",
      "why": "описание адаптивного значка лаунчера, не модуль"
    },
    {
      "path": "app/src/main/res/mipmap-anydpi/ic_launcher_round.xml",
      "why": "описание адаптивного значка лаунчера, не модуль"
    },
    {
      "path": "app/src/main/res/mipmap-anydpi/ic_launcher.xml",
      "why": "описание адаптивного значка лаунчера, не модуль"
    },
    {
      "path": "app/src/main/res/values/colors.xml",
      "why": "цвета из шаблона, не модуль"
    },
    {
      "path": "app/src/main/res/values/strings.xml",
      "why": "строковые ресурсы, не модуль"
    },
    {
      "path": "app/src/main/res/values/themes.xml",
      "why": "тема окна до запуска Compose, не модуль"
    },
    {
      "path": "app/src/main/res/xml/backup_rules.xml",
      "why": "правила резервного копирования, не модуль"
    },
    {
      "path": "app/src/main/res/xml/data_extraction_rules.xml",
      "why": "правила переноса данных, не модуль"
    },
    {
      "path": "app/src/main/res/xml/shared_files.xml",
      "why": "пути FileProvider для обмена файлами, не модуль"
    },
    {
      "path": "app/src/test/java/com/example/fishforecast/ExampleUnitTest.kt",
      "why": "заготовка теста из шаблона Android Studio"
    }
  ]
}
```

## Журнал

- 2026-09-12 · заведена черновиком · модель
- 2026-09-12 · на подтверждение · architect
- 2026-09-12 · подтверждён · architect
