#!/bin/bash

# =============================================================================
#  Redis HSET Command - Explanation & Use Cases
# =============================================================================

# Color codes for formatting
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
BLUE='\033[0;34m'
MAGENTA='\033[0;35m'
BOLD='\033[1m'
RESET='\033[0m'

# Helper: print section header
section() {
    echo ""
    echo -e "${BOLD}${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"
    echo -e "${BOLD}${CYAN}  $1${RESET}"
    echo -e "${BOLD}${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"
}

# Helper: print a command block
show_cmd() {
    echo -e "${YELLOW}  \$${RESET} ${GREEN}$1${RESET}"
}

# Helper: simulate Redis output
redis_out() {
    echo -e "  ${MAGENTA}→ $1${RESET}"
}

# Helper: print a note/explanation
note() {
    echo -e "  ${CYAN}ℹ  $1${RESET}"
}

# Helper: print a label
label() {
    echo -e "\n  ${BOLD}${RED}▶ $1${RESET}"
}

# =============================================================================
clear
echo -e "${BOLD}${GREEN}"
echo "  ██████╗ ███████╗██████╗ ██╗███████╗    ██╗  ██╗███████╗███████╗████████╗"
echo "  ██╔══██╗██╔════╝██╔══██╗██║██╔════╝    ██║  ██║██╔════╝██╔════╝╚══██╔══╝"
echo "  ██████╔╝█████╗  ██║  ██║██║███████╗    ███████║███████╗█████╗     ██║   "
echo "  ██╔══██╗██╔══╝  ██║  ██║██║╚════██║    ██╔══██║╚════██║██╔══╝     ██║   "
echo "  ██║  ██║███████╗██████╔╝██║███████║    ██║  ██║███████║███████╗   ██║   "
echo "  ╚═╝  ╚═╝╚══════╝╚═════╝ ╚═╝╚══════╝   ╚═╝  ╚═╝╚══════╝╚══════╝   ╚═╝  "
echo -e "${RESET}"
echo -e "  ${BOLD}Redis HSET Command — Complete Guide with Use Cases${RESET}"
echo -e "  ${CYAN}Hash Set: Store, Update & Manage Hash Fields in Redis${RESET}"

# =============================================================================
section "1. WHAT IS HSET?"
# =============================================================================

echo ""
note "HSET sets one or more field-value pairs on a Redis Hash stored at a key."
note "If the key does not exist, a new Hash is created automatically."
note "If a field already exists, its value is overwritten."
echo ""
echo -e "  ${BOLD}Syntax:${RESET}"
echo -e "  ${GREEN}  HSET key field1 value1 [field2 value2 ...]${RESET}"
echo ""
note "Returns: (integer) number of NEW fields added (not updated ones)."
echo ""
note "Before Redis 4.0, HSET only accepted a single field-value pair."
note "HMSET was used for multiple fields. Since Redis 4.0, HSET handles both."
note "HMSET is now deprecated in favor of HSET."

# =============================================================================
section "2. BASIC USAGE"
# =============================================================================

label "2a. Set a single field"
show_cmd "HSET user:1001 name \"Alice\""
redis_out "(integer) 1   # 1 new field added"
echo ""

label "2b. Set multiple fields at once"
show_cmd "HSET user:1001 name \"Alice\" age 30 email \"alice@example.com\" city \"New York\""
redis_out "(integer) 3   # 3 new fields added (name already existed, so total new = 3)"
echo ""

label "2c. Retrieve a field (HGET)"
show_cmd "HGET user:1001 name"
redis_out "\"Alice\""
echo ""

label "2d. Retrieve all fields and values (HGETALL)"
show_cmd "HGETALL user:1001"
redis_out "1) \"name\""
redis_out "2) \"Alice\""
redis_out "3) \"age\""
redis_out "4) \"30\""
redis_out "5) \"email\""
redis_out "6) \"alice@example.com\""
redis_out "7) \"city\""
redis_out "8) \"New York\""
echo ""

label "2e. Update an existing field"
show_cmd "HSET user:1001 city \"San Francisco\""
redis_out "(integer) 0   # 0 new fields — 'city' already existed, just updated"

# =============================================================================
section "3. RELATED HASH COMMANDS"
# =============================================================================

echo ""
note "These commands work alongside HSET to manage hashes:"
echo ""

printf "  %-18s %s\n" "${BOLD}${GREEN}Command${RESET}" "${BOLD}${CYAN}Description${RESET}"
echo "  ──────────────────────────────────────────────────────────"
printf "  %-18s %s\n" "HGET"        "Get value of a single field"
printf "  %-18s %s\n" "HMGET"       "Get values of multiple fields"
printf "  %-18s %s\n" "HGETALL"     "Get all fields and values"
printf "  %-18s %s\n" "HKEYS"       "Get all field names"
printf "  %-18s %s\n" "HVALS"       "Get all values"
printf "  %-18s %s\n" "HDEL"        "Delete one or more fields"
printf "  %-18s %s\n" "HEXISTS"     "Check if a field exists (1/0)"
printf "  %-18s %s\n" "HLEN"        "Count number of fields"
printf "  %-18s %s\n" "HINCRBY"     "Increment an integer field"
printf "  %-18s %s\n" "HINCRBYFLOAT" "Increment a float field"
printf "  %-18s %s\n" "HSETNX"      "Set field only if it doesn't exist"
printf "  %-18s %s\n" "HSCAN"       "Iterate over fields (cursor-based)"

# =============================================================================
section "4. USE CASE 1 — User Profile / Session Store"
# =============================================================================

echo ""
note "Store all user profile attributes under a single key."
note "Avoids multiple string keys like user:1001:name, user:1001:age, etc."
echo ""

label "Store user profile"
show_cmd "HSET user:1001 \\
    username    \"alice_dev\" \\
    email       \"alice@example.com\" \\
    age         30 \\
    country     \"USA\" \\
    plan        \"premium\" \\
    joined      \"2023-01-15\" \\
    login_count 142"
redis_out "(integer) 7"
echo ""

label "Fetch only specific fields (HMGET)"
show_cmd "HMGET user:1001 username plan login_count"
redis_out "1) \"alice_dev\""
redis_out "2) \"premium\""
redis_out "3) \"142\""
echo ""

label "Increment login count on each login"
show_cmd "HINCRBY user:1001 login_count 1"
redis_out "(integer) 143"
echo ""
note "✅ Benefit: Atomic field-level reads/writes. No need to deserialize full objects."

# =============================================================================
section "5. USE CASE 2 — Product / Inventory Catalog"
# =============================================================================

echo ""
note "Store product metadata in hashes for fast lookups in e-commerce apps."
echo ""

label "Store product details"
show_cmd "HSET product:SKU-9981 \\
    name     \"Wireless Headphones\" \\
    brand    \"SoundMax\" \\
    price    79.99 \\
    stock    250 \\
    category \"Electronics\" \\
    rating   4.5"
redis_out "(integer) 6"
echo ""

label "Check if product is in stock"
show_cmd "HGET product:SKU-9981 stock"
redis_out "\"250\""
echo ""

label "Decrease stock after a purchase"
show_cmd "HINCRBY product:SKU-9981 stock -1"
redis_out "(integer) 249"
echo ""

label "Update price dynamically"
show_cmd "HSET product:SKU-9981 price 69.99"
redis_out "(integer) 0   # field existed, just updated"
echo ""
note "✅ Benefit: Structured product data in one key. Easy bulk updates."

# =============================================================================
section "6. USE CASE 3 — Real-Time Analytics / Counters"
# =============================================================================

echo ""
note "Track page views, clicks, or event counters per day/week with hashes."
echo ""

label "Increment event counters (daily stats)"
show_cmd "HSET analytics:2024-06-01 page_views 0 clicks 0 signups 0 purchases 0"
redis_out "(integer) 4"
echo ""

show_cmd "HINCRBY analytics:2024-06-01 page_views 1"
redis_out "(integer) 1"
show_cmd "HINCRBY analytics:2024-06-01 clicks 1"
redis_out "(integer) 1"
show_cmd "HINCRBY analytics:2024-06-01 signups 1"
redis_out "(integer) 1"
echo ""

label "Fetch full daily report"
show_cmd "HGETALL analytics:2024-06-01"
redis_out "1) \"page_views\"  2) \"1\""
redis_out "3) \"clicks\"      4) \"1\""
redis_out "5) \"signups\"     6) \"1\""
redis_out "7) \"purchases\"   8) \"0\""
echo ""
note "✅ Benefit: Atomic increments. One key per day. Easy aggregation."

# =============================================================================
section "7. USE CASE 4 — Shopping Cart"
# =============================================================================

echo ""
note "Model a shopping cart where each field is a product ID and value is quantity."
echo ""

label "Add items to cart (user session)"
show_cmd "HSET cart:session:abc123 SKU-9981 2 SKU-4421 1 SKU-7732 3"
redis_out "(integer) 3"
echo ""

label "Update quantity of one item"
show_cmd "HSET cart:session:abc123 SKU-9981 4"
redis_out "(integer) 0   # updated, not new"
echo ""

label "Remove an item from cart"
show_cmd "HDEL cart:session:abc123 SKU-4421"
redis_out "(integer) 1"
echo ""

label "Get all cart items"
show_cmd "HGETALL cart:session:abc123"
redis_out "1) \"SKU-9981\"  2) \"4\""
redis_out "3) \"SKU-7732\"  4) \"3\""
echo ""

label "Count distinct items in cart"
show_cmd "HLEN cart:session:abc123"
redis_out "(integer) 2"
echo ""
note "✅ Benefit: O(1) reads and updates per item. Natural cart model."

# =============================================================================
section "8. USE CASE 5 — Configuration / Feature Flags"
# =============================================================================

echo ""
note "Store app configuration or feature toggles per environment or tenant."
echo ""

label "Set feature flags for a tenant"
show_cmd "HSET config:tenant:acme \\
    dark_mode        true \\
    max_users        500 \\
    beta_features    false \\
    api_rate_limit   1000 \\
    timezone         \"America/Chicago\""
redis_out "(integer) 5"
echo ""

label "Toggle a feature flag"
show_cmd "HSET config:tenant:acme beta_features true"
redis_out "(integer) 0"
echo ""

label "Check if dark_mode is enabled"
show_cmd "HGET config:tenant:acme dark_mode"
redis_out "\"true\""
echo ""

label "Conditionally set a flag only if it doesn't exist (HSETNX)"
show_cmd "HSETNX config:tenant:acme new_ui_flag false"
redis_out "(integer) 1   # set because it didn't exist"
show_cmd "HSETNX config:tenant:acme dark_mode false"
redis_out "(integer) 0   # NOT set — dark_mode already exists"
echo ""
note "✅ Benefit: Instant config lookups. No DB round trips. Easy multi-tenant."

# =============================================================================
section "9. USE CASE 6 — Caching API / DB Responses"
# =============================================================================

echo ""
note "Cache structured API responses as hashes instead of JSON strings."
note "Allows partial updates without re-serializing the full payload."
echo ""

label "Cache weather API response"
show_cmd "HSET weather:cache:london \\
    temperature  \"18°C\" \\
    humidity     \"72%\" \\
    condition    \"Partly Cloudy\" \\
    wind_speed   \"14 km/h\" \\
    fetched_at   \"2024-06-01T09:00:00Z\""
redis_out "(integer) 5"
echo ""

label "Update only the temperature (partial update)"
show_cmd "HSET weather:cache:london temperature \"20°C\" fetched_at \"2024-06-01T10:00:00Z\""
redis_out "(integer) 0   # fields updated, not added"
echo ""

label "Set TTL on the cache key"
show_cmd "EXPIRE weather:cache:london 3600"
redis_out "(integer) 1   # key expires in 1 hour"
echo ""
note "✅ Benefit: Partial cache updates. Avoids full JSON re-serialization."

# =============================================================================
section "10. PERFORMANCE CHARACTERISTICS"
# =============================================================================

echo ""
printf "  %-30s %s\n" "${BOLD}${GREEN}Operation${RESET}" "${BOLD}${CYAN}Time Complexity${RESET}"
echo "  ─────────────────────────────────────────────────────────"
printf "  %-30s %s\n" "HSET (N fields)"      "O(N)"
printf "  %-30s %s\n" "HGET"                 "O(1)"
printf "  %-30s %s\n" "HMGET (N fields)"     "O(N)"
printf "  %-30s %s\n" "HGETALL"              "O(N) — N = number of fields"
printf "  %-30s %s\n" "HDEL (N fields)"      "O(N)"
printf "  %-30s %s\n" "HEXISTS"              "O(1)"
printf "  %-30s %s\n" "HLEN"                 "O(1)"
printf "  %-30s %s\n" "HINCRBY / HINCRBYFLOAT" "O(1)"
echo ""
note "⚠  Avoid HGETALL on hashes with thousands of fields — use HSCAN instead."
note "⚠  Redis hashes are memory-efficient when field count < 128 and values < 64 bytes."
note "   (Controlled by hash-max-listpack-entries and hash-max-listpack-value in redis.conf)"

# =============================================================================
section "11. QUICK REFERENCE CHEATSHEET"
# =============================================================================

echo ""
echo -e "  ${BOLD}Task                              Command${RESET}"
echo "  ───────────────────────────────────────────────────────────────────────"
echo "  Set one field                     HSET key field value"
echo "  Set many fields                   HSET key f1 v1 f2 v2 ..."
echo "  Get one field                     HGET key field"
echo "  Get many fields                   HMGET key f1 f2 f3"
echo "  Get everything                    HGETALL key"
echo "  Delete a field                    HDEL key field"
echo "  Check field existence             HEXISTS key field"
echo "  Count fields                      HLEN key"
echo "  Increment integer field           HINCRBY key field delta"
echo "  Increment float field             HINCRBYFLOAT key field delta"
echo "  Set only if absent                HSETNX key field value"
echo "  Iterate fields safely             HSCAN key cursor [MATCH pattern] [COUNT n]"
echo "  List all field names              HKEYS key"
echo "  List all values                   HVALS key"

# =============================================================================
section "12. WHEN TO USE HSET vs STRING vs JSON"
# =============================================================================

echo ""
printf "  %-20s %-25s %s\n" "${BOLD}${GREEN}Use Case${RESET}" "${BOLD}${CYAN}Best Fit${RESET}" "${BOLD}${RED}Reason${RESET}"
echo "  ──────────────────────────────────────────────────────────────────────────────"
printf "  %-20s %-25s %s\n" "User profile"        "HSET (Hash)"      "Partial field reads/writes"
printf "  %-20s %-25s %s\n" "Simple flag/token"   "SET (String)"     "Single atomic value"
printf "  %-20s %-25s %s\n" "Deep nested obj"     "SET + JSON"       "Complex nesting needed"
printf "  %-20s %-25s %s\n" "Counters per entity" "HSET + HINCRBY"   "Atomic per-field increment"
printf "  %-20s %-25s %s\n" "Config per tenant"   "HSET (Hash)"      "Field-level updates"
printf "  %-20s %-25s %s\n" "Full obj read-only"  "SET + JSON"       "Always read whole object"

# =============================================================================
echo ""
echo -e "${BOLD}${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"
echo -e "  ${BOLD}✅ Script complete! HSET is one of Redis's most versatile commands.${RESET}"
echo -e "  ${CYAN}  Use hashes when your data has multiple related attributes${RESET}"
echo -e "  ${CYAN}  and you need efficient partial reads or field-level updates.${RESET}"
echo -e "${BOLD}${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"
echo ""