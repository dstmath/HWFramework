package com.huawei.zxing.datamatrix.encoder;

import java.util.Arrays;

public class DefaultPlacement {
    private final byte[] bits;
    private final CharSequence codewords;
    private final int numcols;
    private final int numrows;

    public DefaultPlacement(CharSequence codewords, int numcols, int numrows) {
        this.codewords = codewords;
        this.numcols = numcols;
        this.numrows = numrows;
        this.bits = new byte[(numcols * numrows)];
        Arrays.fill(this.bits, (byte) -1);
    }

    final int getNumrows() {
        return this.numrows;
    }

    final int getNumcols() {
        return this.numcols;
    }

    final byte[] getBits() {
        return this.bits;
    }

    public final boolean getBit(int col, int row) {
        return this.bits[(this.numcols * row) + col] == (byte) 1;
    }

    final void setBit(int col, int row, boolean bit) {
        this.bits[(this.numcols * row) + col] = bit ? (byte) 1 : (byte) 0;
    }

    final boolean hasBit(int col, int row) {
        return this.bits[(this.numcols * row) + col] >= (byte) 0;
    }

    public final void place() {
        int pos = 0;
        int row = 4;
        int col = 0;
        while (true) {
            int pos2;
            if (row == this.numrows && col == 0) {
                pos2 = pos + 1;
                corner1(pos);
                pos = pos2;
            }
            if (row == this.numrows - 2 && col == 0 && this.numcols % 4 != 0) {
                pos2 = pos + 1;
                corner2(pos);
                pos = pos2;
            }
            if (row == this.numrows - 2 && col == 0 && this.numcols % 8 == 4) {
                pos2 = pos + 1;
                corner3(pos);
                pos = pos2;
            }
            if (row == this.numrows + 4 && col == 2 && this.numcols % 8 == 0) {
                pos2 = pos + 1;
                corner4(pos);
                pos = pos2;
            }
            do {
                if (row < this.numrows && col >= 0 && (hasBit(col, row) ^ 1) != 0) {
                    pos2 = pos + 1;
                    utah(row, col, pos);
                    pos = pos2;
                }
                row -= 2;
                col += 2;
                if (row < 0) {
                    break;
                }
            } while (col < this.numcols);
            row++;
            col += 3;
            pos2 = pos;
            while (true) {
                if (row < 0 || col >= this.numcols) {
                    pos = pos2;
                } else if ((hasBit(col, row) ^ 1) != 0) {
                    pos = pos2 + 1;
                    utah(row, col, pos2);
                } else {
                    pos = pos2;
                }
                row += 2;
                col -= 2;
                if (row >= this.numrows || col < 0) {
                    row += 3;
                    col++;
                } else {
                    pos2 = pos;
                }
            }
            row += 3;
            col++;
            if (row >= this.numrows && col >= this.numcols) {
                break;
            }
        }
        if (!hasBit(this.numcols - 1, this.numrows - 1)) {
            setBit(this.numcols - 1, this.numrows - 1, true);
            setBit(this.numcols - 2, this.numrows - 2, true);
        }
    }

    private void module(int row, int col, int pos, int bit) {
        boolean z = true;
        if (row < 0) {
            row += this.numrows;
            col += 4 - ((this.numrows + 4) % 8);
        }
        if (col < 0) {
            col += this.numcols;
            row += 4 - ((this.numcols + 4) % 8);
        }
        if ((this.codewords.charAt(pos) & (1 << (8 - bit))) == 0) {
            z = false;
        }
        setBit(col, row, z);
    }

    private void utah(int row, int col, int pos) {
        module(row - 2, col - 2, pos, 1);
        module(row - 2, col - 1, pos, 2);
        module(row - 1, col - 2, pos, 3);
        module(row - 1, col - 1, pos, 4);
        module(row - 1, col, pos, 5);
        module(row, col - 2, pos, 6);
        module(row, col - 1, pos, 7);
        module(row, col, pos, 8);
    }

    private void corner1(int pos) {
        module(this.numrows - 1, 0, pos, 1);
        module(this.numrows - 1, 1, pos, 2);
        module(this.numrows - 1, 2, pos, 3);
        module(0, this.numcols - 2, pos, 4);
        module(0, this.numcols - 1, pos, 5);
        module(1, this.numcols - 1, pos, 6);
        module(2, this.numcols - 1, pos, 7);
        module(3, this.numcols - 1, pos, 8);
    }

    private void corner2(int pos) {
        module(this.numrows - 3, 0, pos, 1);
        module(this.numrows - 2, 0, pos, 2);
        module(this.numrows - 1, 0, pos, 3);
        module(0, this.numcols - 4, pos, 4);
        module(0, this.numcols - 3, pos, 5);
        module(0, this.numcols - 2, pos, 6);
        module(0, this.numcols - 1, pos, 7);
        module(1, this.numcols - 1, pos, 8);
    }

    private void corner3(int pos) {
        module(this.numrows - 3, 0, pos, 1);
        module(this.numrows - 2, 0, pos, 2);
        module(this.numrows - 1, 0, pos, 3);
        module(0, this.numcols - 2, pos, 4);
        module(0, this.numcols - 1, pos, 5);
        module(1, this.numcols - 1, pos, 6);
        module(2, this.numcols - 1, pos, 7);
        module(3, this.numcols - 1, pos, 8);
    }

    private void corner4(int pos) {
        module(this.numrows - 1, 0, pos, 1);
        module(this.numrows - 1, this.numcols - 1, pos, 2);
        module(0, this.numcols - 3, pos, 3);
        module(0, this.numcols - 2, pos, 4);
        module(0, this.numcols - 1, pos, 5);
        module(1, this.numcols - 3, pos, 6);
        module(1, this.numcols - 2, pos, 7);
        module(1, this.numcols - 1, pos, 8);
    }
}
