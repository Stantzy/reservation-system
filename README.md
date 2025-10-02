# Reservation System

A small **Java + Spring Boot** web application implementing a basic reservation system.  
This project was created for practicing Spring Boot, Spring Data JPA, and PostgreSQL.

---
## Technologies
- Java 21+
- Spring Boot (Web, Data JPA, Validation)
- Hibernate
- PostgreSQL (running in Docker)
- Maven

---
## How to Run

1. Start a PostgreSQL container:
  ```bash
   docker pull postgres:latest
   docker run --name reservation-postgres \
     -e POSTGRES_PASSWORD=root \
     -e POSTGRES_DB=reservation_system \
     -p 5430:5432 \
     -d postgres:latest
   ```

---
2. Configure database connection in application.properties:
  ```properties
  spring.datasource.url=jdbc:postgresql://localhost:5430/reservation_system
  spring.datasource.username=postgres
  spring.datasource.password=root
  ```
3. Build and run the project
  ```bash
  mvn spring-boot:run
  ```

---
## Features
- Management of reservations.
- CRUD operations exposed via REST API.
- Input validation.
- Database interaction through Spring Data JPA
