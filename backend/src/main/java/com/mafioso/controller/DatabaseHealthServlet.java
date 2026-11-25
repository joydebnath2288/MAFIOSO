package com.mafioso.controller;

import com.mafioso.dto.DatabaseHealthResponse;
import com.mafioso.service.HealthCheckService;
import com.mafioso.util.JsonUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Exposes {@code GET /api/v1/health/database} to verify live MySQL connectivity.
 */
@WebServlet(name = "DatabaseHealthServlet", urlPatterns = {"/api/v1/health/database"})
public class DatabaseHealthServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final transient HealthCheckService healthCheckService = new HealthCheckService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        DatabaseHealthResponse response = healthCheckService.getDatabaseHealth();
        int statusCode = response.isSuccess()
                ? HttpServletResponse.SC_OK
                : HttpServletResponse.SC_SERVICE_UNAVAILABLE;
        JsonUtil.writeJson(resp, statusCode, response);
    }
}
