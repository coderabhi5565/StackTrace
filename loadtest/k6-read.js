// Read-path load test: feed, post detail and search through the API gateway.
//   k6 run -e BASE_URL=http://localhost:8080 loadtest/k6-read.js
import http from 'k6/http'
import { check, group } from 'k6'
import { Trend } from 'k6/metrics'

const BASE = __ENV.BASE_URL || 'http://localhost:8080'
const feed = new Trend('latency_feed', true)
const detail = new Trend('latency_post_detail', true)
const search = new Trend('latency_search', true)

export const options = {
  scenarios: {
    reads: {
      executor: 'constant-arrival-rate',
      rate: Number(__ENV.RATE || 100), // requests per second (all three endpoints combined)
      timeUnit: '1s',
      duration: __ENV.DURATION || '60s',
      preAllocatedVUs: 50,
      maxVUs: 200,
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    latency_feed: ['p(95)<300'],
    latency_post_detail: ['p(95)<300'],
    latency_search: ['p(95)<500'],
  },
}

const TERMS = ['spring', 'kubernetes caching', 'redis', 'postgres indexing', 'react', 'observability latency']
// spread synthetic clients over many IPs so the per-IP gateway rate limiter is realistic
const ip = () => `10.${(__VU >> 8) & 255}.${__VU & 255}.${__ITER % 250}`

export default function () {
  const params = { headers: { 'X-Forwarded-For': ip() } }
  const r = Math.random()
  if (r < 0.45) {
    group('feed', () => {
      const res = http.get(`${BASE}/api/posts?page=${Math.floor(Math.random() * 5)}&size=10`, params)
      check(res, { 'feed 200': (x) => x.status === 200 })
      feed.add(res.timings.duration)
    })
  } else if (r < 0.85) {
    group('post detail', () => {
      // hot set: most readers hit popular posts
      const n = Math.random() < 0.8 ? 1 + Math.floor(Math.random() * 50) : 1 + Math.floor(Math.random() * 20000)
      const res = http.get(`${BASE}/api/posts/load-post-${n}`, params)
      check(res, { 'detail 200': (x) => x.status === 200 })
      detail.add(res.timings.duration)
    })
  } else {
    group('search', () => {
      const q = TERMS[Math.floor(Math.random() * TERMS.length)]
      const res = http.get(`${BASE}/api/search?keyword=${encodeURIComponent(q)}&size=10`, params)
      check(res, { 'search 200': (x) => x.status === 200 })
      search.add(res.timings.duration)
    })
  }
}

export function handleSummary(data) {
  const pick = (m) => (data.metrics[m] ? Math.round(data.metrics[m].values['p(95)']) : null)
  const summary = {
    when: new Date().toISOString(),
    label: __ENV.LABEL || 'run',
    rate: Number(__ENV.RATE || 100),
    p95_ms: { feed: pick('latency_feed'), post_detail: pick('latency_post_detail'), search: pick('latency_search') },
    failed_rate: data.metrics.http_req_failed?.values.rate,
    requests: data.metrics.http_reqs?.values.count,
  }
  return {
    stdout: JSON.stringify(summary, null, 2) + '\n',
    [`loadtest/results/${summary.label}.json`]: JSON.stringify(summary, null, 2),
  }
}
