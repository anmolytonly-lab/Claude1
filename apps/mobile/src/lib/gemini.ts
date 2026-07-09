import type { Message } from '@nova/shared';

import { useApiSettingsStore } from '@/lib/api-settings-store';

// Direct client-side Gemini integration. There is no backend in this build
// (personal, single-device use - see master build prompt's personal-use
// scope principle), so whichever key ends up in effect ships inside the app
// bundle/traffic - acceptable for a private single-user app, but never
// hardcode a real key directly in this file.
//
// Key/model are changeable at runtime from Settings (see
// api-settings-store.ts) without editing .env or rebuilding: a key entered
// there takes priority over the build-time EXPO_PUBLIC_GEMINI_API_KEY, and
// falls back to it if the user hasn't set one.
// These two are pure functions of an explicit argument rather than reaching
// into the store themselves, specifically so UI code can call them with a
// value obtained from the *reactive* useApiSettingsStore(selector) hook.
// This project has the React Compiler enabled, which memoizes a component's
// rendered output based on the reactive inputs it can see - a function that
// quietly reads useApiSettingsStore.getState() from inside a render body is
// invisible to that analysis, so the compiler can (and did, before this was
// split out) cache stale JSX across renders where only the store had
// changed. The network functions below are plain async functions, not
// component renders, so they call the store directly - that's fine there.
export function resolveApiKey(customKey: string | null): string | undefined {
  return customKey ?? process.env.EXPO_PUBLIC_GEMINI_API_KEY;
}

export function resolveKeySource(customKey: string | null): 'custom' | 'env' | 'none' {
  if (customKey) return 'custom';
  if (process.env.EXPO_PUBLIC_GEMINI_API_KEY) return 'env';
  return 'none';
}

function getApiKey(): string | undefined {
  return resolveApiKey(useApiSettingsStore.getState().apiKey);
}

function getModel(): string {
  return useApiSettingsStore.getState().model;
}

const API_BASE = 'https://generativelanguage.googleapis.com/v1beta/models';

export function isGeminiConfigured(): boolean {
  return !!getApiKey();
}

interface GeminiContentPart {
  text: string;
}

interface GeminiContent {
  role: 'user' | 'model';
  parts: GeminiContentPart[];
}

function toGeminiHistory(history: Message[]): GeminiContent[] {
  return history
    .filter((m) => m.content.trim().length > 0)
    .map((m) => ({
      role: m.role === 'user' ? 'user' : 'model',
      parts: [{ text: m.content }],
    }));
}

function extractErrorMessage(bodyText: string, status: number): string {
  try {
    const parsed = JSON.parse(bodyText);
    if (parsed?.error?.message) return parsed.error.message;
  } catch {
    // leave the generic message
  }
  return `Request failed (HTTP ${status}).`;
}

// One-off, non-streaming call used by the Settings "Test connection" action -
// deliberately separate from streamGeminiReply so a connection test never
// depends on (or accidentally exercises) the streaming/parsing path.
export async function testGeminiConnection(): Promise<{ ok: boolean; message: string }> {
  const apiKey = getApiKey();
  if (!apiKey) return { ok: false, message: 'No API key set.' };

  const url = `${API_BASE}/${getModel()}:generateContent?key=${apiKey}`;
  try {
    const response = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        contents: [{ role: 'user', parts: [{ text: 'Reply with just the word OK.' }] }],
        generationConfig: { thinkingConfig: { thinkingBudget: 0 } },
      }),
    });
    const bodyText = await response.text();
    if (!response.ok) {
      return { ok: false, message: extractErrorMessage(bodyText, response.status) };
    }
    return { ok: true, message: 'Connected successfully.' };
  } catch (err) {
    return { ok: false, message: err instanceof Error ? err.message : 'Could not reach Gemini.' };
  }
}

export async function streamGeminiReply(
  history: Message[],
  onToken: (chunk: string) => void,
  onDone: (fullText: string) => void,
  onError: (message: string) => void
): Promise<void> {
  const apiKey = getApiKey();
  if (!apiKey) {
    onError('No Gemini API key configured.');
    return;
  }

  const url = `${API_BASE}/${getModel()}:streamGenerateContent?alt=sse&key=${apiKey}`;

  try {
    const response = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        contents: toGeminiHistory(history),
        generationConfig: {
          // Chat replies don't need step-by-step reasoning, and thinking
          // tokens are slow and count against quota even for short answers.
          thinkingConfig: { thinkingBudget: 0 },
        },
      }),
    });

    if (!response.ok || !response.body) {
      const bodyText = await response.text().catch(() => '');
      onError(extractErrorMessage(bodyText, response.status));
      return;
    }

    const reader = response.body.getReader();
    const decoder = new TextDecoder();
    let buffer = '';
    let fullText = '';

    while (true) {
      const { done, value } = await reader.read();
      if (done) break;
      buffer += decoder.decode(value, { stream: true });

      const lines = buffer.split('\n');
      buffer = lines.pop() ?? '';

      for (const line of lines) {
        const trimmed = line.trim();
        if (!trimmed.startsWith('data:')) continue;
        const jsonStr = trimmed.slice(5).trim();
        if (!jsonStr) continue;
        try {
          const parsed = JSON.parse(jsonStr);
          const parts = parsed?.candidates?.[0]?.content?.parts as GeminiContentPart[] | undefined;
          const text = parts?.map((p) => p.text ?? '').join('') ?? '';
          if (text) {
            fullText += text;
            onToken(text);
          }
        } catch {
          // A chunk boundary can split a JSON object across reads - the
          // remainder stays in `buffer` and completes on the next read.
        }
      }
    }

    onDone(fullText);
  } catch (err) {
    onError(err instanceof Error ? err.message : 'Could not reach Gemini.');
  }
}
