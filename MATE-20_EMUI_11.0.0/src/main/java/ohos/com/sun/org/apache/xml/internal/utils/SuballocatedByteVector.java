package ohos.com.sun.org.apache.xml.internal.utils;

public class SuballocatedByteVector {
    protected int m_blocksize;
    protected int m_firstFree;
    protected byte[][] m_map;
    protected byte[] m_map0;
    protected int m_numblocks;

    public SuballocatedByteVector() {
        this(2048);
    }

    public SuballocatedByteVector(int i) {
        this.m_numblocks = 32;
        this.m_firstFree = 0;
        this.m_blocksize = i;
        this.m_map0 = new byte[i];
        this.m_map = new byte[this.m_numblocks][];
        this.m_map[0] = this.m_map0;
    }

    public SuballocatedByteVector(int i, int i2) {
        this(i);
    }

    public int size() {
        return this.m_firstFree;
    }

    private void setSize(int i) {
        if (this.m_firstFree < i) {
            this.m_firstFree = i;
        }
    }

    public void addElement(byte b) {
        int i = this.m_firstFree;
        int i2 = this.m_blocksize;
        if (i < i2) {
            byte[] bArr = this.m_map0;
            this.m_firstFree = i + 1;
            bArr[i] = b;
            return;
        }
        int i3 = i / i2;
        int i4 = i % i2;
        this.m_firstFree = i + 1;
        byte[][] bArr2 = this.m_map;
        if (i3 >= bArr2.length) {
            byte[][] bArr3 = new byte[(this.m_numblocks + i3)][];
            System.arraycopy(bArr2, 0, bArr3, 0, bArr2.length);
            this.m_map = bArr3;
        }
        byte[][] bArr4 = this.m_map;
        byte[] bArr5 = bArr4[i3];
        if (bArr5 == null) {
            bArr5 = new byte[this.m_blocksize];
            bArr4[i3] = bArr5;
        }
        bArr5[i4] = b;
    }

    private void addElements(byte b, int i) {
        int i2 = this.m_firstFree;
        int i3 = i2 + i;
        int i4 = this.m_blocksize;
        if (i3 < i4) {
            for (int i5 = 0; i5 < i; i5++) {
                byte[] bArr = this.m_map0;
                int i6 = this.m_firstFree;
                this.m_firstFree = i6 + 1;
                bArr[i6] = b;
            }
            return;
        }
        int i7 = i2 / i4;
        int i8 = i2 % i4;
        this.m_firstFree = i2 + i;
        while (i > 0) {
            byte[][] bArr2 = this.m_map;
            if (i7 >= bArr2.length) {
                byte[][] bArr3 = new byte[(this.m_numblocks + i7)][];
                System.arraycopy(bArr2, 0, bArr3, 0, bArr2.length);
                this.m_map = bArr3;
            }
            byte[][] bArr4 = this.m_map;
            byte[] bArr5 = bArr4[i7];
            if (bArr5 == null) {
                bArr5 = new byte[this.m_blocksize];
                bArr4[i7] = bArr5;
            }
            int i9 = this.m_blocksize;
            int i10 = i9 - i8 < i ? i9 - i8 : i;
            i -= i10;
            while (true) {
                int i11 = i10 - 1;
                if (i10 <= 0) {
                    break;
                }
                bArr5[i8] = b;
                i8++;
                i10 = i11;
            }
            i7++;
            i8 = 0;
        }
    }

    private void addElements(int i) {
        int i2 = this.m_firstFree;
        int i3 = i2 + i;
        int i4 = this.m_blocksize;
        if (i3 > i4) {
            int i5 = i2 % i4;
            int i6 = (i2 + i) % i4;
            while (true) {
                i5++;
                if (i5 > i6) {
                    break;
                }
                this.m_map[i5] = new byte[this.m_blocksize];
            }
        }
        this.m_firstFree = i3;
    }

    private void insertElementAt(byte b, int i) {
        byte b2;
        int i2 = this.m_firstFree;
        if (i == i2) {
            addElement(b);
        } else if (i > i2) {
            int i3 = i / this.m_blocksize;
            byte[][] bArr = this.m_map;
            if (i3 >= bArr.length) {
                byte[][] bArr2 = new byte[(this.m_numblocks + i3)][];
                System.arraycopy(bArr, 0, bArr2, 0, bArr.length);
                this.m_map = bArr2;
            }
            byte[][] bArr3 = this.m_map;
            byte[] bArr4 = bArr3[i3];
            if (bArr4 == null) {
                bArr4 = new byte[this.m_blocksize];
                bArr3[i3] = bArr4;
            }
            int i4 = i % this.m_blocksize;
            bArr4[i4] = b;
            this.m_firstFree = i4 + 1;
        } else {
            int i5 = this.m_blocksize;
            int i6 = i / i5;
            int i7 = (1 / i5) + i2;
            this.m_firstFree = i2 + 1;
            int i8 = i % i5;
            while (i6 <= i7) {
                int i9 = this.m_blocksize;
                int i10 = (i9 - i8) - 1;
                byte[][] bArr5 = this.m_map;
                byte[] bArr6 = bArr5[i6];
                if (bArr6 == null) {
                    bArr6 = new byte[i9];
                    bArr5[i6] = bArr6;
                    b2 = 0;
                } else {
                    b2 = bArr6[i9 - 1];
                    System.arraycopy(bArr6, i8, bArr6, i8 + 1, i10);
                }
                bArr6[i8] = b;
                i6++;
                b = b2;
                i8 = 0;
            }
        }
    }

    public void removeAllElements() {
        this.m_firstFree = 0;
    }

    private boolean removeElement(byte b) {
        int indexOf = indexOf(b, 0);
        if (indexOf < 0) {
            return false;
        }
        removeElementAt(indexOf);
        return true;
    }

    private void removeElementAt(int i) {
        int i2 = this.m_firstFree;
        if (i < i2) {
            int i3 = this.m_blocksize;
            int i4 = i / i3;
            int i5 = i2 / i3;
            int i6 = i % i3;
            while (i4 <= i5) {
                int i7 = this.m_blocksize;
                int i8 = (i7 - i6) - 1;
                byte[][] bArr = this.m_map;
                byte[] bArr2 = bArr[i4];
                if (bArr2 == null) {
                    bArr2 = new byte[i7];
                    bArr[i4] = bArr2;
                } else {
                    System.arraycopy(bArr2, i6 + 1, bArr2, i6, i8);
                }
                if (i4 < i5) {
                    byte[] bArr3 = this.m_map[i4 + 1];
                    if (bArr3 != null) {
                        bArr2[this.m_blocksize - 1] = bArr3[0];
                    }
                } else {
                    bArr2[this.m_blocksize - 1] = 0;
                }
                i4++;
                i6 = 0;
            }
        }
        this.m_firstFree--;
    }

    public void setElementAt(byte b, int i) {
        int i2 = this.m_blocksize;
        if (i < i2) {
            this.m_map0[i] = b;
            return;
        }
        int i3 = i / i2;
        int i4 = i % i2;
        byte[][] bArr = this.m_map;
        if (i3 >= bArr.length) {
            byte[][] bArr2 = new byte[(this.m_numblocks + i3)][];
            System.arraycopy(bArr, 0, bArr2, 0, bArr.length);
            this.m_map = bArr2;
        }
        byte[][] bArr3 = this.m_map;
        byte[] bArr4 = bArr3[i3];
        if (bArr4 == null) {
            bArr4 = new byte[this.m_blocksize];
            bArr3[i3] = bArr4;
        }
        bArr4[i4] = b;
        if (i >= this.m_firstFree) {
            this.m_firstFree = i + 1;
        }
    }

    public byte elementAt(int i) {
        int i2 = this.m_blocksize;
        if (i < i2) {
            return this.m_map0[i];
        }
        return this.m_map[i / i2][i % i2];
    }

    private boolean contains(byte b) {
        return indexOf(b, 0) >= 0;
    }

    public int indexOf(byte b, int i) {
        int i2 = this.m_firstFree;
        if (i >= i2) {
            return -1;
        }
        int i3 = this.m_blocksize;
        int i4 = i % i3;
        int i5 = i2 / i3;
        for (int i6 = i / i3; i6 < i5; i6++) {
            byte[] bArr = this.m_map[i6];
            if (bArr != null) {
                while (true) {
                    int i7 = this.m_blocksize;
                    if (i4 >= i7) {
                        continue;
                        break;
                    } else if (bArr[i4] == b) {
                        return i4 + (i6 * i7);
                    } else {
                        i4++;
                    }
                }
            }
            i4 = 0;
        }
        int i8 = this.m_firstFree % this.m_blocksize;
        byte[] bArr2 = this.m_map[i5];
        while (i4 < i8) {
            if (bArr2[i4] == b) {
                return i4 + (i5 * this.m_blocksize);
            }
            i4++;
        }
        return -1;
    }

    public int indexOf(byte b) {
        return indexOf(b, 0);
    }

    private int lastIndexOf(byte b) {
        int i = this.m_firstFree;
        int i2 = this.m_blocksize;
        int i3 = i % i2;
        for (int i4 = i / i2; i4 >= 0; i4--) {
            byte[] bArr = this.m_map[i4];
            if (bArr != null) {
                while (i3 >= 0) {
                    if (bArr[i3] == b) {
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
}
