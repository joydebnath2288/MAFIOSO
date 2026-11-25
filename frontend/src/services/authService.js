import { clearCsrfToken, requestJson, setCsrfToken } from './apiClient';

export async function fetchCsrfToken() {
  const res = await requestJson('/api/v1/auth/csrf', {
    method: 'GET',
    suppressUnauthorizedEvent: true,
  });
  const token = res.ok && res.data && res.data.data && res.data.data.csrfToken;
  if (token) {
    setCsrfToken(token);
  }
  return token || null;
}

export function fetchCurrentUser() {
  return requestJson('/api/v1/auth/me', {
    method: 'GET',
    suppressUnauthorizedEvent: true,
  });
}

export async function registerStudent(payload) {
  const res = await requestJson('/api/v1/auth/register', {
    method: 'POST',
    body: {
      fullName: payload.fullName,
      email: payload.email,
      password: payload.password,
      confirmPassword: payload.confirmPassword,
      phoneNumber: payload.phoneNumber || null,
    },
    suppressUnauthorizedEvent: true,
  });
  const nextCsrf = res.ok && res.data && res.data.data && res.data.data.csrfToken;
  if (nextCsrf) {
    setCsrfToken(nextCsrf);
  }
  return res;
}

export async function loginUser(credentials) {
  const res = await requestJson('/api/v1/auth/login', {
    method: 'POST',
    body: {
      email: credentials.email,
      password: credentials.password,
    },
    suppressUnauthorizedEvent: true,
  });
  const nextCsrf = res.ok && res.data && res.data.data && res.data.data.csrfToken;
  if (nextCsrf) {
    setCsrfToken(nextCsrf);
  }
  return res;
}

export async function logoutUser() {
  const res = await requestJson('/api/v1/auth/logout', {
    method: 'POST',
    suppressUnauthorizedEvent: true,
  });
  clearCsrfToken();
  await fetchCsrfToken();
  return res;
}

export function fetchStudentDashboard() {
  return requestJson('/api/v1/student/dashboard', { method: 'GET' });
}

export function fetchStudentProfile() {
  return requestJson('/api/v1/student/profile', { method: 'GET' });
}

export function fetchConsultantDashboard() {
  return requestJson('/api/v1/consultant/dashboard', { method: 'GET' });
}

export function fetchAssignedStudents() {
  return requestJson('/api/v1/consultant/assigned-students', { method: 'GET' });
}

export function fetchAdminDashboard() {
  return requestJson('/api/v1/admin/dashboard', { method: 'GET' });
}
