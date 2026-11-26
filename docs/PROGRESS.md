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
| | Frontend wired to real API | 🚧 | |
