# jsonfusion — Домен

## Сущности (model/)

| Сущность | Назначение |
|----------|-----------|
| `MergeSession` | Состояние одной операции мержа |
| `SessionStatus` | Enum: статус сессии |
| `JsonDocument` | JSON-файл (массив записей) |
| `JsonRecord` | Одна запись |
| `MergeConfig` | Конфиг мержа |
| `FieldRule` | Правило для поля |
| `FieldRole` | Enum: ANCHOR, PRIORITY_F1, PRIORITY_F2, DELETE |
| `PriorityMode` | Enum: ALWAYS, IF_EQUALS |
| `ComparisonSettings` | Настройки сравнения якоря |
| `NewField` | Добавляемое поле |
| `MergeResult` | Результат merge |
| `UnmatchedRecord` | Несовпавшая запись |
| `UnmatchedType` | Enum: ONLY_IN_LEFT, ONLY_IN_RIGHT, DUPLICATE |
| `UnmatchedPair` | Ручная пара (1:1) |
| `UnmatchedDecision` | Решения пользователя |

## Движок (engine/)

| Компонент | Назначение |
|-----------|-----------|
| `MergeEngine` | Оркестратор merge |
| `AnchorMatcher` | Сопоставление по якорю |
| `FieldMerger` | Применение правила к полю |
| `RecordMerger` | Слияние двух записей |
| `JsonValueNormalizer` | Нормализация значений |
| `UnmatchedResolver` | Применение решений пользователя |

## Сервисы (service/)

| Сервис | Назначение |
|--------|-----------|
| `SessionService` | Жизненный цикл сессий |
| `FileService` | Приём и парсинг файлов |
| `FieldsAnalyzerService` | Анализ полей |
| `MergeService` | Оркестрация merge |
| `FinalizeService` | Сборка результата |

## Поток домена

```
1. SessionService.create()          → MergeSession (CREATED)
2. FileService.upload()             → JsonDocument × 2 (FILES_UPLOADED)
3. FieldsAnalyzerService.analyze()  → MergeConfig дефолты
4. MergeService.merge()             → MergeResult (MERGED)
5. UnmatchedResolver.apply()        → MergeResult финал (UNMATCHED_RESOLVED)
6. FinalizeService.finalize()       → JsonDocument результат (FINALIZED)
```