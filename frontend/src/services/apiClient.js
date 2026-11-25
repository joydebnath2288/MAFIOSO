import { env } from '../config/env';

const MUTATING_METHODS = new Set(['POST', 'PUT', 'PATCH', 'DELETE']);

// CSRF token is kept strictly in memory (never in localStorage or sessionStorage).
let inMemoryCsrfToken = null;
let csrfFetchPromise = null;
let unauthorizedHandler = null;

export function getCsrfToken() {
  return inMemoryCsrfToken;
}

export function setCsrfToken(token) {
  inMemoryCsrfToken = token || null;
}

export function clearCsrfToken() {
  inMemoryCsrfToken = null;
}

export function setUnauthorizedHandler(handler) {
  unauthorizedHandler = typeof handler === 'function' ? handler : null;
}

export async function ensureCsrfToken() {
  if (inMemoryCsrfToken) {
    return inMemoryCsrfToken;
  }
  if (!csrfFetchPromise) {
    csrfFetchPromise = fetchCsrfFromServer().finally(() => {
      csrfFetchPromise = null;
    });
  }
  return csrfFetchPromise;
}

async function fetchCsrfFromServer() {
  const url = `${env.apiBaseUrl}/api/v1/auth/csrf`;
  const response = await fetch(url, {
    method: 'GET',
    credentials: 'same-origin',
    headers: {
      Accept: 'application/json',
    },
  });
  if (!response.ok) {
    return null;
  }
  const contentType = response.headers.get('content-type') || '';
  if (!contentType.includes('application/json')) {
    return null;
  }
  const payload = await response.json();
  const token = payload && payload.data && payload.data.csrfToken;
  if (token) {
    setCsrfToken(token);
  }
  return token || null;
}

/**
 * Shared HTTP client using relative same-origin paths through Nginx with
 * HttpOnly session credentials and automatic X-CSRF-Token injection.
 */
export async function requestJson(path, options = {}) {
  const {
    timeoutMs = 8000,
    headers = {},
    body,
    skipCsrf = false,
    suppressUnauthorizedEvent = false,
    ...rest
  } = options;

  const method = (rest.method || 'GET').toUpperCase();
  const isMutating = MUTATING_METHODS.has(method);

  if (isMutating && !skipCsrf) {
    await ensureCsrfToken();
  }

  const controller = new AbortController();
  const timeoutId = window.setTimeout(() => controller.abort(), timeoutMs);
  const url = `${env.apiBaseUrl}${path}`;

  const requestHeaders = {
    Accept: 'application/json',
    ...headers,
  };

  let serializedBody = body;
  if (
    body !== undefined &&
    body !== null &&
    typeof body === 'object' &&
    !(body instanceof FormData) &&
    !(body instanceof Blob)
  ) {
    serializedBody = JSON.stringify(body);
    if (!requestHeaders['Content-Type']) {
      requestHeaders['Content-Type'] = 'application/json';
    }
  }

  if (isMutating && !skipCsrf && inMemoryCsrfToken) {
    requestHeaders['X-CSRF-Token'] = inMemoryCsrfToken;
  }

  try {
    const response = await fetch(url, {
      ...rest,
      method,
      body: serializedBody,
      credentials: 'same-origin',
      headers: requestHeaders,
      signal: controller.signal,
    });

    const contentType = response.headers.get('content-type') || '';
    let data = null;

    if (contentType.includes('application/json')) {
      data = await response.json();
    } else {
      const text = await response.text();
      data = { raw: text };
    }

    if (response.status === 401 && !suppressUnauthorizedEvent && unauthorizedHandler) {
      unauthorizedHandler();
    }

    return {
      ok: response.ok,
      status: response.status,
      data,
      fieldErrors: (data && data.fieldErrors) || null,
      error: response.ok ? null : (data && data.message) || `HTTP ${response.status}`,
    };
  } catch (err) {
    const isTimeout = err && err.name === 'AbortError';
    return {
      ok: false,
      status: 0,
      data: null,
      fieldErrors: null,
      error: isTimeout ? 'Request timed out' : (err && err.message) || 'Network request failed',
    };
  } finally {
    window.clearTimeout(timeoutId);
  }
}
