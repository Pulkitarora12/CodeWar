# CodeWar ⚔️

CodeWar is a competitive coding battle platform where users can create rooms, join coding contests, verify their Codeforces accounts, compete with friends, and track leaderboards in real time.

---

# 🚀 Features

- JWT Authentication & Authorization
- Role-Based Access Control (Admin/User)
- Codeforces Handle Verification
- Coding Battle Rooms
- Contest Management System
- Automatic Problem Picking based on ratings
- Live Leaderboards
- Account Management for Admins
- Password Reset via Email
- Swagger API Documentation

---

# 🛠 Tech Stack

## Backend
- Spring Boot
- Spring Security
- JWT Authentication
- Spring Data JPA & Hibernate
- Spring Data Redis (Docker-based Redis cache)
- MySQL
- Swagger / OpenAPI

## Frontend
- React
- Vite
- Axios
- React Router

---

# ⚡ Caching Strategy (Redis)

CodeWar integrates Redis caching to optimize database queries, minimize heavy computations, and protect the system from hitting external API rate limits.

### Cached Data & TTLs:
*   **User Data (`users`, `userDTOs` - 15 mins):** Caches user lookups by ID, username, and email to speed up filter chain security checks on every request.
*   **Battle Rooms (`rooms` - 10 mins):** Caches room metadata and rooms by user ID to optimize lobby lookups.
*   **User Roles (`roles` - 24 hours):** Caches static application user role configurations.
*   **Codeforces Ratings (`cfRatings` - 10 mins):** Caches external API user profile fetches to prevent Codeforces API rate limit blocks.
*   **Codeforces Problemset (`cfProblemset` - 12 hours):** Caches the massive Codeforces problem pool list to instantly pick random battle problems.
*   **Contests & Leaderboards (`contests`, `leaderboards` - 5 mins / 2 mins):** Caches leaderboard scoreboards and lobby contest metadata.

> 💡 For a detailed breakdown of all caching decisions, Spring/Redis configuration terms, serialization fixes, and Hibernate proxy solutions, please refer to the [caching developer guide](cacheable.md).

---

# 📚 API Documentation

Swagger UI is available at:

```bash
http://localhost:8080/swagger-ui/index.html
```

OpenAPI documentation:

```bash
http://localhost:8080/v3/api-docs
```

---

# ⚙️ Installation

## Clone Repository

```bash
git clone https://github.com/your-username/CodeWar.git
cd CodeWar
```

---

# Backend Setup

## Configure MySQL

Update `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/codewar
    username: root
    password: yourpassword
```

## Run Backend

```bash
mvn spring-boot:run
```

Backend runs on:

```bash
http://localhost:8080
```

---

# Frontend Setup

```bash
cd codewar-frontend
npm install
npm run dev
```

Frontend runs on:

```bash
http://localhost:5173
```

---

# 🔑 Authentication

Most APIs require JWT authentication.

Add token in headers:

```bash
Authorization: Bearer YOUR_JWT_TOKEN
```

---

# 📌 Future Improvements

- Contest analytics
- Friend system
- Global rankings

---