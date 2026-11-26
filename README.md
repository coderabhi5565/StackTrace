# 🚀 StackTrace

> A developer learning platform built as microservices: publish technical articles, follow developers,
> discuss in threaded comments, earn points and badges, and find anything with full-text search.

[![CI](https://github.com/coderabhi5565/StackTrace/actions/workflows/ci.yml/badge.svg)](https://github.com/coderabhi5565/StackTrace/actions/workflows/ci.yml)

## Status

Every module builds, passes its tests and runs end to end (`docker compose up --build`).

| Module | Status | What it does |
|---|---|---|
| **api-gateway** | ✅ | Single public entry point: routing, strict CORS, Redis rate limiting (stricter on auth), security headers |
| **user-service** | ✅ | Register/login, JWT access + rotating refresh tokens, profiles, follow, skills, points, threshold badges, leaderboard, trigram user search |
| **post-service** | ✅ | Markdown posts, drafts/publish/schedule, tags, ownership, likes, bookmarks, trending, following feed, Postgres full-text search |
| **comment-service** | ✅ | Nested comment threads with pagination, owner edit/delete, tombstones |
| **search-service** | ✅ | Parallel search over posts and users with graceful degradation |
| **notification-service** | ✅ | Comment, reply and follow notifications, unread count |
| **frontend** | ✅ | Next.js 16 app wired to the real API: feed, post, editor, profile, search, leaderboard, bookmarks, notifications |
| Docker / CI / deploy | ✅ | Multi-stage images, Compose, GitHub Actions (tests, e2e, GHCR), Fly.io + Vercel guide |

Verified with: unit and Testcontainers integration tests per service, a 34-check API smoke test,
a headless-Chrome UI test, k6 load tests and Lighthouse. See [`docs/PROGRESS.md`](docs/PROGRESS.md).

## Architecture

```
                 Browser
                    │  same-origin only (httpOnly session cookies)
                    ▼
        Next.js frontend + BFF (/api/proxy, /api/auth)
                    │  server-side, Bearer JWT
                    ▼
             API Gateway (Spring Cloud Gateway) ── Redis (rate limits)
                    │  /api/** only (/internal/** is never routed)
 ┌──────────┬───────┴──────┬──────────────┬─────────────────┐
 ▼          ▼              ▼              ▼                 ▼
User      Post          Comment        Search          Notification
Service   Service       Service        Service         Service
 │  ╲      │  ╲           │      (fans out to post      │
 │  Redis  │  Redis       │       + user, no DB)        │
 ▼ cache   ▼ cache        ▼                             ▼
userdb    postdb        commentdb                  notificationdb   (one Postgres, one DB per service)

Service-to-service calls use /internal/** endpoints protected by X-Internal-Token.
```

- **Auth**: user-service issues short-lived HS256 access tokens (15 min) and rotating refresh tokens.
  Every service validates tokens statelessly with the shared `JWT_SECRET`. User ids always come from the
  token, never from request bodies.
- **Data**: each service owns its database and migrates it with Flyway. Postgres full-text search
  (`tsvector` + GIN) and `pg_trgm` power search; there is no Elasticsearch.
- **Performance**: targeted indexes, Redis caching with TTLs and write eviction, virtual threads,
  capped pagination, ISR for public pages. At 1000 req/s, p95 went from ~350 ms (saturated) to under
  15 ms. Details in [`docs/PERFORMANCE.md`](docs/PERFORMANCE.md).
- **Security**: see [`docs/SECURITY.md`](docs/SECURITY.md).
  ⚠️ A Cloudinary secret was committed in early history; rotate it.

## Run it locally

Requirements: Docker (with Compose v2). About 4 GB of free RAM.

```bash
git clone https://github.com/coderabhi5565/StackTrace.git
cd StackTrace
docker compose up --build
```

Open **http://localhost:3000** (frontend) or call **http://localhost:8080** (API gateway). The first
build takes a few minutes (Maven and npm downloads); later builds are cached. Compose ships with
development-only secrets; copy `.env.example` to `.env` to set your own.

Verify the running stack:

```bash
./scripts/smoke-test.sh http://localhost:8080        # API: 34 end-to-end checks (needs curl + jq)
cd e2e && npm ci && BASE=http://localhost:3000 npm test   # browser test (needs Google Chrome)
```

Optional load test (needs [k6](https://k6.io)): `./loadtest/seed.sh && k6 run loadtest/k6-read.js`.

### Developing a single service

Each service is a standalone Maven project (Java 21). Tests use Testcontainers, so Docker must be running.

```bash
cd backend/post-service
mvn verify                         # Maven 3.9+
# run against local Postgres/Redis: see .env.example for the variables
```

Frontend:

```bash
cd frontend
cp .env.example .env.local         # API_BASE_URL=http://localhost:8080
npm ci && npm run dev              # lint: npm run lint, types: npx tsc --noEmit
```

## Project structure

```
backend/
  api-gateway/          Spring Cloud Gateway
  User-Service/         auth, profiles, follows, badges, leaderboard
  post-service/         posts, tags, likes, bookmarks, trending, FTS
  comment-service/      nested comments
  search-service/       aggregated search
  NotificationService/  notifications
frontend/               Next.js 16, React 19, TypeScript, Tailwind, shadcn/ui
deploy/                 Fly.io configs + budget bundle image
docker/                 Postgres init (one DB per service)
e2e/                    headless-Chrome UI test
loadtest/               k6 script + seed data
scripts/                smoke test
docs/                   AUDIT, PLAN, PROGRESS, PERFORMANCE, SECURITY, DEPLOY
```

## Deploying

Frontend on **Vercel**, backend on **Fly.io**, Postgres on **Neon**, Redis on **Upstash**. Follow
[`docs/DEPLOY.md`](docs/DEPLOY.md). It covers every environment variable, migrations, domains and CORS,
rollback, and a budget option that runs all backend services in a single machine.

## Tech stack

Java 21 · Spring Boot 3.5 · Spring Security · Spring Data JPA / Hibernate · Flyway · Spring Cloud
Gateway & OpenFeign · Redis · PostgreSQL 16 · Next.js 16 · React 19 · TypeScript · Tailwind CSS ·
shadcn/ui · Docker · GitHub Actions · Testcontainers · k6 · Playwright

## Future work

Out of scope for this iteration:

- Event bus (Kafka) for notifications, counters and search indexing instead of best-effort HTTP calls
- Sandboxed code execution for code blocks
- AI article summaries
- Quiz system
- Outdated-content detection / flagging
- Email verification, password reset, OAuth (GitHub) sign-in
- Prometheus/Grafana metrics and distributed tracing

## License

MIT
