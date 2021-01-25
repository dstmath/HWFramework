package ohos.agp.colors;

import java.util.Objects;

public class Color {
    private int mAlpha = 255;
    private ColorSpec mColorSpec = ColorSpec.INVALID;

    protected Color(ColorSpec colorSpec) {
        this.mColorSpec = colorSpec;
    }

    protected Color(Color color) {
        this.mColorSpec = color.getSpec();
        this.mAlpha = color.getAlpha();
    }

    /* access modifiers changed from: protected */
    public Color assign(Color color) {
        this.mColorSpec = color.getSpec();
        this.mAlpha = color.getAlpha();
        return this;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Color)) {
            return false;
        }
        Color color = (Color) obj;
        return this.mColorSpec == color.getSpec() && this.mAlpha == color.getAlpha();
    }

    public int hashCode() {
        return Objects.hash(this.mColorSpec, Integer.valueOf(this.mAlpha));
    }

    public int getAlpha() {
        return this.mAlpha;
    }

    public ColorSpec getSpec() {
        return this.mColorSpec;
    }

    public void setAlpha(int i) {
        this.mAlpha = Math.max(Math.min(i, 255), 0);
    }
}
