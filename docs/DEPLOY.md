# Deploying StackTrace

This guide takes you from an empty set of accounts to a live site:

| Piece | Where | Why |
|---|---|---|
| Frontend (Next.js) | **Vercel** | Native Next.js hosting: ISR, image optimization, edge CDN, preview deploys |
| Backend (gateway + 5 services) | **Fly.io** | See "Why Fly.io" below |
| PostgreSQL | **Neon** | Serverless Postgres, free tier, `pg_trgm` available, branching and point-in-time restore |
| Redis | **Upstash** | Serverless Redis with TLS, free tier, pay per request |

```
Browser ──https──▶ Vercel (Next.js + BFF, httpOnly cookies)
                      │  server-side only, API_BASE_URL
                      ▼
              Fly: <prefix>-gateway (public)  ── Upstash Redis (rate limits)
                      │  Fly private network (Flycast)
   ┌──────────┬───────┴───────┬──────────────┬──────────────────┐
 user-svc   post-svc      comment-svc    search-svc    notification-svc
   │  └── Upstash Redis (cache) ──┘          │ (no DB)          │
   └──────────── Neon Postgres: userdb · postdb · commentdb · notificationdb ─┘
```

## Why Fly.io

- **Private networking for free.** Internal services get only a private Flycast address, so
  `/internal/**` and the services themselves are unreachable from the internet. Render and Railway need
  paid plans or extra configuration for the same isolation.
- **Right-sized machines.** Each JVM gets its own 512 MB machine. Render's free tier has a shared 750
  instance-hours a month (six services always on need about 4,400), sleeps after 15 minutes and has no
  private network. Render's paid tier is $7 per service per month ($42 here).
- **Auto stop/start.** Low-traffic services (comment, search, notification) scale to zero and wake on
  the first private request.
- **Docker-native.** The same Dockerfiles as `docker compose` and CI, built by Fly's remote builder.

Railway is a fine alternative with similar DX (private networking, Dockerfiles), billed on usage. With
six JVMs at about 0.25–0.3 GB each it costs roughly the same as Fly, with no scale-to-zero.

### Cost estimate (Fly pay-as-you-go, check current pricing)

| Layout | Machines | Approx. / month |
|---|---|---|
| **A. One app per service** (recommended) | gateway, user, post always on; comment, search, notification auto-stop | ~$10–20 |
| **B. Budget bundle** | one `shared-cpu-2x` 2 GB machine running all six JVMs | ~$12–15 |
| Neon, Upstash, Vercel | free tiers | $0 |

**Memory and co-deploy recommendation.** Measured RSS per JVM with the container flags:
post 300 MB, user 220 MB, gateway 185 MB, search 150 MB, notification 105 MB, comment 85 MB.

- Layout A uses `-XX:MaxRAMPercentage=70 -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xss512k`
  in a 512 MB machine per service.
- Layout B (`deploy/bundle`) gives each JVM an explicit heap (`HEAP_*`), caps metaspace and code cache,
  and runs everything in one machine. All six passed 200 req/s with 0% errors in **1 GiB**. Use
  **2 GB** for headroom.
- If you split into two bundles, co-deploy **gateway + user + post** (the hot, always-on path, about
  700 MB, so a 1 GB machine) and **comment + search + notification** (bursty, about 350 MB, a 512 MB
  machine that can auto-stop). Set `SERVICES` and point the `*_SERVICE_URL` variables at the other
  machine's Flycast address.

---

## 0. Prerequisites

- Accounts: GitHub (repo), [Vercel](https://vercel.com), [Fly.io](https://fly.io) (add a card),
  [Neon](https://neon.tech), [Upstash](https://upstash.com).
- CLIs: `flyctl` (`brew install flyctl` or `curl -L https://fly.io/install.sh | sh`), then `fly auth login`.
  Optional: `psql`, `jq` (for the smoke test).
- **Pick one region for everything** (latency between services and the DB matters most). Example:
  Fly `fra`, Neon `aws-eu-central-1` (Frankfurt), Upstash `eu-central-1`, Vercel functions `fra1`.

## 1. Generate secrets

```bash
export JWT_SECRET="$(openssl rand -base64 48)"         # shared by all services + gateway
export INTERNAL_API_TOKEN="$(openssl rand -hex 32)"    # service-to-service calls
```

Keep them in a password manager. Rotating `JWT_SECRET` signs every user out. Rotating
`INTERNAL_API_TOKEN` requires redeploying all services together.

## 2. Database: Neon

1. Create a project in your region. Note the role (e.g. `neondb_owner`) and its password.
2. Create the four databases (Neon console → Databases → New, or SQL editor on the default DB):
   ```sql
   CREATE DATABASE userdb;
   CREATE DATABASE postdb;
   CREATE DATABASE commentdb;
   CREATE DATABASE notificationdb;
   ```
3. Copy the **direct** (non-pooled) host, e.g. `ep-cool-name-123456.eu-central-1.aws.neon.tech`.
   Don't use the `-pooler` host: Flyway's migration lock and server-side prepared statements need a
   session-mode connection, and the services already pool connections (Hikari).
4. Build the JDBC URLs (user and password go in separate variables):
   ```bash
   export DB_USERNAME=neondb_owner
   export DB_PASSWORD='...'
   H=ep-cool-name-123456.eu-central-1.aws.neon.tech
   export USER_DB_URL="jdbc:postgresql://$H/userdb?sslmode=require"
   export POST_DB_URL="jdbc:postgresql://$H/postdb?sslmode=require"
   export COMMENT_DB_URL="jdbc:postgresql://$H/commentdb?sslmode=require"
   export NOTIFICATION_DB_URL="jdbc:postgresql://$H/notificationdb?sslmode=require"
   ```
   Neon's free compute allows about 100 connections. Four services × `DB_POOL_SIZE` (5–10) fits.
   The `pg_trgm` extension is created by the migrations (it's allow-listed on Neon and Supabase).

## 3. Redis: Upstash

Create a Redis database in the same region with TLS enabled, then copy the `rediss://` URL:

```bash
export REDIS_URL='rediss://default:<password>@<name>.upstash.io:6379'
```

Redis holds rate-limit buckets and caches, and nothing that has to be durable. If Redis is down or a
quota is exceeded, caching falls back to Postgres and the rate limiter fails open, so the site keeps
working. Watch the command count on the free tier: every API request costs about one Redis call for
rate limiting.

## 4. Backend: Fly.io

### Layout A: one app per service (recommended)

```bash
export FLY_PREFIX=stacktrace-<yourname>     # must be globally unique on Fly
export FLY_REGION=fra
export CORS_ALLOWED_ORIGINS=https://<your-project>.vercel.app   # update after step 5 if needed
# optional image uploads:
# export CLOUDINARY_CLOUD_NAME=... CLOUDINARY_API_KEY=... CLOUDINARY_API_SECRET=...

./deploy/fly/deploy.sh
```

The script runs in dependency order: user → post → notification → comment → search → gateway. For each
app it:

1. creates the app `$FLY_PREFIX-<service>` if it doesn't exist;
2. gives internal services a **private** IPv6 (Flycast) and never a public one;
3. imports that service's secrets (`fly secrets import --stage`);
4. builds the service's Dockerfile on Fly's remote builder and deploys it.

On first boot each service runs its Flyway migrations (see §7). Then check:

```bash
curl https://$FLY_PREFIX-gateway.fly.dev/actuator/health/readiness     # {"status":"UP"}
./scripts/smoke-test.sh https://$FLY_PREFIX-gateway.fly.dev            # 34 checks
```

Redeploy a single service with `./deploy/fly/deploy.sh post-service`. Logs:
`fly logs -a $FLY_PREFIX-post-service`.

### Layout B: budget bundle (all services in one machine)

```bash
APP=$FLY_PREFIX-backend
sed -e "s/__PREFIX__/$FLY_PREFIX/g" -e "s/__REGION__/$FLY_REGION/g" deploy/fly/bundle.toml > /tmp/bundle.toml
fly apps create $APP
cat <<EOF | fly secrets import -a $APP --stage
JWT_SECRET=$JWT_SECRET
INTERNAL_API_TOKEN=$INTERNAL_API_TOKEN
DB_USERNAME=$DB_USERNAME
DB_PASSWORD=$DB_PASSWORD
USER_DB_URL=$USER_DB_URL
POST_DB_URL=$POST_DB_URL
COMMENT_DB_URL=$COMMENT_DB_URL
NOTIFICATION_DB_URL=$NOTIFICATION_DB_URL
REDIS_URL=$REDIS_URL
CORS_ALLOWED_ORIGINS=$CORS_ALLOWED_ORIGINS
EOF
fly deploy . -c /tmp/bundle.toml --remote-only --ha=false      # build context = repo root
./scripts/smoke-test.sh https://$APP.fly.dev
```

The API base URL is then `https://$FLY_PREFIX-backend.fly.dev`. Only the gateway port (8080) is exposed.
The other services listen on localhost inside the machine.

## 5. Frontend: Vercel

1. Vercel → **Add New Project** → import the GitHub repo.
2. **Root Directory: `frontend`**. The framework preset (Next.js) and build command are detected
   automatically. `output: 'standalone'` is ignored on Vercel.
3. Environment variables (Production, and Preview if you want previews to work):

   | Name | Value |
   |---|---|
   | `API_BASE_URL` | `https://<FLY_PREFIX>-gateway.fly.dev` (or your API domain) |
   | `NEXT_PUBLIC_SITE_URL` | `https://<your-project>.vercel.app` (or your domain) |

   Leave `COOKIE_SECURE` unset: cookies are `Secure` in production.
4. Settings → Functions → **Function Region**: pick the region nearest your Fly region (e.g. `fra1`).
5. Deploy. Open the site, register, write a post.
6. Browser-level check from your machine:
   `cd e2e && npm ci && BASE=https://<your-project>.vercel.app npm test`.

The browser only talks to Vercel. The Next.js server calls the gateway, so the gateway needs no
public CORS for the app to work. `CORS_ALLOWED_ORIGINS` is still enforced for any direct browser
access and must be the exact frontend origin.

## 6. Custom domains and CORS

1. Frontend: Vercel → Project → Domains → add `stacktrace.example.com` and follow the DNS instructions.
2. API (optional, the `fly.dev` URL works fine):
   `fly certs add api.stacktrace.example.com -a $FLY_PREFIX-gateway`, then add the CNAME/A records
   it prints.
3. Update and redeploy:
   ```bash
   fly secrets set CORS_ALLOWED_ORIGINS=https://stacktrace.example.com -a $FLY_PREFIX-gateway
   ```
   On Vercel set `API_BASE_URL=https://api.stacktrace.example.com` and
   `NEXT_PUBLIC_SITE_URL=https://stacktrace.example.com`, then redeploy.

Multiple origins (e.g. a preview domain) are comma-separated. Wildcards are deliberately not supported.

## 7. Database migrations

- Every service owns its schema and migrates **itself on startup** with Flyway
  (`backend/<service>/src/main/resources/db/migration/V*.sql`). Hibernate only validates
  (`ddl-auto=validate`): the app refuses to start if the schema and entities disagree.
- Inspect the state: `select version, description, success from flyway_schema_history order by installed_rank;`
  in each database.
- Add a change: create the next `V<n>__description.sql`. **Never edit a migration that has been
  applied anywhere.** Flyway checks checksums and the service will refuse to start.
- Several instances starting at once are safe: Flyway takes a Postgres advisory lock.
- Big index on a large table: write it as `CREATE INDEX CONCURRENTLY` in its own migration
  `V<n>__name.sql`, with a sibling file `V<n>__name.sql.conf` containing `executeInTransaction=false`.

## 8. Rollback

| What | How |
|---|---|
| A backend service | `fly releases -a <app> --image` lists previous image refs, then `fly deploy -a <app> --image <ref> -c <rendered toml>`. Or `git checkout <good-commit> && ./deploy/fly/deploy.sh <service>`. |
| Frontend | Vercel → Deployments → pick the last good one → **Instant Rollback** (or "Promote to Production"). |
| Bad migration | Flyway Community has no "undo". Ship a new forward migration that reverses it. For data damage, use Neon **point-in-time restore**: create a branch at a timestamp before the incident, verify, then restore or repoint `*_DB_URL`. |
| Secrets | `fly secrets set NAME=old-value -a <app>` (triggers a restart). |

Keep schema changes backward compatible with the previous release (add columns before using them,
drop them one release later) so app rollbacks never need a DB rollback.

## 9. CI/CD

`.github/workflows/ci.yml` runs on every push and PR:

- `backend`: `mvn verify` per service (unit and Testcontainers integration tests);
- `frontend`: lint, type-check, production build;
- `e2e`: `docker compose up --wait`, the curl smoke test and the headless-Chrome UI test;
- on `main` only: builds and pushes all seven images to `ghcr.io/<owner>/stacktrace-<name>:{latest,sha}`.

To auto-deploy after a green `main`, add a `FLY_API_TOKEN` repo secret (`fly tokens create deploy`)
and a job that runs `./deploy/fly/deploy.sh` with the variables from §2–§4 as secrets. Vercel deploys
the frontend from GitHub automatically.

## 10. Environment variables

Every service also has a commented `.env.example` next to its code.

| Variable | gateway | user | post | comment | search | notification | frontend |
|---|---|---|---|---|---|---|---|
| `PORT` | 8080 | 8081 | 8082 | 8083 | 8084 | 8085 | 3000 |
| `SPRING_PROFILES_ACTIVE=prod` (JSON logs, no API docs) | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | |
| `JWT_SECRET` (≥ 32 bytes, same everywhere) | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | |
| `INTERNAL_API_TOKEN` (≥ 16 chars, same everywhere) | | ✓ | ✓ | ✓ | ✓ | ✓ | |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | | ✓ | ✓ | ✓ | | ✓ | |
| `DB_POOL_SIZE` (default 10) | | ✓ | ✓ | ✓ | | ✓ | |
| `REDIS_URL` | ✓ | ✓ | ✓ | | | | |
| `CACHE_TYPE` (`redis` / `none`) | | ✓ | ✓ | | | | |
| `USER_SERVICE_URL` | ✓ | | ✓ | ✓ | ✓ | ✓ | |
| `POST_SERVICE_URL` | ✓ | | | ✓ | ✓ | | |
| `COMMENT_SERVICE_URL`, `SEARCH_SERVICE_URL` | ✓ | | | | | | |
| `NOTIFICATION_SERVICE_URL` | ✓ | ✓ | | ✓ | | | |
| `CORS_ALLOWED_ORIGINS` | ✓ | | | | | | |
| `RATE_LIMIT_PER_SECOND` / `_BURST` (20 / 40) | ✓ | | | | | | |
| `AUTH_RATE_LIMIT_PER_SECOND` / `_BURST` (1 / 10) | ✓ | | | | | | |
| `JWT_ACCESS_TTL_MS` / `JWT_REFRESH_TTL_MS` | | ✓ | | | | | |
| `CLOUDINARY_CLOUD_NAME` / `_API_KEY` / `_API_SECRET` (optional) | | | ✓ | | | | |
| `JAVA_OPTS` (JVM flags, preset in images) | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | |
| `API_BASE_URL` (server-only) | | | | | | | ✓ |
| `NEXT_PUBLIC_SITE_URL` | | | | | | | ✓ |
| `COOKIE_SECURE` (`false` only for plain-http local runs) | | | | | | | ✓ |

The bundle image (`deploy/bundle`) uses `USER_DB_URL`, `POST_DB_URL`, `COMMENT_DB_URL`,
`NOTIFICATION_DB_URL` instead of `DB_URL`, plus `SERVICES`, `GATEWAY_PORT` and `HEAP_<SERVICE>`.

## 11. Troubleshooting

| Symptom | Cause / fix |
|---|---|
| Service crash-loops with `JWT_SECRET must be at least 32 bytes` | The secret is missing or short; set it with `fly secrets set`. |
| `Validate failed: Migrations have failed validation` | Someone edited an applied migration; restore the file and add a new one instead. |
| Gateway returns 503 / 504 | A service is stopped or starting (the first request after scale-to-zero takes a few seconds). Check `fly status -a <app>` and the readiness endpoint. |
| Everything 401s after login | `JWT_SECRET` differs between the user service and another service. |
| Frontend shows "We couldn't load this right now" | `API_BASE_URL` is wrong or the gateway is down; check Vercel function logs. |
| Login loops on plain http (local) | Set `COOKIE_SECURE=false` (only for http). |
| Many 429s | The rate limit is per user or IP: raise `RATE_LIMIT_*` on the gateway. |
| Upload returns 503 | Cloudinary variables aren't set (uploads are optional). |
