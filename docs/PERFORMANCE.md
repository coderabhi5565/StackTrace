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
