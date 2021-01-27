package ohos.com.sun.org.apache.xml.internal.utils.res;

public class CharArrayWrapper {
    private char[] m_char;

    public CharArrayWrapper(char[] cArr) {
        this.m_char = cArr;
    }

    public char getChar(int i) {
        return this.m_char[i];
    }

    public int getLength() {
        return this.m_char.length;
    }
}
