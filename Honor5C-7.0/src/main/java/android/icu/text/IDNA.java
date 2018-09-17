package android.icu.text;

import android.icu.impl.IDNA2003;
import android.icu.impl.UTS46;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public abstract class IDNA {
    @Deprecated
    public static final int ALLOW_UNASSIGNED = 1;
    public static final int CHECK_BIDI = 4;
    public static final int CHECK_CONTEXTJ = 8;
    public static final int CHECK_CONTEXTO = 64;
    public static final int DEFAULT = 0;
    public static final int NONTRANSITIONAL_TO_ASCII = 16;
    public static final int NONTRANSITIONAL_TO_UNICODE = 32;
    public static final int USE_STD3_RULES = 2;

    public enum Error {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.IDNA.Error.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.IDNA.Error.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.IDNA.Error.<clinit>():void");
        }
    }

    public static final class Info {
        private EnumSet<Error> errors;
        private boolean isBiDi;
        private boolean isOkBiDi;
        private boolean isTransDiff;
        private EnumSet<Error> labelErrors;

        public Info() {
            this.errors = EnumSet.noneOf(Error.class);
            this.labelErrors = EnumSet.noneOf(Error.class);
            this.isTransDiff = false;
            this.isBiDi = false;
            this.isOkBiDi = true;
        }

        public boolean hasErrors() {
            return !this.errors.isEmpty();
        }

        public Set<Error> getErrors() {
            return this.errors;
        }

        public boolean isTransitionalDifferent() {
            return this.isTransDiff;
        }

        private void reset() {
            this.errors.clear();
            this.labelErrors.clear();
            this.isTransDiff = false;
            this.isBiDi = false;
            this.isOkBiDi = true;
        }
    }

    public abstract StringBuilder labelToASCII(CharSequence charSequence, StringBuilder stringBuilder, Info info);

    public abstract StringBuilder labelToUnicode(CharSequence charSequence, StringBuilder stringBuilder, Info info);

    public abstract StringBuilder nameToASCII(CharSequence charSequence, StringBuilder stringBuilder, Info info);

    public abstract StringBuilder nameToUnicode(CharSequence charSequence, StringBuilder stringBuilder, Info info);

    public static IDNA getUTS46Instance(int options) {
        return new UTS46(options);
    }

    @Deprecated
    protected static void resetInfo(Info info) {
        info.reset();
    }

    @Deprecated
    protected static boolean hasCertainErrors(Info info, EnumSet<Error> errors) {
        return (info.errors.isEmpty() || Collections.disjoint(info.errors, errors)) ? false : true;
    }

    @Deprecated
    protected static boolean hasCertainLabelErrors(Info info, EnumSet<Error> errors) {
        return (info.labelErrors.isEmpty() || Collections.disjoint(info.labelErrors, errors)) ? false : true;
    }

    @Deprecated
    protected static void addLabelError(Info info, Error error) {
        info.labelErrors.add(error);
    }

    @Deprecated
    protected static void promoteAndResetLabelErrors(Info info) {
        if (!info.labelErrors.isEmpty()) {
            info.errors.addAll(info.labelErrors);
            info.labelErrors.clear();
        }
    }

    @Deprecated
    protected static void addError(Info info, Error error) {
        info.errors.add(error);
    }

    @Deprecated
    protected static void setTransitionalDifferent(Info info) {
        info.isTransDiff = true;
    }

    @Deprecated
    protected static void setBiDi(Info info) {
        info.isBiDi = true;
    }

    @Deprecated
    protected static boolean isBiDi(Info info) {
        return info.isBiDi;
    }

    @Deprecated
    protected static void setNotOkBiDi(Info info) {
        info.isOkBiDi = false;
    }

    @Deprecated
    protected static boolean isOkBiDi(Info info) {
        return info.isOkBiDi;
    }

    @Deprecated
    protected IDNA() {
    }

    @Deprecated
    public static StringBuffer convertToASCII(String src, int options) throws StringPrepParseException {
        return convertToASCII(UCharacterIterator.getInstance(src), options);
    }

    @Deprecated
    public static StringBuffer convertToASCII(StringBuffer src, int options) throws StringPrepParseException {
        return convertToASCII(UCharacterIterator.getInstance(src), options);
    }

    @Deprecated
    public static StringBuffer convertToASCII(UCharacterIterator src, int options) throws StringPrepParseException {
        return IDNA2003.convertToASCII(src, options);
    }

    @Deprecated
    public static StringBuffer convertIDNToASCII(UCharacterIterator src, int options) throws StringPrepParseException {
        return convertIDNToASCII(src.getText(), options);
    }

    @Deprecated
    public static StringBuffer convertIDNToASCII(StringBuffer src, int options) throws StringPrepParseException {
        return convertIDNToASCII(src.toString(), options);
    }

    @Deprecated
    public static StringBuffer convertIDNToASCII(String src, int options) throws StringPrepParseException {
        return IDNA2003.convertIDNToASCII(src, options);
    }

    @Deprecated
    public static StringBuffer convertToUnicode(String src, int options) throws StringPrepParseException {
        return convertToUnicode(UCharacterIterator.getInstance(src), options);
    }

    @Deprecated
    public static StringBuffer convertToUnicode(StringBuffer src, int options) throws StringPrepParseException {
        return convertToUnicode(UCharacterIterator.getInstance(src), options);
    }

    @Deprecated
    public static StringBuffer convertToUnicode(UCharacterIterator src, int options) throws StringPrepParseException {
        return IDNA2003.convertToUnicode(src, options);
    }

    @Deprecated
    public static StringBuffer convertIDNToUnicode(UCharacterIterator src, int options) throws StringPrepParseException {
        return convertIDNToUnicode(src.getText(), options);
    }

    @Deprecated
    public static StringBuffer convertIDNToUnicode(StringBuffer src, int options) throws StringPrepParseException {
        return convertIDNToUnicode(src.toString(), options);
    }

    @Deprecated
    public static StringBuffer convertIDNToUnicode(String src, int options) throws StringPrepParseException {
        return IDNA2003.convertIDNToUnicode(src, options);
    }

    @Deprecated
    public static int compare(StringBuffer s1, StringBuffer s2, int options) throws StringPrepParseException {
        if (s1 != null && s2 != null) {
            return IDNA2003.compare(s1.toString(), s2.toString(), options);
        }
        throw new IllegalArgumentException("One of the source buffers is null");
    }

    @Deprecated
    public static int compare(String s1, String s2, int options) throws StringPrepParseException {
        if (s1 != null && s2 != null) {
            return IDNA2003.compare(s1, s2, options);
        }
        throw new IllegalArgumentException("One of the source buffers is null");
    }

    @Deprecated
    public static int compare(UCharacterIterator s1, UCharacterIterator s2, int options) throws StringPrepParseException {
        if (s1 != null && s2 != null) {
            return IDNA2003.compare(s1.getText(), s2.getText(), options);
        }
        throw new IllegalArgumentException("One of the source buffers is null");
    }
}
