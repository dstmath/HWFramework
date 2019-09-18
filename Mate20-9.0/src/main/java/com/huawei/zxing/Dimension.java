package com.huawei.zxing;

public final class Dimension {
    private final int height;
    private final int width;

    public Dimension(int width2, int height2) {
        if (width2 < 0 || height2 < 0) {
            throw new IllegalArgumentException();
        }
        this.width = width2;
        this.height = height2;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public boolean equals(Object other) {
        boolean z = false;
        if (!(other instanceof Dimension)) {
            return false;
        }
        Dimension d = (Dimension) other;
        if (this.width == d.width && this.height == d.height) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return (this.width * 32713) + this.height;
    }

    public String toString() {
        return this.width + "x" + this.height;
    }
}
