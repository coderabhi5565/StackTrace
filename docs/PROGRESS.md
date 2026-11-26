# Progress log

| Date | Item | Status | Verified by |
|---|---|---|---|
| 2026-11-26 | Step 0 audit (`docs/AUDIT.md`), plan (`docs/PLAN.md`) | ✅ | builds + boot attempts |
| 2026-11-26 | user-service: starts, Flyway, refresh tokens, profiles, follow, leaderboard, internal API | ✅ | Testcontainers tests, boot |
| 2026-11-26 | post-service: builds, CRUD, ownership, tags, likes, bookmarks, trending, FTS | ✅ | Testcontainers tests |
| 2026-11-26 | comment-service: Boot 3.5, nested threads, pagination, best-effort side effects | ✅ | Testcontainers tests |
| 2026-11-26 | search-service: parallel fan-out, degradation | ✅ | tests |
| 2026-11-26 | notification-service: IDOR fixed, Flyway, paging | ✅ | Testcontainers tests |
| 2026-11-26 | api-gateway: routing, CORS, Redis rate limiting | ✅ | tests over real HTTP |
| 2026-11-26 | Full backend through gateway | ✅ | `scripts/smoke-test.sh` — 34/34 checks |
| 2026-11-26 | Frontend wired to real API, mock data removed, BFF cookie auth | ✅ | tsc + eslint clean, `next build`, scripted journey through the BFF (register, publish, comment, like, bookmark, follow, search, refresh, logout) |
| 2026-11-26 | Phase 1 complete; k6 baseline recorded (`docs/PERFORMANCE.md`) | ✅ | k6 |
| 2026-11-26 | Phase 2: indexes (all services), Redis caching (posts, feed, trending, tags, authors, profiles, leaderboard), virtual threads, Hikari | ✅ | EXPLAIN, Testcontainers Redis test, smoke test 34/34 |
| 2026-11-26 | Phase 2: k6 before/after (`docs/PERFORMANCE.md`): baseline saturates at 1000 req/s (p95 ~350 ms), optimized p95 < 15 ms | ✅ | k6 |
| 2026-11-26 | Phase 2: frontend Lighthouse 91-96 perf, 100 a11y/best practices/SEO | ✅ | Lighthouse |
| 2026-11-26 | Phase 3: actuator liveness/readiness, ECS JSON logs + no API docs under `prod`, gateway and frontend security headers (CSP), `.env.example` everywhere, `docs/SECURITY.md` | ✅ | prod profile run: probes UP, `/v3/api-docs` 404, JSON logs; smoke 34/34; BFF journey |
