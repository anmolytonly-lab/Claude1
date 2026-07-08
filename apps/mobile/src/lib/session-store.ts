import type { Profile, ThemePreference } from '@nova/shared';
import { create } from 'zustand';

import { createAccount, findAccountByEmail, hydrateDb, updateProfile } from '@/lib/mock-db';
import { readJSON, removeKey, writeJSON } from '@/lib/storage';

const SESSION_KEY = 'session';

interface SessionState {
  status: 'loading' | 'signed-out' | 'signed-in';
  profile: Profile | null;
  error: string | null;
  hydrate: () => Promise<void>;
  signIn: (email: string, password: string) => Promise<void>;
  signUp: (email: string, password: string, displayName: string) => Promise<void>;
  signOut: () => Promise<void>;
  setTheme: (theme: ThemePreference) => Promise<void>;
  updateDisplayName: (displayName: string) => Promise<void>;
}

export const useSessionStore = create<SessionState>((set, get) => ({
  status: 'loading',
  profile: null,
  error: null,

  hydrate: async () => {
    const db = await hydrateDb();
    const savedUserId = await readJSON<string>(SESSION_KEY);
    if (!savedUserId) {
      set({ status: 'signed-out' });
      return;
    }
    const account = db.accounts.find((a) => a.profile.id === savedUserId);
    if (!account) {
      await removeKey(SESSION_KEY);
      set({ status: 'signed-out' });
      return;
    }
    set({ status: 'signed-in', profile: account.profile });
  },

  signIn: async (email, password) => {
    set({ error: null });
    const existing = await findAccountByEmail(email);
    if (existing) {
      if (existing.password !== password) {
        set({ error: 'Invalid email or password.' });
        throw new Error('Invalid email or password.');
      }
      await writeJSON(SESSION_KEY, existing.profile.id);
      set({ status: 'signed-in', profile: existing.profile });
      return;
    }
    // No live backend yet (mock/local mode) - first sign-in for an email
    // provisions a mock account rather than rejecting it outright.
    const displayName = email.split('@')[0] || 'Nova user';
    const profile = await createAccount(email, password, displayName);
    await writeJSON(SESSION_KEY, profile.id);
    set({ status: 'signed-in', profile });
  },

  signUp: async (email, password, displayName) => {
    set({ error: null });
    const existing = await findAccountByEmail(email);
    if (existing) {
      set({ error: 'An account with that email already exists.' });
      throw new Error('An account with that email already exists.');
    }
    const profile = await createAccount(email, password, displayName);
    await writeJSON(SESSION_KEY, profile.id);
    set({ status: 'signed-in', profile });
  },

  signOut: async () => {
    await removeKey(SESSION_KEY);
    set({ status: 'signed-out', profile: null });
  },

  setTheme: async (theme) => {
    const profile = get().profile;
    if (!profile) return;
    const updated = await updateProfile(profile.id, { themePreference: theme });
    set({ profile: updated });
  },

  updateDisplayName: async (displayName) => {
    const profile = get().profile;
    if (!profile) return;
    const updated = await updateProfile(profile.id, { displayName });
    set({ profile: updated });
  },
}));
