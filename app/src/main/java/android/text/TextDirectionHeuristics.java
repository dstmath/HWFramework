package android.text;

import com.huawei.pgmng.plug.PGSdk;
import java.nio.CharBuffer;
import java.util.Locale;

public class TextDirectionHeuristics {
    public static final TextDirectionHeuristic ANYRTL_LTR = null;
    public static final TextDirectionHeuristic FIRSTSTRONG_LTR = null;
    public static final TextDirectionHeuristic FIRSTSTRONG_RTL = null;
    public static final TextDirectionHeuristic LOCALE = null;
    public static final TextDirectionHeuristic LTR = null;
    public static final TextDirectionHeuristic RTL = null;
    private static final int STATE_FALSE = 1;
    private static final int STATE_TRUE = 0;
    private static final int STATE_UNKNOWN = 2;

    private interface TextDirectionAlgorithm {
        int checkRtl(CharSequence charSequence, int i, int i2);
    }

    private static class AnyStrong implements TextDirectionAlgorithm {
        public static final AnyStrong INSTANCE_LTR = null;
        public static final AnyStrong INSTANCE_RTL = null;
        private final boolean mLookForRtl;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.text.TextDirectionHeuristics.AnyStrong.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.text.TextDirectionHeuristics.AnyStrong.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.text.TextDirectionHeuristics.AnyStrong.<clinit>():void");
        }

        public int checkRtl(CharSequence cs, int start, int count) {
            int i = TextDirectionHeuristics.STATE_FALSE;
            boolean haveUnlookedFor = false;
            int openIsolateCount = TextDirectionHeuristics.STATE_TRUE;
            int i2 = start;
            int end = start + count;
            while (i2 < end) {
                int cp = Character.codePointAt(cs, i2);
                if (8294 <= cp && cp <= 8296) {
                    openIsolateCount += TextDirectionHeuristics.STATE_FALSE;
                } else if (cp == 8297) {
                    if (openIsolateCount > 0) {
                        openIsolateCount--;
                    }
                } else if (openIsolateCount == 0) {
                    switch (TextDirectionHeuristics.isRtlCodePoint(cp)) {
                        case TextDirectionHeuristics.STATE_TRUE /*0*/:
                            if (!this.mLookForRtl) {
                                haveUnlookedFor = true;
                                break;
                            }
                            return TextDirectionHeuristics.STATE_TRUE;
                        case TextDirectionHeuristics.STATE_FALSE /*1*/:
                            if (this.mLookForRtl) {
                                haveUnlookedFor = true;
                                break;
                            }
                            return TextDirectionHeuristics.STATE_FALSE;
                        default:
                            break;
                    }
                } else {
                    continue;
                }
                i2 += Character.charCount(cp);
            }
            if (!haveUnlookedFor) {
                return TextDirectionHeuristics.STATE_UNKNOWN;
            }
            if (!this.mLookForRtl) {
                i = TextDirectionHeuristics.STATE_TRUE;
            }
            return i;
        }

        private AnyStrong(boolean lookForRtl) {
            this.mLookForRtl = lookForRtl;
        }
    }

    private static class FirstStrong implements TextDirectionAlgorithm {
        public static final FirstStrong INSTANCE = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.text.TextDirectionHeuristics.FirstStrong.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.text.TextDirectionHeuristics.FirstStrong.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.text.TextDirectionHeuristics.FirstStrong.<clinit>():void");
        }

        public int checkRtl(CharSequence cs, int start, int count) {
            int result = TextDirectionHeuristics.STATE_UNKNOWN;
            int openIsolateCount = TextDirectionHeuristics.STATE_TRUE;
            int i = start;
            int end = start + count;
            while (i < end && result == TextDirectionHeuristics.STATE_UNKNOWN) {
                int cp = Character.codePointAt(cs, i);
                if (8294 <= cp && cp <= 8296) {
                    openIsolateCount += TextDirectionHeuristics.STATE_FALSE;
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
                case TextDirectionHeuristics.STATE_TRUE /*0*/:
                    return true;
                case TextDirectionHeuristics.STATE_FALSE /*1*/:
                    return false;
                default:
                    return defaultIsRtl();
            }
        }
    }

    private static class TextDirectionHeuristicInternal extends TextDirectionHeuristicImpl {
        private final boolean mDefaultIsRtl;

        /* synthetic */ TextDirectionHeuristicInternal(TextDirectionAlgorithm algorithm, boolean defaultIsRtl, TextDirectionHeuristicInternal textDirectionHeuristicInternal) {
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
        public static final TextDirectionHeuristicLocale INSTANCE = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.text.TextDirectionHeuristics.TextDirectionHeuristicLocale.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.text.TextDirectionHeuristics.TextDirectionHeuristicLocale.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.text.TextDirectionHeuristics.TextDirectionHeuristicLocale.<clinit>():void");
        }

        public TextDirectionHeuristicLocale() {
            super(null);
        }

        protected boolean defaultIsRtl() {
            if (TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == TextDirectionHeuristics.STATE_FALSE) {
                return true;
            }
            return false;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.text.TextDirectionHeuristics.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.text.TextDirectionHeuristics.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.text.TextDirectionHeuristics.<clinit>():void");
    }

    public TextDirectionHeuristics() {
    }

    private static int isRtlCodePoint(int codePoint) {
        switch (Character.getDirectionality(codePoint)) {
            case PGSdk.TYPE_UNKNOW /*-1*/:
                if ((1424 > codePoint || codePoint > 2303) && ((64285 > codePoint || codePoint > 64975) && ((65008 > codePoint || codePoint > 65023) && ((65136 > codePoint || codePoint > 65279) && ((67584 > codePoint || codePoint > 69631) && (124928 > codePoint || codePoint > 126975)))))) {
                    return ((8293 > codePoint || codePoint > 8297) && ((65520 > codePoint || codePoint > 65528) && ((917504 > codePoint || codePoint > 921599) && ((64976 > codePoint || codePoint > 65007) && (codePoint & 65534) != 65534 && ((8352 > codePoint || codePoint > 8399) && (55296 > codePoint || codePoint > 57343)))))) ? STATE_FALSE : STATE_UNKNOWN;
                } else {
                    return STATE_TRUE;
                }
            case STATE_TRUE /*0*/:
                return STATE_FALSE;
            case STATE_FALSE /*1*/:
            case STATE_UNKNOWN /*2*/:
                return STATE_TRUE;
            default:
                return STATE_UNKNOWN;
        }
    }
}
