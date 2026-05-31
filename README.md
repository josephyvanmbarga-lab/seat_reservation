# Seat Reservation API

API REST de réservation de sièges dans des véhicules de transport, avec suivi GPS en temps réel et système de notifications.

## Stack technique

| Couche | Technologie |
|--------|-------------|
| Framework | Spring Boot 3.5.14 |
| Langage | Java 17 |
| Sécurité | Spring Security 6 + JWT (jjwt 0.12.6) |
| Base de données | PostgreSQL 16 + Flyway (migrations) |
| Messaging | Apache Kafka 3.6 |
| Temps réel | WebSocket STOMP |
| Documentation | Swagger UI (SpringDoc OpenAPI 2.8.8) |
| Build | Maven |

## Prérequis

- Java 17+
- Docker et Docker Compose
- (Optionnel) Une clé API HERE Maps pour le calcul de distance exact — un fallback Haversine est intégré

## Démarrage rapide

**1. Démarrer les services (PostgreSQL + Kafka)**

```bash
docker-compose up -d
```

**2. Configurer l'application**

Copier ou modifier `src/main/resources/application.properties` :

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/seat_reservation
spring.datasource.username=admin
spring.datasource.password=password123

app.jwt.secret=<votre-secret-hex-256-bits>
app.jwt.expiration=86400000

# Optionnel — fallback Haversine si non renseigné
app.here.api-key=YOUR_HERE_API_KEY_HERE

# Tarif en FCFA par kilomètre
app.tarif.par-km=100
```

**3. Lancer l'application**

```bash
./mvnw spring-boot:run
```

Les migrations Flyway s'exécutent automatiquement au démarrage.

**4. Accéder à la documentation interactive**

```
http://localhost:8080/swagger-ui.html
```

## Architecture des modules

```
src/main/java/com/mbarga/seat_reservation/
├── auth/           # Authentification JWT, gestion utilisateurs, rôles
├── vehicule/       # Gestion du parc de véhicules
├── trajet/         # Trajets (planifié → en cours → terminé), estimation tarifaire
├── reservation/    # Réservation de sièges, publication Kafka
├── notification/   # Consommateur Kafka, notifications in-app
├── avis/           # Système de notation chauffeur (1–5 étoiles)
├── suivi/          # Position GPS live (WebSocket) + liens de partage publics
└── tarification/   # Calcul distance/prix via HERE Maps ou Haversine
```

## Endpoints principaux

### Authentification (public)

| Méthode | URL | Description |
|---------|-----|-------------|
| POST | `/api/auth/register` | Inscription (USER / CHAUFFEUR / ADMIN) |
| POST | `/api/auth/login` | Connexion → retourne un JWT |

### Profil utilisateur

| Méthode | URL | Rôle requis |
|---------|-----|-------------|
| GET | `/api/users/me` | Voir son profil |
| PUT | `/api/users/me` | Modifier email / téléphone |
| PATCH | `/api/users/me/password` | Changer son mot de passe |
| GET | `/api/users` | Lister tous les utilisateurs (ADMIN) |
| PATCH | `/api/users/{id}/role` | Changer le rôle d'un utilisateur (ADMIN) |

### Véhicules

| Méthode | URL | Rôle requis |
|---------|-----|-------------|
| GET | `/api/vehicules` | Lister tous |
| GET | `/api/vehicules/{id}` | Détail |
| GET | `/api/vehicules/mes-vehicules` | Ses propres véhicules |
| POST | `/api/vehicules` | Créer (CHAUFFEUR / ADMIN) |
| PUT | `/api/vehicules/{id}` | Modifier (CHAUFFEUR / ADMIN) |
| DELETE | `/api/vehicules/{id}` | Supprimer (CHAUFFEUR / ADMIN) |

### Trajets

| Méthode | URL | Rôle requis |
|---------|-----|-------------|
| POST | `/api/trajets` | Créer un trajet (CHAUFFEUR / ADMIN) |
| GET | `/api/trajets` | Rechercher des trajets |
| GET | `/api/trajets/{id}` | Détail d'un trajet |
| GET | `/api/trajets/{id}/sieges-disponibles` | Sièges libres |
| GET | `/api/trajets/mes-trajets` | Trajets du chauffeur connecté |
| GET | `/api/trajets/historique` | Historique des trajets terminés |
| GET | `/api/trajets/estimation` | Estimation du prix (départ/arrivée GPS) |

### Réservations

| Méthode | URL | Description |
|---------|-----|-------------|
| POST | `/api/reservations` | Réserver un siège |
| GET | `/api/reservations/{id}` | Détail |
| GET | `/api/reservations` | Toutes les réservations (ADMIN) |
| GET | `/api/reservations/mes-reservations` | Ses propres réservations |
| GET | `/api/reservations/trajet/{id}` | Réservations d'un trajet |
| PATCH | `/api/reservations/{id}/annuler` | Annuler |
| PATCH | `/api/reservations/{id}/statut` | Changer le statut (CHAUFFEUR / ADMIN) |

### Avis

| Méthode | URL | Description |
|---------|-----|-------------|
| POST | `/api/avis` | Noter un chauffeur (trajet terminé requis) |
| GET | `/api/avis/chauffeur/{id}` | Avis d'un chauffeur |
| GET | `/api/avis/mes-avis` | Avis reçus (chauffeur connecté) |
| GET | `/api/avis` | Tous les avis |

### Notifications

| Méthode | URL | Description |
|---------|-----|-------------|
| GET | `/api/notifications` | Ses notifications |
| PATCH | `/api/notifications/{id}/lu` | Marquer comme lu |
| DELETE | `/api/notifications/{id}` | Supprimer |

### Suivi GPS

| Méthode | URL | Description |
|---------|-----|-------------|
| POST | `/api/suivi/lien/{reservationId}` | Générer un lien de partage |
| GET | `/api/suivi/{token}/positions` | Positions live (PUBLIC) |
| GET | `/api/suivi/trajet/{id}/positions` | Positions d'un trajet (auth) |
| DELETE | `/api/suivi/lien/{token}` | Révoquer un lien |
| WebSocket | `/ws` → `/topic/position/{trajetId}` | Position en temps réel (STOMP) |

## Rôles et permissions

| Rôle | Droits |
|------|--------|
| `USER` | Réserver, noter, recevoir des notifications |
| `CHAUFFEUR` | USER + créer trajets/véhicules, mettre à jour la position |
| `ADMIN` | Tous les droits + gestion des utilisateurs |

## Authentification

Toutes les requêtes protégées nécessitent un header :

```
Authorization: Bearer <token>
```

Le token est obtenu via `POST /api/auth/login`.

## Lancer les tests

```bash
./mvnw test
```

82 tests — 0 échec. Les tests d'intégration complets (`SeatReservationApplicationTests`) nécessitent PostgreSQL + Kafka actifs et sont désactivés en CI par défaut.

## Base de données

Les migrations Flyway créent automatiquement le schéma :

- **V1** : Tables initiales
- **V2** : Table `users`
- **V3** : Restructuration complète — `trajet`, `reservation`, `avis`, `position_live`, `lien_partage`
