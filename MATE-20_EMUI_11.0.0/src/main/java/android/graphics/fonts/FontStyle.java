package android.graphics.fonts;

import com.android.internal.util.Preconditions;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

public final class FontStyle {
    public static final int FONT_SLANT_ITALIC = 1;
    public static final int FONT_SLANT_UPRIGHT = 0;
    public static final int FONT_WEIGHT_BLACK = 900;
    public static final int FONT_WEIGHT_BOLD = 700;
    public static final int FONT_WEIGHT_EXTRA_BOLD = 800;
    public static final int FONT_WEIGHT_EXTRA_LIGHT = 200;
    public static final int FONT_WEIGHT_LIGHT = 300;
    public static final int FONT_WEIGHT_MAX = 1000;
    public static final int FONT_WEIGHT_MEDIUM = 500;
    public static final int FONT_WEIGHT_MIN = 1;
    public static final int FONT_WEIGHT_NORMAL = 400;
    public static final int FONT_WEIGHT_SEMI_BOLD = 600;
    public static final int FONT_WEIGHT_THIN = 100;
    private static final String TAG = "FontStyle";
    private final int mSlant;
    private final int mWeight;

    @Retention(RetentionPolicy.SOURCE)
    public @interface FontSlant {
    }

    public FontStyle() {
        this.mWeight = 400;
        this.mSlant = 0;
    }

    public FontStyle(int weight, int slant) {
        boolean z = false;
        Preconditions.checkArgument(1 <= weight && weight <= 1000, "weight value must be [1, 1000]");
        Preconditions.checkArgument((slant == 0 || slant == 1) ? true : z, "slant value must be FONT_SLANT_UPRIGHT or FONT_SLANT_UPRIGHT");
        this.mWeight = weight;
        this.mSlant = slant;
    }

    public int getWeight() {
        return this.mWeight;
    }

    public int getSlant() {
        return this.mSlant;
    }

    public int getMatchScore(FontStyle o) {
        return (Math.abs(getWeight() - o.getWeight()) / 100) + (getSlant() == o.getSlant() ? 0 : 2);
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || !(o instanceof FontStyle)) {
            return false;
        }
        FontStyle fontStyle = (FontStyle) o;
        if (fontStyle.mWeight == this.mWeight && fontStyle.mSlant == this.mSlant) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.mWeight), Integer.valueOf(this.mSlant));
    }

    public String toString() {
        return "FontStyle { weight=" + this.mWeight + ", slant=" + this.mSlant + "}";
    }
}
