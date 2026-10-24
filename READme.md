# Patient Management System

A microservices-based Patient Management System built with **Spring Boot, PostgreSQL, Apache Kafka, gRPC, and Docker**.

The project is organized into multiple independent services that communicate with each other through REST, gRPC, and Kafka.

---

## 🏗️ Architecture

```text
                         ┌─────────────────────┐
                         │     API Gateway     │
                         │      Port 4004      │
                         └──────────┬──────────┘
                                    │
                    ┌───────────────┼───────────────┐
                    │               │               │
                    ▼               ▼               ▼
             ┌─────────────┐ ┌─────────────┐ ┌───────────────┐
             │   Patient   │ │   Billing   │ │   Analytics   │
             │   Service   │ │   Service   │ │    Service    │
             │   :4000     │ │   :4001     │ │     :4042     │
             └──────┬──────┘ └──────┬──────┘ └───────┬───────┘
                    │               │                │
                    │      gRPC     │                │
                    └──────────────►│                │
                    │                                │
                    │           Kafka                │
                    └────────────────────────────────►
                                    │
                                    ▼
                              ┌───────────┐
                              │   Kafka   │
                              │  :9092    │
                              │  :9094    │
                              └───────────┘

                              PostgreSQL
                              :5433 → :5432
```

---

## 🚀 Services

| Service           | Description                              |            Port |
| ----------------- | ---------------------------------------- | --------------: |
| API Gateway       | Entry point for API requests and routing |          `4004` |
| Patient Service   | Patient management and REST APIs         |          `4000` |
| Billing Service   | Billing functionality and gRPC server    | `4001` / `9001` |
| Analytics Service | Processes patient events and analytics   |          `4042` |
| PostgreSQL        | Application database                     |          `5433` |
| Kafka             | Event/message broker                     | `9092` / `9094` |

---

## 🛠️ Technology Stack

### Backend

* Java 21
* Spring Boot 4.1.1
* Spring Cloud Gateway
* Spring Data JPA
* REST APIs
* gRPC
* Protocol Buffers

### Database

* PostgreSQL 17

### Messaging

* Apache Kafka

### Build

* Maven
* Maven Wrapper

### Containerization

* Docker
* Docker Compose
* Eclipse Temurin JDK 21

---

## 📁 Project Structure

```text
patient-management/
│
├── api-gateway/
│   ├── src/
│   ├── pom.xml
│   ├── Dockerfile
│   └── mvnw
│
├── patient-service/
│   ├── src/
│   ├── pom.xml
│   ├── Dockerfile
│   └── mvnw
│
├── billing-service/
│   ├── src/
│   ├── pom.xml
│   ├── Dockerfile
│   └── mvnw
│
├── analytics-service/
│   ├── src/
│   ├── pom.xml
│   ├── Dockerfile
│   └── mvnw
│
├── docker-compose.yml
└── README.md
```

---

# 🐳 Docker Setup

Each Spring Boot service is packaged as a JAR and then built into a Docker image.

Example `Dockerfile`:

```dockerfile
FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 4004

ENTRYPOINT ["java", "-jar", "app.jar"]
```

Change the exposed port according to the service.

---

# 🔧 Dockerizing a New Service

## 1. Create a Dockerfile

Inside the service directory:

```text
service-name/
└── Dockerfile
```

Example:

```dockerfile
FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 4000

ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## 2. Configure the Application

For Docker-to-Docker communication, use the **Docker Compose service name** instead of `localhost`.

Example:

```yaml
uri: http://patient-service:4000
```

Do not use:

```yaml
uri: http://localhost:4000
```

Inside a Docker container, `localhost` refers to that same container.

---

## 3. Build the Spring Boot Application

```bash
cd service-name
./mvnw clean package -DskipTests
```

After a successful build:

```text
BUILD SUCCESS
```

A JAR will be created inside:

```text
target/
```

---

## 4. Build the Docker Image

Return to the project root:

```bash
cd ..
```
docker
Then:

```bash
docker compose build service-name
```

For a complete clean Docker build:

```bash
docker compose build --no-cache service-name
```

---

## 5. Start the Service

```bash
docker compose up -d service-name
```

---

## 6. Verify the Container

```bash
docker ps
```

Or:

```bash
docker ps | grep service-name
```

---

## 7. Check Logs

```bash
docker logs service-name --tail=100
```

For live logs:

```bash
docker logs -f service-name
```

Press `Ctrl + C` to stop viewing the logs.

---

## 8. Test the Service

Example:

```bash
curl -i http://localhost:4000
```

For API Gateway:

```bash
curl -i http://localhost:4004/api/patients
```

---

# 🔄 Rebuilding After Code Changes

Whenever Java code or configuration changes:

```bash
cd service-name
./mvnw clean package -DskipTests
cd ..
docker compose build service-name
docker compose up -d service-name
```

If Docker appears to be using an old image:

```bash
docker compose build --no-cache service-name
docker compose up -d service-name
```

---

# 🐘 PostgreSQL

PostgreSQL runs inside Docker using:

```yaml
image: postgres:17
```

Configuration:

```text
Database: patientdb
Username: postgres
Password: postgres
```

Host port:

```text
5433
```

Container port:

```text
5432
```

Therefore:

```text
localhost:5433
        ↓
PostgreSQL Container:5432
```

The database uses a persistent Docker volume:

```yaml
volumes:
  - patient-postgres-data:/var/lib/postgresql/data
```

---

# 📨 Kafka

Kafka runs as a Docker container.

Configured ports:

```text
9092
9094
```

The internal Docker listener is:

```text
kafka:29092
```

Services running inside Docker should use:

```text
kafka:29092
```

For example:

```yaml
spring:
  kafka:
    bootstrap-servers: kafka:29092
```

---

# 🌐 API Gateway

The API Gateway runs on:

```text
4004
```

Example route:

```yaml
routes:
  - id: patient-service-route
    uri: http://patient-service:4000
    predicates:
      - Path=/api/patients/**
    filters:
      - StripPrefix=1
```

Request flow:

```text
Client
  │
  ▼
localhost:4004
  │
  ▼
API Gateway
  │
  ▼
patient-service:4000
```

---

# 🔗 Service Communication

### API Gateway → Patient Service

```text
http://patient-service:4000
```

### Patient Service → Billing Service

Billing uses gRPC internally:

```text
billing-service:9001
```

### Patient Service → Kafka

```text
kafka:29092
```

### Analytics Service → Kafka

```text
kafka:29092
```

### Services → PostgreSQL

Inside Docker:

```text
patient-postgres:5432
```

---

# 📋 Useful Docker Commands

### Show running containers

```bash
docker ps
```

### Show all containers

```bash
docker ps -a
```

### Show images

```bash
docker images
```

### View logs

```bash
docker logs SERVICE_NAME
```

### Follow logs

```bash
docker logs -f SERVICE_NAME
```

### Restart a service

```bash
docker compose restart SERVICE_NAME
```

### Stop a service

```bash
docker compose stop SERVICE_NAME
```

### Start a stopped service

```bash
docker compose start SERVICE_NAME
```

### Stop the entire project

```bash
docker compose down
```

### Start the entire project

```bash
docker compose up -d
```

### Rebuild the entire project

```bash
docker compose build
```

### Rebuild without cache

```bash
docker compose build --no-cache
```

---

# 🔍 Troubleshooting

## Container exits immediately

Check:

```bash
docker ps -a
```

Then:

```bash
docker logs SERVICE_NAME --tail=100
```

---

## Port already in use

Check which process is using the port:

```bash
sudo lsof -i :4004
```

Or:

```bash
sudo ss -ltnp | grep 4004
```

---

## Check Docker Compose configuration

```bash
docker compose config
```

---

## Check service status

```bash
docker compose ps
```

---

## Rebuild from scratch

```bash
docker compose down
docker compose build --no-cache
docker compose up -d
```

---

# 🌿 Git Workflow

The project uses feature branches for development.

Example:

```bash
git branch --show-current
```

Create a feature branch:

```bash
git checkout -b feature/service-name-docker
```

Check changes:

```bash
git status
```

Stage:

```bash
git add .
```

Commit:

```bash
git commit -m "dockerize api gateway"
```

Push:

```bash
git push -u origin feature/service-name-docker
```

---

# 📌 Current Development Branches

The main development work is organized through feature branches.

Example:

```text
development
└── feature/api-gateway-docker
```

The API Gateway Dockerization work includes:

* Spring Boot application configuration
* Dockerfile
* Docker Compose configuration
* Port `4004`
* Docker networking
* Patient Service routing
* API documentation routing

---

# ▶️ Quick Start

Clone the repository:

```bash
git clone git@github.com:Pk2006-a11/patient-management.git
```

Enter the project:

```bash
cd patient-management
```

Build and start all services:

```bash
docker compose up -d --build
```

Check the containers:

```bash
docker compose ps
```

View logs:

```bash
docker compose logs -f
```

Stop the project:

```bash
docker compose down
```

---

## 👨‍💻 Author

**Pranjal Kundu**

GitHub: `Pk2006-a11`

---

## 📄 License

This project is currently intended for educational and development purposes.
