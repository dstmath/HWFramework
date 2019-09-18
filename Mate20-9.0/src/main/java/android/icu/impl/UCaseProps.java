package android.icu.impl;

import android.icu.impl.ICUBinary;
import android.icu.impl.Trie2;
import android.icu.text.DateTimePatternGenerator;
import android.icu.text.UTF16;
import android.icu.text.UnicodeSet;
import android.icu.util.ICUUncheckedIOException;
import android.icu.util.ULocale;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Locale;

public final class UCaseProps {
    private static final int ABOVE = 64;
    private static final int CLOSURE_MAX_LENGTH = 15;
    private static final String DATA_FILE_NAME = "ucase.icu";
    private static final String DATA_NAME = "ucase";
    private static final String DATA_TYPE = "icu";
    private static final int DELTA_SHIFT = 7;
    private static final int DOT_MASK = 96;
    private static final int EXCEPTION = 16;
    private static final int EXC_CLOSURE = 6;
    private static final int EXC_CONDITIONAL_FOLD = 32768;
    private static final int EXC_CONDITIONAL_SPECIAL = 16384;
    private static final int EXC_DOT_SHIFT = 7;
    private static final int EXC_DOUBLE_SLOTS = 256;
    private static final int EXC_FOLD = 1;
    private static final int EXC_FULL_MAPPINGS = 7;
    private static final int EXC_LOWER = 0;
    private static final int EXC_SHIFT = 5;
    private static final int EXC_TITLE = 3;
    private static final int EXC_UPPER = 2;
    private static final int FMT = 1665225541;
    private static final int FOLD_CASE_OPTIONS_MASK = 7;
    private static final int FULL_LOWER = 15;
    static final int IGNORABLE = 4;
    public static final UCaseProps INSTANCE;
    private static final int IX_EXC_LENGTH = 3;
    private static final int IX_TOP = 16;
    private static final int IX_TRIE_SIZE = 2;
    private static final int IX_UNFOLD_LENGTH = 4;
    public static final int LOC_DUTCH = 5;
    static final int LOC_GREEK = 4;
    private static final int LOC_LITHUANIAN = 3;
    public static final int LOC_ROOT = 1;
    private static final int LOC_TURKISH = 2;
    public static final int LOWER = 1;
    public static final int MAX_STRING_LENGTH = 31;
    public static final int NONE = 0;
    private static final int OTHER_ACCENT = 96;
    private static final int SENSITIVE = 8;
    private static final int SOFT_DOTTED = 32;
    public static final int TITLE = 3;
    public static final int TYPE_MASK = 3;
    private static final int UNFOLD_ROWS = 0;
    private static final int UNFOLD_ROW_WIDTH = 1;
    private static final int UNFOLD_STRING_WIDTH = 2;
    public static final int UPPER = 2;
    public static final StringBuilder dummyStringBuilder = new StringBuilder();
    private static final byte[] flagsOffset = {0, 1, 1, 2, 1, 2, 2, 3, 1, 2, 2, 3, 2, 3, 3, 4, 1, 2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5, 1, 2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5, 2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 1, 2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5, 2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7, 1, 2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5, 2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7, 2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7, 3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7, 4, 5, 5, 6, 5, 6, 6, 7, 5, 6, 6, 7, 6, 7, 7, 8};
    private static final String iDot = "i̇";
    private static final String iDotAcute = "i̇́";
    private static final String iDotGrave = "i̇̀";
    private static final String iDotTilde = "i̇̃";
    private static final String iOgonekDot = "į̇";
    private static final String jDot = "j̇";
    private String exceptions;
    private int[] indexes;
    private Trie2_16 trie;
    private char[] unfold;

    public interface ContextIterator {
        int next();

        void reset(int i);
    }

    private static final class IsAcceptable implements ICUBinary.Authenticate {
        private IsAcceptable() {
        }

        public boolean isDataVersionAcceptable(byte[] version) {
            return version[0] == 3;
        }
    }

    private UCaseProps() throws IOException {
        readData(ICUBinary.getRequiredData(DATA_FILE_NAME));
    }

    private final void readData(ByteBuffer bytes) throws IOException {
        ICUBinary.readHeader(bytes, FMT, new IsAcceptable());
        int count = bytes.getInt();
        if (count >= 16) {
            this.indexes = new int[count];
            this.indexes[0] = count;
            for (int i = 1; i < count; i++) {
                this.indexes[i] = bytes.getInt();
            }
            this.trie = Trie2_16.createFromSerialized(bytes);
            int expectedTrieLength = this.indexes[2];
            int trieLength = this.trie.getSerializedLength();
            if (trieLength <= expectedTrieLength) {
                ICUBinary.skipBytes(bytes, expectedTrieLength - trieLength);
                int count2 = this.indexes[3];
                if (count2 > 0) {
                    this.exceptions = ICUBinary.getString(bytes, count2, 0);
                }
                int count3 = this.indexes[4];
                if (count3 > 0) {
                    this.unfold = ICUBinary.getChars(bytes, count3, 0);
                    return;
                }
                return;
            }
            throw new IOException("ucase.icu: not enough bytes for the trie");
        }
        throw new IOException("indexes[0] too small in ucase.icu");
    }

    public final void addPropertyStarts(UnicodeSet set) {
        Iterator<Trie2.Range> trieIterator = this.trie.iterator();
        while (trieIterator.hasNext()) {
            Trie2.Range next = trieIterator.next();
            Trie2.Range range = next;
            if (!next.leadSurrogate) {
                set.add(range.startCodePoint);
            } else {
                return;
            }
        }
    }

    private static final int getExceptionsOffset(int props) {
        return props >> 5;
    }

    private static final boolean propsHasException(int props) {
        return (props & 16) != 0;
    }

    static {
        try {
            INSTANCE = new UCaseProps();
        } catch (IOException e) {
            throw new ICUUncheckedIOException((Throwable) e);
        }
    }

    private static final boolean hasSlot(int flags, int index) {
        return ((1 << index) & flags) != 0;
    }

    private static final byte slotOffset(int flags, int index) {
        return flagsOffset[flags & ((1 << index) - 1)];
    }

    private final long getSlotValueAndOffset(int excWord, int index, int excOffset) {
        int excOffset2;
        long value;
        if ((excWord & 256) == 0) {
            excOffset2 = excOffset + slotOffset(excWord, index);
            value = (long) this.exceptions.charAt(excOffset2);
        } else {
            int excOffset3 = excOffset + (2 * slotOffset(excWord, index));
            int excOffset4 = excOffset3 + 1;
            excOffset2 = excOffset4;
            value = (((long) this.exceptions.charAt(excOffset3)) << 16) | ((long) this.exceptions.charAt(excOffset4));
        }
        return (((long) excOffset2) << 32) | value;
    }

    private final int getSlotValue(int excWord, int index, int excOffset) {
        if ((excWord & 256) == 0) {
            return this.exceptions.charAt(excOffset + slotOffset(excWord, index));
        }
        int excOffset2 = excOffset + (2 * slotOffset(excWord, index));
        int excOffset3 = excOffset2 + 1;
        int value = (this.exceptions.charAt(excOffset2) << 16) | this.exceptions.charAt(excOffset3);
        int value2 = excOffset3;
        return value;
    }

    public final int tolower(int c) {
        int props = this.trie.get(c);
        if (propsHasException(props)) {
            int excOffset = getExceptionsOffset(props);
            int excOffset2 = excOffset + 1;
            int excOffset3 = this.exceptions.charAt(excOffset);
            if (hasSlot(excOffset3, 0)) {
                return getSlotValue(excOffset3, 0, excOffset2);
            }
            return c;
        } else if (getTypeFromProps(props) >= 2) {
            return c + getDelta(props);
        } else {
            return c;
        }
    }

    public final int toupper(int c) {
        int props = this.trie.get(c);
        if (propsHasException(props)) {
            int excOffset = getExceptionsOffset(props);
            int excOffset2 = excOffset + 1;
            int excOffset3 = this.exceptions.charAt(excOffset);
            if (hasSlot(excOffset3, 2)) {
                return getSlotValue(excOffset3, 2, excOffset2);
            }
            return c;
        } else if (getTypeFromProps(props) == 1) {
            return c + getDelta(props);
        } else {
            return c;
        }
    }

    public final int totitle(int c) {
        int index;
        int props = this.trie.get(c);
        if (propsHasException(props)) {
            int excOffset = getExceptionsOffset(props);
            int excOffset2 = excOffset + 1;
            int excOffset3 = this.exceptions.charAt(excOffset);
            if (hasSlot(excOffset3, 3)) {
                index = 3;
            } else if (!hasSlot(excOffset3, 2)) {
                return c;
            } else {
                index = 2;
            }
            c = getSlotValue(excOffset3, index, excOffset2);
        } else if (getTypeFromProps(props) == 1) {
            c += getDelta(props);
        }
        return c;
    }

    public final void addCaseClosure(int c, UnicodeSet set) {
        int closureOffset;
        int i = c;
        UnicodeSet unicodeSet = set;
        if (i == 73) {
            unicodeSet.add(105);
        } else if (i != 105) {
            switch (i) {
                case 304:
                    unicodeSet.add((CharSequence) iDot);
                    return;
                case 305:
                    return;
                default:
                    int props = this.trie.get(i);
                    if (propsHasException(props)) {
                        int excOffset = getExceptionsOffset(props);
                        int excOffset2 = excOffset + 1;
                        int excOffset3 = this.exceptions.charAt(excOffset);
                        int excOffset0 = excOffset2;
                        int closureLength = 0;
                        int c2 = i;
                        for (int index = 0; index <= 3; index++) {
                            if (hasSlot(excOffset3, index)) {
                                excOffset2 = excOffset0;
                                c2 = getSlotValue(excOffset3, index, excOffset2);
                                unicodeSet.add(c2);
                            }
                        }
                        if (hasSlot(excOffset3, 6)) {
                            excOffset2 = excOffset0;
                            long value = getSlotValueAndOffset(excOffset3, 6, excOffset2);
                            int i2 = ((int) (value >> 32)) + 1;
                            closureOffset = ((int) value) & 15;
                            closureLength = i2;
                        } else {
                            closureOffset = 0;
                        }
                        if (hasSlot(excOffset3, 7)) {
                            long value2 = getSlotValueAndOffset(excOffset3, 7, excOffset0);
                            int fullLength = 65535 & ((int) value2);
                            int excOffset4 = ((int) (value2 >> 32)) + 1 + (fullLength & 15);
                            int fullLength2 = fullLength >> 4;
                            int length = fullLength2 & 15;
                            if (length != 0) {
                                unicodeSet.add((CharSequence) this.exceptions.substring(excOffset4, excOffset4 + length));
                                excOffset4 += length;
                            }
                            int fullLength3 = fullLength2 >> 4;
                            closureLength = excOffset4 + (fullLength3 & 15) + (fullLength3 >> 4);
                        }
                        int excOffset5 = closureLength + closureOffset;
                        int index2 = closureLength;
                        while (index2 < excOffset5) {
                            c2 = this.exceptions.codePointAt(index2);
                            unicodeSet.add(c2);
                            index2 += UTF16.getCharCount(c2);
                        }
                        int index3 = c2;
                    } else if (getTypeFromProps(props) != 0) {
                        int delta = getDelta(props);
                        if (delta != 0) {
                            unicodeSet.add(i + delta);
                        }
                    }
                    return;
            }
        } else {
            unicodeSet.add(73);
        }
    }

    private final int strcmpMax(String s, int unfoldOffset, int max) {
        int length = s.length();
        int max2 = max - length;
        int length2 = length;
        int length3 = unfoldOffset;
        int c1 = 0;
        while (true) {
            int i1 = c1 + 1;
            int i12 = s.charAt(c1);
            int unfoldOffset2 = length3 + 1;
            char c2 = this.unfold[length3];
            if (c2 == 0) {
                return 1;
            }
            int c12 = i12 - c2;
            if (c12 != 0) {
                return c12;
            }
            length2--;
            if (length2 > 0) {
                c1 = i1;
                length3 = unfoldOffset2;
            } else if (max2 == 0 || this.unfold[unfoldOffset2] == 0) {
                return 0;
            } else {
                return -max2;
            }
        }
    }

    public final boolean addStringCaseClosure(String s, UnicodeSet set) {
        if (this.unfold == null || s == null) {
            return false;
        }
        int length = s.length();
        if (length <= 1) {
            return false;
        }
        char unfoldRows = this.unfold[0];
        char unfoldRowWidth = this.unfold[1];
        char unfoldStringWidth = this.unfold[2];
        if (length > unfoldStringWidth) {
            return false;
        }
        int start = 0;
        int limit = unfoldRows;
        while (start < limit) {
            int i = (start + limit) / 2;
            int unfoldOffset = (i + 1) * unfoldRowWidth;
            int result = strcmpMax(s, unfoldOffset, unfoldStringWidth);
            if (result == 0) {
                int i2 = unfoldStringWidth;
                while (i2 < unfoldRowWidth && this.unfold[unfoldOffset + i2] != 0) {
                    int c = UTF16.charAt(this.unfold, unfoldOffset, this.unfold.length, i2);
                    set.add(c);
                    addCaseClosure(c, set);
                    i2 += UTF16.getCharCount(c);
                }
                return true;
            } else if (result < 0) {
                limit = i;
            } else {
                start = i + 1;
            }
        }
        return false;
    }

    public final int getType(int c) {
        return getTypeFromProps(this.trie.get(c));
    }

    public final int getTypeOrIgnorable(int c) {
        return getTypeAndIgnorableFromProps(this.trie.get(c));
    }

    public final int getDotType(int c) {
        int props = this.trie.get(c);
        if (!propsHasException(props)) {
            return props & 96;
        }
        return (this.exceptions.charAt(getExceptionsOffset(props)) >> 7) & 96;
    }

    public final boolean isSoftDotted(int c) {
        return getDotType(c) == 32;
    }

    public final boolean isCaseSensitive(int c) {
        return (this.trie.get(c) & 8) != 0;
    }

    public static final int getCaseLocale(Locale locale) {
        return getCaseLocale(locale.getLanguage());
    }

    public static final int getCaseLocale(ULocale locale) {
        return getCaseLocale(locale.getLanguage());
    }

    private static final int getCaseLocale(String language) {
        if (language.length() == 2) {
            if (language.equals("en") || language.charAt(0) > 't') {
                return 1;
            }
            if (language.equals("tr") || language.equals("az")) {
                return 2;
            }
            if (language.equals("el")) {
                return 4;
            }
            if (language.equals("lt")) {
                return 3;
            }
            if (language.equals("nl")) {
                return 5;
            }
        } else if (language.length() == 3) {
            if (language.equals("tur") || language.equals("aze")) {
                return 2;
            }
            if (language.equals("ell")) {
                return 4;
            }
            if (language.equals("lit")) {
                return 3;
            }
            if (language.equals("nld")) {
                return 5;
            }
        }
        return 1;
    }

    private final boolean isFollowedByCasedLetter(ContextIterator iter, int dir) {
        int type;
        if (iter == null) {
            return false;
        }
        iter.reset(dir);
        do {
            int next = iter.next();
            int c = next;
            if (next < 0) {
                return false;
            }
            type = getTypeOrIgnorable(c);
        } while ((type & 4) != 0);
        if (type != 0) {
            return true;
        }
        return false;
    }

    private final boolean isPrecededBySoftDotted(ContextIterator iter) {
        int dotType;
        if (iter == null) {
            return false;
        }
        iter.reset(-1);
        do {
            int next = iter.next();
            int c = next;
            if (next < 0) {
                return false;
            }
            dotType = getDotType(c);
            if (dotType == 32) {
                return true;
            }
        } while (dotType == 96);
        return false;
    }

    private final boolean isPrecededBy_I(ContextIterator iter) {
        int c;
        if (iter == null) {
            return false;
        }
        iter.reset(-1);
        do {
            int next = iter.next();
            c = next;
            if (next < 0) {
                return false;
            }
            if (c == 73) {
                return true;
            }
        } while (getDotType(c) == 96);
        return false;
    }

    private final boolean isFollowedByMoreAbove(ContextIterator iter) {
        int dotType;
        if (iter == null) {
            return false;
        }
        iter.reset(1);
        do {
            int next = iter.next();
            int c = next;
            if (next < 0) {
                return false;
            }
            dotType = getDotType(c);
            if (dotType == 64) {
                return true;
            }
        } while (dotType == 96);
        return false;
    }

    private final boolean isFollowedByDotAbove(ContextIterator iter) {
        int c;
        if (iter == null) {
            return false;
        }
        iter.reset(1);
        do {
            int next = iter.next();
            c = next;
            if (next < 0) {
                return false;
            }
            if (c == 775) {
                return true;
            }
        } while (getDotType(c) == 96);
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:93:0x010f  */
    /* JADX WARNING: Removed duplicated region for block: B:94:0x0111  */
    public final int toFullLower(int c, ContextIterator iter, Appendable out, int caseLocale) {
        int result;
        int result2;
        int i = c;
        ContextIterator contextIterator = iter;
        Appendable appendable = out;
        int i2 = caseLocale;
        int result3 = i;
        int props = this.trie.get(i);
        if (propsHasException(props)) {
            int excOffset = getExceptionsOffset(props);
            int excOffset2 = excOffset + 1;
            int excWord = this.exceptions.charAt(excOffset);
            int excOffset22 = excOffset2;
            if ((excWord & 16384) == 0) {
                if (hasSlot(excWord, 7)) {
                    long value = getSlotValueAndOffset(excWord, 7, excOffset2);
                    int full = ((int) value) & 15;
                    if (full != 0) {
                        int i3 = result3;
                        int excOffset3 = ((int) (value >> 32)) + 1;
                        try {
                            appendable.append(this.exceptions, excOffset3, excOffset3 + full);
                            return full;
                        } catch (IOException e) {
                            throw new ICUUncheckedIOException((Throwable) e);
                        }
                    }
                }
                result2 = result3;
            } else if (i2 == 3 && (((i == 73 || i == 74 || i == 302) && isFollowedByMoreAbove(contextIterator)) || i == 204 || i == 205 || i == 296)) {
                switch (i) {
                    case 73:
                        appendable.append(iDot);
                        return 2;
                    case 74:
                        appendable.append(jDot);
                        return 2;
                    case 204:
                        appendable.append(iDotGrave);
                        return 3;
                    case 205:
                        appendable.append(iDotAcute);
                        return 3;
                    case 296:
                        appendable.append(iDotTilde);
                        return 3;
                    case 302:
                        try {
                            appendable.append(iOgonekDot);
                            return 2;
                        } catch (IOException e2) {
                            throw new ICUUncheckedIOException((Throwable) e2);
                        }
                    default:
                        return 0;
                }
            } else if (i2 == 2 && i == 304) {
                return 105;
            } else {
                if (i2 == 2 && i == 775 && isPrecededBy_I(contextIterator)) {
                    return 0;
                }
                if (i2 == 2 && i == 73 && !isFollowedByDotAbove(contextIterator)) {
                    return 305;
                }
                if (i == 304) {
                    try {
                        appendable.append(iDot);
                        return 2;
                    } catch (IOException e3) {
                        throw new ICUUncheckedIOException((Throwable) e3);
                    }
                } else if (i == 931 && !isFollowedByCasedLetter(contextIterator, 1) && isFollowedByCasedLetter(contextIterator, -1)) {
                    return 962;
                } else {
                    result2 = result3;
                }
            }
            if (hasSlot(excWord, 0)) {
                result = getSlotValue(excWord, 0, excOffset22);
                return result == i ? ~result : result;
            }
        } else if (getTypeFromProps(props) >= 2) {
            result = i + getDelta(props);
            return result == i ? ~result : result;
        } else {
            result2 = result3;
        }
        result = result2;
        return result == i ? ~result : result;
    }

    private final int toUpperOrTitle(int c, ContextIterator iter, Appendable out, int loc, boolean upperNotTitle) {
        int index;
        int full;
        int i = c;
        int i2 = loc;
        int result = i;
        int props = this.trie.get(i);
        if (!propsHasException(props)) {
            if (getTypeFromProps(props) == 1) {
                result = i + getDelta(props);
            }
            Appendable appendable = out;
        } else {
            int excOffset = getExceptionsOffset(props);
            int excOffset2 = excOffset + 1;
            int excWord = this.exceptions.charAt(excOffset);
            int excOffset22 = excOffset2;
            if ((excWord & 16384) == 0) {
                ContextIterator contextIterator = iter;
                if (hasSlot(excWord, 7)) {
                    long value = getSlotValueAndOffset(excWord, 7, excOffset2);
                    int full2 = ((int) value) & DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH;
                    int full3 = full2 >> 4;
                    int excOffset3 = ((int) (value >> 32)) + 1 + (full2 & 15) + (full3 & 15);
                    int full4 = full3 >> 4;
                    if (upperNotTitle) {
                        full = full4 & 15;
                    } else {
                        excOffset3 += full4 & 15;
                        full = (full4 >> 4) & 15;
                    }
                    int full5 = full;
                    int excOffset4 = excOffset3;
                    if (full5 != 0) {
                        try {
                            try {
                                out.append(this.exceptions, excOffset4, excOffset4 + full5);
                                return full5;
                            } catch (IOException e) {
                                e = e;
                                throw new ICUUncheckedIOException((Throwable) e);
                            }
                        } catch (IOException e2) {
                            e = e2;
                            Appendable appendable2 = out;
                            throw new ICUUncheckedIOException((Throwable) e);
                        }
                    }
                }
            } else if (i2 == 2 && i == 105) {
                return 304;
            } else {
                if (i2 == 3 && i == 775 && isPrecededBySoftDotted(iter)) {
                    return 0;
                }
            }
            Appendable appendable3 = out;
            if (!upperNotTitle && hasSlot(excWord, 3)) {
                index = 3;
            } else if (!hasSlot(excWord, 2)) {
                return ~i;
            } else {
                index = 2;
            }
            result = getSlotValue(excWord, index, excOffset22);
        }
        return result == i ? ~result : result;
    }

    public final int toFullUpper(int c, ContextIterator iter, Appendable out, int caseLocale) {
        return toUpperOrTitle(c, iter, out, caseLocale, true);
    }

    public final int toFullTitle(int c, ContextIterator iter, Appendable out, int caseLocale) {
        return toUpperOrTitle(c, iter, out, caseLocale, false);
    }

    public final int fold(int c, int options) {
        int index;
        int props = this.trie.get(c);
        if (propsHasException(props)) {
            int excOffset = getExceptionsOffset(props);
            int excOffset2 = excOffset + 1;
            int excOffset3 = this.exceptions.charAt(excOffset);
            if ((32768 & excOffset3) != 0) {
                if ((options & 7) == 0) {
                    if (c == 73) {
                        return 105;
                    }
                    if (c == 304) {
                        return c;
                    }
                } else if (c == 73) {
                    return 305;
                } else {
                    if (c == 304) {
                        return 105;
                    }
                }
            }
            if (hasSlot(excOffset3, 1)) {
                index = 1;
            } else if (!hasSlot(excOffset3, 0)) {
                return c;
            } else {
                index = 0;
            }
            c = getSlotValue(excOffset3, index, excOffset2);
        } else if (getTypeFromProps(props) >= 2) {
            c += getDelta(props);
        }
        return c;
    }

    public final int toFullFolding(int c, Appendable out, int options) {
        int index;
        int result = c;
        int props = this.trie.get(c);
        if (propsHasException(props)) {
            int excOffset = getExceptionsOffset(props);
            int excOffset2 = excOffset + 1;
            int excOffset3 = this.exceptions.charAt(excOffset);
            int excOffset22 = excOffset2;
            if ((32768 & excOffset3) != 0) {
                if ((options & 7) == 0) {
                    if (c == 73) {
                        return 105;
                    }
                    if (c == 304) {
                        try {
                            out.append(iDot);
                            return 2;
                        } catch (IOException e) {
                            throw new ICUUncheckedIOException((Throwable) e);
                        }
                    }
                } else if (c == 73) {
                    return 305;
                } else {
                    if (c == 304) {
                        return 105;
                    }
                }
            } else if (hasSlot(excOffset3, 7)) {
                long value = getSlotValueAndOffset(excOffset3, 7, excOffset2);
                int full = ((int) value) & DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH;
                int excOffset4 = (full & 15) + ((int) (value >> 32)) + 1;
                int full2 = (full >> 4) & 15;
                if (full2 != 0) {
                    try {
                        out.append(this.exceptions, excOffset4, excOffset4 + full2);
                        return full2;
                    } catch (IOException e2) {
                        throw new ICUUncheckedIOException((Throwable) e2);
                    }
                }
            }
            if (hasSlot(excOffset3, 1) != 0) {
                index = 1;
            } else if (!hasSlot(excOffset3, 0)) {
                return ~c;
            } else {
                index = 0;
            }
            result = getSlotValue(excOffset3, index, excOffset22);
        } else if (getTypeFromProps(props) >= 2) {
            result = c + getDelta(props);
        }
        return result == c ? ~result : result;
    }

    public final boolean hasBinaryProperty(int c, int which) {
        boolean z = false;
        if (which == 22) {
            if (1 == getType(c)) {
                z = true;
            }
            return z;
        } else if (which == 27) {
            return isSoftDotted(c);
        } else {
            if (which == 30) {
                if (2 == getType(c)) {
                    z = true;
                }
                return z;
            } else if (which == 34) {
                return isCaseSensitive(c);
            } else {
                if (which != 55) {
                    switch (which) {
                        case 49:
                            if (getType(c) != 0) {
                                z = true;
                            }
                            return z;
                        case 50:
                            if ((getTypeOrIgnorable(c) >> 2) != 0) {
                                z = true;
                            }
                            return z;
                        case 51:
                            dummyStringBuilder.setLength(0);
                            if (toFullLower(c, null, dummyStringBuilder, 1) >= 0) {
                                z = true;
                            }
                            return z;
                        case 52:
                            dummyStringBuilder.setLength(0);
                            if (toFullUpper(c, null, dummyStringBuilder, 1) >= 0) {
                                z = true;
                            }
                            return z;
                        case 53:
                            dummyStringBuilder.setLength(0);
                            if (toFullTitle(c, null, dummyStringBuilder, 1) >= 0) {
                                z = true;
                            }
                            return z;
                        default:
                            return false;
                    }
                } else {
                    dummyStringBuilder.setLength(0);
                    if (toFullLower(c, null, dummyStringBuilder, 1) >= 0 || toFullUpper(c, null, dummyStringBuilder, 1) >= 0 || toFullTitle(c, null, dummyStringBuilder, 1) >= 0) {
                        z = true;
                    }
                    return z;
                }
            }
        }
    }

    private static final int getTypeFromProps(int props) {
        return props & 3;
    }

    private static final int getTypeAndIgnorableFromProps(int props) {
        return props & 7;
    }

    private static final int getDelta(int props) {
        return ((short) props) >> 7;
    }
}
