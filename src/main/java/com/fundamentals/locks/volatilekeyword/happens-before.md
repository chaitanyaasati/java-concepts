# Java `volatile` — Happens-Before Explained

> A write to a `volatile` variable V happens-before every subsequent read of V.

---

## What "Happens-Before" Actually Means

It does **not** mean "executes before in time".

It means:

> "If A happens-before B, then all effects of A are guaranteed to be visible to B"

It is a **visibility and ordering guarantee** — not a timing guarantee.

---

## The Rule Broken Down

```java
volatile boolean ready = false;

// Thread A
ready = true;        // write to volatile V

// Thread B
if (ready == true) { // subsequent read of volatile V
    // guaranteed to see everything Thread A did before its write
}
```

Three words matter here:

| Word | Meaning |
|---|---|
| `"A write"` | any write to a volatile variable |
| `"happens-before"` | visibility guarantee established |
| `"subsequent read"` | any read that sees the NEW value |

---

## What "Subsequent" Really Means

This is the most misunderstood part.

**Subsequent does NOT mean "after in clock time".**

It means **"a read that sees the new value"**.

```
Thread A writes ready = true   at 10:00:00.001
Thread B reads  ready          at 10:00:00.002  → sees true  ✅ subsequent
Thread C reads  ready          at 10:00:00.003  → sees false ❌ NOT subsequent
                                                   (maybe cached old value)
```

Thread C sees `false` even though it read **after** Thread A wrote `true`.
Thread C is NOT a subsequent read — happens-before does NOT apply to Thread C.

---

## Subsequent = "Sees The New Value"

```
Timeline ──────────────────────────────────────────────────────►

Thread A  ───────── writes ready = true ──────────────────────
                            │
                            │ happens-before
                            ▼
Thread B  ───────────────── reads ready → sees TRUE  ✅
                            │             (subsequent read)
                            │             happens-before applies
                            ▼
                    sees ALL of Thread A's
                    writes before ready = true


Thread C  ───────────────── reads ready → sees FALSE ❌
                                          (NOT subsequent)
                                          happens-before does NOT apply
                                          a, b, x could be stale
```

---

## Full Example

```java
class SharedData {
    int a = 0;                   // normal variable
    int b = 0;                   // normal variable
    volatile boolean ready = false;

    // Thread A
    void writer() {
        a = 100;                 // write 1
        b = 200;                 // write 2
        ready = true;            // volatile write — commit point
    }

    // Thread B
    void reader() {
        if (ready == true) {     // sees new value → subsequent read
                                 // happens-before applies ✅
            print(a);            // guaranteed 100
            print(b);            // guaranteed 200
        }
    }

    // Thread C
    void otherReader() {
        if (ready == false) {    // sees OLD value → NOT subsequent
                                 // happens-before does NOT apply ❌
            print(a);            // could be 0 or 100 — no guarantee
            print(b);            // could be 0 or 200 — no guarantee
        }
    }
}
```

---

## Why This Makes Sense Intuitively

Think of it like a **newspaper publication**:

```
Journalist (Thread A)
─────────────────────
writes article draft    → a = 100  (non volatile)
writes headline         → b = 200  (non volatile)
publishes newspaper     → ready = true  (volatile write — commit point)


Reader (Thread B)
─────────────────
picks up newspaper      → reads ready = true  (subsequent read)
                                ↓
                         happens-before applies
                                ↓
reads article           → sees correct draft   ✅
reads headline          → sees correct headline ✅


Reader (Thread C)
─────────────────
checks newsstand        → newspaper not there yet → ready = false
                                ↓
                         NOT subsequent read
                                ↓
no guarantee about article or headline ❌
```

If you **see the published newspaper**, you are guaranteed to see everything written before it was published. If you check before it is published, no guarantees.

---

## The Formal Chain

```
a = 100          ─┐
b = 200          ─┤  program order
ready = true     ─┘
     │
     │  happens-before  (volatile write → volatile read)
     ▼
read ready == true
     │
     │  happens-before  (transitive)
     ▼
read a → 100 ✅
read b → 200 ✅
```

Happens-before is **transitive** — if A happens-before B, and B happens-before C, then A happens-before C.

---

## Summary

| Term | Meaning |
|---|---|
| `happens-before` | visibility guarantee — not time ordering |
| `subsequent read` | a read that sees the NEW volatile value |
| `not subsequent` | a read that sees the OLD volatile value — no guarantee |
| guarantee scope | ALL writes before volatile write are visible to subsequent reader |

```
See new volatile value?
        ↓ Yes                         ↓ No
happens-before applies          no guarantee
all prior writes visible        stale values possible
```

The entire guarantee **hinges on whether the reader sees the new value**. That is the trigger that establishes the happens-before relationship.


---

# Why Thread C Reads Old Volatile Value

---

## You Are Right — Let Me Be Precise

If Thread A **completed** its volatile write before Thread C reads, Thread C **will** see the new value. That is exactly what happens-before guarantees.

---

## The Real Scenario Where Thread C Sees Old Value

Thread C sees `false` only when it reads **before** Thread A's write completes.

```
Timeline ──────────────────────────────────────────────────────►

Thread A  ────────────────────────── writes ready = true
                                             ↑
Thread C  ── reads ready → false ────────────┘
              (reads here, before A's write)
```

Thread C lost the race — it read `ready` **before** Thread A wrote `true`. So it genuinely sees the old value — not a stale cache issue, just real timing.

---

## What The Misleading Example Looked Like

```
Thread A writes ready = true   at 10:00:00.001
Thread C reads  ready          at 10:00:00.003  → sees false ❌
```

This is **misleading** — if Thread C reads at `.003` and Thread A wrote at `.001`, Thread C **will** see `true`. Clock time after the write = sees new value.

The correct way to think about it:

```
Thread C sees false
        ↓
means Thread C read BEFORE Thread A's write
        ↓
NOT that Thread C read after but got stale value
```

---

## Volatile Prevents Stale Cache — It Does Not Control Race Timing

```
volatile guarantees:
✅ once a write happens, it is immediately visible to all threads
✅ no thread reads a cached old value after the write occurred

volatile does NOT guarantee:
❌ which thread reads first
❌ preventing a thread from reading before a write happens
```

---

## The Corrected Timeline

```
Scenario 1 — Thread C reads BEFORE Thread A writes
─────────────────────────────────────────────────
Thread C reads  ready = false  ← real old value, write hasn't happened
Thread A writes ready = true   ← too late for Thread C

Thread C sees false — timing issue, NOT a volatile failure ✅


Scenario 2 — Thread C reads AFTER Thread A writes
──────────────────────────────────────────────────
Thread A writes ready = true   ← volatile flushes immediately
Thread C reads  ready = true   ← guaranteed to see true ✅

volatile working correctly
```

---

## Bottom Line

| Situation | Thread C sees | Reason |
|---|---|---|
| Thread C reads before Thread A writes | `false` | Real timing — write hasn't happened yet |
| Thread C reads after Thread A writes | `true` | Volatile guarantees visibility |
| Thread C reads after Thread A writes but sees `false` | **impossible** | Volatile prevents this |

The only way Thread C sees `false` is if it genuinely raced ahead and read before Thread A wrote. Volatile eliminates stale cache — it cannot eliminate reading before a write physically happens.