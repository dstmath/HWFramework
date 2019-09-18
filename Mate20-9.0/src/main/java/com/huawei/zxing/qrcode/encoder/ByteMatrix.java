package com.huawei.zxing.qrcode.encoder;

import java.lang.reflect.Array;

public final class ByteMatrix {
    private final byte[][] bytes;
    private final int height;
    private final int width;

    public ByteMatrix(int width2, int height2) {
        this.bytes = (byte[][]) Array.newInstance(byte.class, new int[]{height2, width2});
        this.width = width2;
        this.height = height2;
    }

    public int getHeight() {
        return this.height;
    }

    public int getWidth() {
        return this.width;
    }

    public byte get(int x, int y) {
        return this.bytes[y][x];
    }

    public byte[][] getArray() {
        return this.bytes;
    }

    public void set(int x, int y, byte value) {
        this.bytes[y][x] = value;
    }

    public void set(int x, int y, int value) {
        this.bytes[y][x] = (byte) value;
    }

    public void set(int x, int y, boolean value) {
        this.bytes[y][x] = value ? (byte) 1 : 0;
    }

    public void clear(byte value) {
        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                this.bytes[y][x] = value;
            }
        }
    }

    public String toString() {
        StringBuilder result = new StringBuilder((this.width * 2 * this.height) + 2);
        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                switch (this.bytes[y][x]) {
                    case 0:
                        result.append(" 0");
                        break;
                    case 1:
                        result.append(" 1");
                        break;
                    default:
                        result.append("  ");
                        break;
                }
            }
            result.append(10);
        }
        return result.toString();
    }
}
