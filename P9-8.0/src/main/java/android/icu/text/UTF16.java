package android.icu.text;

import android.icu.impl.Utility;
import android.icu.lang.UCharacter;
import java.util.Comparator;

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
            this(false, false, 0);
        }

        public StringComparator(boolean codepointcompare, boolean ignorecase, int foldcaseoption) {
            setCodePointCompare(codepointcompare);
            this.m_ignoreCase_ = ignorecase;
            if (foldcaseoption < 0 || foldcaseoption > 1) {
                throw new IllegalArgumentException("Invalid fold case option");
            }
            this.m_foldCase_ = foldcaseoption;
        }

        public void setCodePointCompare(boolean flag) {
            if (flag) {
                this.m_codePointCompare_ = 32768;
            } else {
                this.m_codePointCompare_ = 0;
            }
        }

        public void setIgnoreCase(boolean ignorecase, int foldcaseoption) {
            this.m_ignoreCase_ = ignorecase;
            if (foldcaseoption < 0 || foldcaseoption > 1) {
                throw new IllegalArgumentException("Invalid fold case option");
            }
            this.m_foldCase_ = foldcaseoption;
        }

        public boolean getCodePointCompare() {
            return this.m_codePointCompare_ == 32768;
        }

        public boolean getIgnoreCase() {
            return this.m_ignoreCase_;
        }

        public int getIgnoreCaseOption() {
            return this.m_foldCase_;
        }

        public int compare(String a, String b) {
            if (Utility.sameObjects(a, b)) {
                return 0;
            }
            if (a == null) {
                return -1;
            }
            if (b == null) {
                return 1;
            }
            if (this.m_ignoreCase_) {
                return compareCaseInsensitive(a, b);
            }
            return compareCaseSensitive(a, b);
        }

        private int compareCaseInsensitive(String s1, String s2) {
            return Normalizer.cmpEquivFold(s1, s2, (this.m_foldCase_ | this.m_codePointCompare_) | 65536);
        }

        private int compareCaseSensitive(String s1, String s2) {
            int length1 = s1.length();
            int length2 = s2.length();
            int minlength = length1;
            int result = 0;
            if (length1 < length2) {
                result = -1;
            } else if (length1 > length2) {
                result = 1;
                minlength = length2;
            }
            int c1 = 0;
            int c2 = 0;
            int index = 0;
            while (index < minlength) {
                c1 = s1.charAt(index);
                c2 = s2.charAt(index);
                if (c1 != c2) {
                    break;
                }
                index++;
            }
            if (index == minlength) {
                return result;
            }
            boolean codepointcompare = this.m_codePointCompare_ == 32768;
            if (c1 >= 55296 && c2 >= 55296 && codepointcompare) {
                if ((c1 > UTF16.LEAD_SURROGATE_MAX_VALUE || index + 1 == length1 || !UTF16.isTrailSurrogate(s1.charAt(index + 1))) && !(UTF16.isTrailSurrogate(c1) && index != 0 && UTF16.isLeadSurrogate(s1.charAt(index - 1)))) {
                    c1 = (char) (c1 - 10240);
                }
                if ((c2 > UTF16.LEAD_SURROGATE_MAX_VALUE || index + 1 == length2 || !UTF16.isTrailSurrogate(s2.charAt(index + 1))) && !(UTF16.isTrailSurrogate(c2) && index != 0 && UTF16.isLeadSurrogate(s2.charAt(index - 1)))) {
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
        if (single < 55296) {
            return single;
        }
        return _charAt(source, offset16, single);
    }

    private static int _charAt(String source, int offset16, char single) {
        if (single > 57343) {
            return single;
        }
        if (single <= UCharacter.MAX_HIGH_SURROGATE) {
            offset16++;
            if (source.length() != offset16) {
                char trail = source.charAt(offset16);
                if (trail >= UCharacter.MIN_LOW_SURROGATE && trail <= 57343) {
                    return Character.toCodePoint(single, trail);
                }
            }
        }
        offset16--;
        if (offset16 >= 0) {
            char lead = source.charAt(offset16);
            if (lead >= 55296 && lead <= UCharacter.MAX_HIGH_SURROGATE) {
                return Character.toCodePoint(lead, single);
            }
        }
        return single;
    }

    public static int charAt(CharSequence source, int offset16) {
        char single = source.charAt(offset16);
        if (single < 55296) {
            return single;
        }
        return _charAt(source, offset16, single);
    }

    private static int _charAt(CharSequence source, int offset16, char single) {
        if (single > 57343) {
            return single;
        }
        if (single <= UCharacter.MAX_HIGH_SURROGATE) {
            offset16++;
            if (source.length() != offset16) {
                char trail = source.charAt(offset16);
                if (trail >= UCharacter.MIN_LOW_SURROGATE && trail <= 57343) {
                    return Character.toCodePoint(single, trail);
                }
            }
        }
        offset16--;
        if (offset16 >= 0) {
            char lead = source.charAt(offset16);
            if (lead >= 55296 && lead <= UCharacter.MAX_HIGH_SURROGATE) {
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
            offset16++;
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
            offset16++;
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
            offset16++;
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
        if (char32 < 65536) {
            return 1;
        }
        return 2;
    }

    public static int bounds(String source, int offset16) {
        char ch = source.charAt(offset16);
        if (isSurrogate(ch)) {
            if (isLeadSurrogate(ch)) {
                offset16++;
                if (offset16 < source.length() && isTrailSurrogate(source.charAt(offset16))) {
                    return 2;
                }
            }
            offset16--;
            if (offset16 >= 0 && isLeadSurrogate(source.charAt(offset16))) {
                return 5;
            }
        }
        return 1;
    }

    public static int bounds(StringBuffer source, int offset16) {
        char ch = source.charAt(offset16);
        if (isSurrogate(ch)) {
            if (isLeadSurrogate(ch)) {
                offset16++;
                if (offset16 < source.length() && isTrailSurrogate(source.charAt(offset16))) {
                    return 2;
                }
            }
            offset16--;
            if (offset16 >= 0 && isLeadSurrogate(source.charAt(offset16))) {
                return 5;
            }
        }
        return 1;
    }

    public static int bounds(char[] source, int start, int limit, int offset16) {
        offset16 += start;
        if (offset16 < start || offset16 >= limit) {
            throw new ArrayIndexOutOfBoundsException(offset16);
        }
        char ch = source[offset16];
        if (isSurrogate(ch)) {
            if (isLeadSurrogate(ch)) {
                offset16++;
                if (offset16 < limit && isTrailSurrogate(source[offset16])) {
                    return 2;
                }
            }
            offset16--;
            if (offset16 >= start && isLeadSurrogate(source[offset16])) {
                return 5;
            }
        }
        return 1;
    }

    public static boolean isSurrogate(char char16) {
        return (char16 & SURROGATE_BITMASK) == 55296;
    }

    public static boolean isTrailSurrogate(char char16) {
        return (char16 & -1024) == 56320;
    }

    public static boolean isLeadSurrogate(char char16) {
        return (char16 & -1024) == 55296;
    }

    public static char getLeadSurrogate(int char32) {
        if (char32 >= 65536) {
            return (char) ((char32 >> 10) + LEAD_SURROGATE_OFFSET_);
        }
        return 0;
    }

    public static char getTrailSurrogate(int char32) {
        if (char32 >= 65536) {
            return (char) ((char32 & 1023) + 56320);
        }
        return (char) char32;
    }

    public static String valueOf(int char32) {
        if (char32 >= 0 && char32 <= 1114111) {
            return toString(char32);
        }
        throw new IllegalArgumentException("Illegal codepoint");
    }

    public static String valueOf(String source, int offset16) {
        switch (bounds(source, offset16)) {
            case 2:
                return source.substring(offset16, offset16 + 2);
            case 5:
                return source.substring(offset16 - 1, offset16 + 1);
            default:
                return source.substring(offset16, offset16 + 1);
        }
    }

    public static String valueOf(StringBuffer source, int offset16) {
        switch (bounds(source, offset16)) {
            case 2:
                return source.substring(offset16, offset16 + 2);
            case 5:
                return source.substring(offset16 - 1, offset16 + 1);
            default:
                return source.substring(offset16, offset16 + 1);
        }
    }

    public static String valueOf(char[] source, int start, int limit, int offset16) {
        switch (bounds(source, start, limit, offset16)) {
            case 2:
                return new String(source, start + offset16, 2);
            case 5:
                return new String(source, (start + offset16) - 1, 2);
            default:
                return new String(source, start + offset16, 1);
        }
    }

    public static int findOffsetFromCodePoint(String source, int offset32) {
        int size = source.length();
        int result = 0;
        int count = offset32;
        if (offset32 < 0 || offset32 > size) {
            throw new StringIndexOutOfBoundsException(offset32);
        }
        while (result < size && count > 0) {
            if (isLeadSurrogate(source.charAt(result)) && result + 1 < size && isTrailSurrogate(source.charAt(result + 1))) {
                result++;
            }
            count--;
            result++;
        }
        if (count == 0) {
            return result;
        }
        throw new StringIndexOutOfBoundsException(offset32);
    }

    public static int findOffsetFromCodePoint(StringBuffer source, int offset32) {
        int size = source.length();
        int result = 0;
        int count = offset32;
        if (offset32 < 0 || offset32 > size) {
            throw new StringIndexOutOfBoundsException(offset32);
        }
        while (result < size && count > 0) {
            if (isLeadSurrogate(source.charAt(result)) && result + 1 < size && isTrailSurrogate(source.charAt(result + 1))) {
                result++;
            }
            count--;
            result++;
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
            if (isLeadSurrogate(source[result]) && result + 1 < limit && isTrailSurrogate(source[result + 1])) {
                result++;
            }
            count--;
            result++;
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
        int result = 0;
        boolean hadLeadSurrogate = false;
        for (int i = 0; i < offset16; i++) {
            char ch = source.charAt(i);
            if (hadLeadSurrogate && isTrailSurrogate(ch)) {
                hadLeadSurrogate = false;
            } else {
                hadLeadSurrogate = isLeadSurrogate(ch);
                result++;
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
        int result = 0;
        boolean hadLeadSurrogate = false;
        for (int i = 0; i < offset16; i++) {
            char ch = source.charAt(i);
            if (hadLeadSurrogate && isTrailSurrogate(ch)) {
                hadLeadSurrogate = false;
            } else {
                hadLeadSurrogate = isLeadSurrogate(ch);
                result++;
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
        int result = 0;
        boolean hadLeadSurrogate = false;
        for (int i = start; i < offset16; i++) {
            char ch = source[i];
            if (hadLeadSurrogate && isTrailSurrogate(ch)) {
                hadLeadSurrogate = false;
            } else {
                hadLeadSurrogate = isLeadSurrogate(ch);
                result++;
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
        if (char32 < 0 || char32 > 1114111) {
            throw new IllegalArgumentException("Illegal codepoint: " + Integer.toHexString(char32));
        }
        if (char32 >= 65536) {
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
        int i;
        if (char32 < 0 || char32 > 1114111) {
            throw new IllegalArgumentException("Illegal codepoint");
        } else if (char32 >= 65536) {
            i = limit + 1;
            target[limit] = getLeadSurrogate(char32);
            limit = i + 1;
            target[i] = getTrailSurrogate(char32);
            return limit;
        } else {
            i = limit + 1;
            target[limit] = (char) char32;
            return i;
        }
    }

    public static int countCodePoint(String source) {
        if (source == null || source.length() == 0) {
            return 0;
        }
        return findCodePointOffset(source, source.length());
    }

    public static int countCodePoint(StringBuffer source) {
        if (source == null || source.length() == 0) {
            return 0;
        }
        return findCodePointOffset(source, source.length());
    }

    public static int countCodePoint(char[] source, int start, int limit) {
        if (source == null || source.length == 0) {
            return 0;
        }
        return findCodePointOffset(source, start, limit, limit - start);
    }

    public static void setCharAt(StringBuffer target, int offset16, int char32) {
        int count = 1;
        char single = target.charAt(offset16);
        if (isSurrogate(single)) {
            if (isLeadSurrogate(single) && target.length() > offset16 + 1 && isTrailSurrogate(target.charAt(offset16 + 1))) {
                count = 2;
            } else if (isTrailSurrogate(single) && offset16 > 0 && isLeadSurrogate(target.charAt(offset16 - 1))) {
                offset16--;
                count = 2;
            }
        }
        target.replace(offset16, offset16 + count, valueOf(char32));
    }

    public static int setCharAt(char[] target, int limit, int offset16, int char32) {
        if (offset16 >= limit) {
            throw new ArrayIndexOutOfBoundsException(offset16);
        }
        int count = 1;
        char single = target[offset16];
        if (isSurrogate(single)) {
            if (isLeadSurrogate(single) && target.length > offset16 + 1 && isTrailSurrogate(target[offset16 + 1])) {
                count = 2;
            } else if (isTrailSurrogate(single) && offset16 > 0 && isLeadSurrogate(target[offset16 - 1])) {
                offset16--;
                count = 2;
            }
        }
        String str = valueOf(char32);
        int result = limit;
        int strlength = str.length();
        target[offset16] = str.charAt(0);
        if (count != strlength) {
            System.arraycopy(target, offset16 + count, target, offset16 + strlength, limit - (offset16 + count));
            if (count < strlength) {
                target[offset16 + 1] = str.charAt(1);
                result = limit + 1;
                if (result >= target.length) {
                    return result;
                }
                target[result] = 0;
                return result;
            }
            result = limit - 1;
            target[result] = 0;
            return result;
        } else if (count != 2) {
            return result;
        } else {
            target[offset16 + 1] = str.charAt(1);
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
                if (isLeadSurrogate(source.charAt(result)) && result + 1 < size && isTrailSurrogate(source.charAt(result + 1))) {
                    result++;
                }
                count--;
                result++;
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
                if (isLeadSurrogate(source.charAt(result)) && result + 1 < size && isTrailSurrogate(source.charAt(result + 1))) {
                    result++;
                }
                count--;
                result++;
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
                    if (isLeadSurrogate(source[result]) && result + 1 < limit && isTrailSurrogate(source[result + 1])) {
                        result++;
                    }
                    count--;
                    result++;
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
        if (offset16 != target.length() && bounds(target, offset16) == 5) {
            offset16++;
        }
        target.insert(offset16, str);
        return target;
    }

    public static int insert(char[] target, int limit, int offset16, int char32) {
        String str = valueOf(char32);
        if (offset16 != limit && bounds(target, 0, limit, offset16) == 5) {
            offset16++;
        }
        int size = str.length();
        if (limit + size > target.length) {
            throw new ArrayIndexOutOfBoundsException(offset16 + size);
        }
        System.arraycopy(target, offset16, target, offset16 + size, limit - offset16);
        target[offset16] = str.charAt(0);
        if (size == 2) {
            target[offset16 + 1] = str.charAt(1);
        }
        return limit + size;
    }

    public static StringBuffer delete(StringBuffer target, int offset16) {
        int count = 1;
        switch (bounds(target, offset16)) {
            case 2:
                count = 2;
                break;
            case 5:
                count = 2;
                offset16--;
                break;
        }
        target.delete(offset16, offset16 + count);
        return target;
    }

    public static int delete(char[] target, int limit, int offset16) {
        int count = 1;
        switch (bounds(target, 0, limit, offset16)) {
            case 2:
                count = 2;
                break;
            case 5:
                count = 2;
                offset16--;
                break;
        }
        System.arraycopy(target, offset16 + count, target, offset16, limit - (offset16 + count));
        target[limit - count] = 0;
        return limit - count;
    }

    public static int indexOf(String source, int char32) {
        if (char32 < 0 || char32 > 1114111) {
            throw new IllegalArgumentException("Argument char32 is not a valid codepoint");
        } else if (char32 < 55296 || (char32 > 57343 && char32 < 65536)) {
            return source.indexOf((char) char32);
        } else {
            if (char32 >= 65536) {
                return source.indexOf(toString(char32));
            }
            int result = source.indexOf((char) char32);
            if (result >= 0) {
                if (isLeadSurrogate((char) char32) && result < source.length() - 1 && isTrailSurrogate(source.charAt(result + 1))) {
                    return indexOf(source, char32, result + 1);
                }
                if (result > 0 && isLeadSurrogate(source.charAt(result - 1))) {
                    return indexOf(source, char32, result + 1);
                }
            }
            return result;
        }
    }

    public static int indexOf(String source, String str) {
        int strLength = str.length();
        if (!isTrailSurrogate(str.charAt(0)) && (isLeadSurrogate(str.charAt(strLength - 1)) ^ 1) != 0) {
            return source.indexOf(str);
        }
        int result = source.indexOf(str);
        int resultEnd = result + strLength;
        if (result >= 0) {
            if (isLeadSurrogate(str.charAt(strLength - 1)) && result < source.length() - 1 && isTrailSurrogate(source.charAt(resultEnd + 1))) {
                return indexOf(source, str, resultEnd + 1);
            }
            if (isTrailSurrogate(str.charAt(0)) && result > 0 && isLeadSurrogate(source.charAt(result - 1))) {
                return indexOf(source, str, resultEnd + 1);
            }
        }
        return result;
    }

    public static int indexOf(String source, int char32, int fromIndex) {
        if (char32 < 0 || char32 > 1114111) {
            throw new IllegalArgumentException("Argument char32 is not a valid codepoint");
        } else if (char32 < 55296 || (char32 > 57343 && char32 < 65536)) {
            return source.indexOf((char) char32, fromIndex);
        } else {
            if (char32 >= 65536) {
                return source.indexOf(toString(char32), fromIndex);
            }
            int result = source.indexOf((char) char32, fromIndex);
            if (result >= 0) {
                if (isLeadSurrogate((char) char32) && result < source.length() - 1 && isTrailSurrogate(source.charAt(result + 1))) {
                    return indexOf(source, char32, result + 1);
                }
                if (result > 0 && isLeadSurrogate(source.charAt(result - 1))) {
                    return indexOf(source, char32, result + 1);
                }
            }
            return result;
        }
    }

    public static int indexOf(String source, String str, int fromIndex) {
        int strLength = str.length();
        if (!isTrailSurrogate(str.charAt(0)) && (isLeadSurrogate(str.charAt(strLength - 1)) ^ 1) != 0) {
            return source.indexOf(str, fromIndex);
        }
        int result = source.indexOf(str, fromIndex);
        int resultEnd = result + strLength;
        if (result >= 0) {
            if (isLeadSurrogate(str.charAt(strLength - 1)) && result < source.length() - 1 && isTrailSurrogate(source.charAt(resultEnd))) {
                return indexOf(source, str, resultEnd + 1);
            }
            if (isTrailSurrogate(str.charAt(0)) && result > 0 && isLeadSurrogate(source.charAt(result - 1))) {
                return indexOf(source, str, resultEnd + 1);
            }
        }
        return result;
    }

    public static int lastIndexOf(String source, int char32) {
        if (char32 < 0 || char32 > 1114111) {
            throw new IllegalArgumentException("Argument char32 is not a valid codepoint");
        } else if (char32 < 55296 || (char32 > 57343 && char32 < 65536)) {
            return source.lastIndexOf((char) char32);
        } else {
            if (char32 >= 65536) {
                return source.lastIndexOf(toString(char32));
            }
            int result = source.lastIndexOf((char) char32);
            if (result >= 0) {
                if (isLeadSurrogate((char) char32) && result < source.length() - 1 && isTrailSurrogate(source.charAt(result + 1))) {
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
        if (!isTrailSurrogate(str.charAt(0)) && (isLeadSurrogate(str.charAt(strLength - 1)) ^ 1) != 0) {
            return source.lastIndexOf(str);
        }
        int result = source.lastIndexOf(str);
        if (result >= 0) {
            if (isLeadSurrogate(str.charAt(strLength - 1)) && result < source.length() - 1 && isTrailSurrogate(source.charAt((result + strLength) + 1))) {
                return lastIndexOf(source, str, result - 1);
            }
            if (isTrailSurrogate(str.charAt(0)) && result > 0 && isLeadSurrogate(source.charAt(result - 1))) {
                return lastIndexOf(source, str, result - 1);
            }
        }
        return result;
    }

    public static int lastIndexOf(String source, int char32, int fromIndex) {
        if (char32 < 0 || char32 > 1114111) {
            throw new IllegalArgumentException("Argument char32 is not a valid codepoint");
        } else if (char32 < 55296 || (char32 > 57343 && char32 < 65536)) {
            return source.lastIndexOf((char) char32, fromIndex);
        } else {
            if (char32 >= 65536) {
                return source.lastIndexOf(toString(char32), fromIndex);
            }
            int result = source.lastIndexOf((char) char32, fromIndex);
            if (result >= 0) {
                if (isLeadSurrogate((char) char32) && result < source.length() - 1 && isTrailSurrogate(source.charAt(result + 1))) {
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
        if (!isTrailSurrogate(str.charAt(0)) && (isLeadSurrogate(str.charAt(strLength - 1)) ^ 1) != 0) {
            return source.lastIndexOf(str, fromIndex);
        }
        int result = source.lastIndexOf(str, fromIndex);
        if (result >= 0) {
            if (isLeadSurrogate(str.charAt(strLength - 1)) && result < source.length() - 1 && isTrailSurrogate(source.charAt(result + strLength))) {
                return lastIndexOf(source, str, result - 1);
            }
            if (isTrailSurrogate(str.charAt(0)) && result > 0 && isLeadSurrogate(source.charAt(result - 1))) {
                return lastIndexOf(source, str, result - 1);
            }
        }
        return result;
    }

    public static String replace(String source, int oldChar32, int newChar32) {
        if (oldChar32 <= 0 || oldChar32 > 1114111) {
            throw new IllegalArgumentException("Argument oldChar32 is not a valid codepoint");
        } else if (newChar32 <= 0 || newChar32 > 1114111) {
            throw new IllegalArgumentException("Argument newChar32 is not a valid codepoint");
        } else {
            int index = indexOf(source, oldChar32);
            if (index == -1) {
                return source;
            }
            String newChar32Str = toString(newChar32);
            int oldChar32Size = 1;
            int newChar32Size = newChar32Str.length();
            StringBuffer result = new StringBuffer(source);
            int resultIndex = index;
            if (oldChar32 >= 65536) {
                oldChar32Size = 2;
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
            int i2 = i;
            i = i2 - 1;
            if (i2 <= 0) {
                return result;
            }
            char ch = source.charAt(i);
            if (isTrailSurrogate(ch) && i > 0) {
                char ch2 = source.charAt(i - 1);
                if (isLeadSurrogate(ch2)) {
                    result.append(ch2);
                    result.append(ch);
                    i--;
                }
            }
            result.append(ch);
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
        if (((length + 1) >> 1) > number) {
            return true;
        }
        int maxsupplementary = length - number;
        if (maxsupplementary <= 0) {
            return false;
        }
        int start = 0;
        while (length != 0) {
            if (number == 0) {
                return true;
            }
            int start2 = start + 1;
            if (isLeadSurrogate(source.charAt(start)) && start2 != length && isTrailSurrogate(source.charAt(start2))) {
                start2++;
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
            if (((length + 1) >> 1) > number) {
                return true;
            }
            int maxsupplementary = length - number;
            if (maxsupplementary <= 0) {
                return false;
            }
            int start2 = start;
            while (length != 0) {
                if (number == 0) {
                    return true;
                }
                start = start2 + 1;
                if (isLeadSurrogate(source[start2]) && start != limit && isTrailSurrogate(source[start])) {
                    start++;
                    maxsupplementary--;
                    if (maxsupplementary <= 0) {
                        return false;
                    }
                }
                number--;
                start2 = start;
            }
            return false;
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
        if (((length + 1) >> 1) > number) {
            return true;
        }
        int maxsupplementary = length - number;
        if (maxsupplementary <= 0) {
            return false;
        }
        int start = 0;
        while (length != 0) {
            if (number == 0) {
                return true;
            }
            int start2 = start + 1;
            if (isLeadSurrogate(source.charAt(start)) && start2 != length && isTrailSurrogate(source.charAt(start2))) {
                start2++;
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
        int w = 0;
        int e = offset + count;
        for (int r = offset; r < e; r++) {
            int cp = codePoints[r];
            if (cp < 0 || cp > 1114111) {
                throw new IllegalArgumentException();
            }
            while (cp < 65536) {
                try {
                    chars[w] = (char) cp;
                    w++;
                    break;
                } catch (IndexOutOfBoundsException e2) {
                    char[] temp = new char[((int) Math.ceil((((double) codePoints.length) * ((double) (w + 2))) / ((double) ((r - offset) + 1))))];
                    System.arraycopy(chars, 0, temp, 0, w);
                    chars = temp;
                }
            }
            chars[w] = (char) ((cp >> 10) + LEAD_SURROGATE_OFFSET_);
            chars[w + 1] = (char) ((cp & 1023) + 56320);
            w += 2;
        }
        return new String(chars, 0, w);
    }

    public static int getSingleCodePoint(CharSequence s) {
        if (s == null || s.length() == 0) {
            return -1;
        }
        if (s.length() == 1) {
            return s.charAt(0);
        }
        if (s.length() > 2) {
            return -1;
        }
        int cp = Character.codePointAt(s, 0);
        if (cp > DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH) {
            return cp;
        }
        return -1;
    }

    public static int compareCodePoint(int codePoint, CharSequence s) {
        int i = 0;
        if (s == null) {
            return 1;
        }
        int strLen = s.length();
        if (strLen == 0) {
            return 1;
        }
        int diff = codePoint - Character.codePointAt(s, 0);
        if (diff != 0) {
            return diff;
        }
        if (strLen != Character.charCount(codePoint)) {
            i = -1;
        }
        return i;
    }

    private static String toString(int ch) {
        if (ch < 65536) {
            return String.valueOf((char) ch);
        }
        StringBuilder result = new StringBuilder();
        result.append(getLeadSurrogate(ch));
        result.append(getTrailSurrogate(ch));
        return result.toString();
    }
}
