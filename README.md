# Food Delivery Multivendor Platform

A full-stack, multi-vendor food delivery marketplace built to demonstrate
production-style architecture across the frontend, backend, and database
layers — inspired by real-world platforms like Swiggy, Zomato, and
DoorDash, scoped down to a learning-focused but architecturally honest
implementation.

## Overview

This platform supports multiple independent restaurants ("vendors")
operating on one shared system. Each restaurant manages its own menu,
customers can browse and order from any active restaurant, delivery
agents fulfill orders end-to-end, and admins oversee the whole platform —
all coordinated through a role-based access system and a stateless,
JWT-secured REST API.

## Tech Stack

**Backend**
- Java 17+
- Spring Boot 3.x — Web, Data JPA, Security, Validation
- Spring Security — JWT-based, stateless authentication + role-based
  method security (`@PreAuthorize`)
- MySQL Connector/J, Hibernate ORM
- Maven

**Frontend**
- React 18+ (Vite)
- React Router (client-side routing, protected/role-gated routes)
- Axios (with request/response interceptors)
- Plain CSS — no Tailwind, no preprocessor

**Database**
- MySQL — relational schema, foreign-key-enforced referential integrity,
  optimistic locking (`@Version`) on order claiming

## Core Features

### User Roles
- **Customer** — browses restaurants, places orders, tracks delivery
  status, views order history, receives status notifications
- **Restaurant Owner** — manages their restaurant profile, menu, opens
  and closes their shop, views and progresses incoming orders
- **Delivery Agent** — views available orders, claims one, marks it
  delivered
- **Admin** — views all users/restaurants/orders, can force
  activate/deactivate any restaurant

### Authentication & Security
- Stateless JWT authentication (role embedded as a signed claim)
- Passwords hashed with BCrypt
- Ownership-based authorization (a restaurant owner can only manage
  *their own* restaurant/menu/orders)
- Role-based authorization via `@PreAuthorize` for admin-only endpoints
- Centralized global exception handling (400/403/409 mapped from
  business exceptions)
- CORS and JWT secret externalized via environment variables

### Multi-Vendor Marketplace
- Each restaurant is owned by a single `RESTAURANT_OWNER`
- Restaurants independently manage their own menu items
- Restaurant owners can open/close their shop; customers see current
  status rather than restaurants silently disappearing
- Customer-facing search (by name) and client-side menu filtering

### Order Lifecycle
- Transactional order placement with **server-side price snapshotting**
  (client never sends prices — only item IDs and quantities)
- Full status tracking: `PLACED → PREPARING → OUT_FOR_DELIVERY →
  DELIVERED` (`CANCELLED` as an exit state)
- Optimistic locking (`@Version`) prevents two delivery agents from
  claiming the same order
- Polling-based in-app notifications on status changes, per role

## Architecture

The backend follows a standard layered architecture:

    Controller → Service → Repository → MySQL

- **Controllers** handle HTTP requests/responses only
- **Services** contain all business rules and authorization checks
- **Repositories** (Spring Data JPA) handle all database access via
  query derivation
- **DTOs** (Java `record`s) decouple the API's public contract from
  internal entities — no entity is ever directly serialized to a client

The frontend communicates with the backend exclusively through REST
endpoints under `/api/**`, authenticating via a JWT attached to every
request through an Axios interceptor, with a matching interceptor
handling global session expiry (401 → auto logout).

## Database Schema (Core Tables)

- `users` — all platform users, differentiated by `role`
- `restaurants` — vendor profiles, linked to an owning user
- `menu_items` — dishes, linked to one restaurant
- `orders` — customer orders, linking a customer, a restaurant, and
  (once assigned) a delivery agent; includes `version` (optimistic
  locking) and `status_updated_at` (notifications)
- `order_items` — line items, with `price_at_order` snapshotting

## Project Structure

```
food-delivery-multivendor/
├── food-delivery-backend/       # Spring Boot API
│   ├── src/main/java/.../
│   │   ├── model/                # JPA entities
│   │   ├── repository/           # Spring Data JPA repositories
│   │   ├── service/               # Business logic + authorization
│   │   ├── controller/            # REST endpoints
│   │   ├── dto/                   # Request/response records
│   │   ├── security/               # JWT service + filter
│   │   ├── config/                 # Security + CORS config
│   │   └── exception/               # Global exception handler
│   ├── src/main/resources/application.properties
│   └── Dockerfile
├── food-delivery-frontend/       # React (Vite) SPA
│   ├── src/
│   │   ├── pages/                  # Route-level components
│   │   ├── components/              # Reusable UI components
│   │   ├── services/                 # Centralized API call modules
│   │   ├── context/                   # AuthContext (global auth state)
│   │   ├── hooks/                      # useNotifications, etc.
│   │   └── api/axiosInstance.js         # Configured Axios client
│   ├── .env / .env.production
│   └── Dockerfile
├── docker-compose.yml
└── README.md
```

## Getting Started

### Option A — Docker (recommended)

Requires only Docker and Docker Compose installed.

```bash
git clone <your-repo-url>
cd food-delivery-multivendor
cp .env.example .env      # then edit values as needed
docker compose up --build
```

This starts MySQL, the Spring Boot backend, and the React frontend
together, fully wired. Once running:

- Frontend: http://localhost:5173
- Backend API: http://localhost:8080/api
- MySQL: localhost:3306

### Option B — Manual local setup

**Prerequisites:** Java 17+, Maven, MySQL 8+, Node.js 18+

**Database**
```sql
CREATE DATABASE food_delivery_db;
```

**Backend**
```bash
cd food-delivery-backend
# set DB_URL, DB_USERNAME, DB_PASSWORD, JWT_SECRET as environment
# variables, or edit application.properties directly for local dev
mvn spring-boot:run
```

**Frontend**
```bash
cd food-delivery-frontend
cp .env.example .env    # set VITE_API_URL=http://localhost:8080/api
npm install
npm run dev
```

## Environment Variables

| Variable | Used by | Description |
|---|---|---|
| `DB_URL` | backend | JDBC connection string |
| `DB_USERNAME` | backend | MySQL username |
| `DB_PASSWORD` | backend | MySQL password |
| `JWT_SECRET` | backend | Signing key for JWTs — generate with `openssl rand -base64 32` |
| `FRONTEND_URL` | backend | Allowed CORS origin |
| `VITE_API_URL` | frontend | Base URL the React app calls |

Never commit real secret values — `.env` files are gitignored; use
`.env.example` as the template.

## API Overview

| Area | Base path | Notes |
|---|---|---|
| Auth | `/api/auth` | register, login |
| Restaurants | `/api/restaurants` | browse, search, CRUD (owner-scoped), status toggle |
| Menu items | `/api/restaurants/{id}/menu-items` | browse, add/update (owner-scoped) |
| Orders | `/api/restaurants/{id}/orders` | place, view (owner-scoped), status transitions |
| Customer orders | `/api/customers/orders` | a customer's own order history |
| Delivery | `/api/orders` | available, mine, claim, deliver |
| Notifications | `/api/notifications` | polling endpoint, `?since=<ISO timestamp>` |
| Admin | `/api/admin` | users, restaurants, orders — `ROLE_ADMIN` only |

## Project Status

Built incrementally as a guided, phase-by-phase learning project:

**Completed:** database design, authentication (JWT), restaurant/menu
management, order placement with transactional price safety, full order
lifecycle, delivery claiming with optimistic locking, admin panel,
customer order history, notifications, search/filtering, environment
externalization, Docker + deployment setup.

**Possible future work:** refresh tokens, pagination on list endpoints,
full-text restaurant search, structured logging, automated tests.

## License

Personal/portfolio project — license terms to be finalized.