package java.lang;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public final class StringBuilder extends AbstractStringBuilder implements Serializable, Appendable, CharSequence {
    static final long serialVersionUID = 4383685877147921099L;

    public /* bridge */ /* synthetic */ int capacity() {
        return super.capacity();
    }

    public /* bridge */ /* synthetic */ char charAt(int index) {
        return super.charAt(index);
    }

    public /* bridge */ /* synthetic */ int codePointAt(int index) {
        return super.codePointAt(index);
    }

    public /* bridge */ /* synthetic */ int codePointBefore(int index) {
        return super.codePointBefore(index);
    }

    public /* bridge */ /* synthetic */ int codePointCount(int beginIndex, int endIndex) {
        return super.codePointCount(beginIndex, endIndex);
    }

    public /* bridge */ /* synthetic */ void ensureCapacity(int minimumCapacity) {
        super.ensureCapacity(minimumCapacity);
    }

    public /* bridge */ /* synthetic */ void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
        super.getChars(srcBegin, srcEnd, dst, dstBegin);
    }

    public /* bridge */ /* synthetic */ int length() {
        return super.length();
    }

    public /* bridge */ /* synthetic */ int offsetByCodePoints(int index, int codePointOffset) {
        return super.offsetByCodePoints(index, codePointOffset);
    }

    public /* bridge */ /* synthetic */ void setCharAt(int index, char ch) {
        super.setCharAt(index, ch);
    }

    public /* bridge */ /* synthetic */ void setLength(int newLength) {
        super.setLength(newLength);
    }

    public /* bridge */ /* synthetic */ CharSequence subSequence(int start, int end) {
        return super.subSequence(start, end);
    }

    public /* bridge */ /* synthetic */ String substring(int start) {
        return super.substring(start);
    }

    public /* bridge */ /* synthetic */ String substring(int start, int end) {
        return super.substring(start, end);
    }

    public /* bridge */ /* synthetic */ void trimToSize() {
        super.trimToSize();
    }

    public StringBuilder() {
        super(16);
    }

    public StringBuilder(int capacity) {
        super(capacity);
    }

    public StringBuilder(String str) {
        super(str.length() + 16);
        append(str);
    }

    public StringBuilder(CharSequence seq) {
        this(seq.length() + 16);
        append(seq);
    }

    public StringBuilder append(Object obj) {
        return append(String.valueOf(obj));
    }

    public StringBuilder append(String str) {
        super.append(str);
        return this;
    }

    private StringBuilder append(StringBuilder sb) {
        if (sb == null) {
            return append("null");
        }
        int len = sb.length();
        int newcount = this.count + len;
        if (newcount > this.value.length) {
            expandCapacity(newcount);
        }
        sb.getChars(0, len, this.value, this.count);
        this.count = newcount;
        return this;
    }

    public StringBuilder append(StringBuffer sb) {
        super.append(sb);
        return this;
    }

    public StringBuilder append(CharSequence s) {
        if (s == null) {
            s = "null";
        }
        if (s instanceof String) {
            return append((String) s);
        }
        if (s instanceof StringBuffer) {
            return append((StringBuffer) s);
        }
        if (s instanceof StringBuilder) {
            return append((StringBuilder) s);
        }
        return append(s, 0, s.length());
    }

    public StringBuilder append(CharSequence s, int start, int end) {
        super.append(s, start, end);
        return this;
    }

    public StringBuilder append(char[] str) {
        super.append(str);
        return this;
    }

    public StringBuilder append(char[] str, int offset, int len) {
        super.append(str, offset, len);
        return this;
    }

    public StringBuilder append(boolean b) {
        super.append(b);
        return this;
    }

    public StringBuilder append(char c) {
        super.append(c);
        return this;
    }

    public StringBuilder append(int i) {
        super.append(i);
        return this;
    }

    public StringBuilder append(long lng) {
        super.append(lng);
        return this;
    }

    public StringBuilder append(float f) {
        super.append(f);
        return this;
    }

    public StringBuilder append(double d) {
        super.append(d);
        return this;
    }

    public StringBuilder appendCodePoint(int codePoint) {
        super.appendCodePoint(codePoint);
        return this;
    }

    public StringBuilder delete(int start, int end) {
        super.delete(start, end);
        return this;
    }

    public StringBuilder deleteCharAt(int index) {
        super.deleteCharAt(index);
        return this;
    }

    public StringBuilder replace(int start, int end, String str) {
        super.replace(start, end, str);
        return this;
    }

    public StringBuilder insert(int index, char[] str, int offset, int len) {
        super.insert(index, str, offset, len);
        return this;
    }

    public StringBuilder insert(int offset, Object obj) {
        return insert(offset, String.valueOf(obj));
    }

    public StringBuilder insert(int offset, String str) {
        super.insert(offset, str);
        return this;
    }

    public StringBuilder insert(int offset, char[] str) {
        super.insert(offset, str);
        return this;
    }

    public StringBuilder insert(int dstOffset, CharSequence s) {
        if (s == null) {
            s = "null";
        }
        if (s instanceof String) {
            return insert(dstOffset, (String) s);
        }
        return insert(dstOffset, s, 0, s.length());
    }

    public StringBuilder insert(int dstOffset, CharSequence s, int start, int end) {
        super.insert(dstOffset, s, start, end);
        return this;
    }

    public StringBuilder insert(int offset, boolean b) {
        super.insert(offset, b);
        return this;
    }

    public StringBuilder insert(int offset, char c) {
        super.insert(offset, c);
        return this;
    }

    public StringBuilder insert(int offset, int i) {
        return insert(offset, String.valueOf(i));
    }

    public StringBuilder insert(int offset, long l) {
        return insert(offset, String.valueOf(l));
    }

    public StringBuilder insert(int offset, float f) {
        return insert(offset, String.valueOf(f));
    }

    public StringBuilder insert(int offset, double d) {
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

    public StringBuilder reverse() {
        super.reverse();
        return this;
    }

    public String toString() {
        if (this.count == 0) {
            return "";
        }
        return StringFactory.newStringFromChars(0, this.count, this.value);
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        s.writeInt(this.count);
        s.writeObject(this.value);
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        this.count = s.readInt();
        this.value = (char[]) s.readObject();
    }
}
