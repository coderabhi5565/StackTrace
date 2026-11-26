#!/usr/bin/env bash
# Deploys the StackTrace backend to Fly.io: one app per service, internal services on the private
# network only (Flycast), gateway public. Idempotent: re-run to redeploy.
#
# Required env (see docs/DEPLOY.md):
#   FLY_PREFIX              unique app-name prefix, e.g. "stacktrace-jdoe"
#   FLY_REGION              e.g. "fra" (put it next to your Neon/Upstash region)
#   JWT_SECRET, INTERNAL_API_TOKEN
#   USER_DB_URL, POST_DB_URL, COMMENT_DB_URL, NOTIFICATION_DB_URL   (jdbc:postgresql://...?...sslmode=require)
#   DB_USERNAME, DB_PASSWORD
#   REDIS_URL               rediss://default:<password>@<host>:6379   (Upstash)
#   CORS_ALLOWED_ORIGINS    https://your-frontend.vercel.app
# Optional: CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, CLOUDINARY_API_SECRET
# Usage: ./deploy/fly/deploy.sh [service ...]     (default: all, in dependency order)
set -euo pipefail
cd "$(dirname "$0")/../.."
command -v fly >/dev/null || { echo "Install flyctl: https://fly.io/docs/flyctl/install/"; exit 2; }
: "${FLY_PREFIX:?}" "${FLY_REGION:?}" "${JWT_SECRET:?}" "${INTERNAL_API_TOKEN:?}"

dir_for() { # works with macOS's bash 3.2 (no associative arrays)
  case $1 in
    user-service) echo User-Service ;; post-service) echo post-service ;; comment-service) echo comment-service ;;
    search-service) echo search-service ;; notification-service) echo NotificationService ;; gateway) echo api-gateway ;;
    *) echo "unknown service: $1" >&2; exit 2 ;;
  esac
}
if [ $# -gt 0 ]; then TARGETS=("$@"); else
  TARGETS=(user-service post-service notification-service comment-service search-service gateway); fi
RENDER="$(mktemp -d)"

secrets_for() {
  local s=$1
  echo "JWT_SECRET=$JWT_SECRET"
  [[ $s != gateway ]] && echo "INTERNAL_API_TOKEN=$INTERNAL_API_TOKEN"
  case $s in
    user-service) echo "DB_URL=$USER_DB_URL"; echo "REDIS_URL=$REDIS_URL" ;;
    post-service) echo "DB_URL=$POST_DB_URL"; echo "REDIS_URL=$REDIS_URL"
                  echo "CLOUDINARY_CLOUD_NAME=${CLOUDINARY_CLOUD_NAME:-}"; echo "CLOUDINARY_API_KEY=${CLOUDINARY_API_KEY:-}"
                  echo "CLOUDINARY_API_SECRET=${CLOUDINARY_API_SECRET:-}" ;;
    comment-service) echo "DB_URL=$COMMENT_DB_URL" ;;
    notification-service) echo "DB_URL=$NOTIFICATION_DB_URL" ;;
    gateway) echo "REDIS_URL=$REDIS_URL"; echo "CORS_ALLOWED_ORIGINS=$CORS_ALLOWED_ORIGINS" ;;
  esac
  case $s in user-service|post-service|comment-service|notification-service)
    echo "DB_USERNAME=$DB_USERNAME"; echo "DB_PASSWORD=$DB_PASSWORD" ;; esac
}

for s in "${TARGETS[@]}"; do
  app="$FLY_PREFIX-$s"
  cfg="$RENDER/$s.toml"
  sed -e "s/__PREFIX__/$FLY_PREFIX/g" -e "s/__REGION__/$FLY_REGION/g" "deploy/fly/$s.toml" > "$cfg"
  echo "==> $app"
  if ! fly apps list --json | grep -q "\"Name\": *\"$app\""; then
    fly apps create "$app"
    if [[ $s != gateway ]]; then
      fly ips allocate-v6 --private -a "$app"      # Flycast: reachable only inside your Fly org
    fi
  fi
  secrets_for "$s" | fly secrets import -a "$app" --stage
  if [[ $s == gateway ]]; then
    fly deploy "backend/$(dir_for "$s")" -c "$cfg" --remote-only --ha=false
  else
    fly deploy "backend/$(dir_for "$s")" -c "$cfg" --remote-only --ha=false --no-public-ips
  fi
done
echo "Done. Gateway: https://$FLY_PREFIX-gateway.fly.dev  (try: ./scripts/smoke-test.sh https://$FLY_PREFIX-gateway.fly.dev)"
