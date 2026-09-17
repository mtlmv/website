# Website

Backend на Spring Boot (Java) + минималистичный фронтенд (HTML/CSS/JS): регистрация, посты, комментарии, лайки, профиль пользователя.

## Стек

- **Backend**: Spring Boot 4, Spring Security (JWT), Spring Data JPA, PostgreSQL
- **Frontend**: без сборки — чистые HTML/CSS/JS, отдаются самим бэкендом из `src/main/resources/static/`

## Требования

- JDK 25+
- PostgreSQL (запущенный локально, порт 5432)
- Python 3 (только чтобы отдавать статику фронтенда) — можно заменить любым другим статическим сервером

## 1. Скачать проект

```powershell
git clone https://github.com/mtlmv/website.git
cd website
```

Если у вас уже есть локальная копия — просто `git pull`, чтобы подтянуть изменения.

## 2. Настроить базу данных и ключи

Создайте базу `website` в PostgreSQL (если её ещё нет):

```powershell
psql -U postgres -c "CREATE DATABASE website;"
```

Скопируйте шаблон переменных окружения:

```powershell
copy .env.example .env
```

Откройте `.env` и заполните:

| Переменная | Что это |
|---|---|
| `DB_URL` | JDBC-адрес базы |
| `DB_USERNAME` | пользователь Postgres |
| `DB_PASSWORD` | его пароль |
| `JWT_SECRET` | ключ для подписи токенов, **минимум 32 байта** |
| `JWT_EXPIRATION_MS` | срок жизни токена (по умолчанию сутки) |

Сгенерировать свой `JWT_SECRET`:

```powershell
[Convert]::ToBase64String((1..48 | ForEach-Object { Get-Random -Max 256 }))
```

> `.env` не в git (`.gitignore`) — там реальные ключи. В репозитории лежит только `.env.example`.
> `application.properties` секретов не содержит: там только ссылки вида `${DB_PASSWORD}`, которые Spring подставляет из `.env` через `spring.config.import`.

## 3. Запустить backend (порт 8081)

```powershell
$env:JAVA_HOME = "путь_к_вашему_JDK_25"
cd website
.\mvnw.cmd -o clean spring-boot:run
```

Сервер поднят, когда в логе видно:
```
Started WebsiteApplication in X seconds
```

**Если `mvnw.cmd`/`java` ничего не выводят и сразу завершаются** — значит `java`/`mvn` в системном PATH битые. Найдите рабочий JDK (например, тот, что использует ваша IDE) и явно укажите его через `$env:JAVA_HOME` перед запуском, как в примере выше.

## 4. Открыть в браузере

Отдельный сервер для фронтенда не нужен — Spring Boot отдаёт его сам из `src/main/resources/static/`:

**http://localhost:8081**

Чтобы открыть с телефона в той же сети, узнайте IP компьютера и подключитесь к нему на том же порту:

```powershell
(Get-NetIPConfiguration | Where-Object { $_.NetProfile.IPv4Connectivity -eq 'Internet' }).IPv4Address.IPAddress
```

Адрес будет вида `http://192.168.1.50:8081`. Если не открывается — скорее всего брандмауэр Windows режет входящие подключения. От имени администратора:

```powershell
New-NetFirewallRule -DisplayName "website dev" -Direction Inbound -Protocol TCP -LocalPort 8081 -Action Allow
```

Учтите: в сетях операторов (адреса вида `100.x.x.x`) устройства часто изолированы друг от друга, и тогда не поможет ничего — проверьте на обычном домашнем Wi-Fi.

## 5. Остановить

- Если запускали в открытом окне терминала — просто `Ctrl+C` в этом окне.
- Если процесс запущен в фоне (не видно окна), останавливайте по порту:

```powershell
Get-NetTCPConnection -LocalPort 8081 -ErrorAction SilentlyContinue |
    Select-Object -ExpandProperty OwningProcess -Unique |
    ForEach-Object { Stop-Process -Id $_ -Force }
```

Проверить, что порт освободился:

```powershell
Get-NetTCPConnection -LocalPort 8081 -ErrorAction SilentlyContinue
```
Если команда ничего не вывела — всё остановлено.

## 6. Выложить в интернет (Render)

В репозитории лежит `Dockerfile` и `render.yaml` — Render поднимет и приложение, и базу сам.

### Сначала база

Блюпринт **не создаёт базу сам**: Render разрешает только одну бесплатную базу на аккаунт, и если она уже есть, весь деплой падает с ошибкой `cannot have more than one active free tier database`.

Возьмите одну из двух:

- **Уже существующую базу на Render** — откройте её, скопируйте `Internal Database URL` и разберите на части: хост, имя базы, пользователь, пароль.
- **Neon** ([neon.tech](https://neon.tech)) — рекомендую: бесплатная база там не удаляется через 30 дней. Реквизиты берутся из строки подключения, а `DB_PARAMS` нужно задать как `?sslmode=require`.

### Потом деплой

1. Зарегистрируйтесь на [render.com](https://render.com) через GitHub.
2. **New → Blueprint**, выберите репозиторий `mtlmv/website`.
3. Render прочитает `render.yaml` и спросит реквизиты базы — заполните `DB_HOST`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` и, если нужен SSL, `DB_PARAMS`.
4. **Apply**.

`JWT_SECRET` сгенерируется автоматически. Первая сборка занимает 5–10 минут: собирается Docker-образ с JDK 25.

Адрес будет вида `https://website-xxxx.onrender.com`.

**Что важно знать про бесплатный тариф:**

- Сервис **засыпает после 15 минут простоя**, первый запрос после сна идёт около минуты.
- Бесплатная база **удаляется через 30 дней** — для учебного проекта нормально, но не для чего-то живого.
- Данные с вашего компьютера **не переедут сами**. Схема создастся пустой (`ddl-auto=update`), пользователей и посты нужно заводить заново или переносить дампом:
  ```powershell
  pg_dump -U postgres -d website --data-only > dump.sql
  ```
- Если приложение не стартует с ошибкой про слабый ключ — значит сгенерированный `JWT_SECRET` короче 32 байт. Задайте свой в **Environment** у сервиса.

Локально всё продолжает работать как раньше: те же переменные берутся из `.env`, а `PORT` и реквизиты базы на Render приходят из панели.
