package ohos.agp.colors;

import java.util.HashMap;
import java.util.Locale;
import ohos.dmsdp.sdk.DMSDPConfig;

public final class RgbPalette {
    public static final RgbColor BLACK = new RgbColor(0, 0, 0);
    public static final RgbColor BLUE = new RgbColor(0, 0, 255);
    private static final HashMap<String, Integer> COLOR_NAME_MAP = new HashMap<>();
    public static final RgbColor CYAN = new RgbColor(0, 255, 255);
    public static final RgbColor DARK_GRAY = new RgbColor(68, 68, 68);
    public static final RgbColor GRAY = new RgbColor(136, 136, 136);
    public static final RgbColor GREEN = new RgbColor(0, 255, 0);
    public static final RgbColor LIGHT_GRAY = new RgbColor(204, 204, 204);
    public static final RgbColor MAGENTA = new RgbColor(255, 0, 255);
    public static final RgbColor RED = new RgbColor(255, 0, 0);
    public static final RgbColor TRANSPARENT = new RgbColor(0, 0, 0, 0);
    public static final RgbColor WHITE = new RgbColor(255, 255, 255);
    public static final RgbColor YELLOW = new RgbColor(255, 255, 0);

    private static long getFormatColor(long j) {
        long j2 = 15 & j;
        long j3 = (j2 << 4) + j2;
        long j4 = 240 & j;
        long j5 = j & 3840;
        return (j5 << 12) + (j5 << 8) + (j4 << 8) + (j4 << 4) + j3;
    }

    static {
        COLOR_NAME_MAP.put("black", Integer.valueOf(BLACK.asArgbInt()));
        COLOR_NAME_MAP.put("blue", Integer.valueOf(BLUE.asArgbInt()));
        COLOR_NAME_MAP.put("cyan", Integer.valueOf(CYAN.asArgbInt()));
        COLOR_NAME_MAP.put("gray", Integer.valueOf(GRAY.asArgbInt()));
        COLOR_NAME_MAP.put("green", Integer.valueOf(GREEN.asArgbInt()));
        COLOR_NAME_MAP.put("magenta", Integer.valueOf(MAGENTA.asArgbInt()));
        COLOR_NAME_MAP.put("red", Integer.valueOf(RED.asArgbInt()));
        COLOR_NAME_MAP.put("transparent", Integer.valueOf(TRANSPARENT.asArgbInt()));
        COLOR_NAME_MAP.put("white", Integer.valueOf(WHITE.asArgbInt()));
        COLOR_NAME_MAP.put("yellow", Integer.valueOf(YELLOW.asArgbInt()));
    }

    private RgbPalette() {
    }

    public static int parse(String str) {
        if (str.startsWith(DMSDPConfig.SPLIT)) {
            try {
                long parseLong = Long.parseLong(str.substring(1), 16);
                if (str.length() == 4) {
                    parseLong = getFormatColor(parseLong) | -16777216;
                } else if (str.length() == 5) {
                    long j = 61440 & parseLong;
                    parseLong = (j << 16) + (j << 12) + getFormatColor(parseLong);
                } else if (str.length() == 7) {
                    parseLong |= -16777216;
                } else if (str.length() != 9) {
                    throw new IllegalArgumentException("Unknown color format. Expected formats: #RGB, #ARGB, #RRGGBB or #AARRGGBB.");
                }
                return (int) parseLong;
            } catch (NumberFormatException unused) {
                throw new IllegalArgumentException("Unknown color format. Expected formats: #RGB, #ARGB, #RRGGBB or #AARRGGBB.");
            }
        } else {
            Integer num = COLOR_NAME_MAP.get(str.toLowerCase(Locale.ROOT));
            if (num != null) {
                return num.intValue();
            }
            throw new IllegalArgumentException("Unknown color format. Expected formats: #RGB, #ARGB, #RRGGBB or #AARRGGBB.");
        }
    }
}
