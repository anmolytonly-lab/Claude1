import { Ionicons } from '@expo/vector-icons';
import { router } from 'expo-router';
import type { ComponentProps } from 'react';
import { Pressable, StyleSheet, View } from 'react-native';

import { ScreenHeader } from '@/components/screen-header';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Spacing } from '@/constants/theme';
import { useTheme } from '@/hooks/use-theme';
import { useSessionStore } from '@/lib/session-store';

interface Row {
  icon: ComponentProps<typeof Ionicons>['name'];
  title: string;
  subtitle: string;
  href: '/memory' | '/agent' | '/voice' | '/organizations' | '/settings';
}

const ROWS: Row[] = [
  { icon: 'sparkles', title: 'Memory', subtitle: "What Nova remembers about you", href: '/memory' },
  { icon: 'flash', title: 'Agent', subtitle: 'Give Nova a goal to work on', href: '/agent' },
  { icon: 'mic', title: 'Voice', subtitle: 'Push-to-talk with Nova', href: '/voice' },
  { icon: 'business', title: 'Organizations', subtitle: 'Personal workspace', href: '/organizations' },
  { icon: 'settings', title: 'Settings', subtitle: 'Profile and theme', href: '/settings' },
];

export default function MoreScreen() {
  const theme = useTheme();
  const profile = useSessionStore((s) => s.profile);

  return (
    <ThemedView style={styles.container}>
      <ScreenHeader title="More" />
      <View style={styles.profileCard}>
        <View style={[styles.avatar, { backgroundColor: theme.primary }]}>
          <ThemedText themeColor="primaryText" type="smallBold">
            {(profile?.displayName ?? '?').slice(0, 1).toUpperCase()}
          </ThemedText>
        </View>
        <View>
          <ThemedText type="smallBold">{profile?.displayName}</ThemedText>
          <ThemedText type="small" themeColor="textSecondary">
            Personal workspace
          </ThemedText>
        </View>
      </View>

      <View style={styles.rows}>
        {ROWS.map((row) => (
          <Pressable
            key={row.href}
            onPress={() => router.push(row.href)}
            style={({ pressed }) => [
              styles.row,
              { borderColor: theme.border, backgroundColor: pressed ? theme.backgroundSelected : theme.backgroundElement },
            ]}>
            <Ionicons name={row.icon} size={22} color={theme.primary} />
            <View style={styles.rowText}>
              <ThemedText type="smallBold">{row.title}</ThemedText>
              <ThemedText type="small" themeColor="textSecondary">
                {row.subtitle}
              </ThemedText>
            </View>
            <Ionicons name="chevron-forward" size={18} color={theme.textSecondary} />
          </Pressable>
        ))}
      </View>
    </ThemedView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  profileCard: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.three,
    paddingHorizontal: Spacing.four,
    paddingVertical: Spacing.three,
  },
  avatar: { width: 44, height: 44, borderRadius: 22, alignItems: 'center', justifyContent: 'center' },
  rows: { paddingHorizontal: Spacing.three, gap: Spacing.two },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.three,
    borderRadius: Spacing.three,
    borderWidth: StyleSheet.hairlineWidth,
    padding: Spacing.three,
  },
  rowText: { flex: 1, gap: 2 },
});
