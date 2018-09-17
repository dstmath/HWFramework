package com.huawei.zxing.qrcode.decoder;

import com.huawei.zxing.common.BitMatrix;

abstract class DataMask {
    private static final DataMask[] DATA_MASKS = new DataMask[]{new DataMask000(), new DataMask001(), new DataMask010(), new DataMask011(), new DataMask100(), new DataMask101(), new DataMask110(), new DataMask111()};

    private static final class DataMask000 extends DataMask {
        /* synthetic */ DataMask000(DataMask000 -this0) {
            this();
        }

        private DataMask000() {
            super();
        }

        boolean isMasked(int i, int j) {
            return ((i + j) & 1) == 0;
        }
    }

    private static final class DataMask001 extends DataMask {
        /* synthetic */ DataMask001(DataMask001 -this0) {
            this();
        }

        private DataMask001() {
            super();
        }

        boolean isMasked(int i, int j) {
            return (i & 1) == 0;
        }
    }

    private static final class DataMask010 extends DataMask {
        /* synthetic */ DataMask010(DataMask010 -this0) {
            this();
        }

        private DataMask010() {
            super();
        }

        boolean isMasked(int i, int j) {
            return j % 3 == 0;
        }
    }

    private static final class DataMask011 extends DataMask {
        /* synthetic */ DataMask011(DataMask011 -this0) {
            this();
        }

        private DataMask011() {
            super();
        }

        boolean isMasked(int i, int j) {
            return (i + j) % 3 == 0;
        }
    }

    private static final class DataMask100 extends DataMask {
        /* synthetic */ DataMask100(DataMask100 -this0) {
            this();
        }

        private DataMask100() {
            super();
        }

        boolean isMasked(int i, int j) {
            return (((i >>> 1) + (j / 3)) & 1) == 0;
        }
    }

    private static final class DataMask101 extends DataMask {
        /* synthetic */ DataMask101(DataMask101 -this0) {
            this();
        }

        private DataMask101() {
            super();
        }

        boolean isMasked(int i, int j) {
            int temp = i * j;
            if ((temp & 1) + (temp % 3) == 0) {
                return true;
            }
            return false;
        }
    }

    private static final class DataMask110 extends DataMask {
        /* synthetic */ DataMask110(DataMask110 -this0) {
            this();
        }

        private DataMask110() {
            super();
        }

        boolean isMasked(int i, int j) {
            int temp = i * j;
            if ((((temp & 1) + (temp % 3)) & 1) == 0) {
                return true;
            }
            return false;
        }
    }

    private static final class DataMask111 extends DataMask {
        /* synthetic */ DataMask111(DataMask111 -this0) {
            this();
        }

        private DataMask111() {
            super();
        }

        boolean isMasked(int i, int j) {
            return ((((i + j) & 1) + ((i * j) % 3)) & 1) == 0;
        }
    }

    /* synthetic */ DataMask(DataMask -this0) {
        this();
    }

    abstract boolean isMasked(int i, int i2);

    private DataMask() {
    }

    final void unmaskBitMatrix(BitMatrix bits, int dimension) {
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                if (isMasked(i, j)) {
                    bits.flip(j, i);
                }
            }
        }
    }

    static DataMask forReference(int reference) {
        if (reference >= 0 && reference <= 7) {
            return DATA_MASKS[reference];
        }
        throw new IllegalArgumentException();
    }
}
