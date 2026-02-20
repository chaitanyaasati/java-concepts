# Internal working of ArrayList

- Unlike a regular array, which has a fixed size, an ArrayList can grow and shrink as elements are added or removed. This dynamic resizing is acheived by creating a new array when the current array is full and copying the elements to the new array.
- Internally, the arraylist is implemented as an array of object references. When you add elements to an arraylist, you are essentially storing these elements in this internal array.
- When you create an ArrayList, it has an initial capacity(defaults to 10). The capacity refers to the size of the internal array that can hold elements before needing to resize.

## The for-each loop works only with things that can be iterated.

# In Java, that means objects that are:

- 1️⃣ Arrays
- 2️⃣ Objects that implement Iterable interface (i.e., Collections)


