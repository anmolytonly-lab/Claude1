import type { Message } from '@nova/shared';

// Direct client-side Gemini integration. There is no backend in this build
// (personal, single-device use - see master build prompt's personal-use
// scope principle), so the key ships inside the app bundle via Expo's
// EXPO_PUBLIC_ env convention. That means it is visible to anyone who
// inspects this device's app/network traffic - acceptable for a private
// single-user app, but this key must never be reused for a public/shared
// build and this file must never hardcode a key directly.
const GEMINI_API_KEY = process.env.EXPO_PUBLIC_GEMINI_API_KEY;

// Single spot for the model id - Gemini model ids/previews get deprecated
// and replaced every few months. gemini-3.5-flash is the current default
// tier as of mid-2026 but was returning transient 503s (demand) at the time
// this was wired up, so gemini-2.5-flash (GA, stable) is the default; bump
// this string once 3.5 settles down.
const GEMINI_MODEL = 'gemini-2.5-flash';

const API_BASE = 'https://generativelanguage.googleapis.com/v1beta/models';

export function isGeminiConfigured(): boolean {
  return !!GEMINI_API_KEY;
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

export async function streamGeminiReply(
  history: Message[],
  onToken: (chunk: string) => void,
  onDone: (fullText: string) => void,
  onError: (message: string) => void
): Promise<void> {
  if (!GEMINI_API_KEY) {
    onError('No Gemini API key configured.');
    return;
  }

  const url = `${API_BASE}/${GEMINI_MODEL}:streamGenerateContent?alt=sse&key=${GEMINI_API_KEY}`;

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
      let message = `Gemini request failed (HTTP ${response.status}).`;
      try {
        const parsed = JSON.parse(bodyText);
        if (parsed?.error?.message) message = parsed.error.message;
      } catch {
        // leave the generic message
      }
      onError(message);
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
