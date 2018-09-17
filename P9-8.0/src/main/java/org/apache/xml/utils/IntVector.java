package org.apache.xml.utils;

import org.apache.xpath.axes.WalkerFactory;

public class IntVector implements Cloneable {
    protected int m_blocksize;
    protected int m_firstFree;
    protected int[] m_map;
    protected int m_mapSize;

    public IntVector() {
        this.m_firstFree = 0;
        this.m_blocksize = 32;
        this.m_mapSize = this.m_blocksize;
        this.m_map = new int[this.m_blocksize];
    }

    public IntVector(int blocksize) {
        this.m_firstFree = 0;
        this.m_blocksize = blocksize;
        this.m_mapSize = blocksize;
        this.m_map = new int[blocksize];
    }

    public IntVector(int blocksize, int increaseSize) {
        this.m_firstFree = 0;
        this.m_blocksize = increaseSize;
        this.m_mapSize = blocksize;
        this.m_map = new int[blocksize];
    }

    public IntVector(IntVector v) {
        this.m_firstFree = 0;
        this.m_map = new int[v.m_mapSize];
        this.m_mapSize = v.m_mapSize;
        this.m_firstFree = v.m_firstFree;
        this.m_blocksize = v.m_blocksize;
        System.arraycopy(v.m_map, 0, this.m_map, 0, this.m_firstFree);
    }

    public final int size() {
        return this.m_firstFree;
    }

    public final void setSize(int sz) {
        this.m_firstFree = sz;
    }

    public final void addElement(int value) {
        if (this.m_firstFree + 1 >= this.m_mapSize) {
            this.m_mapSize += this.m_blocksize;
            int[] newMap = new int[this.m_mapSize];
            System.arraycopy(this.m_map, 0, newMap, 0, this.m_firstFree + 1);
            this.m_map = newMap;
        }
        this.m_map[this.m_firstFree] = value;
        this.m_firstFree++;
    }

    public final void addElements(int value, int numberOfElements) {
        if (this.m_firstFree + numberOfElements >= this.m_mapSize) {
            this.m_mapSize += this.m_blocksize + numberOfElements;
            int[] newMap = new int[this.m_mapSize];
            System.arraycopy(this.m_map, 0, newMap, 0, this.m_firstFree + 1);
            this.m_map = newMap;
        }
        for (int i = 0; i < numberOfElements; i++) {
            this.m_map[this.m_firstFree] = value;
            this.m_firstFree++;
        }
    }

    public final void addElements(int numberOfElements) {
        if (this.m_firstFree + numberOfElements >= this.m_mapSize) {
            this.m_mapSize += this.m_blocksize + numberOfElements;
            int[] newMap = new int[this.m_mapSize];
            System.arraycopy(this.m_map, 0, newMap, 0, this.m_firstFree + 1);
            this.m_map = newMap;
        }
        this.m_firstFree += numberOfElements;
    }

    public final void insertElementAt(int value, int at) {
        if (this.m_firstFree + 1 >= this.m_mapSize) {
            this.m_mapSize += this.m_blocksize;
            int[] newMap = new int[this.m_mapSize];
            System.arraycopy(this.m_map, 0, newMap, 0, this.m_firstFree + 1);
            this.m_map = newMap;
        }
        if (at <= this.m_firstFree - 1) {
            System.arraycopy(this.m_map, at, this.m_map, at + 1, this.m_firstFree - at);
        }
        this.m_map[at] = value;
        this.m_firstFree++;
    }

    public final void removeAllElements() {
        for (int i = 0; i < this.m_firstFree; i++) {
            this.m_map[i] = WalkerFactory.BIT_MATCH_PATTERN;
        }
        this.m_firstFree = 0;
    }

    public final boolean removeElement(int s) {
        for (int i = 0; i < this.m_firstFree; i++) {
            if (this.m_map[i] == s) {
                if (i + 1 < this.m_firstFree) {
                    System.arraycopy(this.m_map, i + 1, this.m_map, i - 1, this.m_firstFree - i);
                } else {
                    this.m_map[i] = WalkerFactory.BIT_MATCH_PATTERN;
                }
                this.m_firstFree--;
                return true;
            }
        }
        return false;
    }

    public final void removeElementAt(int i) {
        if (i > this.m_firstFree) {
            System.arraycopy(this.m_map, i + 1, this.m_map, i, this.m_firstFree);
        } else {
            this.m_map[i] = WalkerFactory.BIT_MATCH_PATTERN;
        }
        this.m_firstFree--;
    }

    public final void setElementAt(int value, int index) {
        this.m_map[index] = value;
    }

    public final int elementAt(int i) {
        return this.m_map[i];
    }

    public final boolean contains(int s) {
        for (int i = 0; i < this.m_firstFree; i++) {
            if (this.m_map[i] == s) {
                return true;
            }
        }
        return false;
    }

    public final int indexOf(int elem, int index) {
        for (int i = index; i < this.m_firstFree; i++) {
            if (this.m_map[i] == elem) {
                return i;
            }
        }
        return WalkerFactory.BIT_MATCH_PATTERN;
    }

    public final int indexOf(int elem) {
        for (int i = 0; i < this.m_firstFree; i++) {
            if (this.m_map[i] == elem) {
                return i;
            }
        }
        return WalkerFactory.BIT_MATCH_PATTERN;
    }

    public final int lastIndexOf(int elem) {
        for (int i = this.m_firstFree - 1; i >= 0; i--) {
            if (this.m_map[i] == elem) {
                return i;
            }
        }
        return WalkerFactory.BIT_MATCH_PATTERN;
    }

    public Object clone() throws CloneNotSupportedException {
        return new IntVector(this);
    }
}
