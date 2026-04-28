

# 🎟️ EventCollab - Plateforme Collaborative de Gestion d'Événements

![Angular](https://img.shields.io/badge/Angular-DD0031?style=for-the-badge&logo=angular&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![WebSockets](https://img.shields.io/badge/WebSockets-000000?style=for-the-badge&logo=socket.io&logoColor=white)

EventCollab est une application Full-Stack orientée **Microservices** permettant la création, la gestion et la réservation d'événements. Elle intègre des fonctionnalités avancées de collaboration en temps réel telles qu'un chat en direct et des notifications instantanées.

## ✨ Fonctionnalités Principales

- **Sécurité & Authentification :** Inscription, connexion et gestion des rôles (User / Organizer / Admin) via **JWT** (JSON Web Tokens).
- **Gestion des Événements :** Création, publication et suivi de la jauge de capacité en temps réel.
- **Billetterie & QR Codes :** Réservation de billets avec génération automatique de QR Codes uniques pour le check-in.
- **Chat en Direct :** Salons de discussion dédiés par événement via **WebSockets** (STOMP/SockJS).
- **Notifications Temps Réel :** Alertes instantanées (réservation, annulation) poussées directement sur le client Angular sans rechargement.

## 🏗️ Architecture du Projet

Le projet est divisé en deux parties principales : un Frontend moderne et un Backend découpé en microservices indépendants.

### Backend (Java 17 / Spring Boot 3)
L'architecture microservices permet une excellente scalabilité. Les services communiquent entre eux de manière sécurisée (WebClient avec propagation du token JWT).

- `api-gateway` (Port 8080) : Point d'entrée unique, routage et gestion des CORS.
- `user-service` (Port 8081) : Gestion des utilisateurs et de l'authentification.
- `event-service` (Port 8082) : Gestion du cycle de vie des événements et des capacités.
- `ticket-service` (Port 8083) : Réservation, annulation et génération des QR Codes.
- `notification-service` (Port 8084) : Serveur WebSocket central, historisation du chat et envois de notifications.

### Frontend (Angular 17+)
Application SPA (Single Page Application) performante utilisant les dernières fonctionnalités d'Angular :
- Architecture **Standalone Components**.
- Détection de changement optimisée.
- Intercepteurs HTTP pour la gestion automatique des tokens.
- Services réactifs (RxJS / Signals) pour la gestion d'état et le temps réel.

## 🚀 Installation et Lancement en local

### Prérequis
- Java 17+
- Node.js & npm
- PostgreSQL (Base de données)

### Lancement du Backend
1. Assurez-vous que votre base de données PostgreSQL est en cours d'exécution.
2. Lancez les microservices dans l'ordre suivant (via votre IDE ou Maven) :
   - `api-gateway`
   - `user-service`
   - `event-service`
   - `ticket-service`
   - `notification-service`

### Lancement du Frontend
```bash
cd frontend
npm install
npm start
