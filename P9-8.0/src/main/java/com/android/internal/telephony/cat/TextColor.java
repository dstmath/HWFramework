package com.android.internal.telephony.cat;

public enum TextColor {
    BLACK(0),
    DARK_GRAY(1),
    DARK_RED(2),
    DARK_YELLOW(3),
    DARK_GREEN(4),
    DARK_CYAN(5),
    DARK_BLUE(6),
    DARK_MAGENTA(7),
    GRAY(8),
    WHITE(9),
    BRIGHT_RED(10),
    BRIGHT_YELLOW(11),
    BRIGHT_GREEN(12),
    BRIGHT_CYAN(13),
    BRIGHT_BLUE(14),
    BRIGHT_MAGENTA(15);
    
    private int mValue;

    private TextColor(int value) {
        this.mValue = value;
    }

    public static TextColor fromInt(int value) {
        for (TextColor e : values()) {
            if (e.mValue == value) {
                return e;
            }
        }
        return null;
    }
}
