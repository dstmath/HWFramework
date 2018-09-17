package org.apache.xml.utils;

import org.apache.xml.dtm.DTMFilter;

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
        this(DTMFilter.SHOW_NOTATION);
    }

    public SuballocatedIntVector(int blocksize, int numblocks) {
        this.m_numblocks = 32;
        this.m_firstFree = 0;
        this.m_SHIFT = 0;
        while (true) {
            blocksize >>>= 1;
            if (blocksize != 0) {
                this.m_SHIFT++;
            } else {
                this.m_blocksize = 1 << this.m_SHIFT;
                this.m_MASK = this.m_blocksize - 1;
                this.m_numblocks = numblocks;
                this.m_map0 = new int[this.m_blocksize];
                this.m_map = new int[numblocks][];
                this.m_map[0] = this.m_map0;
                this.m_buildCache = this.m_map0;
                this.m_buildCacheStartIndex = 0;
                return;
            }
        }
    }

    public SuballocatedIntVector(int blocksize) {
        this(blocksize, 32);
    }

    public int size() {
        return this.m_firstFree;
    }

    public void setSize(int sz) {
        if (this.m_firstFree > sz) {
            this.m_firstFree = sz;
        }
    }

    public void addElement(int value) {
        int indexRelativeToCache = this.m_firstFree - this.m_buildCacheStartIndex;
        if (indexRelativeToCache < 0 || indexRelativeToCache >= this.m_blocksize) {
            int index = this.m_firstFree >>> this.m_SHIFT;
            int offset = this.m_firstFree & this.m_MASK;
            if (index >= this.m_map.length) {
                int[][] newMap = new int[(index + this.m_numblocks)][];
                System.arraycopy(this.m_map, 0, newMap, 0, this.m_map.length);
                this.m_map = newMap;
            }
            int[] block = this.m_map[index];
            if (block == null) {
                block = new int[this.m_blocksize];
                this.m_map[index] = block;
            }
            block[offset] = value;
            this.m_buildCache = block;
            this.m_buildCacheStartIndex = this.m_firstFree - offset;
            this.m_firstFree++;
            return;
        }
        this.m_buildCache[indexRelativeToCache] = value;
        this.m_firstFree++;
    }

    private void addElements(int value, int numberOfElements) {
        if (this.m_firstFree + numberOfElements < this.m_blocksize) {
            for (int i = 0; i < numberOfElements; i++) {
                int[] iArr = this.m_map0;
                int i2 = this.m_firstFree;
                this.m_firstFree = i2 + 1;
                iArr[i2] = value;
            }
            return;
        }
        int index = this.m_firstFree >>> this.m_SHIFT;
        int offset = this.m_firstFree & this.m_MASK;
        this.m_firstFree += numberOfElements;
        while (numberOfElements > 0) {
            if (index >= this.m_map.length) {
                int[][] newMap = new int[(index + this.m_numblocks)][];
                System.arraycopy(this.m_map, 0, newMap, 0, this.m_map.length);
                this.m_map = newMap;
            }
            int[] block = this.m_map[index];
            if (block == null) {
                block = new int[this.m_blocksize];
                this.m_map[index] = block;
            }
            int i3 = this.m_blocksize - offset < numberOfElements ? this.m_blocksize - offset : numberOfElements;
            numberOfElements -= i3;
            while (true) {
                int copied = i3;
                int offset2 = offset;
                i3 = copied - 1;
                if (copied <= 0) {
                    break;
                }
                offset = offset2 + 1;
                block[offset2] = value;
            }
            index++;
            offset = 0;
        }
    }

    private void addElements(int numberOfElements) {
        int newlen = this.m_firstFree + numberOfElements;
        if (newlen > this.m_blocksize) {
            int newindex = (this.m_firstFree + numberOfElements) >>> this.m_SHIFT;
            for (int i = (this.m_firstFree >>> this.m_SHIFT) + 1; i <= newindex; i++) {
                this.m_map[i] = new int[this.m_blocksize];
            }
        }
        this.m_firstFree = newlen;
    }

    private void insertElementAt(int value, int at) {
        int index;
        int[] block;
        int offset;
        if (at == this.m_firstFree) {
            addElement(value);
        } else if (at > this.m_firstFree) {
            index = at >>> this.m_SHIFT;
            if (index >= this.m_map.length) {
                int[][] newMap = new int[(index + this.m_numblocks)][];
                System.arraycopy(this.m_map, 0, newMap, 0, this.m_map.length);
                this.m_map = newMap;
            }
            block = this.m_map[index];
            if (block == null) {
                block = new int[this.m_blocksize];
                this.m_map[index] = block;
            }
            offset = at & this.m_MASK;
            block[offset] = value;
            this.m_firstFree = offset + 1;
        } else {
            int maxindex = this.m_firstFree >>> this.m_SHIFT;
            this.m_firstFree++;
            offset = at & this.m_MASK;
            for (index = at >>> this.m_SHIFT; index <= maxindex; index++) {
                int push;
                int copylen = (this.m_blocksize - offset) - 1;
                block = this.m_map[index];
                if (block == null) {
                    push = 0;
                    block = new int[this.m_blocksize];
                    this.m_map[index] = block;
                } else {
                    push = block[this.m_blocksize - 1];
                    System.arraycopy(block, offset, block, offset + 1, copylen);
                }
                block[offset] = value;
                value = push;
                offset = 0;
            }
        }
    }

    public void removeAllElements() {
        this.m_firstFree = 0;
        this.m_buildCache = this.m_map0;
        this.m_buildCacheStartIndex = 0;
    }

    private boolean removeElement(int s) {
        int at = indexOf(s, 0);
        if (at < 0) {
            return false;
        }
        removeElementAt(at);
        return true;
    }

    private void removeElementAt(int at) {
        if (at < this.m_firstFree) {
            int maxindex = this.m_firstFree >>> this.m_SHIFT;
            int offset = at & this.m_MASK;
            for (int index = at >>> this.m_SHIFT; index <= maxindex; index++) {
                int copylen = (this.m_blocksize - offset) - 1;
                int[] block = this.m_map[index];
                if (block == null) {
                    block = new int[this.m_blocksize];
                    this.m_map[index] = block;
                } else {
                    System.arraycopy(block, offset + 1, block, offset, copylen);
                }
                if (index < maxindex) {
                    int[] next = this.m_map[index + 1];
                    if (next != null) {
                        int i;
                        int i2 = this.m_blocksize - 1;
                        if (next != null) {
                            i = next[0];
                        } else {
                            i = 0;
                        }
                        block[i2] = i;
                    }
                } else {
                    block[this.m_blocksize - 1] = 0;
                }
                offset = 0;
            }
        }
        this.m_firstFree--;
    }

    public void setElementAt(int value, int at) {
        if (at < this.m_blocksize) {
            this.m_map0[at] = value;
        } else {
            int index = at >>> this.m_SHIFT;
            int offset = at & this.m_MASK;
            if (index >= this.m_map.length) {
                int[][] newMap = new int[(index + this.m_numblocks)][];
                System.arraycopy(this.m_map, 0, newMap, 0, this.m_map.length);
                this.m_map = newMap;
            }
            int[] block = this.m_map[index];
            if (block == null) {
                block = new int[this.m_blocksize];
                this.m_map[index] = block;
            }
            block[offset] = value;
        }
        if (at >= this.m_firstFree) {
            this.m_firstFree = at + 1;
        }
    }

    public int elementAt(int i) {
        if (i < this.m_blocksize) {
            return this.m_map0[i];
        }
        return this.m_map[i >>> this.m_SHIFT][this.m_MASK & i];
    }

    private boolean contains(int s) {
        return indexOf(s, 0) >= 0;
    }

    public int indexOf(int elem, int index) {
        if (index >= this.m_firstFree) {
            return -1;
        }
        int[] block;
        int offset;
        int boffset = index & this.m_MASK;
        int maxindex = this.m_firstFree >>> this.m_SHIFT;
        for (int bindex = index >>> this.m_SHIFT; bindex < maxindex; bindex++) {
            block = this.m_map[bindex];
            if (block != null) {
                for (offset = boffset; offset < this.m_blocksize; offset++) {
                    if (block[offset] == elem) {
                        return (this.m_blocksize * bindex) + offset;
                    }
                }
                continue;
            }
            boffset = 0;
        }
        int maxoffset = this.m_firstFree & this.m_MASK;
        block = this.m_map[maxindex];
        for (offset = boffset; offset < maxoffset; offset++) {
            if (block[offset] == elem) {
                return (this.m_blocksize * maxindex) + offset;
            }
        }
        return -1;
    }

    public int indexOf(int elem) {
        return indexOf(elem, 0);
    }

    private int lastIndexOf(int elem) {
        int boffset = this.m_firstFree & this.m_MASK;
        for (int index = this.m_firstFree >>> this.m_SHIFT; index >= 0; index--) {
            int[] block = this.m_map[index];
            if (block != null) {
                for (int offset = boffset; offset >= 0; offset--) {
                    if (block[offset] == elem) {
                        return (this.m_blocksize * index) + offset;
                    }
                }
                continue;
            }
            boffset = 0;
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
