import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import dayjs from 'dayjs';
import { router } from 'expo-router';
import { FlatList, Pressable, StyleSheet } from 'react-native';

import { EmptyState } from '@/components/empty-state';
import { ScreenHeader } from '@/components/screen-header';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Spacing } from '@/constants/theme';
import { useTheme } from '@/hooks/use-theme';
import { createNote, listNotes } from '@/lib/mock-db';
import type { Note } from '@nova/shared';

export default function NotesScreen() {
  const queryClient = useQueryClient();
  const { data: notes = [], isLoading } = useQuery({ queryKey: ['notes'], queryFn: listNotes });

  const createMutation = useMutation({
    mutationFn: () => createNote('Untitled note'),
    onSuccess: (note) => {
      queryClient.invalidateQueries({ queryKey: ['notes'] });
      router.push(`/notes/${note.id}`);
    },
  });

  return (
    <ThemedView style={styles.container}>
      <ScreenHeader title="Notes" onAddPress={() => createMutation.mutate()} addLabel="New note" />
      {!isLoading && notes.length === 0 ? (
        <EmptyState icon="document-text-outline" title="No notes yet" message="Tap + to write your first note." />
      ) : (
        <FlatList
          data={notes}
          keyExtractor={(item) => item.id}
          contentContainerStyle={styles.list}
          renderItem={({ item }) => <NoteRow note={item} />}
        />
      )}
    </ThemedView>
  );
}

function NoteRow({ note }: { note: Note }) {
  const theme = useTheme();
  const preview = note.content.replace(/[#*_`>-]/g, '').trim().slice(0, 80);
  return (
    <Pressable
      onPress={() => router.push(`/notes/${note.id}`)}
      style={({ pressed }) => [
        styles.row,
        { borderColor: theme.border, backgroundColor: pressed ? theme.backgroundSelected : theme.backgroundElement },
      ]}>
      <ThemedText type="smallBold" numberOfLines={1}>
        {note.title || 'Untitled note'}
      </ThemedText>
      {preview.length > 0 && (
        <ThemedText type="small" themeColor="textSecondary" numberOfLines={1}>
          {preview}
        </ThemedText>
      )}
      <ThemedText type="small" themeColor="textSecondary">
        {dayjs(note.updatedAt).format('MMM D, h:mm A')}
      </ThemedText>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  list: { padding: Spacing.three, gap: Spacing.two },
  row: {
    borderRadius: Spacing.three,
    borderWidth: StyleSheet.hairlineWidth,
    padding: Spacing.three,
    gap: 4,
  },
});
