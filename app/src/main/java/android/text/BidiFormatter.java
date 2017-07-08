package android.text;

import android.telephony.PhoneNumberUtils;
import android.text.format.DateFormat;
import com.hisi.perfhub.PerfHub;
import com.huawei.android.statistical.StatisticalConstant;
import com.huawei.indexsearch.IndexSearchConstants;
import com.huawei.pgmng.plug.PGSdk;
import com.nxp.nfc.gsma.internal.NxpNfcController;
import java.util.Locale;

public final class BidiFormatter {
    private static final int DEFAULT_FLAGS = 2;
    private static final BidiFormatter DEFAULT_LTR_INSTANCE = null;
    private static final BidiFormatter DEFAULT_RTL_INSTANCE = null;
    private static TextDirectionHeuristic DEFAULT_TEXT_DIRECTION_HEURISTIC = null;
    private static final int DIR_LTR = -1;
    private static final int DIR_RTL = 1;
    private static final int DIR_UNKNOWN = 0;
    private static final String EMPTY_STRING = "";
    private static final int FLAG_STEREO_RESET = 2;
    private static final char LRE = '\u202a';
    private static final char LRM = '\u200e';
    private static final String LRM_STRING = null;
    private static final char PDF = '\u202c';
    private static final char RLE = '\u202b';
    private static final char RLM = '\u200f';
    private static final String RLM_STRING = null;
    private final TextDirectionHeuristic mDefaultTextDirectionHeuristic;
    private final int mFlags;
    private final boolean mIsRtlContext;

    public static final class Builder {
        private int mFlags;
        private boolean mIsRtlContext;
        private TextDirectionHeuristic mTextDirectionHeuristic;

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
            this.mTextDirectionHeuristic = BidiFormatter.DEFAULT_TEXT_DIRECTION_HEURISTIC;
            this.mFlags = BidiFormatter.FLAG_STEREO_RESET;
        }

        public Builder stereoReset(boolean stereoReset) {
            if (stereoReset) {
                this.mFlags |= BidiFormatter.FLAG_STEREO_RESET;
            } else {
                this.mFlags &= -3;
            }
            return this;
        }

        public Builder setTextDirectionHeuristic(TextDirectionHeuristic heuristic) {
            this.mTextDirectionHeuristic = heuristic;
            return this;
        }

        public BidiFormatter build() {
            if (this.mFlags == BidiFormatter.FLAG_STEREO_RESET && this.mTextDirectionHeuristic == BidiFormatter.DEFAULT_TEXT_DIRECTION_HEURISTIC) {
                return BidiFormatter.getDefaultInstanceFromContext(this.mIsRtlContext);
            }
            return new BidiFormatter(this.mIsRtlContext, this.mFlags, this.mTextDirectionHeuristic, null);
        }
    }

    private static class DirectionalityEstimator {
        private static final byte[] DIR_TYPE_CACHE = null;
        private static final int DIR_TYPE_CACHE_SIZE = 1792;
        private int charIndex;
        private final boolean isHtml;
        private char lastChar;
        private final int length;
        private final CharSequence text;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.text.BidiFormatter.DirectionalityEstimator.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.text.BidiFormatter.DirectionalityEstimator.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.text.BidiFormatter.DirectionalityEstimator.<clinit>():void");
        }

        DirectionalityEstimator(CharSequence text, boolean isHtml) {
            this.text = text;
            this.isHtml = isHtml;
            this.length = text.length();
        }

        int getEntryDir() {
            this.charIndex = BidiFormatter.DIR_UNKNOWN;
            int embeddingLevel = BidiFormatter.DIR_UNKNOWN;
            int embeddingLevelDir = BidiFormatter.DIR_UNKNOWN;
            int firstNonEmptyEmbeddingLevel = BidiFormatter.DIR_UNKNOWN;
            while (this.charIndex < this.length && firstNonEmptyEmbeddingLevel == 0) {
                switch (dirTypeForward()) {
                    case BidiFormatter.DIR_UNKNOWN /*0*/:
                        if (embeddingLevel != 0) {
                            firstNonEmptyEmbeddingLevel = embeddingLevel;
                            break;
                        }
                        return BidiFormatter.DIR_LTR;
                    case BidiFormatter.DIR_RTL /*1*/:
                    case BidiFormatter.FLAG_STEREO_RESET /*2*/:
                        if (embeddingLevel != 0) {
                            firstNonEmptyEmbeddingLevel = embeddingLevel;
                            break;
                        }
                        return BidiFormatter.DIR_RTL;
                    case PGSdk.TYPE_SCRLOCK /*9*/:
                        break;
                    case StatisticalConstant.TYPE_FINGER_BIAS_SPLIT_RIGHT /*14*/:
                    case IndexSearchConstants.INDEX_BUILD_OP_MASK /*15*/:
                        embeddingLevel += BidiFormatter.DIR_RTL;
                        embeddingLevelDir = BidiFormatter.DIR_LTR;
                        break;
                    case NxpNfcController.PROTOCOL_ISO_DEP /*16*/:
                    case StatisticalConstant.TYPE_TRIKEY_RIGHT_RECENT /*17*/:
                        embeddingLevel += BidiFormatter.DIR_RTL;
                        embeddingLevelDir = BidiFormatter.DIR_RTL;
                        break;
                    case PerfHub.PERF_TAG_IPA_CONTROL_TEMP /*18*/:
                        embeddingLevel += BidiFormatter.DIR_LTR;
                        embeddingLevelDir = BidiFormatter.DIR_UNKNOWN;
                        break;
                    default:
                        firstNonEmptyEmbeddingLevel = embeddingLevel;
                        break;
                }
            }
            if (firstNonEmptyEmbeddingLevel == 0) {
                return BidiFormatter.DIR_UNKNOWN;
            }
            if (embeddingLevelDir != 0) {
                return embeddingLevelDir;
            }
            while (this.charIndex > 0) {
                switch (dirTypeBackward()) {
                    case StatisticalConstant.TYPE_FINGER_BIAS_SPLIT_RIGHT /*14*/:
                    case IndexSearchConstants.INDEX_BUILD_OP_MASK /*15*/:
                        if (firstNonEmptyEmbeddingLevel != embeddingLevel) {
                            embeddingLevel += BidiFormatter.DIR_LTR;
                            break;
                        }
                        return BidiFormatter.DIR_LTR;
                    case NxpNfcController.PROTOCOL_ISO_DEP /*16*/:
                    case StatisticalConstant.TYPE_TRIKEY_RIGHT_RECENT /*17*/:
                        if (firstNonEmptyEmbeddingLevel != embeddingLevel) {
                            embeddingLevel += BidiFormatter.DIR_LTR;
                            break;
                        }
                        return BidiFormatter.DIR_RTL;
                    case PerfHub.PERF_TAG_IPA_CONTROL_TEMP /*18*/:
                        embeddingLevel += BidiFormatter.DIR_RTL;
                        break;
                    default:
                        break;
                }
            }
            return BidiFormatter.DIR_UNKNOWN;
        }

        int getExitDir() {
            this.charIndex = this.length;
            int embeddingLevel = BidiFormatter.DIR_UNKNOWN;
            int lastNonEmptyEmbeddingLevel = BidiFormatter.DIR_UNKNOWN;
            while (this.charIndex > 0) {
                switch (dirTypeBackward()) {
                    case BidiFormatter.DIR_UNKNOWN /*0*/:
                        if (embeddingLevel != 0) {
                            if (lastNonEmptyEmbeddingLevel != 0) {
                                break;
                            }
                            lastNonEmptyEmbeddingLevel = embeddingLevel;
                            break;
                        }
                        return BidiFormatter.DIR_LTR;
                    case BidiFormatter.DIR_RTL /*1*/:
                    case BidiFormatter.FLAG_STEREO_RESET /*2*/:
                        if (embeddingLevel != 0) {
                            if (lastNonEmptyEmbeddingLevel != 0) {
                                break;
                            }
                            lastNonEmptyEmbeddingLevel = embeddingLevel;
                            break;
                        }
                        return BidiFormatter.DIR_RTL;
                    case PGSdk.TYPE_SCRLOCK /*9*/:
                        break;
                    case StatisticalConstant.TYPE_FINGER_BIAS_SPLIT_RIGHT /*14*/:
                    case IndexSearchConstants.INDEX_BUILD_OP_MASK /*15*/:
                        if (lastNonEmptyEmbeddingLevel != embeddingLevel) {
                            embeddingLevel += BidiFormatter.DIR_LTR;
                            break;
                        }
                        return BidiFormatter.DIR_LTR;
                    case NxpNfcController.PROTOCOL_ISO_DEP /*16*/:
                    case StatisticalConstant.TYPE_TRIKEY_RIGHT_RECENT /*17*/:
                        if (lastNonEmptyEmbeddingLevel != embeddingLevel) {
                            embeddingLevel += BidiFormatter.DIR_LTR;
                            break;
                        }
                        return BidiFormatter.DIR_RTL;
                    case PerfHub.PERF_TAG_IPA_CONTROL_TEMP /*18*/:
                        embeddingLevel += BidiFormatter.DIR_RTL;
                        break;
                    default:
                        if (lastNonEmptyEmbeddingLevel != 0) {
                            break;
                        }
                        lastNonEmptyEmbeddingLevel = embeddingLevel;
                        break;
                }
            }
            return BidiFormatter.DIR_UNKNOWN;
        }

        private static byte getCachedDirectionality(char c) {
            return c < '\u0700' ? DIR_TYPE_CACHE[c] : Character.getDirectionality(c);
        }

        byte dirTypeForward() {
            this.lastChar = this.text.charAt(this.charIndex);
            if (Character.isHighSurrogate(this.lastChar)) {
                int codePoint = Character.codePointAt(this.text, this.charIndex);
                this.charIndex += Character.charCount(codePoint);
                return Character.getDirectionality(codePoint);
            }
            this.charIndex += BidiFormatter.DIR_RTL;
            byte dirType = getCachedDirectionality(this.lastChar);
            if (this.isHtml) {
                if (this.lastChar == '<') {
                    dirType = skipTagForward();
                } else if (this.lastChar == '&') {
                    dirType = skipEntityForward();
                }
            }
            return dirType;
        }

        byte dirTypeBackward() {
            this.lastChar = this.text.charAt(this.charIndex + BidiFormatter.DIR_LTR);
            if (Character.isLowSurrogate(this.lastChar)) {
                int codePoint = Character.codePointBefore(this.text, this.charIndex);
                this.charIndex -= Character.charCount(codePoint);
                return Character.getDirectionality(codePoint);
            }
            this.charIndex += BidiFormatter.DIR_LTR;
            byte dirType = getCachedDirectionality(this.lastChar);
            if (this.isHtml) {
                if (this.lastChar == '>') {
                    dirType = skipTagBackward();
                } else if (this.lastChar == PhoneNumberUtils.WAIT) {
                    dirType = skipEntityBackward();
                }
            }
            return dirType;
        }

        private byte skipTagForward() {
            int initialCharIndex = this.charIndex;
            while (this.charIndex < this.length) {
                CharSequence charSequence = this.text;
                int i = this.charIndex;
                this.charIndex = i + BidiFormatter.DIR_RTL;
                this.lastChar = charSequence.charAt(i);
                if (this.lastChar == '>') {
                    return (byte) 12;
                }
                if (this.lastChar == '\"' || this.lastChar == DateFormat.QUOTE) {
                    char quote = this.lastChar;
                    while (this.charIndex < this.length) {
                        charSequence = this.text;
                        i = this.charIndex;
                        this.charIndex = i + BidiFormatter.DIR_RTL;
                        char charAt = charSequence.charAt(i);
                        this.lastChar = charAt;
                        if (charAt == quote) {
                            break;
                        }
                    }
                }
            }
            this.charIndex = initialCharIndex;
            this.lastChar = '<';
            return (byte) 13;
        }

        private byte skipTagBackward() {
            int initialCharIndex = this.charIndex;
            while (this.charIndex > 0) {
                CharSequence charSequence = this.text;
                int i = this.charIndex + BidiFormatter.DIR_LTR;
                this.charIndex = i;
                this.lastChar = charSequence.charAt(i);
                if (this.lastChar == '<') {
                    return (byte) 12;
                }
                if (this.lastChar == '>') {
                    break;
                } else if (this.lastChar == '\"' || this.lastChar == DateFormat.QUOTE) {
                    char quote = this.lastChar;
                    while (this.charIndex > 0) {
                        charSequence = this.text;
                        i = this.charIndex + BidiFormatter.DIR_LTR;
                        this.charIndex = i;
                        char charAt = charSequence.charAt(i);
                        this.lastChar = charAt;
                        if (charAt == quote) {
                            break;
                        }
                    }
                }
            }
            this.charIndex = initialCharIndex;
            this.lastChar = '>';
            return (byte) 13;
        }

        private byte skipEntityForward() {
            while (this.charIndex < this.length) {
                CharSequence charSequence = this.text;
                int i = this.charIndex;
                this.charIndex = i + BidiFormatter.DIR_RTL;
                char charAt = charSequence.charAt(i);
                this.lastChar = charAt;
                if (charAt == PhoneNumberUtils.WAIT) {
                    break;
                }
            }
            return (byte) 12;
        }

        private byte skipEntityBackward() {
            int initialCharIndex = this.charIndex;
            while (this.charIndex > 0) {
                CharSequence charSequence = this.text;
                int i = this.charIndex + BidiFormatter.DIR_LTR;
                this.charIndex = i;
                this.lastChar = charSequence.charAt(i);
                if (this.lastChar != '&') {
                    if (this.lastChar == PhoneNumberUtils.WAIT) {
                        break;
                    }
                }
                return (byte) 12;
            }
            this.charIndex = initialCharIndex;
            this.lastChar = PhoneNumberUtils.WAIT;
            return (byte) 13;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.text.BidiFormatter.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.text.BidiFormatter.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.text.BidiFormatter.<clinit>():void");
    }

    /* synthetic */ BidiFormatter(boolean isRtlContext, int flags, TextDirectionHeuristic heuristic, BidiFormatter bidiFormatter) {
        this(isRtlContext, flags, heuristic);
    }

    public static BidiFormatter getInstance() {
        return getDefaultInstanceFromContext(isRtlLocale(Locale.getDefault()));
    }

    public static BidiFormatter getInstance(boolean rtlContext) {
        return getDefaultInstanceFromContext(rtlContext);
    }

    public static BidiFormatter getInstance(Locale locale) {
        return getDefaultInstanceFromContext(isRtlLocale(locale));
    }

    private BidiFormatter(boolean isRtlContext, int flags, TextDirectionHeuristic heuristic) {
        this.mIsRtlContext = isRtlContext;
        this.mFlags = flags;
        this.mDefaultTextDirectionHeuristic = heuristic;
    }

    public boolean isRtlContext() {
        return this.mIsRtlContext;
    }

    public boolean getStereoReset() {
        return (this.mFlags & FLAG_STEREO_RESET) != 0;
    }

    public String markAfter(CharSequence str, TextDirectionHeuristic heuristic) {
        boolean isRtl = heuristic.isRtl(str, (int) DIR_UNKNOWN, str.length());
        if (!this.mIsRtlContext && (isRtl || getExitDir(str) == DIR_RTL)) {
            return LRM_STRING;
        }
        if (!this.mIsRtlContext || (isRtl && getExitDir(str) != DIR_LTR)) {
            return EMPTY_STRING;
        }
        return RLM_STRING;
    }

    public String markBefore(CharSequence str, TextDirectionHeuristic heuristic) {
        boolean isRtl = heuristic.isRtl(str, (int) DIR_UNKNOWN, str.length());
        if (!this.mIsRtlContext && (isRtl || getEntryDir(str) == DIR_RTL)) {
            return LRM_STRING;
        }
        if (!this.mIsRtlContext || (isRtl && getEntryDir(str) != DIR_LTR)) {
            return EMPTY_STRING;
        }
        return RLM_STRING;
    }

    public boolean isRtl(String str) {
        return isRtl((CharSequence) str);
    }

    public boolean isRtl(CharSequence str) {
        return this.mDefaultTextDirectionHeuristic.isRtl(str, (int) DIR_UNKNOWN, str.length());
    }

    public String unicodeWrap(String str, TextDirectionHeuristic heuristic, boolean isolate) {
        if (str == null) {
            return null;
        }
        return unicodeWrap((CharSequence) str, heuristic, isolate).toString();
    }

    public CharSequence unicodeWrap(CharSequence str, TextDirectionHeuristic heuristic, boolean isolate) {
        if (str == null) {
            return null;
        }
        boolean isRtl = heuristic.isRtl(str, (int) DIR_UNKNOWN, str.length());
        SpannableStringBuilder result = new SpannableStringBuilder();
        if (getStereoReset() && isolate) {
            result.append(markBefore(str, isRtl ? TextDirectionHeuristics.RTL : TextDirectionHeuristics.LTR));
        }
        if (isRtl != this.mIsRtlContext) {
            result.append(isRtl ? RLE : LRE);
            result.append(str);
            result.append((char) PDF);
        } else {
            result.append(str);
        }
        if (isolate) {
            result.append(markAfter(str, isRtl ? TextDirectionHeuristics.RTL : TextDirectionHeuristics.LTR));
        }
        return result;
    }

    public String unicodeWrap(String str, TextDirectionHeuristic heuristic) {
        return unicodeWrap(str, heuristic, true);
    }

    public CharSequence unicodeWrap(CharSequence str, TextDirectionHeuristic heuristic) {
        return unicodeWrap(str, heuristic, true);
    }

    public String unicodeWrap(String str, boolean isolate) {
        return unicodeWrap(str, this.mDefaultTextDirectionHeuristic, isolate);
    }

    public CharSequence unicodeWrap(CharSequence str, boolean isolate) {
        return unicodeWrap(str, this.mDefaultTextDirectionHeuristic, isolate);
    }

    public String unicodeWrap(String str) {
        return unicodeWrap(str, this.mDefaultTextDirectionHeuristic, true);
    }

    public CharSequence unicodeWrap(CharSequence str) {
        return unicodeWrap(str, this.mDefaultTextDirectionHeuristic, true);
    }

    private static BidiFormatter getDefaultInstanceFromContext(boolean isRtlContext) {
        return isRtlContext ? DEFAULT_RTL_INSTANCE : DEFAULT_LTR_INSTANCE;
    }

    private static boolean isRtlLocale(Locale locale) {
        return TextUtils.getLayoutDirectionFromLocale(locale) == DIR_RTL;
    }

    private static int getExitDir(CharSequence str) {
        return new DirectionalityEstimator(str, false).getExitDir();
    }

    private static int getEntryDir(CharSequence str) {
        return new DirectionalityEstimator(str, false).getEntryDir();
    }
}
