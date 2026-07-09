import { Ionicons } from '@expo/vector-icons';
import { useQuery } from '@tanstack/react-query';
import { LinearGradient } from 'expo-linear-gradient';
import { Alert, Pressable, StyleSheet, View } from 'react-native';
import Animated, { FadeInDown } from 'react-native-reanimated';

import { GlassCard } from '@/components/glass-card';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Radius, Spacing } from '@/constants/theme';
import { useGradients, useTheme } from '@/hooks/use-theme';
import { listOrganizations } from '@/lib/mock-db';
import { useSessionStore } from '@/lib/session-store';

export default function OrganizationsScreen() {
  const theme = useTheme();
  const gradients = useGradients();
  const profile = useSessionStore((s) => s.profile);
  const { data: organizations = [] } = useQuery({ queryKey: ['organizations'], queryFn: listOrganizations });

  const notifyFuturePhase = () =>
    Alert.alert(
      'Not built yet',
      'Shared organizations (inviting teammates, org-scoped Tasks/Notes/CRM) are a later build phase. Right now every account just has its own personal workspace.'
    );

  return (
    <ThemedView style={styles.container}>
      <ThemedText type="small" themeColor="textSecondary" style={styles.intro}>
        Every Nova account starts with a personal workspace. Shared organizations with teammates come
        in a later build phase.
      </ThemedText>

      <View style={styles.list}>
        {organizations.map((org, index) => {
          const active = org.id === profile?.activeOrganizationId;
          return (
            <Animated.View key={org.id} entering={FadeInDown.delay(index * 60).duration(280)}>
              <GlassCard style={styles.row} radius={Radius.large}>
                <LinearGradient colors={gradients.primary} style={styles.orgIcon}>
                  <Ionicons name={org.isPersonal ? 'person' : 'business'} size={18} color={theme.primaryText} />
                </LinearGradient>
                <View style={styles.rowText}>
                  <ThemedText type="smallBold">{org.name}</ThemedText>
                  <ThemedText type="small" themeColor="textSecondary">
                    {org.role}
                  </ThemedText>
                </View>
                {active && <Ionicons name="checkmark-circle" size={20} color={theme.success} />}
              </GlassCard>
            </Animated.View>
          );
        })}
      </View>

      <Pressable onPress={notifyFuturePhase} style={[styles.createButton, { borderColor: theme.border }]}>
        <Ionicons name="add" size={18} color={theme.text} />
        <ThemedText type="smallBold">Create organization</ThemedText>
      </Pressable>
    </ThemedView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: Spacing.four, gap: Spacing.three },
  intro: {},
  list: { gap: Spacing.two },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.three,
    padding: Spacing.three,
  },
  orgIcon: {
    width: 36,
    height: 36,
    borderRadius: Radius.medium,
    alignItems: 'center',
    justifyContent: 'center',
  },
  rowText: { flex: 1, gap: 2 },
  createButton: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: Spacing.one,
    borderWidth: StyleSheet.hairlineWidth,
    borderRadius: Radius.medium,
    paddingVertical: Spacing.three,
  },
});
