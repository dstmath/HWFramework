package com.huawei.zxing;

public final class PlanarYUVLuminanceSource extends LuminanceSource {
    private static final int THUMBNAIL_SCALE_FACTOR = 2;
    private final int dataHeight;
    private final int dataWidth;
    private final int left;
    private final int recHeight;
    private final int recWidth;
    private final int top;
    private final byte[] yuvData;

    public PlanarYUVLuminanceSource(byte[] yuvData2, int dataWidth2, int dataHeight2, int left2, int top2, int width, int height, boolean reverseHorizontal) {
        super(width, height);
        if (left2 + width > dataWidth2 || top2 + height > dataHeight2) {
            throw new IllegalArgumentException("Crop rectangle does not fit within image data.");
        }
        this.yuvData = yuvData2;
        this.dataWidth = dataWidth2;
        this.dataHeight = dataHeight2;
        this.left = left2;
        this.top = top2;
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

    public LuminanceSource crop(int left2, int top2, int width, int height) {
        PlanarYUVLuminanceSource planarYUVLuminanceSource = new PlanarYUVLuminanceSource(this.yuvData, this.dataWidth, this.dataHeight, this.left + left2, this.top + top2, width, height, false);
        return planarYUVLuminanceSource;
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
                pixels[outputOffset + x] = -16777216 | (65793 * (yuv[(x * 2) + inputOffset] & 255));
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
        byte[] yuvData2 = this.yuvData;
        int y = 0;
        int rowStart = this.top * this.dataWidth;
        int i = this.left;
        while (true) {
            rowStart += i;
            if (y < height) {
                int middle = (width / 2) + rowStart;
                int x1 = rowStart;
                int x2 = (rowStart + width) - 1;
                while (x1 < middle) {
                    byte temp = yuvData2[x1];
                    yuvData2[x1] = yuvData2[x2];
                    yuvData2[x2] = temp;
                    x1++;
                    x2--;
                }
                y++;
                i = this.dataWidth;
            } else {
                return;
            }
        }
    }

    public boolean isRotateSupported() {
        return true;
    }

    public LuminanceSource rotateCounterClockwise() {
        int wh = this.dataWidth * this.dataHeight;
        byte[] yuvRData = new byte[this.yuvData.length];
        int k = 0;
        int i = 0;
        while (i < this.dataWidth) {
            int k2 = k;
            for (int j = 0; j < this.dataHeight; j++) {
                yuvRData[k2] = this.yuvData[(this.dataWidth * j) + i];
                k2++;
            }
            i++;
            k = k2;
        }
        int k3 = k;
        for (int i2 = 0; i2 < this.dataWidth; i2 += 2) {
            for (int j2 = 0; j2 < this.dataHeight / 2; j2++) {
                yuvRData[k3] = this.yuvData[(this.dataWidth * j2) + wh + i2];
                yuvRData[k3 + 1] = this.yuvData[(this.dataWidth * j2) + wh + i2 + 1];
                k3 += 2;
            }
        }
        PlanarYUVLuminanceSource planarYUVLuminanceSource = new PlanarYUVLuminanceSource(yuvRData, this.dataHeight, this.dataWidth, this.top, this.left, this.recHeight, this.recWidth, false);
        return planarYUVLuminanceSource;
    }
}
