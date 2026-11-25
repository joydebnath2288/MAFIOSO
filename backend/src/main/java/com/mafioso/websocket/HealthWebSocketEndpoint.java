package com.mafioso.websocket;

import com.mafioso.config.AppConfig;
import com.mafioso.util.JsonUtil;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Minimal JSR 356 WebSocket health endpoint used in Milestone 1 to verify
 * Nginx-to-Tomcat WebSocket upgrade proxying before live chat is implemented.
 */
@ServerEndpoint("/ws/health")
public class HealthWebSocketEndpoint {

    private static final Logger LOGGER = Logger.getLogger(HealthWebSocketEndpoint.class.getName());

    @OnOpen
    public void onOpen(Session session) throws IOException {
        sendEvent(session, "ws-connected", "CONNECTED");
    }

    @OnMessage
    public String onMessage(String message, Session session) {
        return buildEventJson("ws-pong", "CONNECTED");
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        LOGGER.log(Level.FINE, "WebSocket health session closed: {0}", session.getId());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        LOGGER.log(Level.WARNING, "WebSocket health session error", throwable);
    }

    private void sendEvent(Session session, String eventType, String status) throws IOException {
        if (session != null && session.isOpen()) {
            session.getBasicRemote().sendText(buildEventJson(eventType, status));
        }
    }

    private String buildEventJson(String eventType, String status) {
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("success", Boolean.TRUE);
        payload.put("type", eventType);
        payload.put("application", AppConfig.getAppName());
        payload.put("status", status);
        payload.put("timestamp", Instant.now().toString());
        return JsonUtil.toJson(payload);
    }
}
