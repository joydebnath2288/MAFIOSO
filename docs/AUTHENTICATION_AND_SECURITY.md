# MAFIOSO Authentication, Session, CSRF, and Admin Bootstrap Guide

## 1. Authentication & Session Architecture

MAFIOSO uses stateful, server-side HTTP sessions (`javax.servlet.http.HttpSession`) managed by Apache Tomcat 9 behind the Nginx same-origin reverse proxy:

- **Cookie attributes:** `JSESSIONID` is configured with `HttpOnly=true`, `Path=/`, `SameSite=Lax` (via `META-INF/context.xml` `Rfc6265CookieProcessor`), and `Secure=true` when running in production (`APP_ENV=production` or `SESSION_COOKIE_SECURE=true` or HTTPS `X-Forwarded-Proto: https`).
- **Session ID rotation:** Upon successful login (`POST /api/v1/auth/login`), `SessionUtil.establishAuthenticatedSession` invokes `request.changeSessionId()` to mitigate session fixation attacks.
- **Inactivity timeout:** Defaults to 30 minutes (`SESSION_TIMEOUT_MINUTES`, configurable via `SESSION_TIMEOUT_MINUTES` or `SESSION_TIMEOUT_SECONDS`).
- **Logout invalidation:** `POST /api/v1/auth/logout` invalidates the server-side `HttpSession` and emits an expired `Set-Cookie: JSESSIONID=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax` header.
- **No client token storage:** Passwords, password hashes, and session identifiers are never stored in `localStorage` or returned in API JSON responses.

## 2. How React Obtains and Sends the CSRF Token

All state-changing API requests (`POST`, `PUT`, `PATCH`, `DELETE`) under `/api/*` are protected by `CsrfFilter`:

1. **Initialization:** On application startup (`AuthProvider` in `frontend/src/context/AuthContext.jsx`), React calls `GET /api/v1/auth/me` and `GET /api/v1/auth/csrf` with `credentials: 'same-origin'`.
2. **In-memory caching:** `apiClient.js` stores the returned `csrfToken` strictly in a JavaScript module variable (`inMemoryCsrfToken`), never in `localStorage` or `sessionStorage`.
3. **Mutating requests:** Before any `POST`, `PUT`, `PATCH`, or `DELETE` call, `requestJson` ensures a CSRF token exists (`ensureCsrfToken()`) and attaches it in the `X-CSRF-Token` request header alongside the `same-origin` `JSESSIONID` cookie.
4. **Rotation after login / registration:** When `POST /api/v1/auth/login` rotates the `HttpSession` ID, the backend issues a fresh CSRF token bound to the rotated session and returns it in `data.csrfToken`. `authService.js` updates `inMemoryCsrfToken` immediately.
5. **Logout refresh:** After `POST /api/v1/auth/logout`, `authService.js` clears the in-memory token and requests a new pre-authentication CSRF token from `GET /api/v1/auth/csrf`.

## 3. Initial Administrator Bootstrap (Dockerized Environment)

No administrator credentials or default passwords exist in SQL files or source code.

### Recommended: Interactive CLI Inside the Running Backend Container

Password input is hidden via `System.console().readPassword()` and never appears in `.env` files or shell history:

```bash
docker compose exec -it backend \
  java -cp "/usr/local/tomcat/webapps/ROOT/WEB-INF/classes:/usr/local/tomcat/webapps/ROOT/WEB-INF/lib/*" \
  com.mafioso.bootstrap.AdminBootstrapCli
```

The utility refuses to create a second administrator if any `ADMIN` row already exists in `users`.

### Alternative: One-Time Environment Variables (Must Be Removed Afterward)

1. Temporarily pass `MAFIOSO_BOOTSTRAP_ADMIN_ENABLED=true`, `MAFIOSO_BOOTSTRAP_ADMIN_NAME`, `MAFIOSO_BOOTSTRAP_ADMIN_EMAIL`, and `MAFIOSO_BOOTSTRAP_ADMIN_PASSWORD` (or `MAFIOSO_BOOTSTRAP_ADMIN_PASSWORD_FILE` mounted as a `0400` Docker secret file) when starting `backend`.
2. Verify the startup logs show the initial `ADMIN` account was provisioned.
3. **Disable and remove bootstrap secrets immediately:**
   - Remove `MAFIOSO_BOOTSTRAP_ADMIN_NAME`, `MAFIOSO_BOOTSTRAP_ADMIN_EMAIL`, `MAFIOSO_BOOTSTRAP_ADMIN_PASSWORD`, and set `MAFIOSO_BOOTSTRAP_ADMIN_ENABLED=false` in `.env`.
   - Recreate the backend container so the secrets are cleared from the container's runtime environment:
     ```bash
     docker compose up -d backend
     ```

### Provisioning Consultant Staff Accounts (Milestone 2 CLI)

`CONSULTANT` accounts cannot register publicly. Before the admin staff-management UI is built in future milestones, provision a consultant securely inside `backend` via:

```bash
docker compose exec -it backend \
  java -cp "/usr/local/tomcat/webapps/ROOT/WEB-INF/classes:/usr/local/tomcat/webapps/ROOT/WEB-INF/lib/*" \
  com.mafioso.bootstrap.ConsultantProvisionCli
```
