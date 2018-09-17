package org.apache.xml.utils.res;

public class StringArrayWrapper {
    private String[] m_string;

    public StringArrayWrapper(String[] arg) {
        this.m_string = arg;
    }

    public String getString(int index) {
        return this.m_string[index];
    }

    public int getLength() {
        return this.m_string.length;
    }
}
