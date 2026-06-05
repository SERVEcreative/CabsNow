# CabsNow — Cab Booking Platform

Full-stack cab booking application (Uber/Ola-style) built with **Spring Boot**, **React**, **MySQL**, **Redis**, and **WebSocket**.

## Features

| Feature | Tech |
|---------|------|
| JWT auth (Rider / Driver / Admin) | Spring Security + BCrypt |
| Ride booking lifecycle | REST API + JPA |
| Real-time ride status | WebSocket (STOMP) |
| Fare calculation | GraphHopper API + fallback Haversine |
| Mock payments | Payment entity + confirm flow |
| Driver ratings | 1–5 stars + comments |
| Driver GPS tracking | Lat/lng + nearby drivers |
| Admin dashboard | Stats API |
| Email notifications | Spring Mail (mock log when disabled) |
| Rate limiting | Bucket4j on login endpoints |
| API docs | Swagger UI |
| Caching | Redis (Docker) / in-memory (local) |
| DB migrations | Flyway |
| CI/CD | GitHub Actions |
| Containerized | Docker Compose |

## Architecture

```
React (Vite)  ──REST/JWT──►  Spring Boot API  ──►  MySQL
     │                            │
     └── WebSocket (/ws) ─────────┘
                                  ├── Redis (cache)
                                  └── Mail (optional)
```

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

## Docker (Full Stack)

```bash
docker compose up --build
```

- Frontend: http://localhost:5173
- Backend: http://localhost:8080
- MySQL: localhost:3307

## API Overview

| Method | Endpoint | Auth |
|--------|----------|------|
| POST | `/api/users/signup` | Public |
| POST | `/api/users/login` | Public |
| POST | `/api/riders/book` | Rider |
| GET | `/api/fare/calculate` | Public |
| GET | `/api/duties/getAllDutyDyStatusPending` | Driver |
| POST | `/api/drivers/accept/{id}` | Driver |
| POST | `/api/payments/confirm` | Rider |
| POST | `/api/ratings` | Rider |
| GET | `/api/admin/stats` | Admin |
| WS | `/ws` → `/topic/rider/{id}` | Live updates |

## Environment Variables

| Variable | Description |
|----------|-------------|
| `DB_PASSWORD` | MySQL password |
| `JWT_SECRET` | Min 32 chars |
| `GRAPHHOPPER_API_KEY` | Routing API key |
| `REDIS_ENABLED` | `true` in Docker |
| `MAIL_ENABLED` | Enable email sending |

## Resume Highlights

> Built a production-style cab booking platform with JWT role-based auth, real-time WebSocket updates, payment workflow, driver ratings, Redis caching, Flyway migrations, Docker Compose deployment, GitHub Actions CI, and a React frontend.

## Project Structure

```
CabsNow/
├── src/main/java/     # Spring Boot backend
├── frontend/          # React + Vite UI
├── docker-compose.yml
├── Dockerfile
└── .github/workflows/ci.yml
```

## Tests

```bash
mvn test
```

## License

MIT — portfolio project
