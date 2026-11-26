#!/usr/bin/env bash
# Starts the services listed in $SERVICES as separate JVMs with fixed heap budgets.
# If any of them exits, everything is stopped so the platform restarts the container.
set -uo pipefail

# Lean JVM settings for small VMs: serial GC, C1 only, small stacks/metaspace/code cache.
COMMON="-XX:+UseSerialGC -XX:TieredStopAtLevel=1 -XX:CICompilerCount=1 -Xss512k \
 -XX:MaxMetaspaceSize=110m -XX:ReservedCodeCacheSize=40m -XX:MaxDirectMemorySize=32m \
 -XX:+ExitOnOutOfMemoryError ${EXTRA_JAVA_OPTS:-}"

declare -A PORT=( [user]=8081 [post]=8082 [comment]=8083 [search]=8084 [notification]=8085 [gateway]=${GATEWAY_PORT:-8080} )
declare -A HEAP=( [user]=${HEAP_USER:-112m} [post]=${HEAP_POST:-128m} [comment]=${HEAP_COMMENT:-96m}
                  [search]=${HEAP_SEARCH:-64m} [notification]=${HEAP_NOTIFICATION:-80m} [gateway]=${HEAP_GATEWAY:-96m} )
declare -A DB=( [user]=${USER_DB_URL:-} [post]=${POST_DB_URL:-} [comment]=${COMMENT_DB_URL:-} [notification]=${NOTIFICATION_DB_URL:-} )

# Services in the same container talk over localhost unless overridden (e.g. a split deployment).
export USER_SERVICE_URL=${USER_SERVICE_URL:-http://localhost:8081}
export POST_SERVICE_URL=${POST_SERVICE_URL:-http://localhost:8082}
export COMMENT_SERVICE_URL=${COMMENT_SERVICE_URL:-http://localhost:8083}
export SEARCH_SERVICE_URL=${SEARCH_SERVICE_URL:-http://localhost:8084}
export NOTIFICATION_SERVICE_URL=${NOTIFICATION_SERVICE_URL:-http://localhost:8085}

pids=()
for s in $SERVICES; do
  [[ -n "${PORT[$s]:-}" ]] || { echo "unknown service '$s'"; exit 2; }
  echo "starting $s on :${PORT[$s]} (heap ${HEAP[$s]})"
  PORT=${PORT[$s]} DB_URL=${DB[$s]:-} java $COMMON -Xmx${HEAP[$s]} -jar "/app/$s.jar" &
  pids+=($!)
done

trap 'kill "${pids[@]}" 2>/dev/null; wait; exit 143' TERM INT
wait -n
code=$?
echo "a service exited (code $code); stopping the bundle"
kill "${pids[@]}" 2>/dev/null
wait
exit 1
