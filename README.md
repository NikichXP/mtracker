# mtracker

Трекер Mythic+ статистики гильдии и друзей в World of Warcraft на основе публичного API
[Raider.IO](https://raider.io/api). Собирает данные с фиксированным интервалом, хранит их в MongoDB
и отдаёт агрегированную статистику по игрокам (основной персонаж + альты) через REST API.

Монорепозиторий: бэкенд — в корне, дашборд-фронтенд (React + MUI) — в
[`frontend/`](frontend/README.md). Два независимых CI-пайплайна (`.github/workflows/backend.yaml`,
`.github/workflows/frontend.yaml`), один self-hosted раннер — см. раздел «CI/CD» ниже
(конвенции полностью повторяют `okx-service`).

## Стек

- **Kotlin 2.4.10**, **JDK 21** (через `jvmToolchain(21)`)
- **Spring Boot 4.0.7**: `spring-boot-starter-webflux` (WebClient для вызовов Raider.IO + Netty),
  `spring-boot-starter-data-mongodb` (блокирующий `MongoRepository`/`MongoTemplate`),
  `spring-boot-starter-actuator`
- JSON: Jackson (`jackson-module-kotlin`)
- Сборка: **Gradle 9.x** (wrapper)

## Конфигурация (`src/main/resources/application.yaml`)

Регион Raider.IO — единственная глобальная настройка (`mtracker.region` / `MTRACKER_REGION`,
по умолчанию `eu`; однорегиональное развёртывание). Остальные process-level настройки (интервал
синхронизации, S2S/admin токены и т.д.) тоже задаются через переменные окружения — см.
`.env.example`.

**Какие гильдии и персонажи отслеживаются — больше не статический конфиг, а данные в MongoDB**
(`TrackedGuild` / `TrackedPlayer`), управляемые через CRUD REST API — см. раздел «Admin API» ниже.
Список гильдий/персонажей можно менять на лету, без передеплоя.

### Локальный `.env`

Для локальной разработки переменные окружения можно не экспортировать вручную, а положить в
файл `.env` в корне репозитория — он подхватывается автоматически через
`spring.config.import: "optional:file:.env[.properties]"` (см. `application.yaml`), без
дополнительных зависимостей. Достаточно:

```bash
cp .env.example .env
# отредактировать .env под себя (как минимум MTRACKER_ADMIN_TOKEN)
./gradlew bootRun
# и добавить хотя бы одну гильдию/персонажа через Admin API (см. ниже)
```

`.env` в `.gitignore` и никогда не коммитится — храните в нём реальные значения (пароли Mongo,
S2S/admin-токены и т.д.), а `.env.example` держите в актуальном состоянии как шаблон без секретов.

Явные связи "основной персонаж → альты" (`TrackedPlayer.characterKeys`, первый элемент — main)
настраиваются только через Admin API — у публичного Raider.IO API **нет** поля, официально
связывающего персонажа с его альтами, поэтому группировка по игрокам полностью зависит от того,
что явно указано в `TrackedPlayer` (см. KDoc в `PlayerLinkService`).

## Синхронизация

`SyncScheduler` запускает `SyncOrchestrator.runFullSync()` раз в `mtracker.sync.interval-hours`
(по умолчанию 6ч) и один раз сразу после старта приложения. Между запросами к Raider.IO —
пауза `mtracker.sync.request-delay-ms` (по умолчанию 250мс), чтобы не превышать лимит ~300 запросов/мин.
Ручной запуск синхронизации — `POST /api/v1/sync/run`.

## REST API

| Метод | Путь                            | Что делает                                                    |
|-------|---------------------------------|---------------------------------------------------------------|
| GET   | `/api/v1/stats/overview`        | Список игроков (гильдия + друзья) с агрегированными метриками |
| GET   | `/api/v1/stats/players/{key}`   | Детальная разбивка по персонажам (основной + альты)           |
| GET   | `/api/v1/stats/weekly?week=...` | Недельная статистика для сравнения прогресса неделя-к-неделе  |
| GET   | `/api/v1/stats/weeks`           | Список доступных недель (для селектора на фронтенде)          |
| POST  | `/api/v1/sync/run`              | Запускает полную синхронизацию в фоне                         |

CORS открыт полностью (`*` для origin/methods/headers) — все GET-эндпоинты дашборда read-only и
публичны по дизайну, а S2S-роуты защищены токеном (см. ниже), так что ограничивать CORS смысла нет.

## S2S API (для tg-bot и других внутренних сервисов)

Отдельная группа эндпоинтов `/api/v1/s2s/*` — те же данные, что и в публичном dashboard API, но
защищённые общим секретом (`S2sAuthFilter`), а не CORS. Каждый запрос должен содержать
`Authorization: Bearer <MTRACKER_S2S_TOKEN>`. Пока `MTRACKER_S2S_TOKEN` не задан (пусто по умолчанию),
эти роуты возвращают `401` на любой запрос — "fail closed".

| Метод | Путь                              | Что делает                                  |
|-------|-----------------------------------|---------------------------------------------|
| GET   | `/api/v1/s2s/stats/overview`      | То же, что публичный `/stats/overview`      |
| GET   | `/api/v1/s2s/stats/players/{key}` | То же, что публичный `/stats/players/{key}` |

Токен общий для mtracker-service и потребителя (tg-bot, модуль `com.nikichxp.tgbot.wowstats`),
хранится как k8s Secret в обоих неймспейсах (`infra-scripts/manifests/mtracker/06-s2s-secret.yaml`,
ключ `MTRACKER_S2S_TOKEN`, и `infra-scripts/manifests/tgbot/07-wowstats-s2s-secret.yaml`, ключ
`WOW_STATS_TOKEN`) — имена ключей на сторонах разные, но **значения должны совпадать**.

## Admin API (управление отслеживаемыми гильдиями/персонажами)

CRUD над тем, *что именно* отслеживает mtracker: `TrackedGuild` (гильдии, ростер которых
подтягивается целиком) и `TrackedPlayer` (конкретные персонажи/друзья — main + альты, поле
`characterKeys`, первый элемент — main). `RosterResolver` на каждой синхронизации строит список
персонажей на лету из этих коллекций — никакого статического YAML-конфига гильдии/друзей больше нет.

Защищено отдельным токеном (`AdminAuthFilter`, отдельным от S2S, т.к. даёт право на запись, а не
только чтение): каждый запрос должен содержать `Authorization: Bearer <MTRACKER_ADMIN_TOKEN>`.
Пока `MTRACKER_ADMIN_TOKEN` не задан (пусто по умолчанию), `/api/v1/admin/**` возвращает `401` на
любой запрос — "fail closed", как и `S2sAuthFilter`.

| Метод  | Путь                                | Что делает                                  |
|--------|-------------------------------------|----------------------------------------------|
| GET    | `/api/v1/admin/guilds`              | Список отслеживаемых гильдий                |
| POST   | `/api/v1/admin/guilds`              | Добавить гильдию (`{"name", "realm"}`)      |
| GET    | `/api/v1/admin/guilds/{id}`         | Получить одну гильдию                       |
| PUT    | `/api/v1/admin/guilds/{id}`         | Обновить гильдию                            |
| DELETE | `/api/v1/admin/guilds/{id}`         | Убрать гильдию из отслеживания              |
| GET    | `/api/v1/admin/tracked-players`     | Список отслеживаемых персонажей/друзей      |
| POST   | `/api/v1/admin/tracked-players`     | Добавить (`{"displayName", "characterKeys": ["Main-Realm", "Alt-Realm"], "isFriend": true}`) |
| GET    | `/api/v1/admin/tracked-players/{id}`| Получить одну запись                        |
| PUT    | `/api/v1/admin/tracked-players/{id}`| Обновить запись                             |
| DELETE | `/api/v1/admin/tracked-players/{id}`| Убрать персонажа из отслеживания            |

Пример добавления гильдии локально:

```bash
curl -X POST localhost:8080/api/v1/admin/guilds \
  -H "Authorization: Bearer $MTRACKER_ADMIN_TOKEN" -H "Content-Type: application/json" \
  -d '{"name":"Бладлайн","realm":"Gordunni"}'
```

## CI/CD и инфраструктура

Манифесты k3s лежат в отдельном репозитории `infra-scripts/manifests/mtracker/` (namespace `mtracker`,
Traefik Ingress + cert-manager, домен `mtracker.nikichxp.xyz`). MongoDB — общий инстанс с
`okx-collector`, отдельная база `mtracker_db` (см. `infra-scripts/manifests/mongodb/09-mtracker-user-secret.yaml`
и `41-create-mtracker-user-job.yaml`).

⚠️ Перед применением манифестов нужно заменить плейсхолдерные пароли на реальные (гильдии/персонажи
больше не часть манифестов — их нужно завести через Admin API после деплоя, см. выше) — плейсхолдеры
помечены комментариями `TODO`/`placeholder` в соответствующих файлах.

## Запуск локально

Требуется **JDK 21** и запущенная **MongoDB** на `localhost:27017`.

```powershell
.\gradlew.bat bootRun
```

Сборка jar:

```powershell
.\gradlew.bat build
java -jar build\libs\app.jar
```
