# Проект "Облачное хранилище"

Полностековое веб-приложение для личного облачного хранения файлов. Позволяет пользователям загружать, скачивать и управлять своими файлами через современный веб-интерфейс.

## 🛠️ Стек технологий

*   **Backend:** Java 21, Spring Boot, Spring Security, PostgreSQL, Redis, Minio (S3)
*   **Frontend:** React, Vite, Material-UI
*   **Инфраструктура и развертывание:** Docker, Docker Compose, Nginx

## 🚀 Быстрый старт

### Необходимые условия

Убедитесь, что на вашей системе установлены:
*   [Docker](https://www.docker.com/get-started)
*   [Docker Compose](https://docs.docker.com/compose/install/)

### Установка и запуск

1.  **Клонируйте репозиторий:**
    ```bash
    git clone https://github.com/romilMasnaviev/CloudStorage
    cd CloudStorage
    ```

2.  **Настройте переменные окружения:**
    Перейдите в директорию `infra`. Для работы `docker-compose.yml` требуется файл `.env`. Создайте его в этой директории (`infra/.env`) со следующим содержимым:

    ```dotenv
    # infra/.env

    # Учетные данные для PostgreSQL
    POSTGRES_DB=cloud_storage
    POSTGRES_USER=admin
    POSTGRES_PASSWORD=password

    # Учетные данные для Minio (S3-совместимое хранилище)
    MINIO_USER=minio-admin
    MINIO_PASSWORD=minio-secret-key
    ```

3.  **Соберите и запустите проект с помощью Docker Compose:**
    Из директории `infra` выполните команду:
    ```bash
    docker-compose up --build -d
    ```
    Эта команда соберет образы для frontend и backend и запустит все контейнеры в фоновом режиме.

4.  **Доступ к приложению:**
    *   **Frontend:** Откройте браузер и перейдите по адресу `http://localhost`.
    *   **Backend API:** API доступно через прокси Nginx по адресу `http://localhost/api/`.
    *   **Документация API (Swagger):** Изучить эндпоинты API можно по адресу `http://localhost/api/swagger-ui.html`.

## Структура проекта

```
.
├── backend/         # Приложение на Spring Boot
├── frontend/        # Приложение на React (Vite)
└── infra/           # Конфигурация Docker Compose, Nginx и переменные окружения
```
