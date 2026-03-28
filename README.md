# CodeWar ⚔️

**CodeWar** is a real-time, gamified competitive programming platform that allows developers to compete in synchronized, head-to-head coding challenges. By integrating with the **Codeforces API**, the platform automates problem discovery and submission verification, providing a seamless "competitive room" experience with live leaderboards and contest history.

## 🚀 Core Features

* **Real-Time Lobby System:** Synchronized room management using **WebSockets (STOMP)** for instant participant updates and contest transitions.
* **Dynamic Difficulty Scaling:** A custom algorithm that calculates the optimal problem difficulty (800–3000 rating) by averaging the verified Codeforces ratings of all room participants.
* **Automated Verification:** A secure "handshake" system that verifies Codeforces handles by matching unique tokens within the user's external profile.
* **Live Competition Engine:**
    * **Auto-Grading:** Real-time submission fetching from Codeforces to verify "OK" (Accepted) verdicts.
    * **Scoring Logic:** Points are calculated based on speed and accuracy: $100 - (failed\_attempts \times 5)$.
    * **Tie-Breaking:** Leaderboards are ranked by highest score, then by the lowest time taken to solve.
    * **Contest History:** Users can view their past contest performance, access older leaderboards, and review submissions.
* **Secure Authentication:** Stateless security using **JWT** and **Spring Security**, featuring a multi-step password reset workflow with **JavaMailSender**.

<img width="1514" height="901" alt="Screenshot 2026-03-27 224312" src="https://github.com/user-attachments/assets/0cc49d00-dd57-446c-932c-01c8173b131e" />
<img width="1710" height="924" alt="Screenshot 2026-03-28 181050" src="https://github.com/user-attachments/assets/7d0732d5-98f6-43d2-abd7-8d5df5dc09af" />
<img width="1715" height="917" alt="Screenshot 2026-03-28 181119" src="https://github.com/user-attachments/assets/e304e6a9-63e9-41b1-ab80-df3dc55fb5b7" />
<img width="1671" height="891" alt="Screenshot 2026-03-28 181141" src="https://github.com/user-attachments/assets/d30b51e4-882a-463a-ba1e-27e796544f18" />
<img width="1749" height="935" alt="Screenshot 2026-03-28 181156" src="https://github.com/user-attachments/assets/fcd53f5e-6669-4cdd-bb31-1bc1c8f8ae01" />
<img width="1754" height="927" alt="Screenshot 2026-03-28 181212" src="https://github.com/user-attachments/assets/9afe9f64-33fb-46a8-b517-cc8b5c8e7eb0" />


## 🛠️ Tech Stack

### Frontend
* **Framework:** React 18, Vite
* **Styling:** Tailwind CSS
* **Real-Time:** SockJS, StompJS
* **Routing:** React Router DOM

### Backend
* **Core:** Java 21, Spring Boot, Spring Security, JPA/Hibernate.
* **Real-Time:** Spring WebSocket (STOMP), Message Broker.
* **Database:** MySQL.
* **External APIs:** Codeforces API (user.info, user.status, problemset.problems).
* **Security:** JSON Web Tokens (JWT), BCrypt, OAuth2.

## 🏗️ System Architecture

### 1. Authentication & Identity
Handles user registration, JWT-based login, and administrative oversight for account status (locking/expiring).

### 2. Codeforces Linkage
Uses a non-invasive verification flow:
1.  System generates a unique `cw-xxxx` token.
2.  User updates their Codeforces "First Name".
3.  System validates the token via `user.info` API to mark the account as verified.

### 3. Room & Contest Workflow
* **Lobby:** Users join via unique room codes (e.g., `CW-A1B2C3`).
* **Contest Start:** The system calculates the group's average rating and picks a matching problem.
* **Monitoring:** The backend periodically checks the participant's Codeforces status for the specific problem ID.
* **WebSocket Updates:** Whenever a user solves a problem, the updated leaderboard is broadcasted to all participants.

## 🚦 Getting Started

### Prerequisites
* **Backend:** JDK 21+, Maven 3.8+, MySQL 8.0+
* **Frontend:** Node.js 18+, npm

### Setup

1. **Clone the repository:**
   ```bash
   git clone https://github.com/Pulkitarora12/CodeWar.git
   ```

2. **Backend Setup:**
   * Navigate to the backend directory:
     ```bash
     cd CodeWar/CodeWar
     ```
   * Update `src/main/resources/application.yaml` with your MySQL credentials, JWT Secret, and Mail server details.
   * Build and run the server:
     ```bash
     mvn clean install
     mvn spring-boot:run
     ```
   * *Note: Ensure your MySQL server is running and the database specified in `.yaml` exists.*

3. **Frontend Setup:**
   * Open a new terminal and navigate to the frontend directory:
     ```bash
     cd CodeWar/codewar-frontend
     ```
   * Install dependencies:
     ```bash
     npm install
     ```
   * Start the development server:
     ```bash
     npm run dev
     ```
   * The application will be accessible at `http://localhost:3000`.
