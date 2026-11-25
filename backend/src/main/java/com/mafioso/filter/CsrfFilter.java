package com.mafioso.filter;

import com.mafioso.dto.ApiResponse;
import com.mafioso.util.CsrfTokenUtil;
import com.mafioso.util.JsonUtil;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Validates session-bound CSRF tokens on all state-changing HTTP methods
 * ({@code POST}, {@code PUT}, {@code PATCH}, {@code DELETE}) under {@code /api/*}.
 */
public class CsrfFilter implements Filter {

    private static final Set<String> SAFE_METHODS = Collections.unmodifiableSet(
            new HashSet<String>(Arrays.asList("GET", "HEAD", "OPTIONS", "TRACE"))
    );
    private static final Set<String> PROTECTED_METHODS = Collections.unmodifiableSet(
            new HashSet<String>(Arrays.asList("POST", "PUT", "PATCH", "DELETE"))
    );

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

        String method = httpReq.getMethod() == null ? "" : httpReq.getMethod().toUpperCase(Locale.ROOT);
        if (SAFE_METHODS.contains(method)) {
            chain.doFilter(request, response);
            return;
        }

        if (!PROTECTED_METHODS.contains(method)) {
            JsonUtil.writeJson(
                    httpResp,
                    HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                    ApiResponse.error("METHOD_NOT_ALLOWED", "HTTP method is not supported.")
            );
            return;
        }

        HttpSession session = httpReq.getSession(false);
        String providedToken = httpReq.getHeader(CsrfTokenUtil.HEADER_NAME);

        if (!CsrfTokenUtil.isValid(session, providedToken)) {
            JsonUtil.writeJson(
                    httpResp,
                    HttpServletResponse.SC_FORBIDDEN,
                    ApiResponse.error(
                            "CSRF_INVALID",
                            "Invalid or missing CSRF token. Refresh your session and try again."
                    )
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
