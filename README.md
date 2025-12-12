#  RestoBook - Système de Réservation QuickEat

[![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-Proprietary-red.svg)]()

Système de réservation de restaurants en architecture microservices pour la chaîne **QuickEat**.

##  Table des matières

- [Présentation](#présentation)
- [Architecture](#architecture)
- [Services](#services)
- [Prérequis](#prérequis)
- [Installation](#installation)
- [Configuration](#configuration)
- [Démarrage](#démarrage)
- [Documentation API](#documentation-api)
- [Flux métier](#flux-métier)
- [Base de données](#base-de-données)
- [Sécurité](#sécurité)
- [Tests](#tests)

---

##  Présentation

**RestoBook** est une plateforme de réservation en ligne permettant aux clients de QuickEat de :

-  Rechercher des restaurants par ville, cuisine, note
-  Réserver une table (créneaux de 30 minutes)
-  Laisser des avis après leur repas
-  Gérer leurs réservations (modification, annulation)

### Objectifs business

| Objectif                   | Métrique       | Cible |
|----------------------------|----------------|-------|
| Augmentation fréquentation | Couverts/jour  | +30%  |
| Réduction temps d'attente  | Temps moyen    | -50%  |
| Fidélisation client        | Taux de retour | +25%  |
| Satisfaction client        | Note moyenne   | 4.5/5 |

---

## Architecture

```
┌──────────────────────────────────────────────────────────────────────────┐
│                                 CLIENTS                                  │
│                      (Web App / Mobile App / Postman)                    │
└────────────────────────────────────┬─────────────────────────────────────┘
                                     │
                                     ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                              MICROSERVICES                               │
│                                                                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │     AUTH     │  │  RESTAURANT  │  │   BOOKING    │  │   REVIEW     │  │
│  │    SERVICE   │  │    SERVICE   │  │   SERVICE    │  │   SERVICE    │  │
│  │     (8081)   │  │    (8082)    │  │    (8083)    │  │    (8084)    │  │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  │
│         │                 │                 │                 │          │
│         ▼                 ▼                 ▼                 ▼          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │   auth_db    │  │restaurant_db │  │  booking_db  │  │  review_db   │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘  │
│                                                                          │
└──────────────────────────────────────────────────────────────────────────┘
```

### Communication inter-services

```
                         ┌─────────────┐
                         │ Auth Service│
                         │   (8081)    │
                         └──────┬──────┘
                                │ Validation JWT
           ┌────────────────────┼────────────────────┐
           │                    │                    │
           ▼                    ▼                    ▼
   ┌───────────────┐   ┌───────────────┐   ┌───────────────┐
   │  Restaurant   │   │   Booking     │   │    Review     │
   │   Service     │◄──┤   Service     │   │   Service     │
   │   (8082)      │   │   (8083)      │   │   (8084)      │
   └───────────────┘   └───────┬───────┘   └───────┬───────┘
           ▲                   │                   │
           │                   │                   │
           └───────────────────┴───────────────────┘
                    Capacité, Horaires, Rating
```

---

## Services

### 1. Auth Service (Port 8081) 

Gestion de l'authentification et des utilisateurs.

| Fonctionnalité   | Description                            |
|------------------|----------------------------------------|
| JWT              | Access + Refresh tokens avec rotation  |
| Rôles            | CLIENT, STAFF, OWNER, ADMIN            |
| Authentification | Register, Login, Logout, Refresh       |
| Gestion users    | Profil, changement mot de passe        |
| Administration   | CRUD users, recherche, changement rôle |

**Compte admin par défaut** : `admin@quickeat.fr` / `Admin@123!`

### 2. Restaurant Service (Port 8082) 

Gestion des restaurants et menus.

| Fonctionnalité | Description                                     |
|----------------|-------------------------------------------------|
| Restaurants    | CRUD complet avec recherche avancée             |
| Menus          | Plats par catégories (entrée, plat, dessert...) |
| Horaires       | Ouverture matin/soir par jour                   |
| Recherche      | Par ville, cuisine, note, filtres               |

### 3. Booking Service (Port 8083) 

Gestion des réservations.

| Fonctionnalité | Description                        |
|----------------|------------------------------------|
| Réservations   | Création, modification, annulation |
| Créneaux       | Intervalles de 30 minutes          |
| Règles         | 1-8 personnes, J+0 à J+7           |
| Annulation     | Jusqu'à 2h avant le créneau        |

### 4. Review Service (Port 8084) 

Gestion des avis clients.

| Fonctionnalité | Description                      |
|----------------|----------------------------------|
| Avis           | Notes 1-5 étoiles + commentaire  |
| Condition      | Réservation terminée obligatoire |
| Réponses       | Propriétaire peut répondre       |
| Statistiques   | Moyenne, distribution des notes  |

---

## Prérequis

- **Java 25** (ou version compatible)
- **Maven 3.9+**
- **MySQL 8.0+**
- **Git**

### Vérification

```bash
java -version    # Java 25+
spring -version  # Java 4.0+
mvn -version     # Maven 3.9+
mysql --version  # MySQL 8.0+
```

---

## Installation

### 1. Cloner le projet

```bash
git clone https://github.com/dalton0x0/resto-book-app-microservices.git
cd resto-book-app-microservices
```

### 2. Structure du projet

```
resto-book-app-microservices/
├── auth-service/
├── restaurant-service/
├── booking-service/
├── review-service/
├── .gitignore
├── LICENSE
└── README.md
```

### 3. Créer les bases de données

```sql
CREATE DATABASE auth_db;
CREATE DATABASE restaurant_db;
CREATE DATABASE booking_db;
CREATE DATABASE review_db;
```

### 4. Configurer chaque service

```bash
# Pour chaque service
cd <service-name>
cp .env.properties.example .env.properties
# Éditer .env.properties avec vos paramètres
```

---

## Configuration

### Variables d'environnement communes

| Variable       | Description         | Défaut      |
|----------------|---------------------|-------------|
| `DB_HOST`      | Hôte MySQL          | `localhost` |
| `DB_PORT`      | Port MySQL          | `3306`      |
| `DB_USERNAME`  | Utilisateur MySQL   | `root`      |
| `DB_PASSWORD`  | Mot de passe MySQL  | ``          |
| `JPA_DDL_AUTO` | Stratégie Hibernate | `update`    |

### Variables spécifiques par service

#### Auth Service
| Variable                 | Défaut                      |
|--------------------------|-----------------------------|
| `AUTH_SERVICE_PORT`      | `8081`                      |
| `JWT_SECRET`             | (générer une clé sécurisée) |
| `JWT_ACCESS_EXPIRATION`  | `900000` (15 min)           |
| `JWT_REFRESH_EXPIRATION` | `604800000` (7 jours)       |

#### Restaurant Service
| Variable                  | Défaut                  |
|---------------------------|-------------------------|
| `RESTAURANT_SERVICE_PORT` | `8082`                  |
| `AUTH_SERVICE_URL`        | `http://localhost:8081` |

#### Booking Service
| Variable                        | Défaut |
|---------------------------------|--------|
| `BOOKING_SERVICE_PORT`          | `8083` |
| `BOOKING_DURATION_MINUTES`      | `90`   |
| `BOOKING_SLOT_INTERVAL_MINUTES` | `30`   |
| `BOOKING_MAX_PARTY_SIZE`        | `8`    |
| `BOOKING_MAX_DAYS_ADVANCE`      | `7`    |
| `BOOKING_CANCELLATION_HOURS`    | `2`    |

#### Review Service
| Variable              | Défaut |
|-----------------------|--------|
| `REVIEW_SERVICE_PORT` | `8084` |
| `REVIEW_MIN_RATING`   | `1`    |
| `REVIEW_MAX_RATING`   | `5`    |

---

## Démarrage

### Ordre de démarrage recommandé

```bash
# 1. Auth Service (requis par tous)
cd auth-service
mvn spring-boot:run

# 2. Restaurant Service
cd restaurant-service
mvn spring-boot:run

# 3. Booking Service
cd booking-service
mvn spring-boot:run

# 4. Review Service
cd review-service
mvn spring-boot:run
```

### Vérification

```bash
# Health checks
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
curl http://localhost:8084/actuator/health
```

---

## Documentation API

### Swagger UI

| Service    | URL                                   |
|------------|---------------------------------------|
| Auth       | http://localhost:8081/swagger-ui.html |
| Restaurant | http://localhost:8082/swagger-ui.html |
| Booking    | http://localhost:8083/swagger-ui.html |
| Review     | http://localhost:8084/swagger-ui.html |

### Endpoints principaux

#### Authentification
```bash
# Inscription
POST /api/v1/auth/register

# Connexion
POST /api/v1/auth/login

# Refresh token
POST /api/v1/auth/refresh
```

#### Restaurants
```bash
# Liste des restaurants
GET /api/v1/restaurants

# Recherche
GET /api/v1/restaurants/search?query=burger

# Par ville
GET /api/v1/restaurants/city/Paris

# Menu d'un restaurant
GET /api/v1/restaurants/{id}/menu
```

#### Réservations
```bash
# Créneaux disponibles
GET /api/v1/bookings/available-slots?restaurantId=1&date=2024-12-20&partySize=4

# Créer une réservation
POST /api/v1/bookings

# Mes réservations
GET /api/v1/bookings/my-bookings

# Annuler
POST /api/v1/bookings/{id}/cancel
```

#### Avis
```bash
# Avis d'un restaurant
GET /api/v1/reviews/restaurant/{id}

# Statistiques
GET /api/v1/reviews/restaurant/{id}/stats

# Créer un avis
POST /api/v1/reviews

# Puis-je laisser un avis ?
GET /api/v1/reviews/can-review/restaurant/{id}
```

---

## Flux métier

### Parcours client complet

```
┌─────────────────────────────────────────────────────────────────┐
│                      PARCOURS CLIENT                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  1. INSCRIPTION          2. RECHERCHE           3. RÉSERVATION  │
│  ┌─────────────┐        ┌─────────────┐        ┌─────────────┐  │
│  │  Register   │───────>│  Recherche  │───────>│  Sélection  │  │
│  │  + Login    │        │  Restaurant │        │  Créneau    │  │
│  └─────────────┘        └─────────────┘        └──────┬──────┘  │
│                                                       │         │
│  6. AVIS                 5. REPAS               4. CONFIRMATION │
│  ┌─────────────┐        ┌─────────────┐        ┌──────▼──────┐  │
│  │  Laisser    │<───────│   Repas     │<───────│ Réservation │  │
│  │  un avis    │        │  terminé    │        │  confirmée  │  │
│  └─────────────┘        └─────────────┘        └─────────────┘  │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### Règles métier

| Règle                     | Valeur                                |
|---------------------------|---------------------------------------|
| Durée de réservation      | 90 minutes                            |
| Intervalle des créneaux   | 30 minutes                            |
| Personnes par réservation | 1 à 8                                 |
| Réservation à l'avance    | J+0 à J+7                             |
| Annulation gratuite       | Jusqu'à 2h avant                      |
| Avis                      | Après réservation terminée uniquement |
| Notes                     | 1 à 5 étoiles                         |

---

## Base de données

### Schéma des tables

#### auth_db
- `users` - Utilisateurs
- `refresh_tokens` - Tokens de rafraîchissement

#### restaurant_db
- `restaurants` - Restaurants
- `opening_hours` - Horaires d'ouverture
- `menu_items` - Plats du menu

#### booking_db
- `bookings` - Réservations

#### review_db
- `reviews` - Avis clients

### Relations logiques (inter-services)

```
users (auth_db)
  │
  ├──< restaurants.owner_id (restaurant_db)
  │
  ├──< bookings.user_id (booking_db)
  │     │
  │     └──> restaurants.id
  │
  └──< reviews.user_id (review_db)
        │
        ├──> restaurants.id
        └──> bookings.id (optionnel)
```

---

## Sécurité

### Authentification JWT

```
┌─────────┐     ┌─────────────┐     ┌─────────────┐
│ Client  │────>│ Auth Service│────>│   MySQL     │
└────┬────┘     └──────┬──────┘     └─────────────┘
     │                 │
     │  Access Token   │
     │<────────────────┘
     │
     │  Authorization: Bearer <token>
     ▼
┌─────────────┐     ┌─────────────┐
│ Restaurant  │────>│ Auth Service│ (validation)
│   Service   │     │  /internal  │
└─────────────┘     └─────────────┘
```

### Rôles et permissions

| Rôle     | Permissions                                      |
|----------|--------------------------------------------------|
| `CLIENT` | Réserver, laisser des avis, gérer son profil     |
| `STAFF`  | + Gérer les réservations de tous les restaurants |
| `OWNER`  | + Gérer ses restaurants, répondre aux avis       |
| `ADMIN`  | + Administration complète                        |

### Bonnes pratiques implémentées

-  Hachage des mots de passe (BCrypt)
-  Rotation des refresh tokens
-  Validation des tokens côté service
-  Nettoyage automatique des tokens expirés
-  Protection CORS configurable
-  Validation des entrées (Bean Validation)

---

## Tests

### Exemples de requêtes cURL

#### 1. Inscription

```bash
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "client@example.com",
    "password": "Password123!",
    "firstName": "Jean",
    "lastName": "Dupont",
    "phone": "+33612345678"
  }'
```

#### 2. Connexion

```bash
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "client@example.com",
    "password": "Password123!"
  }'
```

#### 3. Rechercher des restaurants

```bash
curl http://localhost:8082/api/v1/restaurants/city/Paris
```

#### 4. Voir les créneaux disponibles

```bash
curl "http://localhost:8083/api/v1/bookings/available-slots?restaurantId=1&date=2024-12-20&partySize=4"
```

#### 5. Créer une réservation

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
    "customerPhone": "+33612345678"
  }'
```

#### 6. Laisser un avis

```bash
curl -X POST http://localhost:8084/api/v1/reviews \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "restaurantId": 1,
    "rating": 5,
    "comment": "Excellent burger, service impeccable !"
  }'
```

---

## Structure des fichiers par service

```
<service>/
├── .env.properties              # Configuration locale (ignoré git)
├── .env.properties.example      # Template de configuration
├── .gitignore
├── pom.xml
├── README.md
└── src/main/
    ├── java/com/restobook/<service>/
    │   ├── <Service>Application.java
    │   ├── client/              # Clients WebClient
    │   ├── config/              # Configurations
    │   ├── controller/          # Contrôleurs REST
    │   ├── dto/
    │   │   ├── request/         # DTOs de requête
    │   │   └── response/        # DTOs de réponse
    │   ├── entity/              # Entités JPA
    │   ├── exception/           # Exceptions + Handler
    │   ├── repository/          # Repositories JPA
    │   └── service/impl/        # Services
    └── resources/
        └── application.yml
```

---

## Stack technique

| Technologie       | Version | Utilisation                       |
|-------------------|---------|-----------------------------------|
| Java              | 25      | Langage principal                 |
| Spring Boot       | 4.0.0   | Framework                         |
| Spring Data JPA   | -       | Persistence                       |
| Spring Validation | -       | Validation des données            |
| Spring WebFlux    | -       | WebClient (appels inter-services) |
| Spring Actuator   | -       | Monitoring des services           |
| SpringDoc OpenAPI | 2.8.0   | Documentation API                 |
| MySQL             | 8.0     | Base de données                   |
| Lombok            | -       | Réduction du boilerplate          |
| JWT               | -       | Authentification                  |

---

## Support

Pour toute question ou problème :
-  Email : contact@cheridanh.cg
-  Documentation : [RTFM]

---

## Licence

Projet propriétaire - QuickEat © 2025

---

<p align="center">
  Made with ❤️ by Chéridanh TSIELA
</p>
