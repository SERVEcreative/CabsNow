# CabsNow — Interview Preparation Guide

> **Purpose:** Is document se tum project confidently explain kar sakte ho — elevator pitch se lekar deep system design tak.  
> **Repo:** [CabsNow](https://github.com/SERVEcreative/CabsNow)

---

## Table of Contents

1. [30-Second Elevator Pitch](#1-30-second-elevator-pitch)
2. [2-Minute Project Walkthrough](#2-2-minute-project-walkthrough)
3. [Architecture (Draw This on Whiteboard)](#3-architecture-draw-this-on-whiteboard)
4. [Tech Stack — Why Each Tool?](#4-tech-stack--why-each-tool)
5. [Core Ride Flow (Step by Step)](#5-core-ride-flow-step-by-step)
6. [Design Patterns Used](#6-design-patterns-used)
7. [Key Classes — Interviewer Puche To](#7-key-classes--interviewer-puche-to)
8. [Database Schema (High Level)](#8-database-schema-high-level)
9. [API & WebSocket Topics](#9-api--websocket-topics)
10. [Most Likely Interview Questions + Answers](#10-most-likely-interview-questions--answers)
11. [System Design Questions (Scale Wale)](#11-system-design-questions-scale-wale)
12. [Security — Puche To Kya Bolna Hai](#12-security--puche-to-kya-bolna-hai)
13. [Challenges & How You Solved (STAR Format)](#13-challenges--how-you-solved-star-format)
14. [Honest Limitations (Maturity Dikhata Hai)](#14-honest-limitations-maturity-dikhata-hai)
15. [Demo Script (Live Dikhane Ke Liye)](#15-demo-script-live-dikhane-ke-liye)
16. [Quick Revision Cheat Sheet](#16-quick-revision-cheat-sheet)

---

## 1. 30-Second Elevator Pitch

**English (recommended in interview):**

> "CabsNow is a full-stack cab booking platform similar to Rapido/Uber. Riders book rides via a React frontend; the Spring Boot backend matches **nearby online drivers** using Haversine distance and pushes **real-time updates over WebSocket**. Side effects like audit logging and email notifications go through **Kafka** asynchronously, while live UI updates are sent **immediately** without waiting for Kafka. The stack includes MySQL, Redis for driver presence, Docker Compose, JWT auth, and Leaflet maps for live GPS demo."

**Hindi shortcut (practice ke liye):**

> "Maine Uber-style cab app banaya — rider book karta hai, nearby drivers ko notification jaata hai, driver accept karta hai, dono ko live update milta hai WebSocket se. Backend Spring Boot hai, frontend React, aur production-style ke liye Kafka + Redis + Docker bhi lagaya."

---

## 2. 2-Minute Project Walkthrough

Use this structure in every interview:

| Step | Kya bolo |
|------|----------|
| **Problem** | Real-time ride matching + live status sync between rider and driver |
| **Users** | Rider, Driver, Admin (3 roles) |
| **Frontend** | React + Vite, STOMP WebSocket, Leaflet map for live GPS |
| **Backend** | Spring Boot REST API, JWT, role-based security |
| **Real-time** | STOMP topics — geo-targeted, not global broadcast |
| **Matching** | Online drivers within 10 km radius (Haversine) |
| **Async** | Kafka → audit log DB + email notifications |
| **Infra** | Docker Compose: MySQL, Redis, Kafka, backend, frontend |
| **Extra** | Flyway migrations, Actuator/Prometheus, Swagger, rate limiting on login |

**Closing line:**

> "I designed it so live UX stays fast — WebSocket first — and heavy work like audit and email is decoupled via Kafka."

---

## 3. Architecture (Draw This on Whiteboard)

```
┌─────────────┐     REST + JWT      ┌──────────────────────────────────┐
│ React App   │◄───────────────────►│ Spring Boot                      │
│ (Rider/     │                     │  Controllers → Services → Repos  │
│  Driver)    │     STOMP/SockJS    │                                  │
└──────┬──────┘◄───────────────────►│  RideEventPublisher              │
       │                             │       ↓                          │
       │                             │  RideEventBus                     │
       │                             │    ├─► WebSocket (immediate)     │
       │                             │    └─► Kafka (async)             │
       │                             └──────────┬───────────────────────┘
       │                                        │
       │                             ┌──────────┼──────────┐
       │                             ▼          ▼          ▼
       │                          MySQL      Redis       Kafka
       │                          (rides)   (presence)  (audit/email)
       │
       └── Leaflet + OSM (free map tiles)
           Browser Geolocation API (GPS)
```

### Important design decision (memorize this)

```
Live path:     API → RideEventBus → WebSocket → Client   (milliseconds)
Async path:    API → RideEventBus → Kafka → Consumer     (audit, email)
```

**Interviewer puche:** *"Why not send everything through Kafka?"*  
**Answer:** Kafka adds latency. Driver accept popup and rider "driver found" must feel instant. Audit and email can tolerate 100ms–1s delay.

---

## 4. Tech Stack — Why Each Tool?

| Technology | Role in CabsNow | Interview answer — "Why?" |
|------------|-----------------|---------------------------|
| **Java 17 + Spring Boot 3** | Backend framework | Mature ecosystem, Security, JPA, WebSocket built-in |
| **MySQL 8** | Primary datastore | ACID for rides, payments, users; familiar for interviews |
| **Flyway** | DB migrations | Version-controlled schema; no manual SQL in prod |
| **React + Vite** | SPA frontend | Component model, fast dev, lazy routes |
| **JWT** | Stateless auth | Scales horizontally; no server session |
| **WebSocket (STOMP)** | Live ride events | Push model — no polling every second |
| **Redis** | Driver presence + pub/sub | Fast online set; multi-server WebSocket bridge |
| **Kafka** | Event bus (audit, notify) | Decouple side effects; replay audit trail |
| **Leaflet + OSM** | Live map (demo) | Free, no Google Maps API cost |
| **Docker Compose** | Local prod-like stack | One command full environment |
| **Bucket4j** | Rate limiting | Brute-force protection on login |
| **Actuator + Prometheus** | Metrics | Health checks, observability talking point |

---

## 5. Core Ride Flow (Step by Step)

### Booking → Accept → Complete

```
1. Rider POST /api/riders/book
      ↓
2. Duty created (status = PENDING) + pickup/drop coordinates saved
      ↓
3. RideEventPublisher finds nearby online drivers (10 km, Haversine)
      ↓
4. Destinations: /topic/driver/{id} for each nearby driver
                 /topic/rider/{riderId}
                 /topic/ride/{dutyId}
      ↓
5. RideEventBus → WebSocket IMMEDIATE + Kafka async (if enabled)
      ↓
6. Driver sees IncomingRideCard (30 sec accept window)
      ↓
7. Driver POST /api/drivers/accept/{dutyId}
      ↓
8. Status = ACCEPTED, driver = ON_DUTY
      ↓
9. Rider gets WebSocket update + map shows driver GPS
      ↓
10. Driver location updates every 5s → RideLocationPublisher → rider WebSocket
      ↓
11. Driver completes → status COMPLETED → payment + rating
```

### Driver matching logic (explain clearly)

- Filter: `AVAILABLE` drivers with lat/lng
- Filter: must be **online** (Redis or in-memory presence)
- Calculate Haversine distance from pickup
- Keep drivers within **10 km** (configurable)
- Sort by distance, notify top **20**

---

## 6. Design Patterns Used

| Pattern | Where | One-line explanation |
|---------|-------|----------------------|
| **Layered Architecture** | Controller → Service → Repository | Separation of concerns |
| **Repository Pattern** | JPA repositories | DB access abstracted |
| **DTO Pattern** | `BookRideRequest`, `RideStatusEvent` | API ≠ entity; safe payloads |
| **Strategy Pattern** | `DriverPresenceService` | Redis vs in-memory via `@ConditionalOnProperty` |
| **Event Bus / Facade** | `RideEventBus` | Single entry for WebSocket + Kafka + audit |
| **Publisher-Subscriber** | WebSocket STOMP topics | Clients subscribe; server pushes |
| **Pub/Sub (Redis)** | `RideMessageBroadcaster` | Multi-instance WebSocket sync |
| **Idempotent Consumer** | `RideAuditKafkaConsumer` | `existsByEventId()` prevents duplicates |
| **Factory Method** | `RideDispatchMessage.of(...)` | Consistent event object creation |
| **Observer (Frontend)** | `useRideSocket` hook | UI reacts to server events |

---

## 7. Key Classes — Interviewer Puche To

| Class | Responsibility |
|-------|----------------|
| `RiderController` / `DriverController` | REST endpoints, role checks via `SecurityHelper` |
| `RiderService` | Book ride, cancel, PDF history |
| `DutyService` | Accept, complete, reject — business rules |
| `RideEventPublisher` | Build event + resolve **who gets notified** (geo-targeted) |
| `RideEventBus` | WebSocket first, then Kafka or sync fallback |
| `RideMessageBroadcaster` | STOMP send + optional Redis bridge |
| `NearbyDriverService` | Haversine matching within radius |
| `DriverPresenceService` | Online/offline + location (Redis or memory) |
| `RideLocationPublisher` | Live GPS broadcast during active ride |
| `JwtAuthenticationFilter` | Extract JWT → Spring Security context |
| `RideAuditKafkaConsumer` | Persist immutable event log |
| `FareService` | GraphHopper API + Haversine fallback |

---

## 8. Database Schema (High Level)

| Table | Purpose |
|-------|---------|
| `users` | Rider accounts (linked to `riders`) |
| `riders` | Rider profile |
| `drivers` | Driver profile + lat/lng + status |
| `duties` | Rides (PENDING → ACCEPTED → COMPLETED) |
| `payments` | Mock payment per duty |
| `ratings` | Driver ratings |
| `admins` | Admin login |
| `ride_event_log` | Audit trail (Kafka or sync) |

**Flyway migrations:** V1 schema → V2 payments/ratings → V3 driver location → V4 audit log → V5 ride coordinates

**Interviewer puche:** *"Why separate users and riders?"*  
**Answer:** `users` holds auth (email/password); `riders` holds domain profile. Allows future multi-role or OAuth without mixing concerns.

---

## 9. API & WebSocket Topics

### Important REST endpoints

| Method | Path | Role | Purpose |
|--------|------|------|---------|
| POST | `/api/users/login` | Public | Rider login |
| POST | `/api/riders/book` | RIDER | Book ride |
| POST | `/api/drivers/accept/{id}` | DRIVER | Accept ride |
| PUT | `/api/drivers/location` | DRIVER | Update GPS |
| POST | `/api/drivers/online` | DRIVER | Go online |
| GET | `/api/admin/rides/{id}/events` | ADMIN | Audit timeline |

### WebSocket topics (STOMP)

| Topic | Who subscribes | Purpose |
|-------|----------------|---------|
| `/topic/driver/{driverId}` | Driver | Incoming ride requests |
| `/topic/rider/{riderId}` | Rider | Ride status + driver GPS |
| `/topic/ride/{dutyId}` | Both (during ride) | Shared ride channel |

**Why not `/topic/rides` global?**  
At scale, broadcasting every ride to every client is O(drivers × rides). Geo-targeted topics reduce noise and bandwidth.

---

## 10. Most Likely Interview Questions + Answers

### Project overview

**Q: Apna project explain karo.**  
**A:** Use [Section 2](#2-2-minute-project-walkthrough). End with tech stack.

**Q: Tumne ye akela banaya ya team mein?**  
**A:** Honest answer. Agar solo: "Full stack — backend, frontend, Docker, Kafka integration."

**Q: Sabse challenging part kya tha?**  
**A:** See [Section 13 — STAR format](#13-challenges--how-you-solved-star-format).

---

### Backend / Spring

**Q: Spring Boot mein layers kaise organize kiye?**  
**A:** Controller (HTTP) → Service (business logic) → Repository (JPA). DTOs for API; entities for DB. Exception handling via `GlobalExceptionHandler`.

**Q: JWT ka flow kya hai?**  
**A:** Login → server signs JWT with secret (claims: id, role) → client sends `Authorization: Bearer` → `JwtAuthenticationFilter` validates → `SecurityContext` set → `@PreAuthorize` / `hasRole` on endpoints.

**Q: `@Transactional` kahan use kiya?**  
**A:** `acceptDuty`, `completeDuty` — multiple DB writes (duty + driver status) must be atomic.

**Q: Flyway kyun?**  
**A:** Schema changes version-controlled; same migrations run in dev, Docker, CI. `ddl-auto=validate` in prod — Hibernate doesn't auto-alter tables.

---

### Real-time / WebSocket

**Q: WebSocket vs polling?**  
**A:** Polling wastes requests and adds delay. WebSocket = persistent connection; server pushes when ride status changes. Better for "driver accepted" in <1 second.

**Q: STOMP kya hai?**  
**A:** Messaging protocol on top of WebSocket. Clients **subscribe** to `/topic/...` and receive JSON payloads. SockJS fallback for older browsers.

**Q: Agar 2 backend servers hon?**  
**A:** Redis pub/sub — one server publishes ride event to Redis channel; all servers receive and push to their connected WebSocket clients (`RideMessageBroadcaster` + `RedisRideEventSubscriber`).

---

### Kafka

**Q: Kafka kahan use kiya?**  
**A:** Two topics: `cabsnow.ride.audit` (immutable log in MySQL) and `cabsnow.ride.notifications` (async email). **Not** used for live WebSocket — that would add latency.

**Q: Kafka ke fayde?**  
**A:** Decoupling, durability, replay for audit, independent scaling of consumers, backpressure handling.

**Q: Consumer idempotent kaise?**  
**A:** `RideAuditKafkaConsumer` checks `existsByEventId()` before insert — duplicate messages safe.

**Q: Kafka off ho to?**  
**A:** `@ConditionalOnProperty(app.kafka.enabled)` — sync audit via `RideEventLogService` + direct email call. App still works locally without Kafka.

---

### Redis

**Q: Redis kya store karta hai?**  
**A:** Online driver set + GEO coordinates for presence. Optional pub/sub for multi-instance WebSocket.

**Q: Redis na ho to?**  
**A:** `InMemoryDriverPresenceService` activates — fine for local dev, not for multi-pod production.

---

### Matching / GPS

**Q: Nearby driver kaise dhundhte ho?**  
**A:** Haversine formula on pickup lat/lng vs driver lat/lng. Filter online + AVAILABLE. Radius 10 km. Production mein PostGIS or Redis GEO radius query use hota hai at scale.

**Q: Live GPS kaise kaam karta hai?**  
**A:** Browser `watchPosition` → driver sends location every 5s → `RideLocationPublisher` → WebSocket `LOCATION_UPDATE` to rider → Leaflet map updates marker.

---

### Frontend

**Q: React mein state kaise manage?**  
**A:** Local `useState` + custom hooks (`useRideSocket`, `useGeolocation`). No Redux — scope small enough for demo/MVP.

**Q: Rider aur driver same browser mein?**  
**A:** Shared localStorage — use Incognito for second role during demo.

---

### DevOps

**Q: Docker Compose mein kya hai?**  
**A:** mysql, redis, kafka, backend (Spring), frontend (Vite). `SPRING_PROFILES_ACTIVE=docker` enables Redis + Kafka.

**Q: CI/CD?**  
**A:** GitHub Actions — build + test on push (mention if asked; check `.github/workflows`).

---

## 11. System Design Questions (Scale Wale)

Interviewer ye **theoretical** puch sakta hai — project se extend karke jawab do.

### Q: 1 million rides per day — kya change karoge?

| Area | Current | At scale |
|------|---------|----------|
| Matching | Haversine in app | Redis GEO / PostGIS spatial index |
| WebSocket | Single broker | Dedicated WS cluster + sticky sessions |
| Kafka | 1 broker | Partition by city/region |
| DB | Single MySQL | Sharding by city; read replicas |
| Driver location | 5s HTTP POST | MQTT or WS stream; batch writes |

### Q: Driver accept race condition?

**Problem:** Two drivers accept same PENDING ride simultaneously.  
**Fix:** Optimistic locking (`@Version` on Duty) or DB `UPDATE ... WHERE status='PENDING'` with row count check. Mention this as future improvement.

### Q: Surge pricing?

**Current:** `FareService` has peak-hour multiplier.  
**At scale:** Dynamic pricing service reads demand/supply from Redis counters.

### Q: How would you design Uber from scratch? (High level)

1. Location service (driver GPS ingest)  
2. Dispatch service (matching algorithm)  
3. Trip service (state machine: requested → matched → ongoing → completed)  
4. Notification service (push/SMS)  
5. Payment service  
6. API gateway + auth  

**Your project maps to:** Trip service + Dispatch + Notification (partial).

---

## 12. Security — Puche To Kya Bolna Hai

### What you implemented (positive)

- BCrypt passwords  
- JWT stateless auth + role-based access (RIDER, DRIVER, ADMIN)  
- Ownership checks (rider can only cancel own ride; driver only complete assigned ride)  
- Rate limiting on login (Bucket4j)  
- CORS configured for known origins  
- `@JsonIgnore` on password fields  

### What you'd improve (shows awareness — good in interviews)

- WebSocket auth (currently open — add JWT on STOMP connect)  
- Server-side fare validation (don't trust client fare)  
- HTTPS in production  
- Secrets via env vars, not defaults  
- Payment IDOR fix on GET payment  

**Pro tip:** Interview mein pehle strengths bolo, phir 2-3 improvements — interviewer ko lagta hai tum mature soch rakhte ho.

---

## 13. Challenges & How You Solved (STAR Format)

Use STAR = **Situation, Task, Action, Result**

### Challenge 1: Driver ko ride notification nahi aa raha tha (Docker)

| | |
|---|---|
| **Situation** | Kafka enabled in Docker; live updates only went to Kafka, consumer broken |
| **Task** | Driver must see ride popup within 1 second |
| **Action** | Refactored `RideEventBus` — WebSocket **always first**; Kafka only for audit/email |
| **Result** | Instant notifications; Kafka for side effects only |

### Challenge 2: Global broadcast spam

| | |
|---|---|
| **Situation** | Early design used `/topic/rides` — every driver got every ride |
| **Task** | Only relevant drivers should be notified |
| **Action** | Geo-targeted `/topic/driver/{id}` + Haversine + online presence filter |
| **Result** | Scalable pattern; matches real dispatch systems |

### Challenge 3: Backend crash in Docker (Kafka bean)

| | |
|---|---|
| **Situation** | `kafkaListenerContainerFactory` bean missing; app wouldn't start |
| **Task** | Stable Docker demo environment |
| **Action** | Removed conflicting custom config; used Spring Boot Kafka auto-config; profile-specific properties |
| **Result** | `docker compose up --build` works reliably |

### Challenge 4: Live GPS for demo

| | |
|---|---|
| **Situation** | Only manual lat/lng inputs; no map |
| **Task** | Rapido-like live tracking for interview demo |
| **Action** | Leaflet + OSM (free) + browser Geolocation + WebSocket location events |
| **Result** | Rider sees driver moving on map every 5 seconds |

---

## 14. Honest Limitations (Maturity Dikhata Hai)

Interview mein ye voluntarily bolo — trust badhta hai:

1. Mock payment — not real Razorpay/Stripe integration  
2. WebSocket not authenticated yet  
3. Fare calculated on client for booking (should be server-side)  
4. Single-region; no multi-city sharding  
5. Haversine in Java — not optimized geo-index at millions of drivers  
6. No mobile app — responsive web only  

**Frame it as:** "For MVP/demo I prioritized ride lifecycle and real-time sync; production would add X, Y, Z."

---

## 15. Demo Script (Live Dikhane Ke Liye)

**2 windows:**

1. **Rider** — normal Chrome → signup/login → book ride  
2. **Driver** — Incognito → signup/login → auto online + GPS allow  

**Flow:**

1. Rider: Estimate fare → Book now → "Finding driver" overlay  
2. Driver: Incoming ride card → Accept (30 sec window)  
3. Rider: Map shows driver blue dot moving  
4. Admin (optional): `/api/admin/rides/{dutyId}/events` — audit trail  
5. Driver: Complete ride → Rider: Pay + Rate  

**If GPS weak on laptop:** "Demo uses Delhi default coordinates; on phone GPS works live."

---

## 16. Quick Revision Cheat Sheet

```
AUTH:     JWT + BCrypt + roles (RIDER/DRIVER/ADMIN)
BOOK:     POST /api/riders/book → PENDING duty
MATCH:    Haversine 10km + online drivers only
NOTIFY:   /topic/driver/{id} — NOT global
LIVE:     WebSocket immediate
ASYNC:    Kafka → audit + email
PRESENCE: Redis GEO (docker) / memory (local)
GPS:      Browser → 5s → RideLocationPublisher → rider map
MAP:      Leaflet + OpenStreetMap (free)
PATTERNS: Strategy, Event Bus, Pub/Sub, Repository, DTO
DOCKER:   mysql + redis + kafka + backend + frontend
```

### Numbers to remember

| Constant | Value |
|----------|-------|
| Accept window | 30 seconds |
| Match radius | 10 km |
| Max drivers notified | 20 |
| GPS update interval | 5 seconds |
| JWT expiry | 10 hours (configurable) |
| Rate limit login | 60 req/min per IP |

### Files to open if interviewer asks "show me code"

1. `RideEventPublisher.java` — geo-targeted dispatch  
2. `RideEventBus.java` — WebSocket first, Kafka async  
3. `SecurityConfig.java` — role-based routes  
4. `NearbyDriverService.java` — Haversine matching  
5. `useRideSocket.js` — frontend WebSocket  
6. `LiveMap.jsx` — live GPS demo  

---

## Bonus: Role-Specific Questions

### Fresher / SDE-1

- REST vs WebSocket?  
- What is JWT?  
- SQL joins on duties + riders + drivers  
- React hooks you used  
- Git workflow / PR process  

### SDE-2 / Backend heavy

- Kafka consumer groups  
- Redis vs Kafka difference  
- Transaction isolation for accept ride  
- Caching pending duties (`@Cacheable`)  
- Actuator metrics you expose  

### Full stack / Product

- Why Rapido-style blue theme?  
- UX: finding driver overlay, 30s timer  
- How would you A/B test accept window?  

---

## Final Interview Tip

**Structure har answer:**

1. **Direct answer** (1 sentence)  
2. **How you built it** (2-3 sentences)  
3. **Trade-off or improvement** (1 sentence)  

**Example:**

> *"We use WebSocket for live updates because polling adds delay. I used STOMP topics per driver so only nearby drivers get notified. In production I'd add JWT on the WebSocket handshake and consider a dedicated notification service."*

---

*Good luck — project solid hai, bas flow confidently draw kar lo whiteboard par!*
