## Collections Framework

![Collection Framework](../../../../resources/images/collection_framework.png)

- Anything that implements Iterable can be used in for-each loop.

```java
List<Integer> list = new ArrayList<>(List.of(1,2,3));

for (int num : list) {
    System.out.println(num);
}


LinkedList<String> list = new LinkedList<>(List.of("A","B","C"));

for (String s : list) {
        System.out.println(s);
}

Set<Integer> set = new HashSet<>(Set.of(1,2,3));

for (int num : set) {
        System.out.println(num);
}

Set<Integer> set = new TreeSet<>(Set.of(5,1,3));

for (int num : set) {
        System.out.println(num);
}

Things that CANNOT be used directly

Map is not Iterable (it doesn’t implement Iterable)

Map<String,Integer> map = new HashMap<>();
for (var x : map) {} // ❌ compile error
```