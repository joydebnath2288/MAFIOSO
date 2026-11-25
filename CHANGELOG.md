# Changelog

All notable changes to MAFIOSO are documented in this file.

## [0.2.0] - 2026-11-25 — Milestone 2: Authentication & Role-Based Authorization

### Added
- Forward-only `V002__users_and_auth_schema.sql` migration creating the `users` table with unique normalized email, role/status check constraints, and startup `DatabaseMigrationRunner` for already-initialized volumes.
- BCrypt password hashing (`at.favre.lib:bcrypt:0.10.2`), server-side `ValidationUtil`, `LoginRateLimiter`, and `UserDAO`/`AuthService` layered backend.
- Server-side `HttpSession` authentication (`AuthenticationFilter`), session-bound `X-CSRF-Token` validation (`CsrfFilter`), centralized role authorization (`AuthorizationFilter` / `AuthorizationPolicy`), and `SecurityHeadersFilter` ordered in `WEB-INF/web.xml`.
- Tomcat 9 `META-INF/context.xml` configuring `SameSite=Lax` cookies and `RemoteIpValve` for Nginx reverse proxy headers.
- Authentication endpoints (`GET /api/v1/auth/csrf`, `POST /api/v1/auth/register`, `POST /api/v1/auth/login`, `POST /api/v1/auth/logout`, `GET /api/v1/auth/me`) and role-protected placeholder endpoints (`/api/v1/student/dashboard`, `/api/v1/consultant/dashboard`, `/api/v1/admin/dashboard`).
- Secure one-time initial administrator bootstrap service and CLI utilities (`AdminBootstrapCli`, `ConsultantProvisionCli`) with zero hardcoded passwords.
- React `AuthContext`, `useAuth` hook, `ProtectedRoute`, `PublicOnlyRoute`, `/login`, `/register`, `/student/dashboard`, `/staff/dashboard`, and `/unauthorized` views with in-memory CSRF token management.

## [0.1.0] - 2026-11-25 — Milestone 1: Project Foundation & Dockerization

### Added
- Java 8 / Servlet 4.0 Maven WAR backend (`ROOT.war`) compatible with Apache Tomcat 9.
- Environment-driven `DatabaseConfig` and reusable JDBC `DatabaseUtil` for MySQL 8.
- Thread-safe Jackson `JsonUtil` and DTO payloads.
- Health-check endpoints (`GET /api/v1/health`, `GET /api/v1/health/database`) and WebSocket proxy probe (`WS /ws/health`).
- React + Vite + React Router frontend with initial landing page and live system health diagnostics page.
- Multi-stage Dockerfiles for backend (`maven:3.9.9-eclipse-temurin-8` -> `tomcat:9.0.124-jdk8-temurin-noble`) and frontend (`node:22.23.5-alpine3.23` -> `nginx:1.28.3-alpine3.23`).
- Nginx reverse proxy configuration for SPA fallback, `/api/`, and `/ws/` WebSocket upgrades.
- `compose.yaml` orchestrating `frontend`, `backend`, and `db` (`mysql:8.0.46-oraclelinux9`) on an isolated internal network with persistent volumes for MySQL and private student document storage.
- Minimal MySQL initialization scripts (`database/schema.sql`, `database/seed.sql`, `database/init/`, `database/migrations/`).
- Architecture, database design, Docker, and development guides under `docs/` and `docker/`.
