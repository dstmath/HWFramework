package com.huawei.zxing.common;

public final class BitMatrix {
    private final int[] bits;
    private final int height;
    private final int rowSize;
    private final int width;

    public BitMatrix(int dimension) {
        this(dimension, dimension);
    }

    public BitMatrix(int width2, int height2) {
        if (width2 < 1 || height2 < 1) {
            throw new IllegalArgumentException("Both dimensions must be greater than 0");
        }
        this.width = width2;
        this.height = height2;
        this.rowSize = (width2 + 31) >> 5;
        this.bits = new int[(this.rowSize * height2)];
    }

    public boolean get(int x, int y) {
        return ((this.bits[(this.rowSize * y) + (x >> 5)] >>> (x & 31)) & 1) != 0;
    }

    public void set(int x, int y) {
        int offset = (this.rowSize * y) + (x >> 5);
        int[] iArr = this.bits;
        iArr[offset] = iArr[offset] | (1 << (x & 31));
    }

    public void flip(int x, int y) {
        int offset = (this.rowSize * y) + (x >> 5);
        int[] iArr = this.bits;
        iArr[offset] = iArr[offset] ^ (1 << (x & 31));
    }

    public void clear() {
        int max = this.bits.length;
        for (int i = 0; i < max; i++) {
            this.bits[i] = 0;
        }
    }

    public void setRegion(int left, int top, int width2, int height2) {
        if (top < 0 || left < 0) {
            throw new IllegalArgumentException("Left and top must be nonnegative");
        } else if (height2 < 1 || width2 < 1) {
            throw new IllegalArgumentException("Height and width must be at least 1");
        } else {
            int right = left + width2;
            int bottom = top + height2;
            if (bottom > this.height || right > this.width) {
                throw new IllegalArgumentException("The region must fit inside the matrix");
            }
            for (int y = top; y < bottom; y++) {
                int offset = this.rowSize * y;
                for (int x = left; x < right; x++) {
                    int[] iArr = this.bits;
                    int i = (x >> 5) + offset;
                    iArr[i] = iArr[i] | (1 << (x & 31));
                }
            }
        }
    }

    public BitArray getRow(int y, BitArray row) {
        if (row == null || row.getSize() < this.width) {
            row = new BitArray(this.width);
        }
        int offset = this.rowSize * y;
        for (int x = 0; x < this.rowSize; x++) {
            row.setBulk(x << 5, this.bits[offset + x]);
        }
        return row;
    }

    public void setRow(int y, BitArray row) {
        System.arraycopy(row.getBitArray(), 0, this.bits, this.rowSize * y, this.rowSize);
    }

    public int[] getEnclosingRectangle() {
        int bit;
        int left = this.width;
        int left2 = -1;
        int right = -1;
        int top = this.height;
        int top2 = left;
        int y = 0;
        while (y < this.height) {
            int bottom = right;
            int right2 = left2;
            int left3 = top2;
            for (int x32 = 0; x32 < this.rowSize; x32++) {
                int theBits = this.bits[(this.rowSize * y) + x32];
                if (theBits != 0) {
                    if (y < top) {
                        top = y;
                    }
                    if (y > bottom) {
                        bottom = y;
                    }
                    int bit2 = 31;
                    if (x32 * 32 < left3) {
                        int bit3 = 0;
                        while ((theBits << (31 - bit3)) == 0) {
                            bit3++;
                        }
                        if ((x32 * 32) + bit3 < left3) {
                            left3 = (x32 * 32) + bit3;
                        }
                    }
                    if ((x32 * 32) + 31 > right2) {
                        while (true) {
                            bit = bit2;
                            if ((theBits >>> bit) != 0) {
                                break;
                            }
                            bit2 = bit - 1;
                        }
                        if ((x32 * 32) + bit > right2) {
                            right2 = (x32 * 32) + bit;
                        }
                    }
                }
            }
            y++;
            top2 = left3;
            left2 = right2;
            right = bottom;
        }
        int width2 = left2 - top2;
        int height2 = right - top;
        if (width2 < 0 || height2 < 0) {
            return null;
        }
        return new int[]{top2, top, width2, height2};
    }

    public int[] getTopLeftOnBit() {
        int bitsOffset = 0;
        while (bitsOffset < this.bits.length && this.bits[bitsOffset] == 0) {
            bitsOffset++;
        }
        if (bitsOffset == this.bits.length) {
            return null;
        }
        int y = bitsOffset / this.rowSize;
        int x = (bitsOffset % this.rowSize) << 5;
        int bit = 0;
        while ((this.bits[bitsOffset] << (31 - bit)) == 0) {
            bit++;
        }
        return new int[]{x + bit, y};
    }

    public int[] getBottomRightOnBit() {
        int bitsOffset = this.bits.length - 1;
        while (bitsOffset >= 0 && this.bits[bitsOffset] == 0) {
            bitsOffset--;
        }
        if (bitsOffset < 0) {
            return null;
        }
        int y = bitsOffset / this.rowSize;
        int x = (bitsOffset % this.rowSize) << 5;
        int bit = 31;
        while ((this.bits[bitsOffset] >>> bit) == 0) {
            bit--;
        }
        return new int[]{x + bit, y};
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public boolean equals(Object o) {
        if (!(o instanceof BitMatrix)) {
            return false;
        }
        BitMatrix other = (BitMatrix) o;
        if (this.width != other.width || this.height != other.height || this.rowSize != other.rowSize || this.bits.length != other.bits.length) {
            return false;
        }
        for (int i = 0; i < this.bits.length; i++) {
            if (this.bits[i] != other.bits[i]) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        int hash = (31 * ((31 * ((31 * this.width) + this.width)) + this.height)) + this.rowSize;
        for (int bit : this.bits) {
            hash = (31 * hash) + bit;
        }
        return hash;
    }

    public String toString() {
        StringBuilder result = new StringBuilder(this.height * (this.width + 1));
        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                result.append(get(x, y) ? "X " : "  ");
            }
            result.append(10);
        }
        return result.toString();
    }
}
