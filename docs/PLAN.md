# StackTrace — Plan

Ordered by priority. Each line maps to one or more focused commits.

## Cross-cutting decisions

- **Versions:** all services on Spring Boot 3.5.x / Java 21 / Spring Cloud 2025.0.x. comment-service moves down from Boot 4.1 because it doesn't build, and one version line keeps the services consistent.
- **Auth:** user-service issues a short-lived HS256 access token (15 min) plus a rotating refresh token (7 days, stored hashed). Other services validate the access token statelessly with `JWT_SECRET` from the environment. User ids always come from the token, never from a request body.
- **Internal calls:** `/internal/**` endpoints are never routed by the gateway and require an `X-Internal-Token` header (shared `INTERNAL_API_TOKEN` env var).
- **Gateway:** Spring Cloud Gateway is the single public entry point. It routes `/api/**` and handles CORS (frontend origin only) and Redis-backed rate limiting, with a stricter limit on `/api/auth/**`.
- **Frontend auth:** a BFF pattern. Next.js route handlers keep the access and refresh tokens in `httpOnly`, `Secure`, `SameSite=Lax` cookies. The browser only calls same-origin `/api/...` routes, which proxy to the gateway. Middleware protects private routes.
- **Search:** Postgres full-text search (`tsvector` + GIN) for posts, and `pg_trgm` GIN indexes for users. search-service aggregates both in parallel. No Elasticsearch.
- **Likes, bookmarks, trending:** live in post-service. comment-service reports comment-count deltas over an internal endpoint. Trending score = `(likes*2 + comments*3 + 1) / (hours_since_publish + 2)^1.5`.
- **Schema:** Flyway in every service, with `ddl-auto=validate`.

## Phase 1 — features

1. Root `.gitignore` and repo hygiene.
2. user-service: fix startup bugs, Flyway, refresh tokens, profile by username with counts, internal endpoints (batch users, following ids, points delta), lock down points and badges, trigram user search, consistent errors.
3. post-service: fix the build, Flyway, tags by name, excerpt and reading time, ownership checks, editing published posts, author enrichment, likes, bookmarks, trending, following feed, FTS search, internal endpoints.
4. comment-service: downgrade to Boot 3.5, config, Flyway, nested tree with pagination (`root_comment_id`), best-effort notifications and comment-count sync.
5. search-service: correct clients, internal token, parallel fan-out with timeouts, pagination.
6. notification-service: fix the IDOR, Flyway, pagination, unread count.
7. gateway (new module).
8. Frontend: API client and BFF, auth, middleware, every page wired, mock data removed.
9. Phase 1 verification: run everything, smoke flow, k6 baseline.

## Phase 2 — performance

Index migration, EXPLAIN checks, N+1 removal, page-size caps, Redis caching, Hikari/GZIP/virtual threads, a k6 rerun, and frontend server components, ISR, `next/image` and dynamic imports.

## Phase 3 — security and production readiness

Env-only secrets with `.env.example` files, BCrypt cost, refresh flow, strict CORS, auth rate limits, markdown sanitization, actuator health and readiness, JSON logs, OpenAPI disabled in prod.

## Phase 4 — deployment

Multi-stage Dockerfiles, root `docker-compose.yml`, GitHub Actions, Fly.io configs, `DEPLOY.md`, smoke test script, README.

## Future work (out of scope)

Kafka/event bus, code execution sandbox, AI summaries, quiz system, outdated-content detection, image uploads (Cloudinary stays wired but optional), email verification and password reset, OAuth (GitHub).
