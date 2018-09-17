package com.android.internal.telephony.cat;

public enum TextAlignment {
    LEFT(0),
    CENTER(1),
    RIGHT(2),
    DEFAULT(3);
    
    private int mValue;

    private TextAlignment(int value) {
        this.mValue = value;
    }

    public static TextAlignment fromInt(int value) {
        for (TextAlignment e : values()) {
            if (e.mValue == value) {
                return e;
            }
        }
        return null;
    }
}
