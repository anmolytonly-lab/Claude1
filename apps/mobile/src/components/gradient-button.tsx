import { LinearGradient } from 'expo-linear-gradient';
import type { ReactNode } from 'react';
import { ActivityIndicator, StyleSheet, type ViewStyle } from 'react-native';

import { AnimatedPressable } from '@/components/animated-pressable';
import { ThemedText } from '@/components/themed-text';
import { Radius, Spacing, cardShadow } from '@/constants/theme';
import { useGradients, useTheme } from '@/hooks/use-theme';

interface GradientButtonProps {
  label: string;
  onPress: () => void;
  disabled?: boolean;
  loading?: boolean;
  icon?: ReactNode;
  variant?: 'primary' | 'accent';
  style?: ViewStyle;
}

export function GradientButton({
  label,
  onPress,
  disabled,
  loading,
  icon,
  variant = 'primary',
  style,
}: GradientButtonProps) {
  const theme = useTheme();
  const gradients = useGradients();
  const colors = variant === 'accent' ? gradients.accent : gradients.primary;
  const isDisabled = disabled || loading;

  return (
    <AnimatedPressable
      onPress={onPress}
      disabled={isDisabled}
      style={[{ opacity: isDisabled ? 0.55 : 1 }, cardShadow(theme.shadowColor, 0.28), style]}>
      <LinearGradient colors={colors} start={{ x: 0, y: 0 }} end={{ x: 1, y: 1 }} style={styles.gradient}>
        {loading ? (
          <ActivityIndicator color={theme.primaryText} />
        ) : (
          <>
            {icon}
            <ThemedText type="smallBold" themeColor="primaryText">
              {label}
            </ThemedText>
          </>
        )}
      </LinearGradient>
    </AnimatedPressable>
  );
}

const styles = StyleSheet.create({
  gradient: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: Spacing.one + 2,
    borderRadius: Radius.medium,
    paddingVertical: Spacing.three,
    paddingHorizontal: Spacing.four,
  },
});
