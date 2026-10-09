# jsonfusion — Домен

## Сущности (model/)

Расположены в подпапках по смыслу.

### `model/session/`

| Сущность | Назначение |
|----------|-----------|
| `MergeSession` | Состояние одной операции мержа |
| `SessionStatus` | Enum: статус сессии |

### `model/json/`

| Сущность | Назначение |
|----------|-----------|
| `JsonDocument` | JSON-файл (массив записей) |
| `JsonRecord` | Одна запись |

### `model/config/`

| Сущность | Назначение |
|----------|-----------|
| `MergeConfig` | Конфиг мержа |
| `FieldRule` | Правило для поля |
| `FieldRole` | Enum: ANCHOR, PRIORITY_F1, PRIORITY_F2, DELETE |
| `PriorityMode` | Enum: ALWAYS, IF_EQUALS |
| `ComparisonSettings` | Настройки сравнения якоря |
| `NewField` | Добавляемое поле |

### `model/result/`

| Сущность | Назначение |
|----------|-----------|
| `MergeResult` | Результат merge |
| `Finalization` | Финальный результат: основной + опциональный файл нерешённых |

### `model/unmatched/`

| Сущность | Назначение |
|----------|-----------|
| `UnmatchedRecord` | Несовпавшая запись |
| `UnmatchedType` | Enum: ONLY_IN_LEFT, ONLY_IN_RIGHT, DUPLICATE |
| `UnmatchedPair` | Ручная пара (1:1) |
| `UnmatchedDecision` | Решения пользователя |

## Движок (engine/)

| Компонент | Назначение |
|-----------|-----------|
| `MergeEngine` | Оркестратор merge |
| `AnchorMatcher` | Группировка и сопоставление по якорю |
| `FieldMerger` | Применение правила к полю |
| `RecordMerger` | Слияние matched-записей |
| `JsonValueNormalizer` | Нормализация значений якоря |

## Сервисы (service/)

| Сервис | Назначение |
|--------|-----------|
| `SessionService` | Жизненный цикл сессий |
| `FileService` | Приём и парсинг файлов |
| `FieldsAnalyzerService` | Анализ полей, дефолтный конфиг |
| `ConfigService` | Валидация и сохранение конфига |
| `MergeService` | Оркестрация merge |
| `UnmatchedService` | Получение и сохранение решений по несовпавшим |
| `FinalizeService` | Сборка финального результата |

## Поток домена

```
1. SessionService.create()             → MergeSession (CREATED)
2. FileService.upload()                → JsonDocument × 2 (FILES_UPLOADED)
3. FieldsAnalyzerService.analyze()     → MergeConfig (дефолты)
4. ConfigService.update()              → MergeConfig (сохранён) (CONFIGURED)
5. MergeService.merge()                → MergeResult (MERGED)
6. UnmatchedService.saveDecision()     → UnmatchedDecision (UNMATCHED_RESOLVED)
7. FinalizeService.finalizeSession()   → FinalizeOutcome (FINALIZED)
```