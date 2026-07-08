import type { Message } from '@nova/shared';

import { isGeminiConfigured, streamGeminiReply } from '@/lib/gemini';

// Falls back to a canned local simulation when no Gemini key is configured,
// so the chat UI still works end-to-end on a fresh clone of this repo.
const CANNED_REPLIES = [
  "Here's a quick answer based on what you shared. Once Nova is connected to a real model, this reply will come from Gemini instead of a canned script.",
  "Got it — I've noted that down. This is a simulated response so you can see the chat UI working end-to-end before live AI is wired in.",
  "That's a great question. I'm currently running in offline/mock mode, so I can't reason about it yet, but the streaming UI you're seeing is exactly how a real answer will arrive, token by token.",
];

function pickReply(prompt: string): string {
  const index = Math.abs(hashString(prompt)) % CANNED_REPLIES.length;
  return CANNED_REPLIES[index];
}

function hashString(input: string): number {
  let hash = 0;
  for (let i = 0; i < input.length; i++) {
    hash = (hash << 5) - hash + input.charCodeAt(i);
    hash |= 0;
  }
  return hash;
}

export interface StreamHandle {
  cancel: () => void;
}

function mockStreamReply(
  prompt: string,
  onToken: (chunk: string) => void,
  onDone: (fullText: string) => void
): StreamHandle {
  const fullText = pickReply(prompt);
  const words = fullText.split(' ');
  let index = 0;
  let cancelled = false;

  const tick = () => {
    if (cancelled) return;
    if (index >= words.length) {
      onDone(fullText);
      return;
    }
    const chunk = (index === 0 ? '' : ' ') + words[index];
    onToken(chunk);
    index += 1;
    setTimeout(tick, 40 + Math.random() * 60);
  };

  setTimeout(tick, 300);

  return {
    cancel: () => {
      cancelled = true;
    },
  };
}

// `history` should include the just-sent user message as its last entry -
// Gemini uses the full conversation for context, not just the latest turn.
export function streamAssistantReply(
  history: Message[],
  onToken: (chunk: string) => void,
  onDone: (fullText: string) => void
): StreamHandle {
  if (isGeminiConfigured()) {
    let cancelled = false;
    streamGeminiReply(
      history,
      (chunk) => {
        if (!cancelled) onToken(chunk);
      },
      (fullText) => {
        if (!cancelled) onDone(fullText);
      },
      (errorMessage) => {
        // Surfaced as the reply itself rather than thrown, so the chat
        // thread always ends in a readable state instead of a stuck
        // "thinking" bubble.
        if (!cancelled) onDone(`⚠️ Couldn't reach Gemini: ${errorMessage}`);
      }
    );
    return {
      cancel: () => {
        cancelled = true;
      },
    };
  }

  const lastUserMessage = [...history].reverse().find((m) => m.role === 'user');
  return mockStreamReply(lastUserMessage?.content ?? '', onToken, onDone);
}
