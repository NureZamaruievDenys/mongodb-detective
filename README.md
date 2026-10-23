# MongoDB Detective API

> **Status:** In development

A RESTful backend service for a procedural detective game. This project demonstrates advanced NoSQL database modeling, complex aggregations, and procedural world generation using Java and Spring Boot.

## Features
- [x] **Procedural World Generation:** Dynamically generates cases, 500+ citizens, bank transactions, GPS logs, and autopsy records using Datafaker.
- [x] **Advanced Aggregations:** Utilizes `MongoTemplate` (`$match`, `$lookup`, `$unwind`, `$project`) to join cross-collection data for financial suspect analysis.
- [x] **Strict Two-Tier Validation:** Implements Jakarta Validation at the application level and manual BSON `$jsonSchema` validation strictly at the MongoDB engine level.
- [x] **Complex CRUD & Bulk Operations:** Supports targeted document updates and conditional bulk operations (e.g., mass clearing suspect statuses).
- [x] **Database Optimization:** Implements automatic index creation and `@CompoundIndex` configurations for high-performance query execution.
- [x] **On-Demand Game Engine:** Deterministic procedural generation for dynamic interrogations, alibis, and case-solving logic.
- [ ] **Player Logic & Command Parsing:** Implementation of user session handling, API command parsing, and execution of specific detective actions. *(In progress)*

## Tech Stack
- **Language:** Java 17
- **Framework:** Spring Boot 3.x (Spring Web, Spring Data MongoDB)
- **Database:** MongoDB (Analyzed via MongoDB Compass)
- **Libraries:** Datafaker, Jakarta Bean Validation
- **Build Tool:** Maven
- **Tools:** Postman, Git

## How to Run Locally
1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/mongodb-detective.git
   ```
2. Ensure MongoDB is running locally and configure `application.properties`:
   ```properties
   spring.data.mongodb.uri=mongodb://localhost:27017/mongodb_detective
   spring.data.mongodb.auto-index-creation=true
   ```
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```
4. Generate a new world and start playing via Postman:
   ```http
   POST http://localhost:8080/api/game/cases/generate
   ```
