import { Ionicons } from '@expo/vector-icons';
import { Pressable, StyleSheet, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { ThemedText } from '@/components/themed-text';
import { Spacing } from '@/constants/theme';
import { useTheme } from '@/hooks/use-theme';

interface ScreenHeaderProps {
  title: string;
  onAddPress?: () => void;
  addLabel?: string;
}

export function ScreenHeader({ title, onAddPress, addLabel }: ScreenHeaderProps) {
  const theme = useTheme();
  const insets = useSafeAreaInsets();

  return (
    <View style={[styles.container, { paddingTop: insets.top + Spacing.two, borderColor: theme.border }]}>
      <ThemedText type="subtitle">{title}</ThemedText>
      {onAddPress && (
        <Pressable
          onPress={onAddPress}
          hitSlop={12}
          style={[styles.addButton, { backgroundColor: theme.primary }]}
          accessibilityLabel={addLabel ?? `Add to ${title}`}>
          <Ionicons name="add" size={20} color={theme.primaryText} />
        </Pressable>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: Spacing.four,
    paddingBottom: Spacing.three,
    borderBottomWidth: StyleSheet.hairlineWidth,
  },
  addButton: {
    width: 34,
    height: 34,
    borderRadius: 17,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
