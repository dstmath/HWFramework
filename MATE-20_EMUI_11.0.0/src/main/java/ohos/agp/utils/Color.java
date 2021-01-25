package ohos.agp.utils;

public class Color {
    public static final Color BLACK = new Color(-16777216);
    public static final Color BLUE = new Color(-16776961);
    public static final Color CYAN = new Color(-16711681);
    public static final Color DKGRAY = new Color(-12303292);
    public static final Color GRAY = new Color(-7829368);
    public static final Color GREEN = new Color(-16711936);
    public static final Color LTGRAY = new Color(-3355444);
    public static final Color MAGENTA = new Color(-65281);
    public static final Color RED = new Color(-65536);
    public static final Color TRANSPARENT = new Color(0);
    public static final Color WHITE = new Color(-1);
    public static final Color YELLOW = new Color(-256);
    private int mColorValue;

    public Color() {
        this(0);
    }

    public Color(int i) {
        this.mColorValue = i;
    }

    public int getValue() {
        return this.mColorValue;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Color)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        return getValue() == ((Color) obj).getValue();
    }

    public int hashCode() {
        return ("Color :" + this.mColorValue).hashCode();
    }
}
