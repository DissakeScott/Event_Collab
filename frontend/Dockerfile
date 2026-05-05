# Étape 1 : Build Angular
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build --configuration=production

# Étape 2 : Serveur Nginx
FROM nginx:alpine
# Copie des fichiers compilés (Vérifie bien le nom exact de ton dossier dans /dist !)
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html
# Copie de notre configuration réseau Nginx
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80