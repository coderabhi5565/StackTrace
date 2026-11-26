# StackTrace — Audit (Step 0)

Date: 2026-11-26. Baseline commit: `1529163`. Toolchain used: JDK 21.0.13, Maven 3.9, Node 22, Docker 29, PostgreSQL 16.

The README overstates progress. **None of the five backend services started**, and the frontend
ran entirely on mock data. Every finding below comes from building or running the code, not from reading the README.

## 1. Real completion state per module

| Module | Compiles | Starts | Real state |
|---|---|---|---|
| User-Service | ✅ | ❌ `SecurityConfig` needs a `JwtAuthFilter` bean that does not exist (class is not a `@Component`, and its field is non-final, so it cannot be injected) | ~60%. Register, login, profile, follow, skills, badges and leaderboard logic exists, but the service cannot boot. No `ddl-auto`, so no tables would be created. |
| post-service | ❌ POM is invalid: `spring-boot-starter-security` declared twice | ❌ | ~45%. Also: `TagRepository extends JpaRepository<Long, Tag>` (generic args swapped), `UploadServiceImpl` is missing its Cloudinary imports, and there are two `SecurityConfig` classes with the same bean name. Internal endpoints are mapped to `/internal/posts/internal/posts/...` (prefix doubled). |
| comment-service | ❌ uses jjwt but does not declare it | ❌ no datasource, port or `jwt.secret` config | ~40%. On Spring Boot **4.1.0**, while every other service is on 3.5.x. Flat lists only: no nesting tree and no pagination. |
| search-service | ✅ | ❌ no `jwt.secret` | ~20%. Fans out to endpoints that don't exist (wrong paths). DTO fields don't match (`fullName`/`profilePicture` vs `name`/`avatarUrl`). The permit rule is for `/api/search/**`, but the controller is on `/api/v1/search`. |
| NotificationService | ✅ | ❌ no datasource or `jwt.secret` | ~40%. Every endpoint trusts `recipientId` from the query string (IDOR). Its SecurityConfig was copy-pasted from comment-service. |
| API gateway | — | — | Missing. |
| frontend | ✅ (`ignoreBuildErrors: true` hides 5 TS errors) | ✅ | ~35% (UI only). **100% mock data** (`lib/mock-data.ts`). No API client, no auth, no route protection. `eslint` is not installed, so `npm run lint` fails. Login and register just `router.push('/home')`. |
| Docker / CI / docs | — | — | Missing. |

## 2. Bug list

### User-Service
1. `JwtAuthFilter` is not a bean, and `private JwtService jwt` is non-final, so `@RequiredArgsConstructor` never injects it. **App fails to start.**
2. `JwtAuthFilter` has no `return` after `filterChain.doFilter` when the header is missing. That gives an NPE on `token.substring`, and the chain runs twice.
3. `getFollowing(id, size, page)`: page and size are passed in the wrong order.
4. `getUser` calls `findById(id).get()`, so an unknown id throws `NoSuchElementException` and returns 500, not 404.
5. `login` throws a bare `RuntimeException`, so bad credentials return 500, not 401. It also does two DB lookups.
6. `register` doesn't check for a duplicate username, so it hits a DB unique-constraint error and returns 500. It also BCrypt-hashes the password twice and throws one hash away (~100 ms wasted).
7. `UpdateProfileRequest` is not `@Valid` in the controller, so its `@Size` limits are never enforced.
8. Leaderboard entries have no `id` or `avatarUrl`, so the frontend can't link to profiles.
9. The `/api/users/internal/users/search` path doesn't match what search-service calls (`/internal/users/search`).
10. `testng` is declared with `compile` scope at version `RELEASE`, so a test framework ships in the production jar.
11. `java.version` is 17 (the target is 21).

### post-service
1. The POM is invalid (duplicate dependency), so the module doesn't build.
2. `TagRepository` has its generic type arguments swapped.
3. `UploadServiceImpl` is missing its imports.
4. There are two `@Configuration SecurityConfig` beans with the same name, which causes a bean-definition conflict.
5. Internal controller paths are doubled, so comment-service and search-service calls return 404.
6. `/internal/**` requires a user JWT. Service-to-service calls carry none, so they get 401/403.
7. `getPostBySlug` returns drafts and soft-deleted posts to anyone.
8. `createPost` throws a bare `RuntimeException` for a missing tag, which returns 500.
9. Missing exception handlers for `PostNotFoundException`, `IllegalStateException`, `IllegalArgumentException` and `PostAlreadyPublishedException` mean they all return 500.
10. There's no way to create tags, and the tags table starts empty, so **no post can ever be created**.
11. `excerpt` is never set. `Post` has both `scheduledAt` and `scheduledPublishAt`; the response reads the one that is never written.
12. Published posts can't be edited (`checkDraft` blocks every update).
13. The search query does `LOWER(content) LIKE '%x%'`, a full table scan with no limit.
14. `show-sql=true` in the default config.

### comment-service
1. Doesn't compile (jjwt is missing).
2. Spring Boot 4.1 is mixed with 3.5 in the other services, so dependencies and conventions diverge.
3. No `application.properties` for the datasource, port or JWT.
4. Comment creation calls notification-service synchronously, so if that service is down, **commenting fails**.
5. `getCommentsByPost` makes a remote `exists` call on every read.
6. Replies to a soft-deleted parent are orphaned and hidden.
7. `CreateCommentRequest.postId` has no `@NotNull`.

### search-service
1. Wrong downstream paths, mismatched DTO fields and an unreachable permit rule (see the table).
2. Downstream calls run sequentially with no timeouts.

### NotificationService
1. IDOR: any authenticated user can read, mark or delete anyone's notifications by passing `recipientId` or `id`.
2. No DB config.
3. `/internal/notifications` is open to the world (permitAll, with no shared secret).

## 3. Frontend ↔ backend contract mismatches

| Frontend expects | Backend provides |
|---|---|
| `Article { author{name,username,avatar}, description, readingTime, views, likes, bookmarked }` | `PostResponse { authorId, content, tags }` with no author info, excerpt, counts or reading time |
| Tags as names (`'java'`) | `tagIds: Set<Long>` and no tag listing endpoint |
| Profile routes `/profile/{username}` | Lookup by numeric id only |
| Follower and following counts, points, rank, badges, skills on the profile | Counts and rank missing; badges and skills are separate endpoints |
| Leaderboard with `author{id,avatar}`, articles, badges | `{rank, username, name, points}` |
| Comment author name and avatar, nested replies | `authorId` only, flat lists |
| Likes, bookmarks, trending, following feed | Not implemented |
| Search with tag filter and sort | Not implemented |
| Body as structured blocks | Raw markdown string |

## 4. Security issues

1. **Secrets committed to git:** the Cloudinary `api-secret`, the JWT secret, and the DB password are in `post-service/application.properties` and the other services. **The Cloudinary key must be rotated.** It stays in git history; per the rules, history is not rewritten.
2. Any user can give themselves unlimited points with `PUT /api/users/points`.
3. Any user can award themselves any badge with `POST /api/badges/users/{id}/{badgeId}`.
4. Notification IDOR (see above).
5. Internal endpoints have no service-to-service authentication.
6. JWT lifetime is 24h, with no refresh flow and no revocation.
7. No CORS config, no rate limiting, and no brute-force protection on login.
8. Login error messages and timing differ between "no such email" and "wrong password", which allows user enumeration.
9. Actuator and Swagger are `permitAll` in post-service.
10. The frontend uses no sanitization layer (once real markdown is rendered, it needs one).

## 5. Performance risks

1. No indexes on `follows(following_id)`, `skills(user_id)`, `comments(post_id)` or `comments(parent_comment_id)`. There is no search index (LIKE `%x%` over `TEXT`).
2. N+1 queries: the feed mapping touches lazy `post.tags` per post, and `user_badges → badge` is lazy per row.
3. Page size is uncapped everywhere (`?size=100000` works).
4. No caching. Every request hits the DB.
5. Synchronous cross-service calls with no timeouts.
6. `open-in-view` is on by default in most services.
7. Frontend: every page is a client component, `images.unoptimized: true`, and no code-splitting for the editor.

## 6. What is mocked

The whole frontend: feed, post detail, comments, profile, leaderboard, search, notifications, login and register.
The code "Run" button, the AI summary, the quiz and the "flag outdated" button are UI-only fakes. They are out of scope,
listed under Future Work, and removed from the UI.
