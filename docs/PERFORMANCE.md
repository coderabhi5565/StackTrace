# Performance

## Method

- Data: `loadtest/seed.sh` loads 500 users, ~5k follows and 20,000 published posts across 30 tags.
- Load: `loadtest/k6-read.js`, constant arrival rate, 60 s. Mix: 45% feed page, 40% post detail
  (80% of reads hit 50 hot posts), 15% search. Requests go through the API gateway, spread over many
  client IPs so the per-IP rate limiter behaves as it would in production.
- Hardware: one Apple-silicon laptop running all six JVMs (`-Xmx256m` each), Postgres 16 and Redis 7.
  Absolute numbers are optimistic compared with a small cloud VM; compare **before vs after** only.

## Baseline (end of Phase 1: no extra indexes, no cache)

| Rate | feed p95 | post detail p95 | search p95 | errors |
|---|---|---|---|---|
| 100 req/s | 16 ms | 6 ms | 27 ms | 0% |
| 300 req/s | 14 ms | 3 ms | 26 ms | 0% |

EXPLAIN at baseline:

- Latest feed (`ORDER BY published_at DESC LIMIT 10`): **Seq Scan** on 20k posts plus a top-N sort, 5.5 ms.
- Search (`search_vector @@ … OR title ILIKE …`): **Seq Scan**, 11.9 ms. The cost grows linearly with the post count.

## After Phase 2 (indexes + Redis cache + virtual threads)

| Rate | feed p95 | post detail p95 | search p95 | errors | completed |
|---|---|---|---|---|---|
| 100 req/s | 3 ms | 8 ms | 33 ms | 0% | 6,000 / 6,000 |
| 300 req/s | 2 ms | 3 ms | 12 ms | 0% | 18,000 / 18,000 |
| 1000 req/s (30 s) | 2 ms | 3 ms | 13 ms | 0% | 30,000 / 30,000 |

Baseline, re-measured on the same data with the new indexes dropped and `CACHE_TYPE=none`:

| Rate | feed p95 | post detail p95 | search p95 | completed |
|---|---|---|---|---|
| 300 req/s (30 s) | 23 ms | 8 ms | 35 ms | 8,991 / 9,000 |
| 1000 req/s (30 s) | **363 ms** | **344 ms** | **383 ms** | **18,856 / 30,000** (saturated) |

At low load both builds are fast on this machine, and the millisecond differences at 100 req/s are noise
(cache warm-up). The difference shows under load: the baseline saturates around 1000 req/s, while the
optimized build serves it with p95 under 15 ms.

EXPLAIN after:

- Latest feed: **Index Scan** on `idx_posts_published_feed`, 0.06 ms (was a Seq Scan + sort, 5.5 ms).
- Tag feed: index scans on the feed index and `post_tag` PK, 0.5 ms.
- Search, selective term: **BitmapOr** over `idx_posts_search_vector` (GIN) and `idx_posts_title_trgm`
  (trigram GIN), 7 ms. For very common terms (about 10% of rows) Postgres correctly prefers a seq scan;
  search-service results are paged and capped at 50.
- User search uses the trigram indexes once the table is large enough to make them worthwhile
  (with 500 users, a seq scan is cheaper and the planner picks it).

## What changed

- Indexes: partial feed index, author/status, scheduler, GIN FTS, trigram title/username/name, follow
  recency, points, comment threads, notification recipient/unread (`V2/V3__performance_indexes.sql`).
- N+1: post tags are batch-fetched (one query per page); badges are fetch-joined; followers/following
  are a single join query; comment trees take 2 queries per page whatever their depth; authors come from
  one batched, per-id-cached call.
- Redis caches (TTL, evicted on writes): post by slug 5 min, latest feed 30 s, trending 2 min,
  tags 5 min, authors 10 min, profiles 5 min, leaderboard 60 s. A Redis outage only logs and falls back to Postgres.
- Every list endpoint is paginated and page size is capped at 50.
- `spring.threads.virtual.enabled=true`, Hikari pool limits (`DB_POOL_SIZE`, default 10), gzip at the gateway.
- Frontend: public pages are server components with ISR (landing 60 s, post 5 min with on-demand
  revalidation on edit); personal data is loaded by small client islands; the editor and markdown
  preview are code-split; independent requests run in parallel (`Promise.all`, no waterfalls);
  `next/link` prefetches in production.

## Frontend (Lighthouse 12, mobile emulation, production build)

| Page | Performance | Accessibility | Best practices | SEO |
|---|---|---|---|---|
| `/` | 92 → **96** | 100 | 96 → **100** | 100 |
| `/home` | 88 → **94** | 100 | 96 → **100** | 100 |
| `/post/[slug]` | 91 → **94** | 96 → **100** | 96 → **100** | 100 |
| `/leaderboard` | **91** | 100 | 100 | 100 |

Fixes from the first audit: Vercel Analytics now loads only on Vercel (it 404'd elsewhere), a real app
icon (favicon 404), a react-markdown `node` prop leaking into the DOM, `<p>` inside a `<ul>`, a modern
`browserslist` (no legacy polyfills), and `next/image` for cover images from allowed hosts.
