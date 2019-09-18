package org.apache.xml.utils.res;

public class CharArrayWrapper {
    private char[] m_char;

    public CharArrayWrapper(char[] arg) {
        this.m_char = arg;
    }

    public char getChar(int index) {
        return this.m_char[index];
    }

    public int getLength() {
        return this.m_char.length;
    }
}
