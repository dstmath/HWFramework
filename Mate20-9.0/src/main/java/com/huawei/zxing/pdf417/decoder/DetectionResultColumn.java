package com.huawei.zxing.pdf417.decoder;

import java.util.Formatter;

class DetectionResultColumn {
    private static final int MAX_NEARBY_DISTANCE = 5;
    private final BoundingBox boundingBox;
    private final Codeword[] codewords;

    DetectionResultColumn(BoundingBox boundingBox2) {
        this.boundingBox = new BoundingBox(boundingBox2);
        this.codewords = new Codeword[((boundingBox2.getMaxY() - boundingBox2.getMinY()) + 1)];
    }

    /* access modifiers changed from: package-private */
    public final Codeword getCodewordNearby(int imageRow) {
        Codeword codeword = getCodeword(imageRow);
        if (codeword != null) {
            return codeword;
        }
        for (int i = 1; i < 5; i++) {
            int nearImageRow = imageRowToCodewordIndex(imageRow) - i;
            if (nearImageRow >= 0) {
                Codeword codeword2 = this.codewords[nearImageRow];
                if (codeword2 != null) {
                    return codeword2;
                }
            }
            int nearImageRow2 = imageRowToCodewordIndex(imageRow) + i;
            if (nearImageRow2 < this.codewords.length) {
                Codeword codeword3 = this.codewords[nearImageRow2];
                if (codeword3 != null) {
                    return codeword3;
                }
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public final int imageRowToCodewordIndex(int imageRow) {
        return imageRow - this.boundingBox.getMinY();
    }

    /* access modifiers changed from: package-private */
    public final void setCodeword(int imageRow, Codeword codeword) {
        this.codewords[imageRowToCodewordIndex(imageRow)] = codeword;
    }

    /* access modifiers changed from: package-private */
    public final Codeword getCodeword(int imageRow) {
        return this.codewords[imageRowToCodewordIndex(imageRow)];
    }

    /* access modifiers changed from: package-private */
    public final BoundingBox getBoundingBox() {
        return this.boundingBox;
    }

    /* access modifiers changed from: package-private */
    public final Codeword[] getCodewords() {
        return this.codewords;
    }

    public String toString() {
        Formatter formatter = new Formatter();
        int row = 0;
        for (Codeword codeword : this.codewords) {
            if (codeword == null) {
                formatter.format("%3d:    |   \n", new Object[]{Integer.valueOf(row)});
                row++;
            } else {
                formatter.format("%3d: %3d|%3d\n", new Object[]{Integer.valueOf(row), Integer.valueOf(codeword.getRowNumber()), Integer.valueOf(codeword.getValue())});
                row++;
            }
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }
}
