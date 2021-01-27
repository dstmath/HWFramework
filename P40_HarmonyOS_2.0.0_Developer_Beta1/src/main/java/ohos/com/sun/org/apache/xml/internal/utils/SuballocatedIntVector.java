package ohos.com.sun.org.apache.xml.internal.utils;

public class SuballocatedIntVector {
    protected static final int NUMBLOCKS_DEFAULT = 32;
    protected int m_MASK;
    protected int m_SHIFT;
    protected int m_blocksize;
    protected int[] m_buildCache;
    protected int m_buildCacheStartIndex;
    protected int m_firstFree;
    protected int[][] m_map;
    protected int[] m_map0;
    protected int m_numblocks;

    public SuballocatedIntVector() {
        this(2048);
    }

    public SuballocatedIntVector(int i, int i2) {
        this.m_numblocks = 32;
        this.m_firstFree = 0;
        this.m_SHIFT = 0;
        while (true) {
            i >>>= 1;
            if (i != 0) {
                this.m_SHIFT++;
            } else {
                this.m_blocksize = 1 << this.m_SHIFT;
                int i3 = this.m_blocksize;
                this.m_MASK = i3 - 1;
                this.m_numblocks = i2;
                this.m_map0 = new int[i3];
                this.m_map = new int[i2][];
                int[][] iArr = this.m_map;
                int[] iArr2 = this.m_map0;
                iArr[0] = iArr2;
                this.m_buildCache = iArr2;
                this.m_buildCacheStartIndex = 0;
                return;
            }
        }
    }

    public SuballocatedIntVector(int i) {
        this(i, 32);
    }

    public int size() {
        return this.m_firstFree;
    }

    public void setSize(int i) {
        if (this.m_firstFree > i) {
            this.m_firstFree = i;
        }
    }

    public void addElement(int i) {
        int i2 = this.m_firstFree;
        int i3 = i2 - this.m_buildCacheStartIndex;
        if (i3 < 0 || i3 >= this.m_blocksize) {
            int i4 = this.m_firstFree;
            int i5 = i4 >>> this.m_SHIFT;
            int i6 = i4 & this.m_MASK;
            int[][] iArr = this.m_map;
            if (i5 >= iArr.length) {
                int[][] iArr2 = new int[(this.m_numblocks + i5)][];
                System.arraycopy(iArr, 0, iArr2, 0, iArr.length);
                this.m_map = iArr2;
            }
            int[][] iArr3 = this.m_map;
            int[] iArr4 = iArr3[i5];
            if (iArr4 == null) {
                iArr4 = new int[this.m_blocksize];
                iArr3[i5] = iArr4;
            }
            iArr4[i6] = i;
            this.m_buildCache = iArr4;
            int i7 = this.m_firstFree;
            this.m_buildCacheStartIndex = i7 - i6;
            this.m_firstFree = i7 + 1;
            return;
        }
        this.m_buildCache[i3] = i;
        this.m_firstFree = i2 + 1;
    }

    private void addElements(int i, int i2) {
        int i3 = this.m_firstFree;
        if (i3 + i2 < this.m_blocksize) {
            for (int i4 = 0; i4 < i2; i4++) {
                int[] iArr = this.m_map0;
                int i5 = this.m_firstFree;
                this.m_firstFree = i5 + 1;
                iArr[i5] = i;
            }
            return;
        }
        int i6 = i3 >>> this.m_SHIFT;
        int i7 = this.m_MASK & i3;
        this.m_firstFree = i3 + i2;
        while (i2 > 0) {
            int[][] iArr2 = this.m_map;
            if (i6 >= iArr2.length) {
                int[][] iArr3 = new int[(this.m_numblocks + i6)][];
                System.arraycopy(iArr2, 0, iArr3, 0, iArr2.length);
                this.m_map = iArr3;
            }
            int[][] iArr4 = this.m_map;
            int[] iArr5 = iArr4[i6];
            if (iArr5 == null) {
                iArr5 = new int[this.m_blocksize];
                iArr4[i6] = iArr5;
            }
            int i8 = this.m_blocksize;
            int i9 = i8 - i7 < i2 ? i8 - i7 : i2;
            i2 -= i9;
            while (true) {
                int i10 = i9 - 1;
                if (i9 <= 0) {
                    break;
                }
                iArr5[i7] = i;
                i7++;
                i9 = i10;
            }
            i6++;
            i7 = 0;
        }
    }

    private void addElements(int i) {
        int i2 = this.m_firstFree;
        int i3 = i2 + i;
        if (i3 > this.m_blocksize) {
            int i4 = this.m_SHIFT;
            int i5 = i2 >>> i4;
            int i6 = (i2 + i) >>> i4;
            while (true) {
                i5++;
                if (i5 > i6) {
                    break;
                }
                this.m_map[i5] = new int[this.m_blocksize];
            }
        }
        this.m_firstFree = i3;
    }

    private void insertElementAt(int i, int i2) {
        int i3;
        int i4 = this.m_firstFree;
        if (i2 == i4) {
            addElement(i);
        } else if (i2 > i4) {
            int i5 = i2 >>> this.m_SHIFT;
            int[][] iArr = this.m_map;
            if (i5 >= iArr.length) {
                int[][] iArr2 = new int[(this.m_numblocks + i5)][];
                System.arraycopy(iArr, 0, iArr2, 0, iArr.length);
                this.m_map = iArr2;
            }
            int[][] iArr3 = this.m_map;
            int[] iArr4 = iArr3[i5];
            if (iArr4 == null) {
                iArr4 = new int[this.m_blocksize];
                iArr3[i5] = iArr4;
            }
            int i6 = i2 & this.m_MASK;
            iArr4[i6] = i;
            this.m_firstFree = i6 + 1;
        } else {
            int i7 = this.m_SHIFT;
            int i8 = i2 >>> i7;
            int i9 = i4 >>> i7;
            this.m_firstFree = i4 + 1;
            int i10 = i2 & this.m_MASK;
            while (i8 <= i9) {
                int i11 = this.m_blocksize;
                int i12 = (i11 - i10) - 1;
                int[][] iArr5 = this.m_map;
                int[] iArr6 = iArr5[i8];
                if (iArr6 == null) {
                    iArr6 = new int[i11];
                    iArr5[i8] = iArr6;
                    i3 = 0;
                } else {
                    i3 = iArr6[i11 - 1];
                    System.arraycopy(iArr6, i10, iArr6, i10 + 1, i12);
                }
                iArr6[i10] = i;
                i8++;
                i = i3;
                i10 = 0;
            }
        }
    }

    public void removeAllElements() {
        this.m_firstFree = 0;
        this.m_buildCache = this.m_map0;
        this.m_buildCacheStartIndex = 0;
    }

    private boolean removeElement(int i) {
        int indexOf = indexOf(i, 0);
        if (indexOf < 0) {
            return false;
        }
        removeElementAt(indexOf);
        return true;
    }

    private void removeElementAt(int i) {
        int i2 = this.m_firstFree;
        if (i < i2) {
            int i3 = this.m_SHIFT;
            int i4 = i >>> i3;
            int i5 = i2 >>> i3;
            int i6 = i & this.m_MASK;
            while (i4 <= i5) {
                int i7 = this.m_blocksize;
                int i8 = (i7 - i6) - 1;
                int[][] iArr = this.m_map;
                int[] iArr2 = iArr[i4];
                if (iArr2 == null) {
                    iArr2 = new int[i7];
                    iArr[i4] = iArr2;
                } else {
                    System.arraycopy(iArr2, i6 + 1, iArr2, i6, i8);
                }
                if (i4 < i5) {
                    int[] iArr3 = this.m_map[i4 + 1];
                    if (iArr3 != null) {
                        iArr2[this.m_blocksize - 1] = iArr3[0];
                    }
                } else {
                    iArr2[this.m_blocksize - 1] = 0;
                }
                i4++;
                i6 = 0;
            }
        }
        this.m_firstFree--;
    }

    public void setElementAt(int i, int i2) {
        if (i2 < this.m_blocksize) {
            this.m_map0[i2] = i;
        } else {
            int i3 = i2 >>> this.m_SHIFT;
            int i4 = this.m_MASK & i2;
            int[][] iArr = this.m_map;
            if (i3 >= iArr.length) {
                int[][] iArr2 = new int[(this.m_numblocks + i3)][];
                System.arraycopy(iArr, 0, iArr2, 0, iArr.length);
                this.m_map = iArr2;
            }
            int[][] iArr3 = this.m_map;
            int[] iArr4 = iArr3[i3];
            if (iArr4 == null) {
                iArr4 = new int[this.m_blocksize];
                iArr3[i3] = iArr4;
            }
            iArr4[i4] = i;
        }
        if (i2 >= this.m_firstFree) {
            this.m_firstFree = i2 + 1;
        }
    }

    public int elementAt(int i) {
        if (i < this.m_blocksize) {
            return this.m_map0[i];
        }
        return this.m_map[i >>> this.m_SHIFT][this.m_MASK & i];
    }

    private boolean contains(int i) {
        return indexOf(i, 0) >= 0;
    }

    public int indexOf(int i, int i2) {
        int i3 = this.m_firstFree;
        if (i2 >= i3) {
            return -1;
        }
        int i4 = this.m_SHIFT;
        int i5 = i2 & this.m_MASK;
        int i6 = i3 >>> i4;
        for (int i7 = i2 >>> i4; i7 < i6; i7++) {
            int[] iArr = this.m_map[i7];
            if (iArr != null) {
                while (true) {
                    int i8 = this.m_blocksize;
                    if (i5 >= i8) {
                        continue;
                        break;
                    } else if (iArr[i5] == i) {
                        return i5 + (i7 * i8);
                    } else {
                        i5++;
                    }
                }
            }
            i5 = 0;
        }
        int i9 = this.m_firstFree & this.m_MASK;
        int[] iArr2 = this.m_map[i6];
        while (i5 < i9) {
            if (iArr2[i5] == i) {
                return i5 + (i6 * this.m_blocksize);
            }
            i5++;
        }
        return -1;
    }

    public int indexOf(int i) {
        return indexOf(i, 0);
    }

    private int lastIndexOf(int i) {
        int i2 = this.m_firstFree;
        int i3 = this.m_MASK & i2;
        for (int i4 = i2 >>> this.m_SHIFT; i4 >= 0; i4--) {
            int[] iArr = this.m_map[i4];
            if (iArr != null) {
                while (i3 >= 0) {
                    if (iArr[i3] == i) {
                        return i3 + (i4 * this.m_blocksize);
                    }
                    i3--;
                }
                continue;
            }
            i3 = 0;
        }
        return -1;
    }

    public final int[] getMap0() {
        return this.m_map0;
    }

    public final int[][] getMap() {
        return this.m_map;
    }
}
