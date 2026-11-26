import { useEffect, useRef, useState } from 'react';
import { useTheme } from '../hooks/useTheme';
import { CheckIcon, ChevronDownIcon, PaletteIcon } from './Icons';

export function ThemeSwitcher({ compact = false }) {
  const { theme, themeId, themes, setTheme } = useTheme();
  const [open, setOpen] = useState(false);
  const containerRef = useRef(null);

  useEffect(() => {
    if (!open) {
      return undefined;
    }

    function handlePointerDown(event) {
      if (containerRef.current && !containerRef.current.contains(event.target)) {
        setOpen(false);
      }
    }

    function handleKeyDown(event) {
      if (event.key === 'Escape') {
        setOpen(false);
      }
    }

    document.addEventListener('mousedown', handlePointerDown);
    window.addEventListener('keydown', handleKeyDown);
    return () => {
      document.removeEventListener('mousedown', handlePointerDown);
      window.removeEventListener('keydown', handleKeyDown);
    };
  }, [open]);

  return (
    <div className="theme-switcher" ref={containerRef}>
      <button
        type="button"
        className={`theme-switcher-trigger${compact ? ' theme-switcher-trigger-compact' : ''}`}
        aria-haspopup="listbox"
        aria-expanded={open}
        aria-label={`Change theme (current: ${theme.name})`}
        title={`Theme: ${theme.name}`}
        onClick={() => setOpen((prev) => !prev)}
      >
        <PaletteIcon />
        <span className="theme-switcher-current-dot" aria-hidden="true" />
        {!compact && <span className="theme-switcher-label">{theme.name}</span>}
        <ChevronDownIcon />
      </button>

      {open && (
        <div className="theme-switcher-menu" role="listbox" aria-label="Color themes">
          <div className="theme-switcher-menu-header">
            <span>Theme</span>
            <span>{themes.length} presets</span>
          </div>
          {themes.map((item) => {
            const isSelected = item.id === themeId;
            return (
              <button
                key={item.id}
                type="button"
                role="option"
                aria-selected={isSelected}
                className={`theme-option${isSelected ? ' theme-option-active' : ''}`}
                onClick={() => {
                  setTheme(item.id);
                  setOpen(false);
                }}
              >
                <span className="theme-option-left">
                  <span className="theme-swatches" aria-hidden="true">
                    {item.swatches.map((color, index) => (
                      <span
                        key={`${item.id}-${index}`}
                        className="theme-swatch-dot"
                        style={{ backgroundColor: color }}
                      />
                    ))}
                  </span>
                  <span className="theme-option-name">{item.name}</span>
                </span>
                {isSelected && (
                  <span className="theme-option-check" aria-hidden="true">
                    <CheckIcon />
                  </span>
                )}
              </button>
            );
          })}
        </div>
      )}
    </div>
  );
}
