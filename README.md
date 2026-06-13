# Sendlt

API de sessions live et de logbook pour le bloc en salle.

On crée une session, on partage un code à 6 caractères, chacun log ses tentatives.
Le fil d'activité passe en WebSocket (STOMP).

## Dev

```bash
docker compose up -d
./mvnw spring-boot:run
```

- API : http://localhost:8080
- Swagger : http://localhost:8080/swagger-ui.html
- Postgres : `localhost:5433`

## Tests

```bash
./mvnw test
```

Testcontainers sort son propre Postgres.
