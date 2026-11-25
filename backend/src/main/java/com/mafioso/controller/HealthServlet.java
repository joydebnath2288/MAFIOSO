package com.mafioso.controller;

import com.mafioso.dto.HealthResponse;
import com.mafioso.service.HealthCheckService;
import com.mafioso.util.JsonUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Exposes {@code GET /api/v1/health} for runtime liveness verification.
 */
@WebServlet(name = "HealthServlet", urlPatterns = {"/api/v1/health"})
public class HealthServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final transient HealthCheckService healthCheckService = new HealthCheckService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HealthResponse response = healthCheckService.getApplicationHealth();
        JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, response);
    }
}
