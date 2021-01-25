package ohos.com.sun.org.apache.xpath.internal.compiler;

public class OpMapVector {
    protected int m_blocksize;
    protected int m_lengthPos = 0;
    protected int[] m_map;
    protected int m_mapSize;

    public OpMapVector(int i, int i2, int i3) {
        this.m_blocksize = i2;
        this.m_mapSize = i;
        this.m_lengthPos = i3;
        this.m_map = new int[i];
    }

    public final int elementAt(int i) {
        return this.m_map[i];
    }

    public final void setElementAt(int i, int i2) {
        int i3 = this.m_mapSize;
        if (i2 >= i3) {
            this.m_mapSize = this.m_blocksize + i3;
            int[] iArr = new int[this.m_mapSize];
            System.arraycopy(this.m_map, 0, iArr, 0, i3);
            this.m_map = iArr;
        }
        this.m_map[i2] = i;
    }

    public final void setToSize(int i) {
        int[] iArr = new int[i];
        int[] iArr2 = this.m_map;
        System.arraycopy(iArr2, 0, iArr, 0, iArr2[this.m_lengthPos]);
        this.m_mapSize = i;
        this.m_map = iArr;
    }
}
