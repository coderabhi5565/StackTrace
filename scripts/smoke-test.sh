#!/usr/bin/env bash
# End-to-end smoke test against any StackTrace deployment (talks to the API gateway).
#
#   ./scripts/smoke-test.sh                      # http://localhost:8080
#   ./scripts/smoke-test.sh https://api.example.com
#
# Registers two users, logs in, publishes a post, comments + replies, likes, bookmarks,
# follows, searches, checks notifications and the refresh/logout flow.
# Requires: curl, jq. Exit code 0 = all checks passed.
set -euo pipefail

BASE="${1:-${BASE_URL:-http://localhost:8080}}"
BASE="${BASE%/}"
command -v jq >/dev/null || { echo "jq is required (brew install jq / apt install jq)"; exit 2; }

PASS=0
RUN_ID="$(date +%s)$RANDOM"
WORD="zebrafish${RUN_ID}"   # unique search term for this run

green() { printf '\033[32m%s\033[0m\n' "$*"; }
red() { printf '\033[31m%s\033[0m\n' "$*"; }
ok() { PASS=$((PASS + 1)); green "  ✓ $*"; }
fail() { red "  ✗ $*"; exit 1; }

# call METHOD PATH [TOKEN] [JSON] -> sets $STATUS and $BODY
call() {
  local method="$1" path="$2" token="${3:-}" data="${4:-}"
  local args=(-sS -o /tmp/smoke_body.$$ -w '%{http_code}' -X "$method" "$BASE$path" -H 'Accept: application/json')
  [[ -n "$token" ]] && args+=(-H "Authorization: Bearer $token")
  [[ -n "$data" ]] && args+=(-H 'Content-Type: application/json' --data "$data")
  STATUS="$(curl "${args[@]}")" || fail "$method $path: connection failed"
  BODY="$(cat /tmp/smoke_body.$$)"
}
expect() { # expect STATUS description
  [[ "$STATUS" == "$1" ]] || fail "$2 (expected HTTP $1, got $STATUS: ${BODY:0:300})"
  ok "$2"
}
trap 'rm -f /tmp/smoke_body.$$' EXIT

echo "StackTrace smoke test against $BASE"

call GET /api/posts
expect 200 "gateway reachable, public feed readable"

for u in a b; do
  call POST /api/auth/register "" "{\"name\":\"Smoke $(echo "$u" | tr a-z A-Z)\",\"username\":\"smoke_${u}_${RUN_ID: -8}\",\"email\":\"smoke_${u}_${RUN_ID}@example.com\",\"password\":\"Sm0ke-test-pass\"}"
  expect 201 "register user $u"
  call POST /api/auth/login "" "{\"email\":\"smoke_${u}_${RUN_ID}@example.com\",\"password\":\"Sm0ke-test-pass\"}"
  expect 200 "login user $u"
  declare "TOKEN_${u}=$(jq -r .token <<<"$BODY")"
  declare "REFRESH_${u}=$(jq -r .refreshToken <<<"$BODY")"
  declare "ID_${u}=$(jq -r .id <<<"$BODY")"
done

call POST /api/auth/login "" "{\"email\":\"smoke_a_${RUN_ID}@example.com\",\"password\":\"wrong-password\"}"
expect 401 "wrong password rejected"
call POST /api/posts "" '{"title":"x"}'
expect 401 "unauthenticated write rejected"
call GET /internal/users/batch?ids=1
[[ "$STATUS" == "404" || "$STATUS" == "403" ]] || fail "internal API must not be exposed (got $STATUS)"; ok "internal API not exposed"

call POST /api/posts "$TOKEN_a" "{\"title\":\"Smoke test ${WORD}\",\"content\":\"# Hello\\n\\nThis post mentions ${WORD} and **markdown**.\\n\\n\`\`\`js\\nconsole.log(1)\\n\`\`\`\",\"tags\":[\"smoketest\",\"java\"],\"publish\":true}"
expect 201 "user a publishes a post"
POST_ID="$(jq -r .id <<<"$BODY")"; SLUG="$(jq -r .slug <<<"$BODY")"

call GET "/api/posts/$SLUG"
expect 200 "post detail by slug"
[[ "$(jq -r .author.id <<<"$BODY")" == "$ID_a" ]] || fail "post author not resolved"; ok "author resolved from user-service"

call PUT "/api/posts/$POST_ID" "$TOKEN_b" '{"title":"hijack","content":"x","tags":["x"]}'
expect 403 "only the author can edit"

call POST /api/comments "$TOKEN_b" "{\"postId\":$POST_ID,\"content\":\"Great post!\"}"
expect 201 "user b comments"
COMMENT_ID="$(jq -r .id <<<"$BODY")"
call POST /api/comments "$TOKEN_a" "{\"postId\":$POST_ID,\"parentCommentId\":$COMMENT_ID,\"content\":\"Thanks!\"}"
expect 201 "user a replies (nested)"
call GET "/api/comments/post/$POST_ID"
expect 200 "comment thread readable"
[[ "$(jq -r '.content[0].replies[0].content' <<<"$BODY")" == "Thanks!" ]] || fail "reply not nested"; ok "reply nested under comment"

call POST "/api/posts/$POST_ID/like" "$TOKEN_b"
expect 200 "user b likes the post"
call POST "/api/posts/$POST_ID/bookmark" "$TOKEN_b"
expect 200 "user b bookmarks the post"
call GET /api/posts/bookmarks "$TOKEN_b"
[[ "$(jq -r '.content[0].id' <<<"$BODY")" == "$POST_ID" ]] || fail "bookmark not listed"; ok "bookmark listed"

call POST "/api/users/$ID_a/follow" "$TOKEN_b"
expect 200 "user b follows user a"
call GET /api/posts/feed/following "$TOKEN_b"
[[ "$(jq -r '.content[0].id' <<<"$BODY")" == "$POST_ID" ]] || fail "following feed missing post"; ok "following feed shows the post"

# comment counter / notifications are updated asynchronously
sleep 2
call GET "/api/posts/$POST_ID/engagement" "$TOKEN_b"
[[ "$(jq -r .likeCount <<<"$BODY")" == "1" && "$(jq -r .liked <<<"$BODY")" == "true" ]] || fail "like not counted: $BODY"; ok "like counted"
[[ "$(jq -r .commentCount <<<"$BODY")" -ge 1 ]] || fail "comment count not synced: $BODY"; ok "comment count synced to post-service"

call GET "/api/search?keyword=${WORD}"
expect 200 "search"
[[ "$(jq -r '.posts.content[0].id' <<<"$BODY")" == "$POST_ID" ]] || fail "search did not find the post: ${BODY:0:300}"; ok "full-text search finds the post"
call GET "/api/search?keyword=smoke_a_${RUN_ID: -8}&type=USER"
[[ "$(jq -r '.users.totalElements' <<<"$BODY")" -ge 1 ]] || fail "user search failed"; ok "user search finds user a"

call GET /api/notifications "$TOKEN_a"
expect 200 "notifications for user a"
[[ "$(jq -r '.totalElements' <<<"$BODY")" -ge 2 ]] || fail "expected comment + follow notifications: ${BODY:0:300}"; ok "comment and follow notifications delivered"

call GET "/api/users/username/smoke_a_${RUN_ID: -8}"
expect 200 "profile by username"
[[ "$(jq -r .followersCount <<<"$BODY")" == "1" ]] || fail "follower count wrong"; ok "follower count"

call GET /api/users/leaderboard
expect 200 "leaderboard"

call POST /api/auth/refresh "" "{\"refreshToken\":\"$REFRESH_a\"}"
expect 200 "refresh token rotation"
NEW_REFRESH="$(jq -r .refreshToken <<<"$BODY")"
call POST /api/auth/refresh "" "{\"refreshToken\":\"$REFRESH_a\"}"
expect 401 "reused refresh token rejected"
call POST /api/auth/logout "" "{\"refreshToken\":\"$NEW_REFRESH\"}"
expect 204 "logout"

green "All $PASS checks passed."
