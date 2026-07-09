import { Ionicons } from '@expo/vector-icons';
import { LinearGradient } from 'expo-linear-gradient';
import { useEffect, useState } from 'react';
import { Pressable, StyleSheet } from 'react-native';
import Animated, {
  cancelAnimation,
  useAnimatedStyle,
  useSharedValue,
  withRepeat,
  withTiming,
} from 'react-native-reanimated';

import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Radius, Spacing, cardShadow } from '@/constants/theme';
import { useGradients, useTheme } from '@/hooks/use-theme';

export default function VoiceScreen() {
  const theme = useTheme();
  const gradients = useGradients();
  const [pressed, setPressed] = useState(false);
  const pulse = useSharedValue(1);

  useEffect(() => {
    if (pressed) {
      pulse.value = withRepeat(withTiming(1.35, { duration: 700 }), -1, true);
    } else {
      cancelAnimation(pulse);
      pulse.value = withTiming(1, { duration: 200 });
    }
  }, [pressed, pulse]);

  const pulseStyle = useAnimatedStyle(() => ({
    transform: [{ scale: pulse.value }],
    opacity: pressed ? 0.35 : 0,
  }));

  return (
    <ThemedView style={styles.container}>
      <Ionicons name="mic-circle-outline" size={64} color={theme.textSecondary} />
      <ThemedText type="subtitle" style={styles.title}>
        Voice preview
      </ThemedText>
      <ThemedText type="small" themeColor="textSecondary" style={styles.body}>
        Nova's voice mode is push-to-talk and turn-based: hold the button, speak, release, and Nova
        replies with audio. It needs a backend to transcribe your clip, generate a reply, and
        synthesize speech — none of that is connected in this local-only build yet, so this screen is
        a preview of the interaction only.
      </ThemedText>

      <ThemedView style={styles.micWrap}>
        <Animated.View style={[styles.pulseRing, { backgroundColor: theme.primary }, pulseStyle]} />
        <Pressable onPressIn={() => setPressed(true)} onPressOut={() => setPressed(false)}>
          <LinearGradient
            colors={pressed ? [theme.danger, theme.accent] : gradients.primary}
            style={[styles.micButton, cardShadow(theme.shadowColor, 0.35)]}>
            <Ionicons name="mic" size={30} color={theme.primaryText} />
          </LinearGradient>
        </Pressable>
      </ThemedView>
      <ThemedText type="small" themeColor="textSecondary">
        {pressed ? 'Listening… (preview only)' : 'Hold to talk'}
      </ThemedText>
    </ThemedView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, alignItems: 'center', justifyContent: 'center', padding: Spacing.five, gap: Spacing.three },
  title: { textAlign: 'center' },
  body: { textAlign: 'center' },
  micWrap: { alignItems: 'center', justifyContent: 'center', marginTop: Spacing.three, width: 100, height: 100 },
  pulseRing: { position: 'absolute', width: 84, height: 84, borderRadius: 42 },
  micButton: {
    width: 80,
    height: 80,
    borderRadius: Radius.pill,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
