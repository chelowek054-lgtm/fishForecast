---
id: R-0001
type: requirement
title: Коэффициенты модели клёва меняются без релиза приложения
status: approved
owner: architect
created: 2026-08-30
updated: 2026-08-30
tags: [bite-model, knowledge]
links:
  verified_by: [V-0001]
---

# Коэффициенты модели клёва меняются без релиза приложения

Веса факторов и допуски — это знание, которое уточняется по журналу уловов, а не
алгоритм. Пока они лежат константами в
[CalculateFishActivityUseCase.kt](../../../app/src/main/java/com/example/fishforecast/domain/bite/CalculateFishActivityUseCase.kt),
любая правка стоит релиза, а сравнить две версии на одних данных нельзя вовсе.

Требование считается выполненным, когда подмена документа модели меняет расчёт,
а встроенный документ даёт в точности прежние числа.

Основание: [ADR-0003](../../../app/docs/architecture/adr/0003-bite-model-out-of-code.md).
