import { BlurView } from 'expo-blur';
import { StyleSheet, View, type ViewProps, type ViewStyle } from 'react-native';

import { Radius, cardShadow } from '@/constants/theme';
import { useActiveScheme, useTheme } from '@/hooks/use-theme';

interface GlassCardProps extends ViewProps {
  radius?: number;
  intensity?: number;
  noShadow?: boolean;
}

// Properties that determine this card's box size/position within its
// parent's layout. These must live on the outermost wrapper, not just the
// inner BlurView - percentage values like `maxWidth: '82%'` can only
// resolve against a parent with a definite size, and the inner BlurView's
// "parent" for that purpose is the wrapper, which has no intrinsic size of
// its own unless these are copied onto it too.
const SIZING_KEYS = [
  'width',
  'minWidth',
  'maxWidth',
  'height',
  'minHeight',
  'maxHeight',
  'margin',
  'marginTop',
  'marginBottom',
  'marginLeft',
  'marginRight',
  'marginHorizontal',
  'marginVertical',
  'alignSelf',
  'flex',
  'flexBasis',
  'flexGrow',
  'flexShrink',
] as const satisfies readonly (keyof ViewStyle)[];

function extractSizing(style: ViewProps['style']): ViewStyle {
  const flat = StyleSheet.flatten(style) ?? {};
  const sizing: ViewStyle = {};
  for (const key of SIZING_KEYS) {
    if (flat[key] !== undefined) {
      // @ts-expect-error - key is one of ViewStyle's own keys, value copied as-is
      sizing[key] = flat[key];
    }
  }
  return sizing;
}

// Frosted-glass surface used for cards, headers, and modals throughout the
// app. Three layers, each for one reason:
//  1. Sizing/shadow wrapper - shadows and `overflow: hidden` fight each
//     other on iOS (the clip silently eats the shadow), so this layer casts
//     the shadow without clipping anything.
//  2. Clip wrapper - actually clips content to the rounded rect.
//  3. BlurView - computes its own backgroundColor/backdrop-filter from
//     tint+intensity (notably overriding it on web), so the theme's brand
//     tint is layered on top as a separate overlay rather than fighting it
//     for the same style property - this keeps native and web consistent.
export function GlassCard({ style, radius = Radius.large, intensity = 40, noShadow, children, ...rest }: GlassCardProps) {
  const theme = useTheme();
  const scheme = useActiveScheme();

  return (
    <View
      style={[
        { borderRadius: radius },
        extractSizing(style),
        !noShadow && cardShadow(theme.shadowColor, scheme === 'dark' ? 0.35 : 0.1),
      ]}>
      <View style={{ borderRadius: radius, overflow: 'hidden' }}>
        <BlurView
          intensity={intensity}
          tint={scheme === 'dark' ? 'dark' : 'light'}
          style={[styles.blur, { borderColor: theme.glassBorder, borderRadius: radius }, style]}
          {...rest}>
          <View
            style={[StyleSheet.absoluteFill, { backgroundColor: theme.glassSurface, pointerEvents: 'none' }]}
          />
          {children}
        </BlurView>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  blur: {
    borderWidth: StyleSheet.hairlineWidth,
  },
});
