# Digital Construction Passport (backend)

Spring Boot backend для управления проектами (Projects) с авторизацией через JWT и ролями пользователей.

## Требования

- **Java:** 17
- **Spring Boot:** 3.3.2
- **База данных:** SQLite (`passport.db` создаётся локально при запуске)

## Как запустить

1. Открой проект как Maven-проект в IntelliJ IDEA.
2. Запусти класс `org.example.Main`.
3. Приложение стартует на порту `8080`.

## Базовый URL

`http://localhost:8080`

## Пользователи по умолчанию

При первом запуске (если в базе нет пользователей) создаются:

- **editor** / `editor123` (роль `EDITOR`)
- **reader** / `reader123` (роль `READER`)

## Роли и доступ

- **READER**
  - Может: `GET /projects`, `GET /projects/{id}`
  - Не может: `POST/PUT/DELETE /projects/**` (будет 403)

- **EDITOR**
  - Может: `GET/POST/PUT/DELETE /projects/**`

## Авторизация

### Логин

`POST /auth/login`

Headers:

`Content-Type: application/json`

Body:

```json
{
  "username": "reader",
  "password": "reader123"
}
```

Response (пример):

```json
{
  "token": "<JWT>",
  "role": "READER"
}
```

Дальше для защищённых endpoint'ов добавляй header:

`Authorization: Bearer <JWT>`

## Projects API

### Получить все проекты

`GET /projects`

Headers:

- `Authorization: Bearer <JWT>`

Response (пример):

```json
[
  {
    "id": 1,
    "name": "Project A",
    "status": "PLANNED",
    "documents": ["doc1", "doc2"],
    "deleted": false
  }
]
```

### Получить проект по id

`GET /projects/{id}`

Headers:

- `Authorization: Bearer <JWT>`

Если проект удалён (`deleted=true`), вернётся:

- HTTP `410 Gone`
- Body:

```json
{
  "error": "Project was deleted"
}
```

### Создать проект (только EDITOR)

`POST /projects`

Headers:

- `Content-Type: application/json`
- `Authorization: Bearer <JWT>`

Body:

```json
{
  "name": "New Project",
  "status": "PLANNED",
  "documents": ["spec.pdf", "drawing.dwg"]
}
```

### Обновить проект (только EDITOR)

`PUT /projects/{id}`

Headers:

- `Content-Type: application/json`
- `Authorization: Bearer <JWT>`

Body:

```json
{
  "name": "Updated Project",
  "status": "IN_PROGRESS",
  "documents": ["spec_v2.pdf"]
}
```

Если проект удалён (`deleted=true`), вернётся `410 Gone` + `{ "error": "Project was deleted" }`.

### Удалить проект (soft delete, только EDITOR)

`DELETE /projects/{id}`

Headers:

- `Authorization: Bearer <JWT>`

Поведение:

- Проект **не удаляется физически**, а помечается `deleted=true`
- Response: HTTP `204 No Content`
- Повторное удаление того же проекта: HTTP `410 Gone` + `{ "error": "Project was deleted" }`