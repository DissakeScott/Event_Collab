# 🎟️ EventCollab - Plateforme Collaborative de Gestion d'Événements

![Angular](https://img.shields.io/badge/Angular-DD0031?style=for-the-badge&logo=angular&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![WebSockets](https://img.shields.io/badge/WebSockets-000000?style=for-the-badge&logo=socket.io&logoColor=white)


<img width="3440" height="1408" alt="Capture d’écran du 2026-04-28 16-29-59" src="https://github.com/user-attachments/assets/2e583222-f78e-429b-8fdc-8264ae6edd98" />
<p></p>
<img width="3440" height="1408" alt="Capture d’écran du 2026-04-28 16-30-12" src="https://github.com/user-attachments/assets/dfe3e3b7-00a9-4b3b-a137-54016f174534" />
<p></p>
<img width="3440" height="1408" alt="Capture d’écran du 2026-04-28 16-31-01" src="https://github.com/user-attachments/assets/405f7087-df90-4090-838d-c281d443ffb2" />
<img width="3440" height="1408" alt="Capture d’écran du 2026-04-28 16-24-48" src="https://github.com/user-attachments/assets/b23b96a5-d589-4061-b7f6-f9970b6c50c9" />
<p></p>
<img width="3440" height="1408" alt="Capture d’écran du 2026-04-28 16-21-24" src="https://github.com/user-attachments/assets/7d33ddb6-c90f-4943-ac6f-d3656353d000" />
<p></p>
<img width="3440" height="1408" alt="Capture d’écran du 2026-04-28 16-00-22" src="https://github.com/user-attachments/assets/0c18b42a-0526-4e52-aab4-b5907271db47" />
<p></p>
<img width="3440" height="1408" alt="Capture d’écran du 2026-04-28 16-00-10" src="https://github.com/user-attachments/assets/c45b926f-45d3-453b-a1e2-698cce902186" />
<p></p>



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
