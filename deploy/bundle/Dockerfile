# syntax=docker/dockerfile:1.7
# Several backend services in ONE container/VM (budget deployments). Choose which with $SERVICES.
# Build from the repo root:  docker build -f deploy/bundle/Dockerfile -t stacktrace-bundle .
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY backend/ ./backend/
RUN --mount=type=cache,target=/root/.m2 set -e; mkdir /jars; \
    for d in User-Service:user post-service:post comment-service:comment search-service:search \
             NotificationService:notification api-gateway:gateway; do \
      dir=${d%%:*}; name=${d##*:}; \
      (cd backend/$dir && mvn -B -q -ntp package -DskipTests && cp target/*-SNAPSHOT.jar /jars/$name.jar); \
    done

FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache bash && addgroup -S app && adduser -S -G app app
WORKDIR /app
COPY --from=build --chown=app:app /jars/ ./
COPY --chown=app:app deploy/bundle/start.sh ./start.sh
USER app
ENV SERVICES="user post comment search notification gateway"
EXPOSE 8080
HEALTHCHECK --interval=15s --timeout=3s --start-period=120s --retries=5 \
  CMD wget -qO- "http://127.0.0.1:${GATEWAY_PORT:-8080}/actuator/health/liveness" >/dev/null 2>&1 \
   || wget -qO- "http://127.0.0.1:8081/actuator/health/liveness" >/dev/null || exit 1
ENTRYPOINT ["bash", "/app/start.sh"]
