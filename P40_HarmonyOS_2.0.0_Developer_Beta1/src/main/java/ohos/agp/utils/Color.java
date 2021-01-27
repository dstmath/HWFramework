package ohos.agp.utils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import ohos.dmsdp.sdk.DMSDPConfig;

public class Color {
    public static final Color BLACK = new Color(COLOR_ALPHA_TAG);
    public static final Color BLUE = new Color(-16776961);
    private static final int COLOR_ALPHA_TAG = -16777216;
    private static final Map<String, Integer> COLOR_NAME_MAP = new HashMap();
    public static final Color CYAN = new Color(-16711681);
    public static final Color DKGRAY = new Color(-12303292);
    private static final int EIGHT = 8;
    public static final Color GRAY = new Color(-7829368);
    public static final Color GREEN = new Color(-16711936);
    public static final Color LTGRAY = new Color(-3355444);
    public static final Color MAGENTA = new Color(-65281);
    private static final int NINE = 9;
    public static final Color RED = new Color(-65536);
    private static final int SEVEN = 7;
    private static final int SIXTEEN = 16;
    public static final Color TRANSPARENT = new Color(0);
    private static final int TWENTY_FOUR = 24;
    public static final Color WHITE = new Color(-1);
    public static final Color YELLOW = new Color(-256);
    private int mColorValue;

    public static int alpha(int i) {
        return i >>> 24;
    }

    public static int argb(int i, int i2, int i3, int i4) {
        return (i << 24) | (i2 << 16) | (i3 << 8) | i4;
    }

    public static int rgb(int i, int i2, int i3) {
        return (i << 16) | COLOR_ALPHA_TAG | (i2 << 8) | i3;
    }

    static {
        COLOR_NAME_MAP.put("black", Integer.valueOf((int) COLOR_ALPHA_TAG));
        COLOR_NAME_MAP.put("darkgray", -12303292);
        COLOR_NAME_MAP.put("gray", -7829368);
        COLOR_NAME_MAP.put("lightgray", -3355444);
        COLOR_NAME_MAP.put("white", -1);
        COLOR_NAME_MAP.put("red", -65536);
        COLOR_NAME_MAP.put("green", -16711936);
        COLOR_NAME_MAP.put("blue", -16776961);
        COLOR_NAME_MAP.put("yellow", -256);
        COLOR_NAME_MAP.put("cyan", -16711681);
        COLOR_NAME_MAP.put("magenta", -65281);
        COLOR_NAME_MAP.put("aqua", -16711681);
        COLOR_NAME_MAP.put("fuchsia", -65281);
        COLOR_NAME_MAP.put("darkgrey", -12303292);
        COLOR_NAME_MAP.put("grey", -7829368);
        COLOR_NAME_MAP.put("lightgrey", -3355444);
        COLOR_NAME_MAP.put("lime", -16711936);
        COLOR_NAME_MAP.put("maroon", -8388608);
        COLOR_NAME_MAP.put("navy", -16777088);
        COLOR_NAME_MAP.put("olive", -8355840);
        COLOR_NAME_MAP.put("purple", -8388480);
        COLOR_NAME_MAP.put("silver", -4144960);
        COLOR_NAME_MAP.put("teal", -16744320);
    }

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

    public static int getIntColor(String str) {
        if (str == null || str.equals("")) {
            throw new IllegalArgumentException("Unknown color !");
        } else if (str.startsWith(DMSDPConfig.SPLIT)) {
            long parseLong = Long.parseLong(str.substring(1), 16);
            if (str.length() == 7) {
                parseLong |= -16777216;
            } else if (str.length() != 9) {
                throw new IllegalArgumentException("Unknown color !");
            }
            return (int) parseLong;
        } else {
            Integer num = COLOR_NAME_MAP.get(str.toLowerCase(Locale.ROOT));
            if (num != null) {
                return num.intValue();
            }
            throw new IllegalArgumentException("Unknown color !");
        }
    }
}
