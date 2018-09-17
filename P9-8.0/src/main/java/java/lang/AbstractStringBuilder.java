package java.lang;

import java.util.Arrays;
import sun.misc.FloatingDecimal;

abstract class AbstractStringBuilder implements Appendable, CharSequence {
    private static final int MAX_ARRAY_SIZE = 2147483639;
    int count;
    char[] value;

    public abstract String toString();

    AbstractStringBuilder() {
    }

    AbstractStringBuilder(int capacity) {
        this.value = new char[capacity];
    }

    public int length() {
        return this.count;
    }

    public int capacity() {
        return this.value.length;
    }

    public void ensureCapacity(int minimumCapacity) {
        if (minimumCapacity > 0) {
            ensureCapacityInternal(minimumCapacity);
        }
    }

    private void ensureCapacityInternal(int minimumCapacity) {
        if (minimumCapacity - this.value.length > 0) {
            this.value = Arrays.copyOf(this.value, newCapacity(minimumCapacity));
        }
    }

    private int newCapacity(int minCapacity) {
        int newCapacity = (this.value.length << 1) + 2;
        if (newCapacity - minCapacity < 0) {
            newCapacity = minCapacity;
        }
        if (newCapacity <= 0 || MAX_ARRAY_SIZE - newCapacity < 0) {
            return hugeCapacity(minCapacity);
        }
        return newCapacity;
    }

    private int hugeCapacity(int minCapacity) {
        if (Integer.MAX_VALUE - minCapacity >= 0) {
            return minCapacity > MAX_ARRAY_SIZE ? minCapacity : MAX_ARRAY_SIZE;
        } else {
            throw new OutOfMemoryError();
        }
    }

    public void trimToSize() {
        if (this.count < this.value.length) {
            this.value = Arrays.copyOf(this.value, this.count);
        }
    }

    public void setLength(int newLength) {
        if (newLength < 0) {
            throw new StringIndexOutOfBoundsException(newLength);
        }
        ensureCapacityInternal(newLength);
        if (this.count < newLength) {
            Arrays.fill(this.value, this.count, newLength, 0);
        }
        this.count = newLength;
    }

    public char charAt(int index) {
        if (index >= 0 && index < this.count) {
            return this.value[index];
        }
        throw new StringIndexOutOfBoundsException(index);
    }

    public int codePointAt(int index) {
        if (index >= 0 && index < this.count) {
            return Character.codePointAtImpl(this.value, index, this.count);
        }
        throw new StringIndexOutOfBoundsException(index);
    }

    public int codePointBefore(int index) {
        int i = index - 1;
        if (i >= 0 && i < this.count) {
            return Character.codePointBeforeImpl(this.value, index, 0);
        }
        throw new StringIndexOutOfBoundsException(index);
    }

    public int codePointCount(int beginIndex, int endIndex) {
        if (beginIndex >= 0 && endIndex <= this.count && beginIndex <= endIndex) {
            return Character.codePointCountImpl(this.value, beginIndex, endIndex - beginIndex);
        }
        throw new IndexOutOfBoundsException();
    }

    public int offsetByCodePoints(int index, int codePointOffset) {
        if (index >= 0 && index <= this.count) {
            return Character.offsetByCodePointsImpl(this.value, 0, this.count, index, codePointOffset);
        }
        throw new IndexOutOfBoundsException();
    }

    public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
        if (srcBegin < 0) {
            throw new StringIndexOutOfBoundsException(srcBegin);
        } else if (srcEnd < 0 || srcEnd > this.count) {
            throw new StringIndexOutOfBoundsException(srcEnd);
        } else if (srcBegin > srcEnd) {
            throw new StringIndexOutOfBoundsException("srcBegin > srcEnd");
        } else {
            System.arraycopy(this.value, srcBegin, dst, dstBegin, srcEnd - srcBegin);
        }
    }

    public void setCharAt(int index, char ch) {
        if (index < 0 || index >= this.count) {
            throw new StringIndexOutOfBoundsException(index);
        }
        this.value[index] = ch;
    }

    public AbstractStringBuilder append(Object obj) {
        return append(String.valueOf(obj));
    }

    public AbstractStringBuilder append(String str) {
        if (str == null) {
            return appendNull();
        }
        int len = str.length();
        ensureCapacityInternal(this.count + len);
        str.getChars(0, len, this.value, this.count);
        this.count += len;
        return this;
    }

    public AbstractStringBuilder append(StringBuffer sb) {
        if (sb == null) {
            return appendNull();
        }
        int len = sb.length();
        ensureCapacityInternal(this.count + len);
        sb.getChars(0, len, this.value, this.count);
        this.count += len;
        return this;
    }

    AbstractStringBuilder append(AbstractStringBuilder asb) {
        if (asb == null) {
            return appendNull();
        }
        int len = asb.length();
        ensureCapacityInternal(this.count + len);
        asb.getChars(0, len, this.value, this.count);
        this.count += len;
        return this;
    }

    public AbstractStringBuilder append(CharSequence s) {
        if (s == null) {
            return appendNull();
        }
        if (s instanceof String) {
            return append((String) s);
        }
        if (s instanceof AbstractStringBuilder) {
            return append((AbstractStringBuilder) s);
        }
        return append(s, 0, s.length());
    }

    private AbstractStringBuilder appendNull() {
        int i = this.count;
        ensureCapacityInternal(i + 4);
        char[] value = this.value;
        int i2 = i + 1;
        value[i] = 'n';
        i = i2 + 1;
        value[i2] = 'u';
        i2 = i + 1;
        value[i] = 'l';
        i = i2 + 1;
        value[i2] = 'l';
        this.count = i;
        return this;
    }

    public AbstractStringBuilder append(CharSequence s, int start, int end) {
        if (s == null) {
            s = "null";
        }
        if (start < 0 || start > end || end > s.length()) {
            throw new IndexOutOfBoundsException("start " + start + ", end " + end + ", s.length() " + s.length());
        }
        int len = end - start;
        ensureCapacityInternal(this.count + len);
        int i = start;
        int j = this.count;
        while (i < end) {
            this.value[j] = s.charAt(i);
            i++;
            j++;
        }
        this.count += len;
        return this;
    }

    public AbstractStringBuilder append(char[] str) {
        int len = str.length;
        ensureCapacityInternal(this.count + len);
        System.arraycopy(str, 0, this.value, this.count, len);
        this.count += len;
        return this;
    }

    public AbstractStringBuilder append(char[] str, int offset, int len) {
        if (len > 0) {
            ensureCapacityInternal(this.count + len);
        }
        System.arraycopy(str, offset, this.value, this.count, len);
        this.count += len;
        return this;
    }

    public AbstractStringBuilder append(boolean b) {
        char[] cArr;
        int i;
        if (b) {
            ensureCapacityInternal(this.count + 4);
            cArr = this.value;
            i = this.count;
            this.count = i + 1;
            cArr[i] = 't';
            cArr = this.value;
            i = this.count;
            this.count = i + 1;
            cArr[i] = 'r';
            cArr = this.value;
            i = this.count;
            this.count = i + 1;
            cArr[i] = 'u';
            cArr = this.value;
            i = this.count;
            this.count = i + 1;
            cArr[i] = 'e';
        } else {
            ensureCapacityInternal(this.count + 5);
            cArr = this.value;
            i = this.count;
            this.count = i + 1;
            cArr[i] = 'f';
            cArr = this.value;
            i = this.count;
            this.count = i + 1;
            cArr[i] = 'a';
            cArr = this.value;
            i = this.count;
            this.count = i + 1;
            cArr[i] = 'l';
            cArr = this.value;
            i = this.count;
            this.count = i + 1;
            cArr[i] = 's';
            cArr = this.value;
            i = this.count;
            this.count = i + 1;
            cArr[i] = 'e';
        }
        return this;
    }

    public AbstractStringBuilder append(char c) {
        ensureCapacityInternal(this.count + 1);
        char[] cArr = this.value;
        int i = this.count;
        this.count = i + 1;
        cArr[i] = c;
        return this;
    }

    public AbstractStringBuilder append(int i) {
        if (i == Integer.MIN_VALUE) {
            append("-2147483648");
            return this;
        }
        int appendedLength;
        if (i < 0) {
            appendedLength = Integer.stringSize(-i) + 1;
        } else {
            appendedLength = Integer.stringSize(i);
        }
        int spaceNeeded = this.count + appendedLength;
        ensureCapacityInternal(spaceNeeded);
        Integer.getChars(i, spaceNeeded, this.value);
        this.count = spaceNeeded;
        return this;
    }

    public AbstractStringBuilder append(long l) {
        if (l == Long.MIN_VALUE) {
            append("-9223372036854775808");
            return this;
        }
        int appendedLength;
        if (l < 0) {
            appendedLength = Long.stringSize(-l) + 1;
        } else {
            appendedLength = Long.stringSize(l);
        }
        int spaceNeeded = this.count + appendedLength;
        ensureCapacityInternal(spaceNeeded);
        Long.getChars(l, spaceNeeded, this.value);
        this.count = spaceNeeded;
        return this;
    }

    public AbstractStringBuilder append(float f) {
        FloatingDecimal.appendTo(f, (Appendable) this);
        return this;
    }

    public AbstractStringBuilder append(double d) {
        FloatingDecimal.appendTo(d, (Appendable) this);
        return this;
    }

    public AbstractStringBuilder delete(int start, int end) {
        if (start < 0) {
            throw new StringIndexOutOfBoundsException(start);
        }
        if (end > this.count) {
            end = this.count;
        }
        if (start > end) {
            throw new StringIndexOutOfBoundsException();
        }
        int len = end - start;
        if (len > 0) {
            System.arraycopy(this.value, start + len, this.value, start, this.count - end);
            this.count -= len;
        }
        return this;
    }

    public AbstractStringBuilder appendCodePoint(int codePoint) {
        int count = this.count;
        if (Character.isBmpCodePoint(codePoint)) {
            ensureCapacityInternal(count + 1);
            this.value[count] = (char) codePoint;
            this.count = count + 1;
        } else if (Character.isValidCodePoint(codePoint)) {
            ensureCapacityInternal(count + 2);
            Character.toSurrogates(codePoint, this.value, count);
            this.count = count + 2;
        } else {
            throw new IllegalArgumentException();
        }
        return this;
    }

    public AbstractStringBuilder deleteCharAt(int index) {
        if (index < 0 || index >= this.count) {
            throw new StringIndexOutOfBoundsException(index);
        }
        System.arraycopy(this.value, index + 1, this.value, index, (this.count - index) - 1);
        this.count--;
        return this;
    }

    public AbstractStringBuilder replace(int start, int end, String str) {
        if (start < 0) {
            throw new StringIndexOutOfBoundsException(start);
        } else if (start > this.count) {
            throw new StringIndexOutOfBoundsException("start > length()");
        } else if (start > end) {
            throw new StringIndexOutOfBoundsException("start > end");
        } else {
            if (end > this.count) {
                end = this.count;
            }
            int len = str.length();
            int newCount = (this.count + len) - (end - start);
            ensureCapacityInternal(newCount);
            System.arraycopy(this.value, end, this.value, start + len, this.count - end);
            str.getChars(this.value, start);
            this.count = newCount;
            return this;
        }
    }

    public String substring(int start) {
        return substring(start, this.count);
    }

    public CharSequence subSequence(int start, int end) {
        return substring(start, end);
    }

    public String substring(int start, int end) {
        if (start < 0) {
            throw new StringIndexOutOfBoundsException(start);
        } else if (end > this.count) {
            throw new StringIndexOutOfBoundsException(end);
        } else if (start <= end) {
            return new String(this.value, start, end - start);
        } else {
            throw new StringIndexOutOfBoundsException(end - start);
        }
    }

    public AbstractStringBuilder insert(int index, char[] str, int offset, int len) {
        if (index < 0 || index > length()) {
            throw new StringIndexOutOfBoundsException(index);
        } else if (offset < 0 || len < 0 || offset > str.length - len) {
            throw new StringIndexOutOfBoundsException("offset " + offset + ", len " + len + ", str.length " + str.length);
        } else {
            ensureCapacityInternal(this.count + len);
            System.arraycopy(this.value, index, this.value, index + len, this.count - index);
            System.arraycopy(str, offset, this.value, index, len);
            this.count += len;
            return this;
        }
    }

    public AbstractStringBuilder insert(int offset, Object obj) {
        return insert(offset, String.valueOf(obj));
    }

    public AbstractStringBuilder insert(int offset, String str) {
        if (offset < 0 || offset > length()) {
            throw new StringIndexOutOfBoundsException(offset);
        }
        if (str == null) {
            str = "null";
        }
        int len = str.length();
        ensureCapacityInternal(this.count + len);
        System.arraycopy(this.value, offset, this.value, offset + len, this.count - offset);
        str.getChars(this.value, offset);
        this.count += len;
        return this;
    }

    public AbstractStringBuilder insert(int offset, char[] str) {
        if (offset < 0 || offset > length()) {
            throw new StringIndexOutOfBoundsException(offset);
        }
        int len = str.length;
        ensureCapacityInternal(this.count + len);
        System.arraycopy(this.value, offset, this.value, offset + len, this.count - offset);
        System.arraycopy(str, 0, this.value, offset, len);
        this.count += len;
        return this;
    }

    public AbstractStringBuilder insert(int dstOffset, CharSequence s) {
        if (s == null) {
            s = "null";
        }
        if (s instanceof String) {
            return insert(dstOffset, (String) s);
        }
        return insert(dstOffset, s, 0, s.length());
    }

    public AbstractStringBuilder insert(int dstOffset, CharSequence s, int start, int end) {
        if (s == null) {
            s = "null";
        }
        if (dstOffset < 0 || dstOffset > length()) {
            throw new IndexOutOfBoundsException("dstOffset " + dstOffset);
        } else if (start < 0 || end < 0 || start > end || end > s.length()) {
            throw new IndexOutOfBoundsException("start " + start + ", end " + end + ", s.length() " + s.length());
        } else {
            int len = end - start;
            ensureCapacityInternal(this.count + len);
            System.arraycopy(this.value, dstOffset, this.value, dstOffset + len, this.count - dstOffset);
            int i = start;
            int dstOffset2 = dstOffset;
            while (i < end) {
                dstOffset = dstOffset2 + 1;
                this.value[dstOffset2] = s.charAt(i);
                i++;
                dstOffset2 = dstOffset;
            }
            this.count += len;
            return this;
        }
    }

    public AbstractStringBuilder insert(int offset, boolean b) {
        return insert(offset, String.valueOf(b));
    }

    public AbstractStringBuilder insert(int offset, char c) {
        ensureCapacityInternal(this.count + 1);
        System.arraycopy(this.value, offset, this.value, offset + 1, this.count - offset);
        this.value[offset] = c;
        this.count++;
        return this;
    }

    public AbstractStringBuilder insert(int offset, int i) {
        return insert(offset, String.valueOf(i));
    }

    public AbstractStringBuilder insert(int offset, long l) {
        return insert(offset, String.valueOf(l));
    }

    public AbstractStringBuilder insert(int offset, float f) {
        return insert(offset, String.valueOf(f));
    }

    public AbstractStringBuilder insert(int offset, double d) {
        return insert(offset, String.valueOf(d));
    }

    public int indexOf(String str) {
        return indexOf(str, 0);
    }

    public int indexOf(String str, int fromIndex) {
        return String.indexOf(this.value, 0, this.count, str.toCharArray(), 0, str.length(), fromIndex);
    }

    public int lastIndexOf(String str) {
        return lastIndexOf(str, this.count);
    }

    public int lastIndexOf(String str, int fromIndex) {
        return String.lastIndexOf(this.value, 0, this.count, str.toCharArray(), 0, str.length(), fromIndex);
    }

    public AbstractStringBuilder reverse() {
        boolean hasSurrogates = false;
        int n = this.count - 1;
        for (int j = (n - 1) >> 1; j >= 0; j--) {
            int k = n - j;
            char cj = this.value[j];
            char ck = this.value[k];
            this.value[j] = ck;
            this.value[k] = cj;
            if (Character.isSurrogate(cj) || Character.isSurrogate(ck)) {
                hasSurrogates = true;
            }
        }
        if (hasSurrogates) {
            reverseAllValidSurrogatePairs();
        }
        return this;
    }

    private void reverseAllValidSurrogatePairs() {
        int i = 0;
        while (i < this.count - 1) {
            char c2 = this.value[i];
            if (Character.isLowSurrogate(c2)) {
                char c1 = this.value[i + 1];
                if (Character.isHighSurrogate(c1)) {
                    int i2 = i + 1;
                    this.value[i] = c1;
                    this.value[i2] = c2;
                    i = i2;
                }
            }
            i++;
        }
    }

    final char[] getValue() {
        return this.value;
    }
}
