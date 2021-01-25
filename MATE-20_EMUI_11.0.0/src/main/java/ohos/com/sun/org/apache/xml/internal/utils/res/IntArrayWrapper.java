package ohos.com.sun.org.apache.xml.internal.utils.res;

public class IntArrayWrapper {
    private int[] m_int;

    public IntArrayWrapper(int[] iArr) {
        this.m_int = iArr;
    }

    public int getInt(int i) {
        return this.m_int[i];
    }

    public int getLength() {
        return this.m_int.length;
    }
}
