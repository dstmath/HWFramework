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
        int[] bitArray = row.getBitArray();
        int[] iArr = this.bits;
        int i = this.rowSize;
        System.arraycopy(bitArray, 0, iArr, y * i, i);
    }

    /* JADX INFO: Multiple debug info for r4v2 int: [D('width' int), D('y' int)] */
    public int[] getEnclosingRectangle() {
        int left = this.width;
        int top = this.height;
        int right = -1;
        int bottom = -1;
        for (int y = 0; y < this.height; y++) {
            int x32 = 0;
            while (true) {
                int i = this.rowSize;
                if (x32 >= i) {
                    break;
                }
                int theBits = this.bits[(i * y) + x32];
                if (theBits != 0) {
                    if (y < top) {
                        top = y;
                    }
                    if (y > bottom) {
                        bottom = y;
                    }
                    if (x32 * 32 < left) {
                        int bit = 0;
                        while ((theBits << (31 - bit)) == 0) {
                            bit++;
                        }
                        if ((x32 * 32) + bit < left) {
                            left = (x32 * 32) + bit;
                        }
                    }
                    if ((x32 * 32) + 31 > right) {
                        int bit2 = 31;
                        while ((theBits >>> bit2) == 0) {
                            bit2--;
                        }
                        if ((x32 * 32) + bit2 > right) {
                            right = (x32 * 32) + bit2;
                        }
                    }
                }
                x32++;
            }
        }
        int y2 = right - left;
        int height2 = bottom - top;
        if (y2 < 0 || height2 < 0) {
            return null;
        }
        return new int[]{left, top, y2, height2};
    }

    public int[] getTopLeftOnBit() {
        int bitsOffset = 0;
        while (true) {
            int[] iArr = this.bits;
            if (bitsOffset >= iArr.length || iArr[bitsOffset] != 0) {
                int[] iArr2 = this.bits;
            } else {
                bitsOffset++;
            }
        }
        int[] iArr22 = this.bits;
        if (bitsOffset == iArr22.length) {
            return null;
        }
        int i = this.rowSize;
        int y = bitsOffset / i;
        int x = (bitsOffset % i) << 5;
        int bit = 0;
        while ((iArr22[bitsOffset] << (31 - bit)) == 0) {
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
        int i = this.rowSize;
        int y = bitsOffset / i;
        int x = (bitsOffset % i) << 5;
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
        int i = 0;
        while (true) {
            int[] iArr = this.bits;
            if (i >= iArr.length) {
                return true;
            }
            if (iArr[i] != other.bits[i]) {
                return false;
            }
            i++;
        }
    }

    public int hashCode() {
        int hash = (((((this.width * 31) + this.width) * 31) + this.height) * 31) + this.rowSize;
        for (int bit : this.bits) {
            hash = (hash * 31) + bit;
        }
        return hash;
    }

    public String toString() {
        StringBuilder result = new StringBuilder(this.height * (this.width + 1));
        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                result.append(get(x, y) ? "X " : "  ");
            }
            result.append('\n');
        }
        return result.toString();
    }
}
