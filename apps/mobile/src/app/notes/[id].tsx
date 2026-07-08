import { Ionicons } from '@expo/vector-icons';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { router, useLocalSearchParams } from 'expo-router';
import { useEffect, useRef, useState } from 'react';
import { Alert, Pressable, ScrollView, StyleSheet, TextInput, View } from 'react-native';
import Markdown from 'react-native-markdown-display';

import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Spacing } from '@/constants/theme';
import { useTheme } from '@/hooks/use-theme';
import { deleteNote, getNote, updateNote } from '@/lib/mock-db';

export default function NoteEditorScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const theme = useTheme();
  const queryClient = useQueryClient();

  const { data: note } = useQuery({ queryKey: ['note', id], queryFn: () => getNote(id), enabled: !!id });

  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [mode, setMode] = useState<'edit' | 'preview'>('edit');
  const hydrated = useRef(false);

  useEffect(() => {
    if (note && !hydrated.current) {
      setTitle(note.title);
      setContent(note.content);
      hydrated.current = true;
    }
  }, [note]);

  const save = useMutation({
    mutationFn: (patch: { title: string; content: string }) => updateNote(id, patch),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notes'] });
      queryClient.invalidateQueries({ queryKey: ['note', id] });
    },
  });

  // Debounced autosave - mirrors "notes app with live preview" from the
  // build spec without needing an explicit Save button.
  useEffect(() => {
    if (!hydrated.current) return;
    const handle = setTimeout(() => save.mutate({ title, content }), 500);
    return () => clearTimeout(handle);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [title, content]);

  const remove = useMutation({
    mutationFn: () => deleteNote(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notes'] });
      router.back();
    },
  });

  const confirmDelete = () => {
    Alert.alert('Delete note', 'This cannot be undone.', [
      { text: 'Cancel', style: 'cancel' },
      { text: 'Delete', style: 'destructive', onPress: () => remove.mutate() },
    ]);
  };

  return (
    <ThemedView style={styles.container}>
      <View style={[styles.toolbar, { borderColor: theme.border }]}>
        <View style={styles.modeSwitch}>
          <Pressable onPress={() => setMode('edit')} style={styles.modeButton}>
            <ThemedText themeColor={mode === 'edit' ? 'primary' : 'textSecondary'} type="smallBold">
              Edit
            </ThemedText>
          </Pressable>
          <Pressable onPress={() => setMode('preview')} style={styles.modeButton}>
            <ThemedText themeColor={mode === 'preview' ? 'primary' : 'textSecondary'} type="smallBold">
              Preview
            </ThemedText>
          </Pressable>
        </View>
        <Pressable onPress={confirmDelete} hitSlop={8}>
          <Ionicons name="trash-outline" size={20} color={theme.textSecondary} />
        </Pressable>
      </View>

      {mode === 'edit' ? (
        <ScrollView contentContainerStyle={styles.editArea} keyboardShouldPersistTaps="handled">
          <TextInput
            value={title}
            onChangeText={setTitle}
            placeholder="Title"
            placeholderTextColor={theme.textSecondary}
            style={[styles.titleInput, { color: theme.text }]}
          />
          <TextInput
            value={content}
            onChangeText={setContent}
            placeholder="Write in markdown..."
            placeholderTextColor={theme.textSecondary}
            style={[styles.contentInput, { color: theme.text }]}
            multiline
            textAlignVertical="top"
          />
        </ScrollView>
      ) : (
        <ScrollView contentContainerStyle={styles.previewArea}>
          <ThemedText type="title" style={styles.previewTitle}>
            {title || 'Untitled note'}
          </ThemedText>
          <Markdown style={{ body: { color: theme.text, fontSize: 16 } }}>{content || '_Nothing to preview yet._'}</Markdown>
        </ScrollView>
      )}
    </ThemedView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  toolbar: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: Spacing.four,
    paddingVertical: Spacing.two,
    borderBottomWidth: StyleSheet.hairlineWidth,
  },
  modeSwitch: { flexDirection: 'row', gap: Spacing.four },
  modeButton: { paddingVertical: Spacing.one },
  editArea: { padding: Spacing.four, gap: Spacing.three },
  titleInput: { fontSize: 22, fontWeight: '700' },
  contentInput: { fontSize: 16, minHeight: 300 },
  previewArea: { padding: Spacing.four },
  previewTitle: { fontSize: 26, marginBottom: Spacing.three },
});
