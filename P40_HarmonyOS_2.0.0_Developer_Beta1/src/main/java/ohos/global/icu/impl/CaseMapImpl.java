package ohos.global.icu.impl;

import java.io.IOException;
import java.text.CharacterIterator;
import java.util.Locale;
import ohos.global.icu.impl.UCaseProps;
import ohos.global.icu.text.BreakIterator;
import ohos.global.icu.text.Edits;
import ohos.global.icu.util.ICUUncheckedIOException;
import ohos.global.icu.util.ULocale;

public final class CaseMapImpl {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final Trie2_16 CASE_TRIE = UCaseProps.getTrie();
    private static final int LNS = 251792942;
    public static final int OMIT_UNCHANGED_TEXT = 16384;
    private static final int TITLECASE_ADJUSTMENT_MASK = 1536;
    public static final int TITLECASE_ADJUST_TO_CASED = 1024;
    private static final int TITLECASE_ITERATOR_MASK = 224;
    public static final int TITLECASE_SENTENCES = 64;
    public static final int TITLECASE_WHOLE_STRING = 32;

    public static final class StringContextIterator implements UCaseProps.ContextIterator {
        protected int cpLimit;
        protected int cpStart;
        protected int dir;
        protected int index;
        protected int limit;
        protected CharSequence s;

        public StringContextIterator(CharSequence charSequence) {
            this.s = charSequence;
            this.limit = charSequence.length();
            this.index = 0;
            this.cpLimit = 0;
            this.cpStart = 0;
            this.dir = 0;
        }

        public StringContextIterator(CharSequence charSequence, int i, int i2) {
            this.s = charSequence;
            this.index = 0;
            this.limit = charSequence.length();
            this.cpStart = i;
            this.cpLimit = i2;
            this.dir = 0;
        }

        public void setLimit(int i) {
            if (i < 0 || i > this.s.length()) {
                this.limit = this.s.length();
            } else {
                this.limit = i;
            }
        }

        public void moveToLimit() {
            int i = this.limit;
            this.cpLimit = i;
            this.cpStart = i;
        }

        public int nextCaseMapCP() {
            int i = this.cpLimit;
            this.cpStart = i;
            if (i >= this.limit) {
                return -1;
            }
            int codePointAt = Character.codePointAt(this.s, i);
            this.cpLimit += Character.charCount(codePointAt);
            return codePointAt;
        }

        public void setCPStartAndLimit(int i, int i2) {
            this.cpStart = i;
            this.cpLimit = i2;
            this.dir = 0;
        }

        public int getCPStart() {
            return this.cpStart;
        }

        public int getCPLimit() {
            return this.cpLimit;
        }

        public int getCPLength() {
            return this.cpLimit - this.cpStart;
        }

        @Override // ohos.global.icu.impl.UCaseProps.ContextIterator
        public void reset(int i) {
            if (i > 0) {
                this.dir = 1;
                this.index = this.cpLimit;
            } else if (i < 0) {
                this.dir = -1;
                this.index = this.cpStart;
            } else {
                this.dir = 0;
                this.index = 0;
            }
        }

        @Override // ohos.global.icu.impl.UCaseProps.ContextIterator
        public int next() {
            int i;
            if (this.dir > 0 && this.index < this.s.length()) {
                int codePointAt = Character.codePointAt(this.s, this.index);
                this.index += Character.charCount(codePointAt);
                return codePointAt;
            } else if (this.dir >= 0 || (i = this.index) <= 0) {
                return -1;
            } else {
                int codePointBefore = Character.codePointBefore(this.s, i);
                this.index -= Character.charCount(codePointBefore);
                return codePointBefore;
            }
        }
    }

    public static int addTitleAdjustmentOption(int i, int i2) {
        int i3 = i & 1536;
        if (i3 == 0 || i3 == i2) {
            return i | i2;
        }
        throw new IllegalArgumentException("multiple titlecasing index adjustment options");
    }

    private static boolean isLNS(int i) {
        int type = UCharacterProperty.INSTANCE.getType(i);
        if (((1 << type) & LNS) != 0) {
            return true;
        }
        if (type != 4 || UCaseProps.INSTANCE.getType(i) == 0) {
            return false;
        }
        return true;
    }

    public static int addTitleIteratorOption(int i, int i2) {
        int i3 = i & 224;
        if (i3 == 0 || i3 == i2) {
            return i | i2;
        }
        throw new IllegalArgumentException("multiple titlecasing iterator options");
    }

    public static BreakIterator getTitleBreakIterator(Locale locale, int i, BreakIterator breakIterator) {
        int i2 = i & 224;
        if (i2 != 0 && breakIterator != null) {
            throw new IllegalArgumentException("titlecasing iterator option together with an explicit iterator");
        } else if (breakIterator != null) {
            return breakIterator;
        } else {
            if (i2 == 0) {
                return BreakIterator.getWordInstance(locale);
            }
            if (i2 == 32) {
                return new WholeStringBreakIterator();
            }
            if (i2 == 64) {
                return BreakIterator.getSentenceInstance(locale);
            }
            throw new IllegalArgumentException("unknown titlecasing iterator option");
        }
    }

    public static BreakIterator getTitleBreakIterator(ULocale uLocale, int i, BreakIterator breakIterator) {
        int i2 = i & 224;
        if (i2 != 0 && breakIterator != null) {
            throw new IllegalArgumentException("titlecasing iterator option together with an explicit iterator");
        } else if (breakIterator != null) {
            return breakIterator;
        } else {
            if (i2 == 0) {
                return BreakIterator.getWordInstance(uLocale);
            }
            if (i2 == 32) {
                return new WholeStringBreakIterator();
            }
            if (i2 == 64) {
                return BreakIterator.getSentenceInstance(uLocale);
            }
            throw new IllegalArgumentException("unknown titlecasing iterator option");
        }
    }

    /* access modifiers changed from: private */
    public static final class WholeStringBreakIterator extends BreakIterator {
        private int length;

        public int first() {
            return 0;
        }

        private WholeStringBreakIterator() {
        }

        private static void notImplemented() {
            throw new UnsupportedOperationException("should not occur");
        }

        public int last() {
            notImplemented();
            return 0;
        }

        public int next(int i) {
            notImplemented();
            return 0;
        }

        public int next() {
            return this.length;
        }

        public int previous() {
            notImplemented();
            return 0;
        }

        public int following(int i) {
            notImplemented();
            return 0;
        }

        public int current() {
            notImplemented();
            return 0;
        }

        public CharacterIterator getText() {
            notImplemented();
            return null;
        }

        public void setText(CharacterIterator characterIterator) {
            this.length = characterIterator.getEndIndex();
        }

        public void setText(CharSequence charSequence) {
            this.length = charSequence.length();
        }

        public void setText(String str) {
            this.length = str.length();
        }
    }

    private static int appendCodePoint(Appendable appendable, int i) throws IOException {
        if (i <= 65535) {
            appendable.append((char) i);
            return 1;
        }
        appendable.append((char) ((i >> 10) + 55232));
        appendable.append((char) ((i & UCharacterProperty.MAX_SCRIPT) + 56320));
        return 2;
    }

    /* access modifiers changed from: private */
    public static void appendResult(int i, Appendable appendable, int i2, int i3, Edits edits) throws IOException {
        if (i < 0) {
            if (edits != null) {
                edits.addUnchanged(i2);
            }
            if ((i3 & 16384) == 0) {
                appendCodePoint(appendable, ~i);
            }
        } else if (i > 31) {
            int appendCodePoint = appendCodePoint(appendable, i);
            if (edits != null) {
                edits.addReplace(i2, appendCodePoint);
            }
        } else if (edits != null) {
            edits.addReplace(i2, i);
        }
    }

    private static final void appendUnchanged(CharSequence charSequence, int i, int i2, Appendable appendable, int i3, Edits edits) throws IOException {
        if (i2 > 0) {
            if (edits != null) {
                edits.addUnchanged(i2);
            }
            if ((i3 & 16384) == 0) {
                appendable.append(charSequence, i, i2 + i);
            }
        }
    }

    private static String applyEdits(CharSequence charSequence, StringBuilder sb, Edits edits) {
        if (!edits.hasChanges()) {
            return charSequence.toString();
        }
        StringBuilder sb2 = new StringBuilder(charSequence.length() + edits.lengthDelta());
        Edits.Iterator coarseIterator = edits.getCoarseIterator();
        while (coarseIterator.next()) {
            if (coarseIterator.hasChange()) {
                int replacementIndex = coarseIterator.replacementIndex();
                sb2.append((CharSequence) sb, replacementIndex, coarseIterator.newLength() + replacementIndex);
            } else {
                int sourceIndex = coarseIterator.sourceIndex();
                sb2.append(charSequence, sourceIndex, coarseIterator.oldLength() + sourceIndex);
            }
        }
        return sb2.toString();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00c1, code lost:
        if (r3 == 0) goto L_0x00e4;
     */
    private static void internalToLower(int i, int i2, CharSequence charSequence, int i3, int i4, StringContextIterator stringContextIterator, Appendable appendable, Edits edits) throws IOException {
        byte[] bArr;
        int i5;
        int i6;
        int i7 = i4;
        if (i == 1 || (i < 0 ? (i2 & 7) == 0 : !(i == 2 || i == 3))) {
            bArr = UCaseProps.LatinCase.TO_LOWER_NORMAL;
        } else {
            bArr = UCaseProps.LatinCase.TO_LOWER_TR_LT;
        }
        int i8 = i3;
        int i9 = i8;
        StringContextIterator stringContextIterator2 = stringContextIterator;
        while (i9 < i7) {
            char charAt = charSequence.charAt(i9);
            if (charAt < 383) {
                byte b = bArr[charAt];
                if (b != Byte.MIN_VALUE) {
                    i9++;
                    i5 = b;
                    if (b == 0) {
                    }
                    appendUnchanged(charSequence, i8, (i9 - 1) - i8, appendable, i2, edits);
                    appendable.append((char) (charAt + i5));
                    if (edits != null) {
                        edits.addReplace(1, 1);
                    }
                    i7 = i4;
                    i8 = i9;
                }
            } else if (charAt < 55296) {
                int fromU16SingleLead = CASE_TRIE.getFromU16SingleLead(charAt);
                if (!UCaseProps.propsHasException(fromU16SingleLead)) {
                    i9++;
                    if (UCaseProps.isUpperOrTitleFromProps(fromU16SingleLead)) {
                        int delta = UCaseProps.getDelta(fromU16SingleLead);
                        i5 = delta;
                    }
                    i7 = i4;
                }
            }
            int i10 = i9 + 1;
            boolean isHighSurrogate = Character.isHighSurrogate(charAt);
            int i11 = charAt;
            i11 = charAt;
            if (isHighSurrogate && i10 < i7) {
                char charAt2 = charSequence.charAt(i10);
                i11 = charAt;
                if (Character.isLowSurrogate(charAt2)) {
                    i10++;
                    i11 = Character.toCodePoint(charAt, charAt2);
                }
            }
            appendUnchanged(charSequence, i8, i9 - i8, appendable, i2, edits);
            if (i >= 0) {
                if (stringContextIterator2 == null) {
                    stringContextIterator2 = new StringContextIterator(charSequence, i9, i10);
                } else {
                    stringContextIterator2.setCPStartAndLimit(i9, i10);
                }
                i6 = UCaseProps.INSTANCE.toFullLower(i11, stringContextIterator2, appendable, i);
            } else {
                i6 = UCaseProps.INSTANCE.toFullFolding(i11 == 1 ? 1 : 0, appendable, i2);
            }
            if (i6 >= 0) {
                appendResult(i6, appendable, i10 - i9, i2, edits);
                i8 = i10;
            } else {
                i8 = i9;
            }
            i7 = i4;
            i9 = i10;
        }
        appendUnchanged(charSequence, i8, i9 - i8, appendable, i2, edits);
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:47:0x00b5 */
    /* JADX DEBUG: Multi-variable search result rejected for r1v1, resolved type: byte[] */
    /* JADX DEBUG: Multi-variable search result rejected for r3v13, resolved type: byte */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x00cc  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x0019 A[SYNTHETIC] */
    private static void internalToUpper(int i, int i2, CharSequence charSequence, Appendable appendable, Edits edits) throws IOException {
        byte[] bArr;
        byte b;
        if (i == 2) {
            bArr = UCaseProps.LatinCase.TO_UPPER_TR;
        } else {
            bArr = UCaseProps.LatinCase.TO_UPPER_NORMAL;
        }
        int length = charSequence.length();
        int i3 = 0;
        StringContextIterator stringContextIterator = null;
        while (true) {
            int i4 = i3;
            while (i3 < length) {
                char charAt = charSequence.charAt(i3);
                if (charAt < 383) {
                    b = bArr[charAt];
                    if (b != -128) {
                        i3++;
                        if (b != 0) {
                            appendUnchanged(charSequence, i4, (i3 - 1) - i4, appendable, i2, edits);
                            appendable.append((char) (charAt + b));
                            if (edits == null) {
                                edits.addReplace(1, 1);
                            }
                        }
                    }
                } else if (charAt < 55296) {
                    int fromU16SingleLead = CASE_TRIE.getFromU16SingleLead(charAt);
                    if (!UCaseProps.propsHasException(fromU16SingleLead)) {
                        i3++;
                        if (UCaseProps.getTypeFromProps(fromU16SingleLead) == 1 && (b = UCaseProps.getDelta(fromU16SingleLead)) != 0) {
                            appendUnchanged(charSequence, i4, (i3 - 1) - i4, appendable, i2, edits);
                            appendable.append((char) (charAt + b));
                            if (edits == null) {
                            }
                        }
                    }
                }
                int i5 = i3 + 1;
                boolean isHighSurrogate = Character.isHighSurrogate(charAt);
                int i6 = charAt;
                i6 = charAt;
                if (isHighSurrogate && i5 < length) {
                    char charAt2 = charSequence.charAt(i5);
                    i6 = charAt;
                    if (Character.isLowSurrogate(charAt2)) {
                        i5++;
                        i6 = Character.toCodePoint(charAt, charAt2);
                    }
                }
                if (stringContextIterator == null) {
                    stringContextIterator = new StringContextIterator(charSequence, i3, i5);
                } else {
                    stringContextIterator.setCPStartAndLimit(i3, i5);
                }
                appendUnchanged(charSequence, i4, i3 - i4, appendable, i2, edits);
                int fullUpper = UCaseProps.INSTANCE.toFullUpper(i6 == 1 ? 1 : 0, stringContextIterator, appendable, i);
                if (fullUpper >= 0) {
                    appendResult(fullUpper, appendable, i5 - i3, i2, edits);
                    i4 = i5;
                } else {
                    i4 = i3;
                }
                i3 = i5;
            }
            appendUnchanged(charSequence, i4, i3 - i4, appendable, i2, edits);
            return;
        }
    }

    public static String toLower(int i, int i2, CharSequence charSequence) {
        if (charSequence.length() > 100 || (i2 & 16384) != 0) {
            return ((StringBuilder) toLower(i, i2, charSequence, new StringBuilder(charSequence.length()), null)).toString();
        }
        if (charSequence.length() == 0) {
            return charSequence.toString();
        }
        Edits edits = new Edits();
        return applyEdits(charSequence, (StringBuilder) toLower(i, i2 | 16384, charSequence, new StringBuilder(), edits), edits);
    }

    public static <A extends Appendable> A toLower(int i, int i2, CharSequence charSequence, A a, Edits edits) {
        if (edits != null) {
            try {
                edits.reset();
            } catch (IOException e) {
                throw new ICUUncheckedIOException(e);
            }
        }
        internalToLower(i, i2, charSequence, 0, charSequence.length(), null, a, edits);
        return a;
    }

    public static String toUpper(int i, int i2, CharSequence charSequence) {
        if (charSequence.length() > 100 || (i2 & 16384) != 0) {
            return ((StringBuilder) toUpper(i, i2, charSequence, new StringBuilder(charSequence.length()), null)).toString();
        }
        if (charSequence.length() == 0) {
            return charSequence.toString();
        }
        Edits edits = new Edits();
        return applyEdits(charSequence, (StringBuilder) toUpper(i, i2 | 16384, charSequence, new StringBuilder(), edits), edits);
    }

    public static <A extends Appendable> A toUpper(int i, int i2, CharSequence charSequence, A a, Edits edits) {
        if (edits != null) {
            try {
                edits.reset();
            } catch (IOException e) {
                throw new ICUUncheckedIOException(e);
            }
        }
        if (i == 4) {
            return (A) GreekUpper.toUpper(i2, charSequence, a, edits);
        }
        internalToUpper(i, i2, charSequence, a, edits);
        return a;
    }

    public static String toTitle(int i, int i2, BreakIterator breakIterator, CharSequence charSequence) {
        if (charSequence.length() > 100 || (i2 & 16384) != 0) {
            return ((StringBuilder) toTitle(i, i2, breakIterator, charSequence, new StringBuilder(charSequence.length()), null)).toString();
        }
        if (charSequence.length() == 0) {
            return charSequence.toString();
        }
        Edits edits = new Edits();
        return applyEdits(charSequence, (StringBuilder) toTitle(i, i2 | 16384, breakIterator, charSequence, new StringBuilder(), edits), edits);
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x0039 A[Catch:{ IOException -> 0x0112 }] */
    public static <A extends Appendable> A toTitle(int i, int i2, BreakIterator breakIterator, CharSequence charSequence, A a, Edits edits) {
        boolean z;
        int i3;
        int i4;
        int i5;
        int i6;
        char charAt;
        int i7;
        if (edits != null) {
            try {
                edits.reset();
            } catch (IOException e) {
                throw new ICUUncheckedIOException(e);
            }
        }
        StringContextIterator stringContextIterator = new StringContextIterator(charSequence);
        int length = charSequence.length();
        int i8 = 1;
        boolean z2 = true;
        int i9 = 0;
        while (i9 < length) {
            if (z2) {
                i3 = breakIterator.first();
                z = false;
            } else {
                z = z2;
                i3 = breakIterator.next();
            }
            if (i3 != -1) {
                if (i3 <= length) {
                    i4 = i3;
                    if (i9 < i4) {
                        stringContextIterator.setLimit(i4);
                        int nextCaseMapCP = stringContextIterator.nextCaseMapCP();
                        if ((i2 & 512) == 0) {
                            int i10 = (i2 & 1024) != 0 ? i8 : 0;
                            while (true) {
                                if (i10 == 0) {
                                    if (isLNS(nextCaseMapCP)) {
                                        break;
                                    }
                                } else if (UCaseProps.INSTANCE.getType(nextCaseMapCP) != 0) {
                                    break;
                                }
                                nextCaseMapCP = stringContextIterator.nextCaseMapCP();
                                if (nextCaseMapCP < 0) {
                                    break;
                                }
                            }
                            int cPStart = stringContextIterator.getCPStart();
                            if (i9 < cPStart) {
                                i7 = cPStart;
                                appendUnchanged(charSequence, i9, cPStart - i9, a, i2, edits);
                            } else {
                                i7 = cPStart;
                            }
                            nextCaseMapCP = nextCaseMapCP;
                            i9 = i7;
                        }
                        if (i9 < i4) {
                            int cPLimit = stringContextIterator.getCPLimit();
                            appendResult(UCaseProps.INSTANCE.toFullTitle(nextCaseMapCP, stringContextIterator, a, i), a, stringContextIterator.getCPLength(), i2, edits);
                            int i11 = i9 + 1;
                            if (i11 < i4 && i == 5 && ((charAt = charSequence.charAt(i9)) == 'i' || charAt == 'I')) {
                                char charAt2 = charSequence.charAt(i11);
                                if (charAt2 == 'j') {
                                    a.append('J');
                                    if (edits != null) {
                                        edits.addReplace(i8, i8);
                                    }
                                    stringContextIterator.nextCaseMapCP();
                                } else if (charAt2 == 'J') {
                                    appendUnchanged(charSequence, i11, 1, a, i2, edits);
                                    stringContextIterator.nextCaseMapCP();
                                }
                                cPLimit++;
                            }
                            if (cPLimit < i4) {
                                if ((i2 & 256) == 0) {
                                    i6 = i4;
                                    i5 = i8;
                                    internalToLower(i, i2, charSequence, cPLimit, i4, stringContextIterator, a, edits);
                                } else {
                                    i6 = i4;
                                    i5 = i8;
                                    appendUnchanged(charSequence, cPLimit, i6 - cPLimit, a, i2, edits);
                                }
                                stringContextIterator.moveToLimit();
                                z2 = z;
                                i9 = i6;
                                i8 = i5;
                            }
                        }
                    }
                    i6 = i4;
                    i5 = i8;
                    z2 = z;
                    i9 = i6;
                    i8 = i5;
                }
            }
            i4 = length;
            if (i9 < i4) {
            }
            i6 = i4;
            i5 = i8;
            z2 = z;
            i9 = i6;
            i8 = i5;
        }
        return a;
    }

    public static String fold(int i, CharSequence charSequence) {
        if (charSequence.length() > 100 || (i & 16384) != 0) {
            return ((StringBuilder) fold(i, charSequence, new StringBuilder(charSequence.length()), null)).toString();
        }
        if (charSequence.length() == 0) {
            return charSequence.toString();
        }
        Edits edits = new Edits();
        return applyEdits(charSequence, (StringBuilder) fold(i | 16384, charSequence, new StringBuilder(), edits), edits);
    }

    public static <A extends Appendable> A fold(int i, CharSequence charSequence, A a, Edits edits) {
        if (edits != null) {
            try {
                edits.reset();
            } catch (IOException e) {
                throw new ICUUncheckedIOException(e);
            }
        }
        internalToLower(-1, i, charSequence, 0, charSequence.length(), null, a, edits);
        return a;
    }

    /* access modifiers changed from: private */
    public static final class GreekUpper {
        private static final int AFTER_CASED = 1;
        private static final int AFTER_VOWEL_WITH_ACCENT = 2;
        private static final int HAS_ACCENT = 16384;
        private static final int HAS_COMBINING_DIALYTIKA = 65536;
        private static final int HAS_DIALYTIKA = 32768;
        private static final int HAS_EITHER_DIALYTIKA = 98304;
        private static final int HAS_OTHER_GREEK_DIACRITIC = 131072;
        private static final int HAS_VOWEL = 4096;
        private static final int HAS_VOWEL_AND_ACCENT = 20480;
        private static final int HAS_VOWEL_AND_ACCENT_AND_DIALYTIKA = 53248;
        private static final int HAS_YPOGEGRAMMENI = 8192;
        private static final int UPPER_MASK = 1023;
        private static final char[] data0370 = {880, 880, 882, 882, 0, 0, 886, 886, 0, 0, 890, 1021, 1022, 1023, 0, 895, 0, 0, 0, 0, 0, 0, 21393, 0, 21397, 21399, 21401, 0, 21407, 0, 21413, 21417, 54169, 5009, 914, 915, 916, 5013, 918, 5015, 920, 5017, 922, 923, 924, 925, 926, 5023, 928, 929, 0, 931, 932, 5029, 934, 935, 936, data2126, 37785, 37797, 21393, 21397, 21399, 21401, 54181, 5009, 914, 915, 916, 5013, 918, 5015, 920, 5017, 922, 923, 924, 925, 926, 5023, 928, 929, 931, 931, 932, 5029, 934, 935, 936, data2126, 37785, 37797, 21407, 21413, 21417, 975, 914, 920, 978, 17362, 33746, 934, 928, 975, 984, 984, 986, 986, 988, 988, 990, 990, 992, 992, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 922, 929, 1017, 895, 1012, 5013, 0, 1015, 1015, 1017, 1018, 1018, 1020, 1021, 1022, 1023};
        private static final char[] data1F00 = {5009, 5009, 21393, 21393, 21393, 21393, 21393, 21393, 5009, 5009, 21393, 21393, 21393, 21393, 21393, 21393, 5013, 5013, 21397, 21397, 21397, 21397, 0, 0, 5013, 5013, 21397, 21397, 21397, 21397, 0, 0, 5015, 5015, 21399, 21399, 21399, 21399, 21399, 21399, 5015, 5015, 21399, 21399, 21399, 21399, 21399, 21399, 5017, 5017, 21401, 21401, 21401, 21401, 21401, 21401, 5017, 5017, 21401, 21401, 21401, 21401, 21401, 21401, 5023, 5023, 21407, 21407, 21407, 21407, 0, 0, 5023, 5023, 21407, 21407, 21407, 21407, 0, 0, 5029, 5029, 21413, 21413, 21413, 21413, 21413, 21413, 0, 5029, 0, 21413, 0, 21413, 0, 21413, data2126, data2126, 21417, 21417, 21417, 21417, 21417, 21417, data2126, data2126, 21417, 21417, 21417, 21417, 21417, 21417, 21393, 21393, 21397, 21397, 21399, 21399, 21401, 21401, 21407, 21407, 21413, 21413, 21417, 21417, 0, 0, 13201, 13201, 29585, 29585, 29585, 29585, 29585, 29585, 13201, 13201, 29585, 29585, 29585, 29585, 29585, 29585, 13207, 13207, 29591, 29591, 29591, 29591, 29591, 29591, 13207, 13207, 29591, 29591, 29591, 29591, 29591, 29591, 13225, 13225, 29609, 29609, 29609, 29609, 29609, 29609, 13225, 13225, 29609, 29609, 29609, 29609, 29609, 29609, 5009, 5009, 29585, 13201, 29585, 0, 21393, 29585, 5009, 5009, 21393, 21393, 13201, 0, 5017, 0, 0, 0, 29591, 13207, 29591, 0, 21399, 29591, 21397, 21397, 21399, 21399, 13207, 0, 0, 0, 5017, 5017, 54169, 54169, 0, 0, 21401, 54169, 5017, 5017, 21401, 21401, 0, 0, 0, 0, 5029, 5029, 54181, 54181, 929, 929, 21413, 54181, 5029, 5029, 21413, 21413, 929, 0, 0, 0, 0, 0, 29609, 13225, 29609, 0, 21417, 29609, 21407, 21407, 21417, 21417, 13225, 0, 0, 0};
        private static final char data2126 = 5033;

        private static final int getDiacriticData(int i) {
            if (i == 774) {
                return 131072;
            }
            if (i == 776) {
                return 65536;
            }
            if (i == 785) {
                return 16384;
            }
            if (i == 787 || i == 788) {
                return 131072;
            }
            switch (i) {
                case 768:
                case 769:
                case 770:
                case 771:
                    return 16384;
                case 772:
                    return 131072;
                default:
                    switch (i) {
                        case 834:
                            return 16384;
                        case 835:
                            return 131072;
                        case 836:
                            return 81920;
                        case 837:
                            return 8192;
                        default:
                            return 0;
                    }
            }
        }

        private GreekUpper() {
        }

        private static final int getLetterData(int i) {
            if (i >= 880 && 8486 >= i && (1023 >= i || i >= 7936)) {
                if (i <= 1023) {
                    return data0370[i - 880];
                }
                if (i <= 8191) {
                    return data1F00[i - 7936];
                }
                if (i == 8486) {
                    return 5033;
                }
            }
            return 0;
        }

        private static boolean isFollowedByCasedLetter(CharSequence charSequence, int i) {
            while (true) {
                if (i >= charSequence.length()) {
                    break;
                }
                int codePointAt = Character.codePointAt(charSequence, i);
                int typeOrIgnorable = UCaseProps.INSTANCE.getTypeOrIgnorable(codePointAt);
                if ((typeOrIgnorable & 4) != 0) {
                    i += Character.charCount(codePointAt);
                } else if (typeOrIgnorable != 0) {
                    return true;
                }
            }
            return false;
        }

        /* access modifiers changed from: private */
        /* JADX WARNING: Removed duplicated region for block: B:106:0x012f A[SYNTHETIC] */
        /* JADX WARNING: Removed duplicated region for block: B:66:0x00c4  */
        /* JADX WARNING: Removed duplicated region for block: B:73:0x00d5  */
        /* JADX WARNING: Removed duplicated region for block: B:81:0x00ea  */
        /* JADX WARNING: Removed duplicated region for block: B:82:0x00ec  */
        /* JADX WARNING: Removed duplicated region for block: B:85:0x00f0  */
        /* JADX WARNING: Removed duplicated region for block: B:88:0x00f8  */
        /* JADX WARNING: Removed duplicated region for block: B:94:0x0106  */
        public static <A extends Appendable> A toUpper(int i, CharSequence charSequence, A a, Edits edits) throws IOException {
            boolean z;
            boolean z2;
            boolean z3;
            int diacriticData;
            int i2 = 0;
            int i3 = 0;
            int i4 = 0;
            while (i3 < charSequence.length()) {
                int codePointAt = Character.codePointAt(charSequence, i3);
                int charCount = Character.charCount(codePointAt) + i3;
                int typeOrIgnorable = UCaseProps.INSTANCE.getTypeOrIgnorable(codePointAt);
                int i5 = (typeOrIgnorable & 4) != 0 ? (i4 & 1) | i2 : typeOrIgnorable != 0 ? 1 : i2;
                int letterData = getLetterData(codePointAt);
                if (letterData > 0) {
                    int i6 = letterData & 1023;
                    if (!((letterData & 4096) == 0 || (i4 & 2) == 0 || (i6 != 921 && i6 != 933))) {
                        letterData |= 32768;
                    }
                    int i7 = (letterData & 8192) != 0 ? 1 : i2;
                    while (charCount < charSequence.length() && (diacriticData = getDiacriticData(charSequence.charAt(charCount))) != 0) {
                        letterData |= diacriticData;
                        if ((diacriticData & 8192) != 0) {
                            i7++;
                        }
                        charCount++;
                    }
                    if ((HAS_VOWEL_AND_ACCENT_AND_DIALYTIKA & letterData) == HAS_VOWEL_AND_ACCENT) {
                        i5 |= 2;
                    }
                    if (i6 == 919 && (letterData & 16384) != 0 && i7 == 0 && (i4 & 1) == 0 && !isFollowedByCasedLetter(charSequence, charCount)) {
                        if (i3 == charCount) {
                            i6 = 905;
                        } else {
                            z = true;
                            if (!(edits == null && (i & 16384) == 0)) {
                                boolean z4 = charSequence.charAt(i3) == i6 || i7 > 0;
                                int i8 = i3 + 1;
                                if ((letterData & HAS_EITHER_DIALYTIKA) != 0) {
                                    z4 |= i8 >= charCount || charSequence.charAt(i8) != 776;
                                    i8++;
                                }
                                if (z) {
                                    z4 |= i8 >= charCount || charSequence.charAt(i8) != 769;
                                    i8++;
                                }
                                int i9 = charCount - i3;
                                int i10 = (i8 - i3) + i7;
                                z3 = (i9 == i10) | z4;
                                if (!z3) {
                                    if (edits != null) {
                                        edits.addReplace(i9, i10);
                                    }
                                    z2 = z3;
                                } else {
                                    if (edits != null) {
                                        edits.addUnchanged(i9);
                                    }
                                    if ((i & 16384) != 0) {
                                        z2 = false;
                                    }
                                }
                                if (z2) {
                                    a.append((char) i6);
                                    if ((HAS_EITHER_DIALYTIKA & letterData) != 0) {
                                        a.append(776);
                                    }
                                    if (z) {
                                        a.append(769);
                                    }
                                    while (i7 > 0) {
                                        a.append(921);
                                        i7--;
                                    }
                                }
                            }
                            z2 = true;
                            if (z2) {
                            }
                        }
                    } else if ((letterData & 32768) != 0) {
                        if (i6 == 921) {
                            i6 = 938;
                        } else if (i6 == 933) {
                            i6 = 939;
                        }
                        letterData &= -98305;
                    }
                    z = false;
                    if (charSequence.charAt(i3) == i6) {
                    }
                    int i82 = i3 + 1;
                    if ((letterData & HAS_EITHER_DIALYTIKA) != 0) {
                    }
                    if (z) {
                    }
                    int i92 = charCount - i3;
                    int i102 = (i82 - i3) + i7;
                    z3 = (i92 == i102) | z4;
                    if (!z3) {
                    }
                    if (z2) {
                    }
                } else {
                    CaseMapImpl.appendResult(UCaseProps.INSTANCE.toFullUpper(codePointAt, null, a, 4), a, charCount - i3, i, edits);
                }
                i3 = charCount;
                i4 = i5;
                i2 = 0;
            }
            return a;
        }
    }
}
