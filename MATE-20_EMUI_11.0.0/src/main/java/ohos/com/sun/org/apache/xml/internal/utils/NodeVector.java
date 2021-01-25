package ohos.com.sun.org.apache.xml.internal.utils;

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

    public NodeVector(int i) {
        this.m_firstFree = 0;
        this.m_blocksize = i;
        this.m_mapSize = 0;
    }

    @Override // java.lang.Object
    public Object clone() throws CloneNotSupportedException {
        NodeVector nodeVector = (NodeVector) super.clone();
        int[] iArr = this.m_map;
        if (iArr != null && iArr == nodeVector.m_map) {
            nodeVector.m_map = new int[iArr.length];
            int[] iArr2 = this.m_map;
            System.arraycopy(iArr2, 0, nodeVector.m_map, 0, iArr2.length);
        }
        return nodeVector;
    }

    public int size() {
        return this.m_firstFree;
    }

    public void addElement(int i) {
        int i2 = this.m_firstFree;
        int i3 = i2 + 1;
        int i4 = this.m_mapSize;
        if (i3 >= i4) {
            int[] iArr = this.m_map;
            if (iArr == null) {
                int i5 = this.m_blocksize;
                this.m_map = new int[i5];
                this.m_mapSize = i5;
            } else {
                this.m_mapSize = i4 + this.m_blocksize;
                int[] iArr2 = new int[this.m_mapSize];
                System.arraycopy(iArr, 0, iArr2, 0, i2 + 1);
                this.m_map = iArr2;
            }
        }
        int[] iArr3 = this.m_map;
        int i6 = this.m_firstFree;
        iArr3[i6] = i;
        this.m_firstFree = i6 + 1;
    }

    public final void push(int i) {
        int i2 = this.m_firstFree;
        int i3 = i2 + 1;
        int i4 = this.m_mapSize;
        if (i3 >= i4) {
            int[] iArr = this.m_map;
            if (iArr == null) {
                int i5 = this.m_blocksize;
                this.m_map = new int[i5];
                this.m_mapSize = i5;
            } else {
                this.m_mapSize = i4 + this.m_blocksize;
                int[] iArr2 = new int[this.m_mapSize];
                System.arraycopy(iArr, 0, iArr2, 0, i3);
                this.m_map = iArr2;
            }
        }
        this.m_map[i2] = i;
        this.m_firstFree = i3;
    }

    public final int pop() {
        this.m_firstFree--;
        int[] iArr = this.m_map;
        int i = this.m_firstFree;
        int i2 = iArr[i];
        iArr[i] = -1;
        return i2;
    }

    public final int popAndTop() {
        this.m_firstFree--;
        int[] iArr = this.m_map;
        int i = this.m_firstFree;
        iArr[i] = -1;
        if (i == 0) {
            return -1;
        }
        return iArr[i - 1];
    }

    public final void popQuick() {
        this.m_firstFree--;
        this.m_map[this.m_firstFree] = -1;
    }

    public final int peepOrNull() {
        int i;
        int[] iArr = this.m_map;
        if (iArr == null || (i = this.m_firstFree) <= 0) {
            return -1;
        }
        return iArr[i - 1];
    }

    public final void pushPair(int i, int i2) {
        int[] iArr = this.m_map;
        if (iArr == null) {
            int i3 = this.m_blocksize;
            this.m_map = new int[i3];
            this.m_mapSize = i3;
        } else {
            int i4 = this.m_firstFree;
            int i5 = i4 + 2;
            int i6 = this.m_mapSize;
            if (i5 >= i6) {
                this.m_mapSize = i6 + this.m_blocksize;
                int[] iArr2 = new int[this.m_mapSize];
                System.arraycopy(iArr, 0, iArr2, 0, i4);
                this.m_map = iArr2;
            }
        }
        int[] iArr3 = this.m_map;
        int i7 = this.m_firstFree;
        iArr3[i7] = i;
        iArr3[i7 + 1] = i2;
        this.m_firstFree = i7 + 2;
    }

    public final void popPair() {
        this.m_firstFree -= 2;
        int[] iArr = this.m_map;
        int i = this.m_firstFree;
        iArr[i] = -1;
        iArr[i + 1] = -1;
    }

    public final void setTail(int i) {
        this.m_map[this.m_firstFree - 1] = i;
    }

    public final void setTailSub1(int i) {
        this.m_map[this.m_firstFree - 2] = i;
    }

    public final int peepTail() {
        return this.m_map[this.m_firstFree - 1];
    }

    public final int peepTailSub1() {
        return this.m_map[this.m_firstFree - 2];
    }

    public void insertInOrder(int i) {
        for (int i2 = 0; i2 < this.m_firstFree; i2++) {
            if (i < this.m_map[i2]) {
                insertElementAt(i, i2);
                return;
            }
        }
        addElement(i);
    }

    public void insertElementAt(int i, int i2) {
        int[] iArr = this.m_map;
        if (iArr == null) {
            int i3 = this.m_blocksize;
            this.m_map = new int[i3];
            this.m_mapSize = i3;
        } else {
            int i4 = this.m_firstFree;
            int i5 = i4 + 1;
            int i6 = this.m_mapSize;
            if (i5 >= i6) {
                this.m_mapSize = i6 + this.m_blocksize;
                int[] iArr2 = new int[this.m_mapSize];
                System.arraycopy(iArr, 0, iArr2, 0, i4 + 1);
                this.m_map = iArr2;
            }
        }
        int i7 = this.m_firstFree;
        if (i2 <= i7 - 1) {
            int[] iArr3 = this.m_map;
            System.arraycopy(iArr3, i2, iArr3, i2 + 1, i7 - i2);
        }
        this.m_map[i2] = i;
        this.m_firstFree++;
    }

    public void appendNodes(NodeVector nodeVector) {
        int size = nodeVector.size();
        int[] iArr = this.m_map;
        if (iArr == null) {
            this.m_mapSize = this.m_blocksize + size;
            this.m_map = new int[this.m_mapSize];
        } else {
            int i = this.m_firstFree;
            int i2 = i + size;
            int i3 = this.m_mapSize;
            if (i2 >= i3) {
                this.m_mapSize = i3 + this.m_blocksize + size;
                int[] iArr2 = new int[this.m_mapSize];
                System.arraycopy(iArr, 0, iArr2, 0, i + size);
                this.m_map = iArr2;
            }
        }
        System.arraycopy(nodeVector.m_map, 0, this.m_map, this.m_firstFree, size);
        this.m_firstFree += size;
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

    public boolean removeElement(int i) {
        if (this.m_map == null) {
            return false;
        }
        int i2 = 0;
        while (true) {
            int i3 = this.m_firstFree;
            if (i2 >= i3) {
                return false;
            }
            int[] iArr = this.m_map;
            if (iArr[i2] == i) {
                if (i2 > i3) {
                    System.arraycopy(iArr, i2 + 1, iArr, i2 - 1, i3 - i2);
                } else {
                    iArr[i2] = -1;
                }
                this.m_firstFree--;
                return true;
            }
            i2++;
        }
    }

    public void removeElementAt(int i) {
        int[] iArr = this.m_map;
        if (iArr != null) {
            int i2 = this.m_firstFree;
            if (i > i2) {
                System.arraycopy(iArr, i + 1, iArr, i - 1, i2 - i);
            } else {
                iArr[i] = -1;
            }
        }
    }

    public void setElementAt(int i, int i2) {
        if (this.m_map == null) {
            int i3 = this.m_blocksize;
            this.m_map = new int[i3];
            this.m_mapSize = i3;
        }
        if (i2 == -1) {
            addElement(i);
        }
        this.m_map[i2] = i;
    }

    public int elementAt(int i) {
        int[] iArr = this.m_map;
        if (iArr == null) {
            return -1;
        }
        return iArr[i];
    }

    public boolean contains(int i) {
        if (this.m_map == null) {
            return false;
        }
        for (int i2 = 0; i2 < this.m_firstFree; i2++) {
            if (this.m_map[i2] == i) {
                return true;
            }
        }
        return false;
    }

    public int indexOf(int i, int i2) {
        if (this.m_map == null) {
            return -1;
        }
        while (i2 < this.m_firstFree) {
            if (this.m_map[i2] == i) {
                return i2;
            }
            i2++;
        }
        return -1;
    }

    public int indexOf(int i) {
        if (this.m_map == null) {
            return -1;
        }
        for (int i2 = 0; i2 < this.m_firstFree; i2++) {
            if (this.m_map[i2] == i) {
                return i2;
            }
        }
        return -1;
    }

    public void sort(int[] iArr, int i, int i2) throws Exception {
        if (i < i2) {
            if (i != i2 - 1) {
                int i3 = (i + i2) >>> 1;
                int i4 = iArr[i3];
                iArr[i3] = iArr[i2];
                iArr[i2] = i4;
                int i5 = i;
                int i6 = i2;
                while (i5 < i6) {
                    while (iArr[i5] <= i4 && i5 < i6) {
                        i5++;
                    }
                    while (i4 <= iArr[i6] && i5 < i6) {
                        i6--;
                    }
                    if (i5 < i6) {
                        int i7 = iArr[i5];
                        iArr[i5] = iArr[i6];
                        iArr[i6] = i7;
                    }
                }
                iArr[i2] = iArr[i6];
                iArr[i6] = i4;
                sort(iArr, i, i5 - 1);
                sort(iArr, i6 + 1, i2);
            } else if (iArr[i] > iArr[i2]) {
                int i8 = iArr[i];
                iArr[i] = iArr[i2];
                iArr[i2] = i8;
            }
        }
    }

    public void sort() throws Exception {
        sort(this.m_map, 0, this.m_firstFree - 1);
    }
}
