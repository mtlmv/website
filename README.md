# Website

Backend на Spring Boot (Java) + минималистичный фронтенд (HTML/CSS/JS): регистрация, посты, комментарии, лайки, профиль пользователя.

## Стек

- **Backend**: Spring Boot 4, Spring Security (JWT), Spring Data JPA, PostgreSQL
- **Frontend**: без сборки — чистые HTML/CSS/JS (папка `frontend/`)

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

## 2. Настроить базу данных

Создайте базу `website` в PostgreSQL (если её ещё нет):

```powershell
psql -U postgres -c "CREATE DATABASE website;"
```

Скопируйте пример конфига и впишите свой пароль от Postgres:

```powershell
copy src\main\resources\application.properties.example src\main\resources\application.properties
```

Откройте `src\main\resources\application.properties` и замените `CHANGE_ME` на реальный пароль пользователя `postgres`.

> Этот файл сознательно не в git (`.gitignore`) — в нём пароль от БД.

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

## 4. Запустить frontend (порт 8899)

В отдельном окне терминала:

```powershell
cd website\frontend
python -m http.server 8899
```

Откройте в браузере: **http://localhost:8899**

## 5. Остановить

- Если запускали в открытом окне терминала — просто `Ctrl+C` в этом окне (для backend и для frontend отдельно).
- Если процесс запущен в фоне (не видно окна), останавливайте по порту:

```powershell
Get-NetTCPConnection -LocalPort 8081,8899 -ErrorAction SilentlyContinue |
    Select-Object -ExpandProperty OwningProcess -Unique |
    ForEach-Object { Stop-Process -Id $_ -Force }
```

Проверить, что порты освободились:

```powershell
Get-NetTCPConnection -LocalPort 8081,8899 -ErrorAction SilentlyContinue
```
Если команда ничего не вывела — всё остановлено.
