# jsonfusion — Roadmap

## Этапы

### 1. Скелет
- Backend: Spring Boot + Gradle, `/api/ping`.
- Frontend: React + TypeScript + Vite + Chakra UI, wizard-роутинг.
- **Итог:** приложение запускается, ping отвечает.

### 2. Сессии
- `MergeSession`, `SessionStatus`, `SessionStore` (Caffeine).
- `POST /api/sessions`, `GET`, `DELETE`.
- Frontend: создание сессии, `sessionStorage`.
- **Итог:** сессия создаётся и живёт.

### 3. Загрузка файлов
- `JsonDocument`, `JsonRecord`, `JsonParser` (Jackson).
- `FileService`, `POST /files`.
- Frontend: `UploadPage` с drag&drop.
- **Итог:** два файла грузятся, парсятся, показывается анализ полей.

### 4. Конфиг полей
- `MergeConfig`, `FieldRule`, `FieldRole`, `PriorityMode`, `ComparisonSettings`, `NewField`.
- `FieldsAnalyzerService`, `GET /fields`, `PUT /config`.
- Frontend: `FieldsConfigPage` — таблица полей, роли, условия, новые поля.
- **Итог:** пользователь настраивает роли.

### 5. Merge
- `MergeEngine`, `AnchorMatcher`, `FieldMerger`, `RecordMerger`, `JsonValueNormalizer`.
- `MergeResult`, `UnmatchedRecord`, `UnmatchedType`.
- `MergeService`, `POST /merge`.
- Юнит-тесты на движок.
- **Итог:** merge работает, есть результат и несовпавшие.

### 6. Несовпавшие
- `UnmatchedPair`, `UnmatchedDecision`, `UnmatchedResolver`.
- `GET /unmatched`, `PUT /unmatched/decisions`.
- Frontend: `UnmatchedPage` — два списка, метки, игноры.
- **Итог:** пользователь обрабатывает несовпавшие.

### 7. Финал
- `FinalizeService`, `POST /finalize`, `GET /result`.
- Frontend: `ResultPage` + скачивание.
- **Итог:** готовый третий JSON.

### 8. Деплой *(позже)*
- Dockerfile backend + frontend.
- GitHub Actions → GHCR.
- DNS `jsonfusion.npepub.ru`, wildcard-сертификат.
- nginx + docker-compose рядом с npepub.
- Карточка в портфолио.

## Порядок

Идём **строго по этапам**. Каждый этап:
1. Код.
2. Проверка (запуск, тесты).
3. Коммит.

Не перескакиваем. Если что-то меняется — правим `docs/` и продолжаем.