# Eureka Service - RestoBook QuickEat

Service de découverte et d'enregistrement des microservices pour la plateforme RestoBook de QuickEat.

## Table des matières

- [Description](#description)
- [Rôle dans l'architecture](#rôle-dans-larchitecture)
- [Services enregistrés](#services-enregistrés)
- [Endpoints](#endpoints)
- [Configuration](#configuration)
- [Installation](#installation)
- [Interface web](#interface-web)
- [Healthcheck](#healthcheck)
- [License](#license)

## Description

Eureka est le **service de découverte** de Netflix utilisé dans l'architecture microservices de RestoBook. Il permet aux services de :

- **S'enregistrer automatiquement** au démarrage
- **Se découvrir mutuellement** sans configuration statique
- **Maintenir un registre** des instances disponibles
- **Surveiller la santé** des services via heartbeat

## Rôle dans l'architecture

```
┌─────────────────┐
│  Eureka Server  │ ← Services s'enregistrent ici
│   (port 8761)   │
└────────┬────────┘
         │
    ┌────┴────┬─────────┬──────────┬─────────┐
    │         │         │          │         │
┌───▼────┐ ┌──▼───┐ ┌───▼────┐ ┌───▼────┐ ┌──▼────┐
│ Auth   │ │ Rest.│ │ Booking│ │ Review │ │Gateway│
│ 8081   │ │ 8082 │ │ 8083   │ │ 8084   │ │ 8088  │
└────────┘ └──────┘ └────────┘ └────────┘ └───────┘
```

## Services enregistrés

Tous les microservices de RestoBook s'enregistrent automatiquement auprès d'Eureka :

| Service              | Port | Instance ID             |
|----------------------|------|-------------------------|
| `auth-service`       | 8081 | auth-service:8081       |
| `restaurant-service` | 8082 | restaurant-service:8082 |
| `booking-service`    | 8083 | booking-service:8083    |
| `review-service`     | 8084 | review-service:8084     |
| `gateway-service`    | 8088 | gateway-service:8088    |

## Endpoints

### Interface Web Eureka

| Endpoint                | Description                                         |
|-------------------------|-----------------------------------------------------|
| `/`                     | Dashboard Eureka (interface web)                    |
| `/eureka/apps`          | Liste de toutes les applications enregistrées (XML) |
| `/eureka/apps/APP_NAME` | Informations d'une application spécifique           |

### Actuator

| Endpoint            | Description                       |
|---------------------|-----------------------------------|
| `/actuator/health`  | Statut de santé du serveur Eureka |
| `/actuator/info`    | Informations sur l'application    |
| `/actuator/metrics` | Métriques du serveur              |

## Configuration

### Fichiers d'environnement

```bash
# Copier le template
cp .env.properties.example .env.properties

# Modifier les valeurs
nano .env.properties
```

### Variables d'environnement

| Variable              | Description            | Défaut                    |
|-----------------------|------------------------|---------------------------|
| `EUREKA_SERVICE_PORT` | Port du serveur Eureka | `8761`                    |
| `LOG_LEVEL_ROOT`      | Niveau log racine      | `INFO`                    |
| `LOG_LEVEL_APP`       | Niveau log application | `DEBUG`                   |
| `LOG_FILE_PATH`       | Chemin fichier log     | `logs/eureka-service.log` |

### Configuration des clients Eureka

Les services clients doivent configurer cette URL dans leur `application.yml` :

```yaml
eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_SERVER_URL:http://localhost:8761/eureka}
```

## Installation

### Prérequis

- Java 25
- Spring Boot 4.0
- Maven 3.9+

### Lancement

```bash
# Naviguer vers le service
cd eureka-service

# Compiler
mvn clean install

# Lancer (port 8761)
mvn spring-boot:run
```

### Avec Docker (optionnel)

```bash
# Build
docker build -t restobook/eureka-service .

# Run
docker run -p 8761:8761 restobook/eureka-service
```

## Interface web

Le dashboard Eureka est accessible à l'adresse :

**http://localhost:8761**

Vous y trouverez :
- Liste des services enregistrés
- État de santé de chaque instance
- Nombre de renouvellements de bail (heartbeats)
- Informations système

### Exemple de vue

```
Application         AMIs        Availability Zones    Status
AUTH-SERVICE        n/a         (1)                   UP (1) - localhost:auth-service:8081
RESTAURANT-SERVICE  n/a         (1)                   UP (1) - localhost:restaurant-service:8082
BOOKING-SERVICE     n/a         (1)                   UP (1) - localhost:booking-service:8083
REVIEW-SERVICE      n/a         (1)                   UP (1) - localhost:review-service:8084
GATEWAY-SERVICE     n/a         (1)                   UP (1) - localhost:gateway-service:8088
```

## Healthcheck

```bash
curl http://localhost:8761/actuator/health
```

Réponse :
```json
{
  "status": "UP"
}
```

## Ordre de démarrage recommandé

Pour un démarrage optimal de la plateforme :

1. **Eureka Service** (port 8761) - D'abord !
2. **Auth Service** (port 8081)
3. **Restaurant Service** (port 8082)
4. **Booking Service** (port 8083)
5. **Review Service** (port 8084)
6. **Gateway Service** (port 8088) - En dernier

> **Note** : Attendez environ 30 secondes après le démarrage d'Eureka avant de démarrer les autres services pour permettre l'initialisation complète.

## Troubleshooting

### Les services ne s'enregistrent pas

**Vérifiez** :
1. Eureka est démarré et accessible sur http://localhost:8761
2. La configuration `eureka.client.service-url.defaultZone` est correcte
3. Les services ont `spring.cloud.discovery.enabled=true`

### Erreurs de connexion

```bash
# Tester la connexion
curl http://localhost:8761/eureka/apps

# Vérifier les logs
tail -f logs/eureka-service.log
```

## License

Ce projet est sous licence MIT. Voir le fichier [LICENSE](LICENSE) pour plus de détails.

---

**Si ce projet vous a été utile, n'hésitez pas à lui donner une étoile ⭐**

Made with ❤️ by [Chéridanh TSIELA]
