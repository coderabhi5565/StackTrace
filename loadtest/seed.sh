#!/usr/bin/env bash
# Seeds load-test data. Usage: PGHOST=localhost PGUSER=postgres PGPASSWORD=... ./loadtest/seed.sh
# (with docker compose: docker compose exec -T postgres psql ... is used automatically)
set -euo pipefail
cd "$(dirname "$0")"
run() { # db file
  if command -v docker >/dev/null && docker compose ps postgres >/dev/null 2>&1 && [ -z "${PGHOST:-}" ]; then
    docker compose exec -T postgres psql -U "${POSTGRES_USER:-postgres}" -d "$1" -v ON_ERROR_STOP=1 < "$2"
  else
    psql -d "$1" -v ON_ERROR_STOP=1 -f "$2"
  fi
}
run "${USER_DB:-userdb}" seed-users.sql
run "${POST_DB:-postdb}" seed-posts.sql
echo "Seeded."
