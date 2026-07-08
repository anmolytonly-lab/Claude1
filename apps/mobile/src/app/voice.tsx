import { Ionicons } from '@expo/vector-icons';
import { useState } from 'react';
import { Pressable, StyleSheet, View } from 'react-native';

import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Spacing } from '@/constants/theme';
import { useTheme } from '@/hooks/use-theme';

export default function VoiceScreen() {
  const theme = useTheme();
  const [pressed, setPressed] = useState(false);

  return (
    <ThemedView style={styles.container}>
      <Ionicons name="mic-circle-outline" size={72} color={theme.textSecondary} />
      <ThemedText type="subtitle" style={styles.title}>
        Voice preview
      </ThemedText>
      <ThemedText type="small" themeColor="textSecondary" style={styles.body}>
        Nova's voice mode is push-to-talk and turn-based: hold the button, speak, release, and Nova
        replies with audio. It needs a backend to transcribe your clip, generate a reply, and
        synthesize speech — none of that is connected in this local-only build yet, so this screen is
        a preview of the interaction only.
      </ThemedText>

      <Pressable
        onPressIn={() => setPressed(true)}
        onPressOut={() => setPressed(false)}
        style={[
          styles.micButton,
          { backgroundColor: pressed ? theme.danger : theme.primary },
        ]}>
        <Ionicons name="mic" size={32} color={theme.primaryText} />
      </Pressable>
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
  micButton: {
    width: 84,
    height: 84,
    borderRadius: 42,
    alignItems: 'center',
    justifyContent: 'center',
    marginTop: Spacing.three,
  },
});
