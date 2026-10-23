# jsonfusion

Интерактивный веб-инструмент для мержа двух JSON-файлов с автоматическим сопоставлением полей.

🔗 **Live demo:** https://jsonfusion.npepub.ru

## Что делает

- Загружаешь 2 JSON-файла (массивы объектов).
- Система анализирует поля и предлагает дефолтный конфиг.
- Настраиваешь роли полей, якорь, условия переноса.
- Запускаешь merge, видишь несовпавшие записи.
- Вручную спариваешь / игнорируешь.
- Скачиваешь результат.

## Стек

**Backend:** Java 21, Spring Boot 3.4, Gradle (Groovy), Caffeine, Jackson
**Frontend:** React 19, TypeScript, Vite, Chakra UI, Zustand
**Tests:** JUnit 5, Mockito, AssertJ, Vitest, React Testing Library
**DevOps:** Docker, Docker Compose, GitHub Actions, GHCR, Nginx

## Архитектура

- Чистая архитектура (Ports & Adapters).
- Domain не знает про Spring/HTTP.
- SOLID, Dependency Inversion.
- In-memory сессии (Caffeine).
- Подробнее — [docs/architecture.md](docs/architecture.md).

## Документация

- [Требования](docs/requirements.md)
- [Архитектура](docs/architecture.md)
- [Домен](docs/domain.md)
- [API](docs/api.md)
- [Roadmap](docs/roadmap.md)
- [ADR](docs/adr/) 

## Запуск локально

```bash
git clone https://github.com/Woody-rn/jsonfusion.git
cd jsonfusion
docker compose up --build