package com.huawei.zxing.common;

public final class BitArray {
    private int[] bits;
    private int size;

    public BitArray() {
        this.size = 0;
        this.bits = new int[1];
    }

    public BitArray(int size2) {
        this.size = size2;
        this.bits = makeArray(size2);
    }

    BitArray(int[] bits2, int size2) {
        this.bits = bits2;
        this.size = size2;
    }

    public int getSize() {
        return this.size;
    }

    public int getSizeInBytes() {
        return (this.size + 7) / 8;
    }

    private void ensureCapacity(int size2) {
        if (size2 > this.bits.length * 32) {
            int[] newBits = makeArray(size2);
            int[] iArr = this.bits;
            System.arraycopy(iArr, 0, newBits, 0, iArr.length);
            this.bits = newBits;
        }
    }

    public boolean get(int i) {
        return (this.bits[i / 32] & (1 << (i & 31))) != 0;
    }

    public void set(int i) {
        int[] iArr = this.bits;
        int i2 = i / 32;
        iArr[i2] = iArr[i2] | (1 << (i & 31));
    }

    public void flip(int i) {
        int[] iArr = this.bits;
        int i2 = i / 32;
        iArr[i2] = iArr[i2] ^ (1 << (i & 31));
    }

    public int getNextSet(int from) {
        int i = this.size;
        if (from >= i) {
            return i;
        }
        int bitsOffset = from / 32;
        int currentBits = this.bits[bitsOffset] & (~((1 << (from & 31)) - 1));
        while (currentBits == 0) {
            bitsOffset++;
            int[] iArr = this.bits;
            if (bitsOffset == iArr.length) {
                return this.size;
            }
            currentBits = iArr[bitsOffset];
        }
        int result = (bitsOffset * 32) + Integer.numberOfTrailingZeros(currentBits);
        int i2 = this.size;
        return result > i2 ? i2 : result;
    }

    public int getNextUnset(int from) {
        int i = this.size;
        if (from >= i) {
            return i;
        }
        int bitsOffset = from / 32;
        int currentBits = (~this.bits[bitsOffset]) & (~((1 << (from & 31)) - 1));
        while (currentBits == 0) {
            bitsOffset++;
            int[] iArr = this.bits;
            if (bitsOffset == iArr.length) {
                return this.size;
            }
            currentBits = ~iArr[bitsOffset];
        }
        int result = (bitsOffset * 32) + Integer.numberOfTrailingZeros(currentBits);
        int i2 = this.size;
        return result > i2 ? i2 : result;
    }

    public void setBulk(int i, int newBits) {
        this.bits[i / 32] = newBits;
    }

    public void setRange(int start, int end) {
        int mask;
        if (end < start) {
            throw new IllegalArgumentException();
        } else if (end != start) {
            int end2 = end - 1;
            int firstInt = start / 32;
            int lastInt = end2 / 32;
            int i = firstInt;
            while (i <= lastInt) {
                int firstBit = i > firstInt ? 0 : start & 31;
                int lastBit = i < lastInt ? 31 : end2 & 31;
                if (firstBit == 0 && lastBit == 31) {
                    mask = -1;
                } else {
                    mask = 0;
                    for (int j = firstBit; j <= lastBit; j++) {
                        mask |= 1 << j;
                    }
                }
                int[] iArr = this.bits;
                iArr[i] = iArr[i] | mask;
                i++;
            }
        }
    }

    public void clear() {
        int max = this.bits.length;
        for (int i = 0; i < max; i++) {
            this.bits[i] = 0;
        }
    }

    public boolean isRange(int start, int end, boolean value) {
        int mask;
        if (end < start) {
            throw new IllegalArgumentException();
        } else if (end == start) {
            return true;
        } else {
            int end2 = end - 1;
            int firstInt = start / 32;
            int lastInt = end2 / 32;
            int i = firstInt;
            while (i <= lastInt) {
                int firstBit = i > firstInt ? 0 : start & 31;
                int lastBit = i < lastInt ? 31 : end2 & 31;
                if (firstBit == 0 && lastBit == 31) {
                    mask = -1;
                } else {
                    mask = 0;
                    for (int j = firstBit; j <= lastBit; j++) {
                        mask |= 1 << j;
                    }
                }
                if ((this.bits[i] & mask) != (value ? mask : 0)) {
                    return false;
                }
                i++;
            }
            return true;
        }
    }

    public void appendBit(boolean bit) {
        ensureCapacity(this.size + 1);
        if (bit) {
            int[] iArr = this.bits;
            int i = this.size;
            int i2 = i / 32;
            iArr[i2] = (1 << (i & 31)) | iArr[i2];
        }
        this.size++;
    }

    public void appendBits(int value, int numBits) {
        if (numBits < 0 || numBits > 32) {
            throw new IllegalArgumentException("Num bits must be between 0 and 32");
        }
        ensureCapacity(this.size + numBits);
        for (int numBitsLeft = numBits; numBitsLeft > 0; numBitsLeft--) {
            boolean z = true;
            if (((value >> (numBitsLeft - 1)) & 1) != 1) {
                z = false;
            }
            appendBit(z);
        }
    }

    public void appendBitArray(BitArray other) {
        int otherSize = other.size;
        ensureCapacity(this.size + otherSize);
        for (int i = 0; i < otherSize; i++) {
            appendBit(other.get(i));
        }
    }

    public void xor(BitArray other) {
        if (this.bits.length == other.bits.length) {
            int i = 0;
            while (true) {
                int[] iArr = this.bits;
                if (i < iArr.length) {
                    iArr[i] = iArr[i] ^ other.bits[i];
                    i++;
                } else {
                    return;
                }
            }
        } else {
            throw new IllegalArgumentException("Sizes don't match");
        }
    }

    public void toBytes(int bitOffset, byte[] array, int offset, int numBytes) {
        for (int i = 0; i < numBytes; i++) {
            int theByte = 0;
            for (int j = 0; j < 8; j++) {
                if (get(bitOffset)) {
                    theByte |= 1 << (7 - j);
                }
                bitOffset++;
            }
            array[offset + i] = (byte) theByte;
        }
    }

    public int[] getBitArray() {
        return this.bits;
    }

    public void reverse() {
        int[] newBits = new int[this.bits.length];
        int len = (this.size - 1) / 32;
        int oldBitsLen = len + 1;
        for (int i = 0; i < oldBitsLen; i++) {
            long x = (long) this.bits[i];
            long x2 = ((x >> 1) & 1431655765) | ((1431655765 & x) << 1);
            long x3 = ((x2 >> 2) & 858993459) | ((858993459 & x2) << 2);
            long x4 = ((x3 >> 4) & 252645135) | ((252645135 & x3) << 4);
            long x5 = ((x4 >> 8) & 16711935) | ((16711935 & x4) << 8);
            newBits[len - i] = (int) (((x5 >> 16) & 65535) | ((65535 & x5) << 16));
        }
        int i2 = this.size;
        if (i2 != oldBitsLen * 32) {
            int leftOffset = (oldBitsLen * 32) - i2;
            int mask = 1;
            for (int i3 = 0; i3 < 31 - leftOffset; i3++) {
                mask = (mask << 1) | 1;
            }
            int currentInt = (newBits[0] >> leftOffset) & mask;
            for (int i4 = 1; i4 < oldBitsLen; i4++) {
                int nextInt = newBits[i4];
                newBits[i4 - 1] = currentInt | (nextInt << (32 - leftOffset));
                currentInt = (nextInt >> leftOffset) & mask;
            }
            newBits[oldBitsLen - 1] = currentInt;
        }
        this.bits = newBits;
    }

    private static int[] makeArray(int size2) {
        return new int[((size2 + 31) / 32)];
    }

    public String toString() {
        StringBuilder result = new StringBuilder(this.size);
        for (int i = 0; i < this.size; i++) {
            if ((i & 7) == 0) {
                result.append(' ');
            }
            result.append(get(i) ? 'X' : '.');
        }
        return result.toString();
    }
}
