---
id: M-0010
type: map
title: Уведомления зовут за видами рыболова
status: review
created: 2026-09-12
updated: 2026-09-12
intent: true
---

# Уведомления зовут за видами рыболова

Что меняется в устройстве для T-0004. Новых модулей и связей между модулями
нет: воркер уведомлений и так опирается на политику (`domain/alert`) и
справочник видов (`FishRepository` → `FishDao`). Меняется одно — справочник
начинает читать три чужие таблицы, чтобы узнать, какие виды рыболов отметил у
точек, в уловах и в выездах.

Карта описывает план: `intent: true` снимается, когда код написан.

## Потоки данных

```docdd-dataflow
{
  "added": {
    "flows": [
      {
        "from": "app/src/main/java/com/example/fishforecast/data/local/dao/FishDao.kt",
        "to": "room-fishing-spots",
        "direction": "read",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/local/dao/FishDao.kt",
          "line": 23,
          "fragment": "FROM fishing_spots"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/local/dao/FishDao.kt",
        "to": "room-catches",
        "direction": "read",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/local/dao/FishDao.kt",
          "line": 24,
          "fragment": "FROM catches"
        }
      },
      {
        "from": "app/src/main/java/com/example/fishforecast/data/local/dao/FishDao.kt",
        "to": "room-fishing-sessions",
        "direction": "read",
        "evidence": {
          "path": "app/src/main/java/com/example/fishforecast/data/local/dao/FishDao.kt",
          "line": 25,
          "fragment": "FROM fishing_sessions"
        }
      }
    ]
  }
}
```

## Журнал

- 2026-09-12 · заведена · модель
- 2026-09-12 · на подтверждение · модель
