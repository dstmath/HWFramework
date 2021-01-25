package ohos.com.sun.org.apache.xml.internal.utils.res;

public class LongArrayWrapper {
    private long[] m_long;

    public LongArrayWrapper(long[] jArr) {
        this.m_long = jArr;
    }

    public long getLong(int i) {
        return this.m_long[i];
    }

    public int getLength() {
        return this.m_long.length;
    }
}
