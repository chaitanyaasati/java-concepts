# Redis — Complete Reference Guide

> **Re**mote **Di**ctionary **S**erver — an open-source, in-memory data structure store used as a database, cache, message broker, and streaming engine.

---

## Table of Contents

1. [Core Concepts](#core-concepts)
2. [Data Types](#data-types)
3. [Connection & Server Commands](#connection--server-commands)
4. [String Commands](#string-commands)
5. [List Commands](#list-commands)
6. [Hash Commands](#hash-commands)
7. [Set Commands](#set-commands)
8. [Sorted Set Commands](#sorted-set-commands)
9. [Key Expiry & TTL](#key-expiry--ttl)
10. [Transactions](#transactions)
11. [Pub/Sub](#pubsub)
12. [Streams](#streams)
13. [Scripting with Lua](#scripting-with-lua)
14. [Persistence](#persistence)
15. [Replication & Clustering](#replication--clustering)
16. [Security](#security)
17. [Performance Tips](#performance-tips)

---

## Core Concepts

### What is Redis?

Redis stores data **in memory**, making reads and writes extremely fast (sub-millisecond latency). Unlike traditional databases, data lives in RAM but can be persisted to disk. Redis is single-threaded for command execution, which eliminates race conditions and makes it inherently thread-safe per command.

### Key Design Principles

| Principle | Description |
|-----------|-------------|
| **In-memory first** | All data is kept in RAM for maximum speed |
| **Key-value store** | Every piece of data is addressed by a unique key |
| **Rich data structures** | Not just strings — lists, sets, hashes, sorted sets, streams, and more |
| **Atomic operations** | Each command executes atomically; no partial writes |
| **Optional persistence** | Snapshots (RDB) and append-only logs (AOF) for durability |
| **Single-threaded** | One command at a time — no locking needed |

### Key Naming Conventions

Redis keys are binary-safe strings. Common conventions:

```
user:1001:profile          # entity:id:field
session:abc123             # type:identifier
product:electronics:42     # hierarchy with colons
rate_limit:ip:192.168.1.1  # underscores for readability
```

> **Best practice:** Use colons (`:`) as namespace separators. Keep keys short but descriptive. Avoid spaces.

---

## Data Types

Redis supports the following native data types:

| Type | Description | Typical Use Case |
|------|-------------|-----------------|
| **String** | Binary-safe string up to 512 MB | Cache values, counters, tokens |
| **List** | Ordered sequence of strings | Queues, activity feeds, timelines |
| **Hash** | Field-value map | User profiles, object storage |
| **Set** | Unordered unique strings | Tags, unique visitors, permissions |
| **Sorted Set** | Set with a float score per member | Leaderboards, priority queues |
| **Stream** | Append-only log of entries | Event sourcing, message queues |
| **Bitmap** | Bit-level operations on strings | Feature flags, analytics |
| **HyperLogLog** | Probabilistic cardinality estimator | Unique count approximation |
| **Geo** | Geospatial index | Location-based search |

---

## Connection & Server Commands

### Connecting

```bash
# Connect via CLI
redis-cli

# Connect to remote server
redis-cli -h 127.0.0.1 -p 6379

# Connect with password
redis-cli -h 127.0.0.1 -p 6379 -a yourpassword

# Connect to a specific database (0–15)
redis-cli -n 2
```

### Essential Server Commands

```bash
PING                    # Test connection — returns PONG
PING "hello"            # Returns "hello"

INFO                    # Comprehensive server stats
INFO server             # Section-specific: server, clients, memory, stats, replication
INFO memory

DBSIZE                  # Number of keys in current database

SELECT 1                # Switch to database 1 (default is 0)

FLUSHDB                 # Delete all keys in current database ⚠️
FLUSHALL                # Delete all keys in ALL databases ⚠️

SAVE                    # Synchronous snapshot to disk
BGSAVE                  # Asynchronous background snapshot

DEBUG SLEEP 5           # Pause server for 5 seconds (testing only)

SHUTDOWN SAVE           # Save and shut down
SHUTDOWN NOSAVE         # Shut down without saving ⚠️

CONFIG GET maxmemory            # Get config value
CONFIG SET maxmemory 256mb      # Set config value at runtime
CONFIG REWRITE                  # Persist CONFIG SET changes to redis.conf

CLIENT LIST             # List connected clients
CLIENT KILL ID 42       # Disconnect a client
CLIENT SETNAME myapp    # Name the current connection

COMMAND COUNT           # Total number of Redis commands
COMMAND INFO get set    # Details for specific commands

LATENCY HISTORY event   # Latency samples for an event
SLOWLOG GET 10          # Last 10 slow queries
SLOWLOG RESET           # Clear the slow log
```

---

## String Commands

Strings are the simplest Redis type. They can hold text, integers, floats, or raw binary data.

### Basic Get / Set

```bash
SET key value                   # Set a key
SET user:1:name "Alice"

GET key                         # Get a value
GET user:1:name                 # → "Alice"

MSET key1 v1 key2 v2            # Set multiple keys at once
MSET color red shape circle

MGET key1 key2                  # Get multiple keys
MGET color shape                # → ["red", "circle"]

GETSET key newvalue             # Set new value, return old value (atomic)
GETDEL key                      # Get value and delete key (atomic)
GETEX key EX 60                 # Get value and set expiry simultaneously

SETNX key value                 # Set ONLY if key does not exist (0 or 1)
SET key value NX                # Modern equivalent with options

SETEX key seconds value         # Set with expiry in seconds
PSETEX key milliseconds value   # Set with expiry in milliseconds

SET key value EX 60             # Modern: set with EX (seconds)
SET key value PX 5000           # Modern: set with PX (milliseconds)
SET key value NX EX 30          # Set only if not exists + expiry (distributed lock pattern)
SET key value XX                # Set only if key already exists
```

**Use cases:** Session tokens (`SETEX session:abc token 3600`), feature flags, caching API responses.

### Counter / Numeric Operations

```bash
INCR views:page:42              # Increment by 1 → integer
DECR stock:item:7               # Decrement by 1

INCRBY score:user:1 10          # Increment by N
DECRBY balance:user:1 50        # Decrement by N

INCRBYFLOAT price:item 1.99     # Increment float
INCRBYFLOAT temp:sensor -0.5    # Decrement float via negative value
```

**Use cases:** Page view counters, rate limiting, inventory counts, real-time scoring.

### String Manipulation

```bash
APPEND key value                # Append to string, returns new length
APPEND log:today "2024-01-01 login\n"

STRLEN key                      # String length in bytes
STRLEN user:1:name              # → 5

GETRANGE key start end          # Substring (0-indexed, inclusive)
GETRANGE bio:user:1 0 9         # First 10 characters

SETRANGE key offset value       # Overwrite at byte offset
SETRANGE greeting 6 "Redis"     # Overwrite portion
```

---

## List Commands

Lists are ordered, doubly-linked lists of strings. Fast O(1) push/pop from both ends.

### Push & Pop

```bash
LPUSH queue task1 task2 task3   # Push to LEFT (head). task3 is now at head.
RPUSH queue task4 task5         # Push to RIGHT (tail)

LPOP queue                      # Pop from LEFT → "task3"
RPOP queue                      # Pop from RIGHT → "task5"

LMPOP 1 queue LEFT COUNT 3      # Pop up to 3 elements from left

BLPOP queue 30                  # Blocking left pop — waits up to 30s for an element
BRPOP queue 30                  # Blocking right pop

LMOVE src dst LEFT RIGHT        # Atomically move element between lists
```

**Use cases:** Task queues (`RPUSH` + `BLPOP`), activity feeds, undo stacks, job pipelines.

### Inspect & Modify

```bash
LLEN queue                      # Number of elements
LRANGE queue 0 -1               # All elements (0 = first, -1 = last)
LRANGE queue 0 9                # First 10 elements

LINDEX queue 0                  # Element at index
LSET queue 2 "newtask"          # Update element at index

LINSERT queue BEFORE "task2" "taskX"   # Insert before pivot
LINSERT queue AFTER "task2" "taskY"    # Insert after pivot

LREM queue 2 "oldtask"          # Remove 2 occurrences of "oldtask"
LTRIM queue 0 99                # Keep only elements 0–99, discard rest
```

---

## Hash Commands

Hashes are maps of field-value pairs. Ideal for representing objects.

```bash
HSET user:1001 name "Alice" age 30 email "alice@example.com"
HGET user:1001 name             # → "Alice"
HGET user:1001 age              # → "30"

HMGET user:1001 name email      # Get multiple fields

HGETALL user:1001               # All field-value pairs (alternating)
HKEYS user:1001                 # All field names
HVALS user:1001                 # All values
HLEN user:1001                  # Number of fields

HEXISTS user:1001 email         # Does field exist? → 1 or 0
HDEL user:1001 age              # Delete one or more fields

HSETNX user:1001 phone "555-0100"  # Set field only if it doesn't exist

HINCRBY user:1001 login_count 1    # Increment integer field
HINCRBYFLOAT user:1001 balance 9.99

HSCAN user:1001 0 MATCH "e*" COUNT 10  # Cursor-based scan of fields
```

**Use cases:** User profiles, product catalogs, configuration objects, shopping cart items.

> **Tip:** A hash with fewer than 128 fields and small values uses a memory-efficient ziplist encoding. Hashes can be 4–10× more memory-efficient than storing each field as a separate key.

---

## Set Commands

Sets are unordered collections of unique strings. Fast membership tests in O(1).

```bash
SADD tags:post:42 redis database nosql   # Add members (ignores duplicates)
SREM tags:post:42 nosql                  # Remove a member
SMEMBERS tags:post:42                    # All members (unordered)
SCARD tags:post:42                       # Count of members

SISMEMBER tags:post:42 redis             # Is "redis" a member? → 1 or 0
SMISMEMBER tags:post:42 redis java       # Check multiple members at once

SRANDMEMBER tags:post:42 3              # 3 random members (no removal)
SPOP tags:post:42                        # Remove and return random member

# Set operations (return results)
SUNION tags:post:1 tags:post:2          # Union of two sets
SINTER tags:post:1 tags:post:2          # Intersection
SDIFF tags:post:1 tags:post:2           # Difference (in 1 but not 2)

# Set operations (store results in a new key)
SUNIONSTORE result tags:post:1 tags:post:2
SINTERSTORE result tags:post:1 tags:post:2
SDIFFSTORE result tags:post:1 tags:post:2

SSCAN myset 0 MATCH "r*" COUNT 100      # Cursor-based iteration
```

**Use cases:** Unique visitors, tag systems, friend lists, access control, deduplication, random selections.

---

## Sorted Set Commands

Like Sets, but each member has an associated floating-point **score**. Members are always returned ordered by score.

```bash
ZADD leaderboard 1500 "alice" 2300 "bob" 900 "charlie"

# Options: NX (add only), XX (update only), GT (update if greater), LT (update if less)
ZADD leaderboard GT 2400 "bob"          # Update only if new score > current score

ZRANGE leaderboard 0 -1                 # All members, low → high score
ZRANGE leaderboard 0 -1 REV            # High → low (ZREVRANGE equivalent)
ZRANGE leaderboard 0 -1 WITHSCORES    # Include scores in output
ZRANGE leaderboard "(1000" "+inf" BYSCORE LIMIT 0 10  # Score range with pagination

ZRANK leaderboard "alice"               # 0-based rank (low score = rank 0)
ZREVRANK leaderboard "alice"            # Rank from highest
ZSCORE leaderboard "alice"              # Get score of a member

ZINCRBY leaderboard 100 "charlie"       # Increment score by 100

ZCARD leaderboard                       # Total number of members
ZCOUNT leaderboard 1000 2000           # Members with score between 1000 and 2000
ZLEXCOUNT myset "[a" "[z"              # Members in lex range (same score required)

ZREM leaderboard "charlie"              # Remove member
ZREMRANGEBYRANK leaderboard 0 4        # Remove bottom 5 by rank
ZREMRANGEBYSCORE leaderboard 0 999     # Remove members with score < 1000

ZPOPMIN leaderboard 3                  # Remove and return 3 lowest-score members
ZPOPMAX leaderboard 3                  # Remove and return 3 highest-score members
BZPOPMIN leaderboard 30               # Blocking version

ZUNIONSTORE dest 2 z1 z2               # Union of sorted sets
ZINTERSTORE dest 2 z1 z2               # Intersection
ZDIFFSTORE dest 2 z1 z2                # Difference

ZSCAN leaderboard 0 MATCH "a*"         # Cursor scan
```

**Use cases:** Leaderboards, priority queues, time-based event scheduling (score = timestamp), rate limiting sliding windows.

---

## Key Expiry & TTL

```bash
EXPIRE key 60                   # Set expiry in seconds
PEXPIRE key 60000               # Set expiry in milliseconds
EXPIREAT key 1700000000         # Set expiry as Unix timestamp (seconds)
PEXPIREAT key 1700000000000     # Set expiry as Unix timestamp (ms)

TTL key                         # Remaining TTL in seconds (-1 = no expiry, -2 = key gone)
PTTL key                        # Remaining TTL in milliseconds

PERSIST key                     # Remove expiry (make key permanent)

# Check if key exists
EXISTS key                      # → 1 or 0
EXISTS k1 k2 k3                 # Returns count of existing keys

# Rename
RENAME old new                  # Rename (errors if old doesn't exist)
RENAMENX old new                # Rename only if new doesn't exist

# Type checking
TYPE key                        # → string, list, hash, set, zset, stream

# Delete
DEL key1 key2                   # Synchronous delete, returns count deleted
UNLINK key1 key2                # Async delete (non-blocking, background GC)

# Copy
COPY src dst                    # Copy value to new key (fails if dst exists)
COPY src dst REPLACE            # Copy with overwrite

# Scan keys (NEVER use KEYS in production)
SCAN 0 MATCH "user:*" COUNT 100         # Cursor-based scan, non-blocking
SCAN cursor MATCH pattern TYPE hash     # Filter by type
```

> ⚠️ **Never use `KEYS *` in production.** It blocks Redis until all keys are scanned. Use `SCAN` with a cursor instead.

---

## Transactions

Redis transactions execute a block of commands atomically. Either all execute or none do (on syntax/type errors at queue time). Note: runtime errors in EXEC do not roll back other commands.

```bash
MULTI                           # Begin transaction block
SET counter 1
INCR counter
GET counter
EXEC                            # Execute all queued commands atomically
                                # → [OK, 2, "2"]

MULTI
SET x 10
DISCARD                         # Abort — discard queued commands

# Optimistic locking with WATCH
WATCH balance:user:1            # Watch key — transaction aborts if it changes
MULTI
DECRBY balance:user:1 100
INCRBY balance:user:2 100
EXEC                            # Returns nil if watched key changed (retry needed)
```

**Retry pattern:**
```
loop:
  WATCH balance:user:1
  current = GET balance:user:1
  MULTI
  SET balance:user:1 (current - 100)
  result = EXEC
  if result != nil: break   # success
  # else: key changed, retry
```

**Use cases:** Fund transfers, inventory reservations, counters with conditional logic.

---

## Pub/Sub

Redis Pub/Sub is a fire-and-forget messaging system. Publishers send messages to channels; subscribers receive them in real time. Messages are **not persisted** — offline clients miss messages.

```bash
# Subscriber side
SUBSCRIBE news sports           # Subscribe to channels (blocks, receives messages)
PSUBSCRIBE news.*               # Pattern subscribe (glob patterns)
UNSUBSCRIBE news                # Unsubscribe from channel
PUNSUBSCRIBE news.*             # Unsubscribe from pattern

# Publisher side
PUBLISH news "Breaking: Redis 8.0 released!"   # Returns number of subscribers who received it

# Introspection
PUBSUB CHANNELS                 # List all active channels
PUBSUB CHANNELS "news*"        # Channels matching pattern
PUBSUB NUMSUB news sports       # Subscriber count per channel
PUBSUB NUMPAT                   # Number of active pattern subscriptions
```

**Use cases:** Real-time notifications, live dashboards, chat, broadcasting configuration changes.

> For reliable messaging where offline clients must not miss messages, use **Streams** instead.

---

## Streams

Streams are an append-only log data structure. Unlike Pub/Sub, messages are persisted and consumers can read from any point in history. Supports consumer groups for parallel processing.

```bash
# Add entries (auto-generate ID with *)
XADD events * action "login" user_id 42 ip "10.0.0.1"
XADD events * action "purchase" user_id 42 item_id 99

# Read entries
XLEN events                                     # Total entry count
XRANGE events - +                              # All entries (- = min, + = max)
XRANGE events 1700000000000-0 +               # From timestamp onward
XRANGE events - + COUNT 10                    # First 10 entries
XREVRANGE events + - COUNT 5                  # Latest 5 (reverse)

# Read new entries (blocking)
XREAD COUNT 10 BLOCK 5000 STREAMS events 0   # Read up to 10, block 5s, from beginning
XREAD COUNT 5 BLOCK 0 STREAMS events $       # Block indefinitely for NEW entries only

# Consumer groups
XGROUP CREATE events mygroup $ MKSTREAM       # Create group at last entry
XGROUP CREATE events mygroup 0                # Create group from beginning

XREADGROUP GROUP mygroup consumer1 COUNT 5 BLOCK 2000 STREAMS events >
# > means: deliver undelivered messages

XACK events mygroup 1700000000001-0           # Acknowledge processed message

XPENDING events mygroup - + 10               # List unacknowledged (pending) messages
XCLAIM events mygroup consumer2 60000 id     # Reassign stale pending message (idle > 60s)

# Trim stream (avoid unbounded growth)
XTRIM events MAXLEN 10000                    # Keep only last 10,000 entries
XTRIM events MAXLEN ~ 10000                  # Approximate trim (faster)
XADD events MAXLEN ~ 10000 * key val         # Trim on add

XDEL events 1700000000001-0                  # Delete specific entry
XINFO STREAM events                          # Stream metadata
XINFO GROUPS events                          # Group info
XINFO CONSUMERS events mygroup              # Consumer info
```

**Use cases:** Event sourcing, audit logs, distributed task queues, IoT sensor data, microservice event buses.

---

## Scripting with Lua

Lua scripts run atomically on the Redis server, allowing complex logic without round trips.

```bash
# Inline eval (script, num_keys, keys..., args...)
EVAL "return redis.call('GET', KEYS[1])" 1 mykey

# Conditional set
EVAL "
  local val = redis.call('GET', KEYS[1])
  if val == ARGV[1] then
    return redis.call('SET', KEYS[1], ARGV[2])
  end
  return nil
" 1 mykey expected_value new_value

# Load script (returns SHA1 digest)
SCRIPT LOAD "return redis.call('PING')"
# → "e0e1f9fabfa9d353eca4c6f5d2d78b27b2f5e82a"

# Execute cached script by SHA
EVALSHA e0e1f9fabfa9d353eca4c6f5d2d78b27b2f5e82a 0

SCRIPT EXISTS sha1 sha2         # Check if scripts are cached
SCRIPT FLUSH                    # Remove all cached scripts

# Redis 7.0+ Functions (persistent, reloadable alternative to EVALSHA)
FUNCTION LOAD "#!lua name=mylib\nredis.register_function('myfunc', function(keys, args) return 'hello' end)"
FCALL myfunc 0
FUNCTION LIST
FUNCTION DELETE mylib
```

**Use cases:** Atomic compare-and-swap, distributed locks, complex multi-key operations, reducing round trips.

---

## Persistence

Redis offers two persistence mechanisms that can be used independently or together.

### RDB (Redis Database Snapshots)

Point-in-time snapshots written to disk as a compact binary file (`dump.rdb`).

```bash
# In redis.conf:
save 900 1      # Snapshot if ≥1 key changed in 900s
save 300 10     # Snapshot if ≥10 keys changed in 300s
save 60 10000   # Snapshot if ≥10000 keys changed in 60s

dbfilename dump.rdb
dir /var/lib/redis

# Trigger manually:
SAVE            # Synchronous (blocks Redis)
BGSAVE          # Background (fork, non-blocking)
LASTSAVE        # Unix timestamp of last successful save
```

**Pros:** Fast restarts, small files, no write overhead.
**Cons:** Risk of data loss between snapshots.

### AOF (Append-Only File)

Every write command is logged to an append-only file (`appendonly.aof`).

```bash
# In redis.conf:
appendonly yes
appendfilename "appendonly.aof"

# fsync strategy:
appendfsync always      # Safe, slow — fsync on every write
appendfsync everysec    # Default — fsync every second (max ~1s data loss)
appendfsync no          # Fast — let OS decide (max data loss = OS buffer)

# Auto-rewrite when AOF grows too large:
auto-aof-rewrite-percentage 100
auto-aof-rewrite-min-size 64mb

# Trigger rewrite manually:
BGREWRITEAOF
```

**Pros:** Near zero data loss, human-readable log.
**Cons:** Larger file size, slower restarts.

> **Recommendation:** Use `appendfsync everysec` + RDB together for production. RDB for fast restarts, AOF for durability.

---

## Replication & Clustering

### Replication (Primary–Replica)

```bash
# On replica server (redis.conf):
replicaof 192.168.1.10 6379

# Or at runtime:
REPLICAOF 192.168.1.10 6379     # Start replication from primary
REPLICAOF NO ONE                # Promote replica to primary

# Check replication status:
INFO replication
```

### Redis Sentinel

Sentinel provides automatic failover and monitoring for primary–replica setups.

```bash
# sentinel.conf
sentinel monitor mymaster 127.0.0.1 6379 2   # 2 = quorum
sentinel down-after-milliseconds mymaster 30000
sentinel failover-timeout mymaster 60000

redis-sentinel /etc/redis/sentinel.conf
```

### Redis Cluster

Cluster distributes data across multiple shards using hash slots (0–16383).

```bash
# Create cluster from CLI
redis-cli --cluster create \
  127.0.0.1:7000 127.0.0.1:7001 127.0.0.1:7002 \
  127.0.0.1:7003 127.0.0.1:7004 127.0.0.1:7005 \
  --cluster-replicas 1

redis-cli --cluster info 127.0.0.1:7000    # Cluster health
redis-cli --cluster check 127.0.0.1:7000

CLUSTER INFO                    # Cluster state
CLUSTER NODES                   # All nodes and their slots
CLUSTER KEYSLOT mykey           # Which hash slot does this key map to?

# Rebalance slots
redis-cli --cluster rebalance 127.0.0.1:7000
```

---

## Security

```bash
# In redis.conf:
bind 127.0.0.1                  # Only listen on localhost
protected-mode yes              # Requires bind or password if exposed
requirepass yourStrongPassword  # Set password

# ACL (Redis 6+)
ACL LIST                        # All ACL rules
ACL WHOAMI                      # Current user
ACL CAT                         # All command categories
ACL CAT string                  # Commands in "string" category

# Create a restricted user
ACL SETUSER appuser on >secretpass ~user:* ~session:* +GET +SET +DEL +EXPIRE

# Save ACL config
ACL SAVE

# TLS (Redis 6+) — in redis.conf:
tls-port 6380
tls-cert-file /etc/redis/redis.crt
tls-key-file /etc/redis/redis.key
tls-ca-cert-file /etc/redis/ca.crt
```

---

## Performance Tips

### Memory

```bash
CONFIG SET maxmemory 512mb
CONFIG SET maxmemory-policy allkeys-lru    # Eviction policy

# Eviction policies:
# noeviction       — return error when full (default)
# allkeys-lru      — evict least recently used keys
# volatile-lru     — LRU among keys with TTL
# allkeys-lfu      — evict least frequently used
# volatile-lfu     — LFU among keys with TTL
# allkeys-random   — evict random key
# volatile-random  — random key with TTL
# volatile-ttl     — evict key closest to expiry

MEMORY USAGE key                # Memory used by key (bytes)
MEMORY DOCTOR                   # Memory health analysis
OBJECT ENCODING key             # Internal encoding (ziplist, skiplist, etc.)
OBJECT IDLETIME key             # Seconds since last access
OBJECT FREQ key                 # Access frequency (LFU only)
```

### Pipelining

Send multiple commands without waiting for individual responses. Reduces round-trip overhead dramatically.

```bash
redis-cli --pipe < commands.txt

# In application code (example concept):
pipeline.SET("k1", "v1")
pipeline.SET("k2", "v2")
pipeline.INCR("counter")
pipeline.execute()              # All sent in one network round trip
```

### Connection Pooling

Always use a connection pool in production. Creating a new TCP connection per command adds ~1ms overhead. Most Redis clients (redis-py, ioredis, Jedis) handle pooling automatically with configuration.

### Avoid Blocking Commands in Production

| Avoid | Use Instead |
|-------|------------|
| `KEYS *` | `SCAN 0 MATCH * COUNT 100` |
| `SMEMBERS` on huge sets | `SSCAN` |
| `HGETALL` on huge hashes | `HSCAN` |
| `LRANGE 0 -1` on huge lists | Paginate with `LRANGE 0 99`, `100 199` |

### Useful Diagnostic Commands

```bash
MONITOR                         # Stream all commands in real time (dev only!) ⚠️
DEBUG OBJECT key                # Low-level object info
OBJECT HELP                     # Object subcommands
LATENCY RESET                   # Reset latency samples
SLOWLOG GET 25                  # Last 25 slow commands (> slowlog-log-slower-than µs)
MEMORY STATS                    # Detailed memory breakdown
```

---

## Quick Command Cheatsheet

```
# Strings
SET / GET / MSET / MGET / DEL
INCR / DECR / INCRBY / INCRBYFLOAT
SETEX / SETNX / GETSET / APPEND

# Lists
LPUSH / RPUSH / LPOP / RPOP
LRANGE / LLEN / LINDEX / LTRIM
BLPOP / BRPOP / LMOVE

# Hashes
HSET / HGET / HMGET / HGETALL
HDEL / HEXISTS / HINCRBY / HLEN

# Sets
SADD / SREM / SMEMBERS / SCARD
SISMEMBER / SUNION / SINTER / SDIFF
SPOP / SRANDMEMBER

# Sorted Sets
ZADD / ZRANGE / ZRANK / ZSCORE
ZINCRBY / ZCARD / ZCOUNT
ZPOPMIN / ZPOPMAX / ZREM

# Keys & TTL
EXISTS / TYPE / RENAME / COPY
EXPIRE / TTL / PERSIST / SCAN / UNLINK

# Transactions
MULTI / EXEC / DISCARD / WATCH

# Pub/Sub
PUBLISH / SUBSCRIBE / PSUBSCRIBE / UNSUBSCRIBE

# Streams
XADD / XRANGE / XREAD / XLEN
XGROUP / XREADGROUP / XACK / XPENDING
```

---

## Further Reading

- [Official Redis Documentation](https://redis.io/docs/)
- [Redis Commands Reference](https://redis.io/commands/)
- [Redis University (free courses)](https://university.redis.com/)
- [Redis GitHub](https://github.com/redis/redis)
- [Redis Design Patterns](https://redis.io/docs/manual/patterns/)