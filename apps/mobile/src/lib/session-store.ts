import type { Profile, ThemePreference } from '@nova/shared';
import { create } from 'zustand';

import { getProfile, updateProfile } from '@/lib/mock-db';

interface SessionState {
  status: 'loading' | 'ready';
  profile: Profile | null;
  hydrate: () => Promise<void>;
  setTheme: (theme: ThemePreference) => Promise<void>;
  updateDisplayName: (displayName: string) => Promise<void>;
}

// Personal-use build: no login system - there's exactly one local profile,
// auto-created on first launch (see mock-db.ts). This store just loads it.
export const useSessionStore = create<SessionState>((set, get) => ({
  status: 'loading',
  profile: null,

  hydrate: async () => {
    const profile = await getProfile();
    set({ status: 'ready', profile });
  },

  setTheme: async (theme) => {
    if (!get().profile) return;
    const updated = await updateProfile({ themePreference: theme });
    set({ profile: updated });
  },

  updateDisplayName: async (displayName) => {
    if (!get().profile) return;
    const updated = await updateProfile({ displayName });
    set({ profile: updated });
  },
}));
