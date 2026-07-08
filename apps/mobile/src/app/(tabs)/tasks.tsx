import { Ionicons } from '@expo/vector-icons';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import dayjs from 'dayjs';
import { useState } from 'react';
import { Modal, Pressable, SectionList, StyleSheet, TextInput, View } from 'react-native';

import { EmptyState } from '@/components/empty-state';
import { ScreenHeader } from '@/components/screen-header';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Spacing } from '@/constants/theme';
import { useTheme } from '@/hooks/use-theme';
import { createTask, deleteTask, listTasks, setTaskStatus } from '@/lib/mock-db';
import type { Task, TaskPriority, TaskStatus } from '@nova/shared';

const STATUS_LABEL: Record<TaskStatus, string> = {
  todo: 'To do',
  in_progress: 'In progress',
  done: 'Done',
};

const STATUS_ORDER: TaskStatus[] = ['todo', 'in_progress', 'done'];

const PRIORITY_COLOR: Record<TaskPriority, 'textSecondary' | 'primary' | 'danger'> = {
  low: 'textSecondary',
  medium: 'primary',
  high: 'danger',
};

function nextStatus(status: TaskStatus): TaskStatus {
  const index = STATUS_ORDER.indexOf(status);
  return STATUS_ORDER[(index + 1) % STATUS_ORDER.length];
}

export default function TasksScreen() {
  const queryClient = useQueryClient();
  const [modalVisible, setModalVisible] = useState(false);

  const { data: tasks = [] } = useQuery({ queryKey: ['tasks'], queryFn: listTasks });

  const invalidate = () => queryClient.invalidateQueries({ queryKey: ['tasks'] });

  const cycleStatus = useMutation({
    mutationFn: (task: Task) => setTaskStatus(task.id, nextStatus(task.status)),
    onSuccess: invalidate,
  });

  const removeTask = useMutation({
    mutationFn: (id: string) => deleteTask(id),
    onSuccess: invalidate,
  });

  const sections = STATUS_ORDER.map((status) => ({
    title: STATUS_LABEL[status],
    data: tasks.filter((t) => t.status === status),
  })).filter((section) => section.data.length > 0);

  return (
    <ThemedView style={styles.container}>
      <ScreenHeader title="Tasks" onAddPress={() => setModalVisible(true)} addLabel="New task" />
      {tasks.length === 0 ? (
        <EmptyState icon="checkbox-outline" title="No tasks yet" message="Tap + to add your first task." />
      ) : (
        <SectionList
          sections={sections}
          keyExtractor={(item) => item.id}
          contentContainerStyle={styles.list}
          renderSectionHeader={({ section }) => (
            <ThemedText type="smallBold" themeColor="textSecondary" style={styles.sectionTitle}>
              {section.title.toUpperCase()}
            </ThemedText>
          )}
          renderItem={({ item }) => (
            <TaskRow task={item} onCycle={() => cycleStatus.mutate(item)} onDelete={() => removeTask.mutate(item.id)} />
          )}
        />
      )}

      <NewTaskModal visible={modalVisible} onClose={() => setModalVisible(false)} onCreated={invalidate} />
    </ThemedView>
  );
}

function TaskRow({ task, onCycle, onDelete }: { task: Task; onCycle: () => void; onDelete: () => void }) {
  const theme = useTheme();
  return (
    <View style={[styles.row, { borderColor: theme.border, backgroundColor: theme.backgroundElement }]}>
      <Pressable onPress={onCycle} hitSlop={8} style={styles.checkbox}>
        <Ionicons
          name={task.status === 'done' ? 'checkmark-circle' : 'ellipse-outline'}
          size={24}
          color={task.status === 'done' ? theme.success : theme.textSecondary}
        />
      </Pressable>
      <View style={styles.rowText}>
        <ThemedText
          type="smallBold"
          style={task.status === 'done' ? styles.doneText : undefined}>
          {task.title}
        </ThemedText>
        <View style={styles.metaRow}>
          <ThemedText type="small" themeColor={PRIORITY_COLOR[task.priority]}>
            {task.priority.toUpperCase()}
          </ThemedText>
          {task.dueDate && (
            <ThemedText type="small" themeColor="textSecondary">
              · due {dayjs(task.dueDate).format('MMM D')}
            </ThemedText>
          )}
        </View>
      </View>
      <Pressable onPress={onDelete} hitSlop={8}>
        <Ionicons name="trash-outline" size={18} color={theme.textSecondary} />
      </Pressable>
    </View>
  );
}

function NewTaskModal({
  visible,
  onClose,
  onCreated,
}: {
  visible: boolean;
  onClose: () => void;
  onCreated: () => void;
}) {
  const theme = useTheme();
  const [title, setTitle] = useState('');
  const [priority, setPriority] = useState<TaskPriority>('medium');

  const create = useMutation({
    mutationFn: () => createTask({ title: title.trim(), priority, description: undefined, dueDate: undefined }),
    onSuccess: () => {
      setTitle('');
      setPriority('medium');
      onCreated();
      onClose();
    },
  });

  return (
    <Modal visible={visible} animationType="slide" transparent onRequestClose={onClose}>
      <View style={styles.modalBackdrop}>
        <ThemedView type="background" style={styles.modalCard}>
          <ThemedText type="smallBold">New task</ThemedText>
          <TextInput
            value={title}
            onChangeText={setTitle}
            placeholder="Task title"
            placeholderTextColor={theme.textSecondary}
            style={[styles.input, { color: theme.text, borderColor: theme.border }]}
            autoFocus
          />
          <View style={styles.priorityRow}>
            {(['low', 'medium', 'high'] as TaskPriority[]).map((p) => (
              <Pressable
                key={p}
                onPress={() => setPriority(p)}
                style={[
                  styles.priorityChip,
                  { borderColor: theme.border, backgroundColor: priority === p ? theme.primary : 'transparent' },
                ]}>
                <ThemedText type="small" themeColor={priority === p ? 'primaryText' : 'text'}>
                  {p}
                </ThemedText>
              </Pressable>
            ))}
          </View>
          <View style={styles.modalActions}>
            <Pressable onPress={onClose} style={styles.modalButton}>
              <ThemedText themeColor="textSecondary">Cancel</ThemedText>
            </Pressable>
            <Pressable
              onPress={() => title.trim() && create.mutate()}
              style={[styles.modalButton, { backgroundColor: theme.primary, borderRadius: Spacing.two }]}>
              <ThemedText themeColor="primaryText">Add</ThemedText>
            </Pressable>
          </View>
        </ThemedView>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  list: { padding: Spacing.three, gap: Spacing.two },
  sectionTitle: { marginTop: Spacing.three, marginBottom: Spacing.one },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.two,
    borderRadius: Spacing.three,
    borderWidth: StyleSheet.hairlineWidth,
    padding: Spacing.three,
    marginBottom: Spacing.two,
  },
  checkbox: { paddingRight: Spacing.one },
  rowText: { flex: 1, gap: 2 },
  metaRow: { flexDirection: 'row', gap: 4 },
  doneText: { textDecorationLine: 'line-through', opacity: 0.6 },
  modalBackdrop: { flex: 1, backgroundColor: 'rgba(0,0,0,0.4)', justifyContent: 'flex-end' },
  modalCard: { padding: Spacing.four, gap: Spacing.three, borderTopLeftRadius: Spacing.four, borderTopRightRadius: Spacing.four },
  input: {
    borderWidth: StyleSheet.hairlineWidth,
    borderRadius: Spacing.two,
    paddingHorizontal: Spacing.three,
    paddingVertical: Spacing.two + 2,
    fontSize: 16,
  },
  priorityRow: { flexDirection: 'row', gap: Spacing.two },
  priorityChip: {
    paddingHorizontal: Spacing.three,
    paddingVertical: Spacing.one + 2,
    borderRadius: Spacing.five,
    borderWidth: StyleSheet.hairlineWidth,
  },
  modalActions: { flexDirection: 'row', justifyContent: 'flex-end', gap: Spacing.three, marginTop: Spacing.two },
  modalButton: { paddingHorizontal: Spacing.four, paddingVertical: Spacing.two },
});
