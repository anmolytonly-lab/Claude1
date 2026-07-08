import { Link, router } from 'expo-router';
import { useState } from 'react';
import { ActivityIndicator, KeyboardAvoidingView, Platform, Pressable, StyleSheet, TextInput } from 'react-native';

import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Spacing } from '@/constants/theme';
import { useTheme } from '@/hooks/use-theme';
import { useSessionStore } from '@/lib/session-store';

export default function SignUpScreen() {
  const theme = useTheme();
  const signUp = useSessionStore((s) => s.signUp);
  const [displayName, setDisplayName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const onSubmit = async () => {
    setError(null);
    if (!displayName.trim() || !email.trim() || password.length < 4) {
      setError('Please fill in your name, email, and a password of at least 4 characters.');
      return;
    }
    setSubmitting(true);
    try {
      await signUp(email.trim(), password, displayName.trim());
      router.replace('/(tabs)');
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Something went wrong.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <KeyboardAvoidingView
      style={{ flex: 1 }}
      behavior={Platform.select({ ios: 'padding', default: undefined })}>
      <ThemedView style={styles.container}>
        <ThemedText type="title" style={styles.title}>
          Create account
        </ThemedText>
        <ThemedText type="subtitle" style={styles.subtitle} themeColor="textSecondary">
          Set up your personal Nova
        </ThemedText>

        <ThemedView type="backgroundElement" style={styles.card}>
          <TextInput
            placeholder="Name"
            placeholderTextColor={theme.textSecondary}
            value={displayName}
            onChangeText={setDisplayName}
            style={[styles.input, { color: theme.text, borderColor: theme.border }]}
          />
          <TextInput
            placeholder="Email"
            placeholderTextColor={theme.textSecondary}
            autoCapitalize="none"
            keyboardType="email-address"
            value={email}
            onChangeText={setEmail}
            style={[styles.input, { color: theme.text, borderColor: theme.border }]}
          />
          <TextInput
            placeholder="Password"
            placeholderTextColor={theme.textSecondary}
            secureTextEntry
            value={password}
            onChangeText={setPassword}
            style={[styles.input, { color: theme.text, borderColor: theme.border }]}
          />

          {error && (
            <ThemedText themeColor="danger" type="small">
              {error}
            </ThemedText>
          )}

          <Pressable
            onPress={onSubmit}
            disabled={submitting}
            style={[styles.button, { backgroundColor: theme.primary, opacity: submitting ? 0.7 : 1 }]}>
            {submitting ? (
              <ActivityIndicator color={theme.primaryText} />
            ) : (
              <ThemedText type="smallBold" themeColor="primaryText">
                Create account
              </ThemedText>
            )}
          </Pressable>
        </ThemedView>

        <Link href="/(auth)/sign-in" style={styles.link}>
          <ThemedText type="link" themeColor="primary">
            Already have an account? Sign in
          </ThemedText>
        </Link>
      </ThemedView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: Spacing.four,
    gap: Spacing.two,
  },
  title: { textAlign: 'center' },
  subtitle: { textAlign: 'center', marginBottom: Spacing.four },
  card: {
    width: '100%',
    maxWidth: 420,
    borderRadius: Spacing.four,
    padding: Spacing.four,
    gap: Spacing.three,
  },
  input: {
    borderWidth: StyleSheet.hairlineWidth,
    borderRadius: Spacing.two,
    paddingHorizontal: Spacing.three,
    paddingVertical: Spacing.two + 2,
    fontSize: 16,
  },
  button: {
    borderRadius: Spacing.two,
    paddingVertical: Spacing.three,
    alignItems: 'center',
    justifyContent: 'center',
  },
  link: {
    marginTop: Spacing.three,
  },
});
