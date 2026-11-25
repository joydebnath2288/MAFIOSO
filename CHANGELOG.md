# Changelog

All notable changes to MAFIOSO are documented in this file.

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
