#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"

USER1_EMAIL="u1@test.com"
USER1_PASS="Password123"
USER2_EMAIL="u2@test.com"
USER2_PASS="Password123"

register() {
  local email="$1"
  local pass="$2"
  curl -s -X POST "$BASE_URL/auth/register" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"$email\",\"password\":\"$pass\"}" >/dev/null || true
}

login_token() {
  local email="$1"
  local pass="$2"
  curl -s -X POST "$BASE_URL/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"$email\",\"password\":\"$pass\"}" | jq -r .accessToken
}

post_event() {
  local token="$1"
  local type="$2"
  local entityType="$3"
  local entityId="$4"
  local occurredAt="$5"

  curl -s -o /dev/null -w "%{http_code}\n" -X POST "$BASE_URL/events" \
    -H "Authorization: Bearer $token" \
    -H "Content-Type: application/json" \
    -d "{
      \"type\":\"$type\",
      \"entityType\":\"$entityType\",
      \"entityId\":\"$entityId\",
      \"occurredAt\":\"$occurredAt\",
      \"metadata\": {\"seed\": true}
    }"
}

echo "== Registering users (idempotent) =="
register "$USER1_EMAIL" "$USER1_PASS"
register "$USER2_EMAIL" "$USER2_PASS"

echo "== Logging in =="
TOKEN1="$(login_token "$USER1_EMAIL" "$USER1_PASS")"
TOKEN2="$(login_token "$USER2_EMAIL" "$USER2_PASS")"

if [[ -z "$TOKEN1" || "$TOKEN1" == "null" ]]; then
  echo "Failed to get token for user1"; exit 1
fi
if [[ -z "$TOKEN2" || "$TOKEN2" == "null" ]]; then
  echo "Failed to get token for user2"; exit 1
fi

# Use explicit UTC instants; adjust dates as you like.
# We'll seed 3 days worth of events with repeats to create top-K behavior.

DAY1="2026-01-27T10:00:00Z"
DAY2="2026-01-28T12:00:00Z"
DAY3="2026-01-29T14:00:00Z"

echo "== Seeding events for user1 =="
post_event "$TOKEN1" "TASK_CREATED"   "TASK" "123" "$DAY1"
post_event "$TOKEN1" "TASK_CREATED"   "TASK" "123" "$DAY1"
post_event "$TOKEN1" "TASK_COMPLETED" "TASK" "123" "$DAY1"

post_event "$TOKEN1" "TASK_CREATED"   "TASK" "456" "$DAY2"
post_event "$TOKEN1" "TASK_CREATED"   "TASK" "456" "$DAY2"
post_event "$TOKEN1" "TASK_CREATED"   "TASK" "456" "$DAY2"

post_event "$TOKEN1" "PROJECT_CREATED" "PROJECT" "P1" "$DAY3"
post_event "$TOKEN1" "PROJECT_CREATED" "PROJECT" "P1" "$DAY3"
post_event "$TOKEN1" "TASK_CREATED"    "TASK"    "123" "$DAY3"

echo "== Seeding events for user2 (separate tenant) =="
post_event "$TOKEN2" "TASK_CREATED"   "TASK" "999" "$DAY1"
post_event "$TOKEN2" "TASK_CREATED"   "TASK" "999" "$DAY2"
post_event "$TOKEN2" "TASK_COMPLETED" "TASK" "999" "$DAY2"
post_event "$TOKEN2" "ORDER_PLACED"   "ORDER" "O-1" "$DAY3"
post_event "$TOKEN2" "ORDER_PLACED"   "ORDER" "O-1" "$DAY3"

echo "Done."
