package ohos.agp.colors;

import java.util.Objects;

public class RgbColor extends Color {
    private int mBlue;
    private int mGreen;
    private int mRed;

    public RgbColor() {
        super(ColorSpec.RGB);
        this.mRed = 0;
        this.mGreen = 0;
        this.mBlue = 0;
    }

    public RgbColor(int i) {
        super(ColorSpec.RGB);
        this.mRed = 0;
        this.mGreen = 0;
        this.mBlue = 0;
        setRed((i >> 24) & 255);
        setGreen((i >> 16) & 255);
        setBlue((i >> 8) & 255);
        setAlpha(i & 255);
    }

    public RgbColor(int i, int i2, int i3) {
        super(ColorSpec.RGB);
        this.mRed = 0;
        this.mGreen = 0;
        this.mBlue = 0;
        setRed(i);
        setGreen(i2);
        setBlue(i3);
    }

    public RgbColor(int i, int i2, int i3, int i4) {
        this(i, i2, i3);
        setAlpha(i4);
    }

    public RgbColor(RgbColor rgbColor) {
        super(rgbColor);
        this.mRed = 0;
        this.mGreen = 0;
        this.mBlue = 0;
        this.mRed = rgbColor.getRed();
        this.mGreen = rgbColor.getGreen();
        this.mBlue = rgbColor.getBlue();
    }

    public RgbColor assign(RgbColor rgbColor) {
        super.assign((Color) rgbColor);
        this.mRed = rgbColor.getRed();
        this.mGreen = rgbColor.getGreen();
        this.mBlue = rgbColor.getBlue();
        return this;
    }

    @Override // ohos.agp.colors.Color
    public boolean equals(Object obj) {
        if (!super.equals(obj) || !(obj instanceof RgbColor)) {
            return false;
        }
        RgbColor rgbColor = (RgbColor) obj;
        if (this.mRed == rgbColor.getRed() && this.mGreen == rgbColor.getGreen() && this.mBlue == rgbColor.getBlue()) {
            return true;
        }
        return false;
    }

    @Override // ohos.agp.colors.Color
    public int hashCode() {
        return Objects.hash(Integer.valueOf(super.hashCode()), Integer.valueOf(this.mRed), Integer.valueOf(this.mGreen), Integer.valueOf(this.mBlue));
    }

    public int asRgbaInt() {
        return getAlpha() | (this.mRed << 24) | (this.mGreen << 16) | (this.mBlue << 8);
    }

    public int asArgbInt() {
        return this.mBlue | (getAlpha() << 24) | (this.mRed << 16) | (this.mGreen << 8);
    }

    public static RgbColor fromRgbaInt(int i) {
        return new RgbColor(i);
    }

    public static RgbColor fromArgbInt(int i) {
        return new RgbColor((i >> 16) & 255, (i >> 8) & 255, i & 255, (i >> 24) & 255);
    }

    public void setRed(int i) {
        this.mRed = Math.max(Math.min(i, 255), 0);
    }

    public int getRed() {
        return this.mRed;
    }

    public void setGreen(int i) {
        this.mGreen = Math.max(Math.min(i, 255), 0);
    }

    public int getGreen() {
        return this.mGreen;
    }

    public void setBlue(int i) {
        this.mBlue = Math.max(Math.min(i, 255), 0);
    }

    public int getBlue() {
        return this.mBlue;
    }
}
