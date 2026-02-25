# Booking Service - RestoBook QuickEat

Service de gestion des réservations pour la plateforme RestoBook de QuickEat.

## Table des matières

- [Description](#description)
- [Base de sonnées](#base-de-données)
- [Règles métier](#règles-métier)
- [Statut de réservation](#statuts-de-réservation)
- [Endpoints API](#endpoints-api)
- [Configuration](#configuration)
- [Installation](#installation)
- [Documentation API](#documentation-api)
- [Sécurité](#sécurité)
- [Healthcheck](#healthcheck)
- [Communication Inter-Services](#communication-inter-services)
- [Exemples de Requêtes](#exemples-de-requêtes)
- [License](#license)

## Description

Ce microservice gère :
- **Création de réservations** avec vérification de disponibilité
- **Gestion des créneaux** (intervalles de 30 minutes)
- **Annulation** (jusqu'à 2h avant le créneau)
- **Actions restaurant** (confirmation, refus, marquage terminé/absence)

## Base de données

| Table      | Description  |
|------------|--------------|
| `bookings` | Réservations |

## Règles métier

| Règle                   | Valeur         |
|-------------------------|----------------|
| Durée de réservation    | 90 minutes     |
| Intervalle des créneaux | 30 minutes     |
| Nombre de personnes     | 1-8            |
| Jours à l'avance        | J+0 à J+7      |
| Délai d'annulation      | 2 heures avant |

## Statuts de réservation

| Statut      | Description                |
|-------------|----------------------------|
| `PENDING`   | En attente de confirmation |
| `CONFIRMED` | Confirmée                  |
| `CANCELLED` | Annulée par le client      |
| `REJECTED`  | Refusée par le restaurant  |
| `COMPLETED` | Terminée (repas effectué)  |
| `NO_SHOW`   | Client absent              |

## Endpoints API

### Réservations - `/api/v1/bookings`

| Méthode | Endpoint           | Description               |
|---------|--------------------|---------------------------|
| POST    | `/`                | Créer une réservation     |
| GET     | `/{id}`            | Détails d'une réservation |
| GET     | `/reference/{ref}` | Rechercher par référence  |
| PUT     | `/{id}`            | Modifier une réservation  |
| POST    | `/{id}/cancel`     | Annuler une réservation   |

### Mes réservations

| Méthode | Endpoint                       | Description             |
|---------|--------------------------------|-------------------------|
| GET     | `/my-bookings`                 | Toutes mes réservations |
| GET     | `/my-bookings/status/{status}` | Par statut              |
| GET     | `/my-bookings/upcoming`        | À venir                 |
| GET     | `/my-bookings/past`            | Passées                 |

### Créneaux disponibles

| Méthode | Endpoint           | Description          |
|---------|--------------------|----------------------|
| GET     | `/available-slots` | Créneaux disponibles |

### Restaurant (OWNER/STAFF/ADMIN)

| Méthode | Endpoint                       | Description                |
|---------|--------------------------------|----------------------------|
| GET     | `/restaurant/{id}`             | Réservations du restaurant |
| GET     | `/restaurant/{id}/date/{date}` | Par date                   |
| GET     | `/restaurant/{id}/today`       | Du jour                    |
| PATCH   | `/{id}/confirm`                | Confirmer                  |
| PATCH   | `/{id}/reject`                 | Refuser                    |
| PATCH   | `/{id}/complete`               | Marquer terminée           |
| PATCH   | `/{id}/no-show`                | Marquer absence            |

### Endpoints Internes - `/api/v1/internal`

| Méthode | Endpoint              | Description                   |
|---------|-----------------------|-------------------------------|
| GET     | `/bookings/completed` | Vérifier réservation terminée |

## Configuration

```bash
cp .env.example .env
```

### Variables d'environnement

| Variable                        | Description             | Défaut                  |
|---------------------------------|-------------------------|-------------------------|
| `BOOKING_SERVICE_PORT`          | Port du service         | `8083`                  |
| `DB_NAME`                       | Nom de la base          | `booking_db`            |
| `AUTH_SERVICE_URL`              | URL Auth Service        | `http://localhost:8081` |
| `RESTAURANT_SERVICE_URL`        | URL Restaurant Service  | `http://localhost:8082` |
| `BOOKING_DURATION_MINUTES`      | Durée réservation       | `90`                    |
| `BOOKING_SLOT_INTERVAL_MINUTES` | Intervalle créneaux     | `30`                    |
| `BOOKING_MAX_PARTY_SIZE`        | Max personnes           | `8`                     |
| `BOOKING_MAX_DAYS_ADVANCE`      | Jours à l'avance        | `7`                     |
| `BOOKING_CANCELLATION_HOURS`    | Heures avant annulation | `2`                     |

## Installation

```bash
# Compiler
mvn clean install

# Lancer (port 8083)
mvn spring-boot:run
```

## Documentation API

- **Swagger UI**: http://localhost:8083/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8083/api-docs

## Sécurité

- Tous les endpoints nécessitent une authentification JWT
- Les clients peuvent gérer leurs propres réservations
- Les propriétaires (OWNER) peuvent gérer les réservations de leurs restaurants
- Les STAFF et ADMIN ont accès à toutes les réservations

## Healthcheck

```bash
curl http://localhost:8083/actuator/health
```

## Communication Inter-Services

La validation du token auprès du `auth-service` se fait via l'endpoint `/internal/validate` :

```bash
curl -H "Authorization: Bearer <token>" \
  http://localhost:8081/api/v1/internal/validate
```

Réponse :
```json
{
  "valid": true,
  "userId": 1,
  "email": "user@example.com",
  "role": "ROLE_CLIENT"
}
```

## Exemples de requêtes

### Créer une réservation

```bash
curl -X POST http://localhost:8083/api/v1/bookings \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "restaurantId": 1,
    "bookingDate": "2024-12-20",
    "bookingTime": "19:30",
    "partySize": 4,
    "customerName": "Jean Dupont",
    "customerEmail": "jean@example.com",
    "customerPhone": "+33612345678",
    "specialRequests": "Table près de la fenêtre"
  }'
```

### Voir les créneaux disponibles

```bash
curl "http://localhost:8083/api/v1/bookings/available-slots?restaurantId=1&date=2024-12-20&partySize=4"
```

### Annuler une réservation

```bash
curl -X POST http://localhost:8083/api/v1/bookings/1/cancel \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"reason": "Changement de plans"}'
```

## License

Ce projet est sous licence MIT. Voir le fichier [LICENSE](LICENSE) pour plus de détails.

---

**Si ce projet vous a été utile, n'hésitez pas à lui donner une étoile ⭐**

Made with ❤️ by [Chéridanh TSIELA]
