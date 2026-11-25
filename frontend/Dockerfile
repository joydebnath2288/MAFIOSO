# =============================================================================
# MAFIOSO Frontend - Multi-stage Build (Node.js Vite -> Nginx Alpine)
# =============================================================================

FROM node:22.23.5-alpine3.23 AS builder
WORKDIR /app

ARG VITE_APP_NAME=MAFIOSO
ARG VITE_API_BASE_URL=
ENV VITE_APP_NAME=${VITE_APP_NAME}
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}

COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund

COPY . .
RUN npm run build

FROM nginx:1.28.3-alpine3.23

RUN rm -f /etc/nginx/conf.d/default.conf
COPY nginx/default.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80

HEALTHCHECK --interval=15s --timeout=5s --start-period=10s --retries=3 \
    CMD wget -qO- http://127.0.0.1:80/ > /dev/null || exit 1

CMD ["nginx", "-g", "daemon off;"]
