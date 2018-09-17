package java.lang;

import java.io.ObjectStreamField;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.Formatter;
import java.util.Locale;
import java.util.jar.Pack200.Unpacker;
import java.util.regex.Pattern;
import libcore.util.CharsetUtils;

public final class String implements Serializable, Comparable<String>, CharSequence {
    public static final Comparator<String> CASE_INSENSITIVE_ORDER = null;
    private static final ObjectStreamField[] serialPersistentFields = null;
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
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.lang.String.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.lang.String.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.lang.String.<clinit>():void");
    }

    private native int fastIndexOf(int i, int i2);

    private native String fastSubstring(int i, int i2);

    public native char charAt(int i);

    public native int compareTo(String str);

    public native String concat(String str);

    native void getCharsNoCheck(int i, int i2, char[] cArr, int i3);

    public native String intern();

    native void setCharAt(int i, char c);

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
        return this.count;
    }

    public boolean isEmpty() {
        return this.count == 0;
    }

    public int codePointAt(int index) {
        if (index >= 0 && index < this.count) {
            return Character.codePointAt((CharSequence) this, index);
        }
        throw new StringIndexOutOfBoundsException(index);
    }

    public int codePointBefore(int index) {
        int i = index - 1;
        if (i >= 0 && i < this.count) {
            return Character.codePointBefore((CharSequence) this, index);
        }
        throw new StringIndexOutOfBoundsException(index);
    }

    public int codePointCount(int beginIndex, int endIndex) {
        if (beginIndex >= 0 && endIndex <= this.count && beginIndex <= endIndex) {
            return Character.codePointCount((CharSequence) this, beginIndex, endIndex);
        }
        throw new IndexOutOfBoundsException();
    }

    public int offsetByCodePoints(int index, int codePointOffset) {
        if (index >= 0 && index <= this.count) {
            return Character.offsetByCodePoints(this, index, codePointOffset);
        }
        throw new IndexOutOfBoundsException();
    }

    public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
        if (dst == null) {
            throw new NullPointerException("dst == null");
        } else if (srcBegin < 0) {
            throw new StringIndexOutOfBoundsException(this, srcBegin);
        } else if (srcEnd > this.count) {
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
        } else if (srcEnd > this.count) {
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
        return getBytes(Charset.forNameUEE(charsetName));
    }

    public byte[] getBytes(Charset charset) {
        if (charset == null) {
            throw new NullPointerException("charset == null");
        }
        String name = charset.name();
        if ("UTF-8".equals(name)) {
            return CharsetUtils.toUtf8Bytes(this, 0, this.count);
        }
        if ("ISO-8859-1".equals(name)) {
            return CharsetUtils.toIsoLatin1Bytes(this, 0, this.count);
        }
        if ("US-ASCII".equals(name)) {
            return CharsetUtils.toAsciiBytes(this, 0, this.count);
        }
        if ("UTF-16BE".equals(name)) {
            return CharsetUtils.toBigEndianUtf16Bytes(this, 0, this.count);
        }
        return StringCoding.encode(charset, this);
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
            int n = this.count;
            if (n == anotherString.count) {
                int i = 0;
                int n2 = n;
                while (true) {
                    n = n2 - 1;
                    if (n2 == 0) {
                        return true;
                    }
                    if (charAt(i) != anotherString.charAt(i)) {
                        return false;
                    }
                    i++;
                    n2 = n;
                }
            }
        }
        return false;
    }

    public boolean contentEquals(StringBuffer sb) {
        boolean contentEquals;
        synchronized (sb) {
            contentEquals = contentEquals((CharSequence) sb);
        }
        return contentEquals;
    }

    public boolean contentEquals(CharSequence cs) {
        if (this.count != cs.length()) {
            return false;
        }
        int i;
        int n;
        int n2;
        if (cs instanceof AbstractStringBuilder) {
            char[] v2 = ((AbstractStringBuilder) cs).getValue();
            i = 0;
            n = this.count;
            while (true) {
                n2 = n - 1;
                if (n == 0) {
                    return true;
                }
                if (charAt(i) != v2[i]) {
                    return false;
                }
                i++;
                n = n2;
            }
        } else if (cs.equals(this)) {
            return true;
        } else {
            i = 0;
            n = this.count;
            while (true) {
                n2 = n - 1;
                if (n == 0) {
                    return true;
                }
                if (charAt(i) != cs.charAt(i)) {
                    return false;
                }
                i++;
                n = n2;
            }
        }
    }

    public boolean equalsIgnoreCase(String anotherString) {
        if (this == anotherString) {
            return true;
        }
        if (anotherString == null || anotherString.count != this.count) {
            return false;
        }
        return regionMatches(true, 0, anotherString, 0, this.count);
    }

    public int compareToIgnoreCase(String str) {
        return CASE_INSENSITIVE_ORDER.compare(this, str);
    }

    public boolean regionMatches(int toffset, String other, int ooffset, int len) {
        int to = toffset;
        int po = ooffset;
        if (ooffset < 0 || toffset < 0 || ((long) toffset) > ((long) this.count) - ((long) len) || ((long) ooffset) > ((long) other.count) - ((long) len)) {
            return false;
        }
        int po2 = po;
        int to2 = to;
        int len2 = len;
        while (true) {
            len = len2 - 1;
            if (len2 <= 0) {
                return true;
            }
            to = to2 + 1;
            po = po2 + 1;
            if (charAt(to2) != other.charAt(po2)) {
                return false;
            }
            po2 = po;
            to2 = to;
            len2 = len;
        }
    }

    public boolean regionMatches(boolean ignoreCase, int toffset, String other, int ooffset, int len) {
        int to = toffset;
        int po = ooffset;
        if (ooffset >= 0 && toffset >= 0) {
            if (((long) toffset) <= ((long) this.count) - ((long) len)) {
                if (((long) ooffset) <= ((long) other.count) - ((long) len)) {
                    int po2 = po;
                    int to2 = to;
                    int len2 = len;
                    while (true) {
                        len = len2 - 1;
                        if (len2 > 0) {
                            to = to2 + 1;
                            char c1 = charAt(to2);
                            po = po2 + 1;
                            char c2 = other.charAt(po2);
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
                                    po2 = po;
                                    to2 = to;
                                    len2 = len;
                                } else {
                                    po2 = po;
                                    to2 = to;
                                    len2 = len;
                                }
                            } else {
                                po2 = po;
                                to2 = to;
                                len2 = len;
                            }
                        } else {
                            return true;
                        }
                    }
                    return false;
                }
            }
        }
        return false;
    }

    public boolean startsWith(String prefix, int toffset) {
        int to = toffset;
        int pc = prefix.count;
        if (toffset < 0 || toffset > this.count - pc) {
            return false;
        }
        int po = 0;
        int to2 = to;
        while (true) {
            pc--;
            if (pc < 0) {
                return true;
            }
            to = to2 + 1;
            int po2 = po + 1;
            if (charAt(to2) != prefix.charAt(po)) {
                return false;
            }
            po = po2;
            to2 = to;
        }
    }

    public boolean startsWith(String prefix) {
        return startsWith(prefix, 0);
    }

    public boolean endsWith(String suffix) {
        return startsWith(suffix, this.count - suffix.count);
    }

    public int hashCode() {
        int h = this.hash;
        if (h == 0 && this.count > 0) {
            for (int i = 0; i < this.count; i++) {
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
        int max = this.count;
        if (fromIndex < 0) {
            fromIndex = 0;
        } else if (fromIndex >= max) {
            return -1;
        }
        if (ch >= Record.OVERFLOW_OF_INT16) {
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
            int max = this.count - 1;
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
        return lastIndexOf(ch, this.count - 1);
    }

    public int lastIndexOf(int ch, int fromIndex) {
        if (ch >= Record.OVERFLOW_OF_INT16) {
            return lastIndexOfSupplementary(ch, fromIndex);
        }
        for (int i = Math.min(fromIndex, this.count - 1); i >= 0; i--) {
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
            int i = Math.min(fromIndex, this.count - 2);
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
        int i = -1;
        if (fromIndex >= source.count) {
            if (target.count == 0) {
                i = source.count;
            }
            return i;
        }
        if (fromIndex < 0) {
            fromIndex = 0;
        }
        if (target.count == 0) {
            return fromIndex;
        }
        char first = target.charAt(0);
        int max = source.count - target.count;
        int i2 = fromIndex;
        while (i2 <= max) {
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
                int end = (target.count + j) - 1;
                int k = 1;
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
        return lastIndexOf(str, this.count);
    }

    public int lastIndexOf(String str, int fromIndex) {
        return lastIndexOf(this, str, fromIndex);
    }

    static int lastIndexOf(String source, String target, int fromIndex) {
        int rightIndex = source.count - target.count;
        if (fromIndex < 0) {
            return -1;
        }
        if (fromIndex > rightIndex) {
            fromIndex = rightIndex;
        }
        if (target.count == 0) {
            return fromIndex;
        }
        int strLastIndex = target.count - 1;
        char strLastChar = target.charAt(strLastIndex);
        int min = target.count - 1;
        int i = min + fromIndex;
        while (true) {
            if (i >= min && source.charAt(i) != strLastChar) {
                i--;
            } else if (i < min) {
                return -1;
            } else {
                int j = i - 1;
                int start = j - (target.count - 1);
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
        int subLen = this.count - beginIndex;
        if (subLen >= 0) {
            return beginIndex == 0 ? this : fastSubstring(beginIndex, subLen);
        } else {
            throw new StringIndexOutOfBoundsException(this, beginIndex);
        }
    }

    public String substring(int beginIndex, int endIndex) {
        if (beginIndex < 0) {
            throw new StringIndexOutOfBoundsException(this, beginIndex);
        }
        int subLen = endIndex - beginIndex;
        if (endIndex > this.count || subLen < 0) {
            throw new StringIndexOutOfBoundsException(this, beginIndex, subLen);
        } else if (beginIndex == 0 && endIndex == this.count) {
            return this;
        } else {
            return fastSubstring(beginIndex, subLen);
        }
    }

    public CharSequence subSequence(int beginIndex, int endIndex) {
        return substring(beginIndex, endIndex);
    }

    public String replace(char oldChar, char newChar) {
        String replaced = this;
        if (oldChar != newChar) {
            for (int i = 0; i < this.count; i++) {
                if (charAt(i) == oldChar) {
                    if (replaced == this) {
                        replaced = StringFactory.newStringFromString(this);
                    }
                    replaced.setCharAt(i, newChar);
                }
            }
        }
        return replaced;
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
            StringBuilder sb;
            if (targetStr.isEmpty()) {
                sb = new StringBuilder((replacementStr.length() * (this.count + 2)) + this.count);
                sb.append(replacementStr);
                for (int i = 0; i < this.count; i++) {
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
                    sb = new StringBuilder(this.count);
                }
                sb.append((CharSequence) this, lastMatch, currentMatch);
                sb.append(replacementStr);
                lastMatch = currentMatch + targetStr.count;
            }
            if (sb == null) {
                return this;
            }
            sb.append((CharSequence) this, lastMatch, this.count);
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

    public String toLowerCase(Locale locale) {
        return CaseMapper.toLowerCase(locale, this);
    }

    public String toLowerCase() {
        return toLowerCase(Locale.getDefault());
    }

    public String toUpperCase(Locale locale) {
        return CaseMapper.toUpperCase(locale, this, this.count);
    }

    public String toUpperCase() {
        return toUpperCase(Locale.getDefault());
    }

    public String trim() {
        int len = this.count;
        int st = 0;
        while (st < len && charAt(st) <= ' ') {
            st++;
        }
        while (st < len && charAt(len - 1) <= ' ') {
            len--;
        }
        return (st > 0 || len < this.count) ? substring(st, len) : this;
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
        return b ? Unpacker.TRUE : Unpacker.FALSE;
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
