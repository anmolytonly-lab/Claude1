// Simulated AI reply generator. Stands in for the real Gemini-backed SSE
// stream from apps/server (see master build prompt Phase 1/2). Swap
// `streamAssistantReply` for a real fetch-based SSE reader once a live
// backend + API key are wired up — callers only depend on the token
// callback shape below, not on how the tokens are produced.

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

export function streamAssistantReply(
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
