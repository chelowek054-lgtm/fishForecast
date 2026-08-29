# Диаграммы последовательности

Пошагово — только то, что ломается интереснее всего: обмен между устройством и
платформой, публикация знаний и обработка промеров. Простые чтения сюда не
попали намеренно.

## 1. Решение «ехать или нет» — без сети

```mermaid
sequenceDiagram
    actor F as Рыболов
    participant UI as Экран «Клёв»
    participant CTX as FishingContextRepository
    participant DB as Room
    participant W as Модель воды
    participant B as Расчёт клёва
    participant K as Кэш знаний

    F->>UI: открыл вкладку
    UI->>CTX: контекст активного района
    CTX->>DB: район, прогноз, восходы
    DB-->>CTX: часы прогноза
    CTX->>W: посчитать воду по слоям и кислород
    W-->>CTX: мель, яма, кислород по часам
    UI->>K: словари и модель клёва
    K-->>UI: действующие версии
    UI->>B: вид, часы, вода, место, модель
    B-->>UI: оценка по часам с причиной каждого фактора
    UI-->>F: «ехать» и куда встать
```

Ни одной стрелки наружу. Если сеть есть, она работала раньше — когда наполняла
базу.

## 2. Публикация района

```mermaid
sequenceDiagram
    actor F as Рыболов
    participant App as Приложение
    participant Q as Очередь исходящего
    participant API as Шлюз
    participant DB as PostGIS
    participant MOD as Проверка публикаций

    F->>App: поделиться районом на сервер
    App->>App: собрать пакет (район, точки, виды)
    App->>Q: операция upsert region + ключ идемпотентности
    Q->>API: POST /v1/regions
    API->>API: проверить схему пакета
    alt схема чужая или новее
        API-->>Q: 422 с причиной
        Q->>App: показать причину, не повторять
    else годится
        API->>DB: сохранить район, версия 1
        API->>MOD: поставить задачу проверки
        API-->>Q: 201 + версия
        Q->>App: снять операцию, записать версию
        MOD->>DB: пометить проверенным или спорным
    end
```

## 3. Синхронизация пачкой и конфликт версий

```mermaid
sequenceDiagram
    participant W as SyncWorker
    participant Q as Очередь
    participant API as Шлюз
    participant DB as PostGIS

    W->>Q: взять до 200 операций
    W->>API: POST /v1/sync/batch
    API->>DB: применить по одной, проверяя базовую версию
    DB-->>API: результаты
    API-->>W: 207, статус на каждую
    loop по результатам
        alt 200/201
            W->>Q: снять операцию, записать новую версию
        else 409 конфликт
            W->>API: GET актуальную запись
            API-->>W: запись и версия
            W->>W: слить непересекающееся
            alt спор по одному полю
                W->>W: отложить и спросить рыболова
            else слилось
                W->>Q: переложить операцию с новой базовой версией
            end
        else 4xx прочее
            W->>Q: снять и показать причину
        end
    end
```

## 4. Выпуск новой модели клёва

```mermaid
sequenceDiagram
    actor E as Эксперт
    participant A as Админка
    participant API as Шлюз
    participant T as Воркер обучения
    participant S3 as Хранилище
    participant D as Устройство

    E->>A: открыть прогон обучения
    A->>API: GET прогоны и метрики
    API-->>A: коэффициенты кандидата
    E->>A: перенести в черновик модели
    A->>API: POST версии (черновик)
    E->>A: прогнать песочницу
    A->>API: POST preview
    API->>T: пересчитать клёв по историческим уловам
    T-->>API: отчёт: действующая против кандидата
    API-->>A: метрики по видам и водоёмам
    alt хуже
        E->>A: править дальше
    else лучше
        E->>A: опубликовать с комментарием
        A->>API: POST publish
        API->>S3: положить документ
        API->>API: отметить версию актуальной
        D->>API: GET latest (If-None-Match)
        API-->>D: 200, документ, ETag
        D->>D: проверить схему и сумму
        alt негоден
            D->>D: остаться на прежнем, записать причину
        else годен
            D->>D: применить, расчёт идёт по новым весам
        end
    end
```

## 5. Промер эхолота

```mermaid
sequenceDiagram
    actor F as Рыболов
    participant App as Приложение
    participant API as Шлюз
    participant S3 as Хранилище
    participant BW as Воркер промеров
    participant DB as PostGIS

    F->>App: выбрать файл выгрузки
    App->>App: разобрать в точки, отсеять мусор
    alt промер небольшой
        App->>App: интерполировать на устройстве
        App-->>F: изобаты и глубины слоёв сразу
    else большой или несколько заездов
        App->>API: POST /v1/media/upload-url
        API-->>App: подписанная ссылка
        App->>S3: положить файл напрямую
        App->>API: POST /v1/regions/{uid}/bathymetry
        API->>BW: задача обработки
        API-->>App: 202, набор в состоянии queued
        BW->>S3: забрать сырые точки
        BW->>BW: отсев, интерполяция, изобаты
        BW->>S3: положить сетку
        BW->>DB: состояние ready
        BW-->>App: bathymetry.ready по WebSocket
        App->>API: забрать сетку
        App-->>F: изобаты на карте
    end
```

## 6. Улов в ленте

```mermaid
sequenceDiagram
    actor F as Рыболов
    participant App as Приложение
    participant DB as Room
    participant API as Шлюз
    participant WS as Realtime
    participant Others as Другие рыболовы

    F->>App: записать улов
    App->>DB: улов со снимком часа, visibility=private
    Note over App,DB: на этом сценарий заканчивается,<br/>если делиться не захотели
    F->>App: опубликовать в ленту
    App->>API: POST /v1/media/upload-url (фото)
    App->>API: POST /v1/catches + POST /v1/wall
    API->>API: округлить место до района
    API-->>App: запись создана
    API->>WS: wall.updated подписчикам
    WS-->>Others: в ленте новое
```

## 7. Приём чужого района файлом

```mermaid
sequenceDiagram
    actor F as Рыболов
    participant App as Приложение
    participant DB as Room

    F->>App: открыть .ffpack
    App->>App: проверить схему
    alt поколение не то
        App-->>F: отказ с понятной причиной
    else годится
        App->>DB: район по uid — обновить или завести
        App->>DB: точки по uid
        App->>DB: виды: неизвестные завести, известные не трогать
        App-->>F: «принято: точек N, видов M, тайлы докачаются при сети»
    end
```

Сервер в этом сценарии не участвует вовсе — и не должен.
