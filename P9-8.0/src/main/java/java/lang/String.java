package java.lang;

import java.io.ObjectStreamField;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.Formatter;
import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.regex.Pattern;
import libcore.util.CharsetUtils;

public final class String implements Serializable, Comparable<String>, CharSequence {
    public static final Comparator<String> CASE_INSENSITIVE_ORDER = new CaseInsensitiveComparator();
    private static final ObjectStreamField[] serialPersistentFields = new ObjectStreamField[0];
    private static final long serialVersionUID = -6849794470754667710L;
    private final int count;
    private int hash;

    private static class CaseInsensitiveComparator implements Comparator<String>, Serializable {
        private static final long serialVersionUID = 8575799808933029326L;

        /* synthetic */ CaseInsensitiveComparator(CaseInsensitiveComparator -this0) {
            this();
        }

        private CaseInsensitiveComparator() {
        }

        public int compare(String s1, String s2) {
            int n1 = s1.length();
            int n2 = s2.length();
            int min = Math.min(n1, n2);
            for (int i = 0; i < min; i++) {
                char c1 = s1.charAt(i);
                char c2 = s2.charAt(i);
                if (c1 != c2) {
                    c1 = Character.toUpperCase(c1);
                    c2 = Character.toUpperCase(c2);
                    if (c1 != c2) {
                        c1 = Character.toLowerCase(c1);
                        c2 = Character.toLowerCase(c2);
                        if (c1 != c2) {
                            return c1 - c2;
                        }
                    } else {
                        continue;
                    }
                }
            }
            return n1 - n2;
        }

        private Object readResolve() {
            return String.CASE_INSENSITIVE_ORDER;
        }
    }

    private native String doReplace(char c, char c2);

    private native int fastIndexOf(int i, int i2);

    private native String fastSubstring(int i, int i2);

    public native char charAt(int i);

    public native int compareTo(String str);

    public native String concat(String str);

    native void getCharsNoCheck(int i, int i2, char[] cArr, int i3);

    public native String intern();

    public native char[] toCharArray();

    public String() {
        throw new UnsupportedOperationException("Use StringFactory instead.");
    }

    public String(String original) {
        throw new UnsupportedOperationException("Use StringFactory instead.");
    }

    public String(char[] value) {
        throw new UnsupportedOperationException("Use StringFactory instead.");
    }

    public String(char[] value, int offset, int count) {
        throw new UnsupportedOperationException("Use StringFactory instead.");
    }

    public String(int[] codePoints, int offset, int count) {
        throw new UnsupportedOperationException("Use StringFactory instead.");
    }

    @Deprecated
    public String(byte[] ascii, int hibyte, int offset, int count) {
        throw new UnsupportedOperationException("Use StringFactory instead.");
    }

    @Deprecated
    public String(byte[] ascii, int hibyte) {
        throw new UnsupportedOperationException("Use StringFactory instead.");
    }

    public String(byte[] bytes, int offset, int length, String charsetName) throws UnsupportedEncodingException {
        throw new UnsupportedOperationException("Use StringFactory instead.");
    }

    public String(byte[] bytes, int offset, int length, Charset charset) {
        throw new UnsupportedOperationException("Use StringFactory instead.");
    }

    public String(byte[] bytes, String charsetName) throws UnsupportedEncodingException {
        throw new UnsupportedOperationException("Use StringFactory instead.");
    }

    public String(byte[] bytes, Charset charset) {
        throw new UnsupportedOperationException("Use StringFactory instead.");
    }

    public String(byte[] bytes, int offset, int length) {
        throw new UnsupportedOperationException("Use StringFactory instead.");
    }

    public String(byte[] bytes) {
        throw new UnsupportedOperationException("Use StringFactory instead.");
    }

    public String(StringBuffer buffer) {
        throw new UnsupportedOperationException("Use StringFactory instead.");
    }

    public String(StringBuilder builder) {
        throw new UnsupportedOperationException("Use StringFactory instead.");
    }

    @Deprecated
    String(int offset, int count, char[] value) {
        throw new UnsupportedOperationException("Use StringFactory instead.");
    }

    public int length() {
        return this.count >>> 1;
    }

    public boolean isEmpty() {
        return this.count == 0;
    }

    public int codePointAt(int index) {
        if (index >= 0 && index < length()) {
            return Character.codePointAt((CharSequence) this, index);
        }
        throw new StringIndexOutOfBoundsException(index);
    }

    public int codePointBefore(int index) {
        int i = index - 1;
        if (i >= 0 && i < length()) {
            return Character.codePointBefore((CharSequence) this, index);
        }
        throw new StringIndexOutOfBoundsException(index);
    }

    public int codePointCount(int beginIndex, int endIndex) {
        if (beginIndex >= 0 && endIndex <= length() && beginIndex <= endIndex) {
            return Character.codePointCount((CharSequence) this, beginIndex, endIndex);
        }
        throw new IndexOutOfBoundsException();
    }

    public int offsetByCodePoints(int index, int codePointOffset) {
        if (index >= 0 && index <= length()) {
            return Character.offsetByCodePoints(this, index, codePointOffset);
        }
        throw new IndexOutOfBoundsException();
    }

    void getChars(char[] dst, int dstBegin) {
        getCharsNoCheck(0, length(), dst, dstBegin);
    }

    public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
        if (dst == null) {
            throw new NullPointerException("dst == null");
        } else if (srcBegin < 0) {
            throw new StringIndexOutOfBoundsException(this, srcBegin);
        } else if (srcEnd > length()) {
            throw new StringIndexOutOfBoundsException(this, srcEnd);
        } else {
            int n = srcEnd - srcBegin;
            if (srcEnd < srcBegin) {
                throw new StringIndexOutOfBoundsException(this, srcBegin, n);
            } else if (dstBegin < 0) {
                throw new ArrayIndexOutOfBoundsException("dstBegin < 0. dstBegin=" + dstBegin);
            } else if (dstBegin > dst.length) {
                throw new ArrayIndexOutOfBoundsException("dstBegin > dst.length. dstBegin=" + dstBegin + ", dst.length=" + dst.length);
            } else if (n > dst.length - dstBegin) {
                throw new ArrayIndexOutOfBoundsException("n > dst.length - dstBegin. n=" + n + ", dst.length=" + dst.length + "dstBegin=" + dstBegin);
            } else {
                getCharsNoCheck(srcBegin, srcEnd, dst, dstBegin);
            }
        }
    }

    @Deprecated
    public void getBytes(int srcBegin, int srcEnd, byte[] dst, int dstBegin) {
        if (srcBegin < 0) {
            throw new StringIndexOutOfBoundsException(this, srcBegin);
        } else if (srcEnd > length()) {
            throw new StringIndexOutOfBoundsException(this, srcEnd);
        } else if (srcBegin > srcEnd) {
            throw new StringIndexOutOfBoundsException(this, srcEnd - srcBegin);
        } else {
            int n = srcEnd;
            int i = srcBegin;
            int j = dstBegin;
            while (i < srcEnd) {
                int j2 = j + 1;
                int i2 = i + 1;
                dst[j] = (byte) charAt(i);
                i = i2;
                j = j2;
            }
        }
    }

    public byte[] getBytes(String charsetName) throws UnsupportedEncodingException {
        if (charsetName != null) {
            return getBytes(Charset.forNameUEE(charsetName));
        }
        throw new NullPointerException();
    }

    public byte[] getBytes(Charset charset) {
        if (charset == null) {
            throw new NullPointerException("charset == null");
        }
        int len = length();
        String name = charset.name();
        if ("UTF-8".equals(name)) {
            return CharsetUtils.toUtf8Bytes(this, 0, len);
        }
        if ("ISO-8859-1".equals(name)) {
            return CharsetUtils.toIsoLatin1Bytes(this, 0, len);
        }
        if ("US-ASCII".equals(name)) {
            return CharsetUtils.toAsciiBytes(this, 0, len);
        }
        if ("UTF-16BE".equals(name)) {
            return CharsetUtils.toBigEndianUtf16Bytes(this, 0, len);
        }
        ByteBuffer buffer = charset.encode(this);
        byte[] bytes = new byte[buffer.limit()];
        buffer.get(bytes);
        return bytes;
    }

    public byte[] getBytes() {
        return getBytes(Charset.defaultCharset());
    }

    public boolean equals(Object anObject) {
        if (this == anObject) {
            return true;
        }
        if (anObject instanceof String) {
            String anotherString = (String) anObject;
            int n = length();
            if (n == anotherString.length()) {
                int i = 0;
                while (true) {
                    int n2 = n;
                    n = n2 - 1;
                    if (n2 == 0) {
                        return true;
                    }
                    if (charAt(i) != anotherString.charAt(i)) {
                        return false;
                    }
                    i++;
                }
            }
        }
        return false;
    }

    public boolean contentEquals(StringBuffer sb) {
        return contentEquals((CharSequence) sb);
    }

    private boolean nonSyncContentEquals(AbstractStringBuilder sb) {
        char[] v2 = sb.getValue();
        int n = length();
        if (n != sb.length()) {
            return false;
        }
        for (int i = 0; i < n; i++) {
            if (charAt(i) != v2[i]) {
                return false;
            }
        }
        return true;
    }

    public boolean contentEquals(CharSequence cs) {
        if (cs instanceof AbstractStringBuilder) {
            if (!(cs instanceof StringBuffer)) {
                return nonSyncContentEquals((AbstractStringBuilder) cs);
            }
            boolean nonSyncContentEquals;
            synchronized (cs) {
                nonSyncContentEquals = nonSyncContentEquals((AbstractStringBuilder) cs);
            }
            return nonSyncContentEquals;
        } else if (cs instanceof String) {
            return equals(cs);
        } else {
            int n = length();
            if (n != cs.length()) {
                return false;
            }
            for (int i = 0; i < n; i++) {
                if (charAt(i) != cs.charAt(i)) {
                    return false;
                }
            }
            return true;
        }
    }

    public boolean equalsIgnoreCase(String anotherString) {
        int len = length();
        if (this == anotherString) {
            return true;
        }
        return (anotherString == null || anotherString.length() != len) ? false : regionMatches(true, 0, anotherString, 0, len);
    }

    public int compareToIgnoreCase(String str) {
        return CASE_INSENSITIVE_ORDER.compare(this, str);
    }

    public boolean regionMatches(int toffset, String other, int ooffset, int len) {
        int i = toffset;
        int i2 = ooffset;
        if (ooffset < 0 || toffset < 0 || ((long) toffset) > ((long) length()) - ((long) len) || ((long) ooffset) > ((long) other.length()) - ((long) len)) {
            return false;
        }
        while (true) {
            int i3 = i2;
            int to = i;
            int len2 = len;
            len = len2 - 1;
            if (len2 <= 0) {
                return true;
            }
            i = to + 1;
            i2 = i3 + 1;
            if (charAt(to) != other.charAt(i3)) {
                return false;
            }
        }
    }

    public boolean regionMatches(boolean ignoreCase, int toffset, String other, int ooffset, int len) {
        int i = toffset;
        int i2 = ooffset;
        if (ooffset < 0 || toffset < 0 || ((long) toffset) > ((long) length()) - ((long) len) || ((long) ooffset) > ((long) other.length()) - ((long) len)) {
            return false;
        }
        while (true) {
            int i3 = i2;
            int i4 = i;
            int len2 = len;
            len = len2 - 1;
            if (len2 > 0) {
                i = i4 + 1;
                char c1 = charAt(i4);
                i2 = i3 + 1;
                char c2 = other.charAt(i3);
                if (c1 != c2) {
                    if (!ignoreCase) {
                        break;
                    }
                    char u1 = Character.toUpperCase(c1);
                    char u2 = Character.toUpperCase(c2);
                    if (u1 != u2) {
                        if (Character.toLowerCase(u1) != Character.toLowerCase(u2)) {
                            break;
                        }
                    }
                }
            } else {
                return true;
            }
        }
        return false;
    }

    public boolean startsWith(String prefix, int toffset) {
        int to = toffset;
        int i = 0;
        int pc = prefix.length();
        if (toffset < 0 || toffset > length() - pc) {
            return false;
        }
        while (true) {
            int po = i;
            int to2 = to;
            pc--;
            if (pc < 0) {
                return true;
            }
            to = to2 + 1;
            i = po + 1;
            if (charAt(to2) != prefix.charAt(po)) {
                return false;
            }
        }
    }

    public boolean startsWith(String prefix) {
        return startsWith(prefix, 0);
    }

    public boolean endsWith(String suffix) {
        return startsWith(suffix, length() - suffix.length());
    }

    public int hashCode() {
        int h = this.hash;
        int len = length();
        if (h == 0 && len > 0) {
            for (int i = 0; i < len; i++) {
                h = (h * 31) + charAt(i);
            }
            this.hash = h;
        }
        return h;
    }

    public int indexOf(int ch) {
        return indexOf(ch, 0);
    }

    public int indexOf(int ch, int fromIndex) {
        int max = length();
        if (fromIndex < 0) {
            fromIndex = 0;
        } else if (fromIndex >= max) {
            return -1;
        }
        if (ch >= 65536) {
            return indexOfSupplementary(ch, fromIndex);
        }
        for (int i = fromIndex; i < max; i++) {
            if (charAt(i) == ch) {
                return i;
            }
        }
        return -1;
    }

    private int indexOfSupplementary(int ch, int fromIndex) {
        if (Character.isValidCodePoint(ch)) {
            char hi = Character.highSurrogate(ch);
            char lo = Character.lowSurrogate(ch);
            int max = length() - 1;
            int i = fromIndex;
            while (i < max) {
                if (charAt(i) == hi && charAt(i + 1) == lo) {
                    return i;
                }
                i++;
            }
        }
        return -1;
    }

    public int lastIndexOf(int ch) {
        return lastIndexOf(ch, length() - 1);
    }

    public int lastIndexOf(int ch, int fromIndex) {
        if (ch >= 65536) {
            return lastIndexOfSupplementary(ch, fromIndex);
        }
        for (int i = Math.min(fromIndex, length() - 1); i >= 0; i--) {
            if (charAt(i) == ch) {
                return i;
            }
        }
        return -1;
    }

    private int lastIndexOfSupplementary(int ch, int fromIndex) {
        if (Character.isValidCodePoint(ch)) {
            char hi = Character.highSurrogate(ch);
            char lo = Character.lowSurrogate(ch);
            int i = Math.min(fromIndex, length() - 2);
            while (i >= 0) {
                if (charAt(i) == hi && charAt(i + 1) == lo) {
                    return i;
                }
                i--;
            }
        }
        return -1;
    }

    public int indexOf(String str) {
        return indexOf(str, 0);
    }

    public int indexOf(String str, int fromIndex) {
        return indexOf(this, str, fromIndex);
    }

    static int indexOf(String source, String target, int fromIndex) {
        int sourceLength = source.length();
        int targetLength = target.length();
        if (fromIndex >= sourceLength) {
            if (targetLength != 0) {
                sourceLength = -1;
            }
            return sourceLength;
        }
        if (fromIndex < 0) {
            fromIndex = 0;
        }
        if (targetLength == 0) {
            return fromIndex;
        }
        char first = target.charAt(0);
        int max = sourceLength - targetLength;
        int i = fromIndex;
        while (i <= max) {
            if (source.charAt(i) != first) {
                do {
                    i++;
                    if (i > max) {
                        break;
                    }
                } while (source.charAt(i) != first);
            }
            if (i <= max) {
                int j = i + 1;
                int end = (j + targetLength) - 1;
                int k = 1;
                while (j < end && source.charAt(j) == target.charAt(k)) {
                    j++;
                    k++;
                }
                if (j == end) {
                    return i;
                }
            }
            i++;
        }
        return -1;
    }

    static int indexOf(char[] source, int sourceOffset, int sourceCount, char[] target, int targetOffset, int targetCount, int fromIndex) {
        if (fromIndex >= sourceCount) {
            if (targetCount != 0) {
                sourceCount = -1;
            }
            return sourceCount;
        }
        if (fromIndex < 0) {
            fromIndex = 0;
        }
        if (targetCount == 0) {
            return fromIndex;
        }
        char first = target[targetOffset];
        int max = sourceOffset + (sourceCount - targetCount);
        int i = sourceOffset + fromIndex;
        while (i <= max) {
            if (source[i] != first) {
                do {
                    i++;
                    if (i > max) {
                        break;
                    }
                } while (source[i] != first);
            }
            if (i <= max) {
                int j = i + 1;
                int end = (j + targetCount) - 1;
                int k = targetOffset + 1;
                while (j < end && source[j] == target[k]) {
                    j++;
                    k++;
                }
                if (j == end) {
                    return i - sourceOffset;
                }
            }
            i++;
        }
        return -1;
    }

    public int lastIndexOf(String str) {
        return lastIndexOf(str, length());
    }

    public int lastIndexOf(String str, int fromIndex) {
        return lastIndexOf(this, str, fromIndex);
    }

    static int lastIndexOf(String source, String target, int fromIndex) {
        int sourceLength = source.length();
        int targetLength = target.length();
        int rightIndex = sourceLength - targetLength;
        if (fromIndex < 0) {
            return -1;
        }
        if (fromIndex > rightIndex) {
            fromIndex = rightIndex;
        }
        if (targetLength == 0) {
            return fromIndex;
        }
        int strLastIndex = targetLength - 1;
        char strLastChar = target.charAt(strLastIndex);
        int min = targetLength - 1;
        int i = min + fromIndex;
        while (true) {
            if (i >= min && source.charAt(i) != strLastChar) {
                i--;
            } else if (i < min) {
                return -1;
            } else {
                int j = i - 1;
                int start = j - (targetLength - 1);
                int k = strLastIndex - 1;
                int j2 = j;
                while (j2 > start) {
                    j = j2 - 1;
                    int k2 = k - 1;
                    if (source.charAt(j2) != target.charAt(k)) {
                        i--;
                    } else {
                        k = k2;
                        j2 = j;
                    }
                }
                return start + 1;
            }
        }
    }

    static int lastIndexOf(char[] source, int sourceOffset, int sourceCount, char[] target, int targetOffset, int targetCount, int fromIndex) {
        int rightIndex = sourceCount - targetCount;
        if (fromIndex < 0) {
            return -1;
        }
        if (fromIndex > rightIndex) {
            fromIndex = rightIndex;
        }
        if (targetCount == 0) {
            return fromIndex;
        }
        int strLastIndex = (targetOffset + targetCount) - 1;
        char strLastChar = target[strLastIndex];
        int min = (sourceOffset + targetCount) - 1;
        int i = min + fromIndex;
        while (true) {
            if (i >= min && source[i] != strLastChar) {
                i--;
            } else if (i < min) {
                return -1;
            } else {
                int j = i - 1;
                int start = j - (targetCount - 1);
                int k = strLastIndex - 1;
                int j2 = j;
                while (j2 > start) {
                    j = j2 - 1;
                    int k2 = k - 1;
                    if (source[j2] != target[k]) {
                        i--;
                    } else {
                        k = k2;
                        j2 = j;
                    }
                }
                return (start - sourceOffset) + 1;
            }
        }
    }

    public String substring(int beginIndex) {
        if (beginIndex < 0) {
            throw new StringIndexOutOfBoundsException(this, beginIndex);
        }
        int subLen = length() - beginIndex;
        if (subLen >= 0) {
            return beginIndex == 0 ? this : fastSubstring(beginIndex, subLen);
        } else {
            throw new StringIndexOutOfBoundsException(this, beginIndex);
        }
    }

    public String substring(int beginIndex, int endIndex) {
        if (beginIndex < 0) {
            throw new StringIndexOutOfBoundsException(this, beginIndex);
        } else if (endIndex > length()) {
            throw new StringIndexOutOfBoundsException(this, endIndex);
        } else {
            int subLen = endIndex - beginIndex;
            if (subLen < 0) {
                throw new StringIndexOutOfBoundsException(subLen);
            } else if (beginIndex == 0 && endIndex == length()) {
                return this;
            } else {
                return fastSubstring(beginIndex, subLen);
            }
        }
    }

    public CharSequence subSequence(int beginIndex, int endIndex) {
        return substring(beginIndex, endIndex);
    }

    public String replace(char oldChar, char newChar) {
        if (oldChar != newChar) {
            int len = length();
            for (int i = 0; i < len; i++) {
                if (charAt(i) == oldChar) {
                    return doReplace(oldChar, newChar);
                }
            }
        }
        return this;
    }

    public boolean matches(String regex) {
        return Pattern.matches(regex, this);
    }

    public boolean contains(CharSequence s) {
        return indexOf(s.toString()) > -1;
    }

    public String replaceFirst(String regex, String replacement) {
        return Pattern.compile(regex).matcher(this).replaceFirst(replacement);
    }

    public String replaceAll(String regex, String replacement) {
        return Pattern.compile(regex).matcher(this).replaceAll(replacement);
    }

    public String replace(CharSequence target, CharSequence replacement) {
        if (target == null) {
            throw new NullPointerException("target == null");
        } else if (replacement == null) {
            throw new NullPointerException("replacement == null");
        } else {
            String replacementStr = replacement.toString();
            String targetStr = target.toString();
            int len = length();
            StringBuilder sb;
            if (targetStr.isEmpty()) {
                sb = new StringBuilder((replacementStr.length() * (len + 2)) + len);
                sb.append(replacementStr);
                for (int i = 0; i < len; i++) {
                    sb.append(charAt(i));
                    sb.append(replacementStr);
                }
                return sb.toString();
            }
            int lastMatch = 0;
            sb = null;
            while (true) {
                int currentMatch = indexOf(this, targetStr, lastMatch);
                if (currentMatch == -1) {
                    break;
                }
                if (sb == null) {
                    sb = new StringBuilder(len);
                }
                sb.append((CharSequence) this, lastMatch, currentMatch);
                sb.append(replacementStr);
                lastMatch = currentMatch + targetStr.length();
            }
            if (sb == null) {
                return this;
            }
            sb.append((CharSequence) this, lastMatch, len);
            return sb.toString();
        }
    }

    public String[] split(String regex, int limit) {
        String[] fast = Pattern.fastSplit(regex, this, limit);
        if (fast != null) {
            return fast;
        }
        return Pattern.compile(regex).split(this, limit);
    }

    public String[] split(String regex) {
        return split(regex, 0);
    }

    public static String join(CharSequence delimiter, CharSequence... elements) {
        Objects.requireNonNull(delimiter);
        Objects.requireNonNull(elements);
        StringJoiner joiner = new StringJoiner(delimiter);
        for (CharSequence cs : elements) {
            joiner.add(cs);
        }
        return joiner.toString();
    }

    public static String join(CharSequence delimiter, Iterable<? extends CharSequence> elements) {
        Objects.requireNonNull(delimiter);
        Objects.requireNonNull(elements);
        StringJoiner joiner = new StringJoiner(delimiter);
        for (CharSequence cs : elements) {
            joiner.add(cs);
        }
        return joiner.toString();
    }

    public String toLowerCase(Locale locale) {
        return CaseMapper.toLowerCase(locale, this);
    }

    public String toLowerCase() {
        return toLowerCase(Locale.getDefault());
    }

    public String toUpperCase(Locale locale) {
        return CaseMapper.toUpperCase(locale, this, length());
    }

    public String toUpperCase() {
        return toUpperCase(Locale.getDefault());
    }

    public String trim() {
        int len = length();
        int st = 0;
        while (st < len && charAt(st) <= ' ') {
            st++;
        }
        while (st < len && charAt(len - 1) <= ' ') {
            len--;
        }
        return (st > 0 || len < length()) ? substring(st, len) : this;
    }

    public String toString() {
        return this;
    }

    public static String format(String format, Object... args) {
        return new Formatter().format(format, args).toString();
    }

    public static String format(Locale l, String format, Object... args) {
        return new Formatter(l).format(format, args).toString();
    }

    public static String valueOf(Object obj) {
        return obj == null ? "null" : obj.toString();
    }

    public static String valueOf(char[] data) {
        return StringFactory.newStringFromChars(data);
    }

    public static String valueOf(char[] data, int offset, int count) {
        return StringFactory.newStringFromChars(data, offset, count);
    }

    public static String copyValueOf(char[] data, int offset, int count) {
        return StringFactory.newStringFromChars(data, offset, count);
    }

    public static String copyValueOf(char[] data) {
        return StringFactory.newStringFromChars(data);
    }

    public static String valueOf(boolean b) {
        return b ? "true" : "false";
    }

    public static String valueOf(char c) {
        return StringFactory.newStringFromChars(0, 1, new char[]{c});
    }

    public static String valueOf(int i) {
        return Integer.toString(i);
    }

    public static String valueOf(long l) {
        return Long.toString(l);
    }

    public static String valueOf(float f) {
        return Float.toString(f);
    }

    public static String valueOf(double d) {
        return Double.toString(d);
    }
}
