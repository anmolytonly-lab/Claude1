import { Ionicons } from '@expo/vector-icons';
import { LinearGradient } from 'expo-linear-gradient';
import { StyleSheet, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { AnimatedPressable } from '@/components/animated-pressable';
import { ThemedText } from '@/components/themed-text';
import { Radius, Spacing, cardShadow } from '@/constants/theme';
import { useGradients, useTheme } from '@/hooks/use-theme';

interface ScreenHeaderProps {
  title: string;
  subtitle?: string;
  onAddPress?: () => void;
  addLabel?: string;
}

export function ScreenHeader({ title, subtitle, onAddPress, addLabel }: ScreenHeaderProps) {
  const theme = useTheme();
  const gradients = useGradients();
  const insets = useSafeAreaInsets();

  return (
    <View style={[styles.container, { paddingTop: insets.top + Spacing.two, borderColor: theme.border }]}>
      <View style={styles.titleBlock}>
        <ThemedText type="title" style={styles.title}>
          {title}
        </ThemedText>
        {subtitle && (
          <ThemedText type="small" themeColor="textSecondary">
            {subtitle}
          </ThemedText>
        )}
      </View>
      {onAddPress && (
        <AnimatedPressable
          onPress={onAddPress}
          hitSlop={12}
          style={[styles.addButton, cardShadow(theme.shadowColor, 0.3)]}
          accessibilityLabel={addLabel ?? `Add to ${title}`}>
          <LinearGradient
            colors={gradients.primary}
            start={{ x: 0, y: 0 }}
            end={{ x: 1, y: 1 }}
            style={styles.addButtonGradient}>
            <Ionicons name="add" size={22} color={theme.primaryText} />
          </LinearGradient>
        </AnimatedPressable>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    alignItems: 'flex-end',
    justifyContent: 'space-between',
    paddingHorizontal: Spacing.four,
    paddingBottom: Spacing.three,
    borderBottomWidth: StyleSheet.hairlineWidth,
  },
  titleBlock: { gap: 2, flexShrink: 1 },
  title: { fontSize: 32, lineHeight: 36 },
  addButton: { borderRadius: Radius.pill },
  addButtonGradient: {
    width: 40,
    height: 40,
    borderRadius: Radius.pill,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
