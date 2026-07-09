import { create } from 'zustand';

import { readJSON, writeJSON } from '@/lib/storage';

export interface ModelOption {
  id: string;
  label: string;
  hint: string;
}

// Curated to models actually verified against the live API with the
// thinkingBudget:0 override this app uses for fast chat replies - some
// model ids that look valid from Google's docs 404 or reject that config
// (see lib/gemini.ts), so this list is deliberately not "every Gemini model".
export const AVAILABLE_MODELS: ModelOption[] = [
  { id: 'gemini-2.5-flash', label: 'Gemini 2.5 Flash', hint: 'Recommended - stable, fast' },
  { id: 'gemini-2.5-flash-lite', label: 'Gemini 2.5 Flash Lite', hint: 'Cheapest, lower quality' },
  { id: 'gemini-3.5-flash', label: 'Gemini 3.5 Flash', hint: 'Newest - may be rate-limited' },
];

const DEFAULT_MODEL = AVAILABLE_MODELS[0].id;
const STORAGE_KEY = 'api-settings';

interface StoredSettings {
  apiKey: string | null;
  model: string;
}

interface ApiSettingsState {
  hydrated: boolean;
  apiKey: string | null;
  model: string;
  hydrate: () => Promise<void>;
  setApiKey: (key: string | null) => Promise<void>;
  setModel: (model: string) => Promise<void>;
}

async function persist(settings: StoredSettings) {
  await writeJSON(STORAGE_KEY, settings);
}

export const useApiSettingsStore = create<ApiSettingsState>((set, get) => ({
  hydrated: false,
  apiKey: null,
  model: DEFAULT_MODEL,

  hydrate: async () => {
    const stored = await readJSON<StoredSettings>(STORAGE_KEY);
    set({
      hydrated: true,
      apiKey: stored?.apiKey ?? null,
      model: stored?.model ?? DEFAULT_MODEL,
    });
  },

  setApiKey: async (key) => {
    const trimmed = key?.trim() || null;
    set({ apiKey: trimmed });
    await persist({ apiKey: trimmed, model: get().model });
  },

  setModel: async (model) => {
    set({ model });
    await persist({ apiKey: get().apiKey, model });
  },
}));
