# 🎟️ EventCollab - Plateforme Collaborative de Gestion d'Événements


![Angular](https://img.shields.io/badge/Angular-DD0031?style=for-the-badge&logo=angular&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![WebSockets](https://img.shields.io/badge/WebSockets-000000?style=for-the-badge&logo=socket.io&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)


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



docker-compose up -d --build



```markdown



EventCollab est une application Full-Stack orientée **Microservices** permettant la création, la gestion et la réservation d'événements. Elle intègre des fonctionnalités avancées de collaboration en temps réel telles qu'un chat en direct et des notifications instantanées.

L'ensemble de l'application est entièrement conteneurisé via **Docker** pour un déploiement fluide, isolé et prêt pour la production.

---

## ✨ Fonctionnalités Principales

*   **Architecture Microservices :** Découpage métier strict assurant scalabilité et maintenabilité. Communication inter-services fluide via `WebClient`.
*   **Sécurité & Authentification :** Inscription, connexion et gestion des rôles (User / Organizer / Admin) via **JWT** (JSON Web Tokens). Le contexte de sécurité est propagé entre les microservices.
*   **Gestion des Événements :** Création d'événements, suivi de la jauge de capacité en temps réel et gestion des statuts.
*   **Billetterie & QR Codes :** Réservation de billets avec génération automatique de QR Codes uniques pour le check-in le jour de l'événement.
*   **Temps Réel (WebSockets) :** 
    *   Salons de discussion (Chat) dédiés par événement via STOMP/SockJS.
    *   Notifications "push" instantanées lors d'une réservation ou d'une annulation, sans rechargement de la page.
*   **Conteneurisation totale :** Multi-stage builds pour le frontend (Angular + Nginx) et images Alpine allégées pour le backend Java.

---

## 🏗️ Architecture du Projet

Le système s'articule autour d'un Frontend moderne et d'un Backend découpé en 5 services indépendants, le tout relié par un réseau virtuel Docker (`ec-network`).

### Backend (Java 17 / Spring Boot 3)
*   `api-gateway` (Port 8080) : Point d'entrée unique de l'application, gestion du routage dynamique et des CORS.
*   `user-service` (Port 8081) : Gestion des utilisateurs, de l'authentification et de la validation des tokens JWT.
*   `event-service` (Port 8082) : Gestion du cycle de vie des événements et du contrôle des capacités.
*   `ticket-service` (Port 8083) : Réservation, annulation, génération des QR Codes et communication avec les autres services.
*   `notification-service` (Port 8084) : Serveur WebSocket central, historisation du chat et envois de notifications asynchrones.
*   **Base de données :** Instance PostgreSQL 15 centralisée et persistante.

### Frontend (Angular 17+)
Application SPA (Single Page Application) performante servie par **Nginx** :
*   Architecture basée sur les **Standalone Components**.
*   Gestion d'état réactive et détection de changement optimisée avec les **Signals**.
*   Intercepteurs HTTP pour l'injection automatique des tokens JWT.
*   Services RxJS pour la gestion des flux WebSockets en temps réel.

---

## 🚀 Installation et Déploiement Local

Le projet est conçu pour être lancé en une seule commande grâce à l'orchestration Docker Compose.

### Prérequis
*   [Docker](https://docs.docker.com/get-docker/) et Docker Compose installés.
*   Java 17 et Maven (pour la compilation préalable des services).

### Étapes de lancement

1. **Cloner le dépôt :**
   ```bash
   git clone https://github.com/DissakeScott/Event_Collab.git
   cd Event_Collab
