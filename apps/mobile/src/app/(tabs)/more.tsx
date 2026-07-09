import { Ionicons } from '@expo/vector-icons';
import { LinearGradient } from 'expo-linear-gradient';
import { router } from 'expo-router';
import type { ComponentProps } from 'react';
import { StyleSheet, View } from 'react-native';
import Animated, { FadeInDown } from 'react-native-reanimated';

import { AnimatedPressable } from '@/components/animated-pressable';
import { GlassCard } from '@/components/glass-card';
import { ScreenHeader } from '@/components/screen-header';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Radius, Spacing, cardShadow } from '@/constants/theme';
import { useGradients, useTheme } from '@/hooks/use-theme';
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
  const gradients = useGradients();
  const profile = useSessionStore((s) => s.profile);

  return (
    <ThemedView style={styles.container}>
      <ScreenHeader title="More" />
      <Animated.View entering={FadeInDown.duration(300)} style={styles.profileCard}>
        <LinearGradient colors={gradients.primary} style={[styles.avatar, cardShadow(theme.shadowColor, 0.3)]}>
          <ThemedText themeColor="primaryText" type="smallBold">
            {(profile?.displayName ?? '?').slice(0, 1).toUpperCase()}
          </ThemedText>
        </LinearGradient>
        <View>
          <ThemedText type="smallBold">{profile?.displayName}</ThemedText>
          <ThemedText type="small" themeColor="textSecondary">
            Personal workspace
          </ThemedText>
        </View>
      </Animated.View>

      <View style={styles.rows}>
        {ROWS.map((row, index) => (
          <Animated.View key={row.href} entering={FadeInDown.delay(80 + index * 40).duration(280)}>
            <AnimatedPressable onPress={() => router.push(row.href)} scaleTo={0.98}>
              <GlassCard style={styles.row} radius={Radius.large}>
                <LinearGradient colors={gradients.hero} style={styles.rowIcon}>
                  <Ionicons name={row.icon} size={20} color={theme.primary} />
                </LinearGradient>
                <View style={styles.rowText}>
                  <ThemedText type="smallBold">{row.title}</ThemedText>
                  <ThemedText type="small" themeColor="textSecondary">
                    {row.subtitle}
                  </ThemedText>
                </View>
                <Ionicons name="chevron-forward" size={18} color={theme.textSecondary} />
              </GlassCard>
            </AnimatedPressable>
          </Animated.View>
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
  avatar: { width: 48, height: 48, borderRadius: Radius.pill, alignItems: 'center', justifyContent: 'center' },
  rows: { paddingHorizontal: Spacing.three, gap: Spacing.two },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.three,
    padding: Spacing.three,
  },
  rowIcon: {
    width: 38,
    height: 38,
    borderRadius: Radius.medium,
    alignItems: 'center',
    justifyContent: 'center',
  },
  rowText: { flex: 1, gap: 2 },
});
