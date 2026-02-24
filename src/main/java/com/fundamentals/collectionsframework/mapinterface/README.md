# Map Interface

Order - Some implementations maintain insertion order(LinkedHashMap), natural order(TreeMap), or no order(HashMap)

## HashMap

- Unordered: Does not maintain any order of its elements
- Allows null keys and Values: Can have one null key and multiple nill values
- Not synchronized: Not thread-safe; requires external synchronization if used in a multi-threaded context
- Performance: Offers constant-time performance O(1) for basic operations like get and put, assuming the hash function disperses elements properly

## LinkedHashMap