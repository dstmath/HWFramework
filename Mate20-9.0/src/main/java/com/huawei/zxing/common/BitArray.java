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
            System.arraycopy(this.bits, 0, newBits, 0, this.bits.length);
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
        if (from >= this.size) {
            return this.size;
        }
        int bitsOffset = from / 32;
        int currentBits = this.bits[bitsOffset] & (~((1 << (from & 31)) - 1));
        while (currentBits == 0) {
            bitsOffset++;
            if (bitsOffset == this.bits.length) {
                return this.size;
            }
            currentBits = this.bits[bitsOffset];
        }
        int result = (bitsOffset * 32) + Integer.numberOfTrailingZeros(currentBits);
        return result > this.size ? this.size : result;
    }

    public int getNextUnset(int from) {
        if (from >= this.size) {
            return this.size;
        }
        int bitsOffset = from / 32;
        int currentBits = (~this.bits[bitsOffset]) & (~((1 << (from & 31)) - 1));
        while (currentBits == 0) {
            bitsOffset++;
            if (bitsOffset == this.bits.length) {
                return this.size;
            }
            currentBits = ~this.bits[bitsOffset];
        }
        int result = (bitsOffset * 32) + Integer.numberOfTrailingZeros(currentBits);
        return result > this.size ? this.size : result;
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
                    int mask2 = 0;
                    for (int j = firstBit; j <= lastBit; j++) {
                        mask2 |= 1 << j;
                    }
                    mask = mask2;
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
                    int mask2 = 0;
                    for (int j = firstBit; j <= lastBit; j++) {
                        mask2 |= 1 << j;
                    }
                    mask = mask2;
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
            int i = this.size / 32;
            iArr[i] = iArr[i] | (1 << (this.size & 31));
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
            for (int i = 0; i < this.bits.length; i++) {
                int[] iArr = this.bits;
                iArr[i] = iArr[i] ^ other.bits[i];
            }
            return;
        }
        throw new IllegalArgumentException("Sizes don't match");
    }

    public void toBytes(int bitOffset, byte[] array, int offset, int numBytes) {
        int bitOffset2 = bitOffset;
        int i = 0;
        while (i < numBytes) {
            int theByte = 0;
            int bitOffset3 = bitOffset2;
            for (int j = 0; j < 8; j++) {
                if (get(bitOffset3)) {
                    theByte |= 1 << (7 - j);
                }
                bitOffset3++;
            }
            array[offset + i] = (byte) theByte;
            i++;
            bitOffset2 = bitOffset3;
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
        if (this.size != oldBitsLen * 32) {
            int leftOffset = (oldBitsLen * 32) - this.size;
            int mask = 1;
            for (int i2 = 0; i2 < 31 - leftOffset; i2++) {
                mask = (mask << 1) | 1;
            }
            int currentInt = (newBits[0] >> leftOffset) & mask;
            for (int i3 = 1; i3 < oldBitsLen; i3++) {
                int nextInt = newBits[i3];
                newBits[i3 - 1] = currentInt | (nextInt << (32 - leftOffset));
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
