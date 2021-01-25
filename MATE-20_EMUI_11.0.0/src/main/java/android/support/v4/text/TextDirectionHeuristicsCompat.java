package android.support.v4.text;

import java.nio.CharBuffer;
import java.util.Locale;

public final class TextDirectionHeuristicsCompat {
    public static final TextDirectionHeuristicCompat ANYRTL_LTR = new TextDirectionHeuristicInternal(AnyStrong.INSTANCE_RTL, false);
    public static final TextDirectionHeuristicCompat FIRSTSTRONG_LTR = new TextDirectionHeuristicInternal(FirstStrong.INSTANCE, false);
    public static final TextDirectionHeuristicCompat FIRSTSTRONG_RTL = new TextDirectionHeuristicInternal(FirstStrong.INSTANCE, true);
    public static final TextDirectionHeuristicCompat LOCALE = TextDirectionHeuristicLocale.INSTANCE;
    public static final TextDirectionHeuristicCompat LTR = new TextDirectionHeuristicInternal(null, false);
    public static final TextDirectionHeuristicCompat RTL = new TextDirectionHeuristicInternal(null, true);
    private static final int STATE_FALSE = 1;
    private static final int STATE_TRUE = 0;
    private static final int STATE_UNKNOWN = 2;

    /* access modifiers changed from: private */
    public interface TextDirectionAlgorithm {
        int checkRtl(CharSequence charSequence, int i, int i2);
    }

    static int isRtlText(int directionality) {
        switch (directionality) {
            case 0:
                return 1;
            case 1:
            case 2:
                return 0;
            default:
                return 2;
        }
    }

    static int isRtlTextOrFormat(int directionality) {
        switch (directionality) {
            case 0:
                return 1;
            case 1:
            case 2:
                return 0;
            default:
                switch (directionality) {
                    case 14:
                    case 15:
                        return 1;
                    case 16:
                    case 17:
                        return 0;
                    default:
                        return 2;
                }
        }
    }

    private static abstract class TextDirectionHeuristicImpl implements TextDirectionHeuristicCompat {
        private final TextDirectionAlgorithm mAlgorithm;

        /* access modifiers changed from: protected */
        public abstract boolean defaultIsRtl();

        TextDirectionHeuristicImpl(TextDirectionAlgorithm algorithm) {
            this.mAlgorithm = algorithm;
        }

        @Override // android.support.v4.text.TextDirectionHeuristicCompat
        public boolean isRtl(char[] array, int start, int count) {
            return isRtl(CharBuffer.wrap(array), start, count);
        }

        @Override // android.support.v4.text.TextDirectionHeuristicCompat
        public boolean isRtl(CharSequence cs, int start, int count) {
            if (cs == null || start < 0 || count < 0 || cs.length() - count < start) {
                throw new IllegalArgumentException();
            } else if (this.mAlgorithm == null) {
                return defaultIsRtl();
            } else {
                return doCheck(cs, start, count);
            }
        }

        private boolean doCheck(CharSequence cs, int start, int count) {
            switch (this.mAlgorithm.checkRtl(cs, start, count)) {
                case 0:
                    return true;
                case 1:
                    return false;
                default:
                    return defaultIsRtl();
            }
        }
    }

    private static class TextDirectionHeuristicInternal extends TextDirectionHeuristicImpl {
        private final boolean mDefaultIsRtl;

        TextDirectionHeuristicInternal(TextDirectionAlgorithm algorithm, boolean defaultIsRtl) {
            super(algorithm);
            this.mDefaultIsRtl = defaultIsRtl;
        }

        /* access modifiers changed from: protected */
        @Override // android.support.v4.text.TextDirectionHeuristicsCompat.TextDirectionHeuristicImpl
        public boolean defaultIsRtl() {
            return this.mDefaultIsRtl;
        }
    }

    private static class FirstStrong implements TextDirectionAlgorithm {
        static final FirstStrong INSTANCE = new FirstStrong();

        @Override // android.support.v4.text.TextDirectionHeuristicsCompat.TextDirectionAlgorithm
        public int checkRtl(CharSequence cs, int start, int count) {
            int result = 2;
            int e = start + count;
            for (int i = start; i < e && result == 2; i++) {
                result = TextDirectionHeuristicsCompat.isRtlTextOrFormat(Character.getDirectionality(cs.charAt(i)));
            }
            return result;
        }

        private FirstStrong() {
        }
    }

    private static class AnyStrong implements TextDirectionAlgorithm {
        static final AnyStrong INSTANCE_LTR = new AnyStrong(false);
        static final AnyStrong INSTANCE_RTL = new AnyStrong(true);
        private final boolean mLookForRtl;

        @Override // android.support.v4.text.TextDirectionHeuristicsCompat.TextDirectionAlgorithm
        public int checkRtl(CharSequence cs, int start, int count) {
            boolean haveUnlookedFor = false;
            int e = start + count;
            for (int i = start; i < e; i++) {
                switch (TextDirectionHeuristicsCompat.isRtlText(Character.getDirectionality(cs.charAt(i)))) {
                    case 0:
                        if (!this.mLookForRtl) {
                            haveUnlookedFor = true;
                            break;
                        } else {
                            return 0;
                        }
                    case 1:
                        if (this.mLookForRtl) {
                            haveUnlookedFor = true;
                            break;
                        } else {
                            return 1;
                        }
                }
            }
            if (haveUnlookedFor) {
                return this.mLookForRtl ? 1 : 0;
            }
            return 2;
        }

        private AnyStrong(boolean lookForRtl) {
            this.mLookForRtl = lookForRtl;
        }
    }

    private static class TextDirectionHeuristicLocale extends TextDirectionHeuristicImpl {
        static final TextDirectionHeuristicLocale INSTANCE = new TextDirectionHeuristicLocale();

        TextDirectionHeuristicLocale() {
            super(null);
        }

        /* access modifiers changed from: protected */
        @Override // android.support.v4.text.TextDirectionHeuristicsCompat.TextDirectionHeuristicImpl
        public boolean defaultIsRtl() {
            return TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == 1;
        }
    }

    private TextDirectionHeuristicsCompat() {
    }
}
