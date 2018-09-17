package android.icu.impl;

import android.icu.impl.UCaseProps.ContextIterator;
import android.icu.text.BreakIterator;
import android.icu.text.DateTimePatternGenerator;
import android.icu.text.Edits;
import android.icu.text.UTF16;
import android.icu.util.ICUUncheckedIOException;
import dalvik.bytecode.Opcodes;
import java.io.IOException;

public final class CaseMapImpl {
    static final /* synthetic */ boolean -assertionsDisabled = (CaseMapImpl.class.desiredAssertionStatus() ^ 1);
    public static final int OMIT_UNCHANGED_TEXT = 16384;

    private static final class GreekUpper {
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
        private static final char[] data0370 = new char[]{880, 880, 882, 882, 0, 0, 886, 886, 0, 0, 890, 1021, 1022, 1023, 0, 895, 0, 0, 0, 0, 0, 0, 21393, 0, 21397, 21399, 21401, 0, 21407, 0, 21413, 21417, 54169, 5009, 914, 915, 916, 5013, 918, 5015, 920, 5017, 922, 923, 924, 925, 926, 5023, 928, 929, 0, 931, 932, 5029, 934, 935, 936, data2126, 37785, 37797, 21393, 21397, 21399, 21401, 54181, 5009, 914, 915, 916, 5013, 918, 5015, 920, 5017, 922, 923, 924, 925, 926, 5023, 928, 929, 931, 931, 932, 5029, 934, 935, 936, data2126, 37785, 37797, 21407, 21413, 21417, 975, 914, 920, 978, 17362, 33746, 934, 928, 975, 984, 984, 986, 986, 988, 988, 990, 990, 992, 992, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 922, 929, 1017, 895, 1012, 5013, 0, 1015, 1015, 1017, 1018, 1018, 1020, 1021, 1022, 1023};
        private static final char[] data1F00 = new char[]{5009, 5009, 21393, 21393, 21393, 21393, 21393, 21393, 5009, 5009, 21393, 21393, 21393, 21393, 21393, 21393, 5013, 5013, 21397, 21397, 21397, 21397, 0, 0, 5013, 5013, 21397, 21397, 21397, 21397, 0, 0, 5015, 5015, 21399, 21399, 21399, 21399, 21399, 21399, 5015, 5015, 21399, 21399, 21399, 21399, 21399, 21399, 5017, 5017, 21401, 21401, 21401, 21401, 21401, 21401, 5017, 5017, 21401, 21401, 21401, 21401, 21401, 21401, 5023, 5023, 21407, 21407, 21407, 21407, 0, 0, 5023, 5023, 21407, 21407, 21407, 21407, 0, 0, 5029, 5029, 21413, 21413, 21413, 21413, 21413, 21413, 0, 5029, 0, 21413, 0, 21413, 0, 21413, data2126, data2126, 21417, 21417, 21417, 21417, 21417, 21417, data2126, data2126, 21417, 21417, 21417, 21417, 21417, 21417, 21393, 21393, 21397, 21397, 21399, 21399, 21401, 21401, 21407, 21407, 21413, 21413, 21417, 21417, 0, 0, 13201, 13201, 29585, 29585, 29585, 29585, 29585, 29585, 13201, 13201, 29585, 29585, 29585, 29585, 29585, 29585, 13207, 13207, 29591, 29591, 29591, 29591, 29591, 29591, 13207, 13207, 29591, 29591, 29591, 29591, 29591, 29591, 13225, 13225, 29609, 29609, 29609, 29609, 29609, 29609, 13225, 13225, 29609, 29609, 29609, 29609, 29609, 29609, 5009, 5009, 29585, 13201, 29585, 0, 21393, 29585, 5009, 5009, 21393, 21393, 13201, 0, 5017, 0, 0, 0, 29591, 13207, 29591, 0, 21399, 29591, 21397, 21397, 21399, 21399, 13207, 0, 0, 0, 5017, 5017, 54169, 54169, 0, 0, 21401, 54169, 5017, 5017, 21401, 21401, 0, 0, 0, 0, 5029, 5029, 54181, 54181, 929, 929, 21413, 54181, 5029, 5029, 21413, 21413, 929, 0, 0, 0, 0, 0, 29609, 13225, 29609, 0, 21417, 29609, 21407, 21407, 21417, 21417, 13225, 0, 0, 0};
        private static final char data2126 = 'Ꭹ';

        private GreekUpper() {
        }

        private static final int getLetterData(int c) {
            if (c < 880 || 8486 < c || (1023 < c && c < 7936)) {
                return 0;
            }
            if (c <= 1023) {
                return data0370[c - 880];
            }
            if (c <= Opcodes.OP_SPUT_BYTE_JUMBO) {
                return data1F00[c - 7936];
            }
            if (c == 8486) {
                return 5033;
            }
            return 0;
        }

        private static final int getDiacriticData(int c) {
            switch (c) {
                case 768:
                case 769:
                case 770:
                case 771:
                case 785:
                case 834:
                    return 16384;
                case 772:
                case 774:
                case 787:
                case 788:
                case 835:
                    return 131072;
                case 776:
                    return 65536;
                case 836:
                    return 81920;
                case 837:
                    return 8192;
                default:
                    return 0;
            }
        }

        private static boolean isFollowedByCasedLetter(CharSequence s, int i) {
            while (i < s.length()) {
                int type = UCaseProps.INSTANCE.getTypeOrIgnorable(Character.codePointAt(s, i));
                if ((type & 4) == 0) {
                    if (type != 0) {
                        return true;
                    }
                    return false;
                }
            }
            return false;
        }

        private static <A extends Appendable> A toUpper(int options, CharSequence src, A dest, Edits edits) throws IOException {
            int state = 0;
            int i = 0;
            while (i < src.length()) {
                int c = Character.codePointAt(src, i);
                int nextIndex = i + Character.charCount(c);
                int nextState = 0;
                int type = UCaseProps.INSTANCE.getTypeOrIgnorable(c);
                if ((type & 4) != 0) {
                    nextState = (state & 1) | 0;
                } else if (type != 0) {
                    nextState = 1;
                }
                int data = getLetterData(c);
                if (data > 0) {
                    boolean change;
                    char upper = data & 1023;
                    if (!((data & 4096) == 0 || (state & 2) == 0 || (upper != 921 && upper != 933))) {
                        data |= 32768;
                    }
                    int numYpogegrammeni = 0;
                    if ((data & 8192) != 0) {
                        numYpogegrammeni = 1;
                    }
                    while (nextIndex < src.length()) {
                        int diacriticData = getDiacriticData(src.charAt(nextIndex));
                        if (diacriticData == 0) {
                            break;
                        }
                        data |= diacriticData;
                        if ((diacriticData & 8192) != 0) {
                            numYpogegrammeni++;
                        }
                        nextIndex++;
                    }
                    if ((HAS_VOWEL_AND_ACCENT_AND_DIALYTIKA & data) == HAS_VOWEL_AND_ACCENT) {
                        nextState |= 2;
                    }
                    boolean addTonos = false;
                    if (upper == 919 && (data & 16384) != 0 && numYpogegrammeni == 0 && (state & 1) == 0 && (isFollowedByCasedLetter(src, nextIndex) ^ 1) != 0) {
                        if (i == nextIndex) {
                            upper = 905;
                        } else {
                            addTonos = true;
                        }
                    } else if ((32768 & data) != 0) {
                        if (upper == 921) {
                            upper = 938;
                            data &= -98305;
                        } else if (upper == 933) {
                            upper = 939;
                            data &= -98305;
                        }
                    }
                    if (edits == null) {
                        change = true;
                    } else {
                        int i2;
                        int change2 = src.charAt(i) != upper || numYpogegrammeni > 0;
                        int i22 = i + 1;
                        if ((HAS_EITHER_DIALYTIKA & data) != 0) {
                            i2 = (i22 >= nextIndex || src.charAt(i22) != 776) ? 1 : 0;
                            change2 |= i2;
                            i22++;
                        }
                        if (addTonos) {
                            i2 = (i22 >= nextIndex || src.charAt(i22) != 769) ? 1 : 0;
                            change2 |= i2;
                            i22++;
                        }
                        int oldLength = nextIndex - i;
                        int newLength = (i22 - i) + numYpogegrammeni;
                        change = change2 | (oldLength != newLength ? 1 : 0);
                        if (!change) {
                            if (edits != null) {
                                edits.addUnchanged(oldLength);
                            }
                            change = (options & 16384) == 0;
                        } else if (edits != null) {
                            edits.addReplace(oldLength, newLength);
                        }
                    }
                    if (change) {
                        dest.append((char) upper);
                        if ((HAS_EITHER_DIALYTIKA & data) != 0) {
                            dest.append(776);
                        }
                        if (addTonos) {
                            dest.append(769);
                        }
                        while (numYpogegrammeni > 0) {
                            dest.append(921);
                            numYpogegrammeni--;
                        }
                    }
                } else {
                    CaseMapImpl.appendResult(UCaseProps.INSTANCE.toFullUpper(c, null, dest, 4), dest, nextIndex - i, options, edits);
                }
                i = nextIndex;
                state = nextState;
            }
            return dest;
        }
    }

    public static final class StringContextIterator implements ContextIterator {
        protected int cpLimit = 0;
        protected int cpStart = 0;
        protected int dir = 0;
        protected int index = 0;
        protected int limit;
        protected CharSequence s;

        public StringContextIterator(CharSequence src) {
            this.s = src;
            this.limit = src.length();
        }

        public void setLimit(int lim) {
            if (lim < 0 || lim > this.s.length()) {
                this.limit = this.s.length();
            } else {
                this.limit = lim;
            }
        }

        public void moveToLimit() {
            int i = this.limit;
            this.cpLimit = i;
            this.cpStart = i;
        }

        public int nextCaseMapCP() {
            this.cpStart = this.cpLimit;
            if (this.cpLimit >= this.limit) {
                return -1;
            }
            int c = Character.codePointAt(this.s, this.cpLimit);
            this.cpLimit += Character.charCount(c);
            return c;
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

        public void reset(int direction) {
            if (direction > 0) {
                this.dir = 1;
                this.index = this.cpLimit;
            } else if (direction < 0) {
                this.dir = -1;
                this.index = this.cpStart;
            } else {
                this.dir = 0;
                this.index = 0;
            }
        }

        public int next() {
            int c;
            if (this.dir > 0 && this.index < this.s.length()) {
                c = Character.codePointAt(this.s, this.index);
                this.index += Character.charCount(c);
                return c;
            } else if (this.dir >= 0 || this.index <= 0) {
                return -1;
            } else {
                c = Character.codePointBefore(this.s, this.index);
                this.index -= Character.charCount(c);
                return c;
            }
        }
    }

    private static int appendCodePoint(Appendable a, int c) throws IOException {
        if (c <= DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH) {
            a.append((char) c);
            return 1;
        }
        a.append((char) ((c >> 10) + 55232));
        a.append((char) ((c & Opcodes.OP_NEW_INSTANCE_JUMBO) + UTF16.TRAIL_SURROGATE_MIN_VALUE));
        return 2;
    }

    private static void appendResult(int result, Appendable dest, int cpLength, int options, Edits edits) throws IOException {
        if (result < 0) {
            if (edits != null) {
                edits.addUnchanged(cpLength);
                if ((options & 16384) != 0) {
                    return;
                }
            }
            appendCodePoint(dest, ~result);
        } else if (result > 31) {
            int length = appendCodePoint(dest, result);
            if (edits != null) {
                edits.addReplace(cpLength, length);
            }
        } else if (edits != null) {
            edits.addReplace(cpLength, result);
        }
    }

    private static final void appendUnchanged(CharSequence src, int start, int length, Appendable dest, int options, Edits edits) throws IOException {
        if (length > 0) {
            if (edits != null) {
                edits.addUnchanged(length);
                if ((options & 16384) != 0) {
                    return;
                }
            }
            dest.append(src, start, start + length);
        }
    }

    private static void internalToLower(int caseLocale, int options, StringContextIterator iter, Appendable dest, Edits edits) throws IOException {
        while (true) {
            int c = iter.nextCaseMapCP();
            if (c >= 0) {
                appendResult(UCaseProps.INSTANCE.toFullLower(c, iter, dest, caseLocale), dest, iter.getCPLength(), options, edits);
            } else {
                return;
            }
        }
    }

    public static <A extends Appendable> A toLower(int caseLocale, int options, CharSequence src, A dest, Edits edits) {
        if (edits != null) {
            try {
                edits.reset();
            } catch (Throwable e) {
                throw new ICUUncheckedIOException(e);
            }
        }
        internalToLower(caseLocale, options, new StringContextIterator(src), dest, edits);
        return dest;
    }

    public static <A extends Appendable> A toUpper(int caseLocale, int options, CharSequence src, A dest, Edits edits) {
        if (edits != null) {
            try {
                edits.reset();
            } catch (Throwable e) {
                throw new ICUUncheckedIOException(e);
            }
        }
        if (caseLocale == 4) {
            return GreekUpper.toUpper(options, src, dest, edits);
        }
        StringContextIterator iter = new StringContextIterator(src);
        while (true) {
            int c = iter.nextCaseMapCP();
            if (c < 0) {
                return dest;
            }
            appendResult(UCaseProps.INSTANCE.toFullUpper(c, iter, dest, caseLocale), dest, iter.getCPLength(), options, edits);
        }
    }

    public static <A extends Appendable> A toTitle(int caseLocale, int options, BreakIterator titleIter, CharSequence src, A dest, Edits edits) {
        if (edits != null) {
            try {
                edits.reset();
            } catch (Throwable e) {
                throw new ICUUncheckedIOException(e);
            }
        }
        StringContextIterator stringContextIterator = new StringContextIterator(src);
        int srcLength = src.length();
        boolean isFirstIndex = true;
        int index;
        for (int prev = 0; prev < srcLength; prev = index) {
            if (isFirstIndex) {
                isFirstIndex = false;
                index = titleIter.first();
            } else {
                index = titleIter.next();
            }
            if (index == -1 || index > srcLength) {
                index = srcLength;
            }
            if (prev < index) {
                int titleStart = prev;
                stringContextIterator.setLimit(index);
                int c = stringContextIterator.nextCaseMapCP();
                if ((options & 512) == 0 && UCaseProps.INSTANCE.getType(c) == 0) {
                    do {
                        c = stringContextIterator.nextCaseMapCP();
                        if (c < 0) {
                            break;
                        }
                    } while (UCaseProps.INSTANCE.getType(c) == 0);
                    titleStart = stringContextIterator.getCPStart();
                    appendUnchanged(src, prev, titleStart - prev, dest, options, edits);
                }
                if (titleStart < index) {
                    int titleLimit;
                    int titleLimit2 = stringContextIterator.getCPLimit();
                    appendResult(UCaseProps.INSTANCE.toFullTitle(c, stringContextIterator, dest, caseLocale), dest, stringContextIterator.getCPLength(), options, edits);
                    if (titleStart + 1 >= index || caseLocale != 5) {
                        titleLimit = titleLimit2;
                    } else {
                        char c1 = src.charAt(titleStart);
                        if (c1 == UCharacterProperty.LATIN_SMALL_LETTER_I_ || c1 == 'I') {
                            char c2 = src.charAt(titleStart + 1);
                            char c3;
                            if (c2 == 'j') {
                                dest.append('J');
                                if (edits != null) {
                                    edits.addReplace(1, 1);
                                }
                                c3 = stringContextIterator.nextCaseMapCP();
                                titleLimit = titleLimit2 + 1;
                                if (!-assertionsDisabled && c3 != c2) {
                                    throw new AssertionError();
                                } else if (!(-assertionsDisabled || titleLimit == stringContextIterator.getCPLimit())) {
                                    throw new AssertionError();
                                }
                            } else if (c2 == 'J') {
                                appendUnchanged(src, titleStart + 1, 1, dest, options, edits);
                                c3 = stringContextIterator.nextCaseMapCP();
                                titleLimit = titleLimit2 + 1;
                                if (!-assertionsDisabled && c3 != c2) {
                                    throw new AssertionError();
                                } else if (!(-assertionsDisabled || titleLimit == stringContextIterator.getCPLimit())) {
                                    throw new AssertionError();
                                }
                            } else {
                                titleLimit = titleLimit2;
                            }
                        } else {
                            titleLimit = titleLimit2;
                        }
                    }
                    if (titleLimit < index) {
                        if ((options & 256) == 0) {
                            internalToLower(caseLocale, options, stringContextIterator, dest, edits);
                        } else {
                            appendUnchanged(src, titleLimit, index - titleLimit, dest, options, edits);
                            stringContextIterator.moveToLimit();
                        }
                    }
                } else {
                    continue;
                }
            }
        }
        return dest;
    }

    public static <A extends Appendable> A fold(int options, CharSequence src, A dest, Edits edits) {
        if (edits != null) {
            try {
                edits.reset();
            } catch (Throwable e) {
                throw new ICUUncheckedIOException(e);
            }
        }
        int length = src.length();
        int i = 0;
        while (i < length) {
            int c = Character.codePointAt(src, i);
            int cpLength = Character.charCount(c);
            i += cpLength;
            appendResult(UCaseProps.INSTANCE.toFullFolding(c, dest, options), dest, cpLength, options, edits);
        }
        return dest;
    }
}
