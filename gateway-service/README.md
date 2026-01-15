# Gateway Service - RestoBook QuickEat

API Gateway unifié pour la plateforme RestoBook de QuickEat.

## Table des matières

- [Description](#description)
- [Rôle dans l'architecture](#rôle-dans-larchitecture)
- [Routes disponibles](#routes-disponibles)
- [Fonctionnalités](#fonctionnalités)
- [Configuration](#configuration)
- [Installation](#installation)
- [Utilisation](#utilisation)
- [Healthcheck](#healthcheck)
- [Sécurité](#sécurité)
- [License](#license)

## Description

Le **Gateway Service** est le **point d'entrée unique** pour toutes les requêtes API de la plateforme RestoBook. Il utilise Spring Cloud Gateway pour :

- **Router** les requêtes vers les microservices appropriés
- **Équilibrer la charge** entre plusieurs instances
- **Gérer la sécurité** de manière centralisée
- **Offrir une API unifiée** aux clients

## Rôle dans l'architecture

```
┌────────────────────────────────────────────────────────┐
│                    Clients (Web/Mobile)                │
└──────────────────────────┬─────────────────────────────┘
                               │
                      ┌────────▼────────┐
                      │  Gateway :8088  │ ← Point d'entrée unique
                      └────────┬────────┘
                               │
                ┌──────────────┼──────────────┐
                │         Eureka :8761        │ ← Découverte des services
                └──────────────┬──────────────┘
                               │
                ┌─────────┬────┼───┬─────────┐
                │         │        │         │
            ┌───▼───┐  ┌──▼───┐  ┌──▼───┐ ┌──▼───┐
            │ Auth  │  │ Rest.│  │ Book.│ │Review│
            │ 8081  │  │ 8082 │  │ 8083 │ │ 8084 │
            └───────┘  └──────┘  └──────┘ └──────┘
```

## Routes disponibles

Le Gateway route automatiquement les requêtes vers les microservices via leur nom Eureka.

### Routes automatiques (créées par découverte)

Grâce à la configuration de découverte automatique, le Gateway crée ces routes :

| Route Gateway            | Service cible      | Port | Exemple                                                       |
|--------------------------|--------------------|------|---------------------------------------------------------------|
| `/auth-service/**`       | auth-service       | 8081 | `http://localhost:8088/auth-service/api/v1/auth/login`        |
| `/restaurant-service/**` | restaurant-service | 8082 | `http://localhost:8088/restaurant-service/api/v1/restaurants` |
| `/booking-service/**`    | booking-service    | 8083 | `http://localhost:8088/booking-service/api/v1/bookings`       |
| `/review-service/**`     | review-service     | 8084 | `http://localhost:8088/review-service/api/v1/reviews`         |

### Format de requête via Gateway

```
http://localhost:8088/{service-name}/{path-du-service}
```

**Exemples** :
```bash
# Auth Service
curl http://localhost:8088/auth-service/api/v1/auth/login

# Restaurant Service
curl http://localhost:8088/restaurant-service/api/v1/restaurants

# Booking Service
curl http://localhost:8088/booking-service/api/v1/bookings

# Review Service
curl http://localhost:8088/review-service/api/v1/reviews
```

> **Note** : Le nom du service (`auth-service`, `restaurant-service`, etc.) fait partie de l'URL. Cela permet au Gateway de savoir vers quel service router la requête.

### Accès via le Gateway

**Avant (accès direct)** :
```bash
# Appel direct aux services
curl http://localhost:8081/api/v1/auth/login
curl http://localhost:8082/api/v1/restaurants
curl http://localhost:8083/api/v1/bookings
curl http://localhost:8084/api/v1/reviews
```

**Après (via Gateway avec découverte automatique)** :
```bash
# Tout passe par le Gateway avec le nom du service dans l'URL
curl http://localhost:8088/auth-service/api/v1/auth/login
curl http://localhost:8088/restaurant-service/api/v1/restaurants
curl http://localhost:8088/booking-service/api/v1/bookings
curl http://localhost:8088/review-service/api/v1/reviews
```

**Format** : `http://gateway:8088/{nom-service}/{path-original}`

## Fonctionnalités

### 1. Routage dynamique

Le Gateway utilise **Eureka** pour découvrir les services automatiquement :
- Aucune configuration d'URL statique nécessaire
- Équilibrage de charge automatique si plusieurs instances
- Failover automatique en cas d'indisponibilité

### 2. Découverte automatique

Grâce à `spring.cloud.gateway.discovery.locator.enabled=true`, le Gateway crée automatiquement des routes pour tous les services enregistrés dans Eureka.

**Format de route** : `http://gateway:8088/SERVICE-NAME/**`

Exemples :
- `http://localhost:8088/auth-service/**`
- `http://localhost:8088/restaurant-service/**`

### 3. Load Balancing

Si plusieurs instances d'un service sont disponibles, le Gateway répartit automatiquement la charge entre elles (Round Robin par défaut).

### 4. Centralisation

- **Point d'entrée unique** pour les clients
- **Gestion centralisée** de la sécurité (CORS, authentification)
- **Logs unifiés** de toutes les requêtes
- **Monitoring** centralisé

## Configuration

### Fichiers d'environnement

```bash
# Copier le template
cp .env.properties.example .env.properties

# Modifier les valeurs
nano .env.properties
```

### Variables d'environnement

| Variable               | Description            | Défaut                         |
|------------------------|------------------------|--------------------------------|
| `GATEWAY_SERVICE_PORT` | Port du Gateway        | `8088`                         |
| `EUREKA_SERVER_URL`    | URL du serveur Eureka  | `http://localhost:8761/eureka` |
| `LOG_LEVEL_ROOT`       | Niveau log racine      | `INFO`                         |
| `LOG_LEVEL_APP`        | Niveau log application | `DEBUG`                        |
| `LOG_FILE_PATH`        | Chemin fichier log     | `logs/gateway-service.log`     |

### Découverte automatique des routes

**Le Gateway crée automatiquement les routes** grâce à la configuration suivante :

#### Dans `GatewayServiceApplication.java` :
```java
@Bean
DiscoveryClientRouteDefinitionLocator dynamicRoutes(ReactiveDiscoveryClient rdc, DiscoveryLocatorProperties dlp) {
    return new DiscoveryClientRouteDefinitionLocator(rdc, dlp);
}
```

#### Dans `application.yml` :
```yaml
spring:
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
```

Avec cette configuration, **AUCUNE route manuelle n'est nécessaire**. Le Gateway découvre automatiquement tous les services via Eureka et crée les routes correspondantes.

**Routes créées automatiquement** :
- `http://localhost:8088/auth-service/**` → auth-service
- `http://localhost:8088/restaurant-service/**` → restaurant-service
- `http://localhost:8088/booking-service/**` → booking-service
- `http://localhost:8088/review-service/**` → review-service

### Configuration avancée (optionnelle)

**Note** : La configuration ci-dessous est **optionnelle** et utile uniquement si vous avez besoin de :
- Ajouter des filtres personnalisés (authentification, rate limiting, etc.)
- Modifier le path de routage (exemple : `/api/v1/auth/**` au lieu de `/auth-service/**`)
- Configurer des timeouts spécifiques
- Ajouter des transformations de requête/réponse

#### Exemple de route personnalisée avec filtres :

```yaml
spring:
  cloud:
    gateway:
      routes:
        # Route personnalisée pour Auth Service
        - id: auth-custom
          uri: lb://auth-service
          predicates:
            - Path=/api/v1/auth/**
          filters:
            - StripPrefix=2  # Retire /api/v1 du path
            - name: CircuitBreaker
              args:
                name: authCircuitBreaker
                fallbackUri: forward:/fallback
        
        # Route avec rate limiting
        - id: public-api
          uri: lb://restaurant-service
          predicates:
            - Path=/public/**
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10
                redis-rate-limiter.burstCapacity: 20
```

**Important** : Si vous définissez des routes manuelles, elles ont la priorité sur les routes automatiques. Assurez-vous qu'elles ne créent pas de conflits.

## Installation

### Prérequis

- Java 25
- Spring Boot 4.0
- Maven 3.9+
- **Eureka Service** démarré

### Lancement

```bash
# Naviguer vers le service
cd gateway-service

# Compiler
mvn clean install

# Lancer (port 8088)
mvn spring-boot:run
```

### Avec Docker (optionnel)

```bash
# Build
docker build -t restobook/gateway-service .

# Run
docker run -p 8088:8088 \
  -e EUREKA_SERVER_URL=http://eureka:8761/eureka \
  restobook/gateway-service
```

## Utilisation

### Exemples de requêtes

#### 1. Inscription via le Gateway

```bash
curl -X POST http://localhost:8088/auth-service/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jean",
    "lastName": "Dupont",
    "email": "jean.dupont@email.com",
    "password": "MonMotDePasse@123",
    "phone": "+33123456789"
  }'
```

#### 2. Lister les restaurants

```bash
curl http://localhost:8088/restaurant-service/api/v1/restaurants
```

#### 3. Créer une réservation

```bash
curl -X POST http://localhost:8088/booking-service/api/v1/bookings \
  -H "Authorization: Bearer " \
  -H "Content-Type: application/json" \
  -d '{
    "restaurantId": 1,
    "bookingDate": "2024-12-20",
    "bookingTime": "19:30",
    "partySize": 4
  }'
```

#### 4. Laisser un avis

```bash
curl -X POST http://localhost:8088/review-service/api/v1/reviews \
  -H "Authorization: Bearer " \
  -H "Content-Type: application/json" \
  -d '{
    "restaurantId": 1,
    "bookingId": 5,
    "rating": 4,
    "comment": "Excellent service !"
  }'
```

## Healthcheck

```bash
curl http://localhost:8088/actuator/health
```

Réponse :
```json
{
  "status": "UP"
}
```

### Vérifier les routes

```bash
curl http://localhost:8088/actuator/gateway/routes
```

Cette commande liste toutes les routes actives et leurs configurations.

## Sécurité

### CORS (Cross-Origin Resource Sharing)

Le Gateway peut gérer la configuration CORS de manière centralisée. Ajoutez dans `application.yml` :

```yaml
spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOrigins: "http://localhost:3000"
            allowedMethods:
              - GET
              - POST
              - PUT
              - DELETE
              - PATCH
            allowedHeaders: "*"
            allowCredentials: true
```

### Authentification

Le Gateway peut valider les tokens JWT avant de router les requêtes vers les services. Cela évite que chaque service ait à valider le token indépendamment.

### Rate Limiting (optionnel)

Vous pouvez ajouter un filtre de limitation de débit :

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: auth-service
          uri: lb://auth-service
          predicates:
            - Path=/api/v1/auth/**
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10
                redis-rate-limiter.burstCapacity: 20
```

## Monitoring

### Métriques disponibles

```bash
# Métriques générales
curl http://localhost:8088/actuator/metrics

# Métriques spécifiques au Gateway
curl http://localhost:8088/actuator/metrics/gateway.requests
```

### Logs

Les logs du Gateway incluent :
- Toutes les requêtes entrantes
- Routes sélectionnées
- Temps de réponse
- Erreurs de routage

```bash
# Suivre les logs
tail -f logs/gateway-service.log
```

## Troubleshooting

### Le Gateway ne trouve pas les services

**Vérifiez** :
1. Eureka est démarré et accessible
2. Les services sont enregistrés dans Eureka (vérifiez http://localhost:8761)
3. La configuration `eureka.client.service-url.defaultZone` est correcte

### Erreurs 503 Service Unavailable

Cela signifie que le service cible n'est pas disponible :
1. Vérifiez que le service est démarré
2. Vérifiez qu'il est bien enregistré dans Eureka
3. Consultez les logs du Gateway pour plus de détails

### Routes non trouvées (404)

Vérifiez :
1. Le préfixe de route est correct
2. Le service cible existe et est enregistré
3. Les logs du Gateway pour voir les routes actives

## Ordre de démarrage

Pour éviter les erreurs :

1. **Eureka Service** (8761) - En premier
2. **Microservices** (8081-8084) - Ensuite
3. **Gateway Service** (8088) - En dernier

> **Astuce** : Le Gateway peut démarrer avant les autres services, mais les routes ne seront actives que lorsque les services seront enregistrés dans Eureka.

## Avantages du Gateway

**Point d'entrée unique** - Simplifie l'intégration client  
**Découverte automatique** - Pas besoin de connaître les ports des services  
**Load Balancing** - Répartition automatique de la charge  
**Sécurité centralisée** - CORS, authentification, rate limiting  
**Monitoring** - Métriques et logs centralisés  
**Évolutivité** - Ajout facile de nouveaux services

## License

Ce projet est sous licence MIT. Voir le fichier [LICENSE](LICENSE) pour plus de détails.

---

**Si ce projet vous a été utile, n'hésitez pas à lui donner une étoile ⭐**

Made with ❤️ by [Chéridanh TSIELA]
