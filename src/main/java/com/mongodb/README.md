# MongoDB Fundamentals

A comprehensive guide to core MongoDB concepts for developers.

---

## Table of Contents

1. [What is MongoDB?](#what-is-mongodb)
2. [Core Concepts](#core-concepts)
3. [Data Model](#data-model)
4. [CRUD Operations](#crud-operations)
5. [Query Operators](#query-operators)
6. [Indexes](#indexes)
7. [Aggregation Framework](#aggregation-framework)
8. [Schema Design](#schema-design)
9. [Transactions](#transactions)
10. [Replication & Sharding](#replication--sharding)

---

## What is MongoDB?

MongoDB is a **document-oriented NoSQL database** designed for scalability, flexibility, and high performance. Unlike relational databases that store data in rows and tables, MongoDB stores data as **BSON documents** (Binary JSON) inside **collections**.

Key characteristics:

- **Schema-less** — Documents in the same collection can have different fields
- **Horizontally scalable** — Built-in sharding distributes data across multiple servers
- **Rich query language** — Supports filtering, projection, sorting, and complex aggregations
- **High availability** — Replica sets provide automatic failover

---

## Core Concepts

| MongoDB Term   | Relational Equivalent | Description                                      |
|----------------|----------------------|--------------------------------------------------|
| Database       | Database             | A namespace that groups collections              |
| Collection     | Table                | A group of MongoDB documents                     |
| Document       | Row                  | A single record stored as BSON                   |
| Field          | Column               | A key-value pair inside a document               |
| `_id`          | Primary Key          | Auto-generated unique identifier per document    |
| Index          | Index                | A data structure that speeds up queries          |
| Replica Set    | —                    | A group of mongod instances for high availability|
| Shard          | —                    | A horizontal partition of data                   |

---

## Data Model

### Documents

MongoDB documents are JSON-like objects stored internally as **BSON**. They support rich, nested data structures.

```json
{
  "_id": "64a1f2b3c4e5d6f7a8b9c0d1",
  "name": "Alice Johnson",
  "age": 30,
  "email": "alice@example.com",
  "address": {
    "street": "123 Main St",
    "city": "Bangalore",
    "pincode": "560001"
  },
  "skills": ["MongoDB", "Node.js", "Python"],
  "isActive": true,
  "createdAt": "2024-01-15T10:30:00Z"
}
```

### Supported BSON Data Types

| Type        | Example                              |
|-------------|--------------------------------------|
| String      | `"hello"`                            |
| Integer     | `42`                                 |
| Double      | `3.14`                               |
| Boolean     | `true` / `false`                     |
| Date        | `ISODate("2024-01-15")`              |
| ObjectId    | `ObjectId("64a1f2b3...")`            |
| Array       | `["a", "b", "c"]`                    |
| Embedded Doc| `{ "city": "Bangalore" }`            |
| Null        | `null`                               |
| Binary      | Binary data (files, images)          |

### Embedding vs. Referencing

**Embedding** (denormalization) — store related data inside the same document:

```json
{
  "_id": 1,
  "title": "MongoDB Basics",
  "author": {
    "name": "Bob",
    "email": "bob@example.com"
  }
}
```

**Referencing** (normalization) — store related data in separate collections and link via `_id`:

```json
// posts collection
{ "_id": 1, "title": "MongoDB Basics", "authorId": 42 }

// users collection
{ "_id": 42, "name": "Bob", "email": "bob@example.com" }
```

**Rule of thumb:** Embed when data is frequently read together; reference when data is shared across many documents or grows unboundedly.

---

## CRUD Operations

### Create

```javascript
// Insert one document
db.users.insertOne({
  name: "Alice",
  age: 30,
  email: "alice@example.com"
});

// Insert multiple documents
db.users.insertMany([
  { name: "Bob", age: 25 },
  { name: "Carol", age: 35 }
]);
```

### Read

```javascript
// Find all documents
db.users.find({});

// Find with a filter
db.users.find({ age: { $gte: 25 } });

// Find one document
db.users.findOne({ email: "alice@example.com" });

// Projection — include only specific fields
db.users.find({ age: { $gte: 25 } }, { name: 1, email: 1, _id: 0 });

// Sort, skip, and limit
db.users.find({}).sort({ age: -1 }).skip(10).limit(5);
```

### Update

```javascript
// Update one document
db.users.updateOne(
  { email: "alice@example.com" },
  { $set: { age: 31 } }
);

// Update many documents
db.users.updateMany(
  { isActive: false },
  { $set: { archived: true } }
);

// Replace a document entirely
db.users.replaceOne(
  { _id: ObjectId("...") },
  { name: "Alice New", age: 32 }
);

// Upsert — insert if not found
db.users.updateOne(
  { email: "new@example.com" },
  { $set: { name: "New User" } },
  { upsert: true }
);
```

### Delete

```javascript
// Delete one document
db.users.deleteOne({ email: "alice@example.com" });

// Delete many documents
db.users.deleteMany({ isActive: false });
```

---

## Query Operators

### Comparison Operators

| Operator | Meaning                  | Example                          |
|----------|--------------------------|----------------------------------|
| `$eq`    | Equal                    | `{ age: { $eq: 30 } }`           |
| `$ne`    | Not equal                | `{ age: { $ne: 30 } }`           |
| `$gt`    | Greater than             | `{ age: { $gt: 18 } }`           |
| `$gte`   | Greater than or equal    | `{ age: { $gte: 18 } }`          |
| `$lt`    | Less than                | `{ age: { $lt: 65 } }`           |
| `$lte`   | Less than or equal       | `{ age: { $lte: 65 } }`          |
| `$in`    | In a list                | `{ age: { $in: [25, 30, 35] } }` |
| `$nin`   | Not in a list            | `{ age: { $nin: [25, 30] } }`    |

### Logical Operators

```javascript
// AND (implicit — multiple fields)
db.users.find({ age: { $gte: 18 }, isActive: true });

// AND (explicit)
db.users.find({ $and: [{ age: { $gte: 18 } }, { isActive: true }] });

// OR
db.users.find({ $or: [{ age: { $lt: 18 } }, { age: { $gt: 65 } }] });

// NOT
db.users.find({ age: { $not: { $gte: 18 } } });

// NOR
db.users.find({ $nor: [{ age: 25 }, { isActive: false }] });
```

### Element Operators

```javascript
// Field exists
db.users.find({ email: { $exists: true } });

// Field type check
db.users.find({ age: { $type: "int" } });
```

### Array Operators

```javascript
// Match documents where array contains a value
db.users.find({ skills: "MongoDB" });

// All values must be present
db.users.find({ skills: { $all: ["MongoDB", "Node.js"] } });

// Array size
db.users.find({ skills: { $size: 3 } });

// Element match (for arrays of objects)
db.orders.find({
  items: { $elemMatch: { product: "Laptop", qty: { $gte: 2 } } }
});
```

---

## Indexes

Indexes dramatically speed up read queries. Without an index, MongoDB does a **collection scan** (reads every document).

### Types of Indexes

```javascript
// Single field index (ascending)
db.users.createIndex({ email: 1 });

// Single field index (descending)
db.users.createIndex({ createdAt: -1 });

// Compound index — order matters!
db.users.createIndex({ lastName: 1, firstName: 1 });

// Unique index — enforces uniqueness
db.users.createIndex({ email: 1 }, { unique: true });

// Sparse index — only indexes documents that have the field
db.users.createIndex({ phone: 1 }, { sparse: true });

// TTL index — auto-deletes documents after a time period
db.sessions.createIndex({ createdAt: 1 }, { expireAfterSeconds: 3600 });

// Text index — full-text search
db.articles.createIndex({ content: "text" });

// Wildcard index — indexes all fields
db.products.createIndex({ "$**": 1 });
```

### Analyzing Query Performance

```javascript
// Use explain() to inspect query plans
db.users.find({ email: "alice@example.com" }).explain("executionStats");

// List all indexes on a collection
db.users.getIndexes();

// Drop an index
db.users.dropIndex("email_1");
```

**Key metrics to watch in `explain()`:**
- `COLLSCAN` → no index used (slow)
- `IXSCAN` → index used (fast)
- `totalDocsExamined` → lower is better
- `nReturned` → documents returned

---

## Aggregation Framework

The aggregation framework processes documents through a **pipeline** of stages, transforming data step by step.

### Common Pipeline Stages

| Stage       | Description                                      |
|-------------|--------------------------------------------------|
| `$match`    | Filters documents (like `find`)                  |
| `$group`    | Groups documents and computes aggregates         |
| `$project`  | Shapes the output — include, exclude, rename     |
| `$sort`     | Sorts documents                                  |
| `$limit`    | Limits the number of documents                   |
| `$skip`     | Skips a number of documents                      |
| `$lookup`   | Performs a left outer join with another collection|
| `$unwind`   | Deconstructs an array field into separate docs   |
| `$addFields`| Adds new computed fields                         |
| `$count`    | Counts documents                                 |
| `$facet`    | Runs multiple pipelines in parallel              |

### Examples

```javascript
// Total revenue per product category
db.orders.aggregate([
  { $match: { status: "completed" } },
  { $unwind: "$items" },
  { $group: {
      _id: "$items.category",
      totalRevenue: { $sum: { $multiply: ["$items.price", "$items.qty"] } },
      orderCount: { $sum: 1 }
  }},
  { $sort: { totalRevenue: -1 } },
  { $limit: 10 }
]);

// Join users with their orders
db.users.aggregate([
  { $lookup: {
      from: "orders",
      localField: "_id",
      foreignField: "userId",
      as: "orders"
  }},
  { $project: {
      name: 1,
      email: 1,
      orderCount: { $size: "$orders" }
  }}
]);

// Bucket users by age group
db.users.aggregate([
  { $bucket: {
      groupBy: "$age",
      boundaries: [0, 18, 30, 45, 60, 100],
      default: "Other",
      output: { count: { $sum: 1 } }
  }}
]);
```

---

## Schema Design

### Design Patterns

**1. Subset Pattern** — Store only the most-accessed subset of a large array in the parent document:

```json
{
  "_id": 1,
  "name": "Laptop",
  "topReviews": [ "...5 most recent..." ],
  "reviewCount": 342
}
```

**2. Bucket Pattern** — Group time-series data into buckets to reduce document count:

```json
{
  "sensorId": "temp-01",
  "date": "2024-01-15",
  "readings": [
    { "ts": "2024-01-15T00:00:00Z", "value": 22.3 },
    { "ts": "2024-01-15T00:01:00Z", "value": 22.5 }
  ],
  "count": 1440
}
```

**3. Computed Pattern** — Pre-compute expensive aggregations and store the result:

```json
{
  "_id": "product-42",
  "name": "Wireless Keyboard",
  "avgRating": 4.7,
  "totalSales": 1523
}
```

**4. Polymorphic Pattern** — Store different shapes of documents in the same collection using a type discriminator:

```json
{ "_id": 1, "type": "book",  "title": "MongoDB Guide", "pages": 350 }
{ "_id": 2, "type": "video", "title": "MongoDB Course", "duration": 7200 }
```

---

## Transactions

MongoDB supports **multi-document ACID transactions** (since v4.0 for replica sets, v4.2 for sharded clusters).

```javascript
const session = client.startSession();

try {
  session.startTransaction({
    readConcern: { level: "snapshot" },
    writeConcern: { w: "majority" }
  });

  await db.accounts.updateOne(
    { _id: "account-A" },
    { $inc: { balance: -500 } },
    { session }
  );

  await db.accounts.updateOne(
    { _id: "account-B" },
    { $inc: { balance: 500 } },
    { session }
  );

  await session.commitTransaction();
  console.log("Transaction committed.");

} catch (error) {
  await session.abortTransaction();
  console.error("Transaction aborted:", error);

} finally {
  session.endSession();
}
```

> **Note:** Transactions add latency. Prefer denormalized schema designs that avoid cross-document writes when possible.

---

## Replication & Sharding

### Replica Sets

A **replica set** is a group of MongoDB instances that maintain the same data. It provides:

- **High availability** — automatic failover if the primary goes down
- **Data redundancy** — copies of data on multiple nodes
- **Read scaling** — read from secondaries (with eventual consistency)

```
Primary  <->  Secondary 1
    |
 Secondary 2  (Arbiter optional)
```

- **Primary** — accepts all write operations
- **Secondary** — replicates data from primary; can serve reads
- **Arbiter** — participates in elections but holds no data

### Sharding

**Sharding** horizontally distributes data across multiple servers (**shards**) to handle large datasets and high throughput.

```
Client -> mongos (router)
              |
     Config Servers (metadata)
     /         |         \
  Shard 1   Shard 2   Shard 3
```

**Shard key** — the field(s) used to distribute documents across shards.

```javascript
// Enable sharding on a database
sh.enableSharding("mydb");

// Shard a collection on the userId field
sh.shardCollection("mydb.orders", { userId: "hashed" });
```

**Shard key strategies:**
- **Hashed sharding** — even distribution, good for write-heavy workloads
- **Ranged sharding** — efficient range queries, risk of hotspots
- **Zone sharding** — route data to specific shards based on ranges (e.g., by geography)

---

## Quick Reference Cheatsheet

```javascript
// Database & Collection
use mydb
db.createCollection("users")
show collections

// CRUD
db.col.insertOne({})               // Create
db.col.find({})                    // Read
db.col.updateOne({}, { $set:{} })  // Update
db.col.deleteOne({})               // Delete

// Index
db.col.createIndex({ field: 1 })
db.col.getIndexes()

// Aggregation
db.col.aggregate([ { $match:{} }, { $group:{} } ])

// Admin
db.col.countDocuments({})
db.col.estimatedDocumentCount()
db.col.drop()
db.dropDatabase()
```

---

## Further Reading

- [Official MongoDB Documentation](https://www.mongodb.com/docs/)
- [MongoDB University (free courses)](https://learn.mongodb.com/)
- [MongoDB Schema Design Patterns](https://www.mongodb.com/blog/post/building-with-patterns-a-summary)
- [Aggregation Pipeline Reference](https://www.mongodb.com/docs/manual/reference/operator/aggregation-pipeline/)

---

*Last updated: May 2026*