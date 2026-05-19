#!/bin/bash

# =============================================================================
#  Redis HSET — Live Demo Script
#  Runs real HSET commands against a Redis instance
# =============================================================================

REDIS_CLI="redis-cli"
HOST="127.0.0.1"
PORT="6379"
RC="$REDIS_CLI -h $HOST -p $PORT"

# ── Colors ────────────────────────────────────────────────────────────────────
RED='\033[0;31m';  GREEN='\033[0;32m'; YELLOW='\033[1;33m'
CYAN='\033[0;36m'; BLUE='\033[0;34m';  MAGENTA='\033[0;35m'
BOLD='\033[1m';    RESET='\033[0m'

# ── Helpers ───────────────────────────────────────────────────────────────────
section()  { echo -e "\n${BOLD}${BLUE}══════════════════════════════════════════════════${RESET}"
             echo -e "${BOLD}${CYAN}  $1${RESET}"
             echo -e "${BOLD}${BLUE}══════════════════════════════════════════════════${RESET}"; }

run_cmd() {
    # $@ allows quoted multi-word args to be passed correctly
    echo -e "\n  ${YELLOW}\$${RESET} ${GREEN}redis-cli $*${RESET}"
    local result
    result=$($RC "$@" 2>&1)
    echo -e "  ${MAGENTA}→ ${result}${RESET}"
}

run_raw() {
    echo -e "\n  ${YELLOW}\$${RESET} ${GREEN}redis-cli $*${RESET}"
    $RC "$@" 2>&1 | while IFS= read -r line; do
        echo -e "  ${MAGENTA}  ${line}${RESET}"
    done
}

note() { echo -e "  ${CYAN}ℹ  $1${RESET}"; }
ok()   { echo -e "  ${GREEN}✔  $1${RESET}"; }
warn() { echo -e "  ${RED}⚠  $1${RESET}"; }

# ── Preflight check ───────────────────────────────────────────────────────────
clear
echo -e "${BOLD}${GREEN}"
echo "  ██████╗ ███████╗██████╗ ██╗███████╗     ██╗     ██╗██╗   ██╗███████╗"
echo "  ██╔══██╗██╔════╝██╔══██╗██║██╔════╝     ██║     ██║██║   ██║██╔════╝"
echo "  ██████╔╝█████╗  ██║  ██║██║███████╗     ██║     ██║██║   ██║█████╗  "
echo "  ██╔══██╗██╔══╝  ██║  ██║██║╚════██║     ██║     ██║╚██╗ ██╔╝██╔══╝  "
echo "  ██║  ██║███████╗██████╔╝██║███████║     ███████╗██║ ╚████╔╝ ███████╗ "
echo "  ╚═╝  ╚═╝╚══════╝╚═════╝ ╚═╝╚══════╝     ╚══════╝╚═╝  ╚═══╝  ╚══════╝"
echo -e "${RESET}"
echo -e "  ${BOLD}Redis HSET — Live Command Demo${RESET}"
echo -e "  ${CYAN}Connecting to ${HOST}:${PORT}${RESET}"
echo ""

if ! $RC PING &>/dev/null; then
    warn "Cannot reach Redis at ${HOST}:${PORT}. Start Redis and retry."
    exit 1
fi
ok "Redis is reachable (PONG received)"

# Clean up any previous demo keys
$RC DEL user:1001 user:1002 \
       product:SKU-001 product:SKU-002 \
       cart:session:xyz \
       analytics:today \
       config:app:prod \
       cache:weather:london &>/dev/null

# =============================================================================
section "1 · USER PROFILES  (HSET / HGET / HGETALL / HMGET)"
# =============================================================================

note "Create user:1001 with multiple fields in one HSET call"
run_cmd HSET user:1001 name Alice age 30 email alice@example.com city "New York" plan premium

note "Retrieve a single field"
run_cmd HGET user:1001 name

note "Retrieve multiple specific fields (HMGET)"
run_raw HMGET user:1001 name age plan

note "Retrieve every field + value (HGETALL)"
run_raw HGETALL user:1001

note "Update just the city field — returns 0 because field already existed"
run_cmd HSET user:1001 city "San Francisco"

note "Check the update took effect"
run_cmd HGET user:1001 city

note "Create a second user"
run_cmd HSET user:1002 name Bob age 25 email bob@example.com city London plan free

note "Count how many fields user:1002 has (HLEN)"
run_cmd HLEN user:1002

note "Check that a field exists (HEXISTS)"
run_cmd HEXISTS user:1001 email
run_cmd HEXISTS user:1001 phone

# =============================================================================
section "2 · PRODUCT INVENTORY  (HSET / HINCRBY / HINCRBYFLOAT)"
# =============================================================================

note "Store product details"
run_cmd HSET product:SKU-001 name "Wireless Headphones" brand SoundMax price 79.99 stock 100 rating 4.5
run_cmd HSET product:SKU-002 name "USB-C Hub" brand TechLink price 34.99 stock 250 rating 4.2

note "Sell 3 units — decrement stock atomically"
run_cmd HINCRBY product:SKU-001 stock -3

note "Apply a discount — update price using HINCRBYFLOAT"
run_cmd HINCRBYFLOAT product:SKU-001 price -10.00

note "Check updated product state"
run_raw HGETALL product:SKU-001

note "List only field names (HKEYS)"
run_raw HKEYS product:SKU-002

note "List only values (HVALS)"
run_raw HVALS product:SKU-002

# =============================================================================
section "3 · SHOPPING CART  (HSET / HINCRBY / HDEL / HLEN)"
# =============================================================================

note "Add items to cart — field = SKU, value = quantity"
run_cmd HSET cart:session:xyz SKU-001 2 SKU-002 1

note "Add more of SKU-001 (update quantity)"
run_cmd HINCRBY cart:session:xyz SKU-001 1

note "View full cart"
run_raw HGETALL cart:session:xyz

note "Remove SKU-002 from cart (HDEL)"
run_cmd HDEL cart:session:xyz SKU-002

note "Confirm item was removed"
run_cmd HEXISTS cart:session:xyz SKU-002

note "Total distinct items in cart"
run_cmd HLEN cart:session:xyz

# =============================================================================
section "4 · REAL-TIME ANALYTICS  (HSET / HINCRBY)"
# =============================================================================

note "Initialize today's counters"
run_cmd HSET analytics:today page_views 0 clicks 0 signups 0 purchases 0 revenue 0.0

note "Simulate some traffic events"
run_cmd HINCRBY analytics:today page_views 520
run_cmd HINCRBY analytics:today clicks 134
run_cmd HINCRBY analytics:today signups 17
run_cmd HINCRBY analytics:today purchases 9
run_cmd HINCRBYFLOAT analytics:today revenue 449.91

note "Today's full analytics snapshot"
run_raw HGETALL analytics:today

# =============================================================================
section "5 · APP CONFIG / FEATURE FLAGS  (HSET / HSETNX)"
# =============================================================================

note "Set application configuration"
run_cmd HSET config:app:prod debug false max_connections 200 timeout_secs 30 log_level info maintenance false

note "HSETNX — set only if field does NOT exist"
run_cmd HSETNX config:app:prod new_feature_flag false
note "(trying to HSETNX a field that already exists — should return 0)"
run_cmd HSETNX config:app:prod debug true

note "Enable maintenance mode"
run_cmd HSET config:app:prod maintenance true

note "Read config state"
run_raw HGETALL config:app:prod

# =============================================================================
section "6 · WEATHER CACHE  (HSET / EXPIRE / TTL)"
# =============================================================================

note "Cache a structured API response as a hash"
run_cmd HSET cache:weather:london temp "18C" humidity "72%" condition "Partly Cloudy" wind "14 km/h" updated_at "$(date -u +%Y-%m-%dT%H:%M:%SZ)"

note "Set a TTL of 300 seconds on the cache key"
run_cmd EXPIRE cache:weather:london 300

note "Check remaining TTL"
run_cmd TTL cache:weather:london

note "Partial cache update — only refresh temp and timestamp"
run_cmd HSET cache:weather:london temp "21C" updated_at "$(date -u +%Y-%m-%dT%H:%M:%SZ)"

note "Verify the partial update"
run_raw HGETALL cache:weather:london

# =============================================================================
section "7 · SCANNING FIELDS  (HSCAN — safe for large hashes)"
# =============================================================================

note "HSCAN with cursor 0 — safe iteration, no blocking"
run_raw HSCAN user:1001 0

note "HSCAN with a MATCH pattern to filter fields"
run_raw HSCAN product:SKU-001 0 MATCH "*price*"

# =============================================================================
section "8 · CLEANUP"
# =============================================================================

note "Delete all demo keys"
run_cmd DEL user:1001 user:1002 product:SKU-001 product:SKU-002 cart:session:xyz analytics:today config:app:prod cache:weather:london

echo ""
echo -e "${BOLD}${GREEN}══════════════════════════════════════════════════${RESET}"
echo -e "  ${BOLD}✅  All HSET demos completed successfully!${RESET}"
echo -e "${BOLD}${GREEN}══════════════════════════════════════════════════${RESET}"
echo ""