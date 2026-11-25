import { env } from '../config/env';

/**
 * Shared HTTP client using relative same-origin paths through Nginx.
 */
export async function requestJson(path, options = {}) {
  const { timeoutMs = 8000, headers = {}, ...rest } = options;
  const controller = new AbortController();
  const timeoutId = window.setTimeout(() => controller.abort(), timeoutMs);

  const url = `${env.apiBaseUrl}${path}`;

  try {
    const response = await fetch(url, {
      ...rest,
      headers: {
        Accept: 'application/json',
        ...headers,
      },
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

    return {
      ok: response.ok,
      status: response.status,
      data,
      error: response.ok ? null : (data && data.message) || `HTTP ${response.status}`,
    };
  } catch (err) {
    const isTimeout = err && err.name === 'AbortError';
    return {
      ok: false,
      status: 0,
      data: null,
      error: isTimeout ? 'Request timed out' : (err && err.message) || 'Network request failed',
    };
  } finally {
    window.clearTimeout(timeoutId);
  }
}
