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
import { useGradients } from '@/hooks/use-theme';
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
      <ScreenHeader title="Notes" subtitle={`${notes.length} note${notes.length === 1 ? '' : 's'}`} onAddPress={() => createMutation.mutate()} addLabel="New note" />
      {!isLoading && notes.length === 0 ? (
        <EmptyState icon="document-text-outline" title="No notes yet" message="Tap + to write your first note." />
      ) : (
        <FlatList
          data={notes}
          keyExtractor={(item) => item.id}
          contentContainerStyle={styles.list}
          renderItem={({ item, index }) => <NoteRow note={item} index={index} />}
        />
      )}
    </ThemedView>
  );
}

function NoteRow({ note, index }: { note: Note; index: number }) {
  const gradients = useGradients();
  const preview = note.content.replace(/[#*_`>-]/g, '').trim().slice(0, 80);
  return (
    <Animated.View entering={FadeInDown.delay(index * 40).duration(300)}>
      <AnimatedPressable onPress={() => router.push(`/notes/${note.id}`)} scaleTo={0.98}>
        <GlassCard style={styles.row} radius={Radius.large}>
          <View style={styles.rowInner}>
            <LinearGradient colors={gradients.accent} style={styles.accentBar} />
            <View style={styles.rowText}>
              <ThemedText type="smallBold" numberOfLines={1}>
                {note.title || 'Untitled note'}
              </ThemedText>
              {preview.length > 0 && (
                <ThemedText type="small" themeColor="textSecondary" numberOfLines={2}>
                  {preview}
                </ThemedText>
              )}
              <ThemedText type="small" themeColor="textSecondary">
                {dayjs(note.updatedAt).format('MMM D, h:mm A')}
              </ThemedText>
            </View>
          </View>
        </GlassCard>
      </AnimatedPressable>
    </Animated.View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  list: { padding: Spacing.three, gap: Spacing.three },
  row: { padding: Spacing.three },
  rowInner: { flexDirection: 'row', gap: Spacing.three },
  accentBar: { width: 4, borderRadius: Radius.pill, alignSelf: 'stretch' },
  rowText: { flex: 1, gap: 4 },
});
