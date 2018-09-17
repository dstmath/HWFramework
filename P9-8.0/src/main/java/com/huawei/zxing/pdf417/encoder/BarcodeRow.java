package com.huawei.zxing.pdf417.encoder;

final class BarcodeRow {
    private int currentLocation = 0;
    private final byte[] row;

    BarcodeRow(int width) {
        this.row = new byte[width];
    }

    void set(int x, byte value) {
        this.row[x] = value;
    }

    void set(int x, boolean black) {
        this.row[x] = (byte) (black ? 1 : 0);
    }

    void addBar(boolean black, int width) {
        for (int ii = 0; ii < width; ii++) {
            int i = this.currentLocation;
            this.currentLocation = i + 1;
            set(i, black);
        }
    }

    byte[] getScaledRow(int scale) {
        byte[] output = new byte[(this.row.length * scale)];
        for (int i = 0; i < output.length; i++) {
            output[i] = this.row[i / scale];
        }
        return output;
    }
}
