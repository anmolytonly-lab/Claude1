import { Ionicons } from '@expo/vector-icons';
import { useState } from 'react';
import { Pressable, ScrollView, StyleSheet, TextInput, View } from 'react-native';

import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Spacing } from '@/constants/theme';
import { useTheme } from '@/hooks/use-theme';
import { useSessionStore } from '@/lib/session-store';
import type { ThemePreference } from '@nova/shared';

const THEME_OPTIONS: { value: ThemePreference; label: string; icon: 'sunny' | 'moon' | 'phone-portrait' }[] = [
  { value: 'light', label: 'Light', icon: 'sunny' },
  { value: 'dark', label: 'Dark', icon: 'moon' },
  { value: 'system', label: 'System', icon: 'phone-portrait' },
];

export default function SettingsScreen() {
  const theme = useTheme();
  const profile = useSessionStore((s) => s.profile);
  const setTheme = useSessionStore((s) => s.setTheme);
  const updateDisplayName = useSessionStore((s) => s.updateDisplayName);

  const [displayName, setDisplayName] = useState(profile?.displayName ?? '');

  return (
    <ScrollView style={{ flex: 1 }} contentContainerStyle={styles.container}>
      <ThemedView type="backgroundElement" style={styles.section}>
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
      </ThemedView>

      <ThemedView type="backgroundElement" style={styles.section}>
        <ThemedText type="smallBold" themeColor="textSecondary">
          APPEARANCE
        </ThemedText>
        <View style={styles.themeRow}>
          {THEME_OPTIONS.map((option) => {
            const active = profile?.themePreference === option.value;
            return (
              <Pressable
                key={option.value}
                onPress={() => setTheme(option.value)}
                style={[
                  styles.themeChip,
                  { borderColor: theme.border, backgroundColor: active ? theme.primary : 'transparent' },
                ]}>
                <Ionicons name={option.icon} size={16} color={active ? theme.primaryText : theme.text} />
                <ThemedText type="small" themeColor={active ? 'primaryText' : 'text'}>
                  {option.label}
                </ThemedText>
              </Pressable>
            );
          })}
        </View>
      </ThemedView>

      <ThemedView type="backgroundElement" style={styles.section}>
        <ThemedText type="smallBold" themeColor="textSecondary">
          ABOUT
        </ThemedText>
        <ThemedText type="small" themeColor="textSecondary">
          Nova is a personal, single-user app running in local/mock mode — chat, tasks, notes, and
          calendar are stored on this device only. Connect a real Supabase project and Gemini API key
          to switch to live sync.
        </ThemedText>
      </ThemedView>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { padding: Spacing.four, gap: Spacing.three },
  section: { borderRadius: Spacing.three, padding: Spacing.three, gap: Spacing.two },
  input: {
    borderWidth: StyleSheet.hairlineWidth,
    borderRadius: Spacing.two,
    paddingHorizontal: Spacing.three,
    paddingVertical: Spacing.two,
    fontSize: 16,
  },
  themeRow: { flexDirection: 'row', gap: Spacing.two },
  themeChip: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    paddingHorizontal: Spacing.three,
    paddingVertical: Spacing.two,
    borderRadius: Spacing.five,
    borderWidth: StyleSheet.hairlineWidth,
  },
});
