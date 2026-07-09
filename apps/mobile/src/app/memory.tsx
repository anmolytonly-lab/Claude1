import { Ionicons } from '@expo/vector-icons';
import { useQuery } from '@tanstack/react-query';
import dayjs from 'dayjs';
import { FlatList, StyleSheet } from 'react-native';
import Animated, { FadeInDown } from 'react-native-reanimated';

import { EmptyState } from '@/components/empty-state';
import { GlassCard } from '@/components/glass-card';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Radius, Spacing } from '@/constants/theme';
import { useTheme } from '@/hooks/use-theme';
import { listMemories } from '@/lib/mock-db';
import type { MemoryItem } from '@nova/shared';

export default function MemoryScreen() {
  const { data: memories = [], isLoading } = useQuery({ queryKey: ['memories'], queryFn: listMemories });

  return (
    <ThemedView style={styles.container}>
      <ThemedText type="small" themeColor="textSecondary" style={styles.intro}>
        Nova saves short summaries from your conversations here and pulls them back into context on
        future chats. Semantic (pgvector) search will replace this simple list once a real backend is
        connected.
      </ThemedText>
      {!isLoading && memories.length === 0 ? (
        <EmptyState icon="sparkles-outline" title="Nothing remembered yet" message="Chat with Nova and memories will start showing up here." />
      ) : (
        <FlatList
          data={memories}
          keyExtractor={(item) => item.id}
          contentContainerStyle={styles.list}
          renderItem={({ item, index }) => <MemoryRow memory={item} index={index} />}
        />
      )}
    </ThemedView>
  );
}

function MemoryRow({ memory, index }: { memory: MemoryItem; index: number }) {
  const theme = useTheme();
  return (
    <Animated.View entering={FadeInDown.delay(index * 40).duration(280)}>
      <GlassCard style={styles.row} radius={Radius.large}>
        <Ionicons name="sparkles" size={16} color={theme.primary} />
        <ThemedText type="small" style={{ flex: 1 }}>
          {memory.summary}
        </ThemedText>
        <ThemedText type="small" themeColor="textSecondary">
          {dayjs(memory.createdAt).format('MMM D')}
        </ThemedText>
      </GlassCard>
    </Animated.View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  intro: { padding: Spacing.four, paddingBottom: Spacing.two },
  list: { padding: Spacing.three, gap: Spacing.two },
  row: { flexDirection: 'row', alignItems: 'center', gap: Spacing.two, padding: Spacing.three },
});
