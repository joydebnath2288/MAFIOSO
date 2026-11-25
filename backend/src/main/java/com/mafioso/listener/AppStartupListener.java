package com.mafioso.listener;

import com.mafioso.bootstrap.AdminBootstrapService;
import com.mafioso.config.AppConfig;
import com.mafioso.util.DatabaseMigrationRunner;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.SessionCookieConfig;
import javax.servlet.annotation.WebListener;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Configures session cookie security attributes, applies pending forward-only database migrations,
 * and runs opt-in environment administrator bootstrap when the application context starts.
 */
@WebListener
public class AppStartupListener implements ServletContextListener {

    private static final Logger LOGGER = Logger.getLogger(AppStartupListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        configureSessionCookies(context);

        try {
            DatabaseMigrationRunner.migrate();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Database migration execution failed during startup", ex);
            throw new IllegalStateException("Failed to apply database migrations at startup", ex);
        }

        new AdminBootstrapService().bootstrapFromEnvironmentIfEnabled();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // No cleanup required
    }

    private static void configureSessionCookies(ServletContext context) {
        SessionCookieConfig cookieConfig = context.getSessionCookieConfig();
        cookieConfig.setHttpOnly(true);
        cookieConfig.setPath("/");
        cookieConfig.setSecure(AppConfig.isSessionCookieSecure());
        context.setSessionTimeout(AppConfig.getSessionTimeoutMinutes());
        LOGGER.log(Level.INFO,
                "Configured HttpSession timeout={0}m, HttpOnly=true, Secure={1}",
                new Object[]{AppConfig.getSessionTimeoutMinutes(), AppConfig.isSessionCookieSecure()});
    }
}
