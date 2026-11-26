export const THEME_STORAGE_KEY = 'mafioso_theme';
export const DEFAULT_THEME_ID = 'classic';

export const THEMES = [
  {
    id: 'classic',
    name: 'Classic Ivory',
    mode: 'light',
    swatches: ['#f7f4ee', '#1b4d3e', '#b88a38'],
  },
  {
    id: 'one-dark',
    name: 'One Dark',
    mode: 'dark',
    swatches: ['#282c34', '#61afef', '#e5c07b'],
  },
  {
    id: 'catppuccin',
    name: 'Catppuccin',
    mode: 'dark',
    swatches: ['#1e1e2e', '#cba6f7', '#f9e2af'],
  },
  {
    id: 'rose-pine',
    name: 'Rosé Pine',
    mode: 'dark',
    swatches: ['#191724', '#ebbcba', '#f6c177'],
  },
  {
    id: 'solarized',
    name: 'Solarized',
    mode: 'dark',
    swatches: ['#002b36', '#2aa198', '#b58900'],
  },
  {
    id: 'dracula',
    name: 'Dracula',
    mode: 'dark',
    swatches: ['#282a36', '#bd93f9', '#ffb86c'],
  },
  {
    id: 'tokyo-night',
    name: 'Tokyo Night',
    mode: 'dark',
    swatches: ['#1a1b26', '#7aa2f7', '#e0af68'],
  },
  {
    id: 'strawberry-daiquiri',
    name: 'Strawberry Daiquiri',
    mode: 'dark',
    swatches: ['#1c0f15', '#fb6f92', '#ffd166'],
  },
  {
    id: 'synthwave-84',
    name: "Synthwave '84",
    mode: 'dark',
    swatches: ['#262335', '#ff7edb', '#fede5d'],
  },
  {
    id: 'emerald-forest',
    name: 'Emerald Forest',
    mode: 'dark',
    swatches: ['#0b1914', '#10b981', '#d4a853'],
  },
];

export const VALID_THEME_IDS = new Set(THEMES.map((theme) => theme.id));

export function getThemeById(themeId) {
  return THEMES.find((theme) => theme.id === themeId) || THEMES[0];
}
