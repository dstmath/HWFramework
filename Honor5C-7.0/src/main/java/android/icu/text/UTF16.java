package android.icu.text;

import android.icu.lang.UCharacter;
import com.android.dex.DexFormat;
import java.util.Comparator;
import libcore.icu.DateUtilsBridge;

public final class UTF16 {
    public static final int CODEPOINT_MAX_VALUE = 1114111;
    public static final int CODEPOINT_MIN_VALUE = 0;
    private static final int LEAD_SURROGATE_BITMASK = -1024;
    private static final int LEAD_SURROGATE_BITS = 55296;
    public static final int LEAD_SURROGATE_BOUNDARY = 2;
    public static final int LEAD_SURROGATE_MAX_VALUE = 56319;
    public static final int LEAD_SURROGATE_MIN_VALUE = 55296;
    private static final int LEAD_SURROGATE_OFFSET_ = 55232;
    private static final int LEAD_SURROGATE_SHIFT_ = 10;
    public static final int SINGLE_CHAR_BOUNDARY = 1;
    public static final int SUPPLEMENTARY_MIN_VALUE = 65536;
    private static final int SURROGATE_BITMASK = -2048;
    private static final int SURROGATE_BITS = 55296;
    public static final int SURROGATE_MAX_VALUE = 57343;
    public static final int SURROGATE_MIN_VALUE = 55296;
    private static final int TRAIL_SURROGATE_BITMASK = -1024;
    private static final int TRAIL_SURROGATE_BITS = 56320;
    public static final int TRAIL_SURROGATE_BOUNDARY = 5;
    private static final int TRAIL_SURROGATE_MASK_ = 1023;
    public static final int TRAIL_SURROGATE_MAX_VALUE = 57343;
    public static final int TRAIL_SURROGATE_MIN_VALUE = 56320;

    public static final class StringComparator implements Comparator<String> {
        private static final int CODE_POINT_COMPARE_SURROGATE_OFFSET_ = 10240;
        public static final int FOLD_CASE_DEFAULT = 0;
        public static final int FOLD_CASE_EXCLUDE_SPECIAL_I = 1;
        private int m_codePointCompare_;
        private int m_foldCase_;
        private boolean m_ignoreCase_;

        public StringComparator() {
            this(false, false, FOLD_CASE_DEFAULT);
        }

        public StringComparator(boolean codepointcompare, boolean ignorecase, int foldcaseoption) {
            setCodePointCompare(codepointcompare);
            this.m_ignoreCase_ = ignorecase;
            if (foldcaseoption < 0 || foldcaseoption > FOLD_CASE_EXCLUDE_SPECIAL_I) {
                throw new IllegalArgumentException("Invalid fold case option");
            }
            this.m_foldCase_ = foldcaseoption;
        }

        public void setCodePointCompare(boolean flag) {
            if (flag) {
                this.m_codePointCompare_ = DateUtilsBridge.FORMAT_ABBREV_WEEKDAY;
            } else {
                this.m_codePointCompare_ = FOLD_CASE_DEFAULT;
            }
        }

        public void setIgnoreCase(boolean ignorecase, int foldcaseoption) {
            this.m_ignoreCase_ = ignorecase;
            if (foldcaseoption < 0 || foldcaseoption > FOLD_CASE_EXCLUDE_SPECIAL_I) {
                throw new IllegalArgumentException("Invalid fold case option");
            }
            this.m_foldCase_ = foldcaseoption;
        }

        public boolean getCodePointCompare() {
            return this.m_codePointCompare_ == DateUtilsBridge.FORMAT_ABBREV_WEEKDAY;
        }

        public boolean getIgnoreCase() {
            return this.m_ignoreCase_;
        }

        public int getIgnoreCaseOption() {
            return this.m_foldCase_;
        }

        public int compare(String a, String b) {
            if (a == b) {
                return FOLD_CASE_DEFAULT;
            }
            if (a == null) {
                return -1;
            }
            if (b == null) {
                return FOLD_CASE_EXCLUDE_SPECIAL_I;
            }
            if (this.m_ignoreCase_) {
                return compareCaseInsensitive(a, b);
            }
            return compareCaseSensitive(a, b);
        }

        private int compareCaseInsensitive(String s1, String s2) {
            return Normalizer.cmpEquivFold(s1, s2, (this.m_foldCase_ | this.m_codePointCompare_) | UTF16.SUPPLEMENTARY_MIN_VALUE);
        }

        private int compareCaseSensitive(String s1, String s2) {
            int length1 = s1.length();
            int length2 = s2.length();
            int minlength = length1;
            int result = FOLD_CASE_DEFAULT;
            if (length1 < length2) {
                result = -1;
            } else if (length1 > length2) {
                result = FOLD_CASE_EXCLUDE_SPECIAL_I;
                minlength = length2;
            }
            int c1 = '\u0000';
            int c2 = '\u0000';
            int index = FOLD_CASE_DEFAULT;
            while (index < minlength) {
                c1 = s1.charAt(index);
                c2 = s2.charAt(index);
                if (c1 != c2) {
                    break;
                }
                index += FOLD_CASE_EXCLUDE_SPECIAL_I;
            }
            if (index == minlength) {
                return result;
            }
            boolean codepointcompare = this.m_codePointCompare_ == DateUtilsBridge.FORMAT_ABBREV_WEEKDAY;
            if (c1 >= UTF16.SURROGATE_MIN_VALUE && c2 >= UTF16.SURROGATE_MIN_VALUE && codepointcompare) {
                if ((c1 > UTF16.LEAD_SURROGATE_MAX_VALUE || index + FOLD_CASE_EXCLUDE_SPECIAL_I == length1 || !UTF16.isTrailSurrogate(s1.charAt(index + FOLD_CASE_EXCLUDE_SPECIAL_I))) && !(UTF16.isTrailSurrogate(c1) && index != 0 && UTF16.isLeadSurrogate(s1.charAt(index - 1)))) {
                    c1 = (char) (c1 - 10240);
                }
                if ((c2 > UTF16.LEAD_SURROGATE_MAX_VALUE || index + FOLD_CASE_EXCLUDE_SPECIAL_I == length2 || !UTF16.isTrailSurrogate(s2.charAt(index + FOLD_CASE_EXCLUDE_SPECIAL_I))) && !(UTF16.isTrailSurrogate(c2) && index != 0 && UTF16.isLeadSurrogate(s2.charAt(index - 1)))) {
                    c2 = (char) (c2 - 10240);
                }
            }
            return c1 - c2;
        }
    }

    private UTF16() {
    }

    public static int charAt(String source, int offset16) {
        char single = source.charAt(offset16);
        if (single < UCharacter.MIN_SURROGATE) {
            return single;
        }
        return _charAt(source, offset16, single);
    }

    private static int _charAt(String source, int offset16, char single) {
        if (single > UCharacter.MAX_SURROGATE) {
            return single;
        }
        if (single <= UCharacter.MAX_HIGH_SURROGATE) {
            offset16 += SINGLE_CHAR_BOUNDARY;
            if (source.length() != offset16) {
                char trail = source.charAt(offset16);
                if (trail >= UCharacter.MIN_LOW_SURROGATE && trail <= UCharacter.MAX_SURROGATE) {
                    return Character.toCodePoint(single, trail);
                }
            }
        }
        offset16--;
        if (offset16 >= 0) {
            char lead = source.charAt(offset16);
            if (lead >= UCharacter.MIN_SURROGATE && lead <= UCharacter.MAX_HIGH_SURROGATE) {
                return Character.toCodePoint(lead, single);
            }
        }
        return single;
    }

    public static int charAt(CharSequence source, int offset16) {
        char single = source.charAt(offset16);
        if (single < UCharacter.MIN_SURROGATE) {
            return single;
        }
        return _charAt(source, offset16, single);
    }

    private static int _charAt(CharSequence source, int offset16, char single) {
        if (single > UCharacter.MAX_SURROGATE) {
            return single;
        }
        if (single <= UCharacter.MAX_HIGH_SURROGATE) {
            offset16 += SINGLE_CHAR_BOUNDARY;
            if (source.length() != offset16) {
                char trail = source.charAt(offset16);
                if (trail >= UCharacter.MIN_LOW_SURROGATE && trail <= UCharacter.MAX_SURROGATE) {
                    return Character.toCodePoint(single, trail);
                }
            }
        }
        offset16--;
        if (offset16 >= 0) {
            char lead = source.charAt(offset16);
            if (lead >= UCharacter.MIN_SURROGATE && lead <= UCharacter.MAX_HIGH_SURROGATE) {
                return Character.toCodePoint(lead, single);
            }
        }
        return single;
    }

    public static int charAt(StringBuffer source, int offset16) {
        if (offset16 < 0 || offset16 >= source.length()) {
            throw new StringIndexOutOfBoundsException(offset16);
        }
        char single = source.charAt(offset16);
        if (!isSurrogate(single)) {
            return single;
        }
        if (single <= UCharacter.MAX_HIGH_SURROGATE) {
            offset16 += SINGLE_CHAR_BOUNDARY;
            if (source.length() != offset16) {
                char trail = source.charAt(offset16);
                if (isTrailSurrogate(trail)) {
                    return Character.toCodePoint(single, trail);
                }
            }
        }
        offset16--;
        if (offset16 >= 0) {
            char lead = source.charAt(offset16);
            if (isLeadSurrogate(lead)) {
                return Character.toCodePoint(lead, single);
            }
        }
        return single;
    }

    public static int charAt(char[] source, int start, int limit, int offset16) {
        offset16 += start;
        if (offset16 < start || offset16 >= limit) {
            throw new ArrayIndexOutOfBoundsException(offset16);
        }
        char single = source[offset16];
        if (!isSurrogate(single)) {
            return single;
        }
        if (single <= UCharacter.MAX_HIGH_SURROGATE) {
            offset16 += SINGLE_CHAR_BOUNDARY;
            if (offset16 >= limit) {
                return single;
            }
            char trail = source[offset16];
            if (isTrailSurrogate(trail)) {
                return Character.toCodePoint(single, trail);
            }
        } else if (offset16 == start) {
            return single;
        } else {
            char lead = source[offset16 - 1];
            if (isLeadSurrogate(lead)) {
                return Character.toCodePoint(lead, single);
            }
        }
        return single;
    }

    public static int charAt(Replaceable source, int offset16) {
        if (offset16 < 0 || offset16 >= source.length()) {
            throw new StringIndexOutOfBoundsException(offset16);
        }
        char single = source.charAt(offset16);
        if (!isSurrogate(single)) {
            return single;
        }
        if (single <= UCharacter.MAX_HIGH_SURROGATE) {
            offset16 += SINGLE_CHAR_BOUNDARY;
            if (source.length() != offset16) {
                char trail = source.charAt(offset16);
                if (isTrailSurrogate(trail)) {
                    return Character.toCodePoint(single, trail);
                }
            }
        }
        offset16--;
        if (offset16 >= 0) {
            char lead = source.charAt(offset16);
            if (isLeadSurrogate(lead)) {
                return Character.toCodePoint(lead, single);
            }
        }
        return single;
    }

    public static int getCharCount(int char32) {
        if (char32 < SUPPLEMENTARY_MIN_VALUE) {
            return SINGLE_CHAR_BOUNDARY;
        }
        return LEAD_SURROGATE_BOUNDARY;
    }

    public static int bounds(String source, int offset16) {
        char ch = source.charAt(offset16);
        if (isSurrogate(ch)) {
            if (isLeadSurrogate(ch)) {
                offset16 += SINGLE_CHAR_BOUNDARY;
                if (offset16 < source.length() && isTrailSurrogate(source.charAt(offset16))) {
                    return LEAD_SURROGATE_BOUNDARY;
                }
            }
            offset16--;
            if (offset16 >= 0 && isLeadSurrogate(source.charAt(offset16))) {
                return TRAIL_SURROGATE_BOUNDARY;
            }
        }
        return SINGLE_CHAR_BOUNDARY;
    }

    public static int bounds(StringBuffer source, int offset16) {
        char ch = source.charAt(offset16);
        if (isSurrogate(ch)) {
            if (isLeadSurrogate(ch)) {
                offset16 += SINGLE_CHAR_BOUNDARY;
                if (offset16 < source.length() && isTrailSurrogate(source.charAt(offset16))) {
                    return LEAD_SURROGATE_BOUNDARY;
                }
            }
            offset16--;
            if (offset16 >= 0 && isLeadSurrogate(source.charAt(offset16))) {
                return TRAIL_SURROGATE_BOUNDARY;
            }
        }
        return SINGLE_CHAR_BOUNDARY;
    }

    public static int bounds(char[] source, int start, int limit, int offset16) {
        offset16 += start;
        if (offset16 < start || offset16 >= limit) {
            throw new ArrayIndexOutOfBoundsException(offset16);
        }
        char ch = source[offset16];
        if (isSurrogate(ch)) {
            if (isLeadSurrogate(ch)) {
                offset16 += SINGLE_CHAR_BOUNDARY;
                if (offset16 < limit && isTrailSurrogate(source[offset16])) {
                    return LEAD_SURROGATE_BOUNDARY;
                }
            }
            offset16--;
            if (offset16 >= start && isLeadSurrogate(source[offset16])) {
                return TRAIL_SURROGATE_BOUNDARY;
            }
        }
        return SINGLE_CHAR_BOUNDARY;
    }

    public static boolean isSurrogate(char char16) {
        return (char16 & SURROGATE_BITMASK) == SURROGATE_MIN_VALUE;
    }

    public static boolean isTrailSurrogate(char char16) {
        return (char16 & TRAIL_SURROGATE_BITMASK) == TRAIL_SURROGATE_MIN_VALUE;
    }

    public static boolean isLeadSurrogate(char char16) {
        return (char16 & TRAIL_SURROGATE_BITMASK) == SURROGATE_MIN_VALUE;
    }

    public static char getLeadSurrogate(int char32) {
        if (char32 >= SUPPLEMENTARY_MIN_VALUE) {
            return (char) ((char32 >> LEAD_SURROGATE_SHIFT_) + LEAD_SURROGATE_OFFSET_);
        }
        return '\u0000';
    }

    public static char getTrailSurrogate(int char32) {
        if (char32 >= SUPPLEMENTARY_MIN_VALUE) {
            return (char) ((char32 & TRAIL_SURROGATE_MASK_) + TRAIL_SURROGATE_MIN_VALUE);
        }
        return (char) char32;
    }

    public static String valueOf(int char32) {
        if (char32 >= 0 && char32 <= CODEPOINT_MAX_VALUE) {
            return toString(char32);
        }
        throw new IllegalArgumentException("Illegal codepoint");
    }

    public static String valueOf(String source, int offset16) {
        switch (bounds(source, offset16)) {
            case LEAD_SURROGATE_BOUNDARY /*2*/:
                return source.substring(offset16, offset16 + LEAD_SURROGATE_BOUNDARY);
            case TRAIL_SURROGATE_BOUNDARY /*5*/:
                return source.substring(offset16 - 1, offset16 + SINGLE_CHAR_BOUNDARY);
            default:
                return source.substring(offset16, offset16 + SINGLE_CHAR_BOUNDARY);
        }
    }

    public static String valueOf(StringBuffer source, int offset16) {
        switch (bounds(source, offset16)) {
            case LEAD_SURROGATE_BOUNDARY /*2*/:
                return source.substring(offset16, offset16 + LEAD_SURROGATE_BOUNDARY);
            case TRAIL_SURROGATE_BOUNDARY /*5*/:
                return source.substring(offset16 - 1, offset16 + SINGLE_CHAR_BOUNDARY);
            default:
                return source.substring(offset16, offset16 + SINGLE_CHAR_BOUNDARY);
        }
    }

    public static String valueOf(char[] source, int start, int limit, int offset16) {
        switch (bounds(source, start, limit, offset16)) {
            case LEAD_SURROGATE_BOUNDARY /*2*/:
                return new String(source, start + offset16, LEAD_SURROGATE_BOUNDARY);
            case TRAIL_SURROGATE_BOUNDARY /*5*/:
                return new String(source, (start + offset16) - 1, LEAD_SURROGATE_BOUNDARY);
            default:
                return new String(source, start + offset16, SINGLE_CHAR_BOUNDARY);
        }
    }

    public static int findOffsetFromCodePoint(String source, int offset32) {
        int size = source.length();
        int result = CODEPOINT_MIN_VALUE;
        int count = offset32;
        if (offset32 < 0 || offset32 > size) {
            throw new StringIndexOutOfBoundsException(offset32);
        }
        while (result < size && count > 0) {
            if (isLeadSurrogate(source.charAt(result)) && result + SINGLE_CHAR_BOUNDARY < size && isTrailSurrogate(source.charAt(result + SINGLE_CHAR_BOUNDARY))) {
                result += SINGLE_CHAR_BOUNDARY;
            }
            count--;
            result += SINGLE_CHAR_BOUNDARY;
        }
        if (count == 0) {
            return result;
        }
        throw new StringIndexOutOfBoundsException(offset32);
    }

    public static int findOffsetFromCodePoint(StringBuffer source, int offset32) {
        int size = source.length();
        int result = CODEPOINT_MIN_VALUE;
        int count = offset32;
        if (offset32 < 0 || offset32 > size) {
            throw new StringIndexOutOfBoundsException(offset32);
        }
        while (result < size && count > 0) {
            if (isLeadSurrogate(source.charAt(result)) && result + SINGLE_CHAR_BOUNDARY < size && isTrailSurrogate(source.charAt(result + SINGLE_CHAR_BOUNDARY))) {
                result += SINGLE_CHAR_BOUNDARY;
            }
            count--;
            result += SINGLE_CHAR_BOUNDARY;
        }
        if (count == 0) {
            return result;
        }
        throw new StringIndexOutOfBoundsException(offset32);
    }

    public static int findOffsetFromCodePoint(char[] source, int start, int limit, int offset32) {
        int result = start;
        int count = offset32;
        if (offset32 > limit - start) {
            throw new ArrayIndexOutOfBoundsException(offset32);
        }
        while (result < limit && count > 0) {
            if (isLeadSurrogate(source[result]) && result + SINGLE_CHAR_BOUNDARY < limit && isTrailSurrogate(source[result + SINGLE_CHAR_BOUNDARY])) {
                result += SINGLE_CHAR_BOUNDARY;
            }
            count--;
            result += SINGLE_CHAR_BOUNDARY;
        }
        if (count == 0) {
            return result - start;
        }
        throw new ArrayIndexOutOfBoundsException(offset32);
    }

    public static int findCodePointOffset(String source, int offset16) {
        if (offset16 < 0 || offset16 > source.length()) {
            throw new StringIndexOutOfBoundsException(offset16);
        }
        int result = CODEPOINT_MIN_VALUE;
        boolean hadLeadSurrogate = false;
        for (int i = CODEPOINT_MIN_VALUE; i < offset16; i += SINGLE_CHAR_BOUNDARY) {
            char ch = source.charAt(i);
            if (hadLeadSurrogate && isTrailSurrogate(ch)) {
                hadLeadSurrogate = false;
            } else {
                hadLeadSurrogate = isLeadSurrogate(ch);
                result += SINGLE_CHAR_BOUNDARY;
            }
        }
        if (offset16 == source.length()) {
            return result;
        }
        if (hadLeadSurrogate && isTrailSurrogate(source.charAt(offset16))) {
            result--;
        }
        return result;
    }

    public static int findCodePointOffset(StringBuffer source, int offset16) {
        if (offset16 < 0 || offset16 > source.length()) {
            throw new StringIndexOutOfBoundsException(offset16);
        }
        int result = CODEPOINT_MIN_VALUE;
        boolean hadLeadSurrogate = false;
        for (int i = CODEPOINT_MIN_VALUE; i < offset16; i += SINGLE_CHAR_BOUNDARY) {
            char ch = source.charAt(i);
            if (hadLeadSurrogate && isTrailSurrogate(ch)) {
                hadLeadSurrogate = false;
            } else {
                hadLeadSurrogate = isLeadSurrogate(ch);
                result += SINGLE_CHAR_BOUNDARY;
            }
        }
        if (offset16 == source.length()) {
            return result;
        }
        if (hadLeadSurrogate && isTrailSurrogate(source.charAt(offset16))) {
            result--;
        }
        return result;
    }

    public static int findCodePointOffset(char[] source, int start, int limit, int offset16) {
        offset16 += start;
        if (offset16 > limit) {
            throw new StringIndexOutOfBoundsException(offset16);
        }
        int result = CODEPOINT_MIN_VALUE;
        boolean hadLeadSurrogate = false;
        for (int i = start; i < offset16; i += SINGLE_CHAR_BOUNDARY) {
            char ch = source[i];
            if (hadLeadSurrogate && isTrailSurrogate(ch)) {
                hadLeadSurrogate = false;
            } else {
                hadLeadSurrogate = isLeadSurrogate(ch);
                result += SINGLE_CHAR_BOUNDARY;
            }
        }
        if (offset16 == limit) {
            return result;
        }
        if (hadLeadSurrogate && isTrailSurrogate(source[offset16])) {
            result--;
        }
        return result;
    }

    public static StringBuffer append(StringBuffer target, int char32) {
        if (char32 < 0 || char32 > CODEPOINT_MAX_VALUE) {
            throw new IllegalArgumentException("Illegal codepoint: " + Integer.toHexString(char32));
        }
        if (char32 >= SUPPLEMENTARY_MIN_VALUE) {
            target.append(getLeadSurrogate(char32));
            target.append(getTrailSurrogate(char32));
        } else {
            target.append((char) char32);
        }
        return target;
    }

    public static StringBuffer appendCodePoint(StringBuffer target, int cp) {
        return append(target, cp);
    }

    public static int append(char[] target, int limit, int char32) {
        if (char32 < 0 || char32 > CODEPOINT_MAX_VALUE) {
            throw new IllegalArgumentException("Illegal codepoint");
        } else if (char32 >= SUPPLEMENTARY_MIN_VALUE) {
            r0 = limit + SINGLE_CHAR_BOUNDARY;
            target[limit] = getLeadSurrogate(char32);
            limit = r0 + SINGLE_CHAR_BOUNDARY;
            target[r0] = getTrailSurrogate(char32);
            return limit;
        } else {
            r0 = limit + SINGLE_CHAR_BOUNDARY;
            target[limit] = (char) char32;
            return r0;
        }
    }

    public static int countCodePoint(String source) {
        if (source == null || source.length() == 0) {
            return CODEPOINT_MIN_VALUE;
        }
        return findCodePointOffset(source, source.length());
    }

    public static int countCodePoint(StringBuffer source) {
        if (source == null || source.length() == 0) {
            return CODEPOINT_MIN_VALUE;
        }
        return findCodePointOffset(source, source.length());
    }

    public static int countCodePoint(char[] source, int start, int limit) {
        if (source == null || source.length == 0) {
            return CODEPOINT_MIN_VALUE;
        }
        return findCodePointOffset(source, start, limit, limit - start);
    }

    public static void setCharAt(StringBuffer target, int offset16, int char32) {
        int count = SINGLE_CHAR_BOUNDARY;
        char single = target.charAt(offset16);
        if (isSurrogate(single)) {
            if (isLeadSurrogate(single) && target.length() > offset16 + SINGLE_CHAR_BOUNDARY && isTrailSurrogate(target.charAt(offset16 + SINGLE_CHAR_BOUNDARY))) {
                count = LEAD_SURROGATE_BOUNDARY;
            } else if (isTrailSurrogate(single) && offset16 > 0 && isLeadSurrogate(target.charAt(offset16 - 1))) {
                offset16--;
                count = LEAD_SURROGATE_BOUNDARY;
            }
        }
        target.replace(offset16, offset16 + count, valueOf(char32));
    }

    public static int setCharAt(char[] target, int limit, int offset16, int char32) {
        if (offset16 >= limit) {
            throw new ArrayIndexOutOfBoundsException(offset16);
        }
        int count = SINGLE_CHAR_BOUNDARY;
        char single = target[offset16];
        if (isSurrogate(single)) {
            if (isLeadSurrogate(single) && target.length > offset16 + SINGLE_CHAR_BOUNDARY && isTrailSurrogate(target[offset16 + SINGLE_CHAR_BOUNDARY])) {
                count = LEAD_SURROGATE_BOUNDARY;
            } else if (isTrailSurrogate(single) && offset16 > 0 && isLeadSurrogate(target[offset16 - 1])) {
                offset16--;
                count = LEAD_SURROGATE_BOUNDARY;
            }
        }
        String str = valueOf(char32);
        int result = limit;
        int strlength = str.length();
        target[offset16] = str.charAt(CODEPOINT_MIN_VALUE);
        if (count != strlength) {
            System.arraycopy(target, offset16 + count, target, offset16 + strlength, limit - (offset16 + count));
            if (count < strlength) {
                target[offset16 + SINGLE_CHAR_BOUNDARY] = str.charAt(SINGLE_CHAR_BOUNDARY);
                result = limit + SINGLE_CHAR_BOUNDARY;
                if (result >= target.length) {
                    return result;
                }
                target[result] = '\u0000';
                return result;
            }
            result = limit - 1;
            target[result] = '\u0000';
            return result;
        } else if (count != LEAD_SURROGATE_BOUNDARY) {
            return result;
        } else {
            target[offset16 + SINGLE_CHAR_BOUNDARY] = str.charAt(SINGLE_CHAR_BOUNDARY);
            return result;
        }
    }

    public static int moveCodePointOffset(String source, int offset16, int shift32) {
        int result = offset16;
        int size = source.length();
        if (offset16 < 0 || offset16 > size) {
            throw new StringIndexOutOfBoundsException(offset16);
        }
        int count;
        if (shift32 > 0) {
            if (shift32 + offset16 > size) {
                throw new StringIndexOutOfBoundsException(offset16);
            }
            count = shift32;
            while (result < size && count > 0) {
                if (isLeadSurrogate(source.charAt(result)) && result + SINGLE_CHAR_BOUNDARY < size && isTrailSurrogate(source.charAt(result + SINGLE_CHAR_BOUNDARY))) {
                    result += SINGLE_CHAR_BOUNDARY;
                }
                count--;
                result += SINGLE_CHAR_BOUNDARY;
            }
        } else if (offset16 + shift32 < 0) {
            throw new StringIndexOutOfBoundsException(offset16);
        } else {
            count = -shift32;
            while (count > 0) {
                result--;
                if (result < 0) {
                    break;
                }
                if (isTrailSurrogate(source.charAt(result)) && result > 0 && isLeadSurrogate(source.charAt(result - 1))) {
                    result--;
                }
                count--;
            }
        }
        if (count == 0) {
            return result;
        }
        throw new StringIndexOutOfBoundsException(shift32);
    }

    public static int moveCodePointOffset(StringBuffer source, int offset16, int shift32) {
        int result = offset16;
        int size = source.length();
        if (offset16 < 0 || offset16 > size) {
            throw new StringIndexOutOfBoundsException(offset16);
        }
        int count;
        if (shift32 > 0) {
            if (shift32 + offset16 > size) {
                throw new StringIndexOutOfBoundsException(offset16);
            }
            count = shift32;
            while (result < size && count > 0) {
                if (isLeadSurrogate(source.charAt(result)) && result + SINGLE_CHAR_BOUNDARY < size && isTrailSurrogate(source.charAt(result + SINGLE_CHAR_BOUNDARY))) {
                    result += SINGLE_CHAR_BOUNDARY;
                }
                count--;
                result += SINGLE_CHAR_BOUNDARY;
            }
        } else if (offset16 + shift32 < 0) {
            throw new StringIndexOutOfBoundsException(offset16);
        } else {
            count = -shift32;
            while (count > 0) {
                result--;
                if (result < 0) {
                    break;
                }
                if (isTrailSurrogate(source.charAt(result)) && result > 0 && isLeadSurrogate(source.charAt(result - 1))) {
                    result--;
                }
                count--;
            }
        }
        if (count == 0) {
            return result;
        }
        throw new StringIndexOutOfBoundsException(shift32);
    }

    public static int moveCodePointOffset(char[] source, int start, int limit, int offset16, int shift32) {
        int size = source.length;
        int result = offset16 + start;
        if (start < 0 || limit < start) {
            throw new StringIndexOutOfBoundsException(start);
        } else if (limit > size) {
            throw new StringIndexOutOfBoundsException(limit);
        } else if (offset16 < 0 || result > limit) {
            throw new StringIndexOutOfBoundsException(offset16);
        } else {
            int count;
            if (shift32 > 0) {
                if (shift32 + result > size) {
                    throw new StringIndexOutOfBoundsException(result);
                }
                count = shift32;
                while (result < limit && count > 0) {
                    if (isLeadSurrogate(source[result]) && result + SINGLE_CHAR_BOUNDARY < limit && isTrailSurrogate(source[result + SINGLE_CHAR_BOUNDARY])) {
                        result += SINGLE_CHAR_BOUNDARY;
                    }
                    count--;
                    result += SINGLE_CHAR_BOUNDARY;
                }
            } else if (result + shift32 < start) {
                throw new StringIndexOutOfBoundsException(result);
            } else {
                count = -shift32;
                while (count > 0) {
                    result--;
                    if (result < start) {
                        break;
                    }
                    if (isTrailSurrogate(source[result]) && result > start && isLeadSurrogate(source[result - 1])) {
                        result--;
                    }
                    count--;
                }
            }
            if (count == 0) {
                return result - start;
            }
            throw new StringIndexOutOfBoundsException(shift32);
        }
    }

    public static StringBuffer insert(StringBuffer target, int offset16, int char32) {
        String str = valueOf(char32);
        if (offset16 != target.length() && bounds(target, offset16) == TRAIL_SURROGATE_BOUNDARY) {
            offset16 += SINGLE_CHAR_BOUNDARY;
        }
        target.insert(offset16, str);
        return target;
    }

    public static int insert(char[] target, int limit, int offset16, int char32) {
        String str = valueOf(char32);
        if (offset16 != limit && bounds(target, CODEPOINT_MIN_VALUE, limit, offset16) == TRAIL_SURROGATE_BOUNDARY) {
            offset16 += SINGLE_CHAR_BOUNDARY;
        }
        int size = str.length();
        if (limit + size > target.length) {
            throw new ArrayIndexOutOfBoundsException(offset16 + size);
        }
        System.arraycopy(target, offset16, target, offset16 + size, limit - offset16);
        target[offset16] = str.charAt(CODEPOINT_MIN_VALUE);
        if (size == LEAD_SURROGATE_BOUNDARY) {
            target[offset16 + SINGLE_CHAR_BOUNDARY] = str.charAt(SINGLE_CHAR_BOUNDARY);
        }
        return limit + size;
    }

    public static StringBuffer delete(StringBuffer target, int offset16) {
        int count = SINGLE_CHAR_BOUNDARY;
        switch (bounds(target, offset16)) {
            case LEAD_SURROGATE_BOUNDARY /*2*/:
                count = LEAD_SURROGATE_BOUNDARY;
                break;
            case TRAIL_SURROGATE_BOUNDARY /*5*/:
                count = LEAD_SURROGATE_BOUNDARY;
                offset16--;
                break;
        }
        target.delete(offset16, offset16 + count);
        return target;
    }

    public static int delete(char[] target, int limit, int offset16) {
        int count = SINGLE_CHAR_BOUNDARY;
        switch (bounds(target, CODEPOINT_MIN_VALUE, limit, offset16)) {
            case LEAD_SURROGATE_BOUNDARY /*2*/:
                count = LEAD_SURROGATE_BOUNDARY;
                break;
            case TRAIL_SURROGATE_BOUNDARY /*5*/:
                count = LEAD_SURROGATE_BOUNDARY;
                offset16--;
                break;
        }
        System.arraycopy(target, offset16 + count, target, offset16, limit - (offset16 + count));
        target[limit - count] = '\u0000';
        return limit - count;
    }

    public static int indexOf(String source, int char32) {
        if (char32 < 0 || char32 > CODEPOINT_MAX_VALUE) {
            throw new IllegalArgumentException("Argument char32 is not a valid codepoint");
        } else if (char32 < SURROGATE_MIN_VALUE || (char32 > TRAIL_SURROGATE_MAX_VALUE && char32 < SUPPLEMENTARY_MIN_VALUE)) {
            return source.indexOf((char) char32);
        } else {
            if (char32 >= SUPPLEMENTARY_MIN_VALUE) {
                return source.indexOf(toString(char32));
            }
            int result = source.indexOf((char) char32);
            if (result >= 0) {
                if (isLeadSurrogate((char) char32) && result < source.length() - 1 && isTrailSurrogate(source.charAt(result + SINGLE_CHAR_BOUNDARY))) {
                    return indexOf(source, char32, result + SINGLE_CHAR_BOUNDARY);
                }
                if (result > 0 && isLeadSurrogate(source.charAt(result - 1))) {
                    return indexOf(source, char32, result + SINGLE_CHAR_BOUNDARY);
                }
            }
            return result;
        }
    }

    public static int indexOf(String source, String str) {
        int strLength = str.length();
        if (!isTrailSurrogate(str.charAt(CODEPOINT_MIN_VALUE)) && !isLeadSurrogate(str.charAt(strLength - 1))) {
            return source.indexOf(str);
        }
        int result = source.indexOf(str);
        int resultEnd = result + strLength;
        if (result >= 0) {
            if (isLeadSurrogate(str.charAt(strLength - 1)) && result < source.length() - 1 && isTrailSurrogate(source.charAt(resultEnd + SINGLE_CHAR_BOUNDARY))) {
                return indexOf(source, str, resultEnd + SINGLE_CHAR_BOUNDARY);
            }
            if (isTrailSurrogate(str.charAt(CODEPOINT_MIN_VALUE)) && result > 0 && isLeadSurrogate(source.charAt(result - 1))) {
                return indexOf(source, str, resultEnd + SINGLE_CHAR_BOUNDARY);
            }
        }
        return result;
    }

    public static int indexOf(String source, int char32, int fromIndex) {
        if (char32 < 0 || char32 > CODEPOINT_MAX_VALUE) {
            throw new IllegalArgumentException("Argument char32 is not a valid codepoint");
        } else if (char32 < SURROGATE_MIN_VALUE || (char32 > TRAIL_SURROGATE_MAX_VALUE && char32 < SUPPLEMENTARY_MIN_VALUE)) {
            return source.indexOf((char) char32, fromIndex);
        } else {
            if (char32 >= SUPPLEMENTARY_MIN_VALUE) {
                return source.indexOf(toString(char32), fromIndex);
            }
            int result = source.indexOf((char) char32, fromIndex);
            if (result >= 0) {
                if (isLeadSurrogate((char) char32) && result < source.length() - 1 && isTrailSurrogate(source.charAt(result + SINGLE_CHAR_BOUNDARY))) {
                    return indexOf(source, char32, result + SINGLE_CHAR_BOUNDARY);
                }
                if (result > 0 && isLeadSurrogate(source.charAt(result - 1))) {
                    return indexOf(source, char32, result + SINGLE_CHAR_BOUNDARY);
                }
            }
            return result;
        }
    }

    public static int indexOf(String source, String str, int fromIndex) {
        int strLength = str.length();
        if (!isTrailSurrogate(str.charAt(CODEPOINT_MIN_VALUE)) && !isLeadSurrogate(str.charAt(strLength - 1))) {
            return source.indexOf(str, fromIndex);
        }
        int result = source.indexOf(str, fromIndex);
        int resultEnd = result + strLength;
        if (result >= 0) {
            if (isLeadSurrogate(str.charAt(strLength - 1)) && result < source.length() - 1 && isTrailSurrogate(source.charAt(resultEnd))) {
                return indexOf(source, str, resultEnd + SINGLE_CHAR_BOUNDARY);
            }
            if (isTrailSurrogate(str.charAt(CODEPOINT_MIN_VALUE)) && result > 0 && isLeadSurrogate(source.charAt(result - 1))) {
                return indexOf(source, str, resultEnd + SINGLE_CHAR_BOUNDARY);
            }
        }
        return result;
    }

    public static int lastIndexOf(String source, int char32) {
        if (char32 < 0 || char32 > CODEPOINT_MAX_VALUE) {
            throw new IllegalArgumentException("Argument char32 is not a valid codepoint");
        } else if (char32 < SURROGATE_MIN_VALUE || (char32 > TRAIL_SURROGATE_MAX_VALUE && char32 < SUPPLEMENTARY_MIN_VALUE)) {
            return source.lastIndexOf((char) char32);
        } else {
            if (char32 >= SUPPLEMENTARY_MIN_VALUE) {
                return source.lastIndexOf(toString(char32));
            }
            int result = source.lastIndexOf((char) char32);
            if (result >= 0) {
                if (isLeadSurrogate((char) char32) && result < source.length() - 1 && isTrailSurrogate(source.charAt(result + SINGLE_CHAR_BOUNDARY))) {
                    return lastIndexOf(source, char32, result - 1);
                }
                if (result > 0 && isLeadSurrogate(source.charAt(result - 1))) {
                    return lastIndexOf(source, char32, result - 1);
                }
            }
            return result;
        }
    }

    public static int lastIndexOf(String source, String str) {
        int strLength = str.length();
        if (!isTrailSurrogate(str.charAt(CODEPOINT_MIN_VALUE)) && !isLeadSurrogate(str.charAt(strLength - 1))) {
            return source.lastIndexOf(str);
        }
        int result = source.lastIndexOf(str);
        if (result >= 0) {
            if (isLeadSurrogate(str.charAt(strLength - 1)) && result < source.length() - 1 && isTrailSurrogate(source.charAt((result + strLength) + SINGLE_CHAR_BOUNDARY))) {
                return lastIndexOf(source, str, result - 1);
            }
            if (isTrailSurrogate(str.charAt(CODEPOINT_MIN_VALUE)) && result > 0 && isLeadSurrogate(source.charAt(result - 1))) {
                return lastIndexOf(source, str, result - 1);
            }
        }
        return result;
    }

    public static int lastIndexOf(String source, int char32, int fromIndex) {
        if (char32 < 0 || char32 > CODEPOINT_MAX_VALUE) {
            throw new IllegalArgumentException("Argument char32 is not a valid codepoint");
        } else if (char32 < SURROGATE_MIN_VALUE || (char32 > TRAIL_SURROGATE_MAX_VALUE && char32 < SUPPLEMENTARY_MIN_VALUE)) {
            return source.lastIndexOf((char) char32, fromIndex);
        } else {
            if (char32 >= SUPPLEMENTARY_MIN_VALUE) {
                return source.lastIndexOf(toString(char32), fromIndex);
            }
            int result = source.lastIndexOf((char) char32, fromIndex);
            if (result >= 0) {
                if (isLeadSurrogate((char) char32) && result < source.length() - 1 && isTrailSurrogate(source.charAt(result + SINGLE_CHAR_BOUNDARY))) {
                    return lastIndexOf(source, char32, result - 1);
                }
                if (result > 0 && isLeadSurrogate(source.charAt(result - 1))) {
                    return lastIndexOf(source, char32, result - 1);
                }
            }
            return result;
        }
    }

    public static int lastIndexOf(String source, String str, int fromIndex) {
        int strLength = str.length();
        if (!isTrailSurrogate(str.charAt(CODEPOINT_MIN_VALUE)) && !isLeadSurrogate(str.charAt(strLength - 1))) {
            return source.lastIndexOf(str, fromIndex);
        }
        int result = source.lastIndexOf(str, fromIndex);
        if (result >= 0) {
            if (isLeadSurrogate(str.charAt(strLength - 1)) && result < source.length() - 1 && isTrailSurrogate(source.charAt(result + strLength))) {
                return lastIndexOf(source, str, result - 1);
            }
            if (isTrailSurrogate(str.charAt(CODEPOINT_MIN_VALUE)) && result > 0 && isLeadSurrogate(source.charAt(result - 1))) {
                return lastIndexOf(source, str, result - 1);
            }
        }
        return result;
    }

    public static String replace(String source, int oldChar32, int newChar32) {
        if (oldChar32 <= 0 || oldChar32 > CODEPOINT_MAX_VALUE) {
            throw new IllegalArgumentException("Argument oldChar32 is not a valid codepoint");
        } else if (newChar32 <= 0 || newChar32 > CODEPOINT_MAX_VALUE) {
            throw new IllegalArgumentException("Argument newChar32 is not a valid codepoint");
        } else {
            int index = indexOf(source, oldChar32);
            if (index == -1) {
                return source;
            }
            String newChar32Str = toString(newChar32);
            int oldChar32Size = SINGLE_CHAR_BOUNDARY;
            int newChar32Size = newChar32Str.length();
            StringBuffer result = new StringBuffer(source);
            int resultIndex = index;
            if (oldChar32 >= SUPPLEMENTARY_MIN_VALUE) {
                oldChar32Size = LEAD_SURROGATE_BOUNDARY;
            }
            while (index != -1) {
                result.replace(resultIndex, resultIndex + oldChar32Size, newChar32Str);
                int lastEndIndex = index + oldChar32Size;
                index = indexOf(source, oldChar32, lastEndIndex);
                resultIndex += (newChar32Size + index) - lastEndIndex;
            }
            return result.toString();
        }
    }

    public static String replace(String source, String oldStr, String newStr) {
        int index = indexOf(source, oldStr);
        if (index == -1) {
            return source;
        }
        int oldStrSize = oldStr.length();
        int newStrSize = newStr.length();
        StringBuffer result = new StringBuffer(source);
        int resultIndex = index;
        while (index != -1) {
            result.replace(resultIndex, resultIndex + oldStrSize, newStr);
            int lastEndIndex = index + oldStrSize;
            index = indexOf(source, oldStr, lastEndIndex);
            resultIndex += (newStrSize + index) - lastEndIndex;
        }
        return result.toString();
    }

    public static StringBuffer reverse(StringBuffer source) {
        int length = source.length();
        StringBuffer result = new StringBuffer(length);
        int i = length;
        while (true) {
            int i2 = i - 1;
            if (i <= 0) {
                return result;
            }
            char ch = source.charAt(i2);
            if (isTrailSurrogate(ch) && i2 > 0) {
                char ch2 = source.charAt(i2 - 1);
                if (isLeadSurrogate(ch2)) {
                    result.append(ch2);
                    result.append(ch);
                    i2--;
                    i = i2;
                }
            }
            result.append(ch);
            i = i2;
        }
    }

    public static boolean hasMoreCodePointsThan(String source, int number) {
        if (number < 0) {
            return true;
        }
        if (source == null) {
            return false;
        }
        int length = source.length();
        if (((length + SINGLE_CHAR_BOUNDARY) >> SINGLE_CHAR_BOUNDARY) > number) {
            return true;
        }
        int maxsupplementary = length - number;
        if (maxsupplementary <= 0) {
            return false;
        }
        int start = CODEPOINT_MIN_VALUE;
        while (length != 0) {
            if (number == 0) {
                return true;
            }
            int start2 = start + SINGLE_CHAR_BOUNDARY;
            if (isLeadSurrogate(source.charAt(start)) && start2 != length && isTrailSurrogate(source.charAt(start2))) {
                start2 += SINGLE_CHAR_BOUNDARY;
                maxsupplementary--;
                if (maxsupplementary <= 0) {
                    return false;
                }
            }
            number--;
            start = start2;
        }
        return false;
    }

    public static boolean hasMoreCodePointsThan(char[] source, int start, int limit, int number) {
        int length = limit - start;
        if (length < 0 || start < 0 || limit < 0) {
            throw new IndexOutOfBoundsException("Start and limit indexes should be non-negative and start <= limit");
        } else if (number < 0) {
            return true;
        } else {
            if (source == null) {
                return false;
            }
            if (((length + SINGLE_CHAR_BOUNDARY) >> SINGLE_CHAR_BOUNDARY) > number) {
                return true;
            }
            int maxsupplementary = length - number;
            if (maxsupplementary <= 0) {
                return false;
            }
            while (true) {
                int start2 = start;
                if (length == 0) {
                    return false;
                }
                if (number == 0) {
                    return true;
                }
                start = start2 + SINGLE_CHAR_BOUNDARY;
                if (isLeadSurrogate(source[start2]) && start != limit && isTrailSurrogate(source[start])) {
                    start += SINGLE_CHAR_BOUNDARY;
                    maxsupplementary--;
                    if (maxsupplementary <= 0) {
                        return false;
                    }
                }
                number--;
            }
        }
    }

    public static boolean hasMoreCodePointsThan(StringBuffer source, int number) {
        if (number < 0) {
            return true;
        }
        if (source == null) {
            return false;
        }
        int length = source.length();
        if (((length + SINGLE_CHAR_BOUNDARY) >> SINGLE_CHAR_BOUNDARY) > number) {
            return true;
        }
        int maxsupplementary = length - number;
        if (maxsupplementary <= 0) {
            return false;
        }
        int start = CODEPOINT_MIN_VALUE;
        while (length != 0) {
            if (number == 0) {
                return true;
            }
            int start2 = start + SINGLE_CHAR_BOUNDARY;
            if (isLeadSurrogate(source.charAt(start)) && start2 != length && isTrailSurrogate(source.charAt(start2))) {
                start2 += SINGLE_CHAR_BOUNDARY;
                maxsupplementary--;
                if (maxsupplementary <= 0) {
                    return false;
                }
            }
            number--;
            start = start2;
        }
        return false;
    }

    public static String newString(int[] codePoints, int offset, int count) {
        if (count < 0) {
            throw new IllegalArgumentException();
        }
        char[] chars = new char[count];
        int w = CODEPOINT_MIN_VALUE;
        int e = offset + count;
        for (int r = offset; r < e; r += SINGLE_CHAR_BOUNDARY) {
            int cp = codePoints[r];
            if (cp < 0 || cp > CODEPOINT_MAX_VALUE) {
                throw new IllegalArgumentException();
            }
            while (cp < SUPPLEMENTARY_MIN_VALUE) {
                try {
                    chars[w] = (char) cp;
                    w += SINGLE_CHAR_BOUNDARY;
                    break;
                } catch (IndexOutOfBoundsException e2) {
                    char[] temp = new char[((int) Math.ceil((((double) codePoints.length) * ((double) (w + LEAD_SURROGATE_BOUNDARY))) / ((double) ((r - offset) + SINGLE_CHAR_BOUNDARY))))];
                    System.arraycopy(chars, CODEPOINT_MIN_VALUE, temp, CODEPOINT_MIN_VALUE, w);
                    chars = temp;
                }
            }
            chars[w] = (char) ((cp >> LEAD_SURROGATE_SHIFT_) + LEAD_SURROGATE_OFFSET_);
            chars[w + SINGLE_CHAR_BOUNDARY] = (char) ((cp & TRAIL_SURROGATE_MASK_) + TRAIL_SURROGATE_MIN_VALUE);
            w += LEAD_SURROGATE_BOUNDARY;
        }
        return new String(chars, CODEPOINT_MIN_VALUE, w);
    }

    public static int getSingleCodePoint(CharSequence s) {
        if (s == null || s.length() == 0) {
            return -1;
        }
        if (s.length() == SINGLE_CHAR_BOUNDARY) {
            return s.charAt(CODEPOINT_MIN_VALUE);
        }
        if (s.length() > LEAD_SURROGATE_BOUNDARY) {
            return -1;
        }
        int cp = Character.codePointAt(s, CODEPOINT_MIN_VALUE);
        if (cp > DexFormat.MAX_TYPE_IDX) {
            return cp;
        }
        return -1;
    }

    public static int compareCodePoint(int codePoint, CharSequence s) {
        int i = CODEPOINT_MIN_VALUE;
        if (s == null) {
            return SINGLE_CHAR_BOUNDARY;
        }
        int strLen = s.length();
        if (strLen == 0) {
            return SINGLE_CHAR_BOUNDARY;
        }
        int diff = codePoint - Character.codePointAt(s, CODEPOINT_MIN_VALUE);
        if (diff != 0) {
            return diff;
        }
        if (strLen != Character.charCount(codePoint)) {
            i = -1;
        }
        return i;
    }

    private static String toString(int ch) {
        if (ch < SUPPLEMENTARY_MIN_VALUE) {
            return String.valueOf((char) ch);
        }
        StringBuilder result = new StringBuilder();
        result.append(getLeadSurrogate(ch));
        result.append(getTrailSurrogate(ch));
        return result.toString();
    }
}
