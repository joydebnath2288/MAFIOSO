package com.mafioso.util;

import com.mafioso.config.AppConfig;
import com.mafioso.model.AuthenticatedUser;
import com.mafioso.model.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Helper for managing server-side {@link HttpSession} authentication state,
 * session ID rotation, and cookie clearance on logout.
 * Never logs session IDs or returns them in response payloads.
 */
public final class SessionUtil {

    public static final String SESSION_USER_ATTR = "MAFIOSO_AUTH_USER";
    public static final String REQUEST_USER_ATTR = "MAFIOSO_REQUEST_USER";
    public static final String SESSION_COOKIE_NAME = "JSESSIONID";

    private SessionUtil() {
    }

    public static AuthenticatedUser getAuthenticatedUser(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Object requestUser = request.getAttribute(REQUEST_USER_ATTR);
        if (requestUser instanceof AuthenticatedUser) {
            return (AuthenticatedUser) requestUser;
        }
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object attr = session.getAttribute(SESSION_USER_ATTR);
        return attr instanceof AuthenticatedUser ? (AuthenticatedUser) attr : null;
    }

    /**
     * Establishes an authenticated session, rotates the session ID to mitigate session fixation,
     * configures inactivity timeout, stores the safe user principal, and issues a fresh CSRF token.
     *
     * @param request current HTTP request
     * @param user authenticated domain user
     * @return fresh CSRF token bound to the rotated session
     */
    public static String establishAuthenticatedSession(HttpServletRequest request, User user) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            session = request.getSession(true);
        } else {
            request.changeSessionId();
            session = request.getSession(false);
            if (session == null) {
                session = request.getSession(true);
            }
        }

        session.setMaxInactiveInterval(AppConfig.getSessionTimeoutSeconds());
        AuthenticatedUser principal = AuthenticatedUser.fromUser(user);
        session.setAttribute(SESSION_USER_ATTR, principal);
        request.setAttribute(REQUEST_USER_ATTR, principal);
        return CsrfTokenUtil.rotateToken(session);
    }

    /**
     * Updates the cached principal stored on an existing active session (e.g., when refreshing user status/name).
     */
    public static void refreshSessionPrincipal(HttpServletRequest request, User user) {
        AuthenticatedUser principal = AuthenticatedUser.fromUser(user);
        request.setAttribute(REQUEST_USER_ATTR, principal);
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.setAttribute(SESSION_USER_ATTR, principal);
        }
    }

    /**
     * Invalidates the server-side session and instructs the client to clear {@code JSESSIONID}.
     */
    public static void invalidateSession(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            try {
                session.invalidate();
            } catch (IllegalStateException ignored) {
                // Already invalidated
            }
        }
        request.removeAttribute(REQUEST_USER_ATTR);
        clearSessionCookie(request, response);
    }

    public static void clearSessionCookie(HttpServletRequest request, HttpServletResponse response) {
        if (response == null) {
            return;
        }
        StringBuilder cookie = new StringBuilder();
        cookie.append(SESSION_COOKIE_NAME).append("=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax");
        if (shouldUseSecureCookie(request)) {
            cookie.append("; Secure");
        }
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public static boolean shouldUseSecureCookie(HttpServletRequest request) {
        if (AppConfig.isSessionCookieSecure()) {
            return true;
        }
        return request != null && request.isSecure();
    }
}
