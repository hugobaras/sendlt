# Sendlt

API de sessions live et de logbook pour le bloc en salle.

On crée une session, on partage un code à 6 caractères, chacun log ses tentatives.
Le fil d'activité passe en WebSocket (STOMP).

## Lancer

```bash
docker compose up --build
```

- App : http://localhost:8080
- Swagger : http://localhost:8080/swagger-ui.html
- Postgres (hôte) : `localhost:5433`

Compte admin : après un register, passer le rôle en SQL :

```bash
docker compose exec postgres psql -U sendlt -d sendlt \
  -c "UPDATE users SET role = 'SETTER_ADMIN' WHERE email = 'toi@example.com';"
```

## Dev local

Postgres seul, API et frontend à part :

```bash
docker compose up -d postgres
./mvnw spring-boot:run
cd frontend && npm install && npm run dev
```

L'UI Vite est sur http://localhost:5173 (proxy `/api` et `/ws` vers 8080).

## Tests

```bash
./mvnw test
```

Testcontainers sort son propre Postgres, pas besoin de la base locale.

## Stack

Java 21, Spring Boot, Postgres, Flyway, JWT, STOMP. Front React/Vite.
