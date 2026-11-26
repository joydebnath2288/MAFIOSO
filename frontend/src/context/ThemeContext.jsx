import { createContext, useCallback, useEffect, useMemo, useState } from 'react';
import {
  DEFAULT_THEME_ID,
  getThemeById,
  THEMES,
  THEME_STORAGE_KEY,
  VALID_THEME_IDS,
} from '../config/themes';

export const ThemeContext = createContext(null);

function resolveInitialThemeId() {
  if (typeof document !== 'undefined') {
    const attrTheme = document.documentElement.dataset.theme;
    if (attrTheme && VALID_THEME_IDS.has(attrTheme)) {
      return attrTheme;
    }
  }
  if (typeof window !== 'undefined') {
    try {
      const saved = window.localStorage.getItem(THEME_STORAGE_KEY);
      if (saved && VALID_THEME_IDS.has(saved)) {
        return saved;
      }
    } catch {
      // Ignore storage errors in restricted browsing contexts
    }
  }
  return DEFAULT_THEME_ID;
}

export function ThemeProvider({ children }) {
  const [themeId, setThemeId] = useState(resolveInitialThemeId);

  const theme = useMemo(() => getThemeById(themeId), [themeId]);

  useEffect(() => {
    const root = document.documentElement;
    root.dataset.theme = theme.id;
    root.style.colorScheme = theme.mode;

    try {
      window.localStorage.setItem(THEME_STORAGE_KEY, theme.id);
    } catch {
      // Ignore storage errors in restricted browsing contexts
    }
  }, [theme]);

  const setTheme = useCallback((nextThemeId) => {
    if (VALID_THEME_IDS.has(nextThemeId)) {
      setThemeId(nextThemeId);
    }
  }, []);

  const value = useMemo(
    () => ({
      theme,
      themeId: theme.id,
      themes: THEMES,
      setTheme,
    }),
    [theme, setTheme]
  );

  return <ThemeContext.Provider value={value}>{children}</ThemeContext.Provider>;
}
