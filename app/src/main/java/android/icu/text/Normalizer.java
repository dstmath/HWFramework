package android.icu.text;

import android.icu.impl.Norm2AllModes;
import android.icu.impl.Norm2AllModes.Normalizer2WithImpl;
import android.icu.impl.Normalizer2Impl;
import android.icu.impl.UCaseProps;
import android.icu.lang.UCharacter;
import android.icu.util.ICUCloneNotSupportedException;
import com.android.dex.DexFormat;
import java.nio.CharBuffer;
import java.text.CharacterIterator;
import org.xmlpull.v1.XmlPullParser;

public final class Normalizer implements Cloneable {
    public static final int COMPARE_CODE_POINT_ORDER = 32768;
    private static final int COMPARE_EQUIV = 524288;
    public static final int COMPARE_IGNORE_CASE = 65536;
    @Deprecated
    public static final int COMPARE_NORM_OPTIONS_SHIFT = 20;
    @Deprecated
    public static final Mode COMPOSE = null;
    @Deprecated
    public static final Mode COMPOSE_COMPAT = null;
    @Deprecated
    public static final Mode DECOMP = null;
    @Deprecated
    public static final Mode DECOMP_COMPAT = null;
    @Deprecated
    public static final Mode DEFAULT = null;
    @Deprecated
    public static final int DONE = -1;
    @Deprecated
    public static final Mode FCD = null;
    public static final int FOLD_CASE_DEFAULT = 0;
    public static final int FOLD_CASE_EXCLUDE_SPECIAL_I = 1;
    @Deprecated
    public static final int IGNORE_HANGUL = 1;
    public static final int INPUT_IS_FCD = 131072;
    public static final QuickCheckResult MAYBE = null;
    @Deprecated
    public static final Mode NFC = null;
    @Deprecated
    public static final Mode NFD = null;
    @Deprecated
    public static final Mode NFKC = null;
    @Deprecated
    public static final Mode NFKD = null;
    public static final QuickCheckResult NO = null;
    @Deprecated
    public static final Mode NONE = null;
    @Deprecated
    public static final Mode NO_OP = null;
    @Deprecated
    public static final int UNICODE_3_2 = 32;
    public static final QuickCheckResult YES = null;
    private StringBuilder buffer;
    private int bufferPos;
    private int currentIndex;
    private Mode mode;
    private int nextIndex;
    private Normalizer2 norm2;
    private int options;
    private UCharacterIterator text;

    private static final class CharsAppendable implements Appendable {
        private final char[] chars;
        private final int limit;
        private int offset;
        private final int start;

        public CharsAppendable(char[] dest, int destStart, int destLimit) {
            this.chars = dest;
            this.offset = destStart;
            this.start = destStart;
            this.limit = destLimit;
        }

        public int length() {
            int len = this.offset - this.start;
            if (this.offset <= this.limit) {
                return len;
            }
            throw new IndexOutOfBoundsException(Integer.toString(len));
        }

        public Appendable append(char c) {
            if (this.offset < this.limit) {
                this.chars[this.offset] = c;
            }
            this.offset += Normalizer.IGNORE_HANGUL;
            return this;
        }

        public Appendable append(CharSequence s) {
            return append(s, Normalizer.FOLD_CASE_DEFAULT, s.length());
        }

        public Appendable append(CharSequence s, int sStart, int sLimit) {
            int len = sLimit - sStart;
            if (len <= this.limit - this.offset) {
                while (true) {
                    int sStart2 = sStart;
                    if (sStart2 >= sLimit) {
                        break;
                    }
                    char[] cArr = this.chars;
                    int i = this.offset;
                    this.offset = i + Normalizer.IGNORE_HANGUL;
                    sStart = sStart2 + Normalizer.IGNORE_HANGUL;
                    cArr[i] = s.charAt(sStart2);
                }
            } else {
                this.offset += len;
            }
            return this;
        }
    }

    private static final class CmpEquivLevel {
        CharSequence cs;
        int s;

        private CmpEquivLevel() {
        }
    }

    private static final class FCD32ModeImpl {
        private static final ModeImpl INSTANCE = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.Normalizer.FCD32ModeImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.Normalizer.FCD32ModeImpl.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.Normalizer.FCD32ModeImpl.<clinit>():void");
        }

        private FCD32ModeImpl() {
        }
    }

    @Deprecated
    public static abstract class Mode {
        @Deprecated
        protected abstract Normalizer2 getNormalizer2(int i);

        @Deprecated
        protected Mode() {
        }
    }

    private static final class FCDMode extends Mode {
        /* synthetic */ FCDMode(FCDMode fCDMode) {
            this();
        }

        private FCDMode() {
        }

        protected Normalizer2 getNormalizer2(int options) {
            return (options & Normalizer.UNICODE_3_2) != 0 ? FCD32ModeImpl.INSTANCE.normalizer2 : FCDModeImpl.INSTANCE.normalizer2;
        }
    }

    private static final class FCDModeImpl {
        private static final ModeImpl INSTANCE = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.Normalizer.FCDModeImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.Normalizer.FCDModeImpl.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.Normalizer.FCDModeImpl.<clinit>():void");
        }

        private FCDModeImpl() {
        }
    }

    private static final class ModeImpl {
        private final Normalizer2 normalizer2;

        /* synthetic */ ModeImpl(Normalizer2 n2, ModeImpl modeImpl) {
            this(n2);
        }

        private ModeImpl(Normalizer2 n2) {
            this.normalizer2 = n2;
        }
    }

    private static final class NFC32ModeImpl {
        private static final ModeImpl INSTANCE = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.Normalizer.NFC32ModeImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.Normalizer.NFC32ModeImpl.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.Normalizer.NFC32ModeImpl.<clinit>():void");
        }

        private NFC32ModeImpl() {
        }
    }

    private static final class NFCMode extends Mode {
        /* synthetic */ NFCMode(NFCMode nFCMode) {
            this();
        }

        private NFCMode() {
        }

        protected Normalizer2 getNormalizer2(int options) {
            return (options & Normalizer.UNICODE_3_2) != 0 ? NFC32ModeImpl.INSTANCE.normalizer2 : NFCModeImpl.INSTANCE.normalizer2;
        }
    }

    private static final class NFCModeImpl {
        private static final ModeImpl INSTANCE = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.Normalizer.NFCModeImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.Normalizer.NFCModeImpl.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.Normalizer.NFCModeImpl.<clinit>():void");
        }

        private NFCModeImpl() {
        }
    }

    private static final class NFD32ModeImpl {
        private static final ModeImpl INSTANCE = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.Normalizer.NFD32ModeImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.Normalizer.NFD32ModeImpl.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.Normalizer.NFD32ModeImpl.<clinit>():void");
        }

        private NFD32ModeImpl() {
        }
    }

    private static final class NFDMode extends Mode {
        /* synthetic */ NFDMode(NFDMode nFDMode) {
            this();
        }

        private NFDMode() {
        }

        protected Normalizer2 getNormalizer2(int options) {
            return (options & Normalizer.UNICODE_3_2) != 0 ? NFD32ModeImpl.INSTANCE.normalizer2 : NFDModeImpl.INSTANCE.normalizer2;
        }
    }

    private static final class NFDModeImpl {
        private static final ModeImpl INSTANCE = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.Normalizer.NFDModeImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.Normalizer.NFDModeImpl.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.Normalizer.NFDModeImpl.<clinit>():void");
        }

        private NFDModeImpl() {
        }
    }

    private static final class NFKC32ModeImpl {
        private static final ModeImpl INSTANCE = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.Normalizer.NFKC32ModeImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.Normalizer.NFKC32ModeImpl.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.Normalizer.NFKC32ModeImpl.<clinit>():void");
        }

        private NFKC32ModeImpl() {
        }
    }

    private static final class NFKCMode extends Mode {
        /* synthetic */ NFKCMode(NFKCMode nFKCMode) {
            this();
        }

        private NFKCMode() {
        }

        protected Normalizer2 getNormalizer2(int options) {
            return (options & Normalizer.UNICODE_3_2) != 0 ? NFKC32ModeImpl.INSTANCE.normalizer2 : NFKCModeImpl.INSTANCE.normalizer2;
        }
    }

    private static final class NFKCModeImpl {
        private static final ModeImpl INSTANCE = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.Normalizer.NFKCModeImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.Normalizer.NFKCModeImpl.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.Normalizer.NFKCModeImpl.<clinit>():void");
        }

        private NFKCModeImpl() {
        }
    }

    private static final class NFKD32ModeImpl {
        private static final ModeImpl INSTANCE = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.Normalizer.NFKD32ModeImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.Normalizer.NFKD32ModeImpl.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.Normalizer.NFKD32ModeImpl.<clinit>():void");
        }

        private NFKD32ModeImpl() {
        }
    }

    private static final class NFKDMode extends Mode {
        /* synthetic */ NFKDMode(NFKDMode nFKDMode) {
            this();
        }

        private NFKDMode() {
        }

        protected Normalizer2 getNormalizer2(int options) {
            return (options & Normalizer.UNICODE_3_2) != 0 ? NFKD32ModeImpl.INSTANCE.normalizer2 : NFKDModeImpl.INSTANCE.normalizer2;
        }
    }

    private static final class NFKDModeImpl {
        private static final ModeImpl INSTANCE = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.Normalizer.NFKDModeImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.Normalizer.NFKDModeImpl.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.Normalizer.NFKDModeImpl.<clinit>():void");
        }

        private NFKDModeImpl() {
        }
    }

    private static final class NONEMode extends Mode {
        /* synthetic */ NONEMode(NONEMode nONEMode) {
            this();
        }

        private NONEMode() {
        }

        protected Normalizer2 getNormalizer2(int options) {
            return Norm2AllModes.NOOP_NORMALIZER2;
        }
    }

    public static final class QuickCheckResult {
        /* synthetic */ QuickCheckResult(int value, QuickCheckResult quickCheckResult) {
            this(value);
        }

        private QuickCheckResult(int value) {
        }
    }

    private static final class Unicode32 {
        private static final UnicodeSet INSTANCE = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.Normalizer.Unicode32.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.Normalizer.Unicode32.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.Normalizer.Unicode32.<clinit>():void");
        }

        private Unicode32() {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.Normalizer.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.Normalizer.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.Normalizer.<clinit>():void");
    }

    static int cmpEquivFold(java.lang.CharSequence r1, java.lang.CharSequence r2, int r3) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.Normalizer.cmpEquivFold(java.lang.CharSequence, java.lang.CharSequence, int):int
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.Normalizer.cmpEquivFold(java.lang.CharSequence, java.lang.CharSequence, int):int
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.Normalizer.cmpEquivFold(java.lang.CharSequence, java.lang.CharSequence, int):int");
    }

    @java.lang.Deprecated
    public void setOption(int r1, boolean r2) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.Normalizer.setOption(int, boolean):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.Normalizer.setOption(int, boolean):void");
    }

    @Deprecated
    public Normalizer(String str, Mode mode, int opt) {
        this.text = UCharacterIterator.getInstance(str);
        this.mode = mode;
        this.options = opt;
        this.norm2 = mode.getNormalizer2(opt);
        this.buffer = new StringBuilder();
    }

    @Deprecated
    public Normalizer(CharacterIterator iter, Mode mode, int opt) {
        this.text = UCharacterIterator.getInstance((CharacterIterator) iter.clone());
        this.mode = mode;
        this.options = opt;
        this.norm2 = mode.getNormalizer2(opt);
        this.buffer = new StringBuilder();
    }

    @Deprecated
    public Normalizer(UCharacterIterator iter, Mode mode, int options) {
        try {
            this.text = (UCharacterIterator) iter.clone();
            this.mode = mode;
            this.options = options;
            this.norm2 = mode.getNormalizer2(options);
            this.buffer = new StringBuilder();
        } catch (Throwable e) {
            throw new ICUCloneNotSupportedException(e);
        }
    }

    @Deprecated
    public Object clone() {
        try {
            Normalizer copy = (Normalizer) super.clone();
            copy.text = (UCharacterIterator) this.text.clone();
            copy.mode = this.mode;
            copy.options = this.options;
            copy.norm2 = this.norm2;
            copy.buffer = new StringBuilder(this.buffer);
            copy.bufferPos = this.bufferPos;
            copy.currentIndex = this.currentIndex;
            copy.nextIndex = this.nextIndex;
            return copy;
        } catch (Throwable e) {
            throw new ICUCloneNotSupportedException(e);
        }
    }

    private static final Normalizer2 getComposeNormalizer2(boolean compat, int options) {
        return (compat ? NFKC : NFC).getNormalizer2(options);
    }

    private static final Normalizer2 getDecomposeNormalizer2(boolean compat, int options) {
        return (compat ? NFKD : NFD).getNormalizer2(options);
    }

    @Deprecated
    public static String compose(String str, boolean compat) {
        return compose(str, compat, FOLD_CASE_DEFAULT);
    }

    @Deprecated
    public static String compose(String str, boolean compat, int options) {
        return getComposeNormalizer2(compat, options).normalize(str);
    }

    @Deprecated
    public static int compose(char[] source, char[] target, boolean compat, int options) {
        return compose(source, FOLD_CASE_DEFAULT, source.length, target, FOLD_CASE_DEFAULT, target.length, compat, options);
    }

    @Deprecated
    public static int compose(char[] src, int srcStart, int srcLimit, char[] dest, int destStart, int destLimit, boolean compat, int options) {
        CharSequence srcBuffer = CharBuffer.wrap(src, srcStart, srcLimit - srcStart);
        Appendable app = new CharsAppendable(dest, destStart, destLimit);
        getComposeNormalizer2(compat, options).normalize(srcBuffer, app);
        return app.length();
    }

    @Deprecated
    public static String decompose(String str, boolean compat) {
        return decompose(str, compat, FOLD_CASE_DEFAULT);
    }

    @Deprecated
    public static String decompose(String str, boolean compat, int options) {
        return getDecomposeNormalizer2(compat, options).normalize(str);
    }

    @Deprecated
    public static int decompose(char[] source, char[] target, boolean compat, int options) {
        return decompose(source, FOLD_CASE_DEFAULT, source.length, target, FOLD_CASE_DEFAULT, target.length, compat, options);
    }

    @Deprecated
    public static int decompose(char[] src, int srcStart, int srcLimit, char[] dest, int destStart, int destLimit, boolean compat, int options) {
        CharSequence srcBuffer = CharBuffer.wrap(src, srcStart, srcLimit - srcStart);
        Appendable app = new CharsAppendable(dest, destStart, destLimit);
        getDecomposeNormalizer2(compat, options).normalize(srcBuffer, app);
        return app.length();
    }

    @Deprecated
    public static String normalize(String str, Mode mode, int options) {
        return mode.getNormalizer2(options).normalize(str);
    }

    @Deprecated
    public static String normalize(String src, Mode mode) {
        return normalize(src, mode, (int) FOLD_CASE_DEFAULT);
    }

    @Deprecated
    public static int normalize(char[] source, char[] target, Mode mode, int options) {
        return normalize(source, FOLD_CASE_DEFAULT, source.length, target, FOLD_CASE_DEFAULT, target.length, mode, options);
    }

    @Deprecated
    public static int normalize(char[] src, int srcStart, int srcLimit, char[] dest, int destStart, int destLimit, Mode mode, int options) {
        CharSequence srcBuffer = CharBuffer.wrap(src, srcStart, srcLimit - srcStart);
        Appendable app = new CharsAppendable(dest, destStart, destLimit);
        mode.getNormalizer2(options).normalize(srcBuffer, app);
        return app.length();
    }

    @Deprecated
    public static String normalize(int char32, Mode mode, int options) {
        if (mode != NFD || options != 0) {
            return normalize(UTF16.valueOf(char32), mode, options);
        }
        String decomposition = Normalizer2.getNFCInstance().getDecomposition(char32);
        if (decomposition == null) {
            decomposition = UTF16.valueOf(char32);
        }
        return decomposition;
    }

    @Deprecated
    public static String normalize(int char32, Mode mode) {
        return normalize(char32, mode, (int) FOLD_CASE_DEFAULT);
    }

    @Deprecated
    public static QuickCheckResult quickCheck(String source, Mode mode) {
        return quickCheck(source, mode, (int) FOLD_CASE_DEFAULT);
    }

    @Deprecated
    public static QuickCheckResult quickCheck(String source, Mode mode, int options) {
        return mode.getNormalizer2(options).quickCheck(source);
    }

    @Deprecated
    public static QuickCheckResult quickCheck(char[] source, Mode mode, int options) {
        return quickCheck(source, FOLD_CASE_DEFAULT, source.length, mode, options);
    }

    @Deprecated
    public static QuickCheckResult quickCheck(char[] source, int start, int limit, Mode mode, int options) {
        return mode.getNormalizer2(options).quickCheck(CharBuffer.wrap(source, start, limit - start));
    }

    @Deprecated
    public static boolean isNormalized(char[] src, int start, int limit, Mode mode, int options) {
        return mode.getNormalizer2(options).isNormalized(CharBuffer.wrap(src, start, limit - start));
    }

    @Deprecated
    public static boolean isNormalized(String str, Mode mode, int options) {
        return mode.getNormalizer2(options).isNormalized(str);
    }

    @Deprecated
    public static boolean isNormalized(int char32, Mode mode, int options) {
        return isNormalized(UTF16.valueOf(char32), mode, options);
    }

    public static int compare(char[] s1, int s1Start, int s1Limit, char[] s2, int s2Start, int s2Limit, int options) {
        if (s1 != null && s1Start >= 0 && s1Limit >= 0 && s2 != null && s2Start >= 0 && s2Limit >= 0 && s1Limit >= s1Start && s2Limit >= s2Start) {
            return internalCompare(CharBuffer.wrap(s1, s1Start, s1Limit - s1Start), CharBuffer.wrap(s2, s2Start, s2Limit - s2Start), options);
        }
        throw new IllegalArgumentException();
    }

    public static int compare(String s1, String s2, int options) {
        return internalCompare(s1, s2, options);
    }

    public static int compare(char[] s1, char[] s2, int options) {
        return internalCompare(CharBuffer.wrap(s1), CharBuffer.wrap(s2), options);
    }

    public static int compare(int char32a, int char32b, int options) {
        return internalCompare(UTF16.valueOf(char32a), UTF16.valueOf(char32b), INPUT_IS_FCD | options);
    }

    public static int compare(int char32a, String str2, int options) {
        return internalCompare(UTF16.valueOf(char32a), str2, options);
    }

    @Deprecated
    public static int concatenate(char[] left, int leftStart, int leftLimit, char[] right, int rightStart, int rightLimit, char[] dest, int destStart, int destLimit, Mode mode, int options) {
        if (dest == null) {
            throw new IllegalArgumentException();
        } else if (right != dest || rightStart >= destLimit || destStart >= rightLimit) {
            StringBuilder destBuilder = new StringBuilder((((leftLimit - leftStart) + rightLimit) - rightStart) + 16);
            destBuilder.append(left, leftStart, leftLimit - leftStart);
            mode.getNormalizer2(options).append(destBuilder, CharBuffer.wrap(right, rightStart, rightLimit - rightStart));
            int destLength = destBuilder.length();
            if (destLength <= destLimit - destStart) {
                destBuilder.getChars(FOLD_CASE_DEFAULT, destLength, dest, destStart);
                return destLength;
            }
            throw new IndexOutOfBoundsException(Integer.toString(destLength));
        } else {
            throw new IllegalArgumentException("overlapping right and dst ranges");
        }
    }

    @Deprecated
    public static String concatenate(char[] left, char[] right, Mode mode, int options) {
        return mode.getNormalizer2(options).append(new StringBuilder((left.length + right.length) + 16).append(left), CharBuffer.wrap(right)).toString();
    }

    @Deprecated
    public static String concatenate(String left, String right, Mode mode, int options) {
        return mode.getNormalizer2(options).append(new StringBuilder((left.length() + right.length()) + 16).append(left), right).toString();
    }

    @Deprecated
    public static int getFC_NFKC_Closure(int c, char[] dest) {
        String closure = getFC_NFKC_Closure(c);
        int length = closure.length();
        if (!(length == 0 || dest == null || length > dest.length)) {
            closure.getChars(FOLD_CASE_DEFAULT, length, dest, FOLD_CASE_DEFAULT);
        }
        return length;
    }

    @Deprecated
    public static String getFC_NFKC_Closure(int c) {
        Normalizer2 nfkc = NFKCModeImpl.INSTANCE.normalizer2;
        UCaseProps csp = UCaseProps.INSTANCE;
        StringBuilder folded = new StringBuilder();
        int folded1Length = csp.toFullFolding(c, folded, FOLD_CASE_DEFAULT);
        if (folded1Length < 0) {
            Normalizer2Impl nfkcImpl = ((Normalizer2WithImpl) nfkc).impl;
            if (nfkcImpl.getCompQuickCheck(nfkcImpl.getNorm16(c)) != 0) {
                return XmlPullParser.NO_NAMESPACE;
            }
            folded.appendCodePoint(c);
        } else if (folded1Length > 31) {
            folded.appendCodePoint(folded1Length);
        }
        String kc1 = nfkc.normalize(folded);
        String kc2 = nfkc.normalize(UCharacter.foldCase(kc1, (int) FOLD_CASE_DEFAULT));
        if (kc1.equals(kc2)) {
            return XmlPullParser.NO_NAMESPACE;
        }
        return kc2;
    }

    @Deprecated
    public int current() {
        if (this.bufferPos < this.buffer.length() || nextNormalize()) {
            return this.buffer.codePointAt(this.bufferPos);
        }
        return DONE;
    }

    @Deprecated
    public int next() {
        if (this.bufferPos >= this.buffer.length() && !nextNormalize()) {
            return DONE;
        }
        int c = this.buffer.codePointAt(this.bufferPos);
        this.bufferPos += Character.charCount(c);
        return c;
    }

    @Deprecated
    public int previous() {
        if (this.bufferPos <= 0 && !previousNormalize()) {
            return DONE;
        }
        int c = this.buffer.codePointBefore(this.bufferPos);
        this.bufferPos -= Character.charCount(c);
        return c;
    }

    @Deprecated
    public void reset() {
        this.text.setToStart();
        this.nextIndex = FOLD_CASE_DEFAULT;
        this.currentIndex = FOLD_CASE_DEFAULT;
        clearBuffer();
    }

    @Deprecated
    public void setIndexOnly(int index) {
        this.text.setIndex(index);
        this.nextIndex = index;
        this.currentIndex = index;
        clearBuffer();
    }

    @Deprecated
    public int setIndex(int index) {
        setIndexOnly(index);
        return current();
    }

    @Deprecated
    public int getBeginIndex() {
        return FOLD_CASE_DEFAULT;
    }

    @Deprecated
    public int getEndIndex() {
        return endIndex();
    }

    @Deprecated
    public int first() {
        reset();
        return next();
    }

    @Deprecated
    public int last() {
        this.text.setToLimit();
        int index = this.text.getIndex();
        this.nextIndex = index;
        this.currentIndex = index;
        clearBuffer();
        return previous();
    }

    @Deprecated
    public int getIndex() {
        if (this.bufferPos < this.buffer.length()) {
            return this.currentIndex;
        }
        return this.nextIndex;
    }

    @Deprecated
    public int startIndex() {
        return FOLD_CASE_DEFAULT;
    }

    @Deprecated
    public int endIndex() {
        return this.text.getLength();
    }

    @Deprecated
    public void setMode(Mode newMode) {
        this.mode = newMode;
        this.norm2 = this.mode.getNormalizer2(this.options);
    }

    @Deprecated
    public Mode getMode() {
        return this.mode;
    }

    @Deprecated
    public int getOption(int option) {
        if ((this.options & option) != 0) {
            return IGNORE_HANGUL;
        }
        return FOLD_CASE_DEFAULT;
    }

    @Deprecated
    public int getText(char[] fillIn) {
        return this.text.getText(fillIn);
    }

    @Deprecated
    public int getLength() {
        return this.text.getLength();
    }

    @Deprecated
    public String getText() {
        return this.text.getText();
    }

    @Deprecated
    public void setText(StringBuffer newText) {
        UCharacterIterator newIter = UCharacterIterator.getInstance(newText);
        if (newIter == null) {
            throw new IllegalStateException("Could not create a new UCharacterIterator");
        }
        this.text = newIter;
        reset();
    }

    @Deprecated
    public void setText(char[] newText) {
        UCharacterIterator newIter = UCharacterIterator.getInstance(newText);
        if (newIter == null) {
            throw new IllegalStateException("Could not create a new UCharacterIterator");
        }
        this.text = newIter;
        reset();
    }

    @Deprecated
    public void setText(String newText) {
        UCharacterIterator newIter = UCharacterIterator.getInstance(newText);
        if (newIter == null) {
            throw new IllegalStateException("Could not create a new UCharacterIterator");
        }
        this.text = newIter;
        reset();
    }

    @Deprecated
    public void setText(CharacterIterator newText) {
        UCharacterIterator newIter = UCharacterIterator.getInstance(newText);
        if (newIter == null) {
            throw new IllegalStateException("Could not create a new UCharacterIterator");
        }
        this.text = newIter;
        reset();
    }

    @Deprecated
    public void setText(UCharacterIterator newText) {
        try {
            UCharacterIterator newIter = (UCharacterIterator) newText.clone();
            if (newIter == null) {
                throw new IllegalStateException("Could not create a new UCharacterIterator");
            }
            this.text = newIter;
            reset();
        } catch (CloneNotSupportedException e) {
            throw new ICUCloneNotSupportedException("Could not clone the UCharacterIterator", e);
        }
    }

    private void clearBuffer() {
        this.buffer.setLength(FOLD_CASE_DEFAULT);
        this.bufferPos = FOLD_CASE_DEFAULT;
    }

    private boolean nextNormalize() {
        boolean z = false;
        clearBuffer();
        this.currentIndex = this.nextIndex;
        this.text.setIndex(this.nextIndex);
        int c = this.text.nextCodePoint();
        if (c < 0) {
            return false;
        }
        CharSequence segment = new StringBuilder().appendCodePoint(c);
        while (true) {
            c = this.text.nextCodePoint();
            if (c < 0) {
                break;
            } else if (this.norm2.hasBoundaryBefore(c)) {
                break;
            } else {
                segment.appendCodePoint(c);
            }
        }
        this.text.moveCodePointIndex(DONE);
        this.nextIndex = this.text.getIndex();
        this.norm2.normalize(segment, this.buffer);
        if (this.buffer.length() != 0) {
            z = true;
        }
        return z;
    }

    private boolean previousNormalize() {
        clearBuffer();
        this.nextIndex = this.currentIndex;
        this.text.setIndex(this.currentIndex);
        CharSequence segment = new StringBuilder();
        int c;
        do {
            c = this.text.previousCodePoint();
            if (c < 0) {
                break;
            } else if (c <= DexFormat.MAX_TYPE_IDX) {
                segment.insert(FOLD_CASE_DEFAULT, (char) c);
            } else {
                segment.insert(FOLD_CASE_DEFAULT, Character.toChars(c));
            }
        } while (!this.norm2.hasBoundaryBefore(c));
        this.currentIndex = this.text.getIndex();
        this.norm2.normalize(segment, this.buffer);
        this.bufferPos = this.buffer.length();
        if (this.buffer.length() != 0) {
            return true;
        }
        return false;
    }

    private static int internalCompare(CharSequence s1, CharSequence s2, int options) {
        int normOptions = options >>> COMPARE_NORM_OPTIONS_SHIFT;
        options |= COMPARE_EQUIV;
        if ((INPUT_IS_FCD & options) == 0 || (options & IGNORE_HANGUL) != 0) {
            Normalizer2 n2;
            if ((options & IGNORE_HANGUL) != 0) {
                n2 = NFD.getNormalizer2(normOptions);
            } else {
                n2 = FCD.getNormalizer2(normOptions);
            }
            int spanQCYes1 = n2.spanQuickCheckYes(s1);
            int spanQCYes2 = n2.spanQuickCheckYes(s2);
            if (spanQCYes1 < s1.length()) {
                s1 = n2.normalizeSecondAndAppend(new StringBuilder(s1.length() + 16).append(s1, FOLD_CASE_DEFAULT, spanQCYes1), s1.subSequence(spanQCYes1, s1.length()));
            }
            if (spanQCYes2 < s2.length()) {
                s2 = n2.normalizeSecondAndAppend(new StringBuilder(s2.length() + 16).append(s2, FOLD_CASE_DEFAULT, spanQCYes2), s2.subSequence(spanQCYes2, s2.length()));
            }
        }
        return cmpEquivFold(s1, s2, options);
    }

    private static final CmpEquivLevel[] createCmpEquivLevelStack() {
        return new CmpEquivLevel[]{new CmpEquivLevel(), new CmpEquivLevel()};
    }
}
