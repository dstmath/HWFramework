package ohos.com.sun.org.apache.xml.internal.utils;

public class ObjectVector implements Cloneable {
    protected int m_blocksize;
    protected int m_firstFree;
    protected Object[] m_map;
    protected int m_mapSize;

    public ObjectVector() {
        this.m_firstFree = 0;
        this.m_blocksize = 32;
        int i = this.m_blocksize;
        this.m_mapSize = i;
        this.m_map = new Object[i];
    }

    public ObjectVector(int i) {
        this.m_firstFree = 0;
        this.m_blocksize = i;
        this.m_mapSize = i;
        this.m_map = new Object[i];
    }

    public ObjectVector(int i, int i2) {
        this.m_firstFree = 0;
        this.m_blocksize = i2;
        this.m_mapSize = i;
        this.m_map = new Object[i];
    }

    public ObjectVector(ObjectVector objectVector) {
        this.m_firstFree = 0;
        int i = objectVector.m_mapSize;
        this.m_map = new Object[i];
        this.m_mapSize = i;
        this.m_firstFree = objectVector.m_firstFree;
        this.m_blocksize = objectVector.m_blocksize;
        System.arraycopy(objectVector.m_map, 0, this.m_map, 0, this.m_firstFree);
    }

    public final int size() {
        return this.m_firstFree;
    }

    public final void setSize(int i) {
        this.m_firstFree = i;
    }

    public final void addElement(Object obj) {
        int i = this.m_firstFree;
        int i2 = i + 1;
        int i3 = this.m_mapSize;
        if (i2 >= i3) {
            this.m_mapSize = i3 + this.m_blocksize;
            Object[] objArr = new Object[this.m_mapSize];
            System.arraycopy(this.m_map, 0, objArr, 0, i + 1);
            this.m_map = objArr;
        }
        Object[] objArr2 = this.m_map;
        int i4 = this.m_firstFree;
        objArr2[i4] = obj;
        this.m_firstFree = i4 + 1;
    }

    public final void addElements(Object obj, int i) {
        int i2 = this.m_firstFree;
        int i3 = i2 + i;
        int i4 = this.m_mapSize;
        if (i3 >= i4) {
            this.m_mapSize = i4 + this.m_blocksize + i;
            Object[] objArr = new Object[this.m_mapSize];
            System.arraycopy(this.m_map, 0, objArr, 0, i2 + 1);
            this.m_map = objArr;
        }
        for (int i5 = 0; i5 < i; i5++) {
            Object[] objArr2 = this.m_map;
            int i6 = this.m_firstFree;
            objArr2[i6] = obj;
            this.m_firstFree = i6 + 1;
        }
    }

    public final void addElements(int i) {
        int i2 = this.m_firstFree;
        int i3 = i2 + i;
        int i4 = this.m_mapSize;
        if (i3 >= i4) {
            this.m_mapSize = i4 + this.m_blocksize + i;
            Object[] objArr = new Object[this.m_mapSize];
            System.arraycopy(this.m_map, 0, objArr, 0, i2 + 1);
            this.m_map = objArr;
        }
        this.m_firstFree += i;
    }

    public final void insertElementAt(Object obj, int i) {
        int i2 = this.m_firstFree;
        int i3 = i2 + 1;
        int i4 = this.m_mapSize;
        if (i3 >= i4) {
            this.m_mapSize = i4 + this.m_blocksize;
            Object[] objArr = new Object[this.m_mapSize];
            System.arraycopy(this.m_map, 0, objArr, 0, i2 + 1);
            this.m_map = objArr;
        }
        int i5 = this.m_firstFree;
        if (i <= i5 - 1) {
            Object[] objArr2 = this.m_map;
            System.arraycopy(objArr2, i, objArr2, i + 1, i5 - i);
        }
        this.m_map[i] = obj;
        this.m_firstFree++;
    }

    public final void removeAllElements() {
        for (int i = 0; i < this.m_firstFree; i++) {
            this.m_map[i] = null;
        }
        this.m_firstFree = 0;
    }

    public final boolean removeElement(Object obj) {
        int i = 0;
        while (true) {
            int i2 = this.m_firstFree;
            if (i >= i2) {
                return false;
            }
            Object[] objArr = this.m_map;
            if (objArr[i] == obj) {
                int i3 = i + 1;
                if (i3 < i2) {
                    System.arraycopy(objArr, i3, objArr, i - 1, i2 - i);
                } else {
                    objArr[i] = null;
                }
                this.m_firstFree--;
                return true;
            }
            i++;
        }
    }

    public final void removeElementAt(int i) {
        int i2 = this.m_firstFree;
        if (i > i2) {
            Object[] objArr = this.m_map;
            System.arraycopy(objArr, i + 1, objArr, i, i2);
        } else {
            this.m_map[i] = null;
        }
        this.m_firstFree--;
    }

    public final void setElementAt(Object obj, int i) {
        this.m_map[i] = obj;
    }

    public final Object elementAt(int i) {
        return this.m_map[i];
    }

    public final boolean contains(Object obj) {
        for (int i = 0; i < this.m_firstFree; i++) {
            if (this.m_map[i] == obj) {
                return true;
            }
        }
        return false;
    }

    public final int indexOf(Object obj, int i) {
        while (i < this.m_firstFree) {
            if (this.m_map[i] == obj) {
                return i;
            }
            i++;
        }
        return Integer.MIN_VALUE;
    }

    public final int indexOf(Object obj) {
        for (int i = 0; i < this.m_firstFree; i++) {
            if (this.m_map[i] == obj) {
                return i;
            }
        }
        return Integer.MIN_VALUE;
    }

    public final int lastIndexOf(Object obj) {
        for (int i = this.m_firstFree - 1; i >= 0; i--) {
            if (this.m_map[i] == obj) {
                return i;
            }
        }
        return Integer.MIN_VALUE;
    }

    public final void setToSize(int i) {
        Object[] objArr = new Object[i];
        System.arraycopy(this.m_map, 0, objArr, 0, this.m_firstFree);
        this.m_mapSize = i;
        this.m_map = objArr;
    }

    @Override // java.lang.Object
    public Object clone() throws CloneNotSupportedException {
        return new ObjectVector(this);
    }
}
