# CodeWar — Caching Analysis

> This document maps every piece of data that can be cached, and tracks where each entity is currently being accessed
> directly from the repository (bypassing the service layer). Use this as a pre-refactoring checklist before adding
> `@Cacheable`, `@CachePut`, and `@CacheEvict` annotations.

---

## Part 1 — What Should Be Cached

### 1. User (entity: `User`, dto: `UserDTO`)
**Why cache?**
- Fetched on **every single authenticated HTTP request** via `AuthTokenFilter` → `UserDetailsServiceImpl.loadUserByUsername()`
- Also fetched multiple times per request through `AuthUtil.loggedInUser()` and `AuthUtil.loggedInUserId()`
- User profile data rarely changes — safe for short-to-medium TTL (e.g. 10–30 min)

**Data to cache:**

| Field(s) | Cache Key Suggestion |
|---|---|
| Full `User` entity by username | `users::username::{username}` |
| Full `User` entity by userId | `users::id::{userId}` |
| Full `User` entity by email | `users::email::{email}` |
| `UserDTO` (for admin view) | `userDTOs::id::{id}` |

**Evict on:**
`updatePassword()`, `updateUserRole()`, `updateAccountLockStatus()`, `updateAccountExpiryStatus()`,
`updateAccountEnabledStatus()`, `updateCredentialsExpiryStatus()`, `registerUser()`,
`generateVerificationToken()`, `verifyHandle()`, `unlinkHandle()`

---

### 2. Room (entity: `Room`)
**Why cache?**
- `getRoomByCode(roomCode)` is called by almost every room operation: get details, get problems, get current problem, start contest, etc.
- Room data is stable between mutations (create/join/status-change)

**Data to cache:**

| Field(s) | Cache Key Suggestion |
|---|---|
| `Room` by roomCode | `rooms::code::{roomCode}` |
| `List<Room>` by userId (user's rooms) | `rooms::user::{userId}` |

**Evict on:** `createRoom()`, `joinRoom()`, `updateRoomStatus()`, `endContest()` (resets room status)

---

### 3. Role (entity: `Role`)
**Why cache?**
- Roles are essentially static/enum-driven (`ROLE_USER`, `ROLE_ADMIN`)
- Fetched during user registration, role update, and security config seeding
- Almost never changes — ideal candidate for long TTL or startup-time load

**Data to cache:**

| Field(s) | Cache Key Suggestion |
|---|---|
| All roles list | `roles::all` |
| `Role` by `AppRole` enum name | `roles::name::{roleName}` |

**Evict on:** Almost never — only if new roles are added at runtime.

---

### 4. Codeforces User Rating / CF API Response (`CfUser`)
**Why cache?**
- `getUserRatingByUserId()` and `getUserRating()` make a **live HTTP call to the Codeforces API** per request
- Called for every participant in a room during contest start (loop over all participants)
- CF API has rate limits — caching is critical here

**Data to cache:**

| Field(s) | Cache Key Suggestion |
|---|---|
| `CfUser` (rating, rank) by CF handle | `cfRatings::handle::{handle}` |
| `CfUser` (rating, rank) by userId | `cfRatings::userId::{userId}` |

**Evict on:** `verifyHandle()` (handle changes), `unlinkHandle()`, or after TTL (5–15 min)

---

### 5. Codeforces Problemset (`List<CfProblem>`)
**Why cache?**
- `ProblemServiceImpl.pickProblemForRoom()` calls `https://codeforces.com/api/problemset.problems` on every contest start
- This returns thousands of problems — a massive external API payload
- CF problems are permanent and almost never removed
- **Highest-impact cache in the whole project**

**Data to cache:**

| Field(s) | Cache Key Suggestion |
|---|---|
| Full CF problemset list | `cfProblemset::all` |

**Evict on:** Time-based TTL only (e.g. 6–24 hours)

---

### 6. Contest (entity: `Contest`)
**Why cache?**
- `getLeaderboard()` fetches the contest by ID repeatedly (also called via WebSocket broadcast)
- `getContestDetails()` also fetches by ID
- Contests are read-heavy once started

**Data to cache:**

| Field(s) | Cache Key Suggestion |
|---|---|
| `Contest` by contestId | `contests::id::{contestId}` |
| `LeaderboardResponse` by contestId | `leaderboards::contestId::{contestId}` |

**Evict on:** `endContest()`, `checkSubmission()` (when a user solves → score changes → leaderboard changes)

---

### 7. Score & Submission (entities: `Score`, `Submission`)
**Why cache?**
- Leaderboard relies on fetching `List<Score>` and `List<Submission>` for the same contestId repeatedly
- During an active contest, `checkSubmission()` is polled frequently by all participants

**Data to cache:**

| Field(s) | Cache Key Suggestion |
|---|---|
| `List<Score>` by contestId | `scores::contestId::{contestId}` |
| `List<Submission>` by contestId | `submissions::contestId::{contestId}` |

**Evict on:** Any new submission save or score save for that contestId.

---

## Part 2 — Where Data Is Currently Accessed (Refactoring Map)

This table shows every place a repository is called **directly** instead of going through the service layer.
These spots **must be refactored to go through a service method first**, so `@Cacheable` annotations actually take effect.

---

### USER Data Access Map

| Method / Query | Accessed Via | Called In Class | Should Go Through |
|---|---|---|---|
| `userRepository.findByUserName(username)` | **REPO DIRECT** | `UserDetailsServiceImpl.loadUserByUsername()` | `UserService.findByUsername()` |
| `userRepository.findByUserName(username)` | **REPO DIRECT** | `AuthUtil.loggedInUser()` | `UserService.findByUsername()` |
| `userRepository.findByUserName(username)` | **REPO DIRECT** | `AuthUtil.loggedInUserId()` | `UserService.findByUsername()` |
| `userRepository.findById(userId)` | **REPO DIRECT** | `RoomServiceImpl.createRoom()` | `UserService.getUserEntityById()` *(new method)* |
| `userRepository.findById(userId)` | **REPO DIRECT** | `RoomServiceImpl.joinRoom()` | `UserService.getUserEntityById()` |
| `userRepository.findById(userId)` | **REPO DIRECT** | `RoomServiceImpl.getRoomsByUser()` | `UserService.getUserEntityById()` |
| `userRepository.findById(userId)` | **REPO DIRECT** | `CodeforcesServiceImpl.generateVerificationToken()` | `UserService.getUserEntityById()` |
| `userRepository.findById(userId)` | **REPO DIRECT** | `CodeforcesServiceImpl.verifyHandle()` | `UserService.getUserEntityById()` |
| `userRepository.findById(userId)` | **REPO DIRECT** | `CodeforcesServiceImpl.unlinkHandle()` | `UserService.getUserEntityById()` |
| `userRepository.findById(userId)` | **REPO DIRECT** | `CodeforcesServiceImpl.getUserRatingByUserId()` | `UserService.getUserEntityById()` |
| `userRepository.existsByUserName()` | **REPO DIRECT** | `AuthController` (signup check) | `UserService.existsByUsername()` *(new method)* |
| `userRepository.existsByEmail()` | **REPO DIRECT** | `AuthController` (signup check) | `UserService.existsByEmail()` *(new method)* |
| `userRepository.save(user)` | **REPO DIRECT** | `AuthController` (signup save) | `UserService.registerUser()` ✅ *(already exists)* |
| `userRepository.findByUserName(username)` | ✅ Service | `UserServiceImpl.findByUsername()` | Already in service |
| `userRepository.findByEmail(email)` | ✅ Service | `UserServiceImpl.findByEmail()` | Already in service |
| `userRepository.findById(id)` | ✅ Service | `UserServiceImpl.getUserById()` | Already in service |
| `userRepository.findById(userId)` | ✅ Service | `UserServiceImpl.updatePassword()` | Already in service |
| `userRepository.findById(userId)` | ✅ Service | `UserServiceImpl.updateUserRole()` | Already in service |

---

### ROOM Data Access Map

| Method / Query | Accessed Via | Called In Class | Should Go Through |
|---|---|---|---|
| `roomRepository.findByRoomCode(roomCode)` | ✅ Service | `RoomServiceImpl.joinRoom()` | Already in service |
| `roomRepository.findByRoomCode(roomCode)` | ✅ Service | `RoomServiceImpl.getRoomByCode()` | Already in service |
| `roomRepository.findByRoomCode(roomCode)` | ✅ Service | `RoomServiceImpl.updateRoomStatus()` | Already in service |
| `roomRepository.findByRoomCode(roomCode)` | ✅ Service | `RoomServiceImpl.getRoomParticipantsRatings()` | Already in service |
| `roomRepository.findByRoomCode(roomCode)` | ✅ Service | `RoomServiceImpl.calculateProblemRating()` | Already in service |
| `roomRepository.findByRoomCode(roomCode)` | ✅ Service | `ContestServiceImpl.startContest()` | Already in service |
| `roomRepository.findByRoomCode(roomCode)` | ✅ Service | `ContestServiceImpl.getContestsByRoom()` | Already in service |
| `roomRepository.findByRoomCode(roomCode)` | ✅ Service | `ProblemServiceImpl.pickProblemForRoom()` | Already in service |
| `roomProblemRepository.findByRoom(room)` | **REPO DIRECT** | `RoomController.getRoomProblems()` | `ProblemService.getRoomProblems(roomCode)` *(new method)* |
| `roomProblemRepository.findTopByRoomOrderByAssignedAtDesc(room)` | **REPO DIRECT** | `RoomController.getCurrentProblem()` | `ProblemService.getCurrentProblem(roomCode)` *(new method)* |

---

### ROLE Data Access Map

| Method / Query | Accessed Via | Called In Class | Should Go Through |
|---|---|---|---|
| `roleRepository.findAll()` | **REPO DIRECT** | `AdminController.getAllRoles()` | `UserService.getAllRoles()` *(new method)* |
| `roleRepository.findByRoleName(appRole)` | ✅ Service | `UserServiceImpl.updateUserRole()` | Already in service |
| `roleRepository.findByRoleName(appRole)` | **REPO DIRECT** | `AuthController` (role assignment during signup) | `UserService.findRoleByName()` *(new method)* |

---

### CONTEST Data Access Map

| Method / Query | Accessed Via | Called In Class | Should Go Through |
|---|---|---|---|
| `contestRepository.findById(contestId)` | ✅ Service | `ContestServiceImpl.checkSubmission()` | Already in service |
| `contestRepository.findById(contestId)` | ✅ Service | `ContestServiceImpl.endContest()` | Already in service |
| `contestRepository.findById(contestId)` | ✅ Service | `ContestServiceImpl.getLeaderboard()` | Already in service |
| `contestRepository.findById(contestId)` | ✅ Service | `ContestServiceImpl.getContestDetails()` | Already in service |
| `contestRepository.findByStatus(ACTIVE)` | ✅ Service | `ContestServiceImpl.reScheduleActiveContests()` | Already in service |

---

### SCORE Data Access Map

| Method / Query | Accessed Via | Called In Class | Should Go Through |
|---|---|---|---|
| `scoreRepository.findByContestId(contestId)` | ✅ Service | `ContestServiceImpl.getLeaderboard()` | Already in service |
| `scoreRepository.findByContestAndUser(contest, user)` | ✅ Service | `ContestServiceImpl.endContest()` | Already in service |
| `scoreRepository.findByContestAndUser(contest, user)` | ✅ Service | `ContestServiceImpl.saveScore()` (private) | Already in service |

---

### SUBMISSION Data Access Map

| Method / Query | Accessed Via | Called In Class | Should Go Through |
|---|---|---|---|
| `submissionRepository.findByContestAndUser()` | ✅ Service | `ContestServiceImpl.checkSubmission()` | Already in service |
| `submissionRepository.findByContestId()` | ✅ Service | `ContestServiceImpl.checkSubmission()` | Already in service |
| `submissionRepository.findByContestId()` | ✅ Service | `ContestServiceImpl.getLeaderboard()` | Already in service |

---

### CODEFORCES API (External) Access Map

| Method / Query | Accessed Via | Called In Class | Cache? |
|---|---|---|---|
| CF `user.info` API — get rating by handle | ✅ Service | `CodeforcesServiceImpl.getUserRating(handle)` | ✅ Add `@Cacheable` here |
| CF `user.info` API — get rating by userId | ✅ Service | `CodeforcesServiceImpl.getUserRatingByUserId()` | ✅ Add `@Cacheable` here |
| CF `user.status` API — get recent submissions | ✅ Service | `CodeforcesServiceImpl.getRecentSubmissions()` | ❌ Do NOT cache — needs fresh data for submission check |
| CF `problemset.problems` API — full list | ✅ Service | `ProblemServiceImpl.pickProblemForRoom()` | ✅ Add `@Cacheable` here |

---

## Part 3 — Refactoring Checklist (Do This Before Adding Cache)

- [ ] **`AuthController`** — Replace `userRepository.existsByUserName()` with `userService.existsByUsername()`
- [ ] **`AuthController`** — Replace `userRepository.existsByEmail()` with `userService.existsByEmail()`
- [ ] **`AuthController`** — Replace `roleRepository.findByRoleName()` with `userService.findRoleByName()`
- [ ] **`AdminController`** — Replace `roleRepository.findAll()` with `userService.getAllRoles()`
- [ ] **`UserDetailsServiceImpl`** — Replace `userRepository.findByUserName()` with `userService.findByUsername()`
- [ ] **`AuthUtil.loggedInUser()`** — Replace `userRepository.findByUserName()` with `userService.findByUsername()`
- [ ] **`AuthUtil.loggedInUserId()`** — Replace `userRepository.findByUserName()` with `userService.findByUsername()`
- [ ] **`RoomServiceImpl.createRoom()`** — Replace `userRepository.findById()` with `userService.getUserEntityById()`
- [ ] **`RoomServiceImpl.joinRoom()`** — Replace `userRepository.findById()` with `userService.getUserEntityById()`
- [ ] **`RoomServiceImpl.getRoomsByUser()`** — Replace `userRepository.findById()` with `userService.getUserEntityById()`
- [ ] **`CodeforcesServiceImpl.generateVerificationToken()`** — Replace `userRepository.findById()` with `userService.getUserEntityById()`
- [ ] **`CodeforcesServiceImpl.verifyHandle()`** — Replace `userRepository.findById()` with `userService.getUserEntityById()`
- [ ] **`CodeforcesServiceImpl.unlinkHandle()`** — Replace `userRepository.findById()` with `userService.getUserEntityById()`
- [ ] **`CodeforcesServiceImpl.getUserRatingByUserId()`** — Replace `userRepository.findById()` with `userService.getUserEntityById()`
- [ ] **`RoomController.getRoomProblems()`** — Replace `roomProblemRepository.findByRoom()` with `problemService.getRoomProblems(roomCode)`
- [ ] **`RoomController.getCurrentProblem()`** — Replace `roomProblemRepository.findTopBy...()` with `problemService.getCurrentProblem(roomCode)`

---

## Part 4 — New Service Methods to Add

| Interface | New Method Signature | Purpose |
|---|---|---|
| `UserService` | `User getUserEntityById(Long id)` | Return raw `User` entity (not DTO) for internal service use |
| `UserService` | `boolean existsByUsername(String username)` | Centralise username existence check |
| `UserService` | `boolean existsByEmail(String email)` | Centralise email existence check |
| `UserService` | `Role findRoleByName(AppRole role)` | Centralise role lookup for registration |
| `UserService` | `List<Role> getAllRoles()` | Centralise role listing for admin endpoint |
| `ProblemService` | `List<RoomProblem> getRoomProblems(String roomCode)` | Move repo call out of RoomController |
| `ProblemService` | `Optional<RoomProblem> getCurrentProblem(String roomCode)` | Move repo call out of RoomController |

---

## Part 5 — Cache Configuration Summary

Once all refactoring above is done, configure these caches in Redis / Spring Cache:

| Cache Name | TTL | Eviction Strategy |
|---|---|---|
| `users` | 15 min | On any user mutation (update, save) |
| `userDTOs` | 15 min | On any user mutation |
| `rooms` | 10 min | On create / join / status update |
| `roles` | 24 hours | Application startup only |
| `cfRatings` | 10 min | On handle link / unlink or TTL |
| `cfProblemset` | 12 hours | Time-based TTL only |
| `contests` | 5 min | On contest end |
| `leaderboards` | 2 min | On each solved submission (score change) |
| `scores` | 2 min | On each score save |
| `submissions` | 2 min | On each submission save |
