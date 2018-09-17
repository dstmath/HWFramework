package org.apache.xpath.compiler;

public class OpMapVector {
    protected int m_blocksize;
    protected int m_lengthPos = 0;
    protected int[] m_map;
    protected int m_mapSize;

    public OpMapVector(int blocksize, int increaseSize, int lengthPos) {
        this.m_blocksize = increaseSize;
        this.m_mapSize = blocksize;
        this.m_lengthPos = lengthPos;
        this.m_map = new int[blocksize];
    }

    public final int elementAt(int i) {
        return this.m_map[i];
    }

    public final void setElementAt(int value, int index) {
        if (index >= this.m_mapSize) {
            int oldSize = this.m_mapSize;
            this.m_mapSize += this.m_blocksize;
            int[] newMap = new int[this.m_mapSize];
            System.arraycopy(this.m_map, 0, newMap, 0, oldSize);
            this.m_map = newMap;
        }
        this.m_map[index] = value;
    }

    public final void setToSize(int size) {
        int[] newMap = new int[size];
        System.arraycopy(this.m_map, 0, newMap, 0, this.m_map[this.m_lengthPos]);
        this.m_mapSize = size;
        this.m_map = newMap;
    }
}
