package org.apache.xml.utils.res;

public class LongArrayWrapper {
    private long[] m_long;

    public LongArrayWrapper(long[] arg) {
        this.m_long = arg;
    }

    public long getLong(int index) {
        return this.m_long[index];
    }

    public int getLength() {
        return this.m_long.length;
    }
}
