import { useCallback, useEffect, useState } from 'react';
import {
  ActivityIcon,
  DatabaseIcon,
  GlobeIcon,
  RefreshIcon,
  ServerIcon,
} from '../components/Icons';
import { env } from '../config/env';
import {
  fetchBackendHealth,
  fetchDatabaseHealth,
  probeWebSocketHealth,
} from '../services/healthService';

function getBadgeClass(state) {
  if (state === 'UP') {
    return 'status-pill status-pill-up';
  }
  if (state === 'DOWN') {
    return 'status-pill status-pill-down';
  }
  return 'status-pill status-pill-pending';
}

export function HealthCheckPage() {
  const [checking, setChecking] = useState(true);
  const [lastChecked, setLastChecked] = useState(null);
  const [backendState, setBackendState] = useState({
    status: 'CHECKING',
    httpStatus: null,
    payload: null,
    error: null,
  });
  const [dbState, setDbState] = useState({
    status: 'CHECKING',
    httpStatus: null,
    payload: null,
    error: null,
  });
  const [wsState, setWsState] = useState({
    status: 'CHECKING',
    latencyMs: null,
    payload: null,
    error: null,
  });

  const runDiagnostics = useCallback(async () => {
    setChecking(true);
    setBackendState((prev) => ({ ...prev, status: 'CHECKING', error: null }));
    setDbState((prev) => ({ ...prev, status: 'CHECKING', error: null }));
    setWsState((prev) => ({ ...prev, status: 'CHECKING', error: null }));

    const [apiRes, dbRes, wsRes] = await Promise.all([
      fetchBackendHealth(),
      fetchDatabaseHealth(),
      probeWebSocketHealth(),
    ]);

    setBackendState({
      status: apiRes.ok && apiRes.data && apiRes.data.status === 'UP' ? 'UP' : 'DOWN',
      httpStatus: apiRes.status,
      payload: apiRes.data,
      error: apiRes.error,
    });

    setDbState({
      status: dbRes.ok && dbRes.data && dbRes.data.status === 'UP' ? 'UP' : 'DOWN',
      httpStatus: dbRes.status,
      payload: dbRes.data,
      error: dbRes.error,
    });

    setWsState({
      status: wsRes.ok ? 'UP' : 'DOWN',
      latencyMs: wsRes.latencyMs,
      payload: wsRes.data,
      error: wsRes.error,
    });

    setLastChecked(new Date().toLocaleTimeString());
    setChecking(false);
  }, []);

  useEffect(() => {
    runDiagnostics();
  }, [runDiagnostics]);

  const allHealthy =
    backendState.status === 'UP' && dbState.status === 'UP' && wsState.status === 'UP';

  return (
    <div className="container health-page">
      <div className="health-header">
        <div>
          <div className="mono-label">Same-Origin Infrastructure Probe</div>
          <h1 className="page-title">System Health Diagnostics</h1>
          <p className="page-subtitle">
            Verifies live React client rendering, Nginx reverse proxy routing, Tomcat 9 Servlet
            responses, MySQL 8 connectivity, and JSR-356 WebSocket upgrades.
          </p>
        </div>

        <div className="health-actions">
          <div className="last-checked">
            {lastChecked ? `Checked at ${lastChecked}` : 'Running initial check'}
          </div>
          <button
            type="button"
            className="btn btn-primary"
            onClick={runDiagnostics}
            disabled={checking}
          >
            <RefreshIcon className={checking ? 'spin' : ''} />
            <span>{checking ? 'Checking' : 'Re-run Diagnostics'}</span>
          </button>
        </div>
      </div>

      <div className="summary-strip">
        <div className="summary-indicator">
          <span
            className={`status-dot ${
              checking ? 'status-dot-pending' : allHealthy ? 'status-dot-up' : 'status-dot-down'
            }`}
            aria-hidden="true"
          />
          <span>
            {checking
              ? 'Verifying stack connectivity'
              : allHealthy
                ? 'All foundation services operational'
                : 'One or more foundation checks reported an issue'}
          </span>
        </div>
        <span className="mono-label">Origin: {window.location.origin}</span>
      </div>

      <div className="health-grid">
        {/* Frontend Status Card */}
        <article className="health-card">
          <div className="health-card-top">
            <div className="health-card-title-group">
              <GlobeIcon className="card-icon" />
              <div>
                <h2 className="health-card-title">Frontend Runtime</h2>
                <div className="health-card-endpoint">React + Vite SPA</div>
              </div>
            </div>
            <span className="status-pill status-pill-up">UP</span>
          </div>

          <dl className="metric-list">
            <div className="metric-row">
              <dt>Application</dt>
              <dd className="mono-val">{env.appName}</dd>
            </div>
            <div className="metric-row">
              <dt>Build Mode</dt>
              <dd className="mono-val">{env.mode}</dd>
            </div>
            <div className="metric-row">
              <dt>API Base Path</dt>
              <dd className="mono-val">{env.apiBaseUrl || '(same-origin relative)'}</dd>
            </div>
            <div className="metric-row">
              <dt>Router State</dt>
              <dd className="mono-val">Active ({window.location.pathname})</dd>
            </div>
          </dl>
        </article>

        {/* Backend API Status Card */}
        <article className="health-card">
          <div className="health-card-top">
            <div className="health-card-title-group">
              <ServerIcon className="card-icon" />
              <div>
                <h2 className="health-card-title">Backend REST API</h2>
                <div className="health-card-endpoint">GET /api/v1/health</div>
              </div>
            </div>
            <span className={getBadgeClass(backendState.status)}>{backendState.status}</span>
          </div>

          <dl className="metric-list">
            <div className="metric-row">
              <dt>HTTP Status</dt>
              <dd className="mono-val">{backendState.httpStatus ?? '—'}</dd>
            </div>
            <div className="metric-row">
              <dt>Application</dt>
              <dd className="mono-val">{backendState.payload?.application || '—'}</dd>
            </div>
            <div className="metric-row">
              <dt>Environment</dt>
              <dd className="mono-val">{backendState.payload?.environment || '—'}</dd>
            </div>
            <div className="metric-row">
              <dt>Version</dt>
              <dd className="mono-val">{backendState.payload?.version || '—'}</dd>
            </div>
          </dl>

          {backendState.error && <div className="health-error">{backendState.error}</div>}
        </article>

        {/* Database Connectivity Status Card */}
        <article className="health-card">
          <div className="health-card-top">
            <div className="health-card-title-group">
              <DatabaseIcon className="card-icon" />
              <div>
                <h2 className="health-card-title">Database Connectivity</h2>
                <div className="health-card-endpoint">GET /api/v1/health/database</div>
              </div>
            </div>
            <span className={getBadgeClass(dbState.status)}>{dbState.status}</span>
          </div>

          <dl className="metric-list">
            <div className="metric-row">
              <dt>HTTP Status</dt>
              <dd className="mono-val">{dbState.httpStatus ?? '—'}</dd>
            </div>
            <div className="metric-row">
              <dt>Database Schema</dt>
              <dd className="mono-val">{dbState.payload?.database || 'mafioso_db'}</dd>
            </div>
            <div className="metric-row">
              <dt>Query Latency</dt>
              <dd className="mono-val">
                {dbState.payload?.latencyMs != null ? `${dbState.payload.latencyMs} ms` : '—'}
              </dd>
            </div>
            <div className="metric-row">
              <dt>Schema Migration</dt>
              <dd className="mono-val">{dbState.payload?.schemaVersion || '—'}</dd>
            </div>
          </dl>

          {dbState.error && <div className="health-error">{dbState.error}</div>}
        </article>

        {/* WebSocket Proxy Card */}
        <article className="health-card">
          <div className="health-card-top">
            <div className="health-card-title-group">
              <ActivityIcon className="card-icon" />
              <div>
                <h2 className="health-card-title">WebSocket Proxy</h2>
                <div className="health-card-endpoint">WS /ws/health</div>
              </div>
            </div>
            <span className={getBadgeClass(wsState.status)}>{wsState.status}</span>
          </div>

          <dl className="metric-list">
            <div className="metric-row">
              <dt>Upgrade Protocol</dt>
              <dd className="mono-val">HTTP/1.1 → WebSocket</dd>
            </div>
            <div className="metric-row">
              <dt>Handshake Latency</dt>
              <dd className="mono-val">
                {wsState.latencyMs ? `${wsState.latencyMs} ms` : '—'}
              </dd>
            </div>
            <div className="metric-row">
              <dt>Tomcat Endpoint</dt>
              <dd className="mono-val">{wsState.payload?.status || '—'}</dd>
            </div>
            <div className="metric-row">
              <dt>Reserved Future Path</dt>
              <dd className="mono-val">/ws/chat</dd>
            </div>
          </dl>

          {wsState.error && <div className="health-error">{wsState.error}</div>}
        </article>
      </div>

      <section className="json-inspector" aria-label="Raw backend JSON responses">
        <div className="json-panel">
          <div className="json-panel-title">GET /api/v1/health response</div>
          <pre className="json-pre">
            {backendState.payload ? JSON.stringify(backendState.payload, null, 2) : 'No payload'}
          </pre>
        </div>
        <div className="json-panel">
          <div className="json-panel-title">GET /api/v1/health/database response</div>
          <pre className="json-pre">
            {dbState.payload ? JSON.stringify(dbState.payload, null, 2) : 'No payload'}
          </pre>
        </div>
      </section>
    </div>
  );
}
