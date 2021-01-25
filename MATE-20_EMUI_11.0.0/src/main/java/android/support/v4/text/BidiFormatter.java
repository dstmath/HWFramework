package android.support.v4.text;

import android.text.SpannableStringBuilder;
import java.util.Locale;

public final class BidiFormatter {
    private static final int DEFAULT_FLAGS = 2;
    static final BidiFormatter DEFAULT_LTR_INSTANCE = new BidiFormatter(false, 2, DEFAULT_TEXT_DIRECTION_HEURISTIC);
    static final BidiFormatter DEFAULT_RTL_INSTANCE = new BidiFormatter(true, 2, DEFAULT_TEXT_DIRECTION_HEURISTIC);
    static final TextDirectionHeuristicCompat DEFAULT_TEXT_DIRECTION_HEURISTIC = TextDirectionHeuristicsCompat.FIRSTSTRONG_LTR;
    private static final int DIR_LTR = -1;
    private static final int DIR_RTL = 1;
    private static final int DIR_UNKNOWN = 0;
    private static final String EMPTY_STRING = "";
    private static final int FLAG_STEREO_RESET = 2;
    private static final char LRE = 8234;
    private static final char LRM = 8206;
    private static final String LRM_STRING = Character.toString(LRM);
    private static final char PDF = 8236;
    private static final char RLE = 8235;
    private static final char RLM = 8207;
    private static final String RLM_STRING = Character.toString(RLM);
    private final TextDirectionHeuristicCompat mDefaultTextDirectionHeuristicCompat;
    private final int mFlags;
    private final boolean mIsRtlContext;

    public static final class Builder {
        private int mFlags;
        private boolean mIsRtlContext;
        private TextDirectionHeuristicCompat mTextDirectionHeuristicCompat;

        public Builder() {
            initialize(BidiFormatter.isRtlLocale(Locale.getDefault()));
        }

        public Builder(boolean rtlContext) {
            initialize(rtlContext);
        }

        public Builder(Locale locale) {
            initialize(BidiFormatter.isRtlLocale(locale));
        }

        private void initialize(boolean isRtlContext) {
            this.mIsRtlContext = isRtlContext;
            this.mTextDirectionHeuristicCompat = BidiFormatter.DEFAULT_TEXT_DIRECTION_HEURISTIC;
            this.mFlags = 2;
        }

        public Builder stereoReset(boolean stereoReset) {
            if (stereoReset) {
                this.mFlags |= 2;
            } else {
                this.mFlags &= -3;
            }
            return this;
        }

        public Builder setTextDirectionHeuristic(TextDirectionHeuristicCompat heuristic) {
            this.mTextDirectionHeuristicCompat = heuristic;
            return this;
        }

        private static BidiFormatter getDefaultInstanceFromContext(boolean isRtlContext) {
            return isRtlContext ? BidiFormatter.DEFAULT_RTL_INSTANCE : BidiFormatter.DEFAULT_LTR_INSTANCE;
        }

        public BidiFormatter build() {
            if (this.mFlags == 2 && this.mTextDirectionHeuristicCompat == BidiFormatter.DEFAULT_TEXT_DIRECTION_HEURISTIC) {
                return getDefaultInstanceFromContext(this.mIsRtlContext);
            }
            return new BidiFormatter(this.mIsRtlContext, this.mFlags, this.mTextDirectionHeuristicCompat);
        }
    }

    public static BidiFormatter getInstance() {
        return new Builder().build();
    }

    public static BidiFormatter getInstance(boolean rtlContext) {
        return new Builder(rtlContext).build();
    }

    public static BidiFormatter getInstance(Locale locale) {
        return new Builder(locale).build();
    }

    BidiFormatter(boolean isRtlContext, int flags, TextDirectionHeuristicCompat heuristic) {
        this.mIsRtlContext = isRtlContext;
        this.mFlags = flags;
        this.mDefaultTextDirectionHeuristicCompat = heuristic;
    }

    public boolean isRtlContext() {
        return this.mIsRtlContext;
    }

    public boolean getStereoReset() {
        return (this.mFlags & 2) != 0;
    }

    private String markAfter(CharSequence str, TextDirectionHeuristicCompat heuristic) {
        boolean isRtl = heuristic.isRtl(str, 0, str.length());
        if (!this.mIsRtlContext && (isRtl || getExitDir(str) == 1)) {
            return LRM_STRING;
        }
        if (!this.mIsRtlContext) {
            return EMPTY_STRING;
        }
        if (!isRtl || getExitDir(str) == -1) {
            return RLM_STRING;
        }
        return EMPTY_STRING;
    }

    private String markBefore(CharSequence str, TextDirectionHeuristicCompat heuristic) {
        boolean isRtl = heuristic.isRtl(str, 0, str.length());
        if (!this.mIsRtlContext && (isRtl || getEntryDir(str) == 1)) {
            return LRM_STRING;
        }
        if (!this.mIsRtlContext) {
            return EMPTY_STRING;
        }
        if (!isRtl || getEntryDir(str) == -1) {
            return RLM_STRING;
        }
        return EMPTY_STRING;
    }

    public boolean isRtl(String str) {
        return isRtl((CharSequence) str);
    }

    public boolean isRtl(CharSequence str) {
        return this.mDefaultTextDirectionHeuristicCompat.isRtl(str, 0, str.length());
    }

    public String unicodeWrap(String str, TextDirectionHeuristicCompat heuristic, boolean isolate) {
        if (str == null) {
            return null;
        }
        return unicodeWrap((CharSequence) str, heuristic, isolate).toString();
    }

    public CharSequence unicodeWrap(CharSequence str, TextDirectionHeuristicCompat heuristic, boolean isolate) {
        if (str == null) {
            return null;
        }
        boolean isRtl = heuristic.isRtl(str, 0, str.length());
        SpannableStringBuilder result = new SpannableStringBuilder();
        if (getStereoReset() && isolate) {
            result.append((CharSequence) markBefore(str, isRtl ? TextDirectionHeuristicsCompat.RTL : TextDirectionHeuristicsCompat.LTR));
        }
        if (isRtl != this.mIsRtlContext) {
            result.append(isRtl ? RLE : LRE);
            result.append(str);
            result.append(PDF);
        } else {
            result.append(str);
        }
        if (isolate) {
            result.append((CharSequence) markAfter(str, isRtl ? TextDirectionHeuristicsCompat.RTL : TextDirectionHeuristicsCompat.LTR));
        }
        return result;
    }

    public String unicodeWrap(String str, TextDirectionHeuristicCompat heuristic) {
        return unicodeWrap(str, heuristic, true);
    }

    public CharSequence unicodeWrap(CharSequence str, TextDirectionHeuristicCompat heuristic) {
        return unicodeWrap(str, heuristic, true);
    }

    public String unicodeWrap(String str, boolean isolate) {
        return unicodeWrap(str, this.mDefaultTextDirectionHeuristicCompat, isolate);
    }

    public CharSequence unicodeWrap(CharSequence str, boolean isolate) {
        return unicodeWrap(str, this.mDefaultTextDirectionHeuristicCompat, isolate);
    }

    public String unicodeWrap(String str) {
        return unicodeWrap(str, this.mDefaultTextDirectionHeuristicCompat, true);
    }

    public CharSequence unicodeWrap(CharSequence str) {
        return unicodeWrap(str, this.mDefaultTextDirectionHeuristicCompat, true);
    }

    static boolean isRtlLocale(Locale locale) {
        return TextUtilsCompat.getLayoutDirectionFromLocale(locale) == 1;
    }

    private static int getExitDir(CharSequence str) {
        return new DirectionalityEstimator(str, false).getExitDir();
    }

    private static int getEntryDir(CharSequence str) {
        return new DirectionalityEstimator(str, false).getEntryDir();
    }

    /* access modifiers changed from: private */
    public static class DirectionalityEstimator {
        private static final byte[] DIR_TYPE_CACHE = new byte[DIR_TYPE_CACHE_SIZE];
        private static final int DIR_TYPE_CACHE_SIZE = 1792;
        private int charIndex;
        private final boolean isHtml;
        private char lastChar;
        private final int length;
        private final CharSequence text;

        static {
            for (int i = 0; i < DIR_TYPE_CACHE_SIZE; i++) {
                DIR_TYPE_CACHE[i] = Character.getDirectionality(i);
            }
        }

        DirectionalityEstimator(CharSequence text2, boolean isHtml2) {
            this.text = text2;
            this.isHtml = isHtml2;
            this.length = text2.length();
        }

        /* access modifiers changed from: package-private */
        public int getEntryDir() {
            this.charIndex = 0;
            int embeddingLevelDir = 0;
            int embeddingLevel = 0;
            int firstNonEmptyEmbeddingLevel = 0;
            while (this.charIndex < this.length && firstNonEmptyEmbeddingLevel == 0) {
                byte dirTypeForward = dirTypeForward();
                if (dirTypeForward != 9) {
                    switch (dirTypeForward) {
                        case 0:
                            if (embeddingLevel == 0) {
                                return -1;
                            }
                            firstNonEmptyEmbeddingLevel = embeddingLevel;
                            continue;
                        case 1:
                        case 2:
                            if (embeddingLevel == 0) {
                                return 1;
                            }
                            firstNonEmptyEmbeddingLevel = embeddingLevel;
                            continue;
                        default:
                            switch (dirTypeForward) {
                                case 14:
                                case 15:
                                    embeddingLevel++;
                                    embeddingLevelDir = -1;
                                    continue;
                                case 16:
                                case 17:
                                    embeddingLevel++;
                                    embeddingLevelDir = 1;
                                    continue;
                                case 18:
                                    embeddingLevel--;
                                    embeddingLevelDir = 0;
                                    continue;
                                default:
                                    firstNonEmptyEmbeddingLevel = embeddingLevel;
                                    continue;
                                    continue;
                            }
                    }
                }
            }
            if (firstNonEmptyEmbeddingLevel == 0) {
                return 0;
            }
            if (embeddingLevelDir != 0) {
                return embeddingLevelDir;
            }
            while (this.charIndex > 0) {
                switch (dirTypeBackward()) {
                    case 14:
                    case 15:
                        if (firstNonEmptyEmbeddingLevel != embeddingLevel) {
                            embeddingLevel--;
                            break;
                        } else {
                            return -1;
                        }
                    case 16:
                    case 17:
                        if (firstNonEmptyEmbeddingLevel != embeddingLevel) {
                            embeddingLevel--;
                            break;
                        } else {
                            return 1;
                        }
                    case 18:
                        embeddingLevel++;
                        break;
                }
            }
            return 0;
        }

        /* access modifiers changed from: package-private */
        public int getExitDir() {
            this.charIndex = this.length;
            int embeddingLevel = 0;
            int lastNonEmptyEmbeddingLevel = 0;
            while (this.charIndex > 0) {
                byte dirTypeBackward = dirTypeBackward();
                if (dirTypeBackward != 9) {
                    switch (dirTypeBackward) {
                        case 0:
                            if (embeddingLevel == 0) {
                                return -1;
                            }
                            if (lastNonEmptyEmbeddingLevel == 0) {
                                lastNonEmptyEmbeddingLevel = embeddingLevel;
                                break;
                            } else {
                                continue;
                            }
                        case 1:
                        case 2:
                            if (embeddingLevel == 0) {
                                return 1;
                            }
                            if (lastNonEmptyEmbeddingLevel == 0) {
                                lastNonEmptyEmbeddingLevel = embeddingLevel;
                                break;
                            } else {
                                continue;
                            }
                        default:
                            switch (dirTypeBackward) {
                                case 14:
                                case 15:
                                    if (lastNonEmptyEmbeddingLevel == embeddingLevel) {
                                        return -1;
                                    }
                                    embeddingLevel--;
                                    continue;
                                case 16:
                                case 17:
                                    if (lastNonEmptyEmbeddingLevel == embeddingLevel) {
                                        return 1;
                                    }
                                    embeddingLevel--;
                                    continue;
                                case 18:
                                    embeddingLevel++;
                                    continue;
                                default:
                                    if (lastNonEmptyEmbeddingLevel == 0) {
                                        lastNonEmptyEmbeddingLevel = embeddingLevel;
                                        break;
                                    } else {
                                        continue;
                                        continue;
                                    }
                            }
                    }
                }
            }
            return 0;
        }

        private static byte getCachedDirectionality(char c) {
            return c < DIR_TYPE_CACHE_SIZE ? DIR_TYPE_CACHE[c] : Character.getDirectionality(c);
        }

        /* access modifiers changed from: package-private */
        public byte dirTypeForward() {
            this.lastChar = this.text.charAt(this.charIndex);
            if (Character.isHighSurrogate(this.lastChar)) {
                int codePoint = Character.codePointAt(this.text, this.charIndex);
                this.charIndex += Character.charCount(codePoint);
                return Character.getDirectionality(codePoint);
            }
            this.charIndex++;
            byte dirType = getCachedDirectionality(this.lastChar);
            if (!this.isHtml) {
                return dirType;
            }
            if (this.lastChar == '<') {
                return skipTagForward();
            }
            if (this.lastChar == '&') {
                return skipEntityForward();
            }
            return dirType;
        }

        /* access modifiers changed from: package-private */
        public byte dirTypeBackward() {
            this.lastChar = this.text.charAt(this.charIndex - 1);
            if (Character.isLowSurrogate(this.lastChar)) {
                int codePoint = Character.codePointBefore(this.text, this.charIndex);
                this.charIndex -= Character.charCount(codePoint);
                return Character.getDirectionality(codePoint);
            }
            this.charIndex--;
            byte dirType = getCachedDirectionality(this.lastChar);
            if (!this.isHtml) {
                return dirType;
            }
            if (this.lastChar == '>') {
                return skipTagBackward();
            }
            if (this.lastChar == ';') {
                return skipEntityBackward();
            }
            return dirType;
        }

        private byte skipTagForward() {
            int initialCharIndex = this.charIndex;
            while (this.charIndex < this.length) {
                CharSequence charSequence = this.text;
                int i = this.charIndex;
                this.charIndex = i + 1;
                this.lastChar = charSequence.charAt(i);
                if (this.lastChar == '>') {
                    return 12;
                }
                if (this.lastChar == '\"' || this.lastChar == '\'') {
                    char quote = this.lastChar;
                    while (this.charIndex < this.length) {
                        CharSequence charSequence2 = this.text;
                        int i2 = this.charIndex;
                        this.charIndex = i2 + 1;
                        char charAt = charSequence2.charAt(i2);
                        this.lastChar = charAt;
                        if (charAt == quote) {
                            break;
                        }
                    }
                }
            }
            this.charIndex = initialCharIndex;
            this.lastChar = '<';
            return 13;
        }

        private byte skipTagBackward() {
            int initialCharIndex = this.charIndex;
            while (this.charIndex > 0) {
                CharSequence charSequence = this.text;
                int i = this.charIndex - 1;
                this.charIndex = i;
                this.lastChar = charSequence.charAt(i);
                if (this.lastChar == '<') {
                    return 12;
                }
                if (this.lastChar == '>') {
                    break;
                } else if (this.lastChar == '\"' || this.lastChar == '\'') {
                    char quote = this.lastChar;
                    while (this.charIndex > 0) {
                        CharSequence charSequence2 = this.text;
                        int i2 = this.charIndex - 1;
                        this.charIndex = i2;
                        char charAt = charSequence2.charAt(i2);
                        this.lastChar = charAt;
                        if (charAt == quote) {
                            break;
                        }
                    }
                }
            }
            this.charIndex = initialCharIndex;
            this.lastChar = '>';
            return 13;
        }

        private byte skipEntityForward() {
            while (this.charIndex < this.length) {
                CharSequence charSequence = this.text;
                int i = this.charIndex;
                this.charIndex = i + 1;
                char charAt = charSequence.charAt(i);
                this.lastChar = charAt;
                if (charAt == ';') {
                    return 12;
                }
            }
            return 12;
        }

        private byte skipEntityBackward() {
            int initialCharIndex = this.charIndex;
            while (this.charIndex > 0) {
                CharSequence charSequence = this.text;
                int i = this.charIndex - 1;
                this.charIndex = i;
                this.lastChar = charSequence.charAt(i);
                if (this.lastChar != '&') {
                    if (this.lastChar == ';') {
                        break;
                    }
                } else {
                    return 12;
                }
            }
            this.charIndex = initialCharIndex;
            this.lastChar = ';';
            return 13;
        }
    }
}
