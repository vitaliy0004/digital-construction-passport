# Задание для Backend-разработчика: Цифровой строительный паспорт

Spring Boot backend для управления проектами с авторизацией через JWT и ролями пользователей.

## Требования

- **Java:** 17
- **Spring Boot:** 3.3.2
- **База данных:** SQLite (`passport.db` создаётся локально при запуске)

## Как запустить

1. Открой проект как Maven-проект в IntelliJ IDEA.
2. Запусти класс `org.example.Main`.
3. Приложение стартует на порту `8080`.

## Базовый URL используеться во всех запросах

`http://localhost:8080`

## Пользователи которых можно создать, их Роли и доступ:

- **READER** / `reader123` (роль в системе `READER` - "читатель")
  - Может:
    -   просматривать все прокты `GET /projects`
    -   просматривать конкретный проект `GET /projects/{id}`
  - Не может:
    -   создавать проект  `POST /projects` (будет 403)
    -   обновлять проект `PUT /projects/{id}` (будет 403)
    -   удалять проект `DELETE /projects/{id}` (будет 403)

- **EDITOR** / `editor123` (роль в системе `EDITOR` - "редактор")
  - Может делать:
    -  просматривать все прокты `GET /projects`
    -  просматривать конкретный проект `GET /projects/{id}`
    -  создавать проект  `POST /projects` 
    -  обновлять проект `PUT /projects/{id}` 
    -  удалять проект `DELETE /projects/{id}` 


- **Public API**
## 1) Авторизация

### Логин

`POST /auth/login`

Headers:

`Content-Type: application/json`

Body для reader:

```json
{
  "username": "reader",
  "password": "reader123"
}
```
Body для editor:

```json
{
  "username": "editor",
  "password": "editor123"
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

### 2) Получить все проекты

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

### 3)Получить проект по id

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

### 4) Создать проект (только EDITOR)

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

### 5) Обновить проект (только EDITOR)

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

### 5) Удалить проект (soft delete, только EDITOR)

`DELETE /projects/{id}`

Headers:

- `Authorization: Bearer <JWT>`

Поведение:

- Проект **не удаляется физически**, а помечается `deleted=true`
- Response: HTTP `204 No Content`
- Повторное удаление того же проекта: HTTP `410 Gone` + `{ "error": "Project was deleted" }`
