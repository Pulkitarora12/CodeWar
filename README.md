# CodeWar ⚔️

**CodeWar** is a high-performance, real-time competitive programming arena built with Spring Boot. It allows users to create private rooms, link their Codeforces profiles securely, and compete against friends with problems automatically scaled to the participants' skill levels.

## 🚀 Features

* **Secure Profile Verification**: Prove ownership of your Codeforces handle by placing a unique system-generated token in your Codeforces profile's "First Name" field.
* **Dynamic Room Management**: Create rooms with unique codes (e.g., `CW-XXXXXX`) and invite participants via direct links.
* **Intelligent Difficulty Scaling**: Automatically calculates the optimal problem rating based on the average Codeforces rating of all verified participants in the room.
* **Stateless Security**: Utilizes JWT-based authentication for a scalable, stateless backend.
* **Multi-Provider OAuth2**: Support for seamless login via Google and GitHub.
* **Admin Dashboard**: Robust controls for managing users, roles, and account statuses.

## 🛠️ Tech Stack

* **Backend**: Java 21, Spring Boot 4.0.4
* **Security**: Spring Security, JWT (jjwt), OAuth2
* **Database**: MySQL
* **Communication**: JavaMail Sender for password recovery

## 🏗️ Core Logic

### Codeforces Verification Protocol
To ensure fair play, the system verifies handles using the following flow:
1.  **Token Generation**: The backend generates a unique `cw-` UUID.
2.  **External Confirmation**: The application calls the **Codeforces user.info API** to check if the user's `firstName` matches the generated token.
3.  **Sync**: Once verified, the user's actual rating and rank are synchronized with the platform.

### Rating Calculation Logic
The `RoomService` determines the competition level by:
* Averaging the ratings of all verified participants.
* Rounding the average to the nearest 100-point difficulty tier used by Codeforces.

## 🛤️ Roadmap (In Progress)

* **Problem Picking**: Automating the fetch of specific problems from the Codeforces API matching the calculated room rating.
* **WebSocket Integration**: Real-time chat and live leaderboards for active rooms.
* **Match History**: Persistent records of previous contests and room outcomes.
* **Code Submission & Judging**: Real-time tracking of problem status directly within the arena.

## ⚙️ Setup

1.  **Configure Environment**: Set the following variables in your environment or `application.yaml`:
    * `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`
    * `JWT_SECRET`, `JWT_EXPIRATION`
    * `GITHUB_CLIENT`, `GITHUB_SECRET`
    * `GOOGLE_CLIENT`, `GOOGLE_SECRET`
2.  **Build & Run**:
    ```bash
    ./mvnw clean install
    ./mvnw spring-boot:run
    ```