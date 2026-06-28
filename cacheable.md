# CodeWar — Caching Documentation & Developer Guide

This document serves as a complete developer guide for the Redis caching implementation in CodeWar. It covers configuration details, annotation mappings, serialization fixes, and troubleshooting guides for common Hibernate-Redis caching issues.

---

## 1. Cache Configuration (`CacheConfig.java`) Explained

Below is a detailed breakdown of what every key term in `CacheConfig.java` means and why it is used.

### Core Annotations
*   **`@Configuration`**: Tells Spring Boot that this class is a source of bean definitions. Spring will process the class and generate Spring Beans to manage the application's configuration at startup.
*   **`@Bean`**: Tells Spring that the method returns an object that should be registered as a bean in the Spring Application Context. The method name (`cacheManager`) becomes the bean's ID.

### Core Classes & Objects
*   **`RedisCacheManager`**: The central controller for caching in Spring Boot when using Redis. It creates, configures, and manages different caches (like `users`, `rooms`, etc.).
*   **`RedisConnectionFactory`**: A Spring abstraction interface that establishes connection endpoints with Redis (in our case, connecting to the Docker container `redis-cache` on port `6379`).
*   **`GenericJacksonJsonRedisSerializer`**: A serializer that converts Java objects into JSON strings before storing them in Redis, and back into Java objects when retrieved. It is the modern replacement for the deprecated `GenericJackson2JsonRedisSerializer`.
*   **`GenericJacksonJsonRedisSerializer.builder()`**: Creates a configuration builder for the JSON serializer to customize Jackson settings.
*   **`enableUnsafeDefaultTyping()`**: Tells the serializer to write class metadata (e.g., `"@class": "com.project.CodeWar.entity.User"`) directly into the JSON string stored in Redis. Without this, Jackson would not know which class to instantiate when reading the JSON back, resulting in a generic `LinkedHashMap` and throwing a `ClassCastException` in your services.
*   **`RedisCacheConfiguration`**: Defines caching parameters like serialized value format, keys prefixes, and TTL (Time to Live).
*   **`defaultCacheConfig()`**: Starts with the default Spring Cache configuration settings.
*   **`serializeValuesWith(...)`**: Configures the serializer (our configured `jsonSerializer`) that Redis will use to encode/decode the cache values.
*   **`Duration`**: A Java Time class used to specify time periods (e.g., `Duration.ofMinutes(15)` or `Duration.ofHours(24)`).
*   **`entryTtl(...)`**: Sets the Time-To-Live (expiry time) for cached entries in Redis.
*   **`initialCacheConfigs`**: A `Map` that associates specific cache names with custom TTL settings.
*   **`withInitialCacheConfigurations(...)`**: Binds the custom cache configurations (our map) to the `RedisCacheManager` build process.

---

## 2. Caching Annotations Table

Here is where each cache annotation is used across the service implementations:

### User Cache (`users`, `userDTOs`)

| Method Name | Annotation | Cache Name | Cache Key |
| :--- | :--- | :--- | :--- |
| `getUserById(Long id)` | `@Cacheable` | `userDTOs` | `'id::' + #id` |
| `findByUsername(String username)` | `@Cacheable` | `users` | `'username::' + #username` |
| `findByEmail(String email)` | `@Cacheable` | `users` | `'email::' + #email` |
| `getUserEntityById(Long id)` | `@Cacheable` | `users` | `'id::' + #id` |
| `updatePassword(...)` | `@CacheEvict` (allEntries) | `users`, `userDTOs` | N/A |
| `updateUserRole(...)` | `@CacheEvict` (allEntries) | `userDTOs`, `users` | N/A |
| `updateAccountLockStatus(...)` | `@CacheEvict` (allEntries) | `userDTOs`, `users` | N/A |
| `updateAccountExpiryStatus(...)` | `@CacheEvict` (allEntries) | `users`, `userDTOs` | N/A |
| `updateAccountEnabledStatus(...)` | `@CacheEvict` (allEntries) | `users`, `userDTOs` | N/A |
| `updateCredentialsExpiryStatus(...)` | `@CacheEvict` (allEntries) | `users`, `userDTOs` | N/A |
| `resetPassword(...)` | `@CacheEvict` (allEntries) | `users`, `userDTOs` | N/A |
| `registerUser(...)` | `@CacheEvict` (allEntries) | `users`, `userDTOs` | N/A |

### Room Cache (`rooms`)

| Method Name | Annotation | Cache Name | Cache Key |
| :--- | :--- | :--- | :--- |
| `getRoomByCode(String roomCode)` | `@Cacheable` | `rooms` | `#roomCode` |
| `getRoomsByUser(Long userId)` | `@Cacheable` | `rooms` | `'user::' + #userId` |
| `createRoom(Long userId)` | `@CacheEvict` (allEntries) | `rooms` | N/A |
| `joinRoom(String roomCode, Long userId)` | `@CacheEvict` (allEntries) | `rooms` | N/A |
| `updateRoomStatus(...)` | `@CacheEvict` (allEntries) | `rooms` | N/A |

### Role Cache (`roles`)

| Method Name | Annotation | Cache Name | Cache Key |
| :--- | :--- | :--- | :--- |
| `findRoleByName(AppRole role)` | `@Cacheable` | `roles` | `#role` |
| `getAllRoles()` | `@Cacheable` | `roles` | `'all'` |

### Codeforces Ratings Cache (`cfRatings`)

| Method Name | Annotation | Cache Name | Cache Key |
| :--- | :--- | :--- | :--- |
| `getUserRatingByUserId(Long userId)` | `@Cacheable` | `cfRatings` | `#userId` |
| `generateVerificationToken(...)` | `@CacheEvict` | `cfRatings` | `#userId` |
| `verifyHandle(Long userId)` | `@CacheEvict` | `cfRatings` | `#userId` |
| `unlinkHandle(Long userId)` | `@CacheEvict` | `cfRatings` | `#userId` |

### Codeforces Problemset Cache (`cfProblemset`)

| Method Name | Annotation | Cache Name | Cache Key |
| :--- | :--- | :--- | :--- |
| `getProblemset()` | `@Cacheable` | `cfProblemset` | `'all'` |

### Contest & Leaderboard Caches (`contests`, `leaderboards`)

| Method Name | Annotation | Cache Name | Cache Key |
| :--- | :--- | :--- | :--- |
| `getContestDetails(Long contestId)` | `@Cacheable` | `contests` | `#contestId` |
| `getContestsByRoom(String roomCode)` | `@Cacheable` | `contests` | `'room::' + #roomCode` |
| `getLeaderboard(Long contestId)` | `@Cacheable` | `leaderboards` | `#contestId` |
| `startContest(String roomCode)` | `@CacheEvict` (allEntries) | `contests`, `leaderboards` | N/A |
| `checkSubmission(Long contestId)` | `@CacheEvict` (allEntries) | `contests`, `leaderboards` | N/A |
| `endContest(Long contestId)` | `@CacheEvict` (allEntries) | `contests`, `leaderboards` | N/A |

---

## Why We Avoided `@CachePut` on Mutations (Important Design Choice)

For mutative methods (like registering a user, creating a room, starting a contest, or updating ratings), we chose `@CacheEvict(allEntries = true)` instead of `@CachePut`. Here is why:

### 1. The "Multiple Key Variation" Problem (Users & CF Ratings)
We cache user entities and ratings under multiple different lookup keys:
*   `User` is cached by `id`, `username`, and `email`.
*   `CfUser` is cached by `userId` (and linked to a handle).

If we used `@CachePut`, we could only update **one** cache key (e.g., the ID). The other cache keys (e.g., username/email) would remain stale or blank. By using `@CacheEvict`, we clear all cached entries for that entity type, forcing any subsequent queries to fetch the fresh, verified state from the database.

### 2. The "Cached List vs. Single Object" Type Mismatch (Rooms & Contests)
We cache lists of objects, such as:
*   `List<Room>` for a user (`rooms::user::{userId}`).
*   `List<Map<String, Object>>` of contests in a room (`contests::room::{roomCode}`).

If a user creates a room, that method returns a single `Room` object. If we used `@CachePut` to update the user's room list cache, it would overwrite the cached `List<Room>` with a single `Room` object. The next time the code tries to retrieve the list, it will fail with a `ClassCastException`. Since you cannot append items to a cached collection inside Redis using standard Spring annotations, evicting the cache is the only safe option.

### 3. Database-Generated Identity Keys
When registering a user or creating a room, the object does not have an ID yet because the database generates the key using an `AUTO_INCREMENT` column. Thus, we cannot compute a cache key containing the new ID during the call itself. We must let the database create it and evict the cache.

---

## 3. Entity Changes & Serialization Fixes

### A. Implementing `java.io.Serializable`
We made the following classes implement the standard Java `java.io.Serializable` interface and added `serialVersionUID = 1L` to each:
*   Entities: `Room`, `Contest`, `RoomProblem`
*   DTOs: `CfUser`, `CfProblem`, `CfProblemsetResponse`

**Why?**
Java objects cannot be written directly into bytes or network streams. Implementing `Serializable` is the standard contract required by caching systems to state that it is safe to parse and output these object representations.

### B. Removing Double `@JsonBackReference` (The User-Role Fix)
Originally, both `User.role` and `Role.users` were annotated with `@JsonBackReference`.

*   **Lombok `toString()` Warning:** Lombok's `@Data` annotation generates a `toString()` method automatically. If a `User` references a `Role`, and that `Role` references the list of its `User`s, calling `toString()` on either class triggers an infinite recursive loop, ending in a `StackOverflowError`. Adding `@ToString.Exclude` on the child side is used to break this loop.
*   **The Double Back-Reference Bug:** `@JsonBackReference` is a Jackson annotation that stops fields from being written into JSON. Having it on **both** sides meant the `role` field was completely omitted when saving a `User` to Redis. When deserialized from the cache, the user's role came back as `null`, leading to a `NullPointerException` during authentication.
*   **The Solution:** We removed `@JsonBackReference` from `User.java`. Now, Jackson successfully serializes the `role` inside the `User` object, but avoids infinite loops because `Role.users` still retains its `@JsonBackReference`.

---

## 4. The Persistent Collection / Lazy Loading Issue Explained

### What is a `PersistentBag`?
In JPA/Hibernate, when you load an entity (like a `Room`) that has relationships (like `List<User> participants`), Hibernate does not use standard Java lists like `ArrayList` under the hood. 

Instead, it wraps the list inside a Hibernate-specific class called **`PersistentBag`**. This wrapper is a "smart proxy" that allows Hibernate to check if the collection has been loaded (Lazy Loading) and monitor if entries are added or removed so it can synchronize them with the database.

### Why does it crash when cached in Redis?
1.  **The Serializer Stores the Proxy:** Because we enabled default typing in Redis (`enableUnsafeDefaultTyping()`), Jackson writes the actual runtime class name into Redis:
    ```json
    "participants": ["org.hibernate.collection.spi.PersistentBag", [...]]
    ```
2.  **No No-Argument Constructor:** When you try to load the room, Jackson reads that class name and tries to instantiate `PersistentBag` from the JSON.
3.  **The Crash:** Unlike `ArrayList`, a `PersistentBag` cannot exist on its own; it requires a connection to an active Hibernate database session (`SessionImplementor`) during creation. Since the JSON deserializer operates outside of Hibernate's database transaction session, it throws the crash:
    `Cannot lazily initialize collection (no session)`

### How copying to `new ArrayList<>()` solved this (Detachment)
In `RoomServiceImpl.java`, we changed the methods to return a **detached** copy of the `Room`:

```java
Room detachedRoom = new Room();
detachedRoom.setParticipants(new ArrayList<>(room.getParticipants()));
```

*   **Copying the elements:** By wrapping `room.getParticipants()` inside `new ArrayList<>(...)`, we pull all the users out of Hibernate's wrapper and place them into a standard, clean Java `ArrayList`.
*   **Plain JSON in Redis:** Now, when Jackson serializes the detached room, it writes the collection type as `java.util.ArrayList`.
*   **Flawless Deserialization:** Since `ArrayList` is a standard Java class that does not depend on database connections, Jackson can easily instantiate it upon lookup, resolving the error!
