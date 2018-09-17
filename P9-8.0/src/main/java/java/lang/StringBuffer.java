package java.lang;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectInputStream.GetField;
import java.io.ObjectOutputStream;
import java.io.ObjectOutputStream.PutField;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.util.Arrays;

public final class StringBuffer extends AbstractStringBuilder implements Serializable, CharSequence {
    private static final ObjectStreamField[] serialPersistentFields = new ObjectStreamField[]{new ObjectStreamField("value", char[].class), new ObjectStreamField("count", Integer.TYPE), new ObjectStreamField("shared", Boolean.TYPE)};
    static final long serialVersionUID = 3388685877147921107L;
    private transient char[] toStringCache;

    public StringBuffer() {
        super(16);
    }

    public StringBuffer(int capacity) {
        super(capacity);
    }

    public StringBuffer(String str) {
        super(str.length() + 16);
        append(str);
    }

    public StringBuffer(CharSequence seq) {
        this(seq.length() + 16);
        append(seq);
    }

    public synchronized int length() {
        return this.count;
    }

    public synchronized int capacity() {
        return this.value.length;
    }

    public synchronized void ensureCapacity(int minimumCapacity) {
        super.ensureCapacity(minimumCapacity);
    }

    public synchronized void trimToSize() {
        super.trimToSize();
    }

    public synchronized void setLength(int newLength) {
        this.toStringCache = null;
        super.setLength(newLength);
    }

    public synchronized char charAt(int index) {
        if (index >= 0) {
            if (index < this.count) {
            }
        }
        throw new StringIndexOutOfBoundsException(index);
        return this.value[index];
    }

    public synchronized int codePointAt(int index) {
        return super.codePointAt(index);
    }

    public synchronized int codePointBefore(int index) {
        return super.codePointBefore(index);
    }

    public synchronized int codePointCount(int beginIndex, int endIndex) {
        return super.codePointCount(beginIndex, endIndex);
    }

    public synchronized int offsetByCodePoints(int index, int codePointOffset) {
        return super.offsetByCodePoints(index, codePointOffset);
    }

    public synchronized void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
        super.getChars(srcBegin, srcEnd, dst, dstBegin);
    }

    public synchronized void setCharAt(int index, char ch) {
        if (index >= 0) {
            if (index < this.count) {
                this.toStringCache = null;
                this.value[index] = ch;
            }
        }
        throw new StringIndexOutOfBoundsException(index);
    }

    public synchronized StringBuffer append(Object obj) {
        this.toStringCache = null;
        super.append(String.valueOf(obj));
        return this;
    }

    public synchronized StringBuffer append(String str) {
        this.toStringCache = null;
        super.append(str);
        return this;
    }

    public synchronized StringBuffer append(StringBuffer sb) {
        this.toStringCache = null;
        super.append(sb);
        return this;
    }

    synchronized StringBuffer append(AbstractStringBuilder asb) {
        this.toStringCache = null;
        super.append(asb);
        return this;
    }

    public synchronized StringBuffer append(CharSequence s) {
        this.toStringCache = null;
        super.append(s);
        return this;
    }

    public synchronized StringBuffer append(CharSequence s, int start, int end) {
        this.toStringCache = null;
        super.append(s, start, end);
        return this;
    }

    public synchronized StringBuffer append(char[] str) {
        this.toStringCache = null;
        super.append(str);
        return this;
    }

    public synchronized StringBuffer append(char[] str, int offset, int len) {
        this.toStringCache = null;
        super.append(str, offset, len);
        return this;
    }

    public synchronized StringBuffer append(boolean b) {
        this.toStringCache = null;
        super.append(b);
        return this;
    }

    public synchronized StringBuffer append(char c) {
        this.toStringCache = null;
        super.append(c);
        return this;
    }

    public synchronized StringBuffer append(int i) {
        this.toStringCache = null;
        super.append(i);
        return this;
    }

    public synchronized StringBuffer appendCodePoint(int codePoint) {
        this.toStringCache = null;
        super.appendCodePoint(codePoint);
        return this;
    }

    public synchronized StringBuffer append(long lng) {
        this.toStringCache = null;
        super.append(lng);
        return this;
    }

    public synchronized StringBuffer append(float f) {
        this.toStringCache = null;
        super.append(f);
        return this;
    }

    public synchronized StringBuffer append(double d) {
        this.toStringCache = null;
        super.append(d);
        return this;
    }

    public synchronized StringBuffer delete(int start, int end) {
        this.toStringCache = null;
        super.delete(start, end);
        return this;
    }

    public synchronized StringBuffer deleteCharAt(int index) {
        this.toStringCache = null;
        super.deleteCharAt(index);
        return this;
    }

    public synchronized StringBuffer replace(int start, int end, String str) {
        this.toStringCache = null;
        super.replace(start, end, str);
        return this;
    }

    public synchronized String substring(int start) {
        return substring(start, this.count);
    }

    public synchronized CharSequence subSequence(int start, int end) {
        return super.substring(start, end);
    }

    public synchronized String substring(int start, int end) {
        return super.substring(start, end);
    }

    public synchronized StringBuffer insert(int index, char[] str, int offset, int len) {
        this.toStringCache = null;
        super.insert(index, str, offset, len);
        return this;
    }

    public synchronized StringBuffer insert(int offset, Object obj) {
        this.toStringCache = null;
        super.insert(offset, String.valueOf(obj));
        return this;
    }

    public synchronized StringBuffer insert(int offset, String str) {
        this.toStringCache = null;
        super.insert(offset, str);
        return this;
    }

    public synchronized StringBuffer insert(int offset, char[] str) {
        this.toStringCache = null;
        super.insert(offset, str);
        return this;
    }

    public StringBuffer insert(int dstOffset, CharSequence s) {
        super.insert(dstOffset, s);
        return this;
    }

    public synchronized StringBuffer insert(int dstOffset, CharSequence s, int start, int end) {
        this.toStringCache = null;
        super.insert(dstOffset, s, start, end);
        return this;
    }

    public StringBuffer insert(int offset, boolean b) {
        super.insert(offset, b);
        return this;
    }

    public synchronized StringBuffer insert(int offset, char c) {
        this.toStringCache = null;
        super.insert(offset, c);
        return this;
    }

    public StringBuffer insert(int offset, int i) {
        super.insert(offset, i);
        return this;
    }

    public StringBuffer insert(int offset, long l) {
        super.insert(offset, l);
        return this;
    }

    public StringBuffer insert(int offset, float f) {
        super.insert(offset, f);
        return this;
    }

    public StringBuffer insert(int offset, double d) {
        super.insert(offset, d);
        return this;
    }

    public int indexOf(String str) {
        return super.indexOf(str);
    }

    public synchronized int indexOf(String str, int fromIndex) {
        return super.indexOf(str, fromIndex);
    }

    public int lastIndexOf(String str) {
        return lastIndexOf(str, this.count);
    }

    public synchronized int lastIndexOf(String str, int fromIndex) {
        return super.lastIndexOf(str, fromIndex);
    }

    public synchronized StringBuffer reverse() {
        this.toStringCache = null;
        super.reverse();
        return this;
    }

    public synchronized String toString() {
        if (this.toStringCache == null) {
            this.toStringCache = Arrays.copyOfRange(this.value, 0, this.count);
        }
        return new String(this.toStringCache, 0, this.count);
    }

    private synchronized void writeObject(ObjectOutputStream s) throws IOException {
        PutField fields = s.putFields();
        fields.put("value", this.value);
        fields.put("count", this.count);
        fields.put("shared", false);
        s.writeFields();
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        GetField fields = s.readFields();
        this.value = (char[]) fields.get("value", null);
        this.count = fields.get("count", 0);
    }
}
