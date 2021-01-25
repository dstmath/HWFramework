package android.text;

import android.content.res.Configuration;
import java.nio.CharBuffer;
import java.util.Locale;

public class TextDirectionHeuristics {
    public static final TextDirectionHeuristic ANYRTL_LTR = new TextDirectionHeuristicInternal(AnyStrong.INSTANCE_RTL, false);
    public static final TextDirectionHeuristic FIRSTSTRONG_LTR = new TextDirectionHeuristicInternal(FirstStrong.INSTANCE, false);
    public static final TextDirectionHeuristic FIRSTSTRONG_RTL = new TextDirectionHeuristicInternal(FirstStrong.INSTANCE, true);
    public static final TextDirectionHeuristic LOCALE = TextDirectionHeuristicLocale.INSTANCE;
    public static final TextDirectionHeuristic LTR = new TextDirectionHeuristicInternal(null, false);
    public static final TextDirectionHeuristic RTL = new TextDirectionHeuristicInternal(null, true);
    private static final int STATE_FALSE = 1;
    private static final int STATE_TRUE = 0;
    private static final int STATE_UNKNOWN = 2;

    /* access modifiers changed from: private */
    public interface TextDirectionAlgorithm {
        int checkRtl(CharSequence charSequence, int i, int i2);
    }

    /* access modifiers changed from: private */
    public static int isRtlCodePoint(int codePoint) {
        byte directionality = Character.getDirectionality(codePoint);
        if (directionality != -1) {
            if (directionality != 0) {
                return (directionality == 1 || directionality == 2) ? 0 : 2;
            }
            return 1;
        } else if ((1424 > codePoint || codePoint > 2303) && ((64285 > codePoint || codePoint > 64975) && ((65008 > codePoint || codePoint > 65023) && ((65136 > codePoint || codePoint > 65279) && ((67584 > codePoint || codePoint > 69631) && (124928 > codePoint || codePoint > 126975)))))) {
            return ((8293 > codePoint || codePoint > 8297) && (65520 > codePoint || codePoint > 65528) && ((917504 > codePoint || codePoint > 921599) && ((64976 > codePoint || codePoint > 65007) && (codePoint & Configuration.DENSITY_DPI_ANY) != 65534 && ((8352 > codePoint || codePoint > 8399) && (55296 > codePoint || codePoint > 57343))))) ? 1 : 2;
        } else {
            return 0;
        }
    }

    private static abstract class TextDirectionHeuristicImpl implements TextDirectionHeuristic {
        private final TextDirectionAlgorithm mAlgorithm;

        /* access modifiers changed from: protected */
        public abstract boolean defaultIsRtl();

        public TextDirectionHeuristicImpl(TextDirectionAlgorithm algorithm) {
            this.mAlgorithm = algorithm;
        }

        @Override // android.text.TextDirectionHeuristic
        public boolean isRtl(char[] array, int start, int count) {
            return isRtl(CharBuffer.wrap(array), start, count);
        }

        @Override // android.text.TextDirectionHeuristic
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
            int checkRtl = this.mAlgorithm.checkRtl(cs, start, count);
            if (checkRtl == 0) {
                return true;
            }
            if (checkRtl != 1) {
                return defaultIsRtl();
            }
            return false;
        }
    }

    private static class TextDirectionHeuristicInternal extends TextDirectionHeuristicImpl {
        private final boolean mDefaultIsRtl;

        private TextDirectionHeuristicInternal(TextDirectionAlgorithm algorithm, boolean defaultIsRtl) {
            super(algorithm);
            this.mDefaultIsRtl = defaultIsRtl;
        }

        /* access modifiers changed from: protected */
        @Override // android.text.TextDirectionHeuristics.TextDirectionHeuristicImpl
        public boolean defaultIsRtl() {
            return this.mDefaultIsRtl;
        }
    }

    private static class FirstStrong implements TextDirectionAlgorithm {
        public static final FirstStrong INSTANCE = new FirstStrong();

        @Override // android.text.TextDirectionHeuristics.TextDirectionAlgorithm
        public int checkRtl(CharSequence cs, int start, int count) {
            int result = 2;
            int openIsolateCount = 0;
            int i = start;
            int end = start + count;
            while (i < end && result == 2) {
                int cp = Character.codePointAt(cs, i);
                if (8294 <= cp && cp <= 8296) {
                    openIsolateCount++;
                } else if (cp == 8297) {
                    if (openIsolateCount > 0) {
                        openIsolateCount--;
                    }
                } else if (openIsolateCount == 0) {
                    result = TextDirectionHeuristics.isRtlCodePoint(cp);
                }
                i += Character.charCount(cp);
            }
            return result;
        }

        private FirstStrong() {
        }
    }

    private static class AnyStrong implements TextDirectionAlgorithm {
        public static final AnyStrong INSTANCE_LTR = new AnyStrong(false);
        public static final AnyStrong INSTANCE_RTL = new AnyStrong(true);
        private final boolean mLookForRtl;

        @Override // android.text.TextDirectionHeuristics.TextDirectionAlgorithm
        public int checkRtl(CharSequence cs, int start, int count) {
            boolean haveUnlookedFor = false;
            int openIsolateCount = 0;
            int i = start;
            int end = start + count;
            while (i < end) {
                int cp = Character.codePointAt(cs, i);
                if (8294 <= cp && cp <= 8296) {
                    openIsolateCount++;
                } else if (cp == 8297) {
                    if (openIsolateCount > 0) {
                        openIsolateCount--;
                    }
                } else if (openIsolateCount != 0) {
                    continue;
                } else {
                    int isRtlCodePoint = TextDirectionHeuristics.isRtlCodePoint(cp);
                    if (isRtlCodePoint != 0) {
                        if (isRtlCodePoint != 1) {
                            continue;
                        } else if (!this.mLookForRtl) {
                            return 1;
                        } else {
                            haveUnlookedFor = true;
                        }
                    } else if (this.mLookForRtl) {
                        return 0;
                    } else {
                        haveUnlookedFor = true;
                    }
                }
                i += Character.charCount(cp);
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
        public static final TextDirectionHeuristicLocale INSTANCE = new TextDirectionHeuristicLocale();

        public TextDirectionHeuristicLocale() {
            super(null);
        }

        /* access modifiers changed from: protected */
        @Override // android.text.TextDirectionHeuristics.TextDirectionHeuristicImpl
        public boolean defaultIsRtl() {
            return TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == 1;
        }
    }
}
