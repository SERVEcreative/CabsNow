# CabsNow — Cab Booking Platform

Full-stack cab booking application (Uber/Rapido-style) built with **Spring Boot**, **React**, **MySQL**, **Redis**, **Kafka**, and **WebSocket**.

## Features

| Feature | Tech |
|---------|------|
| JWT auth (Rider / Driver / Admin) | Spring Security + BCrypt |
| Ride booking lifecycle | REST API + JPA |
| Real-time ride status | WebSocket (STOMP) |
| **Event-driven dispatch** | **Apache Kafka** (optional) |
| Geo-targeted driver matching | Haversine + online presence |
| **Ride event audit log** | Kafka consumer + MySQL |
| **Async notifications** | Kafka → email service |
| **Observability** | Spring Actuator + Prometheus |
| Fare calculation | GraphHopper API + fallback Haversine |
| Mock payments | Payment entity + confirm flow |
| Driver ratings | 1–5 stars + comments |
| Driver GPS tracking | Lat/lng + nearby drivers |
| Admin dashboard | Stats + ride event timeline |
| Email notifications | Spring Mail (mock log when disabled) |
| Rate limiting | Bucket4j on login endpoints |
| API docs | Swagger UI |
| Caching / presence | Redis (Docker) / in-memory (local) |
| DB migrations | Flyway |
| CI/CD | GitHub Actions |
| Containerized | Docker Compose |

## Architecture

### Local dev (Kafka off)
```
React ──REST/JWT──► Spring Boot ──► MySQL
  │                      │
  └── WebSocket ─────────┘──► Redis (optional)
                             └── direct WebSocket fan-out
```

### Production-style (Docker: Kafka + Redis on)
```
React ──REST──► Spring Boot API ──► MySQL
  │                  │
  │                  ├── publish ──► Kafka topics
  │                  │                 ├── cabsnow.ride.dispatch → WebSocket consumer
  │                  │                 ├── cabsnow.ride.audit     → event log DB
  │                  │                 └── cabsnow.ride.notifications → email
  │                  │
  └── WebSocket ◄────┘ (geo-targeted topics only)
                       Redis (driver presence + multi-instance pub/sub)
```

**Kafka topics**
| Topic | Purpose |
|-------|---------|
| `cabsnow.ride.dispatch` | Fan-out to WebSocket clients |
| `cabsnow.ride.audit` | Persist immutable ride event timeline |
| `cabsnow.ride.notifications` | Async email / push pipeline |

## Quick Start (Local)

### Prerequisites
- Java 17
- MySQL 8
- Node.js 20+

### Backend
```bash
brew services start mysql
mysql -u root -e "CREATE DATABASE IF NOT EXISTS user_management;"
cd CabsNow
mvn spring-boot:run
```

- API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html
- Health: http://localhost:8080/actuator/health

### Frontend
```bash
cd frontend
npm install
npm run dev
```

- App: http://localhost:5173

### Default Admin
- Email: `admin@cabsnow.com`
- Password: `Admin@12345`

## Docker (Full Stack — Kafka + Redis + MySQL)

```bash
docker compose up --build
```

- Frontend: http://localhost:5173
- Backend: http://localhost:8080
- MySQL: localhost:3307
- Kafka: localhost:9092
- Redis: localhost:6379

Enable Kafka locally without Docker:
```bash
KAFKA_ENABLED=true KAFKA_BOOTSTRAP_SERVERS=localhost:9092 mvn spring-boot:run
```

## API Overview

| Method | Endpoint | Auth |
|--------|----------|------|
| POST | `/api/users/signup` | Public |
| POST | `/api/users/login` | Public |
| POST | `/api/riders/book` | Rider |
| GET | `/api/fare/calculate` | Public |
| POST | `/api/drivers/online` | Driver |
| GET | `/api/duties/getAllDutyDyStatusPending` | Driver |
| POST | `/api/drivers/accept/{id}` | Driver |
| POST | `/api/payments/confirm` | Rider |
| POST | `/api/ratings` | Rider |
| GET | `/api/admin/stats` | Admin |
| GET | `/api/admin/rides/{dutyId}/events` | Admin — Kafka audit timeline |
| GET | `/actuator/prometheus` | Admin |
| WS | `/ws` → `/topic/rider/{id}` | Live updates |

## Environment Variables

| Variable | Description |
|----------|-------------|
| `DB_PASSWORD` | MySQL password |
| `JWT_SECRET` | Min 32 chars |
| `GRAPHHOPPER_API_KEY` | Routing API key |
| `REDIS_ENABLED` | `true` in Docker |
| `KAFKA_ENABLED` | `true` in Docker |
| `KAFKA_BOOTSTRAP_SERVERS` | e.g. `kafka:9092` |
| `MAIL_ENABLED` | Enable email sending |

## Deploy (Oracle Cloud VM)

Full step-by-step guide: **[docs/DEPLOY_ORACLE.md](docs/DEPLOY_ORACLE.md)**

Quick start on Ubuntu VM:

```bash
git clone https://github.com/SERVEcreative/CabsNow.git
cd CabsNow && git checkout feature/cabsnow-full-stack
cp .env.oracle.example .env   # edit with your VM public IP
docker compose -f docker-compose.prod.yml up -d --build
```

App: `http://YOUR_VM_PUBLIC_IP`

## Resume Highlights

> Built a production-style cab booking platform with JWT auth, geo-targeted WebSocket dispatch, **Apache Kafka event backbone**, Redis driver presence, ride audit logging, Prometheus metrics, React UI with live rider–driver sync, Docker Compose, and CI/CD.

## Project Structure

```
CabsNow/
├── src/main/java/
│   ├── kafka/              # Kafka producers & consumers
│   ├── Services/           # RideEventBus, dispatch, presence
│   └── ...
├── frontend/               # React + Vite UI
├── docker-compose.yml      # MySQL + Redis + Kafka + app
└── .github/workflows/ci.yml
```

## Tests

```bash
mvn test
```

## License

MIT — portfolio project
