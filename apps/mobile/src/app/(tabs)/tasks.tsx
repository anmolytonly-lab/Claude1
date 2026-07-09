import { Ionicons } from '@expo/vector-icons';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import dayjs from 'dayjs';
import { BlurView } from 'expo-blur';
import { LinearGradient } from 'expo-linear-gradient';
import { useState } from 'react';
import { Modal, Pressable, SectionList, StyleSheet, TextInput, View } from 'react-native';
import Animated, { FadeInDown } from 'react-native-reanimated';

import { AnimatedPressable } from '@/components/animated-pressable';
import { EmptyState } from '@/components/empty-state';
import { GlassCard } from '@/components/glass-card';
import { GradientButton } from '@/components/gradient-button';
import { ScreenHeader } from '@/components/screen-header';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Radius, Spacing } from '@/constants/theme';
import { useActiveScheme, useGradients, useTheme } from '@/hooks/use-theme';
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
      <ScreenHeader title="Tasks" subtitle={`${tasks.length} total`} onAddPress={() => setModalVisible(true)} addLabel="New task" />
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
          renderItem={({ item, index }) => (
            <TaskRow
              task={item}
              index={index}
              onCycle={() => cycleStatus.mutate(item)}
              onDelete={() => removeTask.mutate(item.id)}
            />
          )}
        />
      )}

      <NewTaskModal visible={modalVisible} onClose={() => setModalVisible(false)} onCreated={invalidate} />
    </ThemedView>
  );
}

function TaskRow({
  task,
  index,
  onCycle,
  onDelete,
}: {
  task: Task;
  index: number;
  onCycle: () => void;
  onDelete: () => void;
}) {
  const theme = useTheme();
  const gradients = useGradients();
  const isDone = task.status === 'done';

  return (
    <Animated.View entering={FadeInDown.delay(index * 30).duration(280)}>
      <GlassCard style={styles.row} radius={Radius.large}>
        <AnimatedPressable onPress={onCycle} hitSlop={8} style={styles.checkbox} scaleTo={0.85}>
          {isDone ? (
            <LinearGradient colors={gradients.success} style={styles.checkCircle}>
              <Ionicons name="checkmark" size={16} color={theme.primaryText} />
            </LinearGradient>
          ) : (
            <View style={[styles.checkCircleOutline, { borderColor: theme.textSecondary }]} />
          )}
        </AnimatedPressable>
        <View style={styles.rowText}>
          <ThemedText type="smallBold" style={isDone ? styles.doneText : undefined}>
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
        <AnimatedPressable onPress={onDelete} hitSlop={8}>
          <Ionicons name="trash-outline" size={18} color={theme.textSecondary} />
        </AnimatedPressable>
      </GlassCard>
    </Animated.View>
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
  const gradients = useGradients();
  const scheme = useActiveScheme();
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
        <BlurView intensity={30} tint={scheme === 'dark' ? 'dark' : 'light'} style={StyleSheet.absoluteFill} />
        <ThemedView type="backgroundElement" style={styles.modalCard}>
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
            {(['low', 'medium', 'high'] as TaskPriority[]).map((p) => {
              const active = priority === p;
              return (
                <Pressable key={p} onPress={() => setPriority(p)} style={styles.priorityChipWrap}>
                  {active ? (
                    <LinearGradient colors={gradients.primary} style={styles.priorityChip}>
                      <ThemedText type="small" themeColor="primaryText">
                        {p}
                      </ThemedText>
                    </LinearGradient>
                  ) : (
                    <View style={[styles.priorityChip, { borderColor: theme.border, borderWidth: StyleSheet.hairlineWidth }]}>
                      <ThemedText type="small">{p}</ThemedText>
                    </View>
                  )}
                </Pressable>
              );
            })}
          </View>
          <View style={styles.modalActions}>
            <Pressable onPress={onClose} style={styles.cancelButton}>
              <ThemedText themeColor="textSecondary">Cancel</ThemedText>
            </Pressable>
            <GradientButton label="Add" onPress={() => title.trim() && create.mutate()} disabled={!title.trim()} />
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
    padding: Spacing.three,
    marginBottom: Spacing.two,
  },
  checkbox: { paddingRight: Spacing.one },
  checkCircle: {
    width: 26,
    height: 26,
    borderRadius: Radius.pill,
    alignItems: 'center',
    justifyContent: 'center',
  },
  checkCircleOutline: {
    width: 26,
    height: 26,
    borderRadius: Radius.pill,
    borderWidth: 2,
  },
  rowText: { flex: 1, gap: 2 },
  metaRow: { flexDirection: 'row', gap: 4 },
  doneText: { textDecorationLine: 'line-through', opacity: 0.6 },
  modalBackdrop: { flex: 1, justifyContent: 'flex-end' },
  modalCard: {
    padding: Spacing.four,
    gap: Spacing.three,
    borderTopLeftRadius: Radius.large,
    borderTopRightRadius: Radius.large,
  },
  input: {
    borderWidth: StyleSheet.hairlineWidth,
    borderRadius: Radius.medium,
    paddingHorizontal: Spacing.three,
    paddingVertical: Spacing.two + 2,
    fontSize: 16,
  },
  priorityRow: { flexDirection: 'row', gap: Spacing.two },
  priorityChipWrap: { borderRadius: Radius.pill, overflow: 'hidden' },
  priorityChip: {
    paddingHorizontal: Spacing.three,
    paddingVertical: Spacing.one + 2,
    borderRadius: Radius.pill,
  },
  modalActions: { flexDirection: 'row', justifyContent: 'flex-end', alignItems: 'center', gap: Spacing.three, marginTop: Spacing.two },
  cancelButton: { paddingHorizontal: Spacing.three, paddingVertical: Spacing.two },
});
