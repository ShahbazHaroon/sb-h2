# sb-h2
Spring Boot project using H2 database
## Overview

---
## Getting Started

---
## Prerequisites
Make sure you have the following installed:
- Java 17+ (for Spring Boot)
- Maven 3.8+
- Docker & Docker Compose

---
## Tech Stack
- **Spring Boot** – REST APIs
- **Docker & Docker Compose** – Containerized environment for Vespa and the app
- **Maven** – Build tool for Spring Boot application

---
### Clone the repository
```bash
git clone https://github.com/ShahbazHaroon/sb-h2.git
cd sb-h2
```
---
### Run locally (without Docker):
After cloning the repository, navigate to the project root and run:
```bash
./mvnw spring-boot:run
# or, if Maven is installed globally
mvn spring-boot:run
```
Access: http://localhost:8080/sb-h2/swagger-ui/index.html or http://localhost:8080/sb-h2/api/v1/status

---
### Run with Docker:
```dockerfile
docker build -t sb-h2 .
docker run --name sb-h2-container -p 8080:8080 sb-h2
```
Access: http://localhost:8080/sb-h2/swagger-ui/index.html or http://localhost:8080/sb-h2/api/v1/status

---
### Run with Docker Compose:
```dockerfile
docker compose up --build
```
Access: http://localhost:8080/sb-h2/swagger-ui/index.html or http://localhost:8080/sb-h2/api/v1/status

---
### Persist H2 data locally: (Optional)
If you want your H2 database file stored locally (not in-memory), change your datasource URL:
```properties
spring.datasource.url=jdbc:h2:file:/data/sb-h2-db
```
And in Docker Compose:
```yaml
volumes:
      - ./h2-data:/data
```
---
### H2 console
Access: http://localhost:8080/sb-h2/h2-console
```shell
jdbc:h2:mem:sb-h2-db
```