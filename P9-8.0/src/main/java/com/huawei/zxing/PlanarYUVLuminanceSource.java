package com.huawei.zxing;

import android.cover.CoverManager;

public final class PlanarYUVLuminanceSource extends LuminanceSource {
    private static final int THUMBNAIL_SCALE_FACTOR = 2;
    private final int dataHeight;
    private final int dataWidth;
    private final int left;
    private final int recHeight;
    private final int recWidth;
    private final int top;
    private final byte[] yuvData;

    public PlanarYUVLuminanceSource(byte[] yuvData, int dataWidth, int dataHeight, int left, int top, int width, int height, boolean reverseHorizontal) {
        super(width, height);
        if (left + width > dataWidth || top + height > dataHeight) {
            throw new IllegalArgumentException("Crop rectangle does not fit within image data.");
        }
        this.yuvData = yuvData;
        this.dataWidth = dataWidth;
        this.dataHeight = dataHeight;
        this.left = left;
        this.top = top;
        if (reverseHorizontal) {
            reverseHorizontal(width, height);
        }
        this.recWidth = width;
        this.recHeight = height;
    }

    public byte[] getRow(int y, byte[] row) {
        if (y < 0 || y >= getHeight()) {
            throw new IllegalArgumentException("Requested row is outside the image: " + y);
        }
        int width = getWidth();
        if (row == null || row.length < width) {
            row = new byte[width];
        }
        System.arraycopy(this.yuvData, ((this.top + y) * this.dataWidth) + this.left, row, 0, width);
        return row;
    }

    public byte[] getMatrix() {
        int width = getWidth();
        int height = getHeight();
        if (width == this.dataWidth && height == this.dataHeight) {
            return this.yuvData;
        }
        int area = width * height;
        byte[] matrix = new byte[area];
        int inputOffset = (this.top * this.dataWidth) + this.left;
        if (width == this.dataWidth) {
            System.arraycopy(this.yuvData, inputOffset, matrix, 0, area);
            return matrix;
        }
        byte[] yuv = this.yuvData;
        for (int y = 0; y < height; y++) {
            System.arraycopy(yuv, inputOffset, matrix, y * width, width);
            inputOffset += this.dataWidth;
        }
        return matrix;
    }

    public boolean isCropSupported() {
        return true;
    }

    public LuminanceSource crop(int left, int top, int width, int height) {
        return new PlanarYUVLuminanceSource(this.yuvData, this.dataWidth, this.dataHeight, this.left + left, this.top + top, width, height, false);
    }

    public int[] renderThumbnail() {
        int width = getWidth() / 2;
        int height = getHeight() / 2;
        int[] pixels = new int[(width * height)];
        byte[] yuv = this.yuvData;
        int inputOffset = (this.top * this.dataWidth) + this.left;
        for (int y = 0; y < height; y++) {
            int outputOffset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[outputOffset + x] = (65793 * (yuv[(x * 2) + inputOffset] & 255)) | CoverManager.DEFAULT_COLOR;
            }
            inputOffset += this.dataWidth * 2;
        }
        return pixels;
    }

    public int getThumbnailWidth() {
        return getWidth() / 2;
    }

    public int getThumbnailHeight() {
        return getHeight() / 2;
    }

    private void reverseHorizontal(int width, int height) {
        byte[] yuvData = this.yuvData;
        int y = 0;
        int rowStart = (this.top * this.dataWidth) + this.left;
        while (y < height) {
            int middle = rowStart + (width / 2);
            int x1 = rowStart;
            int x2 = (rowStart + width) - 1;
            while (x1 < middle) {
                byte temp = yuvData[x1];
                yuvData[x1] = yuvData[x2];
                yuvData[x2] = temp;
                x1++;
                x2--;
            }
            y++;
            rowStart += this.dataWidth;
        }
    }

    public boolean isRotateSupported() {
        return true;
    }

    public LuminanceSource rotateCounterClockwise() {
        int i;
        int j;
        int wh = this.dataWidth * this.dataHeight;
        byte[] yuvRData = new byte[this.yuvData.length];
        int k = 0;
        for (i = 0; i < this.dataWidth; i++) {
            for (j = 0; j < this.dataHeight; j++) {
                yuvRData[k] = this.yuvData[(this.dataWidth * j) + i];
                k++;
            }
        }
        for (i = 0; i < this.dataWidth; i += 2) {
            for (j = 0; j < this.dataHeight / 2; j++) {
                yuvRData[k] = this.yuvData[((this.dataWidth * j) + wh) + i];
                yuvRData[k + 1] = this.yuvData[(((this.dataWidth * j) + wh) + i) + 1];
                k += 2;
            }
        }
        return new PlanarYUVLuminanceSource(yuvRData, this.dataHeight, this.dataWidth, this.top, this.left, this.recHeight, this.recWidth, false);
    }
}
