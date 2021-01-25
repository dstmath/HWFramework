package ohos.agp.colors;

import java.util.Objects;

public abstract class UserDefinedColor extends Color {
    public abstract void fromRgb(RgbColor rgbColor);

    public abstract RgbColor toRgb();

    public UserDefinedColor() {
        super(ColorSpec.USER_DEFINED);
    }

    public UserDefinedColor(UserDefinedColor userDefinedColor) {
        super(ColorSpec.USER_DEFINED);
        assign(userDefinedColor);
    }

    @Override // ohos.agp.colors.Color
    public boolean equals(Object obj) {
        if (obj instanceof Color) {
            return ColorConverter.toRgb((Color) obj).equals(toRgb());
        }
        return false;
    }

    @Override // ohos.agp.colors.Color
    public int hashCode() {
        return Objects.hash(Integer.valueOf(super.hashCode()), Integer.valueOf(toRgb().getRed()), Integer.valueOf(toRgb().getGreen()), Integer.valueOf(toRgb().getBlue()));
    }

    public UserDefinedColor assign(UserDefinedColor userDefinedColor) {
        fromRgb(userDefinedColor.toRgb());
        return this;
    }
}
