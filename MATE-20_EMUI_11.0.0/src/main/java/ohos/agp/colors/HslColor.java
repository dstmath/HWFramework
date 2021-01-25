package ohos.agp.colors;

import java.util.Objects;

public class HslColor extends Color {
    private float mHue;
    private float mLightness;
    private float mSaturation;

    public HslColor() {
        super(ColorSpec.HSL);
        this.mHue = 0.0f;
        this.mSaturation = 0.0f;
        this.mLightness = 0.0f;
    }

    public HslColor(float f, float f2, float f3) {
        super(ColorSpec.HSL);
        this.mHue = 0.0f;
        this.mSaturation = 0.0f;
        this.mLightness = 0.0f;
        setHue(f);
        setSaturation(f2);
        setLightness(f3);
    }

    public HslColor(float f, float f2, float f3, int i) {
        this(f, f2, f3);
        setAlpha(i);
    }

    public HslColor(HslColor hslColor) {
        super(hslColor);
        this.mHue = 0.0f;
        this.mSaturation = 0.0f;
        this.mLightness = 0.0f;
        this.mHue = hslColor.getHue();
        this.mSaturation = hslColor.getSaturation();
        this.mLightness = hslColor.getLightness();
    }

    public HslColor assign(HslColor hslColor) {
        super.assign((Color) hslColor);
        this.mHue = hslColor.getHue();
        this.mSaturation = hslColor.getSaturation();
        this.mLightness = hslColor.getLightness();
        return this;
    }

    @Override // ohos.agp.colors.Color
    public boolean equals(Object obj) {
        if (!super.equals(obj) || !(obj instanceof HslColor)) {
            return false;
        }
        HslColor hslColor = (HslColor) obj;
        if (Float.compare(hslColor.getHue(), this.mHue) == 0 && Float.compare(hslColor.getSaturation(), this.mSaturation) == 0 && Float.compare(hslColor.getLightness(), this.mLightness) == 0) {
            return true;
        }
        return false;
    }

    @Override // ohos.agp.colors.Color
    public int hashCode() {
        return Objects.hash(Integer.valueOf(super.hashCode()), Float.valueOf(this.mHue), Float.valueOf(this.mSaturation), Float.valueOf(this.mLightness));
    }

    public void setHue(float f) {
        this.mHue = Math.max(Math.min(f, 360.0f), 0.0f);
    }

    public float getHue() {
        return this.mHue;
    }

    public void setSaturation(float f) {
        this.mSaturation = Math.max(Math.min(f, 100.0f), 0.0f);
    }

    public float getSaturation() {
        return this.mSaturation;
    }

    public void setLightness(float f) {
        this.mLightness = Math.max(Math.min(f, 100.0f), 0.0f);
    }

    public float getLightness() {
        return this.mLightness;
    }
}
