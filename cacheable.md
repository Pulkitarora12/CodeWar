# CodeWar — Caching Opportunities & Cacheable Components

This document outlines the performance-critical parts of the CodeWar application that can be optimized using caching (e.g., Redis, Spring Cache, or an in-memory Caffeine cache) to reduce database load and external API latency.

---

## 1. External API Calls (Codeforces Integration)

### 🔴 User Profile & Rating Lookup
* **Target Method**: `CodeforcesServiceImpl.getUserRating(String handle)`
* **Why Cache**: Rating and rank information on Codeforces changes infrequently. Fetching it via external HTTP request on every request is slow (typically taking 500ms - 1s) and makes the application vulnerable to Codeforces API rate limits.
* **Suggested Caching Strategy**: Cache by `handle` with a TTL of 10–30 minutes.
* **Eviction**: None needed (let TTL expire).

### 🟡 User Rating by Database User ID
* **Target Method**: `CodeforcesServiceImpl.getUserRatingByUserId(Long userId)`
* **Why Cache**: Bypasses both the MySQL join query for user verification and the subsequent Codeforces lookup.
* **Suggested Caching Strategy**: Cache by `userId` with a TTL of 10 minutes.
* **Eviction**: Evict when a handle is verified, unlinked, or updated.

---

## 2. Spring Security & Authentication

### 🟢 User Details Loading
* **Target Method**: `UserDetailsServiceImpl.loadUserByUsername(String username)`
* **Why Cache**: The security filter `AuthTokenFilter` executes this method on **every single API request** to authenticate the JWT token, generating a MySQL `SELECT` query on the `users` and `roles` tables.
* **Suggested Caching Strategy**: Cache by `username` with a TTL of 10–15 minutes.
* **Eviction**: Evict when the user's role is updated, password is changed, or the account is locked/disabled.

---

## 3. Game Lobby & Room Metadata

### 🟢 Room Details
* **Target Method**: `RoomServiceImpl.getRoomByCode(String roomCode)`
* **Why Cache**: The room screen pages and WebSocket handlers query the room status and participant list repeatedly. 
* **Suggested Caching Strategy**: Cache by `roomCode` with a TTL of 5 minutes.
* **Eviction**: Evict immediately when a user joins the room, leaves the room, or the room status changes (e.g., contest starting or ending).

### 🟡 Room Problem History
* **Target Method**: `RoomProblemRepository.findByRoom(Room room)`
* **Why Cache**: Used to prevent duplicate problems from being selected for the same room. Can be cached in-memory to prevent hitting the database when compiling eligible problem lists.
* **Suggested Caching Strategy**: Cache by `room.getId()` with a TTL of 10 minutes.
* **Eviction**: Evict when a new `RoomProblem` is saved to the room.

---

## 4. Real-time Leaderboards & Submissions

### 🟡 Contest Leaderboard
* **Target Method**: `ContestServiceImpl.getLeaderboard(Long contestId)`
* **Why Cache**: Calculates rankings by performing multiple DB fetches on the `scores` and `submissions` tables, sorting them in memory.
* **Suggested Caching Strategy**: Cache by `contestId` with a TTL of 5 minutes.
* **Eviction**: Evict immediately when a user achieves an `AC` (solved) submission in `checkSubmission`.
