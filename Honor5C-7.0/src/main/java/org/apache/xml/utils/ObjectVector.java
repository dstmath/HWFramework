package org.apache.xml.utils;

import org.apache.xpath.axes.WalkerFactory;

public class ObjectVector implements Cloneable {
    protected int m_blocksize;
    protected int m_firstFree;
    protected Object[] m_map;
    protected int m_mapSize;

    public ObjectVector() {
        this.m_firstFree = 0;
        this.m_blocksize = 32;
        this.m_mapSize = this.m_blocksize;
        this.m_map = new Object[this.m_blocksize];
    }

    public ObjectVector(int blocksize) {
        this.m_firstFree = 0;
        this.m_blocksize = blocksize;
        this.m_mapSize = blocksize;
        this.m_map = new Object[blocksize];
    }

    public ObjectVector(int blocksize, int increaseSize) {
        this.m_firstFree = 0;
        this.m_blocksize = increaseSize;
        this.m_mapSize = blocksize;
        this.m_map = new Object[blocksize];
    }

    public ObjectVector(ObjectVector v) {
        this.m_firstFree = 0;
        this.m_map = new Object[v.m_mapSize];
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

    public final void addElement(Object value) {
        if (this.m_firstFree + 1 >= this.m_mapSize) {
            this.m_mapSize += this.m_blocksize;
            Object[] newMap = new Object[this.m_mapSize];
            System.arraycopy(this.m_map, 0, newMap, 0, this.m_firstFree + 1);
            this.m_map = newMap;
        }
        this.m_map[this.m_firstFree] = value;
        this.m_firstFree++;
    }

    public final void addElements(Object value, int numberOfElements) {
        if (this.m_firstFree + numberOfElements >= this.m_mapSize) {
            this.m_mapSize += this.m_blocksize + numberOfElements;
            Object[] newMap = new Object[this.m_mapSize];
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
            Object[] newMap = new Object[this.m_mapSize];
            System.arraycopy(this.m_map, 0, newMap, 0, this.m_firstFree + 1);
            this.m_map = newMap;
        }
        this.m_firstFree += numberOfElements;
    }

    public final void insertElementAt(Object value, int at) {
        if (this.m_firstFree + 1 >= this.m_mapSize) {
            this.m_mapSize += this.m_blocksize;
            Object[] newMap = new Object[this.m_mapSize];
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
            this.m_map[i] = null;
        }
        this.m_firstFree = 0;
    }

    public final boolean removeElement(Object s) {
        for (int i = 0; i < this.m_firstFree; i++) {
            if (this.m_map[i] == s) {
                if (i + 1 < this.m_firstFree) {
                    System.arraycopy(this.m_map, i + 1, this.m_map, i - 1, this.m_firstFree - i);
                } else {
                    this.m_map[i] = null;
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
            this.m_map[i] = null;
        }
        this.m_firstFree--;
    }

    public final void setElementAt(Object value, int index) {
        this.m_map[index] = value;
    }

    public final Object elementAt(int i) {
        return this.m_map[i];
    }

    public final boolean contains(Object s) {
        for (int i = 0; i < this.m_firstFree; i++) {
            if (this.m_map[i] == s) {
                return true;
            }
        }
        return false;
    }

    public final int indexOf(Object elem, int index) {
        for (int i = index; i < this.m_firstFree; i++) {
            if (this.m_map[i] == elem) {
                return i;
            }
        }
        return WalkerFactory.BIT_MATCH_PATTERN;
    }

    public final int indexOf(Object elem) {
        for (int i = 0; i < this.m_firstFree; i++) {
            if (this.m_map[i] == elem) {
                return i;
            }
        }
        return WalkerFactory.BIT_MATCH_PATTERN;
    }

    public final int lastIndexOf(Object elem) {
        for (int i = this.m_firstFree - 1; i >= 0; i--) {
            if (this.m_map[i] == elem) {
                return i;
            }
        }
        return WalkerFactory.BIT_MATCH_PATTERN;
    }

    public final void setToSize(int size) {
        Object[] newMap = new Object[size];
        System.arraycopy(this.m_map, 0, newMap, 0, this.m_firstFree);
        this.m_mapSize = size;
        this.m_map = newMap;
    }

    public Object clone() throws CloneNotSupportedException {
        return new ObjectVector(this);
    }
}
