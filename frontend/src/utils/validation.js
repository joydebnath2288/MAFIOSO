export const EMAIL_REGEX = /^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9-]+(?:\.[A-Za-z0-9-]+)+$/;
export const PHONE_REGEX = /^\+?[0-9\s\-()]{7,20}$/;

export function evaluatePasswordRules(password) {
  const safe = password || '';
  return [
    {
      key: 'length',
      label: '8 to 72 characters',
      passed: safe.length >= 8 && new TextEncoder().encode(safe).length <= 72,
    },
    {
      key: 'case',
      label: 'Uppercase and lowercase letters',
      passed: /[A-Z]/.test(safe) && /[a-z]/.test(safe),
    },
    {
      key: 'digit',
      label: 'At least one number',
      passed: /[0-9]/.test(safe),
    },
    {
      key: 'special',
      label: 'At least one special character',
      passed: /[^A-Za-z0-9\s]/.test(safe),
    },
  ];
}

export function validatePhoneInput(phone) {
  const trimmed = (phone || '').trim();
  if (!trimmed) {
    return null;
  }
  if (!PHONE_REGEX.test(trimmed)) {
    return 'Enter a valid phone number (7 to 15 digits).';
  }
  const digitCount = (trimmed.match(/[0-9]/g) || []).length;
  if (digitCount < 7 || digitCount > 15) {
    return 'Enter a valid phone number (7 to 15 digits).';
  }
  return null;
}

export function formatDateTime(isoValue) {
  if (!isoValue) {
    return '—';
  }
  const date = new Date(isoValue);
  if (Number.isNaN(date.getTime())) {
    return isoValue;
  }
  return date.toLocaleString(undefined, {
    year: 'numeric',
    month: 'short',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export function formatDateOnly(isoValue) {
  if (!isoValue) {
    return '—';
  }
  const date = new Date(isoValue);
  if (Number.isNaN(date.getTime())) {
    return isoValue;
  }
  return date.toLocaleDateString(undefined, {
    year: 'numeric',
    month: 'short',
    day: '2-digit',
  });
}
