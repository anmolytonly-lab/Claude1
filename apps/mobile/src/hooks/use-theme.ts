/**
 * Learn more about light and dark modes:
 * https://docs.expo.dev/guides/color-schemes/
 */

import { Colors } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';
import { useSessionStore } from '@/lib/session-store';

export function useActiveScheme(): 'light' | 'dark' {
  const systemScheme = useColorScheme();
  const preference = useSessionStore((s) => s.profile?.themePreference ?? 'system');
  if (preference === 'system') {
    return systemScheme === 'unspecified' ? 'light' : (systemScheme ?? 'light');
  }
  return preference;
}

export function useTheme() {
  const scheme = useActiveScheme();
  return Colors[scheme];
}
