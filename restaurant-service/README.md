# Restaurant Service - RestoBook QuickEat

Service de gestion des restaurants pour la plateforme RestoBook de QuickEat.

## Table des matières

- [Description](#description)
- [Base de sonnées](#base-de-données)
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
- **CRUD des restaurants** (création, lecture, mise à jour, suppression)
- **Gestion des menus** (plats, catégories, prix)
- **Horaires d'ouverture** 
- **Recherche et filtrage** des restaurants

## Base de données

| Table           | Description                  |
|-----------------|------------------------------|
| `restaurants`   | Informations des restaurants |
| `opening_hours` | Horaires d'ouverture         |
| `menu_items`    | Plats du menu                |

## Endpoints API

### Restaurants (Public) - `/api/v1/restaurants`

| Méthode | Endpoint              | Description             |
|---------|-----------------------|-------------------------|
| GET     | `/`                   | Lister les restaurants  |
| GET     | `/{id}`               | Détails d'un restaurant |
| GET     | `/search?keyword=`    | Rechercher              |
| GET     | `/city/{city}`        | Par ville               |
| GET     | `/cuisine/{type}`     | Par type de cuisine     |
| GET     | `/filter`             | Recherche avancée       |
| GET     | `/top-rated`          | Les mieux notés         |
| GET     | `/{id}/opening-hours` | Horaires                |
| GET     | `/cities`             | Liste des villes        |
| GET     | `/cuisine-types`      | Types de cuisine        |

### Restaurants (Authentifié)

| Méthode | Endpoint              | Description         | Rôle         |
|---------|-----------------------|---------------------|--------------|
| POST    | `/`                   | Créer un restaurant | OWNER, ADMIN |
| PUT     | `/{id}`               | Modifier            | Owner, ADMIN |
| DELETE  | `/{id}`               | Supprimer           | Owner, ADMIN |
| PUT     | `/{id}/opening-hours` | Modifier horaires   | Owner, ADMIN |
| PATCH   | `/{id}/activate`      | Activer             | Owner, ADMIN |
| PATCH   | `/{id}/deactivate`    | Désactiver          | Owner, ADMIN |
| GET     | `/my-restaurants`     | Mes restaurants     | Owner        |

### Menu - `/api/v1/restaurants/{restaurantId}/menu`

| Méthode | Endpoint                        | Description        |
|---------|---------------------------------|--------------------|
| GET     | `/`                             | Menu complet       |
| GET     | `/available`                    | Plats disponibles  |
| GET     | `/category/{category}`          | Par catégorie      |
| GET     | `/search?keyword=`              | Rechercher         |
| GET     | `/vegetarian`                   | Plats végétariens  |
| GET     | `/vegan`                        | Plats vegan        |
| GET     | `/gluten-free`                  | Sans gluten        |
| GET     | `/{itemId}`                     | Détails d'un plat  |
| POST    | `/`                             | Ajouter un plat    |
| PUT     | `/{itemId}`                     | Modifier           |
| DELETE  | `/{itemId}`                     | Supprimer          |
| PATCH   | `/{itemId}/toggle-availability` | Activer/Désactiver |

### Endpoints Internes - `/api/v1/internal`

| Méthode | Endpoint                     | Description        |
|---------|------------------------------|--------------------|
| GET     | `/restaurants/{id}/exists`   | Vérifier existence |
| GET     | `/restaurants/{id}/capacity` | Récupérer capacité |
| GET     | `/restaurants/{id}`          | Infos restaurant   |
| GET     | `/restaurants/{id}/is-open`  | Vérifier si ouvert |
| PUT     | `/restaurants/{id}/rating`   | Mettre à jour note |

## Configuration

```bash
cp .env.properties.example .env.properties
```

### Variables d'environnement

| Variable                  | Description         | Défaut                  |
|---------------------------|---------------------|-------------------------|
| `RESTAURANT_SERVICE_PORT` | Port du service     | `8082`                  |
| `DB_HOST`                 | Hôte MySQL          | `localhost`             |
| `DB_PORT`                 | Port MySQL          | `3306`                  |
| `DB_NAME`                 | Nom de la base      | `restaurant_db`         |
| `DB_USERNAME`             | Utilisateur         | ``                      |
| `DB_PASSWORD`             | Mot de passe        | ``                      |
| `AUTH_SERVICE_URL`        | URL du Auth Service | `http://localhost:8081` |

## Installation

```bash
# Compiler
mvn clean install

# Lancer (port 8082)
mvn spring-boot:run
```

## Documentation API

- **Swagger UI**: http://localhost:8082/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8082/api-docs

## Sécurité

- Les endpoints publics (GET) ne nécessitent pas d'authentification
- Les endpoints de modification nécessitent un token JWT valide
- Seuls les propriétaires (OWNER) et administrateurs (ADMIN) peuvent modifier un restaurant

## Healthcheck

```bash
curl http://localhost:8081/actuator/health
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

### Créer un restaurant (OWNER)

```bash
curl -X POST http://localhost:8082/api/v1/restaurants \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "QuickEat Paris Centre",
    "address": "10 rue de la Paix",
    "city": "Paris",
    "postalCode": "75002",
    "phone": "+33145678900",
    "cuisineType": "Fast-food premium",
    "totalCapacity": 80,
    "openingHours": [
      {
        "dayOfWeek": "MONDAY",
        "openingTimeMorning": "11:30",
        "closingTimeMorning": "14:30",
        "openingTimeEvening": "18:30",
        "closingTimeEvening": "22:30"
      }
    ]
  }'
```

### Rechercher des restaurants

```bash
curl "http://localhost:8082/api/v1/restaurants/search?keyword=paris"
```

### Ajouter un plat au menu

```bash
curl -X POST http://localhost:8082/api/v1/restaurants/1/menu \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Classic Burger",
    "description": "Steak haché 150g, cheddar, salade, tomate",
    "price": 12.90,
    "category": "MAIN_COURSE",
    "allergens": "Gluten, Lait",
    "vegetarian": false
  }'
```

## License

Ce projet est sous licence MIT. Voir le fichier [LICENSE](LICENSE) pour plus de détails.

---

**Si ce projet vous a été utile, n'hésitez pas à lui donner une étoile ⭐**

Made with ❤️ by [Chéridanh TSIELA]
