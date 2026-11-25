import { requestJson } from './apiClient';

export function fetchBackendHealth() {
  return requestJson('/api/v1/health');
}

export function fetchDatabaseHealth() {
  return requestJson('/api/v1/health/database');
}

/**
 * Probes Nginx -> Tomcat JSR-356 WebSocket upgrade on /ws/health.
 */
export function probeWebSocketHealth(timeoutMs = 5000) {
  return new Promise((resolve) => {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsUrl = `${protocol}//${window.location.host}/ws/health`;
    const startedAt = performance.now();
    let settled = false;
    let socket;

    const finish = (result) => {
      if (settled) {
        return;
      }
      settled = true;
      window.clearTimeout(timer);
      try {
        if (socket && socket.readyState === WebSocket.OPEN) {
          socket.close(1000, 'healthcheck-complete');
        }
      } catch {
        // Ignore close cleanup errors
      }
      resolve(result);
    };

    const timer = window.setTimeout(() => {
      finish({
        ok: false,
        status: 'DOWN',
        latencyMs: 0,
        data: null,
        error: 'WebSocket handshake timed out',
      });
    }, timeoutMs);

    try {
      socket = new WebSocket(wsUrl);

      socket.onopen = () => {
        socket.send('ping');
      };

      socket.onmessage = (event) => {
        const latencyMs = Math.max(1, Math.round(performance.now() - startedAt));
        let parsed = null;
        try {
          parsed = JSON.parse(event.data);
        } catch {
          parsed = { raw: event.data };
        }
        if (parsed && parsed.type === 'ws-pong') {
          finish({
            ok: true,
            status: 'UP',
            latencyMs,
            data: parsed,
            error: null,
          });
        }
      };

      socket.onerror = () => {
        finish({
          ok: false,
          status: 'DOWN',
          latencyMs: 0,
          data: null,
          error: 'WebSocket upgrade failed',
        });
      };

      socket.onclose = (event) => {
        if (!settled && !event.wasClean) {
          finish({
            ok: false,
            status: 'DOWN',
            latencyMs: 0,
            data: null,
            error: `Connection closed (${event.code})`,
          });
        }
      };
    } catch (err) {
      finish({
        ok: false,
        status: 'DOWN',
        latencyMs: 0,
        data: null,
        error: (err && err.message) || 'Unable to create WebSocket',
      });
    }
  });
}
