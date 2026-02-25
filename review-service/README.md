# Review Service - RestoBook QuickEat

Service de gestion des avis pour la plateforme RestoBook de QuickEat.

## Table des matières

- [Description](#description)
- [Base de sonnées](#base-de-données)
- [Règles métier](#règles-métier)
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
- **Création d'avis** (uniquement après une réservation terminée)
- **Notes de 1 à 5 étoiles** avec commentaires optionnels
- **Réponses des propriétaires** aux avis
- **Statistiques** et distribution des notes
- **Mise à jour automatique** de la note moyenne du restaurant

## Base de données

| Table     | Description           |
|-----------|-----------------------|
| `reviews` | Avis des utilisateurs |

## Règles métier

| Règle           | Valeur                                |
|-----------------|---------------------------------------|
| Notes           | 1 à 5 étoiles                         |
| Commentaire max | 1000 caractères                       |
| Condition       | Réservation terminée obligatoire      |
| Limite          | 1 avis par utilisateur par restaurant |

### Flux de création d'avis

```
1. Utilisateur fait une réservation
2. Réservation marquée comme COMPLETED
3. Utilisateur peut maintenant laisser un avis
4. Avis créé avec isVerified=true
5. Note moyenne du restaurant mise à jour
6. Propriétaire peut répondre
```

## Endpoints API

### Avis - `/api/v1/reviews`

| Méthode | Endpoint | Description       | Auth |
|---------|----------|-------------------|------|
| POST    | `/`      | Créer un avis     | Oui  |
| GET     | `/{id}`  | Détails d'un avis | Non  |
| PUT     | `/{id}`  | Modifier un avis  | Oui  |
| DELETE  | `/{id}`  | Supprimer un avis | Non  |

### Par restaurant

| Méthode | Endpoint                           | Description   | Auth      |
|---------|------------------------------------|---------------|-----------|
| GET     | `/restaurant/{id}`                 | Avis visibles | Non       |
| GET     | `/restaurant/{id}/all`             | Tous les avis | Oui Owner |
| GET     | `/restaurant/{id}/rating/{rating}` | Par note      | Non       |
| GET     | `/restaurant/{id}/verified`        | Avis vérifiés | Non       |
| GET     | `/restaurant/{id}/search?keyword=` | Recherche     | Non       |
| GET     | `/restaurant/{id}/stats`           | Statistiques  | Non       |
| GET     | `/restaurant/{id}/unanswered`      | Sans réponse  | Oui Owner |

### Mes avis

| Méthode | Endpoint                      | Description                 |
|---------|-------------------------------|-----------------------------|
| GET     | `/my-reviews`                 | Mes avis                    |
| GET     | `/my-review/restaurant/{id}`  | Mon avis pour un restaurant |
| GET     | `/can-review/restaurant/{id}` | Puis-je laisser un avis ?   |

### Actions Owner

| Méthode | Endpoint               | Description          |
|---------|------------------------|----------------------|
| POST    | `/{id}/owner-response` | Répondre à un avis   |
| DELETE  | `/{id}/owner-response` | Supprimer la réponse |

### Actions Admin

| Méthode | Endpoint                  | Description              |
|---------|---------------------------|--------------------------|
| PATCH   | `/{id}/toggle-visibility` | Masquer/afficher un avis |

## Configuration

```bash
cp .env.example .env
```

### Variables d'environnement

| Variable                    | Description              | Défaut                  |
|-----------------------------|--------------------------|-------------------------|
| `REVIEW_SERVICE_PORT`       | Port du service          | `8084`                  |
| `DB_NAME`                   | Nom de la base           | `review_db`             |
| `AUTH_SERVICE_URL`          | URL Auth Service         | `http://localhost:8081` |
| `RESTAURANT_SERVICE_URL`    | URL Restaurant Service   | `http://localhost:8082` |
| `BOOKING_SERVICE_URL`       | URL Booking Service      | `http://localhost:8083` |
| `REVIEW_MIN_RATING`         | Note minimale            | `1`                     |
| `REVIEW_MAX_RATING`         | Note maximale            | `5`                     |
| `REVIEW_MAX_COMMENT_LENGTH` | Longueur max commentaire | `1000`                  |

## Installation

```bash
# Compiler
mvn clean install

# Lancer (port 8084)
mvn spring-boot:run
```

## Documentation API

- **Swagger UI**: http://localhost:8084/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8084/api-docs

## Sécurité

- Les avis sont **vérifiés** car liés à une réservation terminée
- Un utilisateur ne peut laisser qu'**un seul avis** par restaurant
- Les propriétaires peuvent **répondre** aux avis de leurs restaurants
- Les administrateurs peuvent **masquer** des avis inappropriés
- La note moyenne du restaurant est mise à jour **automatiquement**

## Healthcheck

```bash
curl http://localhost:8084/actuator/health
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

### Créer un avis

```bash
curl -X POST http://localhost:8084/api/v1/reviews \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "restaurantId": 1,
    "bookingId": 5,
    "rating": 4,
    "comment": "Excellent burger, service rapide. Je recommande !"
  }'
```

### Voir les avis d'un restaurant

```bash
curl http://localhost:8084/api/v1/reviews/restaurant/1
```

### Statistiques

```bash
curl http://localhost:8084/api/v1/reviews/restaurant/1/stats
```

Réponse :
```json
{
  "success": true,
  "data": {
    "restaurantId": 1,
    "averageRating": 4.2,
    "totalReviews": 45,
    "ratingDistribution": {
      "1": 2,
      "2": 3,
      "3": 5,
      "4": 20,
      "5": 15
    }
  }
}
```

### Répondre à un avis (Owner)

```bash
curl -X POST http://localhost:8084/api/v1/reviews/1/owner-response \
  -H "Authorization: Bearer <owner_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "response": "Merci pour votre retour ! Nous sommes ravis que vous ayez apprécié."
  }'
```

### Vérifier si je peux laisser un avis

```bash
curl http://localhost:8084/api/v1/reviews/can-review/restaurant/1 \
  -H "Authorization: Bearer <token>"
```

## License

Ce projet est sous licence MIT. Voir le fichier [LICENSE](LICENSE) pour plus de détails.

---

**Si ce projet vous a été utile, n'hésitez pas à lui donner une étoile ⭐**

Made with ❤️ by [Chéridanh TSIELA]
