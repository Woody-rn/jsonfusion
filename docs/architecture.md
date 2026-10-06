# jsonfusion — Архитектура

## 1. Общая картина

```
┌─────────────────────────────────────────────────────────────┐
│                        FRONTEND (React)                     │
│  TypeScript + Chakra UI + Zustand + Vite                    │
│  sessionId в sessionStorage                                 │
└──────────────────────┬──────────────────────────────────────┘
                       │ REST (JSON)
┌──────────────────────▼──────────────────────────────────────┐
│                       BACKEND (Spring Boot)                 │
│                                                             │
│  api → domain(service → engine → model) → port → infra      │
│                                                             │
│  SessionStore (Caffeine) — состояние сессий                 │
│  Jackson — парсинг JSON                                     │
└─────────────────────────────────────────────────────────────┘
```

## 2. Слои backend

```
api/            — вход (REST): контроллеры, DTO, обработка ошибок
domain/         — ядро: model, engine, service
port/           — интерфейсы к внешнему миру
infrastructure/ — реализации port'ов (Caffeine, Jackson)
config/         — Spring-конфигурация
```

**Принцип:** домен **не знает** про инфраструктуру. Домен говорит «мне нужен
`SessionStore`», реализация приходит снаружи (Spring инжектит).

## 3. Backend — дерево пакетов

```
ru.npepub.jsonfusion
│
├── JsonfusionApplication.java
│
├── api/
│   ├── controller/
│   │   ├── SessionController.java
│   │   ├── FileController.java
│   │   ├── ConfigController.java
│   │   ├── MergeController.java
│   │   ├── UnmatchedController.java
│   │   └── ResultController.java
│   ├── dto/
│   │   ├── request/
│   │   └── response/
│   └── GlobalExceptionHandler.java
│
├── domain/
│   ├── model/
│   │   ├── MergeSession.java
│   │   ├── SessionStatus.java
│   │   ├── JsonDocument.java
│   │   ├── JsonRecord.java
│   │   ├── MergeConfig.java
│   │   ├── FieldRule.java
│   │   ├── FieldRole.java
│   │   ├── PriorityMode.java
│   │   ├── ComparisonSettings.java
│   │   ├── MergeResult.java
│   │   ├── UnmatchedRecord.java
│   │   ├── UnmatchedType.java
│   │   ├── UnmatchedPair.java
│   │   ├── UnmatchedDecision.java
│   │   └── NewField.java
│   │
│   ├── engine/
│   │   ├── MergeEngine.java
│   │   ├── AnchorMatcher.java
│   │   ├── FieldMerger.java
│   │   ├── RecordMerger.java
│   │   ├── JsonValueNormalizer.java
│   │   └── UnmatchedResolver.java
│   │
│   └── service/
│       ├── SessionService.java
│       ├── FileService.java
│       ├── FieldsAnalyzerService.java
│       ├── MergeService.java
│       └── FinalizeService.java
│
├── port/
│   ├── SessionStore.java
│   └── JsonParser.java
│
├── infrastructure/
│   ├── session/
│   │   └── CaffeineSessionStore.java
│   └── json/
│       └── JacksonJsonParser.java
│
└── config/
    ├── JacksonConfig.java
    ├── CaffeineConfig.java
    └── WebConfig.java
```

## 4. Принципы (SOLID)

- **S** — каждый класс одна ответственность (SessionService ≠ MergeEngine).
- **O** — расширение через новые реализации port'ов, не правку существующих.
- **L** — реализации SessionStore взаимозаменяемы (Caffeine, Redis).
- **I** — интерфейсы маленькие и по делу.
- **D** — зависимости от абстракций (SessionStore), не от реализаций (Caffeine).

## 5. Ключевые компоненты

### 5.1. MergeSession

Состояние одной операции мержа. Хранится в SessionStore.

Содержит:
- `id` (UUID v4),
- `status` (SessionStatus),
- `file1`, `file2` (JsonDocument),
- `config` (MergeConfig),
- `result` (MergeResult),
- `decision` (UnmatchedDecision),
- `createdAt`, `lastAccessAt`.

### 5.2. MergeEngine

Сердце домена. Чистый компонент, без Spring-зависимостей.

**Вход:** `JsonDocument × 2 + MergeConfig`
**Выход:** `MergeResult` (merged + unmatched)

**Логика:**
1. Сгруппировать записи F1 и F2 по нормализованному якорю.
2. Для каждого ключа:
    - ровно 1 в F1 и ровно 1 в F2 → matched.
    - иначе → все в unmatched (тип DUPLICATE или ONLY_IN_*).
3. Для matched — применить FieldMerger (роли, условия).
4. Вернуть результат.

Тестируется юнит-тестами, без Spring.

### 5.3. SessionStore (port)

Интерфейс:

```java
public interface SessionStore {
    MergeSession create();
    Optional<MergeSession> get(String id);
    void save(MergeSession session);
    void delete(String id);
}
```

Реализация — `CaffeineSessionStore`. Позже можно `RedisSessionStore`.

### 5.4. JsonParser (port)

Интерфейс:

```java
public interface JsonParser {
    JsonDocument parse(InputStream input);
}
```

Реализация — `JacksonJsonParser`.

## 6. Frontend — структура

```
src/
├── main.tsx
├── App.tsx                 — роутинг
├── api/                    — обёртки над REST
├── store/                  — Zustand (session, fields, unmatched)
├── pages/                  — шаги wizard
│   ├── UploadPage.tsx
│   ├── FieldsConfigPage.tsx
│   ├── UnmatchedPage.tsx
│   └── ResultPage.tsx
├── components/
│   ├── layout/
│   ├── upload/
│   ├── fields/
│   ├── unmatched/
│   └── result/
├── hooks/
└── types/
```

**Роутинг:**
- `/` → upload
- `/fields` → config
- `/unmatched` → несовпавшие
- `/result` → результат

**Состояние:** Zustand, 3 слайса + `sessionId` в `sessionStorage`.

## 7. Поток данных

```
1. Фронт: POST /api/sessions              → sessionId
2. Фронт: POST /api/sessions/{id}/files   → анализ полей
3. Фронт: GET  /api/sessions/{id}/fields  → показать поля
4. Фронт: PUT  /api/sessions/{id}/config  → сохранить роли
5. Фронт: POST /api/sessions/{id}/merge   → результат + несовпавшие
6. Фронт: GET  /api/sessions/{id}/unmatched
7. Фронт: PUT  /api/sessions/{id}/unmatched/decisions
8. Фронт: POST /api/sessions/{id}/finalize
9. Фронт: GET  /api/sessions/{id}/result  → скачать
```

## 8. Нефункциональные решения

| Аспект | Решение |
|--------|---------|
| Сессии | Caffeine in-memory, TTL 15–30 мин, max 2 ч, лимит 100 |
| Сессия на вкладку | `sessionStorage` |
| Авторизация | нет |
| Инстанс | один |
| Формат | массив плоских JSON-объектов |
| Лимит файла | 5 МБ |
| CORS | разрешён для dev (`localhost:5173`) |