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
- Spring Data JPA
- MySQL
- Swagger / OpenAPI

## Frontend
- React
- Vite
- Axios
- React Router

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

- WebSocket-based real-time contests
- Live code editor
- Multi-platform coding support
- Contest analytics
- Friend system
- Global rankings

---

# 👨‍💻 Author

Pulkit Arora