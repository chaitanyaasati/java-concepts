### Issues in VotingSystem1 Singleton Implementation
1. Not Thread-Safe (Critical)
   The getInstance() method has a classic race condition. If two threads simultaneously check INSTANCE == null before either creates the object, both will proceed to instantiate — resulting in two different instances being created.
   ```java
      //Thread A and Thread B can both pass this check at the same time
      if(INSTANCE == null){
          INSTANCE = new VotingSystem1(); // Both threads execute this
      }
   ```
2. No Volatile Keyword on INSTANCE
   Without volatile, due to CPU caching and instruction reordering, one thread might see a partially constructed object while another thread is still initializing it. This can lead to subtle, hard-to-reproduce bugs.
   ```java
      // Unsafe
      private static VotingSystem1 INSTANCE;

      // Safe
      private static volatile VotingSystem1 INSTANCE;
   ```