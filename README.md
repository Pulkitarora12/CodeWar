# CodeWar ⚔️

**CodeWar** is a real-time, gamified competitive programming platform that allows developers to compete in synchronized, head-to-head coding challenges. By integrating with the **Codeforces API**, the platform automates problem discovery and submission verification, providing a seamless "competitive room" experience with live leaderboards.

## 🚀 Core Features

* **Real-Time Lobby System:** Synchronized room management using **WebSockets (STOMP)** for instant participant updates and contest transitions.
* **Dynamic Difficulty Scaling:** A custom algorithm that calculates the optimal problem difficulty (800–3000 rating) by averaging the verified Codeforces ratings of all room participants.
* **Automated Verification:** A secure "handshake" system that verifies Codeforces handles by matching unique tokens within the user's external profile.
* **Live Competition Engine:** * **Auto-Grading:** Real-time submission fetching from Codeforces to verify "OK" (Accepted) verdicts.
    * **Scoring Logic:** Points are calculated based on speed and accuracy: $100 - (failed\_attempts \times 5)$.
    * **Tie-Breaking:** Leaderboards are ranked by highest score, then by the lowest time taken to solve.
* **Secure Authentication:** Stateless security using **JWT** and **Spring Security**, featuring a multi-step password reset workflow with **JavaMailSender**.

## 🛠️ Tech Stack

* **Backend:** Java 21, Spring Boot, Spring Security, JPA/Hibernate.
* **Real-Time:** Spring WebSocket (STOMP), Message Broker.
* **Database:** MySQL.
* **External APIs:** Codeforces API (user.info, user.status, problemset.problems).
* **Security:** JSON Web Tokens (JWT), BCrypt, OAuth2.

## 🏗️ System Architecture

### 1. Authentication & Identity
Handles user registration, JWT-based login, and administrative oversight for account status (locking/expiring).

### 2. Codeforces Linkage
Uses a non-invasive verification flow:
1. System generates a unique `cw-xxxx` token.
2. User updates their Codeforces "First Name".
3. System validates the token via `user.info` API to mark the account as verified.

### 3. Room & Contest Workflow
* **Lobby:** Users join via unique room codes (e.g., `CW-A1B2C3`).
* **Contest Start:** The system calculates the group's average rating and picks a matching problem.
* **Monitoring:** The backend periodically checks the participant's Codeforces status for the specific problem ID.
* **WebSocket Updates:** Whenever a user solves a problem, the updated leaderboard is broadcasted to all participants.

## 🚦 Getting Started

### Prerequisites
* JDK 21+
* Maven 3.8+
* MySQL 8.0+

### Setup
1.  **Clone the repository:**
    ```bash
    git clone https://github.com/pulkitarora12/codewar.git
    ```
2.  **Configure environment:**
    Update `src/main/resources/application.yaml` with your MySQL credentials, JWT Secret, and Mail server details.
3.  **Build and Run:**
    ```bash
    mvn clean install
    mvn spring-boot:run
    ```

---