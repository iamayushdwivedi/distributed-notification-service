# NotifyFlow — Distributed Notification Service

An event-driven notification platform built using **Java 21, Spring Boot, Apache Kafka, PostgreSQL, Redis, and React**.

NotifyFlow accepts notification requests through a REST API, persists them in PostgreSQL, and uses Kafka to process email notifications asynchronously.

## Features

- REST API for creating and retrieving notifications
- Asynchronous notification processing with Apache Kafka
- PostgreSQL persistence and notification status tracking
- Redis caching integration
- Transactional email integration using Brevo
- React and TypeScript dashboard for submitting and viewing notifications
- Pagination support for notification history
- Spring Boot Actuator health checks and application metrics
- Automated backend tests
- Docker image configuration for the backend

## Tech Stack

| Layer | Technologies |
|---|---|
| Backend | Java 21, Spring Boot 4 |
| Messaging | Apache Kafka |
| Database | PostgreSQL |
| Cache | Redis |
| Frontend | React, TypeScript, Vite, Tailwind CSS |
| Email | Brevo API |
| Monitoring | Spring Boot Actuator |
| Build & Infrastructure | Maven, Docker, Docker Compose |

## Architecture

```text
React Dashboard
      |
      v
Spring Boot REST API
      |
      +----> PostgreSQL (Notification Records)
      |
      v
Apache Kafka (notification-events)
      |
      v
Kafka Consumer / Notification Worker
      |
      +----> Brevo Email API
      |
      v
Notification Status Update
      |
      v
PostgreSQL
```

Redis is integrated as a caching layer.

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/notifications` | Create a notification |
| GET | `/api/v1/notifications` | Retrieve paginated notifications |
| GET | `/actuator/health` | Check application health |
| GET | `/actuator/metrics` | View available application metrics |

### Example Notification Request

```json
{
  "recipient": "user@example.com",
  "message": "Hello from NotifyFlow!",
  "channel": "EMAIL"
}
```

## Running Locally

### Prerequisites

- Java 21
- Maven (or the included Maven wrapper)
- Node.js and npm
- Docker
- PostgreSQL

### 1. Start Kafka

From the project root:

```bash
docker compose up -d
```

The current Compose configuration starts Kafka.

Redis must also be running separately on port `6379`, and PostgreSQL must be available on port `5432`.

### 2. Configure Environment Variables

Set the following environment variables before starting Spring Boot:

```text
DB_PASSWORD=your_postgresql_password
BREVO_API_KEY=your_brevo_api_key
```

Never commit real credentials to GitHub.

### 3. Start the Backend

```bash
./mvnw spring-boot:run
```

The backend runs at `http://localhost:8080`.

### 4. Start the Frontend

Open another terminal:

```bash
cd frontend
npm install
npm run dev
```

The dashboard runs at `http://localhost:5173`.

### 5. Verify Application Health

Open:

```text
http://localhost:8080/actuator/health
```

A healthy application should report `UP`.

## Building the Backend Docker Image

First, package the application:

```bash
./mvnw clean package
```

Then build the Docker image:

```bash
docker build -t notifyflow-backend:latest .
```

**Note:** This builds the backend image. Running the backend inside Docker requires additional container networking and environment configuration because the current application settings use `localhost` for PostgreSQL, Kafka, and Redis.

## Testing

Run the backend test suite:

```bash
./mvnw test
```

## Monitoring

Spring Boot Actuator exposes:

- `/actuator/health`
- `/actuator/metrics`
- `/actuator/metrics/jvm.memory.used`

Kafka consumer lag can also be inspected using Kafka's consumer-group CLI.

Monitoring endpoints should be secured appropriately before public deployment.

## Project Status

NotifyFlow is a portfolio project developed to demonstrate REST API design, asynchronous messaging, persistence, caching, frontend integration, automated testing, and application observability.

The application has been tested locally. Public deployment is not yet configured.