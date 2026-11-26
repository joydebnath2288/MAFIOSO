import { requestJson } from './apiClient';
import { ensureCsrfToken, getCsrfToken } from './apiClient';
import { env } from '../config/env';

export function fetchStudentDashboard() {
  return requestJson('/api/v1/student/dashboard', { method: 'GET' });
}

export function fetchStudentProfile() {
  return requestJson('/api/v1/student/profile', { method: 'GET' });
}

export function updateStudentProfile(payload) {
  return requestJson('/api/v1/student/profile', {
    method: 'PUT',
    body: payload,
  });
}

export function fetchStudentApplications({ status = 'ALL', page = 1, pageSize = 10 } = {}) {
  const params = new URLSearchParams();
  if (status && status !== 'ALL') {
    params.set('status', status);
  }
  params.set('page', String(page));
  params.set('pageSize', String(pageSize));
  return requestJson(`/api/v1/student/applications?${params.toString()}`, { method: 'GET' });
}

export function createStudentApplication(payload) {
  return requestJson('/api/v1/student/applications', {
    method: 'POST',
    body: payload,
  });
}

export function fetchStudentApplicationDetail(applicationId) {
  return requestJson(`/api/v1/student/applications/${applicationId}`, { method: 'GET' });
}

export function fetchStudentDocuments({ category = 'ALL', reviewStatus = 'ALL', applicationId = '' } = {}) {
  const params = new URLSearchParams();
  if (category && category !== 'ALL') {
    params.set('category', category);
  }
  if (reviewStatus && reviewStatus !== 'ALL') {
    params.set('reviewStatus', reviewStatus);
  }
  if (applicationId) {
    params.set('applicationId', String(applicationId));
  }
  const qs = params.toString();
  return requestJson(`/api/v1/student/documents${qs ? `?${qs}` : ''}`, { method: 'GET' });
}

export async function uploadStudentDocument({ file, category, applicationId = null }) {
  await ensureCsrfToken();
  const formData = new FormData();
  formData.append('category', category);
  formData.append('file', file);
  if (applicationId && !String(applicationId).trim() === false) {
    formData.append('applicationId', String(applicationId));
  }

  const targetPath = applicationId
    ? `/api/v1/student/applications/${applicationId}/documents`
    : '/api/v1/student/documents';

  const csrfToken = getCsrfToken();
  const headers = { Accept: 'application/json' };
  if (csrfToken) {
    headers['X-CSRF-Token'] = csrfToken;
  }

  try {
    const response = await fetch(`${env.apiBaseUrl}${targetPath}`, {
      method: 'POST',
      credentials: 'same-origin',
      headers,
      body: formData,
    });
    const contentType = response.headers.get('content-type') || '';
    const data = contentType.includes('application/json') ? await response.json() : null;
    return {
      ok: response.ok,
      status: response.status,
      data,
      fieldErrors: (data && data.fieldErrors) || null,
      error: response.ok ? null : (data && data.message) || `HTTP ${response.status}`,
    };
  } catch (err) {
    return {
      ok: false,
      status: 0,
      data: null,
      fieldErrors: null,
      error: (err && err.message) || 'Document upload failed',
    };
  }
}
