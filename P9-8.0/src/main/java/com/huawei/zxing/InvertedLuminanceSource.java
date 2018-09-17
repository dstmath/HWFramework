package com.huawei.zxing;

public final class InvertedLuminanceSource extends LuminanceSource {
    private final LuminanceSource delegate;

    public InvertedLuminanceSource(LuminanceSource delegate) {
        super(delegate.getWidth(), delegate.getHeight());
        this.delegate = delegate;
    }

    public byte[] getRow(int y, byte[] row) {
        row = this.delegate.getRow(y, row);
        int width = getWidth();
        for (int i = 0; i < width; i++) {
            row[i] = (byte) (255 - (row[i] & 255));
        }
        return row;
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
