import { Ionicons } from '@expo/vector-icons';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import dayjs from 'dayjs';
import { router } from 'expo-router';
import { FlatList, Pressable, StyleSheet, View } from 'react-native';

import { EmptyState } from '@/components/empty-state';
import { ScreenHeader } from '@/components/screen-header';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Spacing } from '@/constants/theme';
import { useTheme } from '@/hooks/use-theme';
import { createConversation, listConversations } from '@/lib/mock-db';
import type { Conversation } from '@nova/shared';

export default function ChatListScreen() {
  const theme = useTheme();
  const queryClient = useQueryClient();

  const { data: conversations = [], isLoading } = useQuery({
    queryKey: ['conversations'],
    queryFn: listConversations,
  });

  const createMutation = useMutation({
    mutationFn: () => createConversation('New chat'),
    onSuccess: (conversation) => {
      queryClient.invalidateQueries({ queryKey: ['conversations'] });
      router.push(`/chat/${conversation.id}`);
    },
  });

  return (
    <ThemedView style={styles.container}>
      <ScreenHeader title="Chat" onAddPress={() => createMutation.mutate()} addLabel="New chat" />
      {!isLoading && conversations.length === 0 ? (
        <EmptyState
          icon="chatbubble-ellipses-outline"
          title="No conversations yet"
          message="Tap the + button to start chatting with Nova."
        />
      ) : (
        <FlatList
          data={conversations}
          keyExtractor={(item) => item.id}
          contentContainerStyle={styles.list}
          renderItem={({ item }) => <ConversationRow conversation={item} />}
        />
      )}
    </ThemedView>
  );
}

function ConversationRow({ conversation }: { conversation: Conversation }) {
  const theme = useTheme();
  return (
    <Pressable
      onPress={() => router.push(`/chat/${conversation.id}`)}
      style={({ pressed }) => [
        styles.row,
        { borderColor: theme.border, backgroundColor: pressed ? theme.backgroundSelected : theme.backgroundElement },
      ]}>
      <View style={styles.rowText}>
        <View style={styles.rowTitleLine}>
          {conversation.pinned && <Ionicons name="pin" size={14} color={theme.primary} />}
          <ThemedText type="smallBold" numberOfLines={1} style={styles.rowTitle}>
            {conversation.title}
          </ThemedText>
        </View>
        {conversation.lastMessagePreview && (
          <ThemedText type="small" themeColor="textSecondary" numberOfLines={1}>
            {conversation.lastMessagePreview}
          </ThemedText>
        )}
      </View>
      <ThemedText type="small" themeColor="textSecondary">
        {dayjs(conversation.updatedAt).format('MMM D')}
      </ThemedText>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  list: { padding: Spacing.three, gap: Spacing.two },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    borderRadius: Spacing.three,
    borderWidth: StyleSheet.hairlineWidth,
    paddingVertical: Spacing.three,
    paddingHorizontal: Spacing.three,
    gap: Spacing.two,
  },
  rowText: { flex: 1, gap: 2 },
  rowTitleLine: { flexDirection: 'row', alignItems: 'center', gap: 4 },
  rowTitle: { flexShrink: 1 },
});
