package com.mafioso.filter;

import com.mafioso.dto.ApiResponse;
import com.mafioso.model.AuthenticatedUser;
import com.mafioso.model.Role;
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
import java.util.Set;

/**
 * Enforces centralized role-based access rules from {@link AuthorizationPolicy} across {@code /api/*}.
 */
public class AuthorizationFilter implements Filter {

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

        String path = AuthenticationFilter.extractApplicationPath(httpReq);
        if (AuthorizationPolicy.isPublicEndpoint(path, httpReq.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        Set<Role> requiredRoles = AuthorizationPolicy.getRequiredRoles(path);
        if (requiredRoles == null || requiredRoles.isEmpty()) {
            chain.doFilter(request, response);
            return;
        }

        AuthenticatedUser principal = SessionUtil.getAuthenticatedUser(httpReq);
        if (principal == null) {
            JsonUtil.writeJson(
                    httpResp,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    ApiResponse.error("UNAUTHORIZED", "Authentication required.")
            );
            return;
        }

        if (!AuthorizationPolicy.isAuthorized(path, principal.getRole())) {
            JsonUtil.writeJson(
                    httpResp,
                    HttpServletResponse.SC_FORBIDDEN,
                    ApiResponse.error("FORBIDDEN", "You do not have permission to access this resource.")
            );
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // No resources to release
    }
}
