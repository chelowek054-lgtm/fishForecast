# Словарь данных и ERD

Что где хранится, какого типа, что означает и насколько это личное. Документ —
общий язык клиента, сервера и админки: если поле называется по-разному в трёх
местах, оно рано или поздно разъедется и по смыслу.

Обозначения приватности:

| Класс | Что значит | Правило |
|---|---|---|
| **P0** | Общее знание | Публикуется свободно: словари, виды, модель клёва |
| **P1** | Знание места | Уходит только с явной публикацией района |
| **P2** | Личное | Уловы, выезды, фото. По умолчанию не покидают устройство |
| **P3** | Идентифицирующее | Учётная запись, контакты. Хранится на сервере |

## Клиент: Room

Схема версии 21. Таблицы `bathymetry_grids` и `sync_outbox` появляются в фазах
8 и 11; остальное уже существует.

```mermaid
erDiagram
    saved_maps ||--o{ weather_forecast : "прогноз района"
    saved_maps ||--o{ daily_sun : "восходы"
    saved_maps ||--o{ fishing_sessions : "выезды"
    saved_maps ||--o{ bathymetry_grids : "промеры"
    fish ||--o{ fishing_spots : "кто здесь берёт"
    fish ||--o{ catches : "кого поймали"
    fishing_spots ||--o{ catches : "где поймали"
    fishing_sessions ||--o{ catches : "в каком выезде"

    saved_maps {
        int id PK
        string uid
        string name
        double bounds
        long offlineRegionId
        double baselinePressureMmHg
        double elevationM
        string waterBodyType
        double shallowDepthM
        double deepDepthM
        double waterTempC
        long createdAt
    }
    fish {
        int id PK
        string uid
        string name
        float optMinTemp
        float optMaxTemp
        float absMinTemp
        float absMaxTemp
        float minPressure
        float maxPressure
        float oxygenComfortMgL
        float oxygenCriticalMgL
        string guild
        string lightActivity
        string preferredStructures
        string defaultHorizon
        float coldTempThreshold
        string baits
        string groundbaitRules
    }
    fishing_spots {
        int id PK
        string uid
        string name
        double latitude
        double longitude
        int fishId FK
        string placement
        string structures
        string note
        long createdAt
    }
    weather_forecast {
        int mapId PK
        string time PK
        double temperature
        double pressure
        double windSpeed
        double windDirection
        double precipitation
        double cloudCover
        double shortwaveRadiation
        int weatherCode
    }
    daily_sun {
        int mapId PK
        string date PK
        string sunrise
        string sunset
    }
    barometer_log {
        string time PK
        double pressure
    }
    fishing_sessions {
        int id PK
        string uid
        int mapId
        long startedAt
        long finishedAt
        int targetFishId
        string methodId
        string layer
        string goal
        bool hasGroundbait
        double waterTempC
        double oxygenMgL
        double pressureMmHg
        string lightPhase
        int biteScore
        string plan
        int caughtCount
        int rating
    }
    catches {
        int id PK
        string uid
        int fishId FK
        int spotId FK
        int sessionId FK
        string photoPath
        int weightGrams
        int lengthCm
        long caughtAt
        double temperature
        double pressureMmHg
        double windSpeed
        double waterTempC
        double oxygenMgL
        string lightPhase
        int biteScore
        string shareState
    }
    bathymetry_grids {
        int id PK
        string uid
        int mapId FK
        string source
        double cellSizeM
        string gridPath
        long measuredAt
    }
    sync_outbox {
        int id PK
        string entityType
        string entityUid
        string operation
        string payload
        int attempts
        long createdAt
    }
```

### Что меняется в клиентской схеме

| Таблица | Изменение | Зачем |
|---|---|---|
| `catches` | Добавляются `uid`, `waterTempC`, `oxygenMgL`, `lightPhase`, `shareState` | Сейчас у улова нет глобального ключа и нет воды с кислородом. Получается, что снимок выезда полнее снимка улова, хотя именно улов — точка сверки прогноза с фактом |
| `sync_outbox` | Новая | Очередь исходящего. Без неё синхронизация превращается в «отправили и надеемся» |
| `bathymetry_grids` | Новая | Промеры эхолота рядом с районом |
| Учётная запись | Новый раздел DataStore | Надстройка над нынешним анонимным `authorId` из `ActiveMapStore` |

### Строки-документы внутри Room

Часть полей хранит JSON строкой: `lightActivity`, `preferredStructures`,
`baitsCold`, `groundbaitWarm`, `structures`, `plan`. Так сделано намеренно: их не
выбирают запросами и не правят по одному значению, а читают и пишут целиком.
Кодеки лежат в
[FishCatalog.kt](../../src/main/java/com/example/fishforecast/domain/fish/FishCatalog.kt).

Правило: **нужен поиск или выборка по полю — оно становится колонкой; это
документ — остаётся строкой.**

## Сервер: PostgreSQL + PostGIS

```mermaid
erDiagram
    users ||--o{ regions : "автор"
    users ||--o{ catches : "автор"
    users ||--o{ wall_posts : "автор"
    users ||--o{ chat_members : "участник"
    regions ||--o{ spots : "внутри"
    regions ||--o{ bathymetry_datasets : "промеры"
    regions ||--o{ region_versions : "история"
    regions ||--o{ chat_rooms : "чат района"
    spots ||--o{ catches : "поймано на"
    catches ||--o| wall_posts : "опубликован как"
    chat_rooms ||--o{ chat_messages : "сообщения"
    chat_rooms ||--o{ chat_members : "участники"
    knowledge_documents ||--o{ knowledge_versions : "версии"
    model_runs ||--o| knowledge_versions : "выпущена обучением"

    users {
        uuid id PK
        string display_name
        string email
        string auth_provider
        int trust_score
        timestamptz created_at
    }
    regions {
        uuid id PK
        uuid author_id FK
        string name
        geography bounds
        double baseline_pressure_mmhg
        double elevation_m
        string water_body_type
        double shallow_depth_m
        double deep_depth_m
        int version
        string visibility
        timestamptz updated_at
    }
    region_versions {
        uuid id PK
        uuid region_id FK
        int version
        jsonb pack
        uuid author_id FK
        timestamptz created_at
    }
    spots {
        uuid id PK
        uuid region_id FK
        string name
        geography location
        string placement
        jsonb structures
        string fish_uid
        string visibility
    }
    catches {
        uuid id PK
        uuid author_id FK
        uuid region_id FK
        uuid spot_id FK
        string fish_uid
        int weight_grams
        int length_cm
        timestamptz caught_at
        jsonb conditions
        string photo_key
        string visibility
    }
    bathymetry_datasets {
        uuid id PK
        uuid region_id FK
        uuid author_id FK
        string source
        string raw_key
        string grid_key
        double cell_size_m
        string status
    }
    knowledge_documents {
        string slug PK
        string schema
        string title
    }
    knowledge_versions {
        uuid id PK
        string document_slug FK
        int version
        string storage_key
        string checksum
        uuid published_by FK
        timestamptz published_at
        string state
    }
    model_runs {
        uuid id PK
        string dataset_snapshot
        jsonb metrics
        jsonb coefficients
        string state
        timestamptz created_at
    }
    wall_posts {
        uuid id PK
        uuid author_id FK
        uuid catch_id FK
        string text
        geography place_hint
        timestamptz created_at
    }
    chat_rooms {
        uuid id PK
        string kind
        uuid region_id FK
        string title
    }
    chat_members {
        uuid room_id PK
        uuid user_id PK
        timestamptz joined_at
        timestamptz last_read_at
    }
    chat_messages {
        uuid id PK
        uuid room_id FK
        uuid author_id FK
        string body
        timestamptz sent_at
    }
```

## Словарь ключевых полей

| Поле | Тип | Смысл | Класс |
|---|---|---|---|
| `uid` у района, точки, вида, улова | UUID строкой | Глобальный ключ обмена и слияния: числовой ключ Room у каждого устройства свой | P0–P2 |
| `regions.version` | int | Счётчик версий района. Побеждает не последний записавший, а тот, кто писал поверх актуальной версии | P1 |
| `baseline_pressure_mmhg` | double | Норма давления места по наблюдениям. Без неё давление и тенденция не оцениваются вовсе — подставлять диапазон рыбы нельзя | P1 |
| `water_body_type` | код словаря | Течение и размер задают физику воды: инерцию, аэрацию, ночной ход кислорода | P0 |
| `shallow_depth_m` / `deep_depth_m` | double | Глубины слоёв. До батиметрии вводятся руками, после — берутся из промера | P1 |
| `catches.conditions` | jsonb | Снимок часа: вода, кислород, давление и его тенденция, ветер, фаза света, оценка клёва, слой, структуры | P2 |
| `catches.visibility` / `shareState` | enum | `private`, `region`, `public`; по умолчанию `private` | P2 |
| `knowledge_versions.state` | enum | `draft`, `published`, `rolled_back`. Клиенту видно только опубликованное | P0 |
| `knowledge_versions.checksum` | строка | Контроль целостности: клиент сверяет скачанный документ до применения | P0 |
| `model_runs.coefficients` | jsonb | Результат обучения в виде коэффициентов эвристики — то, что уедет в документ модели клёва | P0 |
| `users.trust_score` | int | Вес автора: сколько его районов подтверждено чужими уловами | P3 |
| `sync_outbox.payload` | JSON строкой | Готовое тело запроса: отправка не должна зависеть от того, что запись успели изменить или удалить | P1–P2 |

## Хранение, сроки и приватность

| Данные | Где горячие | Архив | Удаление |
|---|---|---|---|
| Прогноз погоды | Room: неделя вперёд и назад | Не архивируется | Перезаписывается синхронизацией |
| Показания барометра | `barometer_log`, неделя | Не архивируется | Кольцевая перезапись |
| Уловы и выезды | Room, бессрочно | На сервере — только опубликованные | По требованию автора, каскадом |
| Фото | Файлы приложения | Объектное хранилище — только у опубликованных | Вместе с уловом |
| Сырые промеры эхолота | Файл на устройстве | Объектное хранилище при публикации | По требованию автора |
| Документы знаний | DataStore: последняя версия | Все версии в объектном хранилище | Не удаляются: по ним воспроизводится старый расчёт |
| Сообщения чатов | Сервер | Сервер | По требованию автора и правилам площадки |

**Персональные данные** — это `users` и содержимое сообщений. Координаты точек
считаются чувствительными отдельно: в ленте место округляется до района, точные
координаты отдаются только тем, кому автор их отдал сам. Право на удаление
реализуется каскадом от `users`, поэтому внешние ключи туда обязательны.

## Правила именования

- В базе клиента — `camelCase` (так требует Room и так уже сложилось), в базе
  сервера и в JSON контрактов — `snake_case`. Перевод делается в одном месте — в
  кодеках обмена.
- Единицы измерения всегда в имени: `..._mmhg`, `..._mg_l`, `..._m`,
  `..._grams`. Расхождение гПа и мм рт. ст. однажды уже стоило ошибки в
  сравнении с барометром.
- Время: на клиенте `long` эпохи для событий и ISO8601 строкой для часов прогноза
  (так приходит Open-Meteo), на сервере — `timestamptz` в UTC.
- География на сервере: точка — `geography(Point, 4326)`, границы района —
  `geography(Polygon, 4326)`.
