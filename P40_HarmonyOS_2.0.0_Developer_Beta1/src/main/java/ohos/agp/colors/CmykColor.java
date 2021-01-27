package ohos.agp.colors;

import java.util.Objects;

public class CmykColor extends Color {
    private float mBlack;
    private float mCyan;
    private float mMagenta;
    private float mYellow;

    public CmykColor() {
        super(ColorSpec.CMYK);
        this.mBlack = 0.0f;
        this.mCyan = 0.0f;
        this.mMagenta = 0.0f;
        this.mYellow = 0.0f;
    }

    public CmykColor(float f, float f2, float f3, float f4) {
        super(ColorSpec.CMYK);
        this.mBlack = 0.0f;
        this.mCyan = 0.0f;
        this.mMagenta = 0.0f;
        this.mYellow = 0.0f;
        setBlack(f4);
        setCyan(f);
        setMagenta(f2);
        setYellow(f3);
    }

    public CmykColor(float f, float f2, float f3, float f4, int i) {
        this(f, f2, f3, f4);
        setAlpha(i);
    }

    public CmykColor(CmykColor cmykColor) {
        super(cmykColor);
        this.mBlack = 0.0f;
        this.mCyan = 0.0f;
        this.mMagenta = 0.0f;
        this.mYellow = 0.0f;
        this.mBlack = cmykColor.getBlack();
        this.mCyan = cmykColor.getCyan();
        this.mMagenta = cmykColor.getMagenta();
        this.mYellow = cmykColor.getYellow();
    }

    public CmykColor assign(CmykColor cmykColor) {
        super.assign((Color) cmykColor);
        this.mBlack = cmykColor.getBlack();
        this.mCyan = cmykColor.getCyan();
        this.mMagenta = cmykColor.getMagenta();
        this.mYellow = cmykColor.getYellow();
        return this;
    }

    @Override // ohos.agp.colors.Color
    public boolean equals(Object obj) {
        if (!super.equals(obj) || !(obj instanceof CmykColor)) {
            return false;
        }
        CmykColor cmykColor = (CmykColor) obj;
        if (Float.compare(cmykColor.getBlack(), this.mBlack) == 0 && Float.compare(cmykColor.getCyan(), this.mCyan) == 0 && Float.compare(cmykColor.getMagenta(), this.mMagenta) == 0 && Float.compare(cmykColor.getYellow(), this.mYellow) == 0) {
            return true;
        }
        return false;
    }

    @Override // ohos.agp.colors.Color
    public int hashCode() {
        return Objects.hash(Integer.valueOf(super.hashCode()), Float.valueOf(this.mBlack), Float.valueOf(this.mCyan), Float.valueOf(this.mMagenta), Float.valueOf(this.mYellow));
    }

    public void setBlack(float f) {
        this.mBlack = Math.max(Math.min(f, 100.0f), 0.0f);
    }

    public float getBlack() {
        return this.mBlack;
    }

    public void setCyan(float f) {
        this.mCyan = Math.max(Math.min(f, 100.0f), 0.0f);
    }

    public float getCyan() {
        return this.mCyan;
    }

    public void setMagenta(float f) {
        this.mMagenta = Math.max(Math.min(f, 100.0f), 0.0f);
    }

    public float getMagenta() {
        return this.mMagenta;
    }

    public void setYellow(float f) {
        this.mYellow = Math.max(Math.min(f, 100.0f), 0.0f);
    }

    public float getYellow() {
        return this.mYellow;
    }
}
