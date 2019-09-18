package com.huawei.zxing;

public final class InvertedLuminanceSource extends LuminanceSource {
    private final LuminanceSource delegate;

    public InvertedLuminanceSource(LuminanceSource delegate2) {
        super(delegate2.getWidth(), delegate2.getHeight());
        this.delegate = delegate2;
    }

    public byte[] getRow(int y, byte[] row) {
        byte[] row2 = this.delegate.getRow(y, row);
        int width = getWidth();
        for (int i = 0; i < width; i++) {
            row2[i] = (byte) (255 - (row2[i] & 255));
        }
        return row2;
    }

    public byte[] getMatrix() {
        byte[] matrix = this.delegate.getMatrix();
        int length = getWidth() * getHeight();
        byte[] invertedMatrix = new byte[length];
        for (int i = 0; i < length; i++) {
            invertedMatrix[i] = (byte) (255 - (matrix[i] & 255));
        }
        return invertedMatrix;
    }

    public boolean isCropSupported() {
        return this.delegate.isCropSupported();
    }

    public LuminanceSource crop(int left, int top, int width, int height) {
        return new InvertedLuminanceSource(this.delegate.crop(left, top, width, height));
    }

    public boolean isRotateSupported() {
        return this.delegate.isRotateSupported();
    }

    public LuminanceSource invert() {
        return this.delegate;
    }

    public LuminanceSource rotateCounterClockwise() {
        return new InvertedLuminanceSource(this.delegate.rotateCounterClockwise());
    }

    public LuminanceSource rotateCounterClockwise45() {
        return new InvertedLuminanceSource(this.delegate.rotateCounterClockwise45());
    }
}
