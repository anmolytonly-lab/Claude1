/**
 * Below are the colors that are used in the app. The colors are defined in the light and dark mode.
 * There are many other ways to style your app. For example, [Nativewind](https://www.nativewind.dev/), [Tamagui](https://tamagui.dev/), [unistyles](https://reactnativeunistyles.vercel.app), etc.
 */

import '@/global.css';

import { Platform } from 'react-native';

export const Colors = {
  light: {
    text: '#0F1115',
    textSecondary: '#6B7280',
    background: '#F3F4FA',
    backgroundElement: '#FFFFFF',
    backgroundSelected: '#ECE9FE',
    card: '#FFFFFF',
    glassSurface: 'rgba(255,255,255,0.65)',
    glassBorder: 'rgba(15,17,21,0.08)',
    border: 'rgba(15,17,21,0.08)',
    primary: '#6D5AE0',
    primaryAlt: '#9B6BF2',
    primaryText: '#FFFFFF',
    accent: '#FF7A59',
    danger: '#E5484D',
    success: '#22A06B',
    warning: '#F5A623',
    shadowColor: '#39397A',
  },
  dark: {
    text: '#F5F6FA',
    textSecondary: '#9AA0AC',
    background: '#0B0C10',
    backgroundElement: '#17181F',
    backgroundSelected: 'rgba(139,124,255,0.22)',
    card: '#17181F',
    glassSurface: 'rgba(255,255,255,0.06)',
    glassBorder: 'rgba(255,255,255,0.10)',
    border: 'rgba(255,255,255,0.09)',
    primary: '#8B7CFF',
    primaryAlt: '#C78CFF',
    primaryText: '#0B0C10',
    accent: '#FF9270',
    danger: '#FF6369',
    success: '#4FD394',
    warning: '#FFC24B',
    shadowColor: '#000000',
  },
} as const;

export type ThemeColor = keyof typeof Colors.light & keyof typeof Colors.dark;

export const Gradients = {
  light: {
    primary: ['#6D5AE0', '#9B6BF2'] as const,
    accent: ['#FF7A59', '#FFA45B'] as const,
    success: ['#1FA971', '#4FD394'] as const,
    hero: ['#EDE9FE', '#F3F4FA'] as const,
  },
  dark: {
    primary: ['#8B7CFF', '#C78CFF'] as const,
    accent: ['#FF9270', '#FFC24B'] as const,
    success: ['#22A06B', '#4FD394'] as const,
    hero: ['#1B1530', '#0B0C10'] as const,
  },
} as const;

export const Fonts = Platform.select({
  ios: {
    /** iOS `UIFontDescriptorSystemDesignDefault` */
    sans: 'system-ui',
    /** iOS `UIFontDescriptorSystemDesignSerif` */
    serif: 'ui-serif',
    /** iOS `UIFontDescriptorSystemDesignRounded` */
    rounded: 'ui-rounded',
    /** iOS `UIFontDescriptorSystemDesignMonospaced` */
    mono: 'ui-monospace',
  },
  default: {
    sans: 'normal',
    serif: 'serif',
    rounded: 'normal',
    mono: 'monospace',
  },
  web: {
    sans: 'var(--font-display)',
    serif: 'var(--font-serif)',
    rounded: 'var(--font-rounded)',
    mono: 'var(--font-mono)',
  },
});

export const Spacing = {
  half: 2,
  one: 4,
  two: 8,
  three: 16,
  four: 24,
  five: 32,
  six: 64,
} as const;

export const Radius = {
  small: 10,
  medium: 16,
  large: 22,
  pill: 999,
} as const;

export function cardShadow(shadowColor: string, opacity = 0.12) {
  return Platform.select({
    web: {
      boxShadow: `0 8px 24px -6px ${hexToRgba(shadowColor, opacity)}`,
    },
    default: {
      shadowColor,
      shadowOffset: { width: 0, height: 8 },
      shadowOpacity: opacity,
      shadowRadius: 16,
      elevation: 6,
    },
  });
}

function hexToRgba(hex: string, alpha: number): string {
  const clean = hex.replace('#', '');
  const bigint = parseInt(clean, 16);
  const r = (bigint >> 16) & 255;
  const g = (bigint >> 8) & 255;
  const b = bigint & 255;
  return `rgba(${r}, ${g}, ${b}, ${alpha})`;
}

export const BottomTabInset = Platform.select({ ios: 50, android: 80 }) ?? 0;
export const MaxContentWidth = 800;
