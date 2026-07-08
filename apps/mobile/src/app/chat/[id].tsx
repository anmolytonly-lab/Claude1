import { Ionicons } from '@expo/vector-icons';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { useLocalSearchParams } from 'expo-router';
import { useEffect, useRef, useState } from 'react';
import {
  KeyboardAvoidingView,
  Platform,
  Pressable,
  ScrollView,
  StyleSheet,
  TextInput,
  View,
} from 'react-native';
import Markdown from 'react-native-markdown-display';

import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Spacing } from '@/constants/theme';
import { useTheme } from '@/hooks/use-theme';
import { streamAssistantReply } from '@/lib/chat-engine';
import { generateId } from '@/lib/id';
import { appendMessage, listMessages } from '@/lib/mock-db';
import type { Message } from '@nova/shared';

export default function ChatThreadScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const theme = useTheme();
  const queryClient = useQueryClient();
  const scrollRef = useRef<ScrollView>(null);

  const { data: initialMessages } = useQuery({
    queryKey: ['messages', id],
    queryFn: () => listMessages(id),
    enabled: !!id,
    // This screen takes over as the source of truth for `messages` after the
    // first load (see the effect below) - a background refetch (e.g. on
    // window focus) must not clobber in-progress local/streaming state.
    refetchOnWindowFocus: false,
    refetchOnMount: false,
  });

  const [messages, setMessages] = useState<Message[]>([]);
  const [draft, setDraft] = useState('');
  const [streaming, setStreaming] = useState(false);
  const sendingRef = useRef(false);
  const hydrated = useRef(false);

  useEffect(() => {
    // Seed local state from the query exactly once - this screen owns
    // `messages` after that (see onSend below), same pattern as the notes
    // editor. Re-running this on every query resolution would clobber
    // in-progress optimistic/streaming state if a refetch ever landed late.
    if (initialMessages && !hydrated.current) {
      setMessages(initialMessages);
      hydrated.current = true;
    }
  }, [initialMessages]);

  const scrollToEnd = () => {
    requestAnimationFrame(() => scrollRef.current?.scrollToEnd({ animated: true }));
  };

  const onSend = async () => {
    const content = draft.trim();
    // Guard synchronously with a ref, not the `streaming` state - state
    // updates aren't visible until re-render, so a rapid double-tap could
    // otherwise slip past a state-only check and start two concurrent sends.
    if (!content || sendingRef.current) return;
    sendingRef.current = true;
    setDraft('');

    const userMessage = await appendMessage(id, { role: 'user', content });
    const historyForReply = [...messages, userMessage];
    setMessages((prev) => [...prev, userMessage]);
    scrollToEnd();

    setStreaming(true);
    const pendingId = generateId('pending');
    const pendingMessage: Message = {
      id: pendingId,
      conversationId: id,
      role: 'assistant',
      content: '',
      createdAt: new Date().toISOString(),
      pending: true,
    };
    setMessages((prev) => [...prev, pendingMessage]);

    streamAssistantReply(
      historyForReply,
      (chunk) => {
        setMessages((prev) =>
          prev.map((m) => (m.id === pendingId ? { ...m, content: m.content + chunk } : m))
        );
        scrollToEnd();
      },
      async (fullText) => {
        const saved = await appendMessage(id, { role: 'assistant', content: fullText });
        setMessages((prev) => prev.map((m) => (m.id === pendingId ? saved : m)));
        setStreaming(false);
        sendingRef.current = false;
        queryClient.invalidateQueries({ queryKey: ['conversations'] });
        scrollToEnd();
      }
    );
  };

  return (
    <KeyboardAvoidingView
      style={{ flex: 1 }}
      behavior={Platform.select({ ios: 'padding', default: undefined })}
      keyboardVerticalOffset={90}>
      <ThemedView style={styles.container}>
        <ScrollView
          ref={scrollRef}
          contentContainerStyle={styles.list}
          onContentSizeChange={scrollToEnd}>
          {messages.map((message) => (
            <MessageBubble key={message.id} message={message} />
          ))}
        </ScrollView>
        <View style={[styles.inputBar, { borderColor: theme.border, backgroundColor: theme.backgroundElement }]}>
          <TextInput
            value={draft}
            onChangeText={setDraft}
            placeholder="Message Nova..."
            placeholderTextColor={theme.textSecondary}
            style={[styles.input, { color: theme.text }]}
            multiline
          />
          <Pressable
            onPress={onSend}
            disabled={!draft.trim() || streaming}
            style={[
              styles.sendButton,
              { backgroundColor: theme.primary, opacity: !draft.trim() || streaming ? 0.5 : 1 },
            ]}>
            <Ionicons name="arrow-up" size={18} color={theme.primaryText} />
          </Pressable>
        </View>
      </ThemedView>
    </KeyboardAvoidingView>
  );
}

function MessageBubble({ message }: { message: Message }) {
  const theme = useTheme();
  const isUser = message.role === 'user';

  return (
    <View style={[styles.bubbleRow, { justifyContent: isUser ? 'flex-end' : 'flex-start' }]}>
      <View
        style={[
          styles.bubble,
          {
            backgroundColor: isUser ? theme.primary : theme.backgroundElement,
            borderTopRightRadius: isUser ? 4 : Spacing.three,
            borderTopLeftRadius: isUser ? Spacing.three : 4,
          },
        ]}>
        {isUser ? (
          <ThemedText themeColor="primaryText">{message.content}</ThemedText>
        ) : message.content ? (
          <Markdown
            style={{
              body: { color: theme.text, fontSize: 16 },
              code_inline: { backgroundColor: theme.backgroundSelected, color: theme.text },
              code_block: { backgroundColor: theme.backgroundSelected },
              fence: { backgroundColor: theme.backgroundSelected },
            }}>
            {message.content}
          </Markdown>
        ) : (
          <ThemedText themeColor="textSecondary">Nova is thinking…</ThemedText>
        )}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  list: { padding: Spacing.three, gap: Spacing.two },
  bubbleRow: { flexDirection: 'row' },
  bubble: {
    maxWidth: '82%',
    borderRadius: Spacing.three,
    paddingHorizontal: Spacing.three,
    paddingVertical: Spacing.two,
  },
  inputBar: {
    flexDirection: 'row',
    alignItems: 'flex-end',
    gap: Spacing.two,
    padding: Spacing.two,
    borderTopWidth: StyleSheet.hairlineWidth,
  },
  input: {
    flex: 1,
    fontSize: 16,
    maxHeight: 120,
    paddingHorizontal: Spacing.two,
    paddingVertical: Spacing.two,
  },
  sendButton: {
    width: 36,
    height: 36,
    borderRadius: 18,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
