package android.text;

import com.android.internal.telephony.gsm.SmsCbConstants;
import java.nio.CharBuffer;
import java.util.Locale;

public class TextDirectionHeuristics {
    public static final TextDirectionHeuristic ANYRTL_LTR = new TextDirectionHeuristicInternal(AnyStrong.INSTANCE_RTL, false, null);
    public static final TextDirectionHeuristic FIRSTSTRONG_LTR = new TextDirectionHeuristicInternal(FirstStrong.INSTANCE, false, null);
    public static final TextDirectionHeuristic FIRSTSTRONG_RTL = new TextDirectionHeuristicInternal(FirstStrong.INSTANCE, true, null);
    public static final TextDirectionHeuristic LOCALE = TextDirectionHeuristicLocale.INSTANCE;
    public static final TextDirectionHeuristic LTR = new TextDirectionHeuristicInternal(null, false, null);
    public static final TextDirectionHeuristic RTL = new TextDirectionHeuristicInternal(null, true, null);
    private static final int STATE_FALSE = 1;
    private static final int STATE_TRUE = 0;
    private static final int STATE_UNKNOWN = 2;

    private interface TextDirectionAlgorithm {
        int checkRtl(CharSequence charSequence, int i, int i2);
    }

    private static class AnyStrong implements TextDirectionAlgorithm {
        public static final AnyStrong INSTANCE_LTR = new AnyStrong(false);
        public static final AnyStrong INSTANCE_RTL = new AnyStrong(true);
        private final boolean mLookForRtl;

        public int checkRtl(CharSequence cs, int start, int count) {
            int i = 1;
            boolean haveUnlookedFor = false;
            int openIsolateCount = 0;
            int i2 = start;
            int end = start + count;
            while (i2 < end) {
                int cp = Character.codePointAt(cs, i2);
                if (8294 <= cp && cp <= 8296) {
                    openIsolateCount++;
                } else if (cp == 8297) {
                    if (openIsolateCount > 0) {
                        openIsolateCount--;
                    }
                } else if (openIsolateCount == 0) {
                    switch (TextDirectionHeuristics.isRtlCodePoint(cp)) {
                        case 0:
                            if (!this.mLookForRtl) {
                                haveUnlookedFor = true;
                                break;
                            }
                            return 0;
                        case 1:
                            if (this.mLookForRtl) {
                                haveUnlookedFor = true;
                                break;
                            }
                            return 1;
                        default:
                            continue;
                    }
                } else {
                    continue;
                }
                i2 += Character.charCount(cp);
            }
            if (!haveUnlookedFor) {
                return 2;
            }
            if (!this.mLookForRtl) {
                i = 0;
            }
            return i;
        }

        private AnyStrong(boolean lookForRtl) {
            this.mLookForRtl = lookForRtl;
        }
    }

    private static class FirstStrong implements TextDirectionAlgorithm {
        public static final FirstStrong INSTANCE = new FirstStrong();

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

    private static abstract class TextDirectionHeuristicImpl implements TextDirectionHeuristic {
        private final TextDirectionAlgorithm mAlgorithm;

        protected abstract boolean defaultIsRtl();

        public TextDirectionHeuristicImpl(TextDirectionAlgorithm algorithm) {
            this.mAlgorithm = algorithm;
        }

        public boolean isRtl(char[] array, int start, int count) {
            return isRtl(CharBuffer.wrap(array), start, count);
        }

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

        /* synthetic */ TextDirectionHeuristicInternal(TextDirectionAlgorithm algorithm, boolean defaultIsRtl, TextDirectionHeuristicInternal -this2) {
            this(algorithm, defaultIsRtl);
        }

        private TextDirectionHeuristicInternal(TextDirectionAlgorithm algorithm, boolean defaultIsRtl) {
            super(algorithm);
            this.mDefaultIsRtl = defaultIsRtl;
        }

        protected boolean defaultIsRtl() {
            return this.mDefaultIsRtl;
        }
    }

    private static class TextDirectionHeuristicLocale extends TextDirectionHeuristicImpl {
        public static final TextDirectionHeuristicLocale INSTANCE = new TextDirectionHeuristicLocale();

        public TextDirectionHeuristicLocale() {
            super(null);
        }

        protected boolean defaultIsRtl() {
            if (TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == 1) {
                return true;
            }
            return false;
        }
    }

    private static int isRtlCodePoint(int codePoint) {
        switch (Character.getDirectionality(codePoint)) {
            case (byte) -1:
                if ((1424 > codePoint || codePoint > 2303) && ((64285 > codePoint || codePoint > 64975) && ((65008 > codePoint || codePoint > 65023) && ((65136 > codePoint || codePoint > 65279) && ((67584 > codePoint || codePoint > 69631) && (124928 > codePoint || codePoint > 126975)))))) {
                    return ((8293 > codePoint || codePoint > 8297) && ((65520 > codePoint || codePoint > SmsCbConstants.MESSAGE_ID_ETWS_TYPE_MASK) && ((917504 > codePoint || codePoint > 921599) && ((64976 > codePoint || codePoint > 65007) && (codePoint & 65534) != 65534 && ((8352 > codePoint || codePoint > 8399) && (55296 > codePoint || codePoint > 57343)))))) ? 1 : 2;
                } else {
                    return 0;
                }
            case (byte) 0:
                return 1;
            case (byte) 1:
            case (byte) 2:
                return 0;
            default:
                return 2;
        }
    }
}
