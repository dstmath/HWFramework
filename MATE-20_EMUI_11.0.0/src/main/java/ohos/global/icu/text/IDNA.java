package ohos.global.icu.text;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import ohos.global.icu.impl.IDNA2003;
import ohos.global.icu.impl.UTS46;

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
        EMPTY_LABEL,
        LABEL_TOO_LONG,
        DOMAIN_NAME_TOO_LONG,
        LEADING_HYPHEN,
        TRAILING_HYPHEN,
        HYPHEN_3_4,
        LEADING_COMBINING_MARK,
        DISALLOWED,
        PUNYCODE,
        LABEL_HAS_DOT,
        INVALID_ACE_LABEL,
        BIDI,
        CONTEXTJ,
        CONTEXTO_PUNCTUATION,
        CONTEXTO_DIGITS
    }

    public abstract StringBuilder labelToASCII(CharSequence charSequence, StringBuilder sb, Info info);

    public abstract StringBuilder labelToUnicode(CharSequence charSequence, StringBuilder sb, Info info);

    public abstract StringBuilder nameToASCII(CharSequence charSequence, StringBuilder sb, Info info);

    public abstract StringBuilder nameToUnicode(CharSequence charSequence, StringBuilder sb, Info info);

    public static IDNA getUTS46Instance(int i) {
        return new UTS46(i);
    }

    public static final class Info {
        private EnumSet<Error> errors = EnumSet.noneOf(Error.class);
        private boolean isBiDi = false;
        private boolean isOkBiDi = true;
        private boolean isTransDiff = false;
        private EnumSet<Error> labelErrors = EnumSet.noneOf(Error.class);

        public boolean hasErrors() {
            return !this.errors.isEmpty();
        }

        public Set<Error> getErrors() {
            return this.errors;
        }

        public boolean isTransitionalDifferent() {
            return this.isTransDiff;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void reset() {
            this.errors.clear();
            this.labelErrors.clear();
            this.isTransDiff = false;
            this.isBiDi = false;
            this.isOkBiDi = true;
        }
    }

    @Deprecated
    protected static void resetInfo(Info info) {
        info.reset();
    }

    @Deprecated
    protected static boolean hasCertainErrors(Info info, EnumSet<Error> enumSet) {
        return !info.errors.isEmpty() && !Collections.disjoint(info.errors, enumSet);
    }

    @Deprecated
    protected static boolean hasCertainLabelErrors(Info info, EnumSet<Error> enumSet) {
        return !info.labelErrors.isEmpty() && !Collections.disjoint(info.labelErrors, enumSet);
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
    public static StringBuffer convertToASCII(String str, int i) throws StringPrepParseException {
        return convertToASCII(UCharacterIterator.getInstance(str), i);
    }

    @Deprecated
    public static StringBuffer convertToASCII(StringBuffer stringBuffer, int i) throws StringPrepParseException {
        return convertToASCII(UCharacterIterator.getInstance(stringBuffer), i);
    }

    @Deprecated
    public static StringBuffer convertToASCII(UCharacterIterator uCharacterIterator, int i) throws StringPrepParseException {
        return IDNA2003.convertToASCII(uCharacterIterator, i);
    }

    @Deprecated
    public static StringBuffer convertIDNToASCII(UCharacterIterator uCharacterIterator, int i) throws StringPrepParseException {
        return convertIDNToASCII(uCharacterIterator.getText(), i);
    }

    @Deprecated
    public static StringBuffer convertIDNToASCII(StringBuffer stringBuffer, int i) throws StringPrepParseException {
        return convertIDNToASCII(stringBuffer.toString(), i);
    }

    @Deprecated
    public static StringBuffer convertIDNToASCII(String str, int i) throws StringPrepParseException {
        return IDNA2003.convertIDNToASCII(str, i);
    }

    @Deprecated
    public static StringBuffer convertToUnicode(String str, int i) throws StringPrepParseException {
        return convertToUnicode(UCharacterIterator.getInstance(str), i);
    }

    @Deprecated
    public static StringBuffer convertToUnicode(StringBuffer stringBuffer, int i) throws StringPrepParseException {
        return convertToUnicode(UCharacterIterator.getInstance(stringBuffer), i);
    }

    @Deprecated
    public static StringBuffer convertToUnicode(UCharacterIterator uCharacterIterator, int i) throws StringPrepParseException {
        return IDNA2003.convertToUnicode(uCharacterIterator, i);
    }

    @Deprecated
    public static StringBuffer convertIDNToUnicode(UCharacterIterator uCharacterIterator, int i) throws StringPrepParseException {
        return convertIDNToUnicode(uCharacterIterator.getText(), i);
    }

    @Deprecated
    public static StringBuffer convertIDNToUnicode(StringBuffer stringBuffer, int i) throws StringPrepParseException {
        return convertIDNToUnicode(stringBuffer.toString(), i);
    }

    @Deprecated
    public static StringBuffer convertIDNToUnicode(String str, int i) throws StringPrepParseException {
        return IDNA2003.convertIDNToUnicode(str, i);
    }

    @Deprecated
    public static int compare(StringBuffer stringBuffer, StringBuffer stringBuffer2, int i) throws StringPrepParseException {
        if (stringBuffer != null && stringBuffer2 != null) {
            return IDNA2003.compare(stringBuffer.toString(), stringBuffer2.toString(), i);
        }
        throw new IllegalArgumentException("One of the source buffers is null");
    }

    @Deprecated
    public static int compare(String str, String str2, int i) throws StringPrepParseException {
        if (str != null && str2 != null) {
            return IDNA2003.compare(str, str2, i);
        }
        throw new IllegalArgumentException("One of the source buffers is null");
    }

    @Deprecated
    public static int compare(UCharacterIterator uCharacterIterator, UCharacterIterator uCharacterIterator2, int i) throws StringPrepParseException {
        if (uCharacterIterator != null && uCharacterIterator2 != null) {
            return IDNA2003.compare(uCharacterIterator.getText(), uCharacterIterator2.getText(), i);
        }
        throw new IllegalArgumentException("One of the source buffers is null");
    }
}
