# Link Tracker

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

### Kafka/Redis/PostgreSQL

Для запуска Kafka/Redis/PostgreSQL нужно запустить ```docker-compose.yml``` из IDE,

либо выполнить команду в терминале ```docker-compose up```.

### Scrapper/Bot

Нужно заполнить .env файл, указав все необходимые параметры.

Запуск производится из IDE.
