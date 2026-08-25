# 🎣 FishForecast: Стратегия развития

## 🎯 Vision & Core Value
Создание **автономного экспертного ассистента**. Приложение не просто показывает погоду, а интерпретирует её через призму ихтиологии, помогая рыболову принять решение "ехать или нет" в условиях полной изоляции от сети.

---

## 🏗 Техническая архитектура (Verified)
*   **Modern UI:** Jetpack Compose + **Type-Safe Navigation**.
*   **Reliable Data:** Room (SSOT) + DataStore (Settings).
*   **Predictive Core:** Clean Architecture (UseCases для расчета Bite Score).
*   **Offline First:** Retrofit + WorkManager для предзагрузки прогнозов на 7 дней.
*   **Sensors:** Интеграция системного барометра для уточнения прогноза.

---

## 📍 Дорожная карта (Progress Tracker)

### ✅ Фаза 1: Базис и Знания (Done)
*   [x] Настройка DI (Hilt) и модулей данных.
*   [x] Интерактивный справочник рыб (CRUD + Room).
*   [x] Preload-механизм (базовая база рыб в комплекте).

### 🏗 Фаза 2: Метео-интеллект (Current)
*   [x] Сетевой слой (Retrofit + KotlinX Serialization).
*   [x] Локальный кэш погоды (WeatherEntity + DAO).
*   [x] Реактивный LocationTracker (FusedLocationProvider).
*   [x] **NEW:** Миграция на Type-Safe Navigation.
*   [ ] **NEW:** Датчик барометра (Local Pressure Provider).
*   [x] WorkManager Sync (Фоновое обновление раз в 6 часов).

### ⏳ Фаза 3: Гео-визуализация (Offline Maps)
*   [ ] Интеграция MapLibre/OSM.
*   [ ] Менеджер офлайн-областей (скачивание тайлов в кэш).
*   [ ] Слой "Секретные точки" с привязкой к истории уловов.

### ⏳ Фаза 4: Аналитическое ядро "Bite Score"
*   [ ] Разработка `CalculateFishActivityUseCase`.
*   [ ] Алгоритм оценки давления (анализ градиента изменения за 3 часа).
*   [ ] Визуализация: Графики активности по часам.

### ⏳ Фаза 5: Экосистема рыболова
*   [ ] Журнал трофеев (Photo + Weather Metadata).
*   [ ] Smart Notifications: "Давление стабилизировалось — щука активна!".

---

## ⚠️ Технические вызовы
1.  **Battery Management:** Использование `Balanced Power` для GPS.
2.  **Storage:** Векторные карты вместо растровых для экономии места.
3.  **Data Quality:** Сравнение данных Open-Meteo с локальным барометром.

---
**Текущий статус:** 🛰 Location & Weather работают, прогноз обновляется в фоне.
**Следующий фокус:** Барометр (Local Pressure Provider) и закрытие Фазы 2.

> ⚠️ Открытый вопрос к Фазе 4: справочник рыб хранит пороги давления в мм рт. ст.,
> а Open-Meteo отдаёт `pressure_msl` в гПа. Единицы нужно свести до расчёта Bite Score.
