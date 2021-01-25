package org.apache.http.util;

import org.apache.http.protocol.HTTP;

@Deprecated
public final class CharArrayBuffer {
    private char[] buffer;
    private int len;

    public CharArrayBuffer(int capacity) {
        if (capacity >= 0) {
            this.buffer = new char[capacity];
            return;
        }
        throw new IllegalArgumentException("Buffer capacity may not be negative");
    }

    private void expand(int newlen) {
        char[] newbuffer = new char[Math.max(this.buffer.length << 1, newlen)];
        System.arraycopy(this.buffer, 0, newbuffer, 0, this.len);
        this.buffer = newbuffer;
    }

    public void append(char[] b, int off, int len2) {
        if (b != null) {
            if (off < 0 || off > b.length || len2 < 0 || off + len2 < 0 || off + len2 > b.length) {
                throw new IndexOutOfBoundsException();
            } else if (len2 != 0) {
                int newlen = this.len + len2;
                if (newlen > this.buffer.length) {
                    expand(newlen);
                }
                System.arraycopy(b, off, this.buffer, this.len, len2);
                this.len = newlen;
            }
        }
    }

    public void append(String str) {
        if (str == null) {
            str = "null";
        }
        int strlen = str.length();
        int newlen = this.len + strlen;
        if (newlen > this.buffer.length) {
            expand(newlen);
        }
        str.getChars(0, strlen, this.buffer, this.len);
        this.len = newlen;
    }

    public void append(CharArrayBuffer b, int off, int len2) {
        if (b != null) {
            append(b.buffer, off, len2);
        }
    }

    public void append(CharArrayBuffer b) {
        if (b != null) {
            append(b.buffer, 0, b.len);
        }
    }

    public void append(char ch) {
        int newlen = this.len + 1;
        if (newlen > this.buffer.length) {
            expand(newlen);
        }
        this.buffer[this.len] = ch;
        this.len = newlen;
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:27:0x002c */
    /* JADX DEBUG: Multi-variable search result rejected for r8v0, resolved type: byte[] */
    /* JADX DEBUG: Multi-variable search result rejected for r4v0, resolved type: byte */
    /* JADX DEBUG: Multi-variable search result rejected for r4v2, resolved type: int */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r4v1 */
    public void append(byte[] b, int off, int len2) {
        if (b != 0) {
            if (off < 0 || off > b.length || len2 < 0 || off + len2 < 0 || off + len2 > b.length) {
                throw new IndexOutOfBoundsException();
            } else if (len2 != 0) {
                int oldlen = this.len;
                int newlen = oldlen + len2;
                if (newlen > this.buffer.length) {
                    expand(newlen);
                }
                int i1 = off;
                for (int i2 = oldlen; i2 < newlen; i2++) {
                    byte ch = b[i1];
                    if (ch < 0) {
                        ch += 256;
                    }
                    this.buffer[i2] = ch == true ? (char) 1 : 0;
                    i1++;
                }
                this.len = newlen;
            }
        }
    }

    public void append(ByteArrayBuffer b, int off, int len2) {
        if (b != null) {
            append(b.buffer(), off, len2);
        }
    }

    public void append(Object obj) {
        append(String.valueOf(obj));
    }

    public void clear() {
        this.len = 0;
    }

    public char[] toCharArray() {
        int i = this.len;
        char[] b = new char[i];
        if (i > 0) {
            System.arraycopy(this.buffer, 0, b, 0, i);
        }
        return b;
    }

    public char charAt(int i) {
        return this.buffer[i];
    }

    public char[] buffer() {
        return this.buffer;
    }

    public int capacity() {
        return this.buffer.length;
    }

    public int length() {
        return this.len;
    }

    public void ensureCapacity(int required) {
        int length = this.buffer.length;
        int i = this.len;
        if (required > length - i) {
            expand(i + required);
        }
    }

    public void setLength(int len2) {
        if (len2 < 0 || len2 > this.buffer.length) {
            throw new IndexOutOfBoundsException();
        }
        this.len = len2;
    }

    public boolean isEmpty() {
        return this.len == 0;
    }

    public boolean isFull() {
        return this.len == this.buffer.length;
    }

    public int indexOf(int ch, int beginIndex, int endIndex) {
        if (beginIndex < 0) {
            beginIndex = 0;
        }
        if (endIndex > this.len) {
            endIndex = this.len;
        }
        if (beginIndex > endIndex) {
            return -1;
        }
        for (int i = beginIndex; i < endIndex; i++) {
            if (this.buffer[i] == ch) {
                return i;
            }
        }
        return -1;
    }

    public int indexOf(int ch) {
        return indexOf(ch, 0, this.len);
    }

    public String substring(int beginIndex, int endIndex) {
        if (beginIndex < 0) {
            throw new IndexOutOfBoundsException();
        } else if (endIndex > this.len) {
            throw new IndexOutOfBoundsException();
        } else if (beginIndex <= endIndex) {
            return new String(this.buffer, beginIndex, endIndex - beginIndex);
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    public String substringTrimmed(int beginIndex, int endIndex) {
        if (beginIndex < 0) {
            throw new IndexOutOfBoundsException();
        } else if (endIndex > this.len) {
            throw new IndexOutOfBoundsException();
        } else if (beginIndex <= endIndex) {
            while (beginIndex < endIndex && HTTP.isWhitespace(this.buffer[beginIndex])) {
                beginIndex++;
            }
            while (endIndex > beginIndex && HTTP.isWhitespace(this.buffer[endIndex - 1])) {
                endIndex--;
            }
            return new String(this.buffer, beginIndex, endIndex - beginIndex);
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    public String toString() {
        return new String(this.buffer, 0, this.len);
    }
}
