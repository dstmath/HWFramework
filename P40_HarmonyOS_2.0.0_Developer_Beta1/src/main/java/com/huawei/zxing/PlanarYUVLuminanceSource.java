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

    @Override // com.huawei.zxing.LuminanceSource
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

    @Override // com.huawei.zxing.LuminanceSource
    public byte[] getMatrix() {
        int width = getWidth();
        int height = getHeight();
        if (width == this.dataWidth && height == this.dataHeight) {
            return this.yuvData;
        }
        int area = width * height;
        byte[] matrix = new byte[area];
        int i = this.top;
        int i2 = this.dataWidth;
        int inputOffset = (i * i2) + this.left;
        if (width == i2) {
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

    @Override // com.huawei.zxing.LuminanceSource
    public boolean isCropSupported() {
        return true;
    }

    @Override // com.huawei.zxing.LuminanceSource
    public LuminanceSource crop(int left2, int top2, int width, int height) {
        return new PlanarYUVLuminanceSource(this.yuvData, this.dataWidth, this.dataHeight, this.left + left2, this.top + top2, width, height, false);
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
        int rowStart = (this.top * this.dataWidth) + this.left;
        while (y < height) {
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
            rowStart += this.dataWidth;
        }
    }

    @Override // com.huawei.zxing.LuminanceSource
    public boolean isRotateSupported() {
        return true;
    }

    @Override // com.huawei.zxing.LuminanceSource
    public LuminanceSource rotateCounterClockwise() {
        int wh = this.dataWidth * this.dataHeight;
        byte[] yuvRData = new byte[this.yuvData.length];
        int k = 0;
        for (int i = 0; i < this.dataWidth; i++) {
            for (int j = 0; j < this.dataHeight; j++) {
                yuvRData[k] = this.yuvData[(this.dataWidth * j) + i];
                k++;
            }
        }
        int i2 = 0;
        int k2 = k;
        while (true) {
            int i3 = this.dataWidth;
            if (i2 >= i3) {
                return new PlanarYUVLuminanceSource(yuvRData, this.dataHeight, i3, this.top, this.left, this.recHeight, this.recWidth, false);
            }
            for (int j2 = 0; j2 < this.dataHeight / 2; j2++) {
                byte[] bArr = this.yuvData;
                int i4 = this.dataWidth;
                yuvRData[k2] = bArr[(i4 * j2) + wh + i2];
                yuvRData[k2 + 1] = bArr[(i4 * j2) + wh + i2 + 1];
                k2 += 2;
            }
            i2 += 2;
        }
    }
}
