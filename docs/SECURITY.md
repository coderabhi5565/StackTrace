# Security

## ⚠️ Action required: rotate the leaked Cloudinary key

Before this work, `post-service/src/main/resources/application.properties` committed a real-looking
Cloudinary `api-key`/`api-secret` (and a JWT secret and DB password). The values are gone from the code
now, but **they remain in git history**, which was not rewritten. Rotate the Cloudinary API secret in the
Cloudinary console, and never reuse the old JWT secret.

## Measures in place

| Area | Implementation |
|---|---|
| Secrets | Environment variables only; `JWT_SECRET`/`INTERNAL_API_TOKEN` have no defaults and services refuse to start without them (or if too short). `.env` files are git-ignored; `.env.example` documents every variable. |
| Passwords | BCrypt (cost 12). Login runs a dummy hash for unknown emails, so timing and messages do not reveal which emails exist. |
| Tokens | HS256 access token, 15 min, with `iss` and `typ` claims checked by every service. Opaque refresh token, 7 days, stored only as SHA-256, rotated on every use; reusing a rotated token revokes all of that user's sessions. Logout revokes server-side. |
| Identity | User ids always come from the verified token (`principal`), never from request bodies or query strings. Ownership is enforced server-side for posts, comments, skills and notifications. |
| Service-to-service | `/internal/**` requires `X-Internal-Token` (constant-time compare). The gateway never routes `/internal/**` and strips that header from client requests. |
| Browser auth | The Next.js BFF keeps tokens in `httpOnly`, `Secure`, `SameSite=Lax` cookies, so JS never sees them. Writes through the BFF must be same-origin. `?next=` redirects only allow relative paths. |
| CORS | Gateway only, exact origins from `CORS_ALLOWED_ORIGINS`. |
| Rate limiting | Redis token buckets at the gateway: per user (verified JWT) or per IP; `/api/auth/**` has a separate strict per-IP bucket (default 1/s, burst 10). |
| XSS | Markdown is rendered with `rehype-sanitize` (GitHub schema: no raw HTML, scripts, event handlers or `javascript:` URLs). Comments render as plain text. Strict CSP, `X-Frame-Options: DENY`, `nosniff`, HSTS. |
| Input | Bean Validation on every request DTO; page sizes capped at 50; upload type and size checks. |
| Errors | One JSON error shape; no stack traces or internal messages for 5xx. |
| Operations | Actuator exposes only `health` and `info`; OpenAPI/Swagger disabled under `prod`; JSON logs. |

## Known limitations

- Client IP for rate limiting comes from the first `X-Forwarded-For` hop. That's correct behind
  Vercel/Fly proxies, but a client calling the gateway directly can spoof it. Keep the gateway behind the
  platform proxy.
- No email verification or password reset yet (see Future Work).
