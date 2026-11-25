package com.mafioso.filter;

import com.mafioso.dao.UserDAO;
import com.mafioso.dao.UserDAOImpl;
import com.mafioso.dto.ApiResponse;
import com.mafioso.model.AuthenticatedUser;
import com.mafioso.model.User;
import com.mafioso.security.AuthorizationPolicy;
import com.mafioso.util.JsonUtil;
import com.mafioso.util.SessionUtil;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enforces server-side {@link javax.servlet.http.HttpSession} authentication on all non-public {@code /api/*}
 * endpoints and re-validates account status from MySQL so disabled accounts lose access immediately.
 */
public class AuthenticationFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(AuthenticationFilter.class.getName());
    private final UserDAO userDao;

    public AuthenticationFilter() {
        this(new UserDAOImpl());
    }

    public AuthenticationFilter(UserDAO userDao) {
        this.userDao = userDao;
    }

    @Override
    public void init(FilterConfig filterConfig) {
        // No initialization parameters required
    }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpResp = (HttpServletResponse) response;

        String path = extractApplicationPath(httpReq);
        if (AuthorizationPolicy.isPublicEndpoint(path, httpReq.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        AuthenticatedUser sessionUser = SessionUtil.getAuthenticatedUser(httpReq);
        if (sessionUser == null) {
            JsonUtil.writeJson(
                    httpResp,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    ApiResponse.error("UNAUTHORIZED", "Authentication required.")
            );
            return;
        }

        try {
            User persistedUser = userDao.findById(sessionUser.getId());
            if (persistedUser == null || persistedUser.getStatus() == null || !persistedUser.getStatus().isActive()) {
                SessionUtil.invalidateSession(httpReq, httpResp);
                JsonUtil.writeJson(
                        httpResp,
                        HttpServletResponse.SC_UNAUTHORIZED,
                        ApiResponse.error("UNAUTHORIZED", "Authenticated session is no longer active.")
                );
                return;
            }
            SessionUtil.refreshSessionPrincipal(httpReq, persistedUser);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to verify authenticated user status", ex);
            JsonUtil.writeJson(
                    httpResp,
                    HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    ApiResponse.error("SERVICE_UNAVAILABLE", "Unable to verify session state at this time.")
            );
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // No resources to release
    }

    static String extractApplicationPath(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && requestUri.startsWith(contextPath)) {
            return requestUri.substring(contextPath.length());
        }
        return requestUri;
    }
}
