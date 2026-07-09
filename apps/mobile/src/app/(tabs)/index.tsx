import { Ionicons } from '@expo/vector-icons';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import dayjs from 'dayjs';
import { LinearGradient } from 'expo-linear-gradient';
import { router } from 'expo-router';
import { FlatList, StyleSheet, View } from 'react-native';
import Animated, { FadeInDown } from 'react-native-reanimated';

import { AnimatedPressable } from '@/components/animated-pressable';
import { EmptyState } from '@/components/empty-state';
import { GlassCard } from '@/components/glass-card';
import { ScreenHeader } from '@/components/screen-header';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Radius, Spacing } from '@/constants/theme';
import { useGradients, useTheme } from '@/hooks/use-theme';
import { createConversation, listConversations } from '@/lib/mock-db';
import type { Conversation } from '@nova/shared';

export default function ChatListScreen() {
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
      <ScreenHeader title="Chat" subtitle="Your conversations with Nova" onAddPress={() => createMutation.mutate()} addLabel="New chat" />
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
          renderItem={({ item, index }) => <ConversationRow conversation={item} index={index} />}
        />
      )}
    </ThemedView>
  );
}

function ConversationRow({ conversation, index }: { conversation: Conversation; index: number }) {
  const theme = useTheme();
  const gradients = useGradients();
  return (
    <Animated.View entering={FadeInDown.delay(index * 40).duration(300)}>
      <AnimatedPressable onPress={() => router.push(`/chat/${conversation.id}`)} scaleTo={0.98}>
        <GlassCard style={styles.row} radius={Radius.large}>
          <LinearGradient colors={gradients.primary} style={styles.avatar}>
            <Ionicons name="sparkles" size={18} color={theme.primaryText} />
          </LinearGradient>
          <View style={styles.rowText}>
            <View style={styles.rowTitleLine}>
              {conversation.pinned && <Ionicons name="pin" size={13} color={theme.primary} />}
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
        </GlassCard>
      </AnimatedPressable>
    </Animated.View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  list: { padding: Spacing.three, gap: Spacing.three },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingVertical: Spacing.three,
    paddingHorizontal: Spacing.three,
    gap: Spacing.three,
  },
  avatar: {
    width: 40,
    height: 40,
    borderRadius: Radius.pill,
    alignItems: 'center',
    justifyContent: 'center',
  },
  rowText: { flex: 1, gap: 2 },
  rowTitleLine: { flexDirection: 'row', alignItems: 'center', gap: 4 },
  rowTitle: { flexShrink: 1 },
});
