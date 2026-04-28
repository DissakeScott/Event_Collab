#!/bin/bash
echo "=== Démarrage EventCollab ==="

# Démarrer PostgreSQL
sudo docker start pg-users
sleep 2

# Recompiler common
echo "Compilation common..."
cd ~/Bureau/eventcollab
mvn clean install -pl common -DskipTests -q

echo "Lancez chaque service dans un terminal séparé :"
echo "  mvn spring-boot:run -pl user-service"
echo "  mvn spring-boot:run -pl event-service"
echo "  mvn spring-boot:run -pl ticket-service"
echo "  mvn spring-boot:run -pl notification-service"
