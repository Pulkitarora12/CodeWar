# Frontend vs Backend Implementation Status

Based on an analysis of the Spring Boot backend (`CodeWar/src/main/java/com/project/CodeWar/controller`) and the React frontend (`codewar-frontend/src/api`), here is an overview of what is currently implemented in the frontend and what is left to do.

## ✅ What's Done (Implemented in Frontend)

These backend controllers have their corresponding API calls set up in `codewar-frontend/src/api` and are integrated into the React application:

### 1. Authentication (`/api/auth`)
- [x] Sign In (`POST /public/signin`)
- [x] Sign Up (`POST /public/signup`)
- [x] Forgot Password (`POST /public/forgot-password`)
- [x] Reset Password (`POST /public/reset-password`)
- [x] Get Current User (`GET /user`)
- [x] Get Username (`GET /username`)
- *Pages Implemented:* `Login.jsx`, `Register.jsx`, `ForgotPassword.jsx`, `ResetPassword.jsx`, `OAuth2Redirect.jsx`.

### 2. Room Management (`/api/room`)
- [x] Create Room (`POST /create`)
- [x] Join Room (`POST /join/{roomCode}`)
- [x] Get Room Details (`GET /{roomCode}`)
- [x] Get User's Rooms (`GET /my-rooms`)
- [x] Update Room Status (`PUT /{roomCode}/status`)
- [x] Room Ratings & Settings (`GET /{roomCode}/ratings`, `GET /{roomCode}/problem-rating`)
- [x] Pick & Manage Problems (`POST /{roomCode}/pick-problem`, `GET /{roomCode}/problems`, `GET /{roomCode}/current-problem`)
- *Pages Implemented:* `Room.jsx`, `Join.jsx`, `Dashboard.jsx`, `RoomCard.jsx`.

### 3. Codeforces Integration (`/api/codeforces`)
- [x] Generate Link Token (`POST /generate-token`)
- [x] Verify Token (`POST /verify`)
- [x] Unlink Account (`DELETE /unlink`)
- [x] Check Status (`GET /status`)
- [x] Get User Rating (`GET /rating/user/{userId}`)
- *Pages Implemented:* Integrated within `Profile.jsx` and `Room.jsx`.

---

## ❌ What's Left (Not Implemented in Frontend)

The following backend functionalities exist but have no corresponding frontend API setup or UI components:

### 1. Contest System (`/api/contest`) - **High Priority**
There is no `api/contest.js` file, nor are there active UI components to handle the actual competition phase.
- [ ] **Start Contest:** `POST /api/contest/start/{roomCode}`
- [ ] **Check User Submission:** `POST /api/contest/{contestId}/check-submission` 
- [ ] **End Contest:** `POST /api/contest/{contestId}/end`
- [ ] **Leaderboard Viewing:** `GET /api/contest/{contestId}/leaderboard`
- *Required UI:* A Contest view page for participants to track time, submit solutions manually or sync them, and view real-time leaderboards.

### 2. Admin Dashboard (`/api/admin`) - **Medium Priority**
There is no `api/admin.js` file, nor is an admin interface available to manage users.
- [ ] **List All Users:** `GET /api/admin/getusers`
- [ ] **Get User Details:** `GET /api/admin/user/{id}`
- [ ] **Get System Roles:** `GET /api/admin/roles`
- [ ] **Update User Role:** `PUT /api/admin/update-role`
- [ ] **Manage Account Lock Status:** `PUT /api/admin/update-lock-status`
- [ ] **Manage Expiry/Enable Status:** `PUT /api/admin/update-expiry-status`, `PUT /api/admin/update-enabled-status`
- [ ] **Force Update User Password:** `PUT /api/admin/update-password`
- *Required UI:* An Administrative panel (e.g., `/admin/users`) accessible only by users with Admin roles.
