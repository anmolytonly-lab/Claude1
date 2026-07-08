import { useQuery } from '@tanstack/react-query';
import dayjs from 'dayjs';
import { FlatList, StyleSheet, View } from 'react-native';

import { EmptyState } from '@/components/empty-state';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Spacing } from '@/constants/theme';
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
          renderItem={({ item }) => <MemoryRow memory={item} />}
        />
      )}
    </ThemedView>
  );
}

function MemoryRow({ memory }: { memory: MemoryItem }) {
  return (
    <View style={styles.row}>
      <ThemedText type="small">{memory.summary}</ThemedText>
      <ThemedText type="small" themeColor="textSecondary">
        {dayjs(memory.createdAt).format('MMM D, YYYY')}
      </ThemedText>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  intro: { padding: Spacing.four, paddingBottom: Spacing.two },
  list: { padding: Spacing.three, gap: Spacing.two },
  row: { gap: 4, paddingHorizontal: Spacing.two, paddingVertical: Spacing.two },
});
