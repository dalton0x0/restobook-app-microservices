# Auth Service - RestoBook QuickEat

Service d'authentification et de gestion des utilisateurs pour la plateforme RestoBook de QuickEat.

## Table des matières

- [Description](#description)
- [Rôles disponibles](#rôles-disponibles)
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

Ce microservice gère l'ensemble des fonctionnalités liées à l'authentification et la gestion des utilisateurs :

- **Inscription** des nouveaux utilisateurs (rôle CLIENT par défaut)
- **Connexion/Déconnexion** avec JWT
- **Gestion des tokens** (Access Token + Refresh Token)
- **Gestion des profils** utilisateurs
- **Administration** des utilisateurs (création avec rôle, activation/désactivation)
- **Validation de tokens** pour les appels inter-services

## Rôles Disponibles

| Rôle     | Description                                       |
|----------|---------------------------------------------------|
| `CLIENT` | Client standard (rôle par défaut à l'inscription) |
| `STAFF`  | Employé de restaurant                             |
| `OWNER`  | Propriétaire/Gérant de restaurant                 |
| `ADMIN`  | Administrateur système                            |

## Endpoints API

### Authentification (`/api/v1/auth`)

| Méthode | Endpoint      | Description                         | Auth |
|---------|---------------|-------------------------------------|------|
| POST    | `/register`   | Inscription d'un nouvel utilisateur | Non  |
| POST    | `/login`      | Connexion utilisateur               | Non  |
| POST    | `/refresh`    | Rafraîchir le token d'accès         | Non  |
| POST    | `/logout`     | Déconnexion                         | Non  |
| POST    | `/logout-all` | Déconnexion de toutes les sessions  | Oui  |

### Profil Utilisateur (`/api/v1/users`)

| Méthode | Endpoint       | Description              | Auth |
|---------|----------------|--------------------------|------|
| GET     | `/me`          | Récupérer mon profil     | Oui  |
| PUT     | `/me`          | Modifier mon profil      | Oui  |
| PUT     | `/me/password` | Changer mon mot de passe | Oui  |

### Administration (`/api/v1/admin/users`) - ADMIN uniquement

| Méthode | Endpoint           | Description                    |
|---------|--------------------|--------------------------------|
| GET     | `/`                | Liste paginée des utilisateurs |
| GET     | `/search?keyword=` | Rechercher des utilisateurs    |
| GET     | `/role/{roleName}` | Utilisateurs par rôle          |
| GET     | `/{id}`            | Détails d'un utilisateur       |
| POST    | `/`                | Créer un utilisateur avec rôle |
| PUT     | `/{id}`            | Modifier un utilisateur        |
| PATCH   | `/{id}/role`       | Changer le rôle                |
| PATCH   | `/{id}/enable`     | Activer un compte              |
| PATCH   | `/{id}/disable`    | Désactiver un compte           |
| DELETE  | `/{id}`            | Supprimer un utilisateur       |

### Endpoints Internes (`/api/v1/internal`)

| Méthode | Endpoint               | Description                           |
|---------|------------------------|---------------------------------------|
| GET     | `/validate`            | Valider un token JWT                  |
| GET     | `/users/{id}`          | Récupérer un utilisateur par ID       |
| GET     | `/users/email/{email}` | Récupérer un utilisateur par email    |
| GET     | `/users/{id}/exists`   | Vérifier l'existence d'un utilisateur |

## Configuration

### Fichiers d'environnement

Le service utilise la fonctionnalité native `spring.config.import` de Spring Boot pour charger les variables depuis un fichier `.env.properties`.

**Aucune dépendance externe requise !**

| Fichier                   | Description                          | Emplacement      |
|---------------------------|--------------------------------------|------------------|
| `.env.properties.example` | Template documenté (versionné)       | Racine du projet |
| `.env.properties`         | Configuration locale (non versionné) | Racine du projet |

```bash
# Copier le template
cp .env.properties.example .env.properties

# Modifier les valeurs selon votre environnement
nano .env.properties
```

> **Important** : Le fichier `.env.properties` doit être à la **racine du projet** (même niveau que `pom.xml`)

### Variables d'environnement

| Variable                 | Description              | Défaut                  |
|--------------------------|--------------------------|-------------------------|
| `AUTH_SERVICE_PORT`      | Port du service          | `8081`                  |
| `DB_HOST`                | Hôte MySQL               | `localhost`             |
| `DB_PORT`                | Port MySQL               | `3306`                  |
| `DB_NAME`                | Nom de la base           | `auth_db`               |
| `DB_USERNAME`            | Utilisateur MySQL        | `root`                  |
| `DB_PASSWORD`            | Mot de passe MySQL       | ``                      |
| `JPA_DDL_AUTO`           | Mode Hibernate           | `update`                |
| `JPA_SHOW_SQL`           | Afficher SQL             | `false`                 |
| `JWT_SECRET`             | Clé secrète JWT (Base64) | (généré)                |
| `JWT_ACCESS_EXPIRATION`  | Durée access token (ms)  | `900000` (15min)        |
| `JWT_REFRESH_EXPIRATION` | Durée refresh token (ms) | `604800000` (7j)        |
| `LOG_LEVEL_ROOT`         | Niveau log racine        | `INFO`                  |
| `LOG_LEVEL_APP`          | Niveau log application   | `DEBUG`                 |
| `LOG_LEVEL_SECURITY`     | Niveau log sécurité      | `DEBUG`                 |
| `LOG_LEVEL_SQL`          | Niveau log SQL           | `DEBUG`                 |
| `LOG_FILE_PATH`          | Chemin fichier log       | `logs/auth-service.log` |
| `SWAGGER_ENABLED`        | Activer Swagger UI       | `true`                  |
| `TOKEN_CLEANUP_CRON`     | CRON nettoyage tokens    | `0 0 2 * * ?`           |

## Installation

### Prérequis

- Java 25
- Spring 4.0
- Maven 3.9+
- MySQL 8.0+

### Lancement

```bash
# Cloner et naviguer vers le service
cd auth-service

# Compiler
mvn clean install

# Lancer
mvn spring-boot:run
```

### Avec Docker (optionnel)

```bash
# Build
docker build -t restobook/auth-service .

# Run
docker run -p 8081:8081 \
  -e DB_HOST=mysql \
  -e DB_PASSWORD=secret \
  restobook/auth-service
```

### Compte Admin par défaut

Au premier démarrage, un compte administrateur est créé automatiquement :

- **Email**: `admin@example.fr`
- **Mot de passe**: `StrongP@ssw0rd`

**Important**: Changez ce mot de passe en production !

## Documentation API

La documentation Swagger/OpenAPI est disponible à :

- **Swagger UI**: http://localhost:8081/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8081/api-docs

## Sécurité

### Authentification JWT

Le service utilise des tokens JWT pour l'authentification :

1. **Access Token** : Courte durée (15 min par défaut), utilisé pour les requêtes API
2. **Refresh Token** : Longue durée (7 jours), permet de renouveler l'access token

### Format du token

```
Authorization: Bearer <access_token>
```

### Contenu du token JWT

```json
{
  "sub": "user@example.com",
  "userId": 1,
  "role": "ROLE_CLIENT",
  "fullName": "John Doe",
  "iat": 1699999999,
  "exp": 1700000899
}
```

## Healthcheck

```bash
curl http://localhost:8081/actuator/health
```

## Communication Inter-Services

Les autres microservices peuvent valider les tokens via :

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

## Exemples de Requêtes

### Inscription

```bash
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jean",
    "lastName": "Dupont",
    "email": "jean.dupont@email.com",
    "password": "MonMotDePasse@123",
    "phone": "+33123456789"
  }'
```

**Réponse** (pas de token, l'utilisateur doit se connecter) :
```json
{
  "success": true,
  "message": "Inscription réussie. Veuillez vous connecter.",
  "data": {
    "id": 1,
    "firstName": "Jean",
    "lastName": "Dupont",
    "email": "jean.dupont@email.com",
    "role": "CLIENT"
  }
}
```

### Connexion

```bash
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "jean.dupont@email.com",
    "password": "MonMotDePasse@123"
  }'
```

**Réponse** :
```json
{
  "success": true,
  "message": "Connexion réussie",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
    "tokenType": "Bearer",
    "expiresIn": 900000,
    "user": {
      "id": 1,
      "email": "jean.dupont@email.com",
      "role": "CLIENT"
    }
  }
}
```

### Rafraîchir le token

```bash
curl -X POST http://localhost:8081/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
  }'
```

**Important - Rotation des tokens** :
- L'ancien refresh token est **révoqué** et **expiré** après utilisation
- Un **nouveau refresh token** est retourné dans la réponse
- Le client **DOIT** stocker ce nouveau refresh token pour les prochains rafraîchissements

### Déconnexion

```bash
curl -X POST http://localhost:8081/api/v1/auth/logout \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
  }'
```

**Note** : Après déconnexion, le refresh token est révoqué et expiré et ne peut plus être utilisé.

### Créer un utilisateur Staff (Admin)

```bash
curl -X POST http://localhost:8081/api/v1/admin/users \
  -H "Authorization: Bearer <admin_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jean",
    "lastName": "Dupont",
    "email": "jean.dupont@email.com",
    "password": "MonMotDePasse@123",
    "phone": "+33123456789",
    "roleName": "STAFF",
    "enabled": true
  }'
```

## License

Ce projet est sous licence MIT. Voir le fichier [LICENSE](LICENSE) pour plus de détails.

---

**Si ce projet vous a été utile, n'hésitez pas à lui donner une étoile ⭐**

Made with ❤️ by [Chéridanh TSIELA]
