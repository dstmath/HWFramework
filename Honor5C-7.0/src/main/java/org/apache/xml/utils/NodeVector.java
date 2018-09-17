package org.apache.xml.utils;

import java.io.Serializable;

public class NodeVector implements Serializable, Cloneable {
    static final long serialVersionUID = -713473092200731870L;
    private int m_blocksize;
    protected int m_firstFree;
    private int[] m_map;
    private int m_mapSize;

    public NodeVector() {
        this.m_firstFree = 0;
        this.m_blocksize = 32;
        this.m_mapSize = 0;
    }

    public NodeVector(int blocksize) {
        this.m_firstFree = 0;
        this.m_blocksize = blocksize;
        this.m_mapSize = 0;
    }

    public Object clone() throws CloneNotSupportedException {
        NodeVector clone = (NodeVector) super.clone();
        if (this.m_map != null && this.m_map == clone.m_map) {
            clone.m_map = new int[this.m_map.length];
            System.arraycopy(this.m_map, 0, clone.m_map, 0, this.m_map.length);
        }
        return clone;
    }

    public int size() {
        return this.m_firstFree;
    }

    public void addElement(int value) {
        if (this.m_firstFree + 1 >= this.m_mapSize) {
            if (this.m_map == null) {
                this.m_map = new int[this.m_blocksize];
                this.m_mapSize = this.m_blocksize;
            } else {
                this.m_mapSize += this.m_blocksize;
                int[] newMap = new int[this.m_mapSize];
                System.arraycopy(this.m_map, 0, newMap, 0, this.m_firstFree + 1);
                this.m_map = newMap;
            }
        }
        this.m_map[this.m_firstFree] = value;
        this.m_firstFree++;
    }

    public final void push(int value) {
        int ff = this.m_firstFree;
        if (ff + 1 >= this.m_mapSize) {
            if (this.m_map == null) {
                this.m_map = new int[this.m_blocksize];
                this.m_mapSize = this.m_blocksize;
            } else {
                this.m_mapSize += this.m_blocksize;
                int[] newMap = new int[this.m_mapSize];
                System.arraycopy(this.m_map, 0, newMap, 0, ff + 1);
                this.m_map = newMap;
            }
        }
        this.m_map[ff] = value;
        this.m_firstFree = ff + 1;
    }

    public final int pop() {
        this.m_firstFree--;
        int n = this.m_map[this.m_firstFree];
        this.m_map[this.m_firstFree] = -1;
        return n;
    }

    public final int popAndTop() {
        this.m_firstFree--;
        this.m_map[this.m_firstFree] = -1;
        if (this.m_firstFree == 0) {
            return -1;
        }
        return this.m_map[this.m_firstFree - 1];
    }

    public final void popQuick() {
        this.m_firstFree--;
        this.m_map[this.m_firstFree] = -1;
    }

    public final int peepOrNull() {
        return (this.m_map == null || this.m_firstFree <= 0) ? -1 : this.m_map[this.m_firstFree - 1];
    }

    public final void pushPair(int v1, int v2) {
        if (this.m_map == null) {
            this.m_map = new int[this.m_blocksize];
            this.m_mapSize = this.m_blocksize;
        } else if (this.m_firstFree + 2 >= this.m_mapSize) {
            this.m_mapSize += this.m_blocksize;
            int[] newMap = new int[this.m_mapSize];
            System.arraycopy(this.m_map, 0, newMap, 0, this.m_firstFree);
            this.m_map = newMap;
        }
        this.m_map[this.m_firstFree] = v1;
        this.m_map[this.m_firstFree + 1] = v2;
        this.m_firstFree += 2;
    }

    public final void popPair() {
        this.m_firstFree -= 2;
        this.m_map[this.m_firstFree] = -1;
        this.m_map[this.m_firstFree + 1] = -1;
    }

    public final void setTail(int n) {
        this.m_map[this.m_firstFree - 1] = n;
    }

    public final void setTailSub1(int n) {
        this.m_map[this.m_firstFree - 2] = n;
    }

    public final int peepTail() {
        return this.m_map[this.m_firstFree - 1];
    }

    public final int peepTailSub1() {
        return this.m_map[this.m_firstFree - 2];
    }

    public void insertInOrder(int value) {
        for (int i = 0; i < this.m_firstFree; i++) {
            if (value < this.m_map[i]) {
                insertElementAt(value, i);
                return;
            }
        }
        addElement(value);
    }

    public void insertElementAt(int value, int at) {
        if (this.m_map == null) {
            this.m_map = new int[this.m_blocksize];
            this.m_mapSize = this.m_blocksize;
        } else if (this.m_firstFree + 1 >= this.m_mapSize) {
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

    public void appendNodes(NodeVector nodes) {
        int nNodes = nodes.size();
        if (this.m_map == null) {
            this.m_mapSize = this.m_blocksize + nNodes;
            this.m_map = new int[this.m_mapSize];
        } else if (this.m_firstFree + nNodes >= this.m_mapSize) {
            this.m_mapSize += this.m_blocksize + nNodes;
            int[] newMap = new int[this.m_mapSize];
            System.arraycopy(this.m_map, 0, newMap, 0, this.m_firstFree + nNodes);
            this.m_map = newMap;
        }
        System.arraycopy(nodes.m_map, 0, this.m_map, this.m_firstFree, nNodes);
        this.m_firstFree += nNodes;
    }

    public void removeAllElements() {
        if (this.m_map != null) {
            for (int i = 0; i < this.m_firstFree; i++) {
                this.m_map[i] = -1;
            }
            this.m_firstFree = 0;
        }
    }

    public void RemoveAllNoClear() {
        if (this.m_map != null) {
            this.m_firstFree = 0;
        }
    }

    public boolean removeElement(int s) {
        if (this.m_map == null) {
            return false;
        }
        for (int i = 0; i < this.m_firstFree; i++) {
            if (this.m_map[i] == s) {
                if (i > this.m_firstFree) {
                    System.arraycopy(this.m_map, i + 1, this.m_map, i - 1, this.m_firstFree - i);
                } else {
                    this.m_map[i] = -1;
                }
                this.m_firstFree--;
                return true;
            }
        }
        return false;
    }

    public void removeElementAt(int i) {
        if (this.m_map != null) {
            if (i > this.m_firstFree) {
                System.arraycopy(this.m_map, i + 1, this.m_map, i - 1, this.m_firstFree - i);
            } else {
                this.m_map[i] = -1;
            }
        }
    }

    public void setElementAt(int node, int index) {
        if (this.m_map == null) {
            this.m_map = new int[this.m_blocksize];
            this.m_mapSize = this.m_blocksize;
        }
        if (index == -1) {
            addElement(node);
        }
        this.m_map[index] = node;
    }

    public int elementAt(int i) {
        if (this.m_map == null) {
            return -1;
        }
        return this.m_map[i];
    }

    public boolean contains(int s) {
        if (this.m_map == null) {
            return false;
        }
        for (int i = 0; i < this.m_firstFree; i++) {
            if (this.m_map[i] == s) {
                return true;
            }
        }
        return false;
    }

    public int indexOf(int elem, int index) {
        if (this.m_map == null) {
            return -1;
        }
        for (int i = index; i < this.m_firstFree; i++) {
            if (this.m_map[i] == elem) {
                return i;
            }
        }
        return -1;
    }

    public int indexOf(int elem) {
        if (this.m_map == null) {
            return -1;
        }
        for (int i = 0; i < this.m_firstFree; i++) {
            if (this.m_map[i] == elem) {
                return i;
            }
        }
        return -1;
    }

    public void sort(int[] a, int lo0, int hi0) throws Exception {
        int lo = lo0;
        int hi = hi0;
        if (lo0 < hi0) {
            int T;
            if (lo0 == hi0 - 1) {
                if (a[lo0] > a[hi0]) {
                    T = a[lo0];
                    a[lo0] = a[hi0];
                    a[hi0] = T;
                }
                return;
            }
            int pivot = a[(lo0 + hi0) / 2];
            a[(lo0 + hi0) / 2] = a[hi0];
            a[hi0] = pivot;
            while (lo < hi) {
                while (a[lo] <= pivot && lo < hi) {
                    lo++;
                }
                while (pivot <= a[hi] && lo < hi) {
                    hi--;
                }
                if (lo < hi) {
                    T = a[lo];
                    a[lo] = a[hi];
                    a[hi] = T;
                }
            }
            a[hi0] = a[hi];
            a[hi] = pivot;
            sort(a, lo0, lo - 1);
            sort(a, hi + 1, hi0);
        }
    }

    public void sort() throws Exception {
        sort(this.m_map, 0, this.m_firstFree - 1);
    }
}
