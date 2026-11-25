export const env = {
  appName: import.meta.env.VITE_APP_NAME || 'MAFIOSO',
  apiBaseUrl: (import.meta.env.VITE_API_BASE_URL || '').replace(/\/+$/, ''),
  mode: import.meta.env.MODE,
  milestone: 'Milestone 1',
};
