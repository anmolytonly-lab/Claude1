import { Ionicons } from '@expo/vector-icons';
import { LinearGradient } from 'expo-linear-gradient';
import { useState } from 'react';
import { Pressable, ScrollView, StyleSheet, TextInput, View } from 'react-native';
import Animated, { FadeInDown } from 'react-native-reanimated';

import { GlassCard } from '@/components/glass-card';
import { GradientButton } from '@/components/gradient-button';
import { ThemedText } from '@/components/themed-text';
import { Radius, Spacing } from '@/constants/theme';
import { useGradients, useTheme } from '@/hooks/use-theme';
import { AVAILABLE_MODELS, useApiSettingsStore } from '@/lib/api-settings-store';
import { resolveApiKey, resolveKeySource, testGeminiConnection } from '@/lib/gemini';
import { useSessionStore } from '@/lib/session-store';
import type { ThemePreference } from '@nova/shared';

const THEME_OPTIONS: { value: ThemePreference; label: string; icon: 'sunny' | 'moon' | 'phone-portrait' }[] = [
  { value: 'light', label: 'Light', icon: 'sunny' },
  { value: 'dark', label: 'Dark', icon: 'moon' },
  { value: 'system', label: 'System', icon: 'phone-portrait' },
];

export default function SettingsScreen() {
  const theme = useTheme();
  const gradients = useGradients();
  const profile = useSessionStore((s) => s.profile);
  const setTheme = useSessionStore((s) => s.setTheme);
  const updateDisplayName = useSessionStore((s) => s.updateDisplayName);

  const [displayName, setDisplayName] = useState(profile?.displayName ?? '');
  const apiKeyOverride = useApiSettingsStore((s) => s.apiKey);
  const geminiLive = !!resolveApiKey(apiKeyOverride);

  return (
    <ScrollView style={{ flex: 1 }} contentContainerStyle={styles.container}>
      <Animated.View entering={FadeInDown.duration(280)}>
        <GlassCard style={styles.section} radius={Radius.large}>
          <ThemedText type="smallBold" themeColor="textSecondary">
            PROFILE
          </ThemedText>
          <TextInput
            value={displayName}
            onChangeText={setDisplayName}
            onBlur={() => displayName.trim() && updateDisplayName(displayName.trim())}
            placeholder="Your name"
            placeholderTextColor={theme.textSecondary}
            style={[styles.input, { color: theme.text, borderColor: theme.border }]}
          />
        </GlassCard>
      </Animated.View>

      <Animated.View entering={FadeInDown.delay(60).duration(280)}>
        <GlassCard style={styles.section} radius={Radius.large}>
          <ThemedText type="smallBold" themeColor="textSecondary">
            APPEARANCE
          </ThemedText>
          <View style={styles.themeRow}>
            {THEME_OPTIONS.map((option) => {
              const active = profile?.themePreference === option.value;
              return (
                <Pressable key={option.value} onPress={() => setTheme(option.value)} style={styles.themeChipWrap}>
                  {active ? (
                    <LinearGradient colors={gradients.primary} style={styles.themeChip}>
                      <Ionicons name={option.icon} size={16} color={theme.primaryText} />
                      <ThemedText type="small" themeColor="primaryText">
                        {option.label}
                      </ThemedText>
                    </LinearGradient>
                  ) : (
                    <View style={[styles.themeChip, { borderColor: theme.border, borderWidth: StyleSheet.hairlineWidth }]}>
                      <Ionicons name={option.icon} size={16} color={theme.text} />
                      <ThemedText type="small">{option.label}</ThemedText>
                    </View>
                  )}
                </Pressable>
              );
            })}
          </View>
        </GlassCard>
      </Animated.View>

      <Animated.View entering={FadeInDown.delay(120).duration(280)}>
        <ApiProviderSection />
      </Animated.View>

      <Animated.View entering={FadeInDown.delay(180).duration(280)}>
        <GlassCard style={styles.section} radius={Radius.large}>
          <View style={styles.aboutHeader}>
            <ThemedText type="smallBold" themeColor="textSecondary">
              ABOUT
            </ThemedText>
            <View style={[styles.statusDot, { backgroundColor: geminiLive ? theme.success : theme.warning }]} />
          </View>
          <ThemedText type="small" themeColor="textSecondary">
            Nova is a personal, single-user app. Tasks, notes, and calendar are stored on this device
            only. Chat replies come from{' '}
            {geminiLive ? 'Gemini, live.' : 'a local canned simulation (no Gemini key configured).'}
          </ThemedText>
        </GlassCard>
      </Animated.View>
    </ScrollView>
  );
}

function ApiProviderSection() {
  const theme = useTheme();
  const gradients = useGradients();
  const apiKey = useApiSettingsStore((s) => s.apiKey);
  const model = useApiSettingsStore((s) => s.model);
  const setApiKey = useApiSettingsStore((s) => s.setApiKey);
  const setModel = useApiSettingsStore((s) => s.setModel);

  const [keyDraft, setKeyDraft] = useState(apiKey ?? '');
  const [reveal, setReveal] = useState(false);
  const [testing, setTesting] = useState(false);
  const [testResult, setTestResult] = useState<{ ok: boolean; message: string } | null>(null);

  const keySource = resolveKeySource(apiKey);
  const dirty = keyDraft.trim() !== (apiKey ?? '');

  const onSaveKey = async () => {
    await setApiKey(keyDraft.trim() || null);
    setTestResult(null);
  };

  const onClearKey = async () => {
    setKeyDraft('');
    await setApiKey(null);
    setTestResult(null);
  };

  const onTest = async () => {
    setTesting(true);
    setTestResult(null);
    const result = await testGeminiConnection();
    setTestResult(result);
    setTesting(false);
  };

  return (
    <GlassCard style={styles.section} radius={Radius.large}>
      <View style={styles.aboutHeader}>
        <ThemedText type="smallBold" themeColor="textSecondary">
          AI PROVIDER
        </ThemedText>
        <ThemedText type="small" themeColor="textSecondary">
          {keySource === 'custom' ? 'Custom key' : keySource === 'env' ? 'Using .env key' : 'No key set'}
        </ThemedText>
      </View>

      <ThemedText type="small" themeColor="textSecondary">
        Switch models or paste a different Gemini API key any time - no rebuild needed. Get a free key
        at aistudio.google.com.
      </ThemedText>

      <View style={styles.keyRow}>
        <TextInput
          value={keyDraft}
          onChangeText={setKeyDraft}
          placeholder="Paste a Gemini API key (optional)"
          placeholderTextColor={theme.textSecondary}
          secureTextEntry={!reveal}
          autoCapitalize="none"
          autoCorrect={false}
          style={[styles.input, styles.keyInput, { color: theme.text, borderColor: theme.border }]}
        />
        <Pressable onPress={() => setReveal((r) => !r)} hitSlop={8} style={styles.eyeButton}>
          <Ionicons name={reveal ? 'eye-off' : 'eye'} size={18} color={theme.textSecondary} />
        </Pressable>
      </View>

      <View style={styles.keyActions}>
        <Pressable onPress={onClearKey} disabled={!apiKey} style={[styles.textButton, { opacity: apiKey ? 1 : 0.4 }]}>
          <ThemedText type="small" themeColor="danger">
            Clear
          </ThemedText>
        </Pressable>
        <GradientButton label="Save key" onPress={onSaveKey} disabled={!dirty} />
      </View>

      <ThemedText type="smallBold" themeColor="textSecondary" style={styles.modelLabel}>
        MODEL
      </ThemedText>
      <View style={styles.modelList}>
        {AVAILABLE_MODELS.map((option) => {
          const active = model === option.id;
          return (
            <Pressable key={option.id} onPress={() => setModel(option.id)} style={styles.modelRowWrap}>
              {active ? (
                <LinearGradient colors={gradients.primary} style={styles.modelRow}>
                  <View style={styles.modelRowText}>
                    <ThemedText type="small" themeColor="primaryText">
                      {option.label}
                    </ThemedText>
                    <ThemedText type="small" themeColor="primaryText" style={{ opacity: 0.85 }}>
                      {option.hint}
                    </ThemedText>
                  </View>
                  <Ionicons name="checkmark-circle" size={18} color={theme.primaryText} />
                </LinearGradient>
              ) : (
                <View style={[styles.modelRow, { borderColor: theme.border, borderWidth: StyleSheet.hairlineWidth }]}>
                  <View style={styles.modelRowText}>
                    <ThemedText type="small">{option.label}</ThemedText>
                    <ThemedText type="small" themeColor="textSecondary">
                      {option.hint}
                    </ThemedText>
                  </View>
                </View>
              )}
            </Pressable>
          );
        })}
      </View>

      <Pressable
        onPress={onTest}
        disabled={testing}
        style={[styles.testButton, { borderColor: theme.border, opacity: testing ? 0.6 : 1 }]}>
        <ThemedText type="smallBold">{testing ? 'Testing…' : 'Test connection'}</ThemedText>
      </Pressable>

      {testResult && (
        <View style={styles.testResultRow}>
          <Ionicons
            name={testResult.ok ? 'checkmark-circle' : 'alert-circle'}
            size={16}
            color={testResult.ok ? theme.success : theme.danger}
          />
          <ThemedText type="small" themeColor={testResult.ok ? 'success' : 'danger'} style={{ flex: 1 }}>
            {testResult.message}
          </ThemedText>
        </View>
      )}
    </GlassCard>
  );
}

const styles = StyleSheet.create({
  container: { padding: Spacing.four, gap: Spacing.three },
  section: { padding: Spacing.three, gap: Spacing.two },
  input: {
    borderWidth: StyleSheet.hairlineWidth,
    borderRadius: Radius.medium,
    paddingHorizontal: Spacing.three,
    paddingVertical: Spacing.two,
    fontSize: 16,
  },
  themeRow: { flexDirection: 'row', gap: Spacing.two },
  themeChipWrap: { borderRadius: Radius.pill, overflow: 'hidden' },
  themeChip: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    paddingHorizontal: Spacing.three,
    paddingVertical: Spacing.two,
    borderRadius: Radius.pill,
  },
  aboutHeader: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' },
  statusDot: { width: 8, height: 8, borderRadius: Radius.pill },
  keyRow: { flexDirection: 'row', alignItems: 'center', gap: Spacing.two },
  keyInput: { flex: 1 },
  eyeButton: { padding: Spacing.one },
  keyActions: { flexDirection: 'row', justifyContent: 'flex-end', alignItems: 'center', gap: Spacing.three },
  textButton: { paddingHorizontal: Spacing.two, paddingVertical: Spacing.two },
  modelLabel: { marginTop: Spacing.one },
  modelList: { gap: Spacing.two },
  modelRowWrap: { borderRadius: Radius.medium, overflow: 'hidden' },
  modelRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: Spacing.three,
    paddingVertical: Spacing.two + 2,
    borderRadius: Radius.medium,
  },
  modelRowText: { gap: 2 },
  testButton: {
    borderWidth: StyleSheet.hairlineWidth,
    borderRadius: Radius.medium,
    paddingVertical: Spacing.two + 2,
    alignItems: 'center',
  },
  testResultRow: { flexDirection: 'row', alignItems: 'center', gap: Spacing.one },
});
