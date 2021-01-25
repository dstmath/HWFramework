package ohos.com.sun.org.apache.xerces.internal.util;

import ohos.com.sun.org.apache.xerces.internal.xni.XMLString;

public class XMLStringBuffer extends XMLString {
    public static final int DEFAULT_SIZE = 32;

    public XMLStringBuffer() {
        this(32);
    }

    public XMLStringBuffer(int i) {
        this.ch = new char[i];
    }

    public XMLStringBuffer(char c) {
        this(1);
        append(c);
    }

    public XMLStringBuffer(String str) {
        this(str.length());
        append(str);
    }

    public XMLStringBuffer(char[] cArr, int i, int i2) {
        this(i2);
        append(cArr, i, i2);
    }

    public XMLStringBuffer(XMLString xMLString) {
        this(xMLString.length);
        append(xMLString);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLString
    public void clear() {
        this.offset = 0;
        this.length = 0;
    }

    public void append(char c) {
        if (this.length + 1 > this.ch.length) {
            int length = this.ch.length * 2;
            if (length < this.ch.length + 32) {
                length = this.ch.length + 32;
            }
            char[] cArr = new char[length];
            System.arraycopy(this.ch, 0, cArr, 0, this.length);
            this.ch = cArr;
        }
        this.ch[this.length] = c;
        this.length++;
    }

    public void append(String str) {
        int length = str.length();
        if (this.length + length > this.ch.length) {
            int length2 = this.ch.length * 2;
            if (length2 < this.ch.length + length + 32) {
                length2 = this.ch.length + length + 32;
            }
            char[] cArr = new char[length2];
            System.arraycopy(this.ch, 0, cArr, 0, this.length);
            this.ch = cArr;
        }
        str.getChars(0, length, this.ch, this.length);
        this.length += length;
    }

    public void append(char[] cArr, int i, int i2) {
        if (this.length + i2 > this.ch.length) {
            int length = this.ch.length * 2;
            if (length < this.ch.length + i2 + 32) {
                length = this.ch.length + i2 + 32;
            }
            char[] cArr2 = new char[length];
            System.arraycopy(this.ch, 0, cArr2, 0, this.length);
            this.ch = cArr2;
        }
        if (cArr != null && i2 > 0) {
            System.arraycopy(cArr, i, this.ch, this.length, i2);
            this.length += i2;
        }
    }

    public void append(XMLString xMLString) {
        append(xMLString.ch, xMLString.offset, xMLString.length);
    }
}
