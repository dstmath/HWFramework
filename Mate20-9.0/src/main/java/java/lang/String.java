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
                    char c12 = Character.toUpperCase(c1);
                    char c22 = Character.toUpperCase(c2);
                    if (c12 != c22) {
                        char c13 = Character.toLowerCase(c12);
                        char c23 = Character.toLowerCase(c22);
                        if (c13 != c23) {
                            return c13 - c23;
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

    private native String fastSubstring(int i, int i2);

    public native char charAt(int i);

    public native int compareTo(String str);

    public native String concat(String str);

    /* access modifiers changed from: package-private */
    public native void getCharsNoCheck(int i, int i2, char[] cArr, int i3);

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

    public String(char[] value, int offset, int count2) {
        throw new UnsupportedOperationException("Use StringFactory instead.");
    }

    public String(int[] codePoints, int offset, int count2) {
        throw new UnsupportedOperationException("Use StringFactory instead.");
    }

    @Deprecated
    public String(byte[] ascii, int hibyte, int offset, int count2) {
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
    String(int offset, int count2, char[] value) {
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

    /* access modifiers changed from: package-private */
    public void getChars(char[] dst, int dstBegin) {
        getCharsNoCheck(0, length(), dst, dstBegin);
    }

    public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
        if (dst == null) {
            throw new NullPointerException("dst == null");
        } else if (srcBegin < 0) {
            throw new StringIndexOutOfBoundsException(this, srcBegin);
        } else if (srcEnd <= length()) {
            int n = srcEnd - srcBegin;
            if (srcEnd < srcBegin) {
                throw new StringIndexOutOfBoundsException(this, srcBegin, n);
            } else if (dstBegin < 0) {
                throw new ArrayIndexOutOfBoundsException("dstBegin < 0. dstBegin=" + dstBegin);
            } else if (dstBegin > dst.length) {
                throw new ArrayIndexOutOfBoundsException("dstBegin > dst.length. dstBegin=" + dstBegin + ", dst.length=" + dst.length);
            } else if (n <= dst.length - dstBegin) {
                getCharsNoCheck(srcBegin, srcEnd, dst, dstBegin);
            } else {
                throw new ArrayIndexOutOfBoundsException("n > dst.length - dstBegin. n=" + n + ", dst.length=" + dst.length + "dstBegin=" + dstBegin);
            }
        } else {
            throw new StringIndexOutOfBoundsException(this, srcEnd);
        }
    }

    @Deprecated
    public void getBytes(int srcBegin, int srcEnd, byte[] dst, int dstBegin) {
        if (srcBegin < 0) {
            throw new StringIndexOutOfBoundsException(this, srcBegin);
        } else if (srcEnd > length()) {
            throw new StringIndexOutOfBoundsException(this, srcEnd);
        } else if (srcBegin <= srcEnd) {
            int n = srcEnd;
            int j = dstBegin;
            for (int j2 = srcBegin; j2 < n; j2++) {
                dst[j] = (byte) charAt(j2);
                j++;
            }
        } else {
            throw new StringIndexOutOfBoundsException(this, srcEnd - srcBegin);
        }
    }

    public byte[] getBytes(String charsetName) throws UnsupportedEncodingException {
        if (charsetName != null) {
            return getBytes(Charset.forNameUEE(charsetName));
        }
        throw new NullPointerException();
    }

    public byte[] getBytes(Charset charset) {
        if (charset != null) {
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
        throw new NullPointerException("charset == null");
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
                int n2 = n;
                int i = 0;
                while (true) {
                    int n3 = n2 - 1;
                    if (n2 == 0) {
                        return true;
                    }
                    if (charAt(i) != anotherString.charAt(i)) {
                        return false;
                    }
                    i++;
                    n2 = n3;
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
        boolean nonSyncContentEquals;
        if (cs instanceof AbstractStringBuilder) {
            if (!(cs instanceof StringBuffer)) {
                return nonSyncContentEquals((AbstractStringBuilder) cs);
            }
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
        if (anotherString == null || anotherString.length() != len || !regionMatches(true, 0, anotherString, 0, len)) {
            return false;
        }
        return true;
    }

    public int compareToIgnoreCase(String str) {
        return CASE_INSENSITIVE_ORDER.compare(this, str);
    }

    public boolean regionMatches(int toffset, String other, int ooffset, int to) {
        int to2 = toffset;
        int po = ooffset;
        if (ooffset < 0 || toffset < 0 || ((long) toffset) > ((long) length()) - ((long) to) || ((long) ooffset) > ((long) other.length()) - ((long) to)) {
            return false;
        }
        while (true) {
            int len = to - 1;
            if (to <= 0) {
                return true;
            }
            int to3 = to2 + 1;
            int po2 = po + 1;
            if (charAt(to2) != other.charAt(po)) {
                return false;
            }
            to2 = to3;
            to = len;
            po = po2;
        }
    }

    public boolean regionMatches(boolean ignoreCase, int toffset, String other, int ooffset, int to) {
        int to2 = toffset;
        int po = ooffset;
        if (ooffset < 0 || toffset < 0 || ((long) toffset) > ((long) length()) - ((long) to) || ((long) ooffset) > ((long) other.length()) - ((long) to)) {
            return false;
        }
        while (true) {
            int len = to - 1;
            if (to <= 0) {
                return true;
            }
            int to3 = to2 + 1;
            char c1 = charAt(to2);
            int po2 = po + 1;
            char c2 = other.charAt(po);
            if (c1 != c2) {
                if (!ignoreCase) {
                    break;
                }
                char u1 = Character.toUpperCase(c1);
                char u2 = Character.toUpperCase(c2);
                if (!(u1 == u2 || Character.toLowerCase(u1) == Character.toLowerCase(u2))) {
                    break;
                }
            }
            to2 = to3;
            to = len;
            po = po2;
        }
        return false;
    }

    public boolean startsWith(String prefix, int toffset) {
        int to = toffset;
        int po = 0;
        int pc = prefix.length();
        if (toffset < 0 || toffset > length() - pc) {
            return false;
        }
        while (true) {
            pc--;
            if (pc < 0) {
                return true;
            }
            int to2 = to + 1;
            int po2 = po + 1;
            if (charAt(to) != prefix.charAt(po)) {
                return false;
            }
            to = to2;
            po = po2;
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
                h = (31 * h) + charAt(i);
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
            for (int i = fromIndex; i < max; i++) {
                if (charAt(i) == hi && charAt(i + 1) == lo) {
                    return i;
                }
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
            for (int i = Math.min(fromIndex, length() - 2); i >= 0; i--) {
                if (charAt(i) == hi && charAt(i + 1) == lo) {
                    return i;
                }
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
        int i = -1;
        if (fromIndex >= sourceLength) {
            if (targetLength == 0) {
                i = sourceLength;
            }
            return i;
        }
        if (fromIndex < 0) {
            fromIndex = 0;
        }
        if (targetLength == 0) {
            return fromIndex;
        }
        char first = target.charAt(0);
        int max = sourceLength - targetLength;
        int i2 = fromIndex;
        while (i2 <= max) {
            int k = 1;
            if (source.charAt(i2) != first) {
                do {
                    i2++;
                    if (i2 > max) {
                        break;
                    }
                } while (source.charAt(i2) != first);
            }
            if (i2 <= max) {
                int j = i2 + 1;
                int end = (j + targetLength) - 1;
                while (j < end && source.charAt(j) == target.charAt(k)) {
                    j++;
                    k++;
                }
                if (j == end) {
                    return i2;
                }
            }
            i2++;
        }
        return -1;
    }

    static int indexOf(char[] source, int sourceOffset, int sourceCount, char[] target, int targetOffset, int targetCount, int fromIndex) {
        int i = -1;
        if (fromIndex >= sourceCount) {
            if (targetCount == 0) {
                i = sourceCount;
            }
            return i;
        }
        if (fromIndex < 0) {
            fromIndex = 0;
        }
        if (targetCount == 0) {
            return fromIndex;
        }
        char first = target[targetOffset];
        int max = (sourceCount - targetCount) + sourceOffset;
        int i2 = sourceOffset + fromIndex;
        while (i2 <= max) {
            if (source[i2] != first) {
                do {
                    i2++;
                    if (i2 > max) {
                        break;
                    }
                } while (source[i2] != first);
            }
            if (i2 <= max) {
                int j = i2 + 1;
                int end = (j + targetCount) - 1;
                int k = targetOffset + 1;
                while (j < end && source[j] == target[k]) {
                    j++;
                    k++;
                }
                if (j == end) {
                    return i2 - sourceOffset;
                }
            }
            i2++;
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
                while (j > start) {
                    int j2 = j - 1;
                    int k2 = k - 1;
                    if (source.charAt(j) != target.charAt(k)) {
                        i--;
                    } else {
                        j = j2;
                        k = k2;
                    }
                }
                return start + 1;
            }
        }
    }

    static int lastIndexOf(char[] source, int sourceOffset, int sourceCount, char[] target, int targetOffset, int targetCount, int fromIndex) {
        int fromIndex2 = fromIndex;
        int rightIndex = sourceCount - targetCount;
        if (fromIndex2 < 0) {
            return -1;
        }
        if (fromIndex2 > rightIndex) {
            fromIndex2 = rightIndex;
        }
        if (targetCount == 0) {
            return fromIndex2;
        }
        int strLastIndex = (targetOffset + targetCount) - 1;
        char strLastChar = target[strLastIndex];
        int min = (sourceOffset + targetCount) - 1;
        int i = min + fromIndex2;
        while (true) {
            if (i >= min && source[i] != strLastChar) {
                i--;
            } else if (i < min) {
                return -1;
            } else {
                int j = i - 1;
                int start = j - (targetCount - 1);
                int k = strLastIndex - 1;
                while (j > start) {
                    int j2 = j - 1;
                    int k2 = k - 1;
                    if (source[j] != target[k]) {
                        i--;
                    } else {
                        j = j2;
                        k = k2;
                    }
                }
                return (start - sourceOffset) + 1;
            }
        }
    }

    public String substring(int beginIndex) {
        if (beginIndex >= 0) {
            int subLen = length() - beginIndex;
            if (subLen >= 0) {
                return beginIndex == 0 ? this : fastSubstring(beginIndex, subLen);
            }
            throw new StringIndexOutOfBoundsException(this, beginIndex);
        }
        throw new StringIndexOutOfBoundsException(this, beginIndex);
    }

    public String substring(int beginIndex, int endIndex) {
        if (beginIndex < 0) {
            throw new StringIndexOutOfBoundsException(this, beginIndex);
        } else if (endIndex <= length()) {
            int subLen = endIndex - beginIndex;
            if (subLen < 0) {
                throw new StringIndexOutOfBoundsException(subLen);
            } else if (beginIndex == 0 && endIndex == length()) {
                return this;
            } else {
                return fastSubstring(beginIndex, subLen);
            }
        } else {
            throw new StringIndexOutOfBoundsException(this, endIndex);
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
        } else if (replacement != null) {
            String replacementStr = replacement.toString();
            String targetStr = target.toString();
            int len = length();
            if (targetStr.isEmpty()) {
                StringBuilder sb = new StringBuilder((replacementStr.length() * (len + 2)) + len);
                sb.append(replacementStr);
                for (int i = 0; i < len; i++) {
                    sb.append(charAt(i));
                    sb.append(replacementStr);
                }
                return sb.toString();
            }
            int lastMatch = 0;
            StringBuilder sb2 = null;
            while (true) {
                int currentMatch = indexOf(this, targetStr, lastMatch);
                if (currentMatch == -1) {
                    break;
                }
                if (sb2 == null) {
                    sb2 = new StringBuilder(len);
                }
                sb2.append((CharSequence) this, lastMatch, currentMatch);
                sb2.append(replacementStr);
                lastMatch = currentMatch + targetStr.length();
            }
            if (sb2 == null) {
                return this;
            }
            sb2.append((CharSequence) this, lastMatch, len);
            return sb2.toString();
        } else {
            throw new NullPointerException("replacement == null");
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

    public static String valueOf(char[] data, int offset, int count2) {
        return StringFactory.newStringFromChars(data, offset, count2);
    }

    public static String copyValueOf(char[] data, int offset, int count2) {
        return StringFactory.newStringFromChars(data, offset, count2);
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
