import { Ionicons } from '@expo/vector-icons';
import { useRef, useState } from 'react';
import { ActivityIndicator, Pressable, ScrollView, StyleSheet, TextInput, View } from 'react-native';

import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Spacing } from '@/constants/theme';
import { useTheme } from '@/hooks/use-theme';
import type { AgentStep } from '@nova/shared';

const DEMO_PLAN = ['Understand the goal', 'Look up relevant information', 'Draft a result', 'Review and finalize'];

export default function AgentScreen() {
  const theme = useTheme();
  const [goal, setGoal] = useState('');
  const [plan, setPlan] = useState<string[] | null>(null);
  const [steps, setSteps] = useState<AgentStep[]>([]);
  const [running, setRunning] = useState(false);
  const [finalResult, setFinalResult] = useState<string | null>(null);
  const timers = useRef<ReturnType<typeof setTimeout>[]>([]);

  const runGoal = () => {
    if (!goal.trim() || running) return;
    timers.current.forEach(clearTimeout);
    timers.current = [];

    setRunning(true);
    setFinalResult(null);
    setPlan(DEMO_PLAN);
    setSteps(DEMO_PLAN.map((summary, i) => ({ id: `step-${i}`, summary, status: 'pending' })));

    DEMO_PLAN.forEach((_, index) => {
      timers.current.push(
        setTimeout(() => {
          setSteps((prev) =>
            prev.map((s, i) => (i === index ? { ...s, status: 'running' } : i < index ? { ...s, status: 'success' } : s))
          );
        }, index * 900)
      );
    });

    timers.current.push(
      setTimeout(() => {
        setSteps((prev) => prev.map((s) => ({ ...s, status: 'success' })));
        setRunning(false);
        setFinalResult(
          `This is a simulated run for "${goal.trim()}". The real Agent (Phase 2 of the build plan) will plan and execute actual tool calls — file access, browser automation, email, calendar — with approval gating before anything risky runs.`
        );
      }, DEMO_PLAN.length * 900)
    );
  };

  return (
    <ScrollView style={{ flex: 1 }} contentContainerStyle={styles.container}>
      <ThemedText type="small" themeColor="textSecondary">
        This is a preview of the Agent UI. It simulates a plan and step timeline locally — the real
        tool-calling agent core (file ops, browser automation, approvals) is a later build phase and
        isn't wired up yet.
      </ThemedText>

      <ThemedView type="backgroundElement" style={styles.goalCard}>
        <TextInput
          value={goal}
          onChangeText={setGoal}
          placeholder="Give Nova a goal, e.g. 'Plan my week'"
          placeholderTextColor={theme.textSecondary}
          style={[styles.input, { color: theme.text, borderColor: theme.border }]}
          multiline
        />
        <Pressable
          onPress={runGoal}
          disabled={!goal.trim() || running}
          style={[styles.runButton, { backgroundColor: theme.primary, opacity: !goal.trim() || running ? 0.6 : 1 }]}>
          {running ? <ActivityIndicator color={theme.primaryText} /> : <Ionicons name="play" size={16} color={theme.primaryText} />}
          <ThemedText themeColor="primaryText" type="smallBold">
            {running ? 'Running…' : 'Run goal'}
          </ThemedText>
        </Pressable>
      </ThemedView>

      {plan && (
        <View style={styles.timeline}>
          {steps.map((step) => (
            <View key={step.id} style={styles.stepRow}>
              <StepIcon status={step.status} />
              <ThemedText type="small" style={step.status === 'success' ? styles.stepDone : undefined}>
                {step.summary}
              </ThemedText>
            </View>
          ))}
        </View>
      )}

      {finalResult && (
        <ThemedView type="backgroundElement" style={styles.resultCard}>
          <ThemedText type="smallBold">Result</ThemedText>
          <ThemedText type="small">{finalResult}</ThemedText>
        </ThemedView>
      )}
    </ScrollView>
  );
}

function StepIcon({ status }: { status: AgentStep['status'] }) {
  const theme = useTheme();
  if (status === 'success') return <Ionicons name="checkmark-circle" size={18} color={theme.success} />;
  if (status === 'running') return <ActivityIndicator size="small" color={theme.primary} />;
  return <Ionicons name="ellipse-outline" size={18} color={theme.textSecondary} />;
}

const styles = StyleSheet.create({
  container: { padding: Spacing.four, gap: Spacing.three },
  goalCard: { borderRadius: Spacing.three, padding: Spacing.three, gap: Spacing.three },
  input: {
    borderWidth: StyleSheet.hairlineWidth,
    borderRadius: Spacing.two,
    paddingHorizontal: Spacing.three,
    paddingVertical: Spacing.two,
    fontSize: 16,
    minHeight: 60,
  },
  runButton: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: Spacing.one,
    borderRadius: Spacing.two,
    paddingVertical: Spacing.two + 2,
  },
  timeline: { gap: Spacing.two },
  stepRow: { flexDirection: 'row', alignItems: 'center', gap: Spacing.two },
  stepDone: { opacity: 0.7 },
  resultCard: { borderRadius: Spacing.three, padding: Spacing.three, gap: Spacing.one },
});
