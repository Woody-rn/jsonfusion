# jsonfusion — API

## Общие правила

- **Базовый URL:** `/api`
- **Формат:** JSON (`application/json`), кроме загрузки файлов (`multipart/form-data`).
- **Идентификатор сессии:** в path — `/api/sessions/{id}/...`
- **Версионирование:** не используется (фронт и бэк деплоятся вместе).
- **Полные контракты:** в коде контроллеров и DTO.

### Формат ошибок

```json
{
  "error": "INVALID_SESSION_STATE",
  "message": "Cannot merge: session is not configured",
  "timestamp": "2026-10-06T12:00:00Z"
}
```

**Коды ошибок:**

| Код | HTTP | Смысл |
|-----|------|-------|
| `SESSION_NOT_FOUND` | 404 | Сессия не найдена |
| `INVALID_SESSION_STATE` | 409 | Операция недопустима в текущем статусе |
| `FILE_TOO_LARGE` | 413 | Файл больше 5 МБ |
| `INVALID_JSON` | 400 | Файл не парсится |
| `VALIDATION_ERROR` | 400 | Ошибка валидации запроса |
| `LIMIT_EXCEEDED` | 429 | Превышен лимит сессий |

## Эндпоинты

| Метод | Путь | Назначение |
|-------|------|-----------|
| `POST` | `/api/sessions` | Создать сессию |
| `GET` | `/api/sessions/{id}` | Статус сессии |
| `DELETE` | `/api/sessions/{id}` | Удалить сессию |
| `POST` | `/api/sessions/{id}/files` | Загрузить два файла |
| `GET` | `/api/sessions/{id}/fields` | Анализ полей |
| `PUT` | `/api/sessions/{id}/config` | Сохранить конфиг |
| `POST` | `/api/sessions/{id}/merge` | Запустить merge |
| `GET` | `/api/sessions/{id}/unmatched` | Получить несовпавшие |
| `PUT` | `/api/sessions/{id}/unmatched/decisions` | Сохранить решения |
| `POST` | `/api/sessions/{id}/finalize` | Финализировать |
| `GET` | `/api/sessions/{id}/result` | Скачать результат |

## Ключевые примеры

### Создать сессию

`POST /api/sessions` → `201 Created`

```json
{
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "CREATED",
  "createdAt": "2026-10-06T12:00:00Z"
}
```

### Загрузить файлы

`POST /api/sessions/{id}/files` (multipart: `file1`, `file2`) → `200 OK`

```json
{
  "sessionId": "550e8400-...",
  "status": "FILES_UPLOADED",
  "file1": { "recordsCount": 42, "fields": ["name", "status", "updatedAt"] },
  "file2": { "recordsCount": 38, "fields": ["name", "status", "leadId", "updatedAt"] }
}
```

### Запустить merge

`POST /api/sessions/{id}/merge` → `200 OK`

```json
{
  "sessionId": "550e8400-...",
  "status": "MERGED",
  "summary": {
    "matchedCount": 35,
    "unmatchedLeftCount": 7,
    "unmatchedRightCount": 3
  }
}
```

### Сохранить решения по несовпавшим

`PUT /api/sessions/{id}/unmatched/decisions` → `200 OK`

Request:

```json
{
  "pairs": [ { "leftId": "L1", "rightId": "R1", "label": "1" } ],
  "ignoredIds": ["L2"]
}
```

### Финализировать

`POST /api/sessions/{id}/finalize` → `200 OK`

```json
{
  "sessionId": "550e8400-...",
  "status": "FINALIZED",
  "resultSummary": {
    "totalRecords": 45,
    "fromMatched": 35,
    "fromPairs": 1,
    "fromUnmatchedLeft": 6,
    "fromUnmatchedRight": 3,
    "ignoredCount": 1
  }
}
```

### Скачать результат

`GET /api/sessions/{id}/result` → `200 OK`, `application/json`, файл `merged-result.json`.