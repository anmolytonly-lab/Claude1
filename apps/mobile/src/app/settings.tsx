import { Ionicons } from '@expo/vector-icons';
import { LinearGradient } from 'expo-linear-gradient';
import { useState } from 'react';
import { Pressable, ScrollView, StyleSheet, TextInput, View } from 'react-native';
import Animated, { FadeInDown } from 'react-native-reanimated';

import { GlassCard } from '@/components/glass-card';
import { ThemedText } from '@/components/themed-text';
import { Radius, Spacing } from '@/constants/theme';
import { useGradients, useTheme } from '@/hooks/use-theme';
import { isGeminiConfigured } from '@/lib/gemini';
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
  const geminiLive = isGeminiConfigured();

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
});
