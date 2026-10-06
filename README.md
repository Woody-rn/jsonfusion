# jsonfusion

Интерактивный инструмент для мержа двух JSON-файлов в третий.

Пользователь настраивает правила через web-интерфейс, видит несовпавшие записи,
правит вручную, получает результат.

## Документация

- [Требования](docs/requirements.md)
- [Архитектура](docs/architecture.md)
- [Домен](docs/domain.md)
- API *(в работе)*
- Roadmap *(в работе)*

## Стек

- **Backend:** Java 21, Spring Boot 3.4, Gradle (Groovy DSL)
- **Frontend:** React, TypeScript, Vite, Chakra UI, Zustand
- **Сессии:** Caffeine (in-memory)

## Статус

В разработке.