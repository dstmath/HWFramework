package ohos.agp.colors;

import java.util.Objects;

public class HsvColor extends Color {
    private float mHue;
    private float mSaturation;
    private float mValue;

    public HsvColor() {
        super(ColorSpec.HSV);
        this.mHue = 0.0f;
        this.mSaturation = 0.0f;
        this.mValue = 0.0f;
    }

    public HsvColor(float f, float f2, float f3) {
        super(ColorSpec.HSV);
        this.mHue = 0.0f;
        this.mSaturation = 0.0f;
        this.mValue = 0.0f;
        setHue(f);
        setSaturation(f2);
        setValue(f3);
    }

    public HsvColor(float f, float f2, float f3, int i) {
        this(f, f2, f3);
        setAlpha(i);
    }

    public HsvColor(HsvColor hsvColor) {
        super(hsvColor);
        this.mHue = 0.0f;
        this.mSaturation = 0.0f;
        this.mValue = 0.0f;
        this.mHue = hsvColor.getHue();
        this.mSaturation = hsvColor.getSaturation();
        this.mValue = hsvColor.getValue();
    }

    public HsvColor assign(HsvColor hsvColor) {
        super.assign((Color) hsvColor);
        this.mHue = hsvColor.getHue();
        this.mSaturation = hsvColor.getSaturation();
        this.mValue = hsvColor.getValue();
        return this;
    }

    @Override // ohos.agp.colors.Color
    public boolean equals(Object obj) {
        if (!super.equals(obj) || !(obj instanceof HsvColor)) {
            return false;
        }
        HsvColor hsvColor = (HsvColor) obj;
        if (Float.compare(hsvColor.getHue(), this.mHue) == 0 && Float.compare(hsvColor.getSaturation(), this.mSaturation) == 0 && Float.compare(hsvColor.getValue(), this.mValue) == 0) {
            return true;
        }
        return false;
    }

    @Override // ohos.agp.colors.Color
    public int hashCode() {
        return Objects.hash(Integer.valueOf(super.hashCode()), Float.valueOf(this.mHue), Float.valueOf(this.mSaturation), Float.valueOf(this.mValue));
    }

    public void setHue(float f) {
        this.mHue = Math.max(Math.min(f, 360.0f), 0.0f);
    }

    public float getHue() {
        return this.mHue;
    }

    public void setValue(float f) {
        this.mValue = Math.max(Math.min(f, 100.0f), 0.0f);
    }

    public float getValue() {
        return this.mValue;
    }

    public void setSaturation(float f) {
        this.mSaturation = Math.max(Math.min(f, 100.0f), 0.0f);
    }

    public float getSaturation() {
        return this.mSaturation;
    }

    public static int toColor(int i, float f, float f2, float f3) {
        RgbColor rgb = ColorConverter.toRgb(new HsvColor(f, f2, f3));
        rgb.setAlpha(i);
        return rgb.asArgbInt();
    }

    public static HsvColor toHSV(int i) {
        return ColorConverter.toHsv(RgbColor.fromArgbInt(i));
    }
}
