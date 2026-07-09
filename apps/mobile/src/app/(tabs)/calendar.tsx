import { Ionicons } from '@expo/vector-icons';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import dayjs from 'dayjs';
import { BlurView } from 'expo-blur';
import { LinearGradient } from 'expo-linear-gradient';
import { useState } from 'react';
import { Alert, Modal, Pressable, SectionList, StyleSheet, TextInput, View } from 'react-native';
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
import { createCalendarEvent, deleteCalendarEvent, listCalendarEvents } from '@/lib/mock-db';
import type { CalendarEvent } from '@nova/shared';

export default function CalendarScreen() {
  const queryClient = useQueryClient();
  const [modalVisible, setModalVisible] = useState(false);

  const { data: events = [] } = useQuery({ queryKey: ['calendar-events'], queryFn: listCalendarEvents });

  const invalidate = () => queryClient.invalidateQueries({ queryKey: ['calendar-events'] });

  const removeEvent = useMutation({
    mutationFn: (id: string) => deleteCalendarEvent(id),
    onSuccess: invalidate,
  });

  const grouped = new Map<string, CalendarEvent[]>();
  for (const event of events) {
    const key = dayjs(event.startsAt).format('YYYY-MM-DD');
    grouped.set(key, [...(grouped.get(key) ?? []), event]);
  }
  const sections = Array.from(grouped.entries()).map(([date, data]) => ({
    title: dayjs(date).format('dddd, MMM D'),
    data,
  }));

  const confirmDelete = (event: CalendarEvent) => {
    Alert.alert('Delete event', `Remove "${event.title}"?`, [
      { text: 'Cancel', style: 'cancel' },
      { text: 'Delete', style: 'destructive', onPress: () => removeEvent.mutate(event.id) },
    ]);
  };

  return (
    <ThemedView style={styles.container}>
      <ScreenHeader title="Calendar" subtitle={`${events.length} upcoming`} onAddPress={() => setModalVisible(true)} addLabel="New event" />
      {events.length === 0 ? (
        <EmptyState icon="calendar-outline" title="Nothing scheduled" message="Tap + to add your first event." />
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
            <EventRow event={item} index={index} onDelete={() => confirmDelete(item)} />
          )}
        />
      )}

      <NewEventModal visible={modalVisible} onClose={() => setModalVisible(false)} onCreated={invalidate} />
    </ThemedView>
  );
}

function EventRow({ event, index, onDelete }: { event: CalendarEvent; index: number; onDelete: () => void }) {
  const theme = useTheme();
  const gradients = useGradients();
  return (
    <Animated.View entering={FadeInDown.delay(index * 40).duration(280)}>
      <GlassCard style={styles.row} radius={Radius.large}>
        <LinearGradient colors={gradients.primary} style={styles.timeColumn}>
          <ThemedText type="smallBold" themeColor="primaryText">
            {dayjs(event.startsAt).format('h:mm')}
          </ThemedText>
          <ThemedText type="small" themeColor="primaryText" style={{ opacity: 0.85 }}>
            {dayjs(event.startsAt).format('A')}
          </ThemedText>
        </LinearGradient>
        <View style={styles.rowText}>
          <ThemedText type="smallBold">{event.title}</ThemedText>
          <ThemedText type="small" themeColor="textSecondary">
            until {dayjs(event.endsAt).format('h:mm A')}
            {event.location ? ` · ${event.location}` : ''}
          </ThemedText>
        </View>
        <AnimatedPressable onPress={onDelete} hitSlop={8}>
          <Ionicons name="trash-outline" size={18} color={theme.textSecondary} />
        </AnimatedPressable>
      </GlassCard>
    </Animated.View>
  );
}

function NewEventModal({
  visible,
  onClose,
  onCreated,
}: {
  visible: boolean;
  onClose: () => void;
  onCreated: () => void;
}) {
  const theme = useTheme();
  const scheme = useActiveScheme();
  const [title, setTitle] = useState('');
  const [date, setDate] = useState(dayjs().add(1, 'day').format('YYYY-MM-DD'));
  const [startTime, setStartTime] = useState('09:00');
  const [endTime, setEndTime] = useState('10:00');

  const create = useMutation({
    mutationFn: () => {
      const startsAt = dayjs(`${date} ${startTime}`, 'YYYY-MM-DD HH:mm').toISOString();
      const endsAt = dayjs(`${date} ${endTime}`, 'YYYY-MM-DD HH:mm').toISOString();
      return createCalendarEvent({ title: title.trim(), startsAt, endsAt, description: undefined, location: undefined });
    },
    onSuccess: () => {
      setTitle('');
      onCreated();
      onClose();
    },
  });

  return (
    <Modal visible={visible} animationType="slide" transparent onRequestClose={onClose}>
      <View style={styles.modalBackdrop}>
        <BlurView intensity={30} tint={scheme === 'dark' ? 'dark' : 'light'} style={StyleSheet.absoluteFill} />
        <ThemedView type="backgroundElement" style={styles.modalCard}>
          <ThemedText type="smallBold">New event</ThemedText>
          <TextInput
            value={title}
            onChangeText={setTitle}
            placeholder="Event title"
            placeholderTextColor={theme.textSecondary}
            style={[styles.input, { color: theme.text, borderColor: theme.border }]}
            autoFocus
          />
          <View style={styles.fieldRow}>
            <Field label="Date (YYYY-MM-DD)" value={date} onChangeText={setDate} />
          </View>
          <View style={styles.fieldRow}>
            <Field label="Start (HH:mm)" value={startTime} onChangeText={setStartTime} />
            <Field label="End (HH:mm)" value={endTime} onChangeText={setEndTime} />
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

function Field({
  label,
  value,
  onChangeText,
}: {
  label: string;
  value: string;
  onChangeText: (v: string) => void;
}) {
  const theme = useTheme();
  return (
    <View style={{ flex: 1, gap: 4 }}>
      <ThemedText type="small" themeColor="textSecondary">
        {label}
      </ThemedText>
      <TextInput
        value={value}
        onChangeText={onChangeText}
        style={[styles.input, { color: theme.text, borderColor: theme.border }]}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  list: { padding: Spacing.three, gap: Spacing.two },
  sectionTitle: { marginTop: Spacing.three, marginBottom: Spacing.one },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.three,
    padding: Spacing.three,
    marginBottom: Spacing.two,
  },
  timeColumn: {
    width: 64,
    paddingVertical: Spacing.two,
    borderRadius: Radius.medium,
    alignItems: 'center',
    gap: 2,
  },
  rowText: { flex: 1, gap: 2 },
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
  fieldRow: { flexDirection: 'row', gap: Spacing.three },
  modalActions: { flexDirection: 'row', justifyContent: 'flex-end', alignItems: 'center', gap: Spacing.three, marginTop: Spacing.two },
  cancelButton: { paddingHorizontal: Spacing.three, paddingVertical: Spacing.two },
});
