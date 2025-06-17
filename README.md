# Link Tracker

## Описание
<bold>Link Tracker</bold> — это сервис для отслеживания изменений в ссылках на GitHub и StackOverflow через Telegram-бота.
Бот позволяет отслеживать новые pull requests, issues, ответы и комментарии, а также получать уведомления об изменениях.

### Поддерживаемые команды бота:
	/start — регистрация пользователя
	/stop — удаление пользователя и всех его отслеживаемых ссылок
	/help — список доступных команд
	/track — отслеживание ссылки
	/untrack — прекращение отслеживания
	/list — список отслеживаемых ссылок

### Stack

* Java 23
* Telegram API
* Spring Boot
* PostgreSQL (основное хранилище)
* Liquibase (миграции)
* Redis (кэширование запросов)
* Apache Kafka
* Docker, Docker Compose
* Testcontainers (интеграционные тесты)
* Prometheus, Grafana (RED-метрики, количество используемой памяти в единицу времени с разбивкой по типу, количество пользовательских сообщений в секунду, График количества активных ссылок в БД по типу (github, stackoverflow), p50, p95, p99 времени работы одного scrape по типу (github, stackoverflow))

### Особенности

1. Реализовано 2 способа работы с БД: JDBC и ORM. Выбор способа работы с БД осуществляется через конфигурацию: 
```yaml
database: 
    type: SQL
```
 или 
```yaml
database:
    type: ORM
```

2. Реализован выбор транспорта (HTTP, KAFKA) между Scrapper и Bot через конфигурационной файл при помощи свойства: 
```yaml
transport: 
    type: HTTP
```
 или 
```yaml
transport:
    type: KAFKA
```

3. В случае отказа HTTP или Kafka при отправке уведомлений происходит fallback на альтернативный транспорт
4. В случае недоступности сервиса продолжительное время вместо Retry соединение разрывается при помощи Circuit Breaker
5. Все HTTP-запросы поддерживают Timeout
6. Все HTTP-запросы поддерживают Retry
7. У каждого публичного endpoint'а есть выставленный Rate Limiting на основе IP-адреса клиента


## Метрики

### Scrapper

Количество используемой памяти в единицу времени с разбивкой по типу (если применимо): 
``` 
sum by (area, pool) (jvm_memory_used_bytes{job=~"$app"})
```

R: 
```
sum(rate(http_server_requests_seconds_count{job=~"$app"}[1m])) by (job, method, status)
```
E: 
```
sum(rate(http_server_requests_seconds_count{job=~"$app", status=~"4..|5.."}[1m])) by (job, status)
```
D: 
```
sum(rate(http_server_requests_seconds_sum{job=~"$app"}[1m]))
 /
sum(rate(http_server_requests_seconds_count{job=~"$app"}[1m]))
```

![image](https://github.com/user-attachments/assets/26fde05a-ac7b-46c8-b700-a1fddfdc86de)

![image](https://github.com/user-attachments/assets/d865cf9d-c955-4610-ac00-0af9c8044c7a)

### Bot
![image](https://github.com/user-attachments/assets/6c8b9772-5b3e-440a-99ab-35269f4705ee)
![image](https://github.com/user-attachments/assets/896f8e0d-d465-4a29-8f17-d6264fb9ee3d)

### Custom
p99 времени работы одного scrape по типу (github, stackoverflow):
```
histogram_quantile(0.99, sum(rate(scrape_duration_seconds_bucket[5m])) by (le, type))
```
p95 времени работы одного scrape по типу (github, stackoverflow):
```
histogram_quantile(0.95, sum(rate(scrape_duration_seconds_bucket[5m])) by (le, type))
```
p50 времени работы одного scrape по типу (github, stackoverflow):
```
histogram_quantile(0.50, sum(rate(scrape_duration_seconds_bucket[5m])) by (le, type))
```
ActiveLinks:
```
active_links
```
Количество пользовательских сообщений в секунду:
```
rate(user_messages_total{app=~"$app"}[1m])
```
![image](https://github.com/user-attachments/assets/6952a075-36d4-42f8-8625-ec4a12e3bb62)
![image](https://github.com/user-attachments/assets/d43d005e-8590-4a17-9ee7-7149d5a5317c)
![image](https://github.com/user-attachments/assets/0b757c74-4904-4127-8c21-3d072c7e5978)

## Запуск

### Сборка

Для сборки проекта выполните команду

```bash

mvn compile -am spotless:check modernizer:modernizer spotbugs:check pmd:check pmd:cpd-check

mvn clean install

```

или

Для Mac/Linux введите в терминале, для Windows в GitBash, команду:

```
sh script.sh
```

### Kafka/Redis/PostgreSQL/Prometheus/Grafana/Scrapper/Bot

Нужно заполнить .env файл, указав все необходимые параметры.

Для запуска Kafka/Redis/PostgreSQL/Prometheus/Grafana/Scrapper/Bot нужно запустить ```docker-compose.yml``` из IDE,

либо выполнить команду в терминале ```docker-compose up```.

